package net.mine_diver.macula.sources;

import java.io.Closeable;
import java.io.IOException;

public abstract class ShaderpackSource implements Closeable {

    public static final String TypeSeparator = "$";

    public abstract String getName();

    public abstract String getType();

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ShaderpackSource other) {
            return getName().equals(other.getName());
        }
        return false;
    }

    @Override
    public void close() throws IOException {
    }
}
