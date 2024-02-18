package net.mine_diver.macula.sources;

public abstract class ShaderpackSource {

    public abstract String getName();

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ShaderpackSource other) {
            return getName().equals(other.getName()) && getClass().equals(other.getClass());
        }
        return false;
    }
}
