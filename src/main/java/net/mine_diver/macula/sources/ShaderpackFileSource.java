package net.mine_diver.macula.sources;

import java.io.File;
import java.io.InputStream;
import java.util.Objects;

public abstract class ShaderpackFileSource extends ShaderpackSource {

    protected final File rootPath;

    public ShaderpackFileSource(File rootPath) {
        Objects.requireNonNull(rootPath);
        this.rootPath = rootPath;
    }

    public abstract boolean isDirectory(String path);

    public abstract InputStream getInputStream(String path);

    @Override
    public String getName() {
        return rootPath.getName();
    }
}