package com.example.modid;

import net.minecraft.block.Block;

import java.util.ArrayList;
import java.util.List;

public class BlockHUDBuilder {

    private final BlockHUDHandler blockHUDHandler = BlockHUDHandler.getInstance();
    private final List<Block> blocksToRegister = new ArrayList<>();
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

    public BlockHUDBuilder registerBlocks(List<Block> blocks, String customText) {
        for (Block block : blocks) {
            blockHUDHandler.registerBlockHUD(block, customText);
        }
        return this;
    }

    public BlockHUDHandler build() {
        return blockHUDHandler; // Return the configured instance
    }

    public BlockHUDBuilder addBlock(Block block) {
        blocksToRegister.add(block);
        return this;
    }
}
