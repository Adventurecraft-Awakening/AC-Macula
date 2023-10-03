package net.mine_diver.macula.wrappers;

import java.nio.FloatBuffer;

public final class UniformNull extends UniformLocation {

    public final static UniformNull instance = new UniformNull(-1);

    public UniformNull(int location) {
        super(location);
    }

    @Override
    public void set1i(int x) {
    }

    @Override
    public void set1f(float x) {
    }

    @Override
    public void set3f(float x, float y, float z) {
    }

    @Override
    public void setMat4(boolean transpose, FloatBuffer matrix) {
    }
}
