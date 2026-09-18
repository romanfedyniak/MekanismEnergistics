package com.mekeng.github.common.me.inventory;

import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.util.EnumFacing;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTankInfo;

import appeng.api.behaviors.GenericInternalInventory;
import appeng.api.behaviors.GenericSlotCapacities;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;

import com.mekeng.github.common.me.AEGasKey;
import com.mekeng.github.common.me.AEGasKeyType;

/**
 * The gas handler of an AE2UD machine's stock: its gas slots, as tanks. Built by AE2UD's
 * {@code GenericInventoryAdapters}, once per machine, and shared by every tube that asks.
 * <p>
 * The same rules as AE2UD's fluid handler: gas pushed in goes to the network first and only what the network
 * refuses stays in a slot; drawing is from the slots alone; a slot holding something that is not a gas reads
 * as an empty tank and takes nothing. Every side is the same side.
 */
public class GenericStackGasHandler implements IExtendedGasHandler {

    protected final GenericInternalInventory inv;
    private final Supplier<IStorageService> network;
    private final IActionSource source;

    public GenericStackGasHandler(final GenericInternalInventory inv, final Supplier<IStorageService> network,
            final IActionSource source) {
        this.inv = inv;
        this.network = network;
        this.source = source;
    }

    @Override
    public int receiveGas(final EnumFacing side, final GasStack stack, final boolean doTransfer) {
        final AEGasKey key = AEGasKey.of(stack);
        if (key == null || stack.amount <= 0) {
            return 0;
        }

        final Actionable mode = doTransfer ? Actionable.MODULATE : Actionable.SIMULATE;
        final IStorageService storage = this.network.get();
        long received = storage == null ? 0 : storage.getInventory().insert(key, stack.amount, mode, this.source);

        for (int slot = 0; slot < this.inv.size() && received < stack.amount; slot++) {
            if (this.acceptsGasIn(slot)) {
                received += this.inv.insert(slot, key, stack.amount - received, mode);
            }
        }
        return (int) received;
    }

    /**
     * Untyped, so it hands over whichever gas comes first - the same answer to a simulation and to the real
     * call that follows it, which is what a tube relies on.
     */
    @Nullable
    @Override
    public GasStack drawGas(final EnumFacing side, final int amount, final boolean doTransfer) {
        if (amount <= 0) {
            return null;
        }
        for (int slot = 0; slot < this.inv.size(); slot++) {
            if (this.inv.getKey(slot) instanceof AEGasKey key) {
                return this.drawGas(side, key.toStack(amount), doTransfer);
            }
        }
        return null;
    }

    @Nullable
    @Override
    public GasStack drawGas(final EnumFacing side, final GasStack stack, final boolean doTransfer) {
        final AEGasKey key = AEGasKey.of(stack);
        if (key == null || stack.amount <= 0) {
            return null;
        }

        final long drawn = this.inv.extract(key, stack.amount,
                doTransfer ? Actionable.MODULATE : Actionable.SIMULATE);
        return drawn <= 0 ? null : key.toStack((int) drawn);
    }

    @Override
    public boolean canReceiveGas(final EnumFacing side, final Gas type) {
        return type != null && this.receiveGas(side, new GasStack(type, 1), false) > 0;
    }

    @Override
    public boolean canDrawGas(final EnumFacing side, final Gas type) {
        return type != null && this.drawGas(side, new GasStack(type, 1), false) != null;
    }

    @Nonnull
    @Override
    public GasTankInfo[] getTankInfo() {
        final GasTankInfo[] tanks = new GasTankInfo[this.inv.size()];
        for (int slot = 0; slot < tanks.length; slot++) {
            tanks[slot] = new SlotTank(slot);
        }
        return tanks;
    }

    /**
     * @return the gas in that slot, or null when it is empty or holds something else.
     */
    @Nullable
    protected GasStack gasIn(final int slot) {
        final GenericStack stack = this.inv.getStack(slot);
        if (stack == null || !(stack.what() instanceof AEGasKey key)) {
            return null;
        }
        return key.toStack((int) Math.min(stack.amount(), Integer.MAX_VALUE));
    }

    protected int capacityOf(final int slot) {
        // A machine sizes a slot by key type, so any gas stands in for an empty slot's.
        final AEKey key = this.inv.getKey(slot) instanceof AEGasKey held ? held : anyGas();
        final long capacity = key == null ? GenericSlotCapacities.get(AEGasKeyType.INSTANCE)
                : this.inv.getCapacity(key);
        return (int) Math.min(capacity, Integer.MAX_VALUE);
    }

    @Nullable
    private static AEGasKey anyGas() {
        final List<Gas> gases = GasRegistry.getRegisteredGasses();
        return gases.isEmpty() ? null : AEGasKey.of(gases.get(0));
    }

    /**
     * @return false when the slot already holds something that is not a gas, so gas cannot displace it.
     */
    protected boolean acceptsGasIn(final int slot) {
        final AEKey key = this.inv.getKey(slot);
        return key == null || key instanceof AEGasKey;
    }

    private final class SlotTank implements GasTankInfo {

        private final int slot;

        private SlotTank(final int slot) {
            this.slot = slot;
        }

        @Nullable
        @Override
        public GasStack getGas() {
            return GenericStackGasHandler.this.gasIn(this.slot);
        }

        @Override
        public int getStored() {
            final GasStack gas = this.getGas();
            return gas == null ? 0 : gas.amount;
        }

        @Override
        public int getMaxGas() {
            return GenericStackGasHandler.this.capacityOf(this.slot);
        }
    }
}
