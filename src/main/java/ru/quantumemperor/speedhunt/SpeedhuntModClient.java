package ru.quantumemperor.speedhunt;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.CompassAnglePredicateProvider;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.util.Identifier;
import ru.quantumemperor.speedhunt.item.HuntersCompass;
import ru.quantumemperor.speedhunt.item.ModItems;

@Environment(EnvType.CLIENT)
public class SpeedhuntModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ModelPredicateProviderRegistry.register(ModItems.HUNTERS_COMPASS, new Identifier("angle"), new CompassAnglePredicateProvider((world, stack, entity) -> HuntersCompass.createPlayerPos(stack.getOrCreateNbt())));
    }
}
