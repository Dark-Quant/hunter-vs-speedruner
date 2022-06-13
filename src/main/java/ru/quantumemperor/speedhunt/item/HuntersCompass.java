package ru.quantumemperor.speedhunt.item;

import com.mojang.logging.LogUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import ru.quantumemperor.speedhunt.SpeedhuntMod;
import ru.quantumemperor.speedhunt.config.ModConfig;

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
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        String name = stack.getName().getString();
        world.getPlayers().forEach(player -> {
            if (player.getGameProfile().getName().equalsIgnoreCase(name)) {
                writeNbt(world.getRegistryKey(), player.getBlockPos(), stack.getOrCreateNbt());
                user.getItemCooldownManager().set(this, 20 * ModConfig.COOLDOWN_OF_COMPASS);
            }
        });
        if (!stack.getOrCreateNbt().contains(PLAYER_TRACKED_KEY)) {
            SpeedhuntMod.LOGGER.info("False");
            stack.getOrCreateNbt().putBoolean(PLAYER_TRACKED_KEY, false);
        }
        return TypedActionResult.success(user.getStackInHand(hand));
    }

    public static Optional<RegistryKey<World>> getPlayerDimension(NbtCompound nbt) {
        return World.CODEC.parse(NbtOps.INSTANCE, nbt.get(PLAYER_DIMENSION_KEY)).result();
    }

    @Nullable
    public static GlobalPos createPlayerPos(NbtCompound nbt) {
        Optional<RegistryKey<World>> optional;
        boolean bl = nbt.contains(HuntersCompass.PLAYER_POS_KEY);
        boolean bl2 = nbt.contains(HuntersCompass.PLAYER_DIMENSION_KEY);
        if (bl && bl2 && (optional = HuntersCompass.getPlayerDimension(nbt)).isPresent()) {
            BlockPos blockPos = NbtHelper.toBlockPos(nbt.getCompound(PLAYER_POS_KEY));
            return GlobalPos.create(optional.get(), blockPos);
        }
        return null;
    }

    private void writeNbt(RegistryKey<World> worldKey, BlockPos pos, NbtCompound nbt) {
        nbt.put(PLAYER_POS_KEY, NbtHelper.fromBlockPos(pos));
        World.CODEC.encodeStart(NbtOps.INSTANCE, worldKey).resultOrPartial(LOGGER::error).ifPresent(nbtElement -> nbt.put(PLAYER_DIMENSION_KEY, (NbtElement) nbtElement));
        nbt.putBoolean(PLAYER_TRACKED_KEY, true);
    }
}
