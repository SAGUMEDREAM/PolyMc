package io.github.theepicblock.polymc.mixins.wizards;

import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PistonMovingBlockEntity.class)
public interface PistonBlockEntityAccessor {
    @Invoker
    float callGetExtendedProgress(float progress);

    @Accessor
    float getProgress();
}
