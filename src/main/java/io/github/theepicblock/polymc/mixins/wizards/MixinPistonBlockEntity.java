package io.github.theepicblock.polymc.mixins.wizards;

import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.api.misc.PolyMapProvider;
import io.github.theepicblock.polymc.api.wizard.Wizard;
import io.github.theepicblock.polymc.impl.misc.PolyMapMap;
import io.github.theepicblock.polymc.impl.poly.wizard.PistonWizardInfo;
import io.github.theepicblock.polymc.impl.poly.wizard.PolyMapFilteredPlayerView;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PistonMovingBlockEntity.class)
public abstract class MixinPistonBlockEntity extends BlockEntity {
    public MixinPistonBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Shadow public abstract BlockState getMovedState();

    @Unique
    private final PolyMapMap<Wizard> wizards = new PolyMapMap<Wizard>((map) -> {
        if (!(level instanceof ServerLevel)) return null;

        var block = this.getMovedState().getBlock();
        var poly = map.getBlockPoly(block);
        if (poly != null && poly.hasWizard()) {
            try {
                return poly.createWizard(new PistonWizardInfo((PistonMovingBlockEntity)(Object)this));
            } catch (Throwable t) {
                PolyMc.LOGGER.error("Failed to create block wizard for "+block.getDescriptionId()+" | "+poly);
                t.printStackTrace();
            }
        }
        return null;
    });

    @Override
    public void setLevel(Level world) {
        super.setLevel(world);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
    }

    @Inject(method = "setLevel(Lnet/minecraft/world/level/Level;)V", at = @At("RETURN"))
    private void onInit(Level world, CallbackInfo ci) {
        if (!(this.level instanceof ServerLevel)) return;

        if (!world.isClientSide()) {
            var allPlayers = PolyMapFilteredPlayerView.getAll((ServerLevel)world, this.getBlockPos());
            allPlayers.forEach((player) -> {
                Wizard wiz = wizards.get(PolyMapProvider.getPolyMap(player));
            });
            wizards.forEach(((polyMap, wizard) -> {
                if (wizard == null) return;
                var filteredView = new PolyMapFilteredPlayerView(allPlayers, polyMap);
                wizard.addPlayer(filteredView);
                filteredView.sendBatched();
            }));
        }
    }

    @Inject(method = "tick(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/piston/PistonMovingBlockEntity;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/piston/PistonMovingBlockEntity;moveCollidedEntities(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;FLnet/minecraft/world/level/block/piston/PistonMovingBlockEntity;)V"))
    private static void onTick(Level world, BlockPos pos, BlockState state, PistonMovingBlockEntity blockEntity, CallbackInfo ci) {
        if (!(world instanceof ServerLevel)) return;

        MixinPistonBlockEntity be = (MixinPistonBlockEntity)(Object)blockEntity;
        if (be == null) return;

        var allNearbyPlayers = PolyMapFilteredPlayerView.getAll((ServerLevel)be.getLevel(), be.getBlockPos());
        be.wizards.forEach((polyMap, wizard) -> {
            if (wizard == null) return;
            var filteredView = new PolyMapFilteredPlayerView(allNearbyPlayers, polyMap);
            wizard.onMove(filteredView); // Pistons move constantly
            wizard.onTick(filteredView);
            filteredView.sendBatched();
        });
    }

    @Inject(method = "preRemoveSideEffects", at = @At("HEAD"))
    private void onRemove(CallbackInfo ci) {
        if (!(this.getLevel() instanceof ServerLevel)) return;
        var allNearbyPlayers = PolyMapFilteredPlayerView.getAll((ServerLevel)this.getLevel(), this.getBlockPos());
        wizards.forEach((polyMap, wizard) -> {
            var filteredView = new PolyMapFilteredPlayerView(allNearbyPlayers, polyMap);
            if (wizard != null) wizard.onRemove(filteredView);
            filteredView.sendBatched();
        });
    }
}
