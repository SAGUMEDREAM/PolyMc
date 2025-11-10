package io.github.theepicblock.polymc.mixins.block;

import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SlabBlock.class)
public interface SlabBlockAccessor {
    @Accessor
    static VoxelShape getSHAPE_BOTTOM() {
        throw new IllegalStateException();
    }
}
