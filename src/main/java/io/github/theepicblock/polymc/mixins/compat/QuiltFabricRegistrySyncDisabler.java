package io.github.theepicblock.polymc.mixins.compat;

import io.github.theepicblock.polymc.impl.Util;
import net.minecraft.client.Minecraft;
//import org.quiltmc.qsl.registry.impl.sync.server.ServerFabricRegistrySync;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class QuiltFabricRegistrySyncDisabler {
    /*@SuppressWarnings("MixinAnnotationTarget")
    @Inject(method = "sendSyncPackets(Lnet/minecraft/network/ClientConnection;)V", at = @At("HEAD"), cancellable = true)
    private static void sendPacketInject(ClientConnection connection, CallbackInfo ci) {
        if (Util.isPolyMapVanillaLike(connection)) {
            ci.cancel();
        }
    }*/
}
