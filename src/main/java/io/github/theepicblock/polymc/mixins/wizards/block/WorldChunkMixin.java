package io.github.theepicblock.polymc.mixins.wizards.block;

import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.api.PolyMap;
import io.github.theepicblock.polymc.api.block.BlockPoly;
import io.github.theepicblock.polymc.api.misc.PolyMapProvider;
import io.github.theepicblock.polymc.api.wizard.Wizard;
import io.github.theepicblock.polymc.api.wizard.WizardView;
import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.misc.PolyMapMap;
import io.github.theepicblock.polymc.impl.misc.WatchListener;
import io.github.theepicblock.polymc.impl.mixin.WizardTickerDuck;
import io.github.theepicblock.polymc.impl.poly.wizard.CachedPolyMapFilteredPlayerView;
import io.github.theepicblock.polymc.impl.poly.wizard.PlacedWizardInfo;
import io.github.theepicblock.polymc.impl.poly.wizard.PolyMapFilteredPlayerView;
import io.github.theepicblock.polymc.impl.poly.wizard.SinglePlayerView;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.BitStorage;
import net.minecraft.util.SimpleBitStorage;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
@Mixin(LevelChunk.class)
public abstract class WorldChunkMixin extends ChunkAccess implements WatchListener, WizardView {
    @Unique
    private final PolyMapMap<@NotNull Map<@NotNull BlockPos,@NotNull Wizard>> wizards = new PolyMapMap<>(this::createWizardsForChunk);

    @Shadow @Final Level level;

    public WorldChunkMixin(ChunkPos chunkPos, UpgradeData upgradeData, LevelHeightAccessor levelHeightAccessor, PalettedContainerFactory palettedContainerFactory, long l, @Nullable LevelChunkSection[] levelChunkSections, @Nullable BlendingData blendingData) {
        super(chunkPos, upgradeData, levelHeightAccessor, palettedContainerFactory, l, levelChunkSections, blendingData);
    }


    @Shadow public abstract Level getLevel();

    @Unique
    private Map<BlockPos,Wizard> createWizardsForChunk(PolyMap map) {
        Map<BlockPos,Wizard> ret = new HashMap<>();
        if (!(this.level instanceof ServerLevel))
            return ret; //Wizards are only passed ServerWorlds, so we can't create any wizards here.
        if (!map.hasBlockWizards())
            return ret;

        // Get the lowest Y level of the world
        int sectionBottomY = this.level.getMinY();

        for (LevelChunkSection section : this.sections) {

            if (section != null) {
                PalettedContainer<BlockState> container = section.getStates();

                var data = ((PalettedContainerAccessor<BlockState>)container).getData();
                var palette = data.palette();
                var paletteData = data.storage();
                processWizards(map, palette, paletteData, sectionBottomY, ret);
            }

            sectionBottomY += 16;
        }

        return ret;
    }

