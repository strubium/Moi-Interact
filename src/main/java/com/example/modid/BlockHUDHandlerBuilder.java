package com.example.modid;

import net.minecraft.block.Block;

import java.util.ArrayList;
import java.util.List;

public class BlockHUDHandlerBuilder {

    private final BlockHUDHandler blockHUDHandler = BlockHUDHandler.getInstance();
    private final List<Block> blocksToRegister = new ArrayList<>();
    private String defaultText = "Open Block";

    public BlockHUDHandlerBuilder withDefaultText(String text) {
        this.defaultText = text;
        return this;
    }

    public BlockHUDHandlerBuilder registerBlock(Block block, String customText) {
        blockHUDHandler.registerBlockHUD(block, customText);
        return this;
    }

    public BlockHUDHandlerBuilder registerBlocks(List<Block> blocks, String customText) {
        for (Block block : blocks) {
            blockHUDHandler.registerBlockHUD(block, customText);
        }
        return this;
    }

    public BlockHUDHandler build() {
        return blockHUDHandler; // Return the configured instance
    }

    public BlockHUDHandlerBuilder addBlock(Block block) {
        blocksToRegister.add(block);
        return this;
    }
}
