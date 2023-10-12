package tfar.dankstorage.item;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import tfar.dankstorage.DankStorageForge;
import tfar.dankstorage.Item.CommonDankItem;
import tfar.dankstorage.container.PortableDankProvider;
import tfar.dankstorage.mixin.ItemUsageContextAccessor;
import tfar.dankstorage.network.DankPacketHandler;
import tfar.dankstorage.utils.*;
import tfar.dankstorage.world.DankInventory;
import tfar.dankstorage.world.MaxId;

import javax.annotation.Nonnull;

public class DankItem extends CommonDankItem {

  public static final Rarity DARK_GRAY = Rarity.create("dark_gray", ChatFormatting.DARK_GRAY);
  public static final Rarity DARK_RED = Rarity.create("dark_red", ChatFormatting.DARK_RED);
  public static final Rarity GOLD = Rarity.create("gold", ChatFormatting.GOLD);
  public static final Rarity GREEN = Rarity.create("green", ChatFormatting.GREEN);
  public static final Rarity BLUE = Rarity.create("blue", ChatFormatting.AQUA);
  public static final Rarity DARK_PURPLE = Rarity.create("dark_purple", ChatFormatting.DARK_PURPLE);
  public static final Rarity WHITE = Rarity.create("white", ChatFormatting.WHITE);

    public DankItem(Properties $$0, DankStats stats) {
        super($$0, stats);
    }

    @Nonnull
  @Override
  public Rarity getRarity(ItemStack stack) {
      return switch (stats) {
          case one -> DARK_GRAY;
          case two -> DARK_RED;
          case three -> GOLD;
          case four -> GREEN;
          case five -> BLUE;
          case six -> DARK_PURPLE;
          case seven -> WHITE;
          default -> super.getRarity(stack);
      };
  }

    @Nonnull
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack bag = player.getItemInHand(hand);

            if (Utils.getUseType(bag) == UseType.bag) {
                if (!level.isClientSide) {
                    assignNextId(bag);
                    player.openMenu(new PortableDankProvider(bag));
                }
                return InteractionResultHolder.success(bag);
            } else {
                if (!level.isClientSide) {
                    ItemStack toPlace = Utils.getItemStackInSelectedSlot(bag, (ServerLevel) level);
                    EquipmentSlot hand1 = hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
                    //handle empty
                    if (toPlace.isEmpty()) {
                        return InteractionResultHolder.pass(bag);
                    }

                    //handle food
                    if (toPlace.getItem().isEdible()) {
                        if (player.canEat(false)) {
                            player.startUsingItem(hand);
                            return InteractionResultHolder.consume(bag);
                        }
                    }
                    //handle potion
                    else if (toPlace.getItem() instanceof PotionItem) {
                        player.startUsingItem(hand);
                        return InteractionResultHolder.success(player.getItemInHand(hand));
                    }

                    //handle shield
                    else if (toPlace.getItem() instanceof ShieldItem) {
                        player.startUsingItem(hand);
                        return InteractionResultHolder.success(player.getItemInHand(hand));
                    }

                    //todo support other items?
                    else {
                        ItemStack bagCopy = bag.copy();
                        player.setItemSlot(hand1, toPlace);
                        InteractionResultHolder<ItemStack> actionResult = toPlace.getItem().use(level, player, hand);
                        DankInventory handler = Utils.getInventory(bagCopy, level);
                        handler.setStackInSlot(Utils.getSelectedSlot(bagCopy), actionResult.getObject());
                        player.setItemSlot(hand1, bagCopy);
                    }
                }
                return new InteractionResultHolder<>(InteractionResult.PASS, player.getItemInHand(hand));
            }
    }

    //this is called on the client
    @Override
    public InteractionResult interactLivingEntity(ItemStack bag, Player player, LivingEntity entity, InteractionHand hand) {
        if (!Utils.isConstruction(bag)) return InteractionResult.PASS;

        ItemStack toUse = Utils.getSelectedItem(bag,player.level());
        EquipmentSlot hand1 = hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
        player.setItemSlot(hand1, toUse);
        InteractionResult result = toUse.getItem().interactLivingEntity(toUse, player, entity, hand);

        //the client doesn't have access to the full inventory
        if (!player.level().isClientSide) {
            DankInventory handler = Utils.getInventory(bag, player.level());
            handler.setStackInSlot(Utils.getSelectedSlot(bag), toUse);
        }

        player.setItemSlot(hand1, bag);
        return result;
    }

    @Nonnull
    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        ItemStack bag = ctx.getItemInHand();
        Level level = ctx.getLevel();
        UseType useType = Utils.getUseType(bag);

        if (useType == UseType.bag) {
            return InteractionResult.PASS;
        }

        int selectedSlot = Utils.getSelectedSlot(bag);

        //invalid slot
        if (selectedSlot == Utils.INVALID) {
            return InteractionResult.PASS;
        }

        ItemStack toPlace = Utils.getSelectedItem(bag,level);
        //todo: sync locked slots to client?
        if (/*toPlace.getCount() == 1 && handler.isLocked(selectedSlot)*/ false)
            return InteractionResult.PASS;

        UseOnContext ctx2 = new UseOnContext(ctx.getLevel(), ctx.getPlayer(), ctx.getHand(), toPlace, ((ItemUsageContextAccessor) ctx).getHitResult());
        InteractionResult actionResultType = toPlace.getItem().useOn(ctx2);//ctx2.getItem().onItemUse(ctx);
        if (!level.isClientSide) {
            DankInventory dankInventory = Utils.getInventory(bag,level);
            dankInventory.setStackInSlot(selectedSlot, ctx2.getItemInHand());
        }
        return actionResultType;
    }

    @Override
    public void inventoryTick(ItemStack bag, Level level, Entity entity, int i, boolean equipped) {
        //there has to be a better way
        if (entity instanceof ServerPlayer player && equipped) {
            ItemStack sel = Utils.getSelectedItem(bag,level);
            DankPacketHandler.sendSelectedItem(player, sel);
        }
    }

    public static void assignNextId(ItemStack dank) {
        CompoundTag settings = Utils.getSettings(dank);
        if (settings == null || !settings.contains(Utils.FREQ, Tag.TAG_INT)) {
            MaxId maxId = DankStorageForge.instance.maxId;
            int next = maxId.getMaxId();
            maxId.increment();
            Utils.getOrCreateSettings(dank).putInt(Utils.FREQ,next);
        }
    }

    @Override
    public @org.jetbrains.annotations.Nullable ICapabilityProvider initCapabilities(ItemStack stack, @org.jetbrains.annotations.Nullable CompoundTag nbt) {
        if (Utils.getFrequency(stack)!= Utils.INVALID) {
            return new DankItemCapability(stack);
        }
        return super.initCapabilities(stack, nbt);
    }
}
