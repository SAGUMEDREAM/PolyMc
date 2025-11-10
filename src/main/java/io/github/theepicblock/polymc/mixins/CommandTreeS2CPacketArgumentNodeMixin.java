package io.github.theepicblock.polymc.mixins;

import com.mojang.brigadier.arguments.StringArgumentType;
import io.github.theepicblock.polymc.impl.Util;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.core.registries.BuiltInRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import xyz.nucleoid.packettweaker.PacketContext;

@Mixin(targets = "net/minecraft/network/protocol/game/ClientboundCommandsPacket$ArgumentNodeStub")
public class CommandTreeS2CPacketArgumentNodeMixin {
    @ModifyArg(method = "write(Lnet/minecraft/network/FriendlyByteBuf;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/ClientboundCommandsPacket$ArgumentNodeStub;serializeCap(Lnet/minecraft/network/FriendlyByteBuf;Lnet/minecraft/commands/synchronization/ArgumentTypeInfo$Template;)V"))
    private ArgumentTypeInfo.Template<?> replaceProperties(ArgumentTypeInfo.Template<?> original) {
        var player = PacketContext.get();
        var map = Util.tryGetPolyMap(player);

        var id = BuiltInRegistries.COMMAND_ARGUMENT_TYPE.getKey(original.type());
        var isBrigadier = id != null && id.getNamespace().equals("brigadier");
        if (!map.canReceiveEntry(BuiltInRegistries.COMMAND_ARGUMENT_TYPE, original.type()) && !isBrigadier) {
            return ArgumentTypeInfos.unpack(StringArgumentType.word());
        }
        return original;
    }
}
