package io.github.theepicblock.polymc.mixins.item;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.misc.ComponentChangesMap;
import io.github.theepicblock.polymc.impl.misc.TransformingPacketCodec;
import io.github.theepicblock.polymc.impl.mixin.ItemLocationStaticHack;
import net.minecraft.core.component.DataComponentExactPredicate;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.trading.ItemCost;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nucleoid.packettweaker.PacketContext;

@Mixin(ItemCost.class)
public class TradedItemPolyImplementationMixin {
    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/codec/StreamCodec;composite(Lnet/minecraft/network/codec/StreamCodec;Ljava/util/function/Function;Lnet/minecraft/network/codec/StreamCodec;Ljava/util/function/Function;Lnet/minecraft/network/codec/StreamCodec;Ljava/util/function/Function;Lcom/mojang/datafixers/util/Function3;)Lnet/minecraft/network/codec/StreamCodec;"))
    private static StreamCodec<RegistryFriendlyByteBuf, ItemCost> writeTradedItemHook(StreamCodec<RegistryFriendlyByteBuf, ItemCost> original) {
        return TransformingPacketCodec.encodeOnly(original, (buf, tradedItem) -> {
            var ctx = PacketContext.get();
            var map = Util.tryGetPolyMap(ctx.getClientConnection());
            var ogStack = tradedItem.itemStack();
            var stack = map.getClientItem(ogStack, ctx.getPlayer(), ItemLocationStaticHack.location.get());
            return stack != ogStack ? new ItemCost(stack.getItem().builtInRegistryHolder(), stack.getCount(), DataComponentExactPredicate.allOf(new ComponentChangesMap(stack.getComponentsPatch()))) : tradedItem;
        });
    }
}
