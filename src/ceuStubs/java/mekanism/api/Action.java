package mekanism.api;

/**
 * Compile-time stand-in for Mekanism CEu's enum, never packaged; see {@code mekanism.api.gas.IExtendedGasHandler}.
 */
public enum Action {

    EXECUTE,
    SIMULATE;

    public boolean execute() {
        throw new UnsupportedOperationException();
    }

    public static Action get(boolean execute) {
        throw new UnsupportedOperationException();
    }
}
