package com.example.modid;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemHoe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION)
public class MoiInteract {

    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        // Use the builder to configure the BlockHUDHandler
        new BlockHUDBuilder()
                .registerBlocks(Arrays.asList(Blocks.OAK_DOOR, Blocks.DARK_OAK_DOOR, Blocks.ACACIA_DOOR), "Open Door")
                .registerBlock(Blocks.CHEST, "Open Chest")
                .registerBlock(Blocks.FURNACE, "Open Furnace")
                .registerBlockImageItem(Arrays.asList(Blocks.LOG, Blocks.LOG2), new ResourceLocation(Tags.MOD_ID + ":textures/gui/axe.png"),Items.DIAMOND_AXE.getDefaultInstance())
                .registerBlockImageItem(Arrays.asList(Blocks.GRASS, Blocks.DIRT), new ResourceLocation(Tags.MOD_ID + ":textures/gui/hoe.png"), Items.DIAMOND_HOE.getDefaultInstance())
                .build(); // Call build() if you want to return the handler (though in this case, we are using the singleton)

        // Register the BlockHUDHandler to the MinecraftForge event bus
        MinecraftForge.EVENT_BUS.register(BlockHUDHandler.getInstance());
    }


}
