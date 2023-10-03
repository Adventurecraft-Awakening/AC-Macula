package net.mine_diver.macula.wrappers;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.ARBShaderObjects.glUniform1iARB;

public final class UniformSampler2D extends UniformLocation {

    public UniformSampler2D(int location) {
        super(location);
    }

    @Override
    public void set1i(int x) {
        glUniform1iARB(location, x);
    }

    @Override
    public void set1f(float x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void set3f(float x, float y, float z) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setMat4(boolean transpose, FloatBuffer matrix) {
        throw new UnsupportedOperationException();
    }
}
