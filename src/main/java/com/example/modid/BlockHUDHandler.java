package com.example.modid;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockHUDHandler {

    private static final BlockHUDHandler INSTANCE = new BlockHUDHandler(); // Singleton instance
    private final Map<Block, String> blockDisplayTextMap = new HashMap<>();
    private Vec3d cachedPlayerPosition;
    private Vec3d cachedLookVector;
    private float cachedPlayerEyeHeight;

    private static final double BLOCK_REACH_OVERLAY_DISTANCE = 5.0;  // Max distance for ray tracing

    private final Minecraft MC = Minecraft.getMinecraft();
    private final FontRenderer FONT_RENDERER = MC.fontRenderer;

    // Resource location for your image (make sure the path is correct)
    private final ResourceLocation blockImage = new ResourceLocation(Tags.MOD_ID + ":textures/gui/interact.png");
    private final ResourceLocation crosshairTexture = new ResourceLocation(Tags.MOD_ID + ":textures/gui/crosshair.png");
    private int imageWidth = 16;  // Set to the size of your image
    private int imageHeight = 16; // Set to the size of your image
    private int crosshairSize = 16; // Set to the size of your crosshair image

    public static BlockHUDHandler getInstance() {
        return INSTANCE;
    }
    /**
     * Register a block and its associated custom HUD text.
     *
     * @param block The block to register.
     * @param customText The custom text to display for this block.
     */
    public void registerBlockHUD(Block block, String customText) {
        blockDisplayTextMap.put(block, customText);
    }

    /**
     * Register a list of blocks and associate the same custom HUD text with each block.
     *
     * @param blocks The list of blocks to register.
     * @param customText The custom text to display for these blocks.
     */
    public void registerBlocksHUD(List<Block> blocks, String customText) {
        for (Block block : blocks) {
            blockDisplayTextMap.put(block, customText);
        }
    }

    public void handleOpenBlockHUD(double scaledWidth, double scaledHeight) {
        EntityPlayer player = MC.player;

        // Get the block the player is currently looking at
        Block block = getLookedAtBlock(player);

        // Crosshair is at the center of the screen
        int overlayX = (int) (scaledWidth / 2);
        int overlayY = (int) (scaledHeight / 2);  // Centered on crosshair

        if (block == null) {
            // Render the custom crosshair if the player is NOT looking at a block
            drawCrosshair(overlayX, overlayY, 0.7f,0.7f);
        } else {
            // If the block is registered, render the overlay text and image
            String customText = blockDisplayTextMap.getOrDefault(block, " ");

            // Render the custom overlay text centered at the crosshair
            drawCenteredString(FONT_RENDERER, customText, overlayX, overlayY + 20, 0xFFFFFF);  // Slightly below the crosshair

            // Render the block image at the crosshair
            drawImage(overlayX - (imageWidth / 2), overlayY - (imageHeight / 2)); // Centered on the crosshair
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
    }

    private void drawCrosshair(int x, int y, float scale, float alpha) {
        // Bind the crosshair texture
        TextureManager textureManager = MC.getTextureManager();
        textureManager.bindTexture(crosshairTexture);

        // Calculate the scaled size
        int scaledSize = (int) (crosshairSize * scale);

        // Set up blending for transparency
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // Draw the texture centered on the screen with transparency
        GlStateManager.color(1.0f, 1.0f, 1.0f, alpha); // Set the color and alpha (transparency)
        drawTexturedModalRect(x - (scaledSize / 2), y - (scaledSize / 2), 0, 0, scaledSize, scaledSize);

        GlStateManager.disableBlend();
    }



    /**
     * Gets the block the player is currently looking at, if within range.
     *
     * @param player The player to check.
     * @return The block the player is looking at, or null if none.
     */
    private Block getLookedAtBlock(EntityPlayer player) {
        Vec3d originVector = player.getPositionVector().add(0, player.getEyeHeight(), 0);
        RayTraceResult rtr = player.world.rayTraceBlocks(originVector, originVector.add(player.getLookVec().scale(BLOCK_REACH_OVERLAY_DISTANCE)), false, true, false);
        if (rtr != null) {
            IBlockState state = player.world.getBlockState(rtr.getBlockPos());
            return state.getBlock();
        }
        return null;
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Pre event) {
        // Ensure we are in the correct phase and only for the CROSSHAIR type
        if (event.getType() == RenderGameOverlayEvent.ElementType.CROSSHAIRS) {
            // Cancel the default crosshair rendering
            event.setCanceled(true);
        }

        // Ensure we are in the correct phase and only for the ALL type
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) {
            return; // Only render for the ALL type
        }

        // Get the screen dimensions
        double scaledWidth = event.getResolution().getScaledWidth_double();
        double scaledHeight = event.getResolution().getScaledHeight_double();

        // Call the block HUD handler to display relevant info
        handleOpenBlockHUD(scaledWidth, scaledHeight);
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

    private void drawImage(int x, int y) {
        TextureManager textureManager = MC.getTextureManager();
        textureManager.bindTexture(blockImage);

        // Enable blending for transparency
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // Draw the image at the specified size
        drawTexturedModalRect(x, y, 0, 0, imageWidth, imageHeight);

        // Disable blending after rendering
        GlStateManager.disableBlend();
    }

    private void drawTexturedModalRect(int x, int y, int u, int v, int width, int height) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(x, y + height, 0).tex((float)u / width, (float)(v + height) / height).endVertex();
        buffer.pos(x + width, y + height, 0).tex((float)(u + width) / width, (float)(v + height) / height).endVertex();
        buffer.pos(x + width, y, 0).tex((float)(u + width) / width, (float)v / height).endVertex();
        buffer.pos(x, y, 0).tex((float)u / width, (float)v / height).endVertex();
        tessellator.draw();
    }
}
