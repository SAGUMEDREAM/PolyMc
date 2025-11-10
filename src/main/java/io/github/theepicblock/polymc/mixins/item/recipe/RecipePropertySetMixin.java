package io.github.theepicblock.polymc.mixins.item.recipe;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.theepicblock.polymc.impl.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipePropertySet;

@Mixin(RecipePropertySet.class)
public class RecipePropertySetMixin {
    @ModifyReturnValue(method = "method_64703", at = @At("TAIL"))
    private static List<Holder<Item>> removePolymerEntries(List<Holder<Item>> original) {
        var map = Util.tryGetPolyMap(PacketContext.get());
        var x = new ArrayList<>(original);
        x.removeIf(a -> !map.canReceiveRegistryEntry(BuiltInRegistries.ITEM, a));
        return x;
    }
}
