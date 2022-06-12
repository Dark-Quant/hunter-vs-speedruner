package ru.quantumemperor.speedhunt.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import ru.quantumemperor.speedhunt.SpeedhuntMod;

import static ru.quantumemperor.speedhunt.SpeedhuntMod.MOD_ID;

public class ModItems {
    public static final Item HUNTERS_COMPASS = register("hunters_compass", new HuntersCompass(
            new FabricItemSettings().group(ItemGroup.MISC).maxCount(1)
    ));

    private static Item register(String id, Item item){
        return Registry.register(Registry.ITEM, new Identifier(MOD_ID, id), item);
    }

    public static void registerItems(){
        SpeedhuntMod.LOGGER.info("Registering items for " + MOD_ID);
    }
}
