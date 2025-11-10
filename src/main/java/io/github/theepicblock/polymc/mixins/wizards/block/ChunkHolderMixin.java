package io.github.theepicblock.polymc.mixins.wizards.block;

import io.github.theepicblock.polymc.impl.misc.WatchListener;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkHolder.class)
public abstract class ChunkHolderMixin {
    @Shadow public abstract LevelChunk getTickingChunk();

    @Inject(method = "setTicketLevel", at = @At("HEAD"))
    private void onLevelSet(int level, CallbackInfo ci) {

        if (!ChunkLevel.isLoaded(level)) {
            LevelChunk chunk = this.getTickingChunk();
            if (chunk != null) ((WatchListener)chunk).polymc$removeAllPlayers();
        }
    }
}
