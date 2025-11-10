package io.github.theepicblock.polymc.mixins.entity;

import io.github.theepicblock.polymc.api.misc.PolyMapProvider;
import io.github.theepicblock.polymc.impl.mixin.EntityTrackerEntryDuck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Consumer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;

@Mixin(ServerEntity.class)
public class DisableCustomEntities {
    @Redirect(method = "addPairing", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerEntity;sendPairingData(Lnet/minecraft/server/level/ServerPlayer;Ljava/util/function/Consumer;)V"))
    public void polymc$maybeBlockSpawnPacket(ServerEntity instance, ServerPlayer player, Consumer<Packet<ClientGamePacketListener>> sender) {
        if (((EntityTrackerEntryDuck)this).polymc$getWizards().get(PolyMapProvider.getPolyMap(player)) == null) {
            instance.sendPairingData(player, sender);
        }
    }
}
