package com.mekeng.github.common;

import javax.annotation.Nonnull;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import mekanism.common.MekanismBlocks;

import appeng.api.AEApi;
import appeng.items.materials.MaterialType;

import com.mekeng.github.MekEng;
import com.mekeng.github.common.item.ItemGasP2P;
import com.mekeng.github.common.item.ItemGasStorageCell;

public class ItemAndBlocks {

    public static final CreativeTabs TAB = new CreativeTabs(MekEng.MODID) {
        @Nonnull
        @Override
        public ItemStack createIcon() {
            return new ItemStack(MekanismBlocks.GasTank);
        }
    };

    public static ItemGasP2P GAS_P2P;
    public static Item GAS_CELL_HOUSING;

    public static void init(RegistryHandler regHandler) {
        regHandler.item("gas_p2p", GAS_P2P = new ItemGasP2P());

        regHandler.item("gas_cell_housing", GAS_CELL_HOUSING = new Item());
        regHandler.item("gas_cell_1k", new ItemGasStorageCell(MaterialType.CELL1K_PART, 1, 8, 0.5));
        regHandler.item("gas_cell_4k", new ItemGasStorageCell(MaterialType.CELL4K_PART, 4, 32, 1.0));
        regHandler.item("gas_cell_16k", new ItemGasStorageCell(MaterialType.CELL16K_PART, 16, 128, 1.5));
        regHandler.item("gas_cell_64k", new ItemGasStorageCell(MaterialType.CELL64K_PART, 64, 512, 2.0));
        // The larger tiers follow AE2UD's high capacity switch, which leaves its own 256k cell unregistered.
        if (AEApi.instance().definitions().items().cell256k().isEnabled()) {
            regHandler.item("gas_cell_256k", new ItemGasStorageCell(MaterialType.CELL256K_PART, 256, 2048, 2.5));
            regHandler.item("gas_cell_1024k", new ItemGasStorageCell(MaterialType.CELL1024K_PART, 1024, 8192, 3.0));
            regHandler.item("gas_cell_4096k", new ItemGasStorageCell(MaterialType.CELL4096K_PART, 4096, 32768, 3.5));
            regHandler.item("gas_cell_16384k", new ItemGasStorageCell(MaterialType.CELL16384K_PART, 16384, 131072, 4.0));
        }
    }

}
