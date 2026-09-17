package com.mekeng.github.common.me;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.netty.buffer.ByteBuf;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import mekanism.common.MekanismBlocks;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;

import com.mekeng.github.MekEng;

/**
 * Gas as a kind of content a network can hold.
 * <p>
 * The numbers are the original mod's, so a cell filled before the move still fits in itself: a byte holds
 * 32000 mB of gas, four times what it holds of a fluid. A machine operation moves 500 mB, which keeps the
 * same four-to-one ratio against AE2UD's 125 mB of fluid.
 */
public final class AEGasKeyType extends AEKeyType {

    public static final ResourceLocation ID = MekEng.id("gas");

    public static final AEGasKeyType INSTANCE = new AEGasKeyType();

    private AEGasKeyType() {
        super(ID, AEGasKey.class, new TextComponentString("Gases"));
    }

    @Override
    public AEKey readFromPacket(@Nonnull final ByteBuf input) throws IOException {
        return AEGasKey.fromPacket(input);
    }

    @Nullable
    @Override
    public AEKey loadKeyFromTag(@Nonnull final NBTTagCompound tag) {
        return AEGasKey.fromTag(tag);
    }

    @Override
    public int getAmountPerOperation() {
        return AEGasKey.AMOUNT_BUCKET * 500 / 1000;
    }

    @Override
    public int getAmountPerByte() {
        return 32 * AEGasKey.AMOUNT_BUCKET;
    }

    @Override
    public int getAmountPerUnit() {
        return AEGasKey.AMOUNT_BUCKET;
    }

    @Override
    public String getUnitSymbol() {
        return "B";
    }

    @Override
    public TextFormatting getDisplayColour() {
        return TextFormatting.AQUA;
    }

    @Override
    public ResourceLocation getButtonTexture() {
        return new ResourceLocation("appliedenergistics2", "textures/guis/states.png");
    }

    @Override
    public ItemStack getButtonIcon() {
        return new ItemStack(MekanismBlocks.GasTank);
    }
}
