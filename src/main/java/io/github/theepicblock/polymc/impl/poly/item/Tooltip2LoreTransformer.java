package io.github.theepicblock.polymc.impl.poly.item;

import io.github.theepicblock.polymc.api.PolyMap;
import io.github.theepicblock.polymc.api.item.ItemLocation;
import io.github.theepicblock.polymc.api.item.ItemTransformer;
import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.mixin.TransformingComponent;
import io.github.theepicblock.polymc.mixins.item.ItemEnchantmentsComponentAccessor;
import io.github.theepicblock.polymc.mixins.item.ItemStackAccessor;
import it.unimi.dsi.fastutil.objects.AbstractReferenceList;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceSortedSets;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.TooltipProvider;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Tooltip2LoreTransformer implements ItemTransformer {
    /*private static final List<HideableTooltip<?>> HIDEABLE_TOOLTIPS = List.of(
            HideableTooltip.of(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent::withShowInTooltip),
            HideableTooltip.of(DataComponentTypes.TRIM, ArmorTrim::withShowInTooltip),
            HideableTooltip.ofNeg(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent::isEmpty, ItemEnchantmentsComponent::withShowInTooltip),
            HideableTooltip.ofNeg(DataComponentTypes.STORED_ENCHANTMENTS, ItemEnchantmentsComponent::isEmpty, ItemEnchantmentsComponent::withShowInTooltip),
            HideableTooltip.of(DataComponentTypes.UNBREAKABLE, UnbreakableComponent::withShowInTooltip),
            HideableTooltip.of(DataComponentTypes.CAN_BREAK, BlockPredicatesChecker::withShowInTooltip),
            HideableTooltip.of(DataComponentTypes.JUKEBOX_PLAYABLE, JukeboxPlayableComponent::withShowInTooltip),
            HideableTooltip.of(DataComponentTypes.CAN_PLACE_ON, BlockPredicatesChecker::withShowInTooltip)
    );*/

    @Override
    public ItemStack transform(ItemStack original, ItemStack input, PolyMap polyMap, @Nullable ServerPlayer player, @Nullable ItemLocation location) {
        Item.TooltipContext ctx;
        if (player != null) {
            ctx = Item.TooltipContext.of(player.level());
        } else {
            ctx = Item.TooltipContext.EMPTY;
        }
        var type = TooltipFlag.NORMAL;

        if (shouldPort(input, original, ctx, type, player)) {
            // Copy if needed
            var output = original == input ? input.copy() : input;
            try {
                var list = original.getTooltipLines(ctx, player, type);
                if (list.isEmpty()) {
                    input.set(DataComponents.TOOLTIP_DISPLAY, new TooltipDisplay(true, ReferenceSortedSets.emptySet()));
                } else {
                    list.removeFirst();
                    var style = Style.EMPTY.withItalic(false).withColor(ChatFormatting.WHITE);
                    list.replaceAll(text -> Component.empty().setStyle(style).append(text));
                    output.set(DataComponents.LORE, new ItemLore(list, null));
                    var hidden = new ReferenceLinkedOpenHashSet<DataComponentType<?>>();
                    //output.set(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);
                    for (var x : output.getComponents()) {
                        if (x.type() != DataComponents.LORE && x.value() instanceof TooltipProvider) {
                            hidden.add(x.type());
                        }
                    }
                    input.set(DataComponents.TOOLTIP_DISPLAY, new TooltipDisplay(false, hidden));
                }
            } catch (Throwable e) {
                fallbackPortToLore(output, player, ctx, type);
            }

            return output;
        }
        return input;
    }

    private static boolean shouldPort(ItemStack stack, ItemStack original, Item.TooltipContext ctx, TooltipFlag type, @Nullable ServerPlayer player) {
        // Checks for components which:
        //  - Add things to the tooltip
        //  - Don't generate said tooltip correctly for modded content
        // Note that these components are a slightly different set from those in `fallbackPortToLore`. Since that
        // method has to process a continuous block

        // This method checks the following components:
        //   - DataComponentTypes.STORED_ENCHANTMENTS
        //   - DataComponentTypes.ENCHANTMENTS
        //   - DataComponentTypes.ATTRIBUTE_MODIFIERS
        //   - Anything done using Item#appendTooltip

        var pctx = Util.getContext(player);

        if (original.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT).hideTooltip()) {
            return false;
        }

        if (TransformingComponent.requireTransformForTooltip(original.get(DataComponents.ATTRIBUTE_MODIFIERS), pctx)) {
            return true;
        }

        // Check Item#appendTooltip
        // Includes special-cases for vanilla items, since we know their implementation
        if (original.getItem() instanceof PotionItem) {
            if (TransformingComponent.requireTransformForTooltip(original.get(DataComponents.POTION_CONTENTS), pctx)) {
                return true;
            }
        } else if (!ItemStack.isSameItemSameComponents(original, stack)) {
            /*try {
                original.getItem().appendTooltip(original, ctx, CrashyList.INSTANCE, type);
            } catch (TriedInsertException e) {
                return true;
            }*/
            return true;
        }

        return false;
    }

    /**
     * Computes the tooltip serverside and applies it to the target item.
     * Note: modifies the input
     */
    @SuppressWarnings("UnreachableCode")
    public static void fallbackPortToLore(ItemStack input, @Nullable Player player, Item.TooltipContext ctx, TooltipFlag type) {
        // This function will reprocess the following components:
        //   - Anything done using Item#appendTooltip
        //   - DataComponentTypes.TRIM
        //   - DataComponentTypes.STORED_ENCHANTMENTS
        //   - DataComponentTypes.ENCHANTMENTS
        //   - DataComponentTypes.DYED_COLOR
        //   - DataComponentTypes.LORE
        //   - DataComponentTypes.ATTRIBUTE_MODIFIERS

        // To avoid ordering issues, all of these will be processed as one block
        // This block should be continuous, and thus contains some components which wouldn't have needed to
        // be reprocessed if they appeared on their own (such as DYED_COLOR). Again, this is to prevent ordering issues.

        // Otherwise, there might be a situation where, for example, both ENCHANTMENTS and DYED_COLOR are present.
        // If only ENCHANTMENTS was processed, it'd be moved into LORE after DYED_COLOR, instead of before
        // There's no point detecting these situation for any marginal performance boost. The relevant code will only
        // be run iff a situation arises where the ordering would be messed up.

        var invoker = (ItemStackAccessor)(Object)input;
        assert invoker != null;

        var lore = new ArrayList<Component>();
        Consumer<Component> append_function = (text) -> {
            if (text.getStyle().isEmpty()) {
                // Make sure the lore doesn't mess up the styling
                // It doesn't matter which style we set, as long as the style no longer counts as empty
                lore.add(text.copy().setStyle(Style.EMPTY.withItalic(text.getStyle().isItalic())));
            } else {
                lore.add(text);
            }
        };

        /////////////////
        // Precompute the lore
        // Should match the order of ItemStack#getTooltip

        var hasAdditional = addAdditionalTooltip(input, ctx, append_function, type);
        /*invoker.callAppendTooltip(DataComponentTypes.TRIM, ctx, append_function, type);
        invoker.callAppendTooltip(DataComponentTypes.STORED_ENCHANTMENTS, ctx, append_function, type);
        invoker.callAppendTooltip(DataComponentTypes.ENCHANTMENTS, ctx, append_function, type);
        invoker.callAppendTooltip(DataComponentTypes.DYED_COLOR, ctx, append_function, type);
        invoker.callAppendTooltip(DataComponentTypes.LORE, ctx, append_function, type);
        invoker.callAppendAttributeModifiersTooltip(append_function, player);

        /////////////////
        // Ensure that the components are showInTooltip = false

        if (hasAdditional) {
            input.set(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);
        }

        var trim = input.get(DataComponentTypes.TRIM);
        if (trim != null && trim.showInTooltip()) {
            input.set(DataComponentTypes.TRIM, new ArmorTrim(trim.material(), trim.pattern(), false));
        }

        var stored_enchants = (ItemEnchantmentsComponentAccessor)input.get(DataComponentTypes.STORED_ENCHANTMENTS);
        if (stored_enchants != null && stored_enchants.isShowInTooltip()) {
            input.set(DataComponentTypes.STORED_ENCHANTMENTS, ItemEnchantmentsComponentAccessor.createItemEnchantmentsComponent(stored_enchants.getEnchantments(), false));
        }

        var enchants = (ItemEnchantmentsComponentAccessor)input.get(DataComponentTypes.ENCHANTMENTS);
        if (enchants != null && enchants.isShowInTooltip()) {
            input.set(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponentAccessor.createItemEnchantmentsComponent(enchants.getEnchantments(), false));
        }

        var dyed = input.get(DataComponentTypes.DYED_COLOR);
        if (dyed != null && dyed.showInTooltip()) {
            input.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(dyed.rgb(), false));
        }

        var attributeModifiers = input.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        if (attributeModifiers != null && attributeModifiers.showInTooltip()) {
            input.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, new AttributeModifiersComponent(attributeModifiers.modifiers(), false));
        }

        /////////////////
        // Insert the LORE component
        // No need to set styledLines, it's not used for serialization*/
        input.set(DataComponents.LORE, new ItemLore(lore, null));
    }

    /**
     * Adds tooltip lines appended by {@link net.minecraft.item.Item#}
     *
     * @return true if something was inserted
     */
    private static boolean addAdditionalTooltip(ItemStack stack, Item.TooltipContext ctx, Consumer<Component> textConsumer, TooltipFlag type) {
        var list = new ArrayList<Component>();
        //stack.getItem().appendTooltip(stack, ctx, list, type);
        list.forEach(textConsumer);
        return !list.isEmpty();
    }

    private static class CrashyList<T> extends AbstractReferenceList<T> {
        public static final CrashyList<Component> INSTANCE = new CrashyList<>();

        @Override
        public int size() {
            return 0;
        }

        @Override
        public T get(int index) {
            return null;
        }

        @Override
        public void add(int index, T t) {
            throw new TriedInsertException();
        }
    }

    private static class TriedInsertException extends RuntimeException {

    }

    private record HideableTooltip<T>(DataComponentType<T> type, Predicate<T> shouldSet, TooltipSetter<T> setter) {

        public static <T> HideableTooltip<T> of(DataComponentType<T> type, TooltipSetter<T> setter) {
            return new HideableTooltip<>(type, x -> true, setter);
        }

        public static <T> HideableTooltip<T> of(DataComponentType<T> type, Predicate<T> shouldSet, TooltipSetter<T> setter) {
            return new HideableTooltip<>(type, shouldSet, setter);
        }

        public static <T> HideableTooltip<T> ofNeg(DataComponentType<T> type, Predicate<T> shouldntSet, TooltipSetter<T> setter) {
            return new HideableTooltip<>(type, shouldntSet.negate(), setter);
        }

        public void apply(ItemStack output) {
            var data = output.get(type);
            if (data != null && this.shouldSet.test(data)) {
                output.set(type, setter.setTooltip(data, false));
            }
        }

        interface TooltipSetter<T> {
            T setTooltip(T val, boolean value);
        }
    }
}
