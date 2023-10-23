package tfar.dankstorage.world;

import net.minecraft.core.NonNullList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mth;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import tfar.dankstorage.Constants;
import tfar.dankstorage.DankStorageFabric;
import tfar.dankstorage.ModTags;
import tfar.dankstorage.inventory.DankInterface;
import tfar.dankstorage.mixin.SimpleContainerAccess;
import tfar.dankstorage.utils.*;

import java.util.stream.IntStream;

public class DankInventoryFabric extends SimpleContainer implements DankInterface {

    public DankStats dankStats;
    protected NonNullList<ItemStack> ghostItems;
    protected int frequency;
    public boolean frequencyLocked = true;

    protected int textColor = -1;

    public MinecraftServer server;

    public DankInventoryFabric(DankStats stats, int frequency) {
        super(stats.slots);
        this.dankStats = stats;
        this.ghostItems = NonNullList.withSize(stats.slots, ItemStack.EMPTY);
        this.frequency = frequency;
    }

    @Override
    public DankStats getDankStats() {
        return dankStats;
    }

    public void upgradeTo(DankStats stats) {

        //can't downgrade inventories
        if (stats.ordinal() <= dankStats.ordinal()) {
            return;
        }
        Constants.LOG.debug("Upgrading dank #{} from tier {} to {}", frequency, dankStats.name(), stats.name());
        setTo(stats);
    }

    //like upgradeTo, but can go backwards, should only be used by commands
    public void setTo(DankStats stats) {
        this.dankStats = stats;
        copyItems();
    }

    @Override
    public int getContainerSizeDank() {
        return getContainerSize();
    }

    private void copyItems() {

        NonNullList<ItemStack> newStacks = NonNullList.withSize(dankStats.slots, ItemStack.EMPTY);
        NonNullList<ItemStack> newGhostStacks = NonNullList.withSize(dankStats.slots, ItemStack.EMPTY);

        //don't copy nonexistent items
        int oldSlots = getContainerSize();
        int max = Math.min(oldSlots, dankStats.slots);
        for (int i = 0; i < max; i++) {
            ItemStack oldStack = getItem(i);
            ItemStack oldGhost = getGhostItem(i);
            newStacks.set(i, oldStack);
            newGhostStacks.set(i, oldGhost);
        }

        //caution, will void all current items
        $setSize(dankStats.slots);

        ((SimpleContainerAccess) this).setItems(newStacks);
        setGhostItems(newGhostStacks);
        setChanged();
    }

    protected void setGhostItems(NonNullList<ItemStack> newGhosts) {
        ghostItems = newGhosts;
    }

    @Override
    public ItemStack addItemDank(int slot, ItemStack stack) {
        return addStack(slot,stack);
    }

    ItemStack addStack(int slot,ItemStack stack) {
        ItemStack existing = this.getItem(slot);
        if (ItemStack.isSameItemSameTags(existing, stack)) {
            this.moveItemsBetweenStacks(stack, existing);
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }
        }
        return stack;
    }

    private void moveItemsBetweenStacks(ItemStack itemStack, ItemStack itemStack2) {
        int i = Math.min(this.getMaxStackSize(), itemStack2.getMaxStackSize());
        int j = Math.min(itemStack.getCount(), i - itemStack2.getCount());
        if (j > 0) {
            itemStack2.grow(j);
            itemStack.shrink(j);
            this.setChanged();
        }
    }

    @Override
    public NonNullList<ItemStack> getGhostItems() {
        return ghostItems;
    }

    //distinguish from the mixin accessor
    public void $setSize(int size) {
        ((SimpleContainerAccess)this).setSize(size);
        ((SimpleContainerAccess) this).setItems(NonNullList.withSize(size, ItemStack.EMPTY));
        setGhostItems(NonNullList.withSize(size, ItemStack.EMPTY));
    }

    public void setDankStats(DankStats dankStats) {
        this.dankStats = dankStats;
        $setSize(dankStats.slots);
    }

    @Override
    public int getMaxStackSize() {
        return dankStats.stacklimit;
    }

    public NonNullList<ItemStack> getContents() {
        return items;
    }

    public boolean noValidSlots() {
        return IntStream.range(0, getContainerSize())
                .mapToObj(this::getItem)
                .allMatch(stack -> stack.isEmpty() || stack.is(ModTags.BLACKLISTED_USAGE));
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        boolean checkGhostItem = !hasGhostItem(slot) || getGhostItem(slot).getItem() == stack.getItem();
        return !stack.is(ModTags.BLACKLISTED_STORAGE)
                && checkGhostItem;
    }

    //paranoia
    /*
    @Override


    public boolean canAddItem(ItemStack stack) {
        return !stack.is(Utils.BLACKLISTED_STORAGE) && super.canAddItem(stack);
    }

    //returns the portion of the itemstack that was NOT placed into the storage
    @Override
    public ItemStack addItem(ItemStack itemStack) {
        return itemStack.is(Utils.BLACKLISTED_STORAGE) ? itemStack : super.addItem(itemStack);
    }*/

    @Override
    public int getMaxStackSizeDank() {
        return getMaxStackSize();
    }

    public int calcRedstone() {
        int numStacks = 0;
        float f = 0F;

        for (int slot = 0; slot < this.getContainerSize(); slot++) {
            ItemStack stack = this.getItem(slot);

            if (!stack.isEmpty()) {
                f += (float) stack.getCount() / (float) this.getMaxStackSize();
                numStacks++;
            }
        }

        f /= this.getContainerSize();
        return Mth.floor(f * 14F) + (numStacks > 0 ? 1 : 0);
    }


    @Override
    public void setChanged() {
        super.setChanged();
        if (server != null) {
            DankStorageFabric.getData(frequency,server).write(save());
        }
    }

    @Override
    public int get(int slot) {
        return switch (slot) {
            case 0 -> frequency;
            case 1 -> textColor;
            case 2 -> frequencyLocked ? 1 : 0;
            default -> AbstractContainerMenu.SLOT_CLICKED_OUTSIDE;
        };
    }

    @Override
    public void set(int slot, int value) {
        switch (slot) {
            case 0 -> frequency = value;
            case 1 -> textColor = value;
            case 2 -> frequencyLocked = value == 1;
        }
        setChanged();
    }

    @Override
    public void setItemDank(int slot, ItemStack stack) {
        setItem(slot, stack);
    }

    @Override
    public ItemStack getItemDank(int slot) {
        return getItem(slot);
    }

    @Override
    public ItemStack getGhostItem(int slot) {
        return ghostItems.get(slot);
    }

    public void setGhostItem(int slot, Item item) {
        ghostItems.set(slot, new ItemStack(item));
    }


}