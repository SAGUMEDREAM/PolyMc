package io.github.theepicblock.polymc.mixins.block;

import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.misc.BlockResyncManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.TargetBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TargetBlock.class)
public class ResyncTargetBlock {
    @Inject(method = "setOutputPower(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/world/level/block/state/BlockState;ILnet/minecraft/core/BlockPos;I)V", at = @At("HEAD"))
    private static void onSetPower(LevelAccessor world, BlockState state, int power, BlockPos pos, int delay, CallbackInfo ci) {
        if (world instanceof ServerLevel serverWorld) {
            serverWorld.getChunkSource().chunkMap.getPlayersCloseForSpawning(new ChunkPos(pos)).forEach(player -> {
                if (Util.tryGetPolyMap(player).isVanillaLikeMap()) {
                    BlockResyncManager.onBlockUpdate(null, pos, serverWorld, player, null);
                }
            });
        }
    }
}
