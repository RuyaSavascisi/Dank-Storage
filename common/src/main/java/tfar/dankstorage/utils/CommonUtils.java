package tfar.dankstorage.utils;

import com.mojang.datafixers.util.Pair;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import tfar.dankstorage.DankStorage;
import tfar.dankstorage.Item.CommonDankItem;

import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CommonUtils {

    public static final int INVALID = -1;

    public static final String SET = "settings";
    private static final DecimalFormat decimalFormat = new DecimalFormat("0.##");

    public static final String SELECTED = "selectedSlot";
    public static final String CON = "construction";

    public static final String MODE = "mode";
    public static final String FREQ = "dankstorage:frequency";

    public static final TagKey<Item> BLACKLISTED_STORAGE = bind(new ResourceLocation(DankStorage.MODID, "blacklisted_storage"));
    public static final TagKey<Item> BLACKLISTED_USAGE = bind(new ResourceLocation(DankStorage.MODID, "blacklisted_usage"));

    public static final TagKey<Item> WRENCHES = bind(new ResourceLocation("forge", "wrenches"));

    private static TagKey<Item> bind(ResourceLocation string) {
        return TagKey.create(Registries.ITEM, string);
    }


    public static String formatLargeNumber(int number) {

        if (number >= 1000000000) return decimalFormat.format(number / 1000000000f) + "b";
        if (number >= 1000000) return decimalFormat.format(number / 1000000f) + "m";
        if (number >= 1000) return decimalFormat.format(number / 1000f) + "k";

        return Float.toString(number).replaceAll("\\.?0*$", "");
    }

    private static List<CraftingRecipe> REVERSIBLE3x3 = new ArrayList<>();
    private static List<CraftingRecipe> REVERSIBLE2x2 = new ArrayList<>();
    private static boolean cached = false;

    public static void uncacheRecipes(RecipeManager manager) {
        cached = false;
    }

    public static Pair<ItemStack,Integer> compress(ItemStack stack, RegistryAccess registryAccess) {

        for (CraftingRecipe recipe : REVERSIBLE3x3) {
            if (recipe.getIngredients().get(0).test(stack)) {
                return Pair.of(recipe.getResultItem(registryAccess),9);
            }
        }

        for (CraftingRecipe recipe : REVERSIBLE2x2) {
            if (recipe.getIngredients().get(0).test(stack)) {
                return Pair.of(recipe.getResultItem(registryAccess),4);
            }
        }
        return Pair.of(ItemStack.EMPTY,0);
    }

    public static boolean canCompress(ServerLevel level, ItemStack stack) {
        if (!cached) {
            REVERSIBLE3x3 = findReversibles(level,3);
            REVERSIBLE2x2 = findReversibles(level,2);
            cached = true;
        }

        for (CraftingRecipe recipe : REVERSIBLE3x3) {
            if (recipe.getIngredients().get(0).test(stack)) {
                return stack.getCount() >=9;
            }
        }

        for (CraftingRecipe recipe : REVERSIBLE2x2) {
            if (recipe.getIngredients().get(0).test(stack)) {
                return stack.getCount()>=4;
            }
        }

        return false;
    }
    public static List<CraftingRecipe> findReversibles(ServerLevel level,int size) {
        List<CraftingRecipe> compactingRecipes = new ArrayList<>();
        List<CraftingRecipe> recipes = level.getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING);

        for (CraftingRecipe recipe : recipes) {
            if (recipe instanceof ShapedRecipe shapedRecipe) {
                int x = shapedRecipe.getWidth();
                int y = shapedRecipe.getHeight();
                if (x == size && x == y) {

                    List<Ingredient> inputs = shapedRecipe.getIngredients();

                    Ingredient first = inputs.get(0);
                    if (first != Ingredient.EMPTY) {
                        boolean same = true;
                        for (int i = 1; i < x * y;i++) {
                            Ingredient next = inputs.get(i);
                            if (next != first) {
                                same = false;
                                break;
                            }
                        }
                        if (same && shapedRecipe.getResultItem(level.registryAccess()).getCount() == 1) {
                            DUMMY.setItem(0,shapedRecipe.getResultItem(level.registryAccess()));

                            level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, DUMMY, level).ifPresent(reverseRecipe -> {
                                if (reverseRecipe.getResultItem(level.registryAccess()).getCount() == size * size) {
                                    compactingRecipes.add(shapedRecipe);
                                }
                            });
                        }
                    }
                }
            }
        }
        return compactingRecipes;
    }
    @SuppressWarnings("ConstantConditions")
    private static final CraftingContainer DUMMY = new TransientCraftingContainer(null,1,1) {
        @Override
        public void setItem(int i, ItemStack itemStack) {
            getItems().set(i, itemStack);
        }

        @Override
        public ItemStack removeItem(int i, int j) {
            return ContainerHelper.removeItem(getItems(), i, j);
        }
    };


    public static CompoundTag getSettings(ItemStack bag) {
        return hasSettings(bag) ? bag.getTag().getCompound(SET) : null;
    }

    public static CompoundTag getOrCreateSettings(ItemStack bag) {
        if (hasSettings(bag)) {
            return bag.getTag().getCompound(SET);
        } else {
            bag.getOrCreateTag().put(SET, new CompoundTag());
            return getSettings(bag);
        }
    }

    private static boolean hasSettings(ItemStack bag) {
        return bag.hasTag() && bag.getTag().contains(SET);
    }

    public static PickupMode getPickupMode(ItemStack bag) {
        CompoundTag tag = getSettings(bag);
        if (tag != null) {
            return PickupMode.PICKUP_MODES[tag.getInt(MODE)];
        }
        return PickupMode.none;
    }

    public static void setPickupMode(ItemStack bag, PickupMode mode) {
        CompoundTag tag = getOrCreateSettings(bag);
        tag.putInt(MODE,mode.ordinal());
    }

    public static boolean isConstruction(ItemStack bag) {
        CompoundTag settings = getSettings(bag);
        return settings != null && settings.getInt(CON) == UseType.construction.ordinal();
    }

    //0,1,2,3
    public static void cyclePickupMode(ItemStack bag, Player player) {
        int ordinal = getOrCreateSettings(bag).getInt(MODE);
        ordinal++;
        if (ordinal > PickupMode.PICKUP_MODES.length - 1) ordinal = 0;
        getOrCreateSettings(bag).putInt(MODE, ordinal);
        player.displayClientMessage(
                translatable("dankstorage.mode." + PickupMode.PICKUP_MODES[ordinal].name()), true);
    }

    public static UseType getUseType(ItemStack bag) {
        CompoundTag settings = getSettings(bag);
        return settings != null ? UseType.useTypes[settings.getInt(CON)] : UseType.bag;
    }

    //0,1,2
    public static void cyclePlacement(ItemStack bag, Player player) {
        CompoundTag tag = getOrCreateSettings(bag);
        int ordinal = tag.getInt(CON);
        ordinal++;
        if (ordinal >= UseType.useTypes.length) ordinal = 0;
        tag.putInt(CON, ordinal);
        player.displayClientMessage(
                translatable("dankstorage.usetype." + UseType.useTypes[ordinal].name()), true);
    }

    //this can be 0 - 80
    public static int getSelectedSlot(ItemStack bag) {
        CompoundTag settings = getSettings(bag);
        return settings != null && settings.contains(SELECTED) ? settings.getInt(SELECTED) : INVALID;
    }

    public static void setSelectedSlot(ItemStack bag, int slot) {
        getOrCreateSettings(bag).putInt(SELECTED,slot);
    }

    //make sure to return an invalid ID for unassigned danks
    public static int getFrequency(ItemStack bag) {
        CompoundTag settings = getSettings(bag);
        if (settings != null && settings.contains(FREQ)) {
            return settings.getInt(FREQ);
        }
        return INVALID;
    }

    public static void setFrequency(ItemStack bag,int frequency) {
        getOrCreateSettings(bag).putInt(FREQ,frequency);
    }

    public static MutableComponent translatable(String s) {
        return Component.translatable(s);
    }

    public static MutableComponent translatable(String string, Object... objects) {
        return Component.translatable(string, objects);
    }

    public static MutableComponent literal(String s) {
        return Component.literal(s);
    }

    public static boolean oredict(ItemStack bag) {
        return bag.hasTag() && getSettings(bag).getBoolean("tag");
    }

    public static void warn(Player player, DankStats item, DankStats inventory) {
        player.sendSystemMessage(literal("Dank Item Level "+item.ordinal() +" cannot open Dank Inventory Level "+inventory.ordinal()));
    }

    public static int getNbtSize(@Nullable CompoundTag nbt) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        buffer.writeNbt(nbt);
        buffer.release();
        return buffer.writerIndex();
    }
    public static List<ItemStackWrapper> wrap(List<ItemStack> stacks) {
        return stacks.stream().map(ItemStackWrapper::new).collect(Collectors.toList());
    }

    public static CommonDankItem getItemFromTier(int tier) {
        return (CommonDankItem) BuiltInRegistries.ITEM.get(new ResourceLocation(DankStorage.MODID, "dank_" + tier));
    }
}