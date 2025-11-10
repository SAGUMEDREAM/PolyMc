package io.github.theepicblock.polymc.mixins.block.implementations;

import io.github.theepicblock.polymc.impl.Util;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.packettweaker.PacketContext;

@Mixin(ClientboundAddEntityPacket.class)
public class FallingBlockEntityImplementation {
    @Shadow @Final private EntityType<?> type;
    @Mutable @Shadow @Final private int data;

    @Inject(method = "write(Lnet/minecraft/network/RegistryFriendlyByteBuf;)V", at = @At("HEAD"))
    private void redirectEntityData(RegistryFriendlyByteBuf buf, CallbackInfo ci) {
        if (this.type == EntityType.FALLING_BLOCK) {
            var block = Block.stateById(this.data);
            this.data = Util.getPolydRawIdFromState(block, PacketContext.get());
        }
    }
}
