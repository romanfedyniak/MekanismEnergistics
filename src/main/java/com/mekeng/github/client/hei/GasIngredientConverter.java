package com.mekeng.github.client.hei;

import javax.annotation.Nullable;

import mekanism.api.gas.GasStack;

import appeng.api.integrations.hei.IngredientConverter;
import appeng.api.stacks.GenericStack;

import com.mekeng.github.common.me.AEGasKey;

/**
 * Mekanism registers {@link GasStack} with the recipe viewer itself; this tells AE2UD how that ingredient
 * maps to a gas key, so a gas can be dragged into a filter slot, looked up from a terminal and carried by a
 * recipe transfer.
 */
public final class GasIngredientConverter implements IngredientConverter<GasStack> {

    @Override
    public Class<GasStack> getIngredientClass() {
        return GasStack.class;
    }

    @Nullable
    @Override
    public GasStack getIngredientFromStack(final GenericStack stack) {
        if (!(stack.what() instanceof AEGasKey gasKey)) {
            return null;
        }
        // At least one: the viewer drops a gas stack of nothing, as it does a fluid stack.
        return gasKey.toStack((int) Math.max(1, Math.min(stack.amount(), Integer.MAX_VALUE)));
    }

    @Nullable
    @Override
    public GenericStack getStackFromIngredient(final GasStack ingredient) {
        final AEGasKey key = AEGasKey.of(ingredient);
        if (key == null || ingredient.amount <= 0) {
            return null;
        }
        return new GenericStack(key, ingredient.amount);
    }
}
