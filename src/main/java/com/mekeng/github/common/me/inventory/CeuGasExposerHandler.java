package com.mekeng.github.common.me.inventory;

import javax.annotation.Nullable;

import mekanism.api.Action;
import mekanism.api.gas.GasStack;

import appeng.api.behaviors.ExposedStorage;

import com.mekeng.github.common.me.AEGasKey;

/**
 * {@link GasExposerHandler} with the per-tank half that CEu's tubes and machines ask for.
 */
public class CeuGasExposerHandler extends GasExposerHandler implements mekanism.api.gas.IExtendedGasHandler {

    public CeuGasExposerHandler(final ExposedStorage storage) {
        super(storage);
    }

    @Override
    public int getCountGasTanks() {
        return this.storage.size();
    }

    @Nullable
    @Override
    public GasStack getGasInTank(final int tank) {
        return this.gasIn(tank);
    }

    @Override
    public void setGasInTank(final int tank, @Nullable final GasStack stack) {
    }

    @Override
    public int getGasTankCapacity(final int tank) {
        final GasStack gas = this.gasIn(tank);
        return gas == null ? 0 : gas.amount;
    }

    @Override
    public boolean isGasValid(final int tank, @Nullable final GasStack stack) {
        return false;
    }

    @Nullable
    @Override
    public GasStack insertGas(final int tank, @Nullable final GasStack stack, final Action action) {
        return stack;
    }

    @Nullable
    @Override
    public GasStack extractGas(final int tank, final int amount, final Action action) {
        return this.draw(this.storage.getKey(tank), amount, action.execute());
    }

    @Nullable
    @Override
    public GasStack insertGas(@Nullable final GasStack stack, final Action action) {
        return stack;
    }

    @Nullable
    @Override
    public GasStack extractGas(final int amount, final Action action) {
        return this.drawGas(null, amount, action.execute());
    }

    @Nullable
    @Override
    public GasStack extractGas(@Nullable final GasStack stack, final Action action) {
        return stack == null ? null : this.draw(AEGasKey.of(stack), stack.amount, action.execute());
    }
}