    @Unique
    private void processWizards(PolyMap polyMap, Palette<BlockState> palette, BitStorage data, int yOffset, Map<@NotNull BlockPos,@NotNull Wizard> wizardMap) {
        if (data.getSize() == 0) return;

        if (palette.getSize() < 256) {
            // The palette contains all block states present in the chunk
            var idsWithPolys = new BlockPoly[palette.getSize()];
            for (int i = 0; i < palette.getSize(); i++) {
                var state = palette.valueFor(i);
                var poly = polyMap.getBlockPoly(state.getBlock());
                if (poly != null && poly.hasWizard()) {
                    idsWithPolys[i] = poly;
                }
            }

            if (data instanceof SimpleBitStorage) {
                // Fast way of iterating the packed data with an index
                int i = 0;

                var elementBits = data.getBits();
                var elementsPerLong = (char)(64 / elementBits);
                var maxValue = (1L << elementBits) - 1L;
                var size = data.getSize();

                data:
                for (long l : data.getRaw()) {
                    for (int j = 0; j < elementsPerLong; ++j) {
                        var blockIndex = (int)(l & maxValue);
                        var poly = idsWithPolys[blockIndex];
                        if (poly != null) {
                            processBlock(polyMap, poly, i, yOffset, wizardMap);
                        }

                        l >>= elementBits;
                        ++i;
                        if (i >= size) {
                            break data;
                        }
                    }
                }
            } else {
                for (int i = 0; i < data.getSize(); i++) {
                    var blockIndex = data.get(i);
                    var poly = idsWithPolys[blockIndex];
                    if (poly != null) {
                        processBlock(polyMap, poly, i, yOffset, wizardMap);
                    }
                }
            }
        } else {
            // It's not worth iterating the palette, instead iterate the blocks in the data
            if (data instanceof SimpleBitStorage) {
                // Fast way of iterating the packed data with an index
                int i = 0;

                var elementBits = data.getBits();
                var elementsPerLong = (char)(64 / elementBits);
                var maxValue = (1L << elementBits) - 1L;
                var size = data.getSize();

                data:
                for (long l : data.getRaw()) {
                    for (int j = 0; j < elementsPerLong; ++j) {
                        var blockIndex = (int)(l & maxValue);
                        var poly = polyMap.getBlockPoly(palette.valueFor(blockIndex).getBlock());
                        if (poly != null && poly.hasWizard()) {
                            processBlock(polyMap, poly, i, yOffset, wizardMap);
                        }

                        l >>= elementBits;
                        ++i;
                        if (i >= size) {
                            break data;
                        }
                    }
                }
            } else {
                for (int i = 0; i < data.getSize(); i++) {
                    var blockIndex = data.get(i);
                    var poly = polyMap.getBlockPoly(palette.valueFor(blockIndex).getBlock());
                    if (poly != null && poly.hasWizard()) {
                        processBlock(polyMap, poly, i, yOffset, wizardMap);
                    }
                }
            }
        }
    }

    @Unique
    private void processBlock(@NotNull PolyMap map, @NotNull BlockPoly poly, int index, int yOffset, @NotNull Map<@NotNull BlockPos,@NotNull Wizard> wizardMap) {
        BlockPos pos = Util.fromPalettedContainerIndex(index).offset(this.chunkPos.x * 16, yOffset, this.chunkPos.z * 16);
        try {
            var wiz = poly.createWizard(new PlacedWizardInfo(pos, (ServerLevel)this.level));
            if (wiz == null) {
                PolyMc.LOGGER.warn(poly+" is creating null wizards! This is bad!");
                return;
            }
            ((WizardTickerDuck)this.level).polymc$addBlockTicker(map, this.getPos(), wiz);
            wizardMap.put(pos, wiz);
        } catch (Throwable t) {
            PolyMc.LOGGER.warn("Failed to create block wizard for block at "+pos+" | "+poly);
        }
    }

    @Override
    public void polymc$addPlayer(ServerPlayer playerEntity) {
        PolyMap map = PolyMapProvider.getPolyMap(playerEntity);
        this.wizards.get(map).values().forEach((wizard) -> {
            try {
                var playerView = new SinglePlayerView(playerEntity);
                wizard.addPlayer(playerView);
                playerView.sendBatched();
            } catch (Throwable t) {
                PolyMc.LOGGER.error("Failed to add player to wizard "+wizard);
            }
        });
    }

    @Override
    public void polymc$removePlayer(ServerPlayer playerEntity) {
        if (!(this.level instanceof ServerLevel)) return;
        PolyMap map = PolyMapProvider.getPolyMap(playerEntity);
        this.wizards.get(map).values().forEach((wizard) -> {
            try {
                var playerView = new SinglePlayerView(playerEntity);
                wizard.removePlayer(playerView);
                playerView.sendBatched();
            } catch (Throwable t) {
                PolyMc.LOGGER.error("Failed to remove player from wizard "+wizard);
            }
        });
    }

