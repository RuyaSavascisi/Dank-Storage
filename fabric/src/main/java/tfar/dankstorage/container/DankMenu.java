package tfar.dankstorage.container;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import tfar.dankstorage.DankStorageFabric;
import tfar.dankstorage.inventory.LockedSlot;
import tfar.dankstorage.utils.PickupMode;
import tfar.dankstorage.world.DankInventory;
import tfar.dankstorage.inventory.DankSlot;
import tfar.dankstorage.item.DankItem;
import tfar.dankstorage.utils.Utils;

public class DankMenu extends AbstractDankMenu {

    public ItemStack bag;

    //clientside
    public DankMenu(MenuType<?> type, int windowId, Inventory inv, int rows) {
        this(type, windowId, inv, new DankInventory(Utils.getStatsfromRows(rows), -1));
    }

    public DankMenu(MenuType<?> type, int windowId, Inventory inv, DankInventory dankInventory) {
        super(type, windowId, inv, dankInventory);
        Player player = inv.player;
        this.bag = player.getMainHandItem().getItem() instanceof DankItem ? player.getMainHandItem() : player.getOffhandItem();
        addDankSlots();
        addPlayerSlots(inv, inv.selected);
    }

    @Override
    protected DataSlot getServerPickupData() {
        return new DataSlot() {
            @Override
            public int get() {
                return Utils.getPickupMode(bag).ordinal();
            }

            @Override
            public void set(int pValue) {
                Utils.setPickupMode(bag, PickupMode.PICKUP_MODES[pValue]);
            }
        };
    }

    public static DankMenu t1(int id, Inventory inv) {
        return new DankMenu(DankStorageFabric.portable_dank_1_container, id, inv, 1);
    }

    public static DankMenu t2(int id, Inventory inv) {
        return new DankMenu(DankStorageFabric.portable_dank_2_container, id, inv, 2);
    }

    public static DankMenu t3(int id, Inventory inv) {
        return new DankMenu(DankStorageFabric.portable_dank_3_container, id, inv, 3);
    }

    public static DankMenu t4(int id, Inventory inv) {
        return new DankMenu(DankStorageFabric.portable_dank_4_container, id, inv, 4);
    }

    public static DankMenu t5(int id, Inventory inv) {
        return new DankMenu(DankStorageFabric.portable_dank_5_container, id, inv, 5);
    }

    public static DankMenu t6(int id, Inventory inv) {
        return new DankMenu(DankStorageFabric.portable_dank_6_container, id, inv, 6);
    }

    public static DankMenu t7(int id, Inventory inv) {
        return new DankMenu(DankStorageFabric.portable_dank_7_container, id, inv, 9);
    }

    public static DankMenu t1s(int id, Inventory inv, DankInventory dankInventory) {
        return new DankMenu(DankStorageFabric.portable_dank_1_container, id, inv, dankInventory);
    }

    public static DankMenu t2s(int id, Inventory inv, DankInventory dankInventory) {
        return new DankMenu(DankStorageFabric.portable_dank_2_container, id, inv, dankInventory);
    }

    public static DankMenu t3s(int id, Inventory inv, DankInventory dankInventory) {
        return new DankMenu(DankStorageFabric.portable_dank_3_container, id, inv, dankInventory);
    }

    public static DankMenu t4s(int id, Inventory inv, DankInventory dankInventory) {
        return new DankMenu(DankStorageFabric.portable_dank_4_container, id, inv, dankInventory);
    }

    public static DankMenu t5s(int id, Inventory inv, DankInventory dankInventory) {
        return new DankMenu(DankStorageFabric.portable_dank_5_container, id, inv, dankInventory);
    }

    public static DankMenu t6s(int id, Inventory inv, DankInventory dankInventory) {
        return new DankMenu(DankStorageFabric.portable_dank_6_container, id, inv, dankInventory);
    }

    public static DankMenu t7s(int id, Inventory inv, DankInventory dankInventory) {
        return new DankMenu(DankStorageFabric.portable_dank_7_container, id, inv, dankInventory);
    }

    @Override
    protected void addDankSlots() {
        int slotIndex = 0;
        for (int row = 0; row < rows; ++row) {
            for (int col = 0; col < 9; ++col) {
                int x = 8 + col * 18;
                int y = row * 18 + 18;
                this.addSlot(new DankSlot(dankInventory, slotIndex, x, y));
                slotIndex++;
            }
        }
    }

    @Override
    public void setFrequency(int freq) {
        Utils.getOrCreateSettings(bag).putInt(Utils.FREQ, freq);
    }
}

