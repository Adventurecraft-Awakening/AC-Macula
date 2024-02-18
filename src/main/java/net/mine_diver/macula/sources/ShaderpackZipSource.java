package net.mine_diver.macula.sources;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ShaderpackZipSource extends ShaderpackFileSource {

    private final ZipFile zipFile;

    public ShaderpackZipSource(File rootPath) throws IOException {
        super(rootPath);
        zipFile = new ZipFile(rootPath);
    }

    @Override
    public boolean isDirectory(String path) {
        ZipEntry entry = zipFile.getEntry(path);
        return entry != null && entry.isDirectory();
    }

    @Override
    public InputStream getInputStream(String path) {
        ZipEntry entry = zipFile.getEntry(path);
        if (entry != null) {
            try {
                return zipFile.getInputStream(entry);
            } catch (IOException ignored) {
            }
        }
        return null;
    }
}
