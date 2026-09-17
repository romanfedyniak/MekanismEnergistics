package com.mekeng.github.common.me.strategy;

import javax.annotation.Nullable;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTankInfo;
import mekanism.api.gas.IGasHandler;

import appeng.api.behaviors.ExternalStorageStrategy;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.IStorageChangeSource;
import appeng.api.storage.MEStorage;
import appeng.me.storage.ITickingMonitor;
import appeng.me.storage.StorageChangeListeners;

import com.mekeng.github.common.me.AEGasKey;
import com.mekeng.github.util.GasUtil;

/**
 * A gas tank mounted on a network by a storage bus, seen as storage.
 */
public class GasHandlerAdapter implements MEStorage, ITickingMonitor, IStorageChangeSource {

    private final IGasHandler handler;
    private final EnumFacing side;
    private final boolean extractableOnly;
    @Nullable
    private final Runnable changeListener;

    private KeyCounter currentlyCached = new KeyCounter();
    private final StorageChangeListeners listeners = new StorageChangeListeners();

    GasHandlerAdapter(final IGasHandler handler, final EnumFacing side, final boolean extractableOnly,
            @Nullable final Runnable changeListener) {
        this.handler = handler;
        this.side = side;
        this.extractableOnly = extractableOnly;
        this.changeListener = changeListener;
        this.updateCache();
    }

    @Override
    public void addChangeListener(final Listener listener) {
        this.listeners.addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(final Listener listener) {
        this.listeners.removeChangeListener(listener);
    }

    @Override
    public long insert(final AEKey what, final long amount, final Actionable mode, final IActionSource source) {
        if (!(what instanceof AEGasKey gasKey) || amount <= 0
                || !this.handler.canReceiveGas(this.side, gasKey.getGas())) {
            return 0;
        }

        final int toInsert = (int) Math.min(amount, Integer.MAX_VALUE);
        final int filled = this.handler.receiveGas(this.side, gasKey.toStack(toInsert), mode == Actionable.MODULATE);

        if (filled > 0 && mode == Actionable.MODULATE) {
            this.updateCache();
            this.notifyChange();
        }

        return filled;
    }

    @Override
    public long extract(final AEKey what, final long amount, final Actionable mode, final IActionSource source) {
        if (!(what instanceof AEGasKey gasKey) || amount <= 0
                || !this.handler.canDrawGas(this.side, gasKey.getGas())) {
            return 0;
        }

        final int toDraw = (int) Math.min(amount, Integer.MAX_VALUE);
        final GasStack drawn = GasUtil.drawGas(this.handler, gasKey.getGas(), this.side, toDraw,
                mode == Actionable.MODULATE);
        final long extracted = drawn == null ? 0 : drawn.amount;

        if (extracted > 0 && mode == Actionable.MODULATE) {
            this.updateCache();
            this.notifyChange();
        }

        return extracted;
    }

    @Override
    public void getAvailableStacks(final KeyCounter out) {
        for (final var entry : this.currentlyCached) {
            out.add(entry.getKey(), entry.getLongValue());
        }
    }

    @Override
    public TickRateModulation onTick() {
        final KeyCounter before = this.currentlyCached;
        this.updateCache();
        return keyCountersEqual(before, this.currentlyCached) ? TickRateModulation.SLOWER : TickRateModulation.URGENT;
    }

    private void notifyChange() {
        if (this.changeListener != null) {
            this.changeListener.run();
        }
    }

    private void updateCache() {
        final KeyCounter fresh = new KeyCounter();

        for (final GasTankInfo tank : this.handler.getTankInfo()) {
            final GasStack contents = tank.getGas();
            if (contents == null || contents.amount <= 0 || contents.getGas() == null) {
                continue;
            }
            if (this.extractableOnly && !this.handler.canDrawGas(this.side, contents.getGas())) {
                continue;
            }

            final AEGasKey key = AEGasKey.of(contents);
            if (key != null) {
                fresh.add(key, contents.amount);
            }
        }

        // Everything this adapter can see passes through here, its own insertions as much as a machine
        // filling the tank, so this is the one place that says what moved.
        this.listeners.postDiff(this.currentlyCached, fresh);
        this.currentlyCached = fresh;
    }

    private static boolean keyCountersEqual(final KeyCounter a, final KeyCounter b) {
        if (a.size() != b.size()) {
            return false;
        }
        for (final var entry : a) {
            if (b.get(entry.getKey()) != entry.getLongValue()) {
                return false;
            }
        }
        return true;
    }

    /**
     * The {@link ExternalStorageStrategy} for gas; a storage bus reaches it through
     * {@code StackWorldBehaviors}, never this class directly.
     */
    public static final class Strategy implements ExternalStorageStrategy {

        private final World world;
        private final BlockPos fromPos;
        private final EnumFacing fromSide;

        public Strategy(final World world, final BlockPos fromPos, final EnumFacing fromSide) {
            this.world = world;
            this.fromPos = fromPos;
            this.fromSide = fromSide;
        }

        @Nullable
        @Override
        public MEStorage createWrapper(final boolean extractableOnly, final Runnable injectOrExtractCallback) {
            final IGasHandler handler = GasUtil.getHandler(this.world, this.fromPos, this.fromSide);
            if (handler == null) {
                return null;
            }
            return new GasHandlerAdapter(handler, this.fromSide, extractableOnly, injectOrExtractCallback);
        }
    }
}
