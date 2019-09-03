package com.tfar.dankstorage.client;

import net.minecraft.client.renderer.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nullable;
import java.text.DecimalFormat;

public class RenderItemExtended {

  RenderItem itemRender = Minecraft.getMinecraft().getRenderItem();
  public static final RenderItemExtended INSTANCE = new RenderItemExtended();

  public void setZLevel(float z) {
    itemRender.zLevel = z;
  }

  public void renderItemOverlayIntoGUI(FontRenderer fr, ItemStack stack, int xPosition, int yPosition,
                                       @Nullable String text) {
    if (!stack.isEmpty()) {

      if (stack.getItem().showDurabilityBar(stack)) {
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.disableTexture2D();
        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuffer();
        double health = stack.getItem().getDurabilityForDisplay(stack);
        int rgbfordisplay = stack.getItem().getRGBDurabilityForDisplay(stack);
        int i = Math.round(13.0F - (float) health * 13.0F);
        this.draw(vertexbuffer, xPosition + 2, yPosition + 13, 13, 2, 0, 0, 0, 255);
        this.draw(vertexbuffer, xPosition + 2, yPosition + 13, i, 1, rgbfordisplay >> 16 & 255, rgbfordisplay >> 8 & 255, rgbfordisplay & 255, 255);
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
      }

      if (stack.getCount() != 1 || text != null) {
        String s = text == null ? getStringFromInt(stack.getCount()) : text;
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.disableBlend();
        GlStateManager.pushMatrix();
        float scale = .75f;
        GlStateManager.scale(scale, scale, 1.0F);
        fr.drawStringWithShadow(s, (xPosition + 19 - 2 - (fr.getStringWidth(s)*scale)) /scale,
                (yPosition + 6 + 3 + (1 / (scale * scale) - 1) ) /scale, 0xffffff);
        GlStateManager.popMatrix();
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        // Fixes opaque cooldown overlay a bit lower
        // TODO: check if enabled blending still screws things up down
        // the line.
        GlStateManager.enableBlend();
      }

      EntityPlayerSP entityplayersp = Minecraft.getMinecraft().player;
      float f3 = entityplayersp == null ? 0.0F
              : entityplayersp.getCooldownTracker().getCooldown(stack.getItem(),
              Minecraft.getMinecraft().getRenderPartialTicks());

      if (f3 > 0.0F) {
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.disableTexture2D();
        Tessellator tessellator1 = Tessellator.getInstance();
        BufferBuilder vertexbuffer1 = tessellator1.getBuffer();
        this.draw(vertexbuffer1, xPosition, yPosition + MathHelper.floor(16.0F * (1.0F - f3)), 16,
                MathHelper.ceil(16.0F * f3), 255, 255, 255, 127);
        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
      }
    }
  }

  private static final DecimalFormat decimalFormat = new DecimalFormat("0.#");

  public String getStringFromInt(int number){

    if (number >= 1000000000) return decimalFormat.format(number / 1000000000f) + "b";
    if (number >= 1000000) return decimalFormat.format(number / 1000000f) + "m";
    if (number >= 1000) return decimalFormat.format(number / 1000f) + "k";

    return Float.toString(number).replaceAll("\\.?0*$", "");
  }

  private void draw(BufferBuilder renderer, int x, int y, int width, int height, int red, int green, int blue,
                    int alpha) {
    renderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
    renderer.pos(x, y, 0.0D).color(red, green, blue, alpha).endVertex();
    renderer.pos(x, y + height, 0.0D).color(red, green, blue, alpha).endVertex();
    renderer.pos(x + width, y + height, 0.0D).color(red, green, blue, alpha).endVertex();
    renderer.pos(x + width, y, 0.0D).color(red, green, blue, alpha).endVertex();
    Tessellator.getInstance().draw();
  }

}