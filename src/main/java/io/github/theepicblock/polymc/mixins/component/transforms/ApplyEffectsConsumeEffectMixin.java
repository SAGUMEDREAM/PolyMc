package io.github.theepicblock.polymc.mixins.component.transforms;

import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.mixin.TransformingComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;

@Mixin(ApplyStatusEffectsConsumeEffect.class)
public abstract class ApplyEffectsConsumeEffectMixin implements TransformingComponent {
    @Shadow @Final private List<MobEffectInstance> effects;

    @Override
    public Object polymc$getTransformed(PacketContext context) {
        if (!polymc$requireModification(context)) {
            return this;
        }

        return new ApplyStatusEffectsConsumeEffect(List.of());
    }

    @Override
    public boolean polymc$requireModification(PacketContext context) {
        var map = Util.tryGetPolyMap(context);
        for (var effect : this.effects) {
            if (!map.canReceiveStatusEffect(effect.getEffect())) {
                return true;
            }
        }
        return false;
    }
}
