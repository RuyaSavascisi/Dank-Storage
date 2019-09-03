package com.tfar.dankstorage.network;

import com.tfar.dankstorage.block.DankItemBlock;
import com.tfar.dankstorage.util.Utils;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;


public class CMessageToggleConstruction implements IMessage {

  public CMessageToggleConstruction() {
  }

  /**
   * Convert from the supplied buffer into your specific message type
   *
   * @param buf
   */
  @Override
  public void fromBytes(ByteBuf buf) {}

  /**
   * Deconstruct your message into the supplied byte buffer
   *
   * @param buf
   */
  @Override
  public void toBytes(ByteBuf buf) {}

  public static class Handler implements IMessageHandler<CMessageToggleConstruction, IMessage> {
    @Override
    public IMessage onMessage(CMessageToggleConstruction message, MessageContext ctx) {
      FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(ctx));
      return null;
    }

    private void handle(MessageContext ctx) {
      EntityPlayer player = ctx.getServerHandler().player;
      if (player.getHeldItemMainhand().getItem() instanceof DankItemBlock) {
        boolean toggle = Utils.construction(player.getHeldItemMainhand());
        player.sendStatusMessage(new TextComponentTranslation("dankstorage.construction." + (toggle ? "disabled" : "enabled")), true);
        player.getHeldItemMainhand().getTagCompound().setBoolean("construction", !toggle);
      }
    }
  }
}
