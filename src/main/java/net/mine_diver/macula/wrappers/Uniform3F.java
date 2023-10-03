package net.mine_diver.macula.wrappers;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.ARBShaderObjects.glUniform3fARB;

public final class Uniform3F extends UniformLocation {

    public Uniform3F(int location) {
        super(location);
    }

    @Override
    public void set1i(int x) {
        set1f(x);
    }

    @Override
    public void set1f(float x) {
        //noinspection SuspiciousNameCombination
        set3f(x, x, x);
    }

    @Override
    public void set3f(float x, float y, float z) {
        glUniform3fARB(location, x, y, z);
    }

    @Override
    public void setMat4(boolean transpose, FloatBuffer matrix) {
        throw new UnsupportedOperationException();
    }
}
