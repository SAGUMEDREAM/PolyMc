package io.github.theepicblock.polymc.mixins.block.implementations;

import io.github.theepicblock.polymc.impl.mixin.CustomBlockBreakingCheck;
import io.github.theepicblock.polymc.impl.mixin.PacketReplacementUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoublePlantBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * @see BreakParticleImplementation
 */
@Mixin(DoublePlantBlock.class)
public class TallPlantBreakImplementation {
    @Redirect(method = "preventDropFromBottomPart", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;levelEvent(Lnet/minecraft/world/entity/Entity;ILnet/minecraft/core/BlockPos;I)V"))
    private static void onBreakInCreative(Level world, Entity player, int eventId, BlockPos pos, int data) {
        if (player instanceof ServerPlayer spe) {
            var state = world.getBlockState(pos);

            // Minecraft assumes the player who breaks the block knows it's breaking a block.
            // However, as PolyMc reimplements block breaking server-side, the one breaking the block needs to be notified too
            var needsCustomBreaking = CustomBlockBreakingCheck.needsCustomBreaking(spe, state);
            PacketReplacementUtil.syncWorldEvent(world, needsCustomBreaking ? null : spe, 2001, pos, state);
        }
    }
}
