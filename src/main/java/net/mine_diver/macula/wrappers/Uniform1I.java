package net.mine_diver.macula.wrappers;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.ARBShaderObjects.glUniform1iARB;

public final class Uniform1I extends UniformLocation {

    public Uniform1I(int location) {
        super(location);
    }

    @Override
    public void set1i(int x) {
        glUniform1iARB(location, x);
    }

    @Override
    public void set1f(float x) {
        set1i((int) x);
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
