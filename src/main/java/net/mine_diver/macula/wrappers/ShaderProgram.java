package net.mine_diver.macula.wrappers;


import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.ARBShaderObjects.*;
import static org.lwjgl.opengl.GL20.GL_ACTIVE_UNIFORMS;

public class ShaderProgram {

    public static ShaderProgram empty = new ShaderProgram(0);

    private int program;

    public final UniformLocation uniform_texture;
    public final UniformLocation uniform_lightmap;
    public final UniformLocation uniform_normals;
    public final UniformLocation uniform_specular;

    public final UniformLocation uniform_gcolor;
    public final UniformLocation uniform_gdepth;
    public final UniformLocation uniform_gnormal;
    public final UniformLocation uniform_composite;
    public final UniformLocation uniform_gaux1;
    public final UniformLocation uniform_gaux2;
    public final UniformLocation uniform_gaux3;
    public final UniformLocation uniform_shadow;

    public final UniformLocation uniform_gbufferPreviousProjection;
    public final UniformLocation uniform_gbufferProjection;
    public final UniformLocation uniform_gbufferProjectionInverse;
    public final UniformLocation uniform_gbufferPreviousModelView;
    public final UniformLocation uniform_shadowProjection;
    public final UniformLocation uniform_shadowProjectionInverse;
    public final UniformLocation uniform_shadowModelView;
    public final UniformLocation uniform_shadowModelViewInverse;

    public final UniformLocation uniform_heldItemId;
    public final UniformLocation uniform_heldBlockLightValue;
    public final UniformLocation uniform_fogMode;
    public final UniformLocation uniform_rainStrength;
    public final UniformLocation uniform_worldTime;
    public final UniformLocation uniform_aspectRatio;
    public final UniformLocation uniform_viewWidth;
    public final UniformLocation uniform_viewHeight;
    public final UniformLocation uniform_near;
    public final UniformLocation uniform_far;
    public final UniformLocation uniform_sunPosition;
    public final UniformLocation uniform_moonPosition;
    public final UniformLocation uniform_previousCameraPosition;
    public final UniformLocation uniform_cameraPosition;
    public final UniformLocation uniform_gbufferModelView;
    public final UniformLocation uniform_gbufferModelViewInverse;

    public ShaderProgram(int program) {
        this.program = program;

        var uniformMap = new HashMap<String, UniformInfo>();
        if (this.program != 0) {
            getUniformInfo(this.program, uniformMap);
        }

        uniform_texture = getUniformLocation("texture", uniformMap);
        uniform_lightmap = getUniformLocation("lightmap", uniformMap);
        uniform_normals = getUniformLocation("normals", uniformMap);
        uniform_specular = getUniformLocation("specular", uniformMap);

        uniform_gcolor = getUniformLocation("gcolor", uniformMap);
        uniform_gdepth = getUniformLocation("gdepth", uniformMap);
        uniform_gnormal = getUniformLocation("gnormal", uniformMap);
        uniform_composite = getUniformLocation("composite", uniformMap);
        uniform_gaux1 = getUniformLocation("gaux1", uniformMap);
        uniform_gaux2 = getUniformLocation("gaux2", uniformMap);
        uniform_gaux3 = getUniformLocation("gaux3", uniformMap);
        uniform_shadow = getUniformLocation("shadow", uniformMap);

        uniform_gbufferPreviousProjection = getUniformLocation("gbufferPreviousProjection", uniformMap);
        uniform_gbufferProjection = getUniformLocation("gbufferProjection", uniformMap);
        uniform_gbufferProjectionInverse = getUniformLocation("gbufferProjectionInverse", uniformMap);
        uniform_gbufferPreviousModelView = getUniformLocation("gbufferPreviousModelView", uniformMap);
        uniform_shadowProjection = getUniformLocation("shadowProjection", uniformMap);
        uniform_shadowProjectionInverse = getUniformLocation("shadowProjectionInverse", uniformMap);
        uniform_shadowModelView = getUniformLocation("shadowModelView", uniformMap);
        uniform_shadowModelViewInverse = getUniformLocation("shadowModelViewInverse", uniformMap);

        uniform_heldItemId = getUniformLocation("heldItemId", uniformMap);
        uniform_heldBlockLightValue = getUniformLocation("heldBlockLightValue", uniformMap);
        uniform_fogMode = getUniformLocation("fogMode", uniformMap);
        uniform_rainStrength = getUniformLocation("rainStrength", uniformMap);
        uniform_worldTime = getUniformLocation("worldTime", uniformMap);
        uniform_aspectRatio = getUniformLocation("aspectRatio", uniformMap);
        uniform_viewWidth = getUniformLocation("viewWidth", uniformMap);
        uniform_viewHeight = getUniformLocation("viewHeight", uniformMap);
        uniform_near = getUniformLocation("near", uniformMap);
        uniform_far = getUniformLocation("far", uniformMap);
        uniform_sunPosition = getUniformLocation("sunPosition", uniformMap);
        uniform_moonPosition = getUniformLocation("moonPosition", uniformMap);
        uniform_previousCameraPosition = getUniformLocation("previousCameraPosition", uniformMap);
        uniform_cameraPosition = getUniformLocation("cameraPosition", uniformMap);
        uniform_gbufferModelView = getUniformLocation("gbufferModelView", uniformMap);
        uniform_gbufferModelViewInverse = getUniformLocation("gbufferModelViewInverse", uniformMap);
    }

    private UniformLocation getUniformLocation(String name, HashMap<String, UniformInfo> map) {
        if (program == 0) {
            return UniformNull.instance;
        }

        int location = glGetUniformLocationARB(program, name);
        if (location == -1) {
            return UniformNull.instance;
        }

        UniformInfo info = map.get(name);
        return switch (info.type) {
            case GL11.GL_FLOAT -> new Uniform1F(location);
            case GL20.GL_FLOAT_VEC3 -> new Uniform3F(location);
            case GL11.GL_INT -> new Uniform1I(location);
            case GL20.GL_FLOAT_MAT4 -> new UniformMatrix4F(location);
            case GL20.GL_SAMPLER_2D -> new UniformSampler2D(location);
            default -> new UniformNull(location);
        };
    }

    public boolean isEmpty() {
        return this.program == 0;
    }

    public void use() {
        glUseProgramObjectARB(this.program);
    }

    public void destroy() {
        if (this.program == 0) {
            return;
        }
        glDeleteObjectARB(this.program);
        this.program = 0;
    }

    private static void getUniformInfo(int program, Map<String, UniformInfo> map) {
        int uniformCount = GL20.glGetProgrami(program, GL_ACTIVE_UNIFORMS);
        IntBuffer lengthBuf = BufferUtils.createIntBuffer(1);
        IntBuffer sizeBuf = BufferUtils.createIntBuffer(1);
        IntBuffer typeBuf = BufferUtils.createIntBuffer(1);
        ByteBuffer nameBuf = ByteBuffer.allocateDirect(1024 * 16);

        for (int i = 0; i < uniformCount; i++) {
            lengthBuf.clear();
            sizeBuf.clear();
            typeBuf.clear();
            nameBuf.clear();
            GL20.glGetActiveUniform(program, i, lengthBuf, sizeBuf, typeBuf, nameBuf);

            nameBuf.limit(lengthBuf.get());
            String uniformName = StandardCharsets.UTF_8.decode(nameBuf).toString();
            if (uniformName.length() != 0) {
                map.put(uniformName, new UniformInfo(sizeBuf.get(), typeBuf.get()));
            }
        }
    }

    private record UniformInfo(int size, int type) {
    }
}

