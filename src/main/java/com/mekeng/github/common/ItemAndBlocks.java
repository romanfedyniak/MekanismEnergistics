package com.mekeng.github.common;

import javax.annotation.Nonnull;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

import mekanism.common.MekanismBlocks;

import com.mekeng.github.MekEng;

public class ItemAndBlocks {

    public static final CreativeTabs TAB = new CreativeTabs(MekEng.MODID) {
        @Nonnull
        @Override
        public ItemStack createIcon() {
            return new ItemStack(MekanismBlocks.GasTank);
        }
    };

    public static void init(RegistryHandler regHandler) {
    }

}
