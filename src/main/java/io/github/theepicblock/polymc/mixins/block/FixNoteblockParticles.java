package io.github.theepicblock.polymc.mixins.block;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.NoteBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(NoteBlock.class)
public class FixNoteblockParticles {
    @Redirect(method = "triggerEvent(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;II)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"))
    public void redirectParticle(Level instance, ParticleOptions parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        if (instance instanceof ServerLevel world) {
            world.sendParticles(parameters, x, y, z, 0, velocityX, velocityY, velocityZ, 1);
        }
    }
}
