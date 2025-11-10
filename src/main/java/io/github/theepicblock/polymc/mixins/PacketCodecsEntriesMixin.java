package io.github.theepicblock.polymc.mixins;


import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.misc.TransformingPacketCodec;
import io.github.theepicblock.polymc.impl.mixin.RegistryEntryRegistry;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Holder;
import net.minecraft.core.IdMap;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nucleoid.packettweaker.PacketContext;


@Mixin(targets = "net/minecraft/network/codec/ByteBufCodecs", priority = 800)
public interface PacketCodecsEntriesMixin {
    @ModifyExpressionValue(method = "idMapper(Lnet/minecraft/core/IdMap;)Lnet/minecraft/network/codec/StreamCodec;", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/codec/ByteBufCodecs;idMapper(Ljava/util/function/IntFunction;Ljava/util/function/ToIntFunction;)Lnet/minecraft/network/codec/StreamCodec;"))
    private static <T> StreamCodec<ByteBuf, T> polymer$changeData(StreamCodec<ByteBuf, T> original, @Local(argsOnly = true) IdMap<T> iterable) {
        if (iterable instanceof Registry<T> registry) {
            return TransformingPacketCodec.encodeOnly(original, (byteBuf, val) -> {
                var player = PacketContext.get();
                var map = Util.tryGetPolyMap(player);

                return (T) map.tryRemapping(val, player);
            });
        }
        if (iterable instanceof RegistryEntryRegistry<?> tmp) {
            //noinspection unchecked
            var registry = (Registry<Object>) tmp.polymc$getRegistry();
            return TransformingPacketCodec.encodeOnly(original, (byteBuf, val) -> {
                var player = PacketContext.get();

                var map = Util.tryGetPolyMap(player);

                return (T) registry.wrapAsHolder(map.tryRemapping(((Holder) val).value(), player));
            });
        } else if (iterable == Block.BLOCK_STATE_REGISTRY) {
            return TransformingPacketCodec.encodeOnly(original, (byteBuf, val) -> {
                var player = PacketContext.get();
                var map = Util.tryGetPolyMap(player);

                //noinspection unchecked
                return  (T) map.getClientState((BlockState) val, player.getPlayer());
            });
        }

        return original;
    }

    @Unique
    private static String stringify(Object o) {
        var builder = new StringBuilder();
        var state = (BlockState) o;
        builder.append(BuiltInRegistries.BLOCK.getKey(state.getBlock()));

        if (!state.getBlock().getStateDefinition().getProperties().isEmpty()) {
            builder.append("[");
            var iterator = state.getBlock().getStateDefinition().getProperties().iterator();

            while (iterator.hasNext()) {
                var property = iterator.next();
                builder.append(property.getName());
                builder.append("=");
                builder.append(((Property) property).getName(state.getValue(property)));

                if (iterator.hasNext()) {
                    builder.append(",");
                }
            }
            builder.append("]");
        }
        return builder.toString();
    }
}