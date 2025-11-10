package io.github.theepicblock.polymc.mixins.component.transforms;

import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.mixin.TransformingComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;
import net.minecraft.world.item.component.SuspiciousStewEffects;

@Mixin(SuspiciousStewEffects.class)
public abstract class SuspiciousStewComponentMixin implements TransformingComponent {

    @Shadow @Final private List<SuspiciousStewEffects.Entry> effects;

    @Override
    public Object polymc$getTransformed(PacketContext player) {
        if (!polymc$requireModification(player)) {
            return this;
        }

        return new SuspiciousStewEffects(List.of());
    }

    @Override
    public boolean polymc$requireModification(PacketContext player) {
        var map = Util.tryGetPolyMap(player);
        for (var effect : this.effects) {
            if (!map.canReceiveStatusEffect(effect.effect())) {
                return true;
            }
        }
        return false;
    }
}
