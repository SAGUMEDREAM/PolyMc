package io.github.theepicblock.polymc.impl.resource;

import io.github.theepicblock.polymc.api.resource.ClientJarResources;
import io.github.theepicblock.polymc.api.resource.ModdedResources;
import io.github.theepicblock.polymc.impl.misc.logging.SimpleLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.util.Tuple;

public class MergedModdedResources implements ModdedResources {
    private final ModdedResources base;
    private final ClientJarResources client;

    public MergedModdedResources(ModdedResources base, ClientJarResources client) {
        this.base = base;
        this.client = client;
    }

    @Override
    public @NotNull List<IoSupplier<InputStream>> getInputStreams(String namespace, String path) {
        if (client.containsAsset(namespace, path)) {
            var list = new ArrayList<>(base.getInputStreams(namespace, path));
            list.add(client.getInputStreamSupplier(namespace, path));
            return list;
        }
        return base.getInputStreams(namespace, path);
    }

    @Override
    public @NotNull Set<String> getAllNamespaces() {
        var set = new HashSet<>(base.getAllNamespaces());
        set.add("minecraft");
        return set;
    }

    @Override
    public @NotNull Set<Tuple<ResourceLocation,IoSupplier<InputStream>>> locateLanguageFiles() {
        var set = new HashSet<>(base.locateLanguageFiles());
        set.addAll(client.locateLanguageFiles());
        return set;
    }

    @Override
    public @NotNull Set<Tuple<ResourceLocation, IoSupplier<InputStream>>> locateFiles(String prefix) {
        return this.base.locateFiles(prefix);
    }

    @Override
    public ClientJarResources getClientJar(SimpleLogger logger) {
        return this.client;
    }

    @Override
    public ModdedResources includeClientJar(SimpleLogger logger) {
        return this;
    }

    @Override
    public @Nullable IoSupplier<InputStream> getInputStreamSupplier(String namespace, String path) {
        var ret = base.getInputStreamSupplier(namespace, path);
        if (ret == null) {
            return client.getInputStreamSupplier(namespace, path);
        } else {
            return ret;
        }
    }

    @Override
    public boolean containsAsset(String namespace, String path) {
        return base.containsAsset(namespace, path) || client.containsAsset(namespace, path);
    }

    @Override
    public void close() throws Exception {
        base.close();
    }
}
