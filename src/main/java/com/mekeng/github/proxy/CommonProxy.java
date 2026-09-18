package com.mekeng.github.proxy;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

import mekanism.common.capabilities.Capabilities;

import appeng.api.behaviors.ExternalStorageStrategy;
import appeng.api.behaviors.GenericInventoryAdapters;
import appeng.api.behaviors.GenericSlotCapacities;
import appeng.api.behaviors.StackExportStrategy;
import appeng.api.behaviors.StackImportStrategy;
import appeng.api.stacks.AEKeyType;

import com.mekeng.github.MekEng;
import com.mekeng.github.common.ItemAndBlocks;
import com.mekeng.github.common.RegistryHandler;
import com.mekeng.github.common.me.AEGasKey;
import com.mekeng.github.common.me.AEGasKeyType;
import com.mekeng.github.common.me.inventory.CeuGenericStackGasHandler;
import com.mekeng.github.common.me.inventory.GenericStackGasHandler;
import com.mekeng.github.common.me.strategy.GasExportStrategy;
import com.mekeng.github.common.me.strategy.GasHandlerAdapter;
import com.mekeng.github.common.me.strategy.GasImportStrategy;

public class CommonProxy {

    public final RegistryHandler regHandler = createRegistryHandler();
    public final SimpleNetworkWrapper netHandler = NetworkRegistry.INSTANCE.newSimpleChannel(MekEng.MODID);

    public RegistryHandler createRegistryHandler() {
        return new RegistryHandler();
    }

    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(regHandler);
        ItemAndBlocks.init(regHandler);
    }

    /**
     * Gas joins AE2UD's key types the way a block or an item joins theirs, which is what makes every
     * terminal, bus and cell able to carry it without knowing what it is.
     */
    @SubscribeEvent
    public void onRegisterKeyTypes(final RegistryEvent.Register<AEKeyType> event) {
        event.getRegistry().register(AEGasKeyType.INSTANCE);
    }

    public void init(FMLInitializationEvent event) {
        regHandler.onInit();

        // With these three, the buses, storage buses and interfaces AE2UD already ships carry gas.
        StackImportStrategy.register(AEGasKeyType.INSTANCE, GasImportStrategy::create);
        StackExportStrategy.register(AEGasKeyType.INSTANCE, GasExportStrategy::create);
        ExternalStorageStrategy.register(AEGasKeyType.INSTANCE, GasHandlerAdapter.Strategy::new);

        // An interface's gas slots hold what its fluid slots do; the interface multiplies both alike.
        GenericSlotCapacities.register(AEGasKeyType.INSTANCE, 4L * AEGasKey.AMOUNT_BUCKET);
        if (isMekanismCeu()) {
            GenericInventoryAdapters.register(Capabilities.GAS_HANDLER_CAPABILITY, CeuGenericStackGasHandler::new);
        } else {
            GenericInventoryAdapters.register(Capabilities.GAS_HANDLER_CAPABILITY, GenericStackGasHandler::new);
        }
    }

    /**
     * CEu is told apart by the per-tank gas interface only it has.
     */
    private static boolean isMekanismCeu() {
        try {
            Class.forName("mekanism.api.gas.IExtendedGasHandler", false, CommonProxy.class.getClassLoader());
            return true;
        } catch (final ClassNotFoundException e) {
            return false;
        }
    }

    public void postInit(FMLPostInitializationEvent event) {
    }

}
