package com.mekeng.github.common;

import javax.annotation.Nonnull;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

import mekanism.common.MekanismBlocks;

import com.mekeng.github.MekEng;
import com.mekeng.github.common.item.ItemGasP2P;

public class ItemAndBlocks {

    public static final CreativeTabs TAB = new CreativeTabs(MekEng.MODID) {
        @Nonnull
        @Override
        public ItemStack createIcon() {
            return new ItemStack(MekanismBlocks.GasTank);
        }
    };

    public static ItemGasP2P GAS_P2P;

    public static void init(RegistryHandler regHandler) {
        regHandler.item("gas_p2p", GAS_P2P = new ItemGasP2P());
    }

}
