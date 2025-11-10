package io.github.theepicblock.polymc.api.resource;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import net.minecraft.server.packs.resources.IoSupplier;

public class SimpleAsset implements PolyMcAsset {
    private final IoSupplier<InputStream> inner;

    public SimpleAsset(IoSupplier<InputStream> inner) {
        this.inner = inner;
    }

    @Override
    public void writeToStream(OutputStream stream, Gson gson) throws IOException {
        try (var iStream = inner.get()) {
            iStream.transferTo(stream);
        }
    }
}
