package com.mekeng.github.common.me;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.netty.buffer.ByteBuf;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.storage.AEKeyFilter;

/**
 * One gas, as a network names it. A gas carries nothing but its identity - Mekanism's own
 * {@link GasStack} has no NBT of its own - so there is no secondary part to drop.
 */
public final class AEGasKey extends AEKey {

    /**
     * Millibuckets in one bucket. Mekanism counts gas in the same units as a fluid, and shows it that way.
     */
    public static final int AMOUNT_BUCKET = 1000;

    private final Gas gas;
    private final int hash;

    private AEGasKey(final Gas gas) {
        this.gas = gas;
        // By name, like AEFluidKey: a gas object can be replaced while its name stays the same.
        this.hash = gas.getName().hashCode();
    }

    public static AEGasKey of(final Gas gas) {
        return new AEGasKey(Objects.requireNonNull(gas, "gas"));
    }

    /**
     * @return null for a null or empty stack.
     */
    @Nullable
    public static AEGasKey of(@Nullable final GasStack stack) {
        if (stack == null || stack.getGas() == null) {
            return null;
        }
        return new AEGasKey(stack.getGas());
    }

    public static boolean matches(@Nullable final AEKey what, @Nullable final GasStack gas) {
        return what instanceof AEGasKey gasKey && gasKey.matches(gas);
    }

    public static boolean is(@Nullable final AEKey what) {
        return what instanceof AEGasKey;
    }

    public static AEKeyFilter filter() {
        return AEGasKey::is;
    }

    @Override
    public AEKeyType getType() {
        return AEGasKeyType.INSTANCE;
    }

    @Override
    public ResourceLocation getId() {
        return new ResourceLocation("gas", this.gas.getName());
    }

    @Override
    public Object getPrimaryKey() {
        return this.gas;
    }

    @Override
    public AEGasKey dropSecondary() {
        return this;
    }

    public Gas getGas() {
        return this.gas;
    }

    @Nullable
    public GasStack toStack(final int amount) {
        return amount <= 0 ? null : new GasStack(this.gas, amount);
    }

    public boolean matches(@Nullable final GasStack stack) {
        return stack != null && stack.getGas() == this.gas;
    }

    @Override
    public void toTag(final NBTTagCompound out) {
        out.setString("gas", this.gas.getName());
    }

    @Nullable
    public static AEGasKey fromTag(final NBTTagCompound tag) {
        final Gas gas = GasRegistry.getGas(tag.getString("gas"));
        return gas == null ? null : new AEGasKey(gas);
    }

    @Override
    public void writeToPacket(final ByteBuf data) throws IOException {
        new PacketBuffer(data).writeString(this.gas.getName());
    }

    public static AEGasKey fromPacket(final ByteBuf data) throws IOException {
        final String name = new PacketBuffer(data).readString(Short.MAX_VALUE);
        final Gas gas = GasRegistry.getGas(name);
        if (gas == null) {
            throw new IOException("Received a gas key with an unknown gas: " + name);
        }
        return new AEGasKey(gas);
    }

    @Override
    protected ITextComponent computeDisplayName() {
        try {
            return new TextComponentString(this.gas.getLocalizedName());
        } catch (final Throwable e) {
            // A broken gas must not take down the whole terminal.
            return new TextComponentString(this.gas.getName());
        }
    }

    @Override
    public String getModId() {
        return this.gas.getIcon() == null ? "mekanism" : this.gas.getIcon().getNamespace();
    }

    @Override
    public void addDrops(final long amount, final List<ItemStack> drops, @Nonnull final World world,
            @Nonnull final BlockPos pos) {
        // Gas has no item form of its own, so a machine that breaks voids what it held, as fluids do.
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof AEGasKey other && this.gas.getName().equals(other.gas.getName());
    }

    @Override
    public int hashCode() {
        return this.hash;
    }

    @Override
    public String toString() {
        return this.gas.getName();
    }
}
