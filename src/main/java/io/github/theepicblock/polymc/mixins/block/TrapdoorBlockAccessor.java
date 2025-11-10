package io.github.theepicblock.polymc.mixins.block;

import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TrapDoorBlock.class)
public interface TrapdoorBlockAccessor {
    @Accessor
    BlockSetType getType();
}
