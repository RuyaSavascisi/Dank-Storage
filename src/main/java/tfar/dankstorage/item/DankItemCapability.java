package tfar.dankstorage.item;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tfar.dankstorage.DankStorage;
import tfar.dankstorage.utils.DankStats;
import tfar.dankstorage.utils.Utils;
import tfar.dankstorage.world.DankInventory;

public class DankItemCapability implements ICapabilityProvider {

    private final ItemStack container;

    public DankItemCapability(ItemStack container) {
        this.container = container;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.orEmpty(cap, LazyOptional.of(this::lookup));
    }

    protected DankInventory lookup() {
        //this can be called clientside, functional storage does so for some reason
        //this should be replaced with a proper inventory at some point
        if (DankStorage.instance.data != null) {
            return Utils.getInventory(container, ServerLifecycleHooks.getCurrentServer().getLevel(Level.OVERWORLD));
        }
        else {
            return new DankInventory(DankStats.zero, -1);
        }
    }
}