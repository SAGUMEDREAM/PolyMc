package io.github.theepicblock.polymc.mixins.item.locationproviders;

import io.github.theepicblock.polymc.api.item.ItemLocation;
import io.github.theepicblock.polymc.impl.mixin.ItemLocationStaticHack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientboundContainerSetSlotPacket.class)
public class InventoryLocationProvider {
    @Inject(method = "write(Lnet/minecraft/network/RegistryFriendlyByteBuf;)V", at = @At("HEAD"))
    private void beginWrite(RegistryFriendlyByteBuf buf, CallbackInfo ci) {
        ItemLocationStaticHack.location.set(ItemLocation.INVENTORY);
    }

    @Inject(method = "write(Lnet/minecraft/network/RegistryFriendlyByteBuf;)V", at = @At("RETURN"))
    private void endWrite(RegistryFriendlyByteBuf buf, CallbackInfo ci) {
        ItemLocationStaticHack.location.set(null);
    }
}
