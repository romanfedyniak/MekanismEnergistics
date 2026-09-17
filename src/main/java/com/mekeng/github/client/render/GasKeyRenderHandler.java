package com.mekeng.github.client.render;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.api.client.AEKeyModelContext;
import appeng.api.client.AEKeyRenderHandler;
import appeng.api.client.AEKeyRendering;
import appeng.api.stacks.AEKey;

import com.mekeng.github.common.me.AEGasKey;

/**
 * A gas is drawn as its own icon, the one Mekanism shows in a tank.
 */
@SideOnly(Side.CLIENT)
public class GasKeyRenderHandler implements AEKeyRenderHandler {

    @Nullable
    @Override
    public IBakedModel getModel(final AEKey what, final AEKeyModelContext context) {
        if (!(what instanceof AEGasKey gasKey)) {
            return null;
        }
        return context.getSpriteModel(gasKey.getGas().getIcon());
    }

    @Override
    public int getTint(final AEKey what) {
        // Gas icons are drawn in greyscale and carry their colour separately, as a fluid's still texture does.
        return what instanceof AEGasKey gasKey ? gasKey.getGas().getTint() : AEKeyRendering.NO_TINT;
    }
}
