package io.github.theepicblock.polymc.mixins;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Commands.class)
public class CommandSuggestionFix {
    /**
     * Replaces the ItemStack and BlockState argument types with a list of identifiers instead.
     * This list is calculated serverside and will therefore include modded blocks and items.
     *
     * Code from Polymer:
     * https://github.com/Patbox/polymer/blob/e8007afecfb9cadc5bb0fdcca0d8c6bb47fb9a21/src/main/java/eu/pb4/polymer/mixin/command/CommandManagerMixin.java#L31
     */
    @Inject(method = "argument", at = @At("TAIL"), cancellable = true)
    private static void makeSuggestionsServerSide(String name, ArgumentType<?> type, CallbackInfoReturnable<RequiredArgumentBuilder<CommandSourceStack, ?>> cir) {
        if (type instanceof ItemArgument || type instanceof BlockStateArgument) {
            cir.setReturnValue(cir.getReturnValue().suggests(type::listSuggestions));
        }
    }
}
