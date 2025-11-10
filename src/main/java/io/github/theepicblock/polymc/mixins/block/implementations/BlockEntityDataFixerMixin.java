package io.github.theepicblock.polymc.mixins.block.implementations;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.theepicblock.polymc.impl.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nucleoid.packettweaker.PacketContext;

@Mixin(targets = "net/minecraft/network/protocol/game/ClientboundLevelChunkPacketData$BlockEntityInfo")
public class BlockEntityDataFixerMixin {
    @ModifyExpressionValue(method = "create", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BlockEntity;getUpdateTag(Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/nbt/CompoundTag;"))
    private static CompoundTag changeNbt(CompoundTag original, @Local(argsOnly = true) BlockEntity blockEntity) {
        return Util.transformBlockEntityNbt(PacketContext.get(), blockEntity.getType(), original);
    }
}
