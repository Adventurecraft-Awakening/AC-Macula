package net.mine_diver.macula.wrappers;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.ARBShaderObjects.glUniform1fARB;

public final class Uniform1F extends UniformLocation {

    public Uniform1F(int location) {
        super(location);
    }

    @Override
    public void set1i(int x) {
        set1f(x);
    }

    @Override
    public void set1f(float x) {
        glUniform1fARB(location, x);
    }

    @Override
    public void set3f(float x, float y, float z) {
        set1f(x);
    }

    @Override
    public void setMat4(boolean transpose, FloatBuffer matrix) {
        throw new UnsupportedOperationException();
    }
}
