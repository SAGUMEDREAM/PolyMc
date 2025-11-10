package io.github.theepicblock.polymc.mixins.wizards;

import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.api.wizard.Wizard;
import io.github.theepicblock.polymc.api.wizard.WizardView;
import io.github.theepicblock.polymc.impl.misc.PolyMapMap;
import io.github.theepicblock.polymc.impl.misc.WatchListener;
import io.github.theepicblock.polymc.impl.poly.wizard.FallingBlockWizardInfo;
import io.github.theepicblock.polymc.impl.poly.wizard.PolyMapFilteredPlayerView;
import io.github.theepicblock.polymc.impl.poly.wizard.SinglePlayerView;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(FallingBlockEntity.class)
public abstract class FallingBlockEntityMixin extends Entity implements WatchListener {

    @Shadow private BlockState blockState;
    @Unique
    private final PolyMapMap<Wizard> wizards = new PolyMapMap<Wizard>((map) -> {
        Level world = this.level();

        if (!(world instanceof ServerLevel)) return null;

        var block = this.blockState.getBlock();
        var poly = map.getBlockPoly(block);
        if (poly != null && poly.hasWizard()) {
            try {
                return poly.createWizard(new FallingBlockWizardInfo((FallingBlockEntity)(Object)this));
            } catch (Throwable t) {
                PolyMc.LOGGER.error("Failed to create block wizard for "+block.getDescriptionId()+" | "+poly);
                t.printStackTrace();
            }
        }
        return null;
    });

    public FallingBlockEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Inject(method = "fall", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"), locals = LocalCapture.CAPTURE_FAILHARD)
    private static void onSpawnFromBlock(Level world, BlockPos pos, BlockState state, CallbackInfoReturnable<FallingBlockEntity> cir, FallingBlockEntity entity) {
        //When a falling block falls. The block is actually removed by the falling block entity on the first tick.
        PolyMapMap<Wizard> previousWizards = WizardView.removeWizards(world, pos, true);
        previousWizards.forEach((polyMap, wizard) -> {
            wizard.changeInfo(new FallingBlockWizardInfo(entity));
        });
        ((FallingBlockEntityMixin)(Object)entity).wizards.putAll(previousWizards);
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void onTick(CallbackInfo ci) {
        if (this.level() instanceof ServerLevel world) {
            var allNearbyPlayers = PolyMapFilteredPlayerView.getAll(world, this.chunkPosition());
            wizards.forEach(((polyMap, wizard) -> {
                if (wizard == null) return;
                var filteredView = new PolyMapFilteredPlayerView(allNearbyPlayers, polyMap);
                wizard.onMove(filteredView); // It is assumed that sand is constantly falling
                wizard.onTick(filteredView);
                filteredView.sendBatched();
            }));
        }
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
    }

    @Override
    public void setRemoved(RemovalReason reason) {
        super.setRemoved(reason);
    }

    @Override
    public void polymc$addPlayer(ServerPlayer playerEntity) {
        wizards.forEach(((polyMap, wizard) -> {
            if (wizard == null) return;
            var view = new SinglePlayerView(playerEntity);
            wizard.addPlayer(view);
            view.sendBatched();
        }));
    }

    @Override
    public void polymc$removePlayer(ServerPlayer playerEntity) {
        wizards.forEach(((polyMap, wizard) -> {
            if (wizard == null) return;
            var view = new SinglePlayerView(playerEntity);
            wizard.removePlayer(view);
            view.sendBatched();
        }));
    }

    @Override
    public void polymc$removeAllPlayers() {
        if (this.level() instanceof ServerLevel world) {
            var allNearbyPlayers = PolyMapFilteredPlayerView.getAll(world, this.chunkPosition());
            wizards.forEach(((polyMap, wizard) -> {
                var filteredView = new PolyMapFilteredPlayerView(allNearbyPlayers, polyMap);
                if (wizard != null) wizard.removeAllPlayers(filteredView);
                filteredView.sendBatched();
            }));
        }
    }
}