    @Override
    public void polymc$removeAllPlayers() {
        if (!(this.level instanceof ServerLevel)) return;
        var allPlayers = PolyMapFilteredPlayerView.getAll((ServerLevel)level, this.getPos());
        this.wizards.forEach((polyMap, wizardMap) -> {
            if (!wizardMap.isEmpty()) {
                var players = new CachedPolyMapFilteredPlayerView(allPlayers, polyMap); // The players nearby this chunk, for packet purposes
                wizardMap.values().forEach(wizard -> {
                    try {
                        wizard.removeAllPlayers(players);
                    } catch (Throwable t) {
                        PolyMc.LOGGER.error("Failed to remove all players from wizard " + wizard);
                    }
                    ((WizardTickerDuck)this.level).polymc$removeBlockTicker(polyMap, this.getPos(), wizard);
                });
                players.sendBatched();
            }
        });
        this.wizards.clear();
    }

    @Inject(method = "setBlockState", at = @At("TAIL"))
    private void onSet(BlockPos pos, BlockState state, int flags, CallbackInfoReturnable<BlockState> cir) {
        if (!(this.level instanceof ServerLevel)) return;
        List<ServerPlayer> allPlayers = null;
        for (var entry : wizards.entrySet()) {
            var polyMap = entry.getKey();
            var wizardMap = entry.getValue();

            Wizard oldWiz = wizardMap.remove(pos);
            if (oldWiz != null) {
                if (allPlayers == null) {
                    allPlayers = PolyMapFilteredPlayerView.getAll((ServerLevel)this.getLevel(), this.getPos());
                }
                var view = new PolyMapFilteredPlayerView(allPlayers, polyMap);
                oldWiz.onRemove(view);
                view.sendBatched();
                ((WizardTickerDuck)this.level).polymc$removeBlockTicker(polyMap, this.getPos(), oldWiz);
            }

            BlockPoly poly = polyMap.getBlockPoly(state.getBlock());
            if (poly != null && poly.hasWizard()) {
                try {
                    BlockPos ipos = pos.immutable();
                    Wizard wiz = poly.createWizard(new PlacedWizardInfo(ipos, (ServerLevel)this.level));
                    wizardMap.put(ipos, wiz);
                    if (allPlayers == null) {
                        allPlayers = PolyMapFilteredPlayerView.getAll((ServerLevel)this.getLevel(), this.getPos());
                    }

                    var filteredView = new PolyMapFilteredPlayerView(allPlayers, polyMap);
                    wiz.addPlayer(filteredView);
                    ((WizardTickerDuck)this.level).polymc$addBlockTicker(polyMap, this.getPos(), wiz);
                } catch (Throwable t) {
                    PolyMc.LOGGER.error("Failed to create block wizard for " + state.getBlock().getDescriptionId() + " | " + poly);
                    t.printStackTrace();
                }
            }
        }
    }

    @Override
    public PolyMapMap<Wizard> getWizards(BlockPos pos) {
        PolyMapMap<Wizard> ret = new PolyMapMap<>(null);
        this.wizards.forEach((polyMap, wizardMap) -> {
            Wizard wizard = wizardMap.get(pos);
            if (wizard != null) ret.put(polyMap, wizard);
        });
        return ret;
    }

    @Override
    public PolyMapMap<Wizard> removeWizards(BlockPos pos, boolean move) {
        if (!(this.level instanceof ServerLevel)) return new PolyMapMap<>(null);

        PolyMapMap<Wizard> ret = new PolyMapMap<>(null);
        var allPlayers = move ? null : PolyMapFilteredPlayerView.getAll((ServerLevel)this.getLevel(), this.getPos());

        this.wizards.forEach((polyMap, wizardMap) -> {
            Wizard wizard = wizardMap.remove(pos);
            if (wizard != null) {
                try {
                    if (!move) {
                        var view = new PolyMapFilteredPlayerView(allPlayers, polyMap);
                        wizard.onRemove(view);
                        view.sendBatched();
                    }
                } catch (Throwable t) {
                    PolyMc.LOGGER.error("Failed to remove wizard "+wizard);
                    t.printStackTrace();
                }
                ((WizardTickerDuck)this.level).polymc$removeBlockTicker(polyMap, this.getPos(), wizard);
                ret.put(polyMap, wizard);
            }
        });
        return ret;
    }
}
