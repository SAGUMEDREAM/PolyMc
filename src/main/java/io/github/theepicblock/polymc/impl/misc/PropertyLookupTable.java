package io.github.theepicblock.polymc.impl.misc;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;

public class PropertyLookupTable {
    private final String[] propertyNames;
    private final String[][] valueNames;

    // These are cached from the names
    private final Property<?>[] properties;
    private final Map<Property<?>, Object[]> propertyInt2ValueMap = new HashMap<>();

    public PropertyLookupTable(FriendlyByteBuf buf) {
        var amountOfProperties = buf.readVarInt();
        propertyNames = new String[amountOfProperties];
        valueNames = new String[amountOfProperties][];

        for (int i = 0; i < amountOfProperties; i++) {
            propertyNames[i] = buf.readUtf();
            var amountOfValueNames = buf.readVarInt();
            valueNames[i] = new String[amountOfValueNames];

            for (int j = 0; j < amountOfValueNames; j++) {
                valueNames[i][j] = buf.readUtf();
            }
        }

        properties = new Property[propertyNames.length];
    }

    public Property<?> getProperty(int id, Block base) {
        if (properties[id] == null) {
            var propertyName = propertyNames[id];
            properties[id] = base.getStateDefinition().getProperty(propertyName);

            var theValueNames = valueNames[id];
            var int2Values = new Object[theValueNames.length];
            for (int i = 0; i < theValueNames.length; i++) {
                int2Values[i] = properties[id].getValue(theValueNames[i]).orElseThrow();
            }
            propertyInt2ValueMap.put(properties[id], int2Values);
        }
        return properties[id];
    }

    public <T extends Comparable<T>> T getValue(Property<T> property, int valueId) {
        return (T)propertyInt2ValueMap.get(property)[valueId];
    }
}
