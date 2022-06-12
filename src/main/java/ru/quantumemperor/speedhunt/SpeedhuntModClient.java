package ru.quantumemperor.speedhunt;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.client.model.FabricModelPredicateProviderRegistry;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.item.UnclampedModelPredicateProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.CompassItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import ru.quantumemperor.speedhunt.item.HuntersCompass;
import ru.quantumemperor.speedhunt.item.ModItems;

import java.util.Optional;

@Environment(EnvType.CLIENT)
public class SpeedhuntModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ModelPredicateProviderRegistry.register(ModItems.HUNTERS_COMPASS, new Identifier("angle"), new UnclampedModelPredicateProvider(){
            private final SpeedhuntModClient.AngleInterpolator aimedInterpolator = new SpeedhuntModClient.AngleInterpolator();
            private final SpeedhuntModClient.AngleInterpolator aimlessInterpolator = new SpeedhuntModClient.AngleInterpolator();

            @Override
            public float unclampedCall(ItemStack itemStack, @Nullable ClientWorld clientWorld, @Nullable LivingEntity livingEntity, int i) {
                double g;
                Entity entity;
                Entity entity2 = entity = livingEntity != null ? livingEntity : itemStack.getHolder();
                if (entity == null) {
                    return 0.0f;
                }
                if (clientWorld == null && entity.world instanceof ClientWorld) {
                    clientWorld = (ClientWorld)entity.world;
                }
                BlockPos blockPos = HuntersCompass.hasPlayer(itemStack) ? this.getPlayerPos(clientWorld, itemStack.getOrCreateNbt()) : this.getSpawnPos(clientWorld);
                long l = clientWorld.getTime();
                if (blockPos == null || entity.getPos().squaredDistanceTo((double)blockPos.getX() + 0.5, entity.getPos().getY(), (double)blockPos.getZ() + 0.5) < (double)1.0E-5f) {
                    if (this.aimlessInterpolator.shouldUpdate(l)) {
                        this.aimlessInterpolator.update(l, Math.random());
                    }
                    double d = this.aimlessInterpolator.value;
                    return MathHelper.floorMod((float)d, 1.0f);
                }
                boolean bl = livingEntity instanceof PlayerEntity && ((PlayerEntity)livingEntity).isMainPlayer();
                double e = 0.0;
                if (bl) {
                    e = livingEntity.getYaw();
                } else if (entity instanceof ItemFrameEntity) {
                    e = this.getItemFrameAngleOffset((ItemFrameEntity)entity);
                } else if (entity instanceof ItemEntity) {
                    e = 180.0f - ((ItemEntity)entity).getRotation(0.5f) / ((float)Math.PI * 2) * 360.0f;
                } else if (livingEntity != null) {
                    e = livingEntity.bodyYaw;
                }
                e = MathHelper.floorMod(e / 360.0, 1.0);
                double f = this.getAngleToPos(Vec3d.ofCenter(blockPos), entity) / 6.2831854820251465;
                if (bl) {
                    if (this.aimedInterpolator.shouldUpdate(l)) {
                        this.aimedInterpolator.update(l, 0.5 - (e - 0.25));
                    }
                    g = f + this.aimedInterpolator.value;
                } else {
                    g = 0.5 - (e - 0.25 - f);
                }
                return MathHelper.floorMod((float)g, 1.0f);
            }

//            private int scatter(int seed) {
//                return seed * 1327217883;
//            }

            @Nullable
            private BlockPos getSpawnPos(ClientWorld world) {
                return world.getDimension().isNatural() ? world.getSpawnPos() : null;
            }

            @Nullable
            private BlockPos getPlayerPos(World world, NbtCompound nbt) {
                Optional<RegistryKey<World>> optional;
                boolean bl = nbt.contains(HuntersCompass.PLAYER_POS_KEY);
                boolean bl2 = nbt.contains(HuntersCompass.PLAYER_DIMENSION_KEY);
                if (bl && bl2 && (optional = HuntersCompass.getPlayerDimension(nbt)).isPresent() && world.getRegistryKey() == optional.get()) {
                    return NbtHelper.toBlockPos(nbt.getCompound(HuntersCompass.PLAYER_POS_KEY));
                }
                return null;
            }

            private double getItemFrameAngleOffset(ItemFrameEntity itemFrame) {
                Direction direction = itemFrame.getHorizontalFacing();
                int i = direction.getAxis().isVertical() ? 90 * direction.getDirection().offset() : 0;
                return MathHelper.wrapDegrees(180 + direction.getHorizontal() * 90 + itemFrame.getRotation() * 45 + i);
            }

            private double getAngleToPos(Vec3d pos, Entity entity) {
                return Math.atan2(pos.getZ() - entity.getZ(), pos.getX() - entity.getX());
            }
        });
    }

    @Environment(EnvType.CLIENT)
    static class AngleInterpolator {
        double value;
        private double speed;
        private long lastUpdateTime;

        AngleInterpolator() {
        }

        boolean shouldUpdate(long time) {
            return this.lastUpdateTime != time;
        }

        void update(long time, double target) {
            this.lastUpdateTime = time;
            double d = target - this.value;
            d = MathHelper.floorMod(d + 0.5, 1.0) - 0.5;
            this.speed += d * 0.1;
            this.speed *= 0.8;
            this.value = MathHelper.floorMod(this.value + this.speed, 1.0);
        }
    }
}
