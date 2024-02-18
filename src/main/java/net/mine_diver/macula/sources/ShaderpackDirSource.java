package net.mine_diver.macula.sources;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class ShaderpackDirSource extends ShaderpackFileSource {

    public ShaderpackDirSource(File rootPath) {
        super(rootPath);
    }

    @Override
    public boolean isDirectory(String path) {
        File file = new File(rootPath, path);
        return file.isDirectory();
    }

    @Override
    public InputStream getInputStream(String path) {
        File file = new File(rootPath, path);
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException ex) {
            return null;
        }
    }
}
