package mekanism.api.gas;

import javax.annotation.Nullable;

import mekanism.api.Action;

/**
 * Compile-time stand-in for Mekanism CEu's per-tank gas handler, so the CEu-only class builds against the
 * original Mekanism and CE too. It is never packaged; under {@code -Pmekanism_flavour=ceu} the real one is
 * compiled against instead, which is what catches a signature drifting from this copy.
 */
public interface IExtendedGasHandler extends IGasHandler {

    int getCountGasTanks();

    @Nullable
    GasStack getGasInTank(int tank);

    void setGasInTank(int tank, @Nullable GasStack stack);

    int getGasTankCapacity(int tank);

    boolean isGasValid(int tank, @Nullable GasStack stack);

    @Nullable
    GasStack insertGas(int tank, @Nullable GasStack stack, Action action);

    @Nullable
    GasStack extractGas(int tank, int amount, Action action);

    @Nullable
    default GasStack insertGas(@Nullable GasStack stack, Action action) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    default GasStack extractGas(int amount, Action action) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    default GasStack extractGas(@Nullable GasStack stack, Action action) {
        throw new UnsupportedOperationException();
    }
}
