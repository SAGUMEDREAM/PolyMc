package io.github.theepicblock.polymc.impl.poly.entity;

import io.github.theepicblock.polymc.api.entity.EntityPoly;
import io.github.theepicblock.polymc.api.wizard.PacketConsumer;
import io.github.theepicblock.polymc.api.wizard.Wizard;
import io.github.theepicblock.polymc.api.wizard.WizardInfo;
import io.github.theepicblock.polymc.impl.poly.wizard.AbstractVirtualEntity;
import io.github.theepicblock.polymc.impl.poly.wizard.EntityUtil;
import io.github.theepicblock.polymc.mixins.wizards.EntityAccessor;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import com.mojang.datafixers.util.Pair;

public class DefaultedEntityPoly<T extends Entity> implements EntityPoly<T> {
    private final EntityType<?> displayType;

    public DefaultedEntityPoly(EntityType<?> display) {
        this.displayType = display;
    }

    @Override
    public Wizard createWizard(WizardInfo info, T entity) {
        return new DefaultedEntityWizard<>(info, entity, this.displayType);
    }

    @Override
    public String getDebugInfo(EntityType<?> obj) {
        return displayType.getDescriptionId();
    }

    public static class DefaultedEntityWizard<T extends Entity> extends EntityWizard<T> {
        private final AbstractVirtualEntity virtualEntity;

        public DefaultedEntityWizard(WizardInfo info, T entity, EntityType<?> type) {
            super(info, entity);
            virtualEntity = new AbstractVirtualEntity(entity.getUUID(), entity.getId()) {
                @Override
                public EntityType<?> getEntityType() {
                    return type;
                }
            };
        }

        @Override
        public void onMove(PacketConsumer players) {
        }

        @Override
        public void addPlayer(PacketConsumer player) {
            var original = this.getEntity();
            virtualEntity.spawn(player, this.getPosition(), original.getXRot(), original.getYRot(), 0, original.getDeltaMovement());

            player.sendPacket(EntityUtil.createDataTrackerUpdate(
                    this.virtualEntity.getId(),
                    List.of(
                            new SynchedEntityData.DataItem<>(EntityAccessor.getCustomName(), Optional.of(this.getEntity().getName())),
                            new SynchedEntityData.DataItem<>(EntityAccessor.getNameVisible(), true))
                    )
            );
            
            sendStandardPackets(player, getEntity());
        }

        public static void sendStandardPackets(PacketConsumer player, Entity original) {
            var changedEntries = original.getEntityData().getNonDefaultValues();
            if (changedEntries != null && !changedEntries.isEmpty()) {
                player.sendPacket(new ClientboundSetEntityDataPacket(original.getId(), changedEntries));
            }

            if (original instanceof LivingEntity e) {
                var attributes = e.getAttributes().getSyncableAttributes();
                if (!attributes.isEmpty()) {
                    player.sendPacket(new ClientboundUpdateAttributesPacket(original.getId(), attributes));
                }

                var list = new ArrayList<Pair<EquipmentSlot, ItemStack>>();

                for(var equipmentSlot : EquipmentSlot.values()) {
                    var itemStack = e.getItemBySlot(equipmentSlot);
                    if (!itemStack.isEmpty()) {
                        list.add(Pair.of(equipmentSlot, itemStack.copy()));
                    }
                }

                if (!list.isEmpty()) {
                    player.sendPacket(new ClientboundSetEquipmentPacket(e.getId(), list));
                }
            }

            if (!original.getPassengers().isEmpty()) {
                player.sendPacket(new ClientboundSetPassengersPacket(original));
            }
    
            if (original.isPassenger()) {
                player.sendPacket(new ClientboundSetPassengersPacket(original.getVehicle()));
            }
    
            if (original instanceof Mob mobEntity && mobEntity.isLeashed()) {
                player.sendPacket(new ClientboundSetEntityLinkPacket(mobEntity, mobEntity.getLeashHolder()));
            }
        }

        @Override
        public void removePlayer(PacketConsumer player) {
            virtualEntity.remove(player);
        }
    }
}
