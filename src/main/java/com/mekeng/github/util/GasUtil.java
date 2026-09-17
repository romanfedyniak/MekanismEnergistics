package com.mekeng.github.util;

import javax.annotation.Nullable;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasHandler;
import mekanism.common.capabilities.Capabilities;

import com.mekeng.github.common.me.inventory.IExtendedGasHandler;

/**
 * The few things every gas strategy needs: reaching a neighbour's gas handler, and drawing a gas by type
 * rather than taking whatever the handler hands over first.
 */
public final class GasUtil {

    private GasUtil() {
    }

    /**
     * @return the gas handler of the block at {@code pos}, or null - including when its chunk is not loaded,
     *         which must never be a reason to load one.
     */
    @Nullable
    public static IGasHandler getHandler(final World world, final BlockPos pos, final EnumFacing side) {
        if (world.getChunkProvider().getLoadedChunk(pos.getX() >> 4, pos.getZ() >> 4) == null) {
            return null;
        }

        final TileEntity target = world.getTileEntity(pos);
        return target == null ? null : target.getCapability(Capabilities.GAS_HANDLER_CAPABILITY, side);
    }

    /**
     * Draws a specific gas.
     * <p>
     * Mekanism's own {@code drawGas} only asks for an amount, so a handler holding several gases decides for
     * itself which one to give up. A handler that knows how to be asked for one is asked; anything else is
     * asked the plain way and the answer is checked, and put back if it is the wrong gas.
     *
     * @return null if nothing of that gas came out.
     */
    @Nullable
    public static GasStack drawGas(final IGasHandler handler, final Gas gas, final EnumFacing side, final int amount,
            final boolean doTransfer) {
        if (amount <= 0) {
            return null;
        }

        if (handler instanceof IExtendedGasHandler extended) {
            final GasStack drawn = extended.drawGas(side, new GasStack(gas, amount), doTransfer);
            return drawn == null || drawn.amount <= 0 ? null : drawn;
        }

        final GasStack simulated = handler.drawGas(side, amount, false);
        if (simulated == null || simulated.amount <= 0 || simulated.getGas() != gas) {
            return null;
        }
        if (!doTransfer) {
            return simulated;
        }

        final GasStack drawn = handler.drawGas(side, simulated.amount, true);
        if (drawn == null || drawn.amount <= 0) {
            return null;
        }
        if (drawn.getGas() != gas) {
            // The handler changed its mind between the two calls; give it back rather than void it.
            handler.receiveGas(side, drawn, true);
            return null;
        }
        return drawn;
    }
}
