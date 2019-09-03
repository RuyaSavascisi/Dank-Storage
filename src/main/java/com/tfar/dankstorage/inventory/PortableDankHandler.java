package com.tfar.dankstorage.inventory;

import com.tfar.dankstorage.util.Utils;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class PortableDankHandler extends DankHandler {

  public final ItemStack bag;

  public PortableDankHandler(ItemStack bag) {
    this(Utils.getSlotCount(bag),Utils.getStackLimit(bag),bag);
  }

  protected PortableDankHandler(int size, int stacklimit, ItemStack bag) {
    super(size,stacklimit);
    this.bag = bag;
    readItemStack();
  }

  public void writeItemStack() {
    if (false) {
      return;
    }
    if (bag.isEmpty()) {
      if (bag.hasTagCompound()) {
        bag.getTagCompound().removeTag("Items");
        if (bag.getTagCompound().isEmpty()) {
          bag.setTagCompound(null);
        }
      }
    } else {
      boolean pickup = Utils.autoPickup(bag);
      boolean isVoid = Utils.autoVoid(bag);
      boolean construction = Utils.construction(bag);
      int selectedSlot = Utils.getSelectedSlot(bag);
      bag.setTagCompound(serializeNBT());
      bag.getTagCompound().setBoolean("pickup",pickup);
      bag.getTagCompound().setBoolean("void",isVoid);
      bag.getTagCompound().setBoolean("construction",construction);
      bag.getTagCompound().setInteger("selectedSlot",selectedSlot);
    }
  }

  @Override
  public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
    super.setStackInSlot(slot, stack);
    this.writeItemStack();
  }

  public void readItemStack() {
    if (bag.hasTagCompound()) {
      deserializeNBT(bag.getTagCompound());
    }
  }
}
