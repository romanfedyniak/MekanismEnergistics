package com.mekeng.github.common.me.strategy;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasItem;
import mekanism.common.base.ITierItem;
import mekanism.common.tier.BaseTier;

import appeng.api.behaviors.ContainerItemStrategy;
import appeng.api.config.Actionable;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;

import com.mekeng.github.common.me.AEGasKey;

/**
 * A gas tank item, or anything else holding gas the way Mekanism's items do, filled and emptied against a
 * terminal row or a filter slot like a bucket.
 * <p>
 * {@link IGasItem#addGas} and {@link IGasItem#removeGas} move at most the item's rate per call and cannot
 * simulate, so the amount is worked out here and written with {@link IGasItem#setGas}: one click fills or
 * empties the whole tank, as it did in the original mod. There is no empty gas container to hand out.
 */
public final class GasContainerItemStrategy implements ContainerItemStrategy {

    @Nullable
    @Override
    public GenericStack getContainedStack(final ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof IGasItem item)) {
            return null;
        }
        final GasStack contained = item.getGas(stack);
        final AEGasKey key = AEGasKey.of(contained);
        return key == null || contained.amount <= 0 ? null : new GenericStack(key, contained.amount);
    }

    @Nullable
    @Override
    public Context openContext(final ItemStack container) {
        if (container.isEmpty() || !(container.getItem() instanceof IGasItem item)) {
            return null;
        }
        final ItemStack one = container.copy();
        one.setCount(1);
        return new GasContext(item, one);
    }

    private static final class GasContext implements Context {

        private final IGasItem item;
        private final ItemStack stack;

        private GasContext(final IGasItem item, final ItemStack stack) {
            this.item = item;
            this.stack = stack;
        }

        @Override
        public long insert(final AEKey what, final long amount, final Actionable mode) {
            if (!(what instanceof AEGasKey key) || amount <= 0 || !this.item.canReceiveGas(this.stack, key.getGas())) {
                return 0;
            }
            final GasStack stored = this.item.getGas(this.stack);
            if (stored != null && stored.amount > 0 && stored.getGas() != key.getGas()) {
                return 0;
            }

            final int held = stored == null ? 0 : stored.amount;
            final long space = this.isCreative() ? Integer.MAX_VALUE : this.item.getMaxGas(this.stack) - held;
            final int toAdd = (int) Math.min(space, amount);
            if (toAdd <= 0) {
                return 0;
            }
            if (mode == Actionable.MODULATE) {
                this.item.setGas(this.stack, key.toStack(this.isCreative() ? Integer.MAX_VALUE : held + toAdd));
            }
            return toAdd;
        }

        @Override
        public long extract(final AEKey what, final long amount, final Actionable mode) {
            if (!(what instanceof AEGasKey key) || amount <= 0 || !this.item.canProvideGas(this.stack, key.getGas())) {
                return 0;
            }
            final GasStack stored = this.item.getGas(this.stack);
            if (stored == null || stored.amount <= 0 || stored.getGas() != key.getGas()) {
                return 0;
            }

            final int left = this.isCreative() ? Integer.MAX_VALUE : stored.amount;
            final int toRemove = (int) Math.min(left, amount);
            if (mode == Actionable.MODULATE && !this.isCreative()) {
                this.item.setGas(this.stack, left == toRemove ? null : key.toStack(left - toRemove));
            }
            return toRemove;
        }

        @Nullable
        @Override
        public GenericStack getExtractableContent() {
            final GasStack stored = this.item.getGas(this.stack);
            final Gas gas = stored == null ? null : stored.getGas();
            if (gas == null || stored.amount <= 0 || !this.item.canProvideGas(this.stack, gas)) {
                return null;
            }
            return new GenericStack(AEGasKey.of(gas), stored.amount);
        }

        @Override
        public ItemStack getContainer() {
            return this.stack;
        }

        /** A creative tank gives without end and never empties. */
        private boolean isCreative() {
            return this.stack.getItem() instanceof ITierItem tiered
                    && tiered.getBaseTier(this.stack) == BaseTier.CREATIVE;
        }
    }
}
