package io.github.theepicblock.polymc.mixins.tag;

import io.github.theepicblock.polymc.impl.Util;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.common.ClientboundUpdateTagsPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagNetworkSerialization;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.HashMap;
import java.util.Map;

@Mixin(ClientboundUpdateTagsPacket.class)
public class SynchronizeTagsMixin {
    @ModifyArg(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/FriendlyByteBuf;writeMap(Ljava/util/Map;Lnet/minecraft/network/codec/StreamEncoder;Lnet/minecraft/network/codec/StreamEncoder;)V"))
    public Map<ResourceKey<? extends Registry<?>>, TagNetworkSerialization.NetworkPayload> editTagMap(Map<ResourceKey<? extends Registry<?>>, TagNetworkSerialization.NetworkPayload> groups) {
        var polyMap = Util.tryGetPolyMap(PacketContext.get());
        if (polyMap.isVanillaLikeMap()) {
            var regMap = new HashMap<ResourceKey<? extends Registry<?>>, TagNetworkSerialization.NetworkPayload>();
            for (var regEntry : groups.entrySet()) {
                // Vanilla doesn't like it if it receives tags for registries that don't exist
                if (!Util.isVanilla(regEntry.getKey().location())) {
                    continue;
                }

                var map = new HashMap<ResourceLocation, IntList>();
                var reg = BuiltInRegistries.REGISTRY.getValue(regEntry.getKey().location());
                if (reg != null) {
                    for (var entry : ((SerializedAccessor) (Object) regEntry.getValue()).getContents().entrySet()) {
                        var list = new IntArrayList(entry.getValue().size());

                        for (int i : entry.getValue()) {
                            //noinspection unchecked
                            if (polyMap.canReceiveEntry((Registry<? super Object>) reg, (Object) reg.byId(i))) {
                                list.add(i);
                            }
                        }
                        map.put(entry.getKey(), list);
                    }

                    regMap.put(regEntry.getKey(), SerializedAccessor.createSerialized(map));
                } else {
                    // Dynamic registry, client *should* understand it
                    regMap.put(regEntry.getKey(), regEntry.getValue());
                }
            }
            return regMap;
        } else {
            return groups;
        }
    }
}