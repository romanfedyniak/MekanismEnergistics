package com.mekeng.github.common.me.inventory;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import mekanism.api.Action;
import mekanism.api.gas.GasStack;

import appeng.api.behaviors.GenericInternalInventory;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.GenericStack;

import com.mekeng.github.common.me.AEGasKey;

/**
 * Under Mekanism CEu only: the same handler, also speaking CEu's per-tank interface. A CEu tube asks it for
 * the gas the tube is already carrying, from whichever slot holds it, instead of taking the first gas. Never
 * loaded under the original Mekanism or CE, where that interface does not exist.
 */
public class CeuGenericStackGasHandler extends GenericStackGasHandler
        implements mekanism.api.gas.IExtendedGasHandler {

    public CeuGenericStackGasHandler(final GenericInternalInventory inv, final Supplier<IStorageService> network,
            final IActionSource source) {
        super(inv, network, source);
    }

    @Override
    public int getCountGasTanks() {
        return this.inv.size();
    }

    @Nullable
    @Override
    public GasStack getGasInTank(final int tank) {
        return this.gasIn(tank);
    }

    @Override
    public void setGasInTank(final int tank, @Nullable final GasStack stack) {
        final AEGasKey key = AEGasKey.of(stack);
        this.inv.setStack(tank, key == null || stack.amount <= 0 ? null : new GenericStack(key, stack.amount));
    }

    @Override
    public int getGasTankCapacity(final int tank) {
        return this.capacityOf(tank);
    }

    @Override
    public boolean isGasValid(final int tank, @Nullable final GasStack stack) {
        return this.acceptsGasIn(tank);
    }

    /**
     * Straight into the slot, as the per-tank contract says; gas offered to the handler as a whole goes
     * through {@link #insertGas(GasStack, Action)}, which tries the network first.
     */
    @Nullable
    @Override
    public GasStack insertGas(final int tank, @Nullable final GasStack stack, final Action action) {
        final AEGasKey key = AEGasKey.of(stack);
        if (key == null || stack.amount <= 0 || !this.acceptsGasIn(tank)) {
            return stack;
        }
        final long inserted = this.inv.insert(tank, key, stack.amount, modeOf(action));
        return remainder(stack, inserted);
    }

    @Nullable
    @Override
    public GasStack extractGas(final int tank, final int amount, final Action action) {
        if (amount <= 0 || !(this.inv.getKey(tank) instanceof AEGasKey key)) {
            return null;
        }
        final long extracted = this.inv.extract(tank, key, amount, modeOf(action));
        return extracted <= 0 ? null : key.toStack((int) extracted);
    }

    @Nullable
    @Override
    public GasStack insertGas(@Nullable final GasStack stack, final Action action) {
        if (stack == null) {
            return null;
        }
        return remainder(stack, this.receiveGas(null, stack, action.execute()));
    }

    @Nullable
    @Override
    public GasStack extractGas(final int amount, final Action action) {
        return this.drawGas(null, amount, action.execute());
    }

    @Nullable
    @Override
    public GasStack extractGas(@Nullable final GasStack stack, final Action action) {
        return stack == null ? null : this.drawGas(null, stack, action.execute());
    }

    private static Actionable modeOf(final Action action) {
        return action.execute() ? Actionable.MODULATE : Actionable.SIMULATE;
    }

    @Nullable
    private static GasStack remainder(final GasStack offered, final long taken) {
        return taken >= offered.amount ? null : new GasStack(offered.getGas(), (int) (offered.amount - taken));
    }
}
