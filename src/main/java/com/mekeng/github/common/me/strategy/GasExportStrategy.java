package com.mekeng.github.common.me.strategy;

import javax.annotation.Nullable;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import mekanism.api.gas.IGasHandler;

import appeng.api.behaviors.StackExportStrategy;
import appeng.api.behaviors.StackTransferContext;
import appeng.api.config.Actionable;
import appeng.api.stacks.AEKey;

import com.mekeng.github.common.me.AEGasKey;
import com.mekeng.github.util.GasUtil;

/**
 * What an export bus, an interface or a machine pushes into a gas tank next to it.
 */
public class GasExportStrategy implements StackExportStrategy {

    private final World world;
    private final BlockPos fromPos;
    private final EnumFacing fromSide;

    GasExportStrategy(final World world, final BlockPos fromPos, final EnumFacing fromSide) {
        this.world = world;
        this.fromPos = fromPos;
        this.fromSide = fromSide;
    }

    @Override
    public long transfer(final StackTransferContext context, final AEKey what, final long maxAmount) {
        if (!(what instanceof AEGasKey gasKey) || maxAmount <= 0) {
            return 0;
        }

        final IGasHandler handler = this.getHandler();
        if (handler == null || !handler.canReceiveGas(this.fromSide, gasKey.getGas())) {
            return 0;
        }

        final int amount = (int) Math.min(maxAmount, Integer.MAX_VALUE);
        final var internal = context.getInternalStorage();
        final var source = context.getActionSource();

        final long extractedSim = internal.extract(gasKey, amount, Actionable.SIMULATE, source);
        if (extractedSim <= 0) {
            return 0;
        }

        final int accepted = handler.receiveGas(this.fromSide, gasKey.toStack((int) extractedSim), false);
        if (accepted <= 0) {
            return 0;
        }

        final long extracted = internal.extract(gasKey, accepted, Actionable.MODULATE, source);
        if (extracted <= 0) {
            return 0;
        }

        final int filled = handler.receiveGas(this.fromSide, gasKey.toStack((int) extracted), true);
        if (filled < extracted) {
            // Put back whatever the tank would not take after all, rather than voiding it.
            internal.insert(gasKey, extracted - filled, Actionable.MODULATE, source);
        }

        return filled;
    }

    @Override
    public long push(final AEKey what, final long maxAmount, final Actionable mode) {
        if (!(what instanceof AEGasKey gasKey) || maxAmount <= 0) {
            return 0;
        }

        final IGasHandler handler = this.getHandler();
        if (handler == null || !handler.canReceiveGas(this.fromSide, gasKey.getGas())) {
            return 0;
        }

        final int amount = (int) Math.min(maxAmount, Integer.MAX_VALUE);
        return handler.receiveGas(this.fromSide, gasKey.toStack(amount), mode == Actionable.MODULATE);
    }

    @Nullable
    private IGasHandler getHandler() {
        return GasUtil.getHandler(this.world, this.fromPos, this.fromSide);
    }

    public static StackExportStrategy create(final World world, final BlockPos fromPos, final EnumFacing fromSide) {
        return new GasExportStrategy(world, fromPos, fromSide);
    }
}
