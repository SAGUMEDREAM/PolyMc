package io.github.theepicblock.polymc.mixins.item.recipe;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import eu.pb4.polymer.common.impl.CompatStatus;
import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.misc.SkipCheck;
import io.github.theepicblock.polymc.impl.misc.TransformingPacketCodec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.display.SlotDisplay;

@Mixin(value = SlotDisplay.class, priority = 900)
public interface SlotDisplayMixin {
    @SuppressWarnings("DataFlowIssue")
    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/codec/StreamCodec;dispatch(Ljava/util/function/Function;Ljava/util/function/Function;)Lnet/minecraft/network/codec/StreamCodec;"))
    private static StreamCodec<RegistryFriendlyByteBuf, SlotDisplay> transformDisplays(StreamCodec<RegistryFriendlyByteBuf, SlotDisplay> original) {
        return TransformingPacketCodec.encodeOnly(original, (buf, display) -> {
            var map = Util.tryGetPolyMap(PacketContext.get());

            return switch (display) {
                case SlotDisplay.ItemSlotDisplay item when !map.canReceiveRegistryEntry(BuiltInRegistries.ITEM, item.item()) ->
                        new SlotDisplay.ItemStackSlotDisplay(item.item().value().getDefaultInstance());
                case SlotDisplay.TagSlotDisplay tagSlot when !((SkipCheck) (Object) tagSlot).polymc$skipped() -> {
                    var tag = buf.registryAccess().lookupOrThrow(Registries.ITEM).get(tagSlot.tag());
                    if (tag.isEmpty()) {
                        yield tagSlot;
                    }

                    var array = new ArrayList<SlotDisplay>();
                    for (var entry : tag.get()) {
                        if (!map.canReceiveRegistryEntry(BuiltInRegistries.ITEM, entry)) {
                            array.add(new SlotDisplay.ItemStackSlotDisplay(entry.value().getDefaultInstance()));
                        }
                    }
                    if (!array.isEmpty()) {
                        var out = new SlotDisplay.TagSlotDisplay(tagSlot.tag());
                        ((SkipCheck) (Object) out).polymc$setSkipped();

                        if (CompatStatus.POLYMER_CORE) {
                            if (((SkipCheck) (Object) tagSlot).polymer$skipped()) {
                                ((SkipCheck) (Object) out).polymer$setSkipped();
                            }
                        }
                        array.addFirst(out);
                        yield new SlotDisplay.Composite(array);
                    }
                    yield tagSlot;
                }
                default -> display;
            };
        });
    }
}
