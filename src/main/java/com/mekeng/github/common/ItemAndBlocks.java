package com.mekeng.github.common;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import mekanism.common.MekanismBlocks;

import appeng.api.AEApi;
import appeng.api.definitions.IItemDefinition;
import appeng.api.definitions.IItems;
import appeng.items.materials.MaterialType;
import appeng.items.tools.powered.ToolPortableCell;

import com.mekeng.github.MekEng;
import com.mekeng.github.common.item.ItemGasP2P;
import com.mekeng.github.common.item.ItemGasStorageCell;
import com.mekeng.github.common.me.AEGasKeyType;

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
    public static final List<Item> GAS_CELLS = new ArrayList<>();
    public static final List<Item> PORTABLE_GAS_CELLS = new ArrayList<>();

    public static void init(RegistryHandler regHandler) {
        regHandler.item("gas_p2p", GAS_P2P = new ItemGasP2P());

        regHandler.item("gas_cell_housing", GAS_CELL_HOUSING = new Item());
        gasCell(regHandler, "gas_cell_1k", new ItemGasStorageCell(MaterialType.CELL1K_PART, 1, 8, 0.5));
        gasCell(regHandler, "gas_cell_4k", new ItemGasStorageCell(MaterialType.CELL4K_PART, 4, 32, 1.0));
        gasCell(regHandler, "gas_cell_16k", new ItemGasStorageCell(MaterialType.CELL16K_PART, 16, 128, 1.5));
        gasCell(regHandler, "gas_cell_64k", new ItemGasStorageCell(MaterialType.CELL64K_PART, 64, 512, 2.0));
        // The larger tiers follow AE2UD's high capacity switch, which leaves its own 256k cell unregistered.
        if (AEApi.instance().definitions().items().cell256k().isEnabled()) {
            gasCell(regHandler, "gas_cell_256k", new ItemGasStorageCell(MaterialType.CELL256K_PART, 256, 2048, 2.5));
            gasCell(regHandler, "gas_cell_1024k", new ItemGasStorageCell(MaterialType.CELL1024K_PART, 1024, 8192, 3.0));
            gasCell(regHandler, "gas_cell_4096k", new ItemGasStorageCell(MaterialType.CELL4096K_PART, 4096, 32768, 3.5));
            gasCell(regHandler, "gas_cell_16384k", new ItemGasStorageCell(MaterialType.CELL16384K_PART, 16384, 131072, 4.0));
        }

        // A tier is there when AE2UD's portable fluid cell of that tier is. The 1k keeps the original mod's id.
        final IItems items = AEApi.instance().definitions().items();
        portableCell(regHandler, "portable_gas_cell", 1, items.portableFluidCell1k());
        portableCell(regHandler, "portable_gas_cell_4k", 4, items.portableFluidCell4k());
        portableCell(regHandler, "portable_gas_cell_16k", 16, items.portableFluidCell16k());
        portableCell(regHandler, "portable_gas_cell_64k", 64, items.portableFluidCell64k());
        portableCell(regHandler, "portable_gas_cell_256k", 256, items.portableFluidCell256k());
        portableCell(regHandler, "portable_gas_cell_1024k", 1024, items.portableFluidCell1024k());
        portableCell(regHandler, "portable_gas_cell_4096k", 4096, items.portableFluidCell4096k());
        portableCell(regHandler, "portable_gas_cell_16384k", 16384, items.portableFluidCell16384k());
    }

    private static void gasCell(final RegistryHandler regHandler, final String name, final Item cell) {
        regHandler.item(name, cell);
        GAS_CELLS.add(cell);
    }

    private static void portableCell(final RegistryHandler regHandler, final String name, final int kilobytes,
                                     final IItemDefinition fluidCounterpart) {
        if (fluidCounterpart.isEnabled()) {
            final Item cell = new ToolPortableCell(kilobytes, () -> AEGasKeyType.INSTANCE);
            regHandler.item(name, cell);
            PORTABLE_GAS_CELLS.add(cell);
        }
    }

}
