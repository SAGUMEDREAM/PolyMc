package io.github.theepicblock.polymc.mixins.entity;

import io.github.theepicblock.polymc.impl.mixin.EntityTrackerEntryDuck;
import io.github.theepicblock.polymc.impl.mixin.WizardTickerDuck;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkMap.class)
public class RemoveTickerOnUnloadMixin {
    @Shadow @Final private Int2ObjectMap<ChunkMap.TrackedEntity> entityMap;

    @Shadow @Final ServerLevel level;

    @Inject(method = "addEntity(Lnet/minecraft/world/entity/Entity;)V", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lit/unimi/dsi/fastutil/ints/Int2ObjectMap;put(ILjava/lang/Object;)Ljava/lang/Object;"))
    private void onLoadEntity(Entity entity, CallbackInfo ci) {
        var tracker = this.entityMap.get(entity.getId());
        ((EntityTrackerEntryDuck)((EntityTrackerAccessor)tracker).getServerEntity()).polymc$getWizards().forEach((polyMap, wizard) -> {
            ((WizardTickerDuck)this.level).polymc$removeEntityTicker(polyMap, wizard);
        });
    }
}
