package io.github.theepicblock.polymc.mixins.block.implementations;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.mixins.BlockEntityDataAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;
import java.util.Map;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;

@Mixin(ClientboundLevelChunkPacketData.class)
public class RemoveModdedBlockEntitiesInChunkMixin {
    @WrapWithCondition(method = "<init>(Lnet/minecraft/world/level/chunk/LevelChunk;)V", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z"))
    private boolean skipUnsupportedBlockEntities(List<?> instance, Object e) {
        var player = PacketContext.get();
        var polyMap = Util.tryGetPolyMap(player);

        return polyMap.canReceiveBlockEntity(((BlockEntityDataAccessor) e).getType());
    }
}
