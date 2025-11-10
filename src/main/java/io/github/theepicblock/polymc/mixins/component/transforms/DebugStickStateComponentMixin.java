package io.github.theepicblock.polymc.mixins.component.transforms;

import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.mixin.TransformingComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.component.DebugStickState;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;

@Mixin(DebugStickState.class)
public class DebugStickStateComponentMixin implements TransformingComponent {
    @Shadow @Final private Map<Holder<Block>, Property<?>> properties;

    @Override
    public Object polymc$getTransformed(PacketContext context) {
        if (polymc$requireModification(context)) {
            return DebugStickState.EMPTY;
        }
        return this;
    }

    @Override
    public boolean polymc$requireModification(PacketContext context) {
        var map = Util.tryGetPolyMap(context);
        for (var key : this.properties.keySet()) {
            if (!map.canReceiveRegistryEntry(BuiltInRegistries.BLOCK, key)) {
                return true;
            }
        }
        return false;
    }
}
