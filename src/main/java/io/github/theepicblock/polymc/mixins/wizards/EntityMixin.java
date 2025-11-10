package io.github.theepicblock.polymc.mixins.wizards;

import io.github.theepicblock.polymc.impl.misc.WatchListener;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {
    @Inject(method = "onClientRemoval", at = @At("HEAD"))
    private void removeWizards(CallbackInfo ci) {
        if (this instanceof WatchListener watchListener) {
            watchListener.polymc$removeAllPlayers();
        }
    }
}
