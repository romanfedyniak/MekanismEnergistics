package com.mekeng.github.proxy;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import appeng.api.client.AEKeyRendering;
import appeng.api.integrations.hei.IngredientConverters;

import com.mekeng.github.client.ClientRegistryHandler;
import com.mekeng.github.client.hei.GasIngredientConverter;
import com.mekeng.github.client.render.GasKeyRenderHandler;
import com.mekeng.github.common.RegistryHandler;
import com.mekeng.github.common.me.AEGasKeyType;

public class ClientProxy extends CommonProxy {

    @Override
    public RegistryHandler createRegistryHandler() {
        return new ClientRegistryHandler();
    }

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        AEKeyRendering.register(AEGasKeyType.INSTANCE, new GasKeyRenderHandler());
        IngredientConverters.register(new GasIngredientConverter());
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);
    }

}
