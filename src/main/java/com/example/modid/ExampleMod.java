package com.example.modid;

import com.example.modid.Tags;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION)
public class ExampleMod {

    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);

    /**
     * <a href="https://cleanroommc.com/wiki/forge-mod-development/event#overview">
     *     Take a look at how many FMLStateEvents you can listen to via the @Mod.EventHandler annotation here
     * </a>
     */
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER.info("Hello From {}!", Tags.MOD_NAME);
    }
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        BlockHUDHandler blockHUDHandler = new BlockHUDHandler();

        // Register blocks and their custom HUD text
        blockHUDHandler.registerBlocksHUD(Arrays.asList(Blocks.OAK_DOOR, Blocks.DARK_OAK_DOOR, Blocks.ACACIA_DOOR), "Open Door");
        blockHUDHandler.registerBlockHUD(Blocks.CHEST, "Open Chest");
        blockHUDHandler.registerBlockHUD(Blocks.FURNACE, "Open Furnace");

        MinecraftForge.EVENT_BUS.register(blockHUDHandler);
    }

}
