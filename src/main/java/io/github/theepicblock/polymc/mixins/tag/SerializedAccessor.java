package io.github.theepicblock.polymc.mixins.tag;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagNetworkSerialization;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Mixin(TagNetworkSerialization.NetworkPayload.class)
public interface SerializedAccessor {
    @Accessor("tags")
    Map<ResourceLocation, IntList> getContents();

    @Invoker("<init>")
    static TagNetworkSerialization.NetworkPayload createSerialized(Map<ResourceLocation, IntList> contents) {
        throw new UnsupportedOperationException();
    }
}