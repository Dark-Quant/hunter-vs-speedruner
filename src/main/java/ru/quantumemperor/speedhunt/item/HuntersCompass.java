package ru.quantumemperor.speedhunt.item;

import com.mojang.logging.LogUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.CompassItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.poi.PointOfInterestType;
import org.slf4j.Logger;
import ru.quantumemperor.speedhunt.SpeedhuntMod;
import ru.quantumemperor.speedhunt.config.ModConfig;

import java.util.Arrays;
import java.util.Optional;

public class HuntersCompass extends Item {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final String PLAYER_POS_KEY = "PlayerPos";
    public static final String PLAYER_DIMENSION_KEY = "PlayerDimension";
    public static final String PLAYER_TRACKED_KEY = "PlayerTracked";

    public HuntersCompass(Settings settings) {
        super(settings);
    }



    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {

    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        String name = stack.getName().getString();
        world.getPlayers().forEach(player -> {
            if(player.getGameProfile().getName().equalsIgnoreCase(name)) {
                writeNbt(world.getRegistryKey(), player.getBlockPos(), stack.getOrCreateNbt());
                user.getItemCooldownManager().set(this, 20 * ModConfig.COOLDOWN_OF_COMPASS);
            }
        });
        if(!stack.getOrCreateNbt().contains(PLAYER_TRACKED_KEY)){
            SpeedhuntMod.LOGGER.info("False");
            stack.getOrCreateNbt().putBoolean(PLAYER_TRACKED_KEY, false);
        }
        return TypedActionResult.success(user.getStackInHand(hand));
    }

    public static boolean hasPlayer(ItemStack stack){
        NbtCompound nbtCompound = stack.getNbt();
        return nbtCompound != null && (nbtCompound.contains(PLAYER_DIMENSION_KEY) || nbtCompound.contains(PLAYER_POS_KEY));
    }

    public static Optional<RegistryKey<World>> getPlayerDimension(NbtCompound nbt) {
        return World.CODEC.parse(NbtOps.INSTANCE, nbt.get(PLAYER_DIMENSION_KEY)).result();
    }

    private void writeNbt(RegistryKey<World> worldKey, BlockPos pos, NbtCompound nbt) {
        nbt.put(PLAYER_POS_KEY, NbtHelper.fromBlockPos(pos));
        World.CODEC.encodeStart(NbtOps.INSTANCE, worldKey).resultOrPartial(LOGGER::error).ifPresent(nbtElement -> nbt.put(PLAYER_DIMENSION_KEY, (NbtElement) nbtElement));
        nbt.putBoolean(PLAYER_TRACKED_KEY, true);
    }

    public int getPlayerX(ItemStack itemStack){
        return NbtHelper.toBlockPos((NbtCompound) itemStack.getOrCreateNbt().get("PlayerPos")).getX();
    }

    public int getPlayerY(ItemStack itemStack){
        return NbtHelper.toBlockPos((NbtCompound) itemStack.getOrCreateNbt().get("PlayerPos")).getY();
    }

    @Override
    public String getTranslationKey() {
        return "item.speedhunt.hunters_compass";
    }
}
