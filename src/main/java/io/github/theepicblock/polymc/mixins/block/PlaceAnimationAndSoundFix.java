package io.github.theepicblock.polymc.mixins.block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.poly.item.PlaceableItemPoly;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(BlockItem.class)
public abstract class PlaceAnimationAndSoundFix extends Item {
    public PlaceAnimationAndSoundFix(Properties settings) {
        super(settings);
    }

    @ModifyArg(method = "place(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/InteractionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/BlockPos;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"))
    private Entity removeSource(Entity source) {
        if (source instanceof ServerPlayer serverPlayerEntity
                && Util.tryGetPolyMap(serverPlayerEntity).getItemPoly(this) instanceof PlaceableItemPoly) {
            // Causes the player that broke the block to also receive the sound packet
            return null;
        }

        return source;
    }


    @WrapOperation(method = "place(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/InteractionResult;", at = @At(value = "FIELD", target = "Lnet/minecraft/world/InteractionResult;SUCCESS:Lnet/minecraft/world/InteractionResult$Success;"))
    private InteractionResult.Success changeResult(Operation<InteractionResult.Success> original, @Local(ordinal = 0) BlockPlaceContext context) {
        if (context.getPlayer() instanceof ServerPlayer serverPlayerEntity
                && Util.tryGetPolyMap(serverPlayerEntity).getItemPoly(this) instanceof PlaceableItemPoly) {
            return InteractionResult.SUCCESS_SERVER;
        }

        return original.call();
    }
}
