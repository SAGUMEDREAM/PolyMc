package io.github.theepicblock.polymc.mixins;

import net.minecraft.world.level.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData$BlockEntityInfo")
public interface BlockEntityDataAccessor {
    @Accessor
    BlockEntityType<?> getType();
}
