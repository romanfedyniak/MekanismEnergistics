package com.mekeng.github;

import org.apache.logging.log4j.Logger;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import com.mekeng.github.proxy.CommonProxy;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION, useMetadata = true,
        dependencies = MekEng.DEPENDENCIES)
public class MekEng {

    /**
     * No version on AE2UD: the fork shares its mod id with AE2 and AE2UEL, and the version range that tells
     * them apart waits for AE2UD's first release. See CHANGES.md.
     */
    static final String DEPENDENCIES = "required-after:appliedenergistics2;required-after:mekanism;after:jei";

    public static final String MODID = Tags.MOD_ID;

    @Mod.Instance(Tags.MOD_ID)
    public static MekEng INSTANCE;

    @SidedProxy(clientSide = "com.mekeng.github.proxy.ClientProxy", serverSide = "com.mekeng.github.proxy.CommonProxy")
    public static CommonProxy proxy;

    public static Logger log;

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        log = event.getModLog();
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void onInit(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void onPostInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MODID, path);
    }

}
