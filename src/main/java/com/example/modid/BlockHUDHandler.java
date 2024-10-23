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
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockHUDHandler {

    private static final BlockHUDHandler INSTANCE = new BlockHUDHandler(); // Singleton instance
    private final Map<Block, String> blockDisplayTextMap = new HashMap<>();
    private final Map<Block, ResourceLocation> blockImageMap = new HashMap<>();
    private final Map<String, ResourceLocation> oreDictImageMap = new HashMap<>();

    private final Map<Block, ItemStack> blockImageItemMap = new HashMap<>();
    private Vec3d cachedPlayerPosition;
    private Vec3d cachedLookVector;
    private float cachedPlayerEyeHeight;

    private static final double BLOCK_REACH_OVERLAY_DISTANCE = 5.0;  // Max distance for ray tracing
    private static final float MAX_CROSSHAIR_ALPHA = 0.7f; // Max alpha for crosshair
    private static final float FADE_IN_SPEED = 0.02f; // Speed of fade in
    private static final float FADE_OUT_SPEED = 0.06f; // Speed of fade out
    private static final ResourceLocation DEFAULT_BLOCK_IMAGE = new ResourceLocation(Tags.MOD_ID + ":textures/gui/interact.png");
    private static final ResourceLocation CROSSHAIR_TEXTURE = new ResourceLocation(Tags.MOD_ID + ":textures/gui/crosshair.png");

    private final Minecraft MC = Minecraft.getMinecraft();
    private final FontRenderer FONT_RENDERER = MC.fontRenderer;

    // Default resource location for the image
    private int imageWidth = 16; // Set to the size of your image
    private int imageHeight = 16; // Set to the size of your image
    private int crosshairSize = 16; // Set to the size of your crosshair image
    private float crosshairAlpha = 0.0f; // Current alpha value for fading

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

    public void registerBlockOreDictImage(String oreDictKey, ResourceLocation image) {
        oreDictImageMap.put(oreDictKey, image);
    }

    /**
     * Register a block with a custom image and associated item to show the image when held.
     *
     * @param block The block to register.
     * @param customImage The custom image resource location.
     * @param itemStack The item the player must hold to display the image.
     */
    public void registerBlockImageItem(Block block, ResourceLocation customImage, ItemStack itemStack) {
        blockImageItemMap.put(block, itemStack);
        blockImageMap.put(block, customImage);
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
        Block block = getLookedAtBlock(player);

        // Crosshair is at the center of the screen
        int overlayX = (int) (scaledWidth / 2);
        int overlayY = (int) (scaledHeight / 2); // Centered on crosshair

        // Inverted fade logic: Fade IN when NOT looking at a block, Fade OUT when looking at a block
        crosshairAlpha = (block == null) ?
                Math.min(crosshairAlpha + FADE_IN_SPEED, MAX_CROSSHAIR_ALPHA) :
                Math.max(crosshairAlpha - FADE_OUT_SPEED, 0.0f);

        // Render the crosshair with the current alpha
        drawCrosshair(overlayX, overlayY, 0.7f, crosshairAlpha);

        if (block != null) {
            // If the block is registered, render the overlay text and image
            String customText = blockDisplayTextMap.getOrDefault(block, " ");
            // Render the custom overlay text centered at the crosshair
            // Render the block image only if the player is holding the correct item
            drawCenteredString(FONT_RENDERER, customText, overlayX, overlayY + 20, 0xFFFFFF);
            drawImage(overlayX - (imageWidth / 2), overlayY - (imageHeight / 2), block, player);
        }

        // Check if the player's position, eye height, and look direction haven't changed
        if (cachedPlayerPosition != null && cachedPlayerPosition.equals(player.getPositionVector())
                && cachedPlayerEyeHeight == player.getEyeHeight()
                && cachedLookVector != null && cachedLookVector.equals(player.getLookVec())) {
            return; // No updates needed
        }

        // Update cached values
        cachedPlayerPosition = player.getPositionVector();
        cachedLookVector = player.getLookVec();
        cachedPlayerEyeHeight = player.getEyeHeight();
    }

    private void drawCrosshair(int x, int y, float scale, float alpha) {
        TextureManager textureManager = MC.getTextureManager();
        textureManager.bindTexture(CROSSHAIR_TEXTURE);

        int scaledSize = (int) (crosshairSize * scale);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(1.0f, 1.0f, 1.0f, alpha);

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
        if (event.getType() == RenderGameOverlayEvent.ElementType.CROSSHAIRS) {
            event.setCanceled(true); // Cancel default crosshair rendering
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

    private void drawImage(int x, int y, Block block, EntityPlayer player) {
        // Check if the block has an associated item and if the player is holding it
        ItemStack requiredItem = blockImageItemMap.get(block);

        // Check if the required item is valid
        if (requiredItem != null && !requiredItem.isEmpty()) {
            ItemStack heldItem = player.getHeldItemMainhand();

            // Ensure that the held item is also valid
            if (!heldItem.isEmpty() && heldItem.getItem() != requiredItem.getItem()) {
                return;  // Do not render the image if the player isn't holding the required item
            }
        }


        // Check for ore dictionary key matches and get the image
        String oreDictKey = getOreDictKeyForBlock(block);  // Helper method to find the oreDict key
        ResourceLocation oreDictImage = (oreDictKey != null) ? oreDictImageMap.get(oreDictKey) : null;

        // Use oreDictImage if available, otherwise fall back to block-specific image or default image
        ResourceLocation image = oreDictImage != null
                ? oreDictImage
                : blockImageMap.getOrDefault(block, DEFAULT_BLOCK_IMAGE);

        // Bind the texture and render it
        TextureManager textureManager = MC.getTextureManager();
        textureManager.bindTexture(image);

        // Enable blending for transparency
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // Draw the image at the specified size
        drawTexturedModalRect(x, y, 0, 0, imageWidth, imageHeight);

        // Disable blending after rendering
        GlStateManager.disableBlend();
    }


    private final Map<Block, String> oreDictKeyCache = new HashMap<>();

    private String getOreDictKeyForBlock(Block block) {
        // Check the cache first to avoid redundant calculations
        if (oreDictKeyCache.containsKey(block)) {
            return oreDictKeyCache.get(block);  // Return cached result
        }

        // Iterate through oreDictImageMap and check if the block matches any oreDict keys
        for (Map.Entry<String, ResourceLocation> entry : oreDictImageMap.entrySet()) {
            String oreDictKey = entry.getKey();
            if (isBlockInOreDict(block, oreDictKey)) {
                // Cache the result before returning
                oreDictKeyCache.put(block, oreDictKey);
                return oreDictKey;  // Return the first matching oreDict key
            }
        }

        // Cache the null result as well for blocks that don't match any keys
        oreDictKeyCache.put(block, null);
        return null;  // No matching oreDict key found
    }

    private boolean isBlockInOreDict(Block block, String oreDictKey) {
        // Use OreDictionary to check if the block matches the oreDictKey
        int oreId = OreDictionary.getOreID(oreDictKey);

        // Create an ItemStack from the block
        ItemStack blockItemStack = new ItemStack(block);

        // Check if the ItemStack is empty or invalid before proceeding
        if (blockItemStack.isEmpty()) {
            return false;  // Block does not have a valid ItemStack, so it's not in the ore dictionary
        }

        // Get the ore dictionary IDs for the block's ItemStack
        int[] blockOreIDs = OreDictionary.getOreIDs(blockItemStack);

        // Check if any of the block's OreDictionary IDs match the oreDictKey ID
        for (int id : blockOreIDs) {
            if (id == oreId) {
                return true;  // Block matches the oreDict key
            }
        }

        return false;  // No match found
    }

    private void drawTexturedModalRect(int x, int y, int u, int v, int width, int height) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(x, y + height, 0).tex((float) u / width, (float) (v + height) / height).endVertex();
        buffer.pos(x + width, y + height, 0).tex((float) (u + width) / width, (float) (v + height) / height).endVertex();
        buffer.pos(x + width, y, 0).tex((float) (u + width) / width, (float) v / height).endVertex();
        buffer.pos(x, y, 0).tex((float) u / width, (float) v / height).endVertex();
        tessellator.draw();
    }
}
