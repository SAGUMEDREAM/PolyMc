package io.github.theepicblock.polymc.mixins.component.transforms;

import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.mixin.TransformingComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.consume_effects.ConsumeEffect;

@Mixin(Consumable.class)
public abstract class ConsumableComponentMixin implements TransformingComponent {
    @Shadow @Final private float consumeSeconds;

    @Shadow @Final private Holder<SoundEvent> sound;

    @Shadow @Final private boolean hasConsumeParticles;

    @Shadow @Final private List<ConsumeEffect> onConsumeEffects;

    @Shadow @Final private ItemUseAnimation animation;

    @Override
    public Object polymc$getTransformed(PacketContext player) {
        if (!polymc$requireModification(player)) {
            return this;
        }

        return new Consumable(this.consumeSeconds, this.animation, this.sound, this.hasConsumeParticles, List.of());
    }

    @Override
    public boolean polymc$requireModification(PacketContext player) {
        var map = Util.tryGetPolyMap(player);
        for (var effect : this.onConsumeEffects) {
            if (!map.canReceiveConsumeEffect(effect.getType())) {
                return true;
            }
        }
        return false;
    }
}
