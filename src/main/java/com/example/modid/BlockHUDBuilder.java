package com.example.modid;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class BlockHUDBuilder {

    private final BlockHUDHandler blockHUDHandler = BlockHUDHandler.getInstance();
    private String defaultText = "Open Block";

    public BlockHUDBuilder withDefaultText(String text) {
        this.defaultText = text;
        return this;
    }

    public BlockHUDBuilder registerBlock(Block block) {
        blockHUDHandler.registerBlockHUD(block, defaultText);
        return this;
    }

    public BlockHUDBuilder registerBlock(Block block, String customText) {
        blockHUDHandler.registerBlockHUD(block, customText);
        return this;
    }

    public BlockHUDBuilder registerBlockImage(Block block, ResourceLocation resourceLocation) {
        blockHUDHandler.registerBlockImageItem(block, resourceLocation, ItemStack.EMPTY);
        return this;
    }

    public BlockHUDBuilder registerBlockImageItem(Block block, ResourceLocation resourceLocation, ItemStack itemStack) {
        blockHUDHandler.registerBlockImageItem(block, resourceLocation, itemStack);
        return this;
    }

    public BlockHUDBuilder registerBlockImageItem(List<Block> blocks, ResourceLocation resourceLocation, ItemStack itemStack) {
        for (Block block : blocks) {
            blockHUDHandler.registerBlockImageItem(block, resourceLocation, itemStack);
        }
        return this;
    }


    public BlockHUDBuilder registerBlocks(List<Block> blocks, String customText) {
        blockHUDHandler.registerBlocksHUD(blocks, customText);
        return this;
    }

    public BlockHUDHandler build() {
        return blockHUDHandler; // Return the configured instance
    }
}
