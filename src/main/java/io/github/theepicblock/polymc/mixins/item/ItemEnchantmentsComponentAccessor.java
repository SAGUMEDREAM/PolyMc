package io.github.theepicblock.polymc.mixins.item;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ItemEnchantments.class)
public interface ItemEnchantmentsComponentAccessor {

    @Accessor
    Object2IntOpenHashMap<Holder<Enchantment>> getEnchantments();
}