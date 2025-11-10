package io.github.theepicblock.polymc.mixins.component.transforms;


import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.mixin.TransformingComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.component.ItemAttributeModifiers;

@Mixin(ItemAttributeModifiers.class)
public abstract class AttributeModifiersComponentMixin implements TransformingComponent {

    @Shadow @Final private List<ItemAttributeModifiers.Entry> modifiers;

    @Override
    public Object polymc$getTransformed(PacketContext player) {
        if (!polymc$requireModification(player)) {
            return this;
        }

        var list = new ArrayList<ItemAttributeModifiers.Entry>();
        var map = Util.tryGetPolyMap(player);
        for (var entry : this.modifiers) {
            if (map.canReceiveRegistryEntry(BuiltInRegistries.ATTRIBUTE, entry.attribute())) {
                list.add(entry);
            }
        }

        return new ItemAttributeModifiers(list);
    }

    @Override
    public boolean polymc$requireModification(PacketContext context) {
        var map = Util.tryGetPolyMap(context);
        for (var entry : this.modifiers) {
            if (!map.canReceiveRegistryEntry(BuiltInRegistries.ATTRIBUTE, entry.attribute())) {
                return true;
            }
        }
        return false;
    }
}
