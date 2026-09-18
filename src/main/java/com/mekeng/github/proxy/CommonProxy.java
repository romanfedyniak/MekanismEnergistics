package com.mekeng.github.proxy;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

import mekanism.common.capabilities.Capabilities;

import appeng.api.AEApi;
import appeng.api.behaviors.ContainerItemStrategy;
import appeng.api.behaviors.ExternalStorageStrategy;
import appeng.api.behaviors.GenericInventoryAdapters;
import appeng.api.behaviors.GenericSlotCapacities;
import appeng.api.behaviors.StackExportStrategy;
import appeng.api.behaviors.StackImportStrategy;
import appeng.api.config.TunnelType;
import appeng.api.features.IP2PTunnelRegistry;
import appeng.api.stacks.AEKeyType;

import com.mekeng.github.MekEng;
import com.mekeng.github.common.ItemAndBlocks;
import com.mekeng.github.common.RegistryHandler;
import com.mekeng.github.common.me.AEGasKey;
import com.mekeng.github.common.me.AEGasKeyType;
import com.mekeng.github.common.me.inventory.CeuGenericStackGasHandler;
import com.mekeng.github.common.me.inventory.GenericStackGasHandler;
import com.mekeng.github.common.me.strategy.GasContainerItemStrategy;
import com.mekeng.github.common.me.strategy.GasExportStrategy;
import com.mekeng.github.common.me.strategy.GasHandlerAdapter;
import com.mekeng.github.common.me.strategy.GasImportStrategy;
import com.mekeng.github.common.part.PartP2PGases;

public class CommonProxy {

    /** {@code TransmitterType.PRESSURIZED_TUBE}'s ordinal, the same in the original Mekanism, CE and CEu. */
    private static final int PRESSURIZED_TUBE_META = 2;

    public final RegistryHandler regHandler = createRegistryHandler();
    public final SimpleNetworkWrapper netHandler = NetworkRegistry.INSTANCE.newSimpleChannel(MekEng.MODID);
    private TunnelType gasTunnel;

    public RegistryHandler createRegistryHandler() {
        return new RegistryHandler();
    }

    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(regHandler);
        ItemAndBlocks.init(regHandler);

        // Here rather than in init: the tunnel's models are registered with it, and have to be before baking.
        this.gasTunnel = AEApi.instance().registries().p2pTunnel().registerTunnelType("GAS",
                new ItemStack(ItemAndBlocks.GAS_P2P), PartP2PGases.MODELS);
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
        this.attuneGasTunnel();

        // With these three, the buses, storage buses and interfaces AE2UD already ships carry gas.
        StackImportStrategy.register(AEGasKeyType.INSTANCE, GasImportStrategy::create);
        StackExportStrategy.register(AEGasKeyType.INSTANCE, GasExportStrategy::create);
        ExternalStorageStrategy.register(AEGasKeyType.INSTANCE, GasHandlerAdapter.Strategy::new);

        // A gas tank item fills and empties against a terminal row or a filter slot like a bucket.
        ContainerItemStrategy.register(AEGasKeyType.INSTANCE, new GasContainerItemStrategy());

        // An interface's gas slots hold what its fluid slots do; the interface multiplies both alike.
        GenericSlotCapacities.register(AEGasKeyType.INSTANCE, 4L * AEGasKey.AMOUNT_BUCKET);
        if (isMekanismCeu()) {
            GenericInventoryAdapters.register(Capabilities.GAS_HANDLER_CAPABILITY, CeuGenericStackGasHandler::new);
        } else {
            GenericInventoryAdapters.register(Capabilities.GAS_HANDLER_CAPABILITY, GenericStackGasHandler::new);
        }
    }

    /**
     * What a tunnel is attuned to gas with: the gas items the original mod named, and a pressurized tube of any
     * tier, which is what is in hand while laying out gas lines. Named items are looked at before AE2UD's
     * "anything from Mekanism is a power tunnel" rule.
     */
    private void attuneGasTunnel() {
        final TunnelType gas = this.gasTunnel;
        final IP2PTunnelRegistry tunnels = AEApi.instance().registries().p2pTunnel();
        for (final String name : new String[] {"gastank", "flamethrower", "gaugedropper", "jetpack", "scubatank"}) {
            final Item item = Item.getByNameOrId("mekanism:" + name);
            if (item != null) {
                tunnels.addNewAttunement(new ItemStack(item), gas);
            }
        }
        // Every tier is the one item: the tier sits in NBT, and attunement ignores that.
        final Item transmitter = Item.getByNameOrId("mekanism:transmitter");
        if (transmitter != null) {
            tunnels.addNewAttunement(new ItemStack(transmitter, 1, PRESSURIZED_TUBE_META), gas);
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
