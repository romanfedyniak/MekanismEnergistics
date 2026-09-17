package com.mekeng.github.common.me.strategy;

import javax.annotation.Nullable;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTankInfo;
import mekanism.api.gas.IGasHandler;

import appeng.api.behaviors.StackImportStrategy;
import appeng.api.behaviors.StackTransferContext;
import appeng.api.config.Actionable;

import com.mekeng.github.common.me.AEGasKey;
import com.mekeng.github.common.me.AEGasKeyType;
import com.mekeng.github.util.GasUtil;

/**
 * What an import bus pulls out of a gas tank next to it. Shaped like AE2UD's fluid strategy: one bounded
 * transfer per call, with the budget counted in operations and the key type saying what an operation is worth.
 */
public class GasImportStrategy implements StackImportStrategy {

    private final World world;
    private final BlockPos fromPos;
    private final EnumFacing fromSide;

    GasImportStrategy(final World world, final BlockPos fromPos, final EnumFacing fromSide) {
        this.world = world;
        this.fromPos = fromPos;
        this.fromSide = fromSide;
    }

    @Override
    public boolean transfer(final StackTransferContext context) {
        if (!context.hasOperationsLeft()) {
            return false;
        }

        final IGasHandler handler = this.getHandler();
        if (handler == null) {
            return false;
        }

        final int amountPerOperation = Math.max(1, AEGasKeyType.INSTANCE.getAmountPerOperation());
        final int maxDraw = (int) Math.min((long) context.getOperationsRemaining() * amountPerOperation,
                Integer.MAX_VALUE);
        final var internal = context.getInternalStorage();
        final var source = context.getActionSource();

        // Walk what the tanks say they hold, so a filtered gas behind an unwanted one is still found.
        for (final GasTankInfo tank : handler.getTankInfo()) {
            final GasStack contents = tank.getGas();
            if (contents == null || contents.amount <= 0 || contents.getGas() == null) {
                continue;
            }

            final AEGasKey what = AEGasKey.of(contents);
            if (what == null || !context.getFilter().matches(what)) {
                continue;
            }
            if (!handler.canDrawGas(this.fromSide, contents.getGas())) {
                continue;
            }

            final int candidate = Math.min(maxDraw, contents.amount);
            final long acceptable = internal.insert(what, candidate, Actionable.SIMULATE, source);
            if (acceptable <= 0) {
                continue;
            }

            final GasStack drawn = GasUtil.drawGas(handler, contents.getGas(), this.fromSide,
                    (int) Math.min(acceptable, candidate), true);
            if (drawn == null) {
                continue;
            }

            final long inserted = internal.insert(what, drawn.amount, Actionable.MODULATE, source);
            if (inserted < drawn.amount) {
                // Hand back what did not fit rather than void it.
                handler.receiveGas(this.fromSide, what.toStack((int) (drawn.amount - inserted)), true);
            }

            // Whole operations, like every other bus: anything that moved at all costs at least one.
            context.reduceOperationsRemaining(Math.max(1, inserted / amountPerOperation));
            return inserted > 0;
        }

        return false;
    }

    @Nullable
    private IGasHandler getHandler() {
        return GasUtil.getHandler(this.world, this.fromPos, this.fromSide);
    }

    public static StackImportStrategy create(final World world, final BlockPos fromPos, final EnumFacing fromSide) {
        return new GasImportStrategy(world, fromPos, fromSide);
    }
}
