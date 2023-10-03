package net.mine_diver.macula.wrappers;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.ARBShaderObjects.glUniformMatrix4ARB;

public final class UniformMatrix4F extends UniformLocation {

    public UniformMatrix4F(int location) {
        super(location);
    }

    @Override
    public void set1i(int x) {
        throw new UnsupportedOperationException();
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
        if (matrix == null) return;
        glUniformMatrix4ARB(location, transpose, matrix);
    }
}
