package ru.quantumemperor.speedhunt.config;

import com.mojang.datafixers.util.Pair;
import ru.quantumemperor.speedhunt.SpeedhuntMod;

import java.util.ArrayList;
import java.util.List;

public class ModConfig {
    public static SimpleConfig CONFIG;
    private static ModConfigProvider configs;

    public static int COOLDOWN_OF_COMPASS;

    public static void registerConfig() {
        configs = new ModConfigProvider();
        createConfigs();

        CONFIG = SimpleConfig.of(SpeedhuntMod.MOD_ID).provider(configs).request();

        assignConfigs();
    }

    private static void createConfigs() {
        configs.addKeyValuePair(new Pair<>("hunters.compass.cooldown", 120), "sec");
    }

    private static void assignConfigs() {
        COOLDOWN_OF_COMPASS = CONFIG.getOrDefault("hunters.compass.cooldown", 130);
        SpeedhuntMod.LOGGER.info(String.valueOf(COOLDOWN_OF_COMPASS));
    }

}
