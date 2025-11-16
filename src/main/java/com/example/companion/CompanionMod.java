package com.example.companion;

import com.example.companion.client.CompanionClient;
import com.example.companion.entity.CompanionEntity;
import com.example.companion.inventory.CompanionMenu;
import com.example.companion.item.CompanionCharmItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.common.DeferredRegister;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.network.IContainerFactory;

import java.util.function.Supplier;

@Mod(CompanionMod.MOD_ID)
public class CompanionMod {
    public static final String MOD_ID = "companionmod";

    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, MOD_ID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, MOD_ID);
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MOD_ID);

    public static final Supplier<EntityType<CompanionEntity>> COMPANION_TYPE = ENTITY_TYPES.register("companion",
            () -> EntityType.Builder.<CompanionEntity>of(CompanionEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.8F)
                    .clientTrackingRange(10)
                    .build(ResourceLocation.fromNamespaceAndPath(MOD_ID, "companion").toString()));

    public static final Supplier<Item> COMPANION_CHARM = ITEMS.register("companion_charm",
            () -> new CompanionCharmItem(new Item.Properties().stacksTo(1)));

    public static final Supplier<MenuType<CompanionMenu>> COMPANION_MENU = MENUS.register("companion_inventory",
            () -> MenuType.create((IContainerFactory<CompanionMenu>) (windowId, playerInventory, buffer) -> {
                Level level = playerInventory.player.level();
                if (buffer != null && level != null) {
                    int entityId = buffer.readVarInt();
                    if (level.getEntity(entityId) instanceof CompanionEntity companion) {
                        return new CompanionMenu(windowId, playerInventory, companion);
                    }
                }
                return null;
            }));

    public CompanionMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ENTITY_TYPES.register(modEventBus);
        ITEMS.register(modEventBus);
        MENUS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerAttributes);

        modEventBus.addListener(CompanionClient::registerRenderers);
        modEventBus.addListener(CompanionClient::registerScreens);

        net.neoforged.bus.api.EventBus eventBus = net.neoforged.neoforge.common.NeoForge.EVENT_BUS;
        eventBus.addListener(this::addCreativeTabEntries);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(CompanionEntity::registerSpawnPlacements);
    }

    private void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(COMPANION_TYPE.get(), CompanionEntity.createAttributes().build());
    }

    private void addCreativeTabEntries(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS || event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(COMPANION_CHARM.get());
        }
    }
}
