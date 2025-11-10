package io.github.theepicblock.polymc.mixins;

import io.github.theepicblock.polymc.impl.mixin.RegistryEntryRegistry;
import net.minecraft.core.Registry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net/minecraft/core/Registry$1")
public class RegistryEntryIndexMixin<T> implements RegistryEntryRegistry<T> {

    @SuppressWarnings("rawtypes")
    @Shadow
    @Final
    private Registry field_40939;

    @Override
    public Registry<T> polymc$getRegistry() {
        //noinspection unchecked
        return this.field_40939;
    }
}
