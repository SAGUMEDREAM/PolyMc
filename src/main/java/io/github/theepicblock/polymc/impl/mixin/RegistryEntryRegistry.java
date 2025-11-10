package io.github.theepicblock.polymc.impl.mixin;

import net.minecraft.core.Registry;

public interface RegistryEntryRegistry<T> {
    Registry<T> polymc$getRegistry();
}
