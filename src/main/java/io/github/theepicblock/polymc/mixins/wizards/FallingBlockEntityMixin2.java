package io.github.theepicblock.polymc.mixins.wizards;

import io.github.theepicblock.polymc.impl.misc.WatchListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class FallingBlockEntityMixin2 implements WatchListener {
    @Inject(method = "startSeenByPlayer", at = @At("RETURN"))
    private void onStartTracking(ServerPlayer player, CallbackInfo ci) {
        if ((Object)this instanceof FallingBlockEntity) {
            this.polymc$addPlayer(player);
        }
    }

    @Inject(method = "stopSeenByPlayer", at = @At("RETURN"))
    private void onStopTracking(ServerPlayer player, CallbackInfo ci) {
        if ((Object)this instanceof FallingBlockEntity) {
            this.polymc$removePlayer(player);
        }
    }
}
