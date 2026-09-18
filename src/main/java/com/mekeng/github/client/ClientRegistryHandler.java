package com.mekeng.github.client;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import com.mekeng.github.MekEng;
import com.mekeng.github.common.RegistryHandler;

/**
 * Registers an item model for everything the mod registers, named after its registry name.
 */
public class ClientRegistryHandler extends RegistryHandler {

    @SubscribeEvent
    public void onRegisterModels(final ModelRegistryEvent event) {
        for (final Pair<String, Block> entry : this.blocks) {
            registerModel(entry.getLeft(), Item.getItemFromBlock(entry.getRight()));
        }
        for (final Pair<String, Item> entry : this.items) {
            registerModel(entry.getLeft(), entry.getRight());
        }
    }

    private static void registerModel(final String key, final Item item) {
        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(MekEng.id(key), "inventory"));
    }
}
