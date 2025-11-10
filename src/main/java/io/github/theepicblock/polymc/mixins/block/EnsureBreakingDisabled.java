package io.github.theepicblock.polymc.mixins.block;

import io.github.theepicblock.polymc.impl.misc.BlockBreakingUtil;
import io.github.theepicblock.polymc.impl.mixin.BlockBreakingDuck;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerEntity.class)
public class EnsureBreakingDisabled {
    @Shadow @Final private Entity entity;

    @Inject(method = "sendDirtyEntityData", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/server/level/ServerEntity;broadcastAndSend(Lnet/minecraft/network/protocol/Packet;)V", ordinal = 1))
    private void onSyncAttributes(CallbackInfo ci) {
        if (this.entity instanceof ServerPlayer serverPlayer) {
            if (((BlockBreakingDuck)serverPlayer.gameMode).polymc$isBreakingServerside()) {
                // Send it again to ensure the correct value is still there
                BlockBreakingUtil.sendBreakDisabler(serverPlayer);
            }
        }
    }
}
