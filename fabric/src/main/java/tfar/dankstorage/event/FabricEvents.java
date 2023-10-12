package tfar.dankstorage.event;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import tfar.dankstorage.item.DankItem;
import tfar.dankstorage.utils.Utils;
import tfar.dankstorage.world.ClientData;

public class FabricEvents {

    static Minecraft mc = Minecraft.getInstance();

    public static void renderStack(GuiGraphics matrixStack, float v) {
        Player player = mc.player;
        if (player == null)
            return;
        if (!(player.containerMenu instanceof InventoryMenu)) return;
        ItemStack bag = player.getMainHandItem();
        if (!(bag.getItem() instanceof DankItem)) {
            bag = player.getOffhandItem();
            if (!(bag.getItem() instanceof DankItem))
                return;
        }
        int xStart = mc.getWindow().getGuiScaledWidth() / 2;
        int yStart = mc.getWindow().getGuiScaledHeight();

        ItemStack toPlace = ClientData.selectedItem;

        if (!toPlace.isEmpty()) {


            Integer color = toPlace.getItem().getRarity(toPlace).color.getColor();

            int c = color != null ? color : 0xFFFFFF;


            final int itemX = xStart - 150;
            final int itemY = yStart - 25;
            renderHotbarItem(matrixStack,itemX, itemY, 0, player, toPlace);
        }
        final int stringX = xStart - 155;
        final int stringY = yStart - 10;
        String mode = Utils.getUseType(bag).name();
        matrixStack.drawString(mc.font, mode, stringX, stringY, 0xffffff);
        RenderSystem.setShaderTexture(0, GUI_ICONS_LOCATION);
        }

    private static final ResourceLocation GUI_ICONS_LOCATION = new ResourceLocation("textures/gui/icons.png");


    private static void renderHotbarItem(GuiGraphics guiGraphics,int x, int y, float partialTicks, Player player, ItemStack stack) {
        float f = (float) stack.getPopTime() - partialTicks;
        if (f > 0.0F) {
            guiGraphics.pose().pushPose();
            float f1 = 1.0F + f / 5.0F;
            guiGraphics.pose().translate((float) (x + 8), (float) (y + 12), 0.0F);
            guiGraphics.pose().scale(1.0F / f1, (f1 + 1.0F) / 2.0F, 1.0F);
            guiGraphics.pose().translate((float) (-(x + 8)), (float) (-(y + 12)), 0.0F);
        }

        guiGraphics.renderItemDecorations(mc.font,stack, x, y);
        if (f > 0.0F) {
            guiGraphics.pose().popPose();
        }
        guiGraphics.renderItemDecorations(mc.font,stack, x, y);
    }

}