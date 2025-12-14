package com.anon923882.companionmod;

import com.anon923882.companionmod.entity.ModEntities;
import com.anon923882.companionmod.inventory.ModMenuTypes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(CompanionMod.MOD_ID)
public class CompanionMod {
    public static final String MOD_ID = "companionmod";
    public static final Logger LOGGER = LogManager.getLogger();

    public CompanionMod(IEventBus modEventBus) {
        ModEntities.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        
        LOGGER.info("Companion Mod initialized!");
    }
}
