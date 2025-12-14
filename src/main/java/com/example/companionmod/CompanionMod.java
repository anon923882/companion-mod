package com.example.companionmod;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.bus.api.IEventBus;

@Mod(CompanionMod.MOD_ID)
public class CompanionMod {
    public static final String MOD_ID = "companionmod";

    public CompanionMod(IEventBus modEventBus) {
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onCropHarvest(BlockEvent.BreakEvent event) {
        LevelAccessor levelAccessor = event.getLevel();
        if (!(levelAccessor instanceof Level level)) {
            return;
        }

        if (level.isClientSide()) {
            return;
        }

        BlockState state = event.getState();
        if (!(state.getBlock() instanceof CropBlock crop)) {
            return;
        }

        Player player = event.getPlayer();
        if (player == null) {
            return;
        }

        if (!crop.isMaxAge(state)) {
            return;
        }

        BlockPos pos = event.getPos();
        if (!level.getBlockState(pos.below()).is(Blocks.FARMLAND)) {
            return;
        }

        event.setCanceled(true);

        if (!player.isCreative()) {
            Block.dropResources(state, level, pos, level.getBlockEntity(pos), player, player.getMainHandItem());
        }

        BlockState replanted = crop.getStateForAge(0);
        level.setBlock(pos, replanted, Block.UPDATE_ALL);
    }
}
