package io.github.theepicblock.polymc.mixins.item;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.function.Consumer;
import net.minecraft.world.item.ItemStack;

@Mixin(ItemStack.class)
public interface ItemStackAccessor {

}
