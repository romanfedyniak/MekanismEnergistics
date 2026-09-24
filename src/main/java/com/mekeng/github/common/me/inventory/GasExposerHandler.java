package com.mekeng.github.common.me.inventory;

import java.util.Arrays;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.util.EnumFacing;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTankInfo;

import appeng.api.behaviors.ExposedStorage;
import appeng.api.config.Actionable;
import appeng.api.stacks.AEKey;

import com.mekeng.github.common.me.AEGasKey;

/**
 * What AE2UD's storage exposer shows of gas: every gas the network holds is a tank of its own, which can be
 * drawn from and never filled.
 */
public class GasExposerHandler implements IExtendedGasHandler {

    private static final GasTankInfo[] NONE = new GasTankInfo[0];

    protected final ExposedStorage storage;
    private GasTankInfo[] tanks = NONE;
    private int tanksVersion = -1;

    public GasExposerHandler(final ExposedStorage storage) {
        this.storage = storage;
    }

    @Override
    public int receiveGas(final EnumFacing side, final GasStack stack, final boolean doTransfer) {
        return 0;
    }

    @Nullable
    @Override
    public GasStack drawGas(final EnumFacing side, final int amount, final boolean doTransfer) {
        return this.draw(this.storage.getKey(0), amount, doTransfer);
    }

    @Nullable
    @Override
    public GasStack drawGas(final EnumFacing side, final GasStack stack, final boolean doTransfer) {
        return stack == null ? null : this.draw(AEGasKey.of(stack), stack.amount, doTransfer);
    }

    @Override
    public boolean canReceiveGas(final EnumFacing side, final Gas type) {
        return false;
    }

    @Override
    public boolean canDrawGas(final EnumFacing side, final Gas type) {
        return type != null && this.storage.getAmount(AEGasKey.of(type)) > 0;
    }

    /**
     * Rebuilt only when something changed. The length check catches the exposer losing or regaining its
     * channel, which empties the view without changing the version.
     */
    @Nonnull
    @Override
    public GasTankInfo[] getTankInfo() {
        final int size = this.storage.size();
        if (this.tanksVersion != this.storage.getVersion() || this.tanks.length != size) {
            final GasTankInfo[] built = new GasTankInfo[size];
            int count = 0;
            for (int i = 0; i < size; i++) {
                final GasStack gas = this.gasIn(i);
                if (gas != null) {
                    built[count++] = new Tank(gas);
                }
            }
            this.tanks = count == size ? built : Arrays.copyOf(built, count);
            this.tanksVersion = this.storage.getVersion();
        }
        return this.tanks;
    }

    @Nullable
    protected GasStack gasIn(final int tank) {
        final AEKey key = this.storage.getKey(tank);
        if (!(key instanceof AEGasKey gas)) {
            return null;
        }
        return gas.toStack((int) Math.min(this.storage.getAmount(gas), Integer.MAX_VALUE));
    }

    @Nullable
    protected GasStack draw(@Nullable final AEKey key, final int amount, final boolean doTransfer) {
        if (!(key instanceof AEGasKey gas) || amount <= 0) {
            return null;
        }
        final long drawn = this.storage.extract(gas, amount, doTransfer ? Actionable.MODULATE : Actionable.SIMULATE);
        return drawn <= 0 ? null : gas.toStack((int) drawn);
    }

    private static final class Tank implements GasTankInfo {

        private final GasStack gas;

        private Tank(final GasStack gas) {
            this.gas = gas;
        }

        @Override
        public GasStack getGas() {
            return this.gas;
        }

        @Override
        public int getStored() {
            return this.gas.amount;
        }

        @Override
        public int getMaxGas() {
            return this.gas.amount;
        }
    }
}
