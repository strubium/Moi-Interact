package com.example.modid;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

public class BlockHUDHandler {

    private final Map<Block, String> blockDisplayTextMap = new HashMap<>();
    private boolean shouldRenderBlockOverlay = false;
    private Vec3d cachedPlayerPosition;
    private Vec3d cachedLookVector;
    private float cachedPlayerEyeHeight;

    private static final double OPENBLOCK_REACH_OVERLAY_DISTANCE = 5.0;  // Max distance for ray tracing

    private final Minecraft MC = Minecraft.getMinecraft();
    private final FontRenderer FONT_RENDERER = MC.fontRenderer;

    /**
     * Register a block and its associated custom HUD text.
     *
     * @param block The block to register.
     * @param customText The custom text to display for this block.
     */
    public void registerBlockHUD(Block block, String customText) {
        blockDisplayTextMap.put(block, customText);
    }

    public void handleOpenBlockHUD(RenderGameOverlayEvent.Pre event, double scaledWidth, double scaledHeight) {
        EntityPlayer player = MC.player;

        // Render the overlay if required
        if (shouldRenderBlockOverlay) {
            // Crosshair is at the center of the screen
            int overlayX = (int) (scaledWidth / 2);
            int overlayY = (int) (scaledHeight / 2) + 10;  // Slight offset from crosshair

            Block block = getLookedAtBlock(player);
            if (block != null) {
                String customText = blockDisplayTextMap.getOrDefault(block, "Open Block");

                // Render the custom overlay text centered at the crosshair
                drawCenteredString(FONT_RENDERER, customText, overlayX, overlayY, 0xFFFFFF);  // White color
            }
        }

        // Check if the player's position, eye height, and look direction haven't changed
        if (cachedPlayerPosition != null && cachedPlayerPosition.equals(player.getPositionVector())
                && cachedPlayerEyeHeight == player.getEyeHeight()
                && cachedLookVector != null && cachedLookVector.equals(player.getLookVec())) {
            return;
        }

        // Update cached position, look vector, and eye height
        cachedPlayerPosition = player.getPositionVector();
        cachedLookVector = player.getLookVec();
        cachedPlayerEyeHeight = player.getEyeHeight();
        shouldRenderBlockOverlay = false;

        // Calculate the player's look direction and reach
        Vec3d originVector = player.getPositionVector().add(0, player.getEyeHeight(), 0);
        RayTraceResult rtr = player.world.rayTraceBlocks(originVector, originVector.add(player.getLookVec().scale(OPENBLOCK_REACH_OVERLAY_DISTANCE)), false, true, false);

        if (rtr != null) {
            Block block = player.world.getBlockState(rtr.getBlockPos()).getBlock();

            // If the block is registered, trigger the overlay
            if (blockDisplayTextMap.containsKey(block)) {
                shouldRenderBlockOverlay = true;
            }
        }
    }

    /**
     * Gets the block the player is currently looking at, if within range.
     *
     * @param player The player to check.
     * @return The block the player is looking at, or null if none.
     */
    private Block getLookedAtBlock(EntityPlayer player) {
        Vec3d originVector = player.getPositionVector().add(0, player.getEyeHeight(), 0);
        RayTraceResult rtr = player.world.rayTraceBlocks(originVector, originVector.add(player.getLookVec().scale(OPENBLOCK_REACH_OVERLAY_DISTANCE)), false, true, false);
        if (rtr != null) {
            IBlockState state = player.world.getBlockState(rtr.getBlockPos());
            return state.getBlock();
        }
        return null;
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Pre event) {
        // Get the screen dimensions
        double scaledWidth = event.getResolution().getScaledWidth_double();
        double scaledHeight = event.getResolution().getScaledHeight_double();

        // Call the block HUD handler to display relevant info
        handleOpenBlockHUD(event, scaledWidth, scaledHeight);
    }

    /**
     * Helper method to draw a centered string on the screen.
     *
     * @param fontRenderer The font renderer to use for drawing the string.
     * @param text The text to render.
     * @param x The x position (centered).
     * @param y The y position.
     * @param color The color of the text.
     */
    private void drawCenteredString(FontRenderer fontRenderer, String text, int x, int y, int color) {
        int width = fontRenderer.getStringWidth(text);
        fontRenderer.drawString(text, x - (width / 2), y, color);
    }
}
