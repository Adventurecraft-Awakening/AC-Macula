// Code written by daxnitro.  Do what you want with it but give me some credit if you use it in whole or in part.

package net.mine_diver.macula;

import net.fabricmc.loader.api.FabricLoader;
import net.mine_diver.macula.option.ShaderOption;
import net.mine_diver.macula.sources.*;
import net.mine_diver.macula.util.MinecraftInstance;
import net.mine_diver.macula.wrappers.ShaderProgram;
import net.minecraft.client.Minecraft;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.tile.Tile;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.slf4j.Logger;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.lwjgl.opengl.ARBFragmentShader.GL_FRAGMENT_SHADER_ARB;
import static org.lwjgl.opengl.ARBShaderObjects.*;
import static org.lwjgl.opengl.ARBTextureFloat.GL_RGB32F_ARB;
import static org.lwjgl.opengl.ARBVertexShader.GL_VERTEX_SHADER_ARB;
import static org.lwjgl.opengl.ARBVertexShader.glBindAttribLocationARB;
import static org.lwjgl.opengl.EXTFramebufferObject.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.util.glu.GLU.gluPerspective;

public class Shaders {
    private static boolean isInitialized = false;

    private static int renderWidth = 0;
    private static int renderHeight = 0;

    private static final FloatBuffer celestialModelView = BufferUtils.createFloatBuffer(16);
    private static final float[] sunPosition = new float[3];
    private static final float[] moonPosition = new float[3];

    private static final float[] clearColor = new float[3];

    private static float rainStrength = 0.0f;

    private static boolean fogEnabled = true;

    public static int entityAttrib = -1;

    private static FloatBuffer previousProjection = BufferUtils.createFloatBuffer(16);
    private static FloatBuffer projection = BufferUtils.createFloatBuffer(16);
    private static final FloatBuffer projectionInverse = BufferUtils.createFloatBuffer(16);

    private static FloatBuffer previousModelView = BufferUtils.createFloatBuffer(16);
    private static FloatBuffer modelView = BufferUtils.createFloatBuffer(16);
    private static final FloatBuffer modelViewInverse = BufferUtils.createFloatBuffer(16);

    private static final double[] previousCameraPosition = new double[3];
    private static final double[] cameraPosition = new double[3];

    // Shadow stuff

    // configuration
    private static int shadowPassInterval = 0;
    private static int shadowMapWidth = 1024;
    private static int shadowMapHeight = 1024;
    private static float shadowMapFOV = 25.0f;
    private static float shadowMapHalfPlane = 30.0f;
    private static boolean shadowMapIsOrtho = true;

    private static int shadowPassCounter = 0;

    private static boolean isShadowPass = false;

    private static int sfb = 0;
    private static int sfbDepthTexture = 0;
    private static int sfbDepthBuffer = 0;

    private static final FloatBuffer shadowProjection = BufferUtils.createFloatBuffer(16);
    private static final FloatBuffer shadowProjectionInverse = BufferUtils.createFloatBuffer(16);

    private static final FloatBuffer shadowModelView = BufferUtils.createFloatBuffer(16);
    private static final FloatBuffer shadowModelViewInverse = BufferUtils.createFloatBuffer(16);

    // Color attachment stuff

    private static int colorAttachments = 0;

    private static IntBuffer dfbDrawBuffers = null;

    private static IntBuffer dfbTextures = null;
    private static IntBuffer dfbRenderBuffers = null;

    private static int dfb = 0;
    private static int dfbDepthBuffer = 0;

    // Program stuff

    public static int activeProgram = 0;

    public final static int ProgramNone = 0;
    public final static int ProgramBasic = 1;
    public final static int ProgramTextured = 2;
    public final static int ProgramTexturedLit = 3;
    public final static int ProgramTerrain = 4;
    public final static int ProgramWater = 5;
    public final static int ProgramHand = 6;
    public final static int ProgramWeather = 7;
    public final static int ProgramComposite = 8;
    public final static int ProgramFinal = 9;
    public final static int ProgramCount = 10;

    private static final String[] programNames = new String[]{
        "",
        "gbuffers_basic",
        "gbuffers_textured",
        "gbuffers_textured_lit",
        "gbuffers_terrain",
        "gbuffers_water",
        "gbuffers_hand",
        "gbuffers_weather",
        "composite",
        "final",
    };

    private static final int[] programBackups = new int[]{
        ProgramNone,            // none
        ProgramNone,            // basic
        ProgramBasic,           // textured
        ProgramTextured,        // textured/lit
        ProgramTexturedLit,     // terrain
        ProgramTerrain,         // water
        ProgramTexturedLit,     // hand
        ProgramTexturedLit,     // weather
        ProgramNone,            // composite
        ProgramNone,            // final
    };

    private static final ShaderProgram[] programs = new ShaderProgram[ProgramCount];

    // shaderpack fields

    public static final File shaderPacksDir = FabricLoader.getInstance().getGameDir().resolve("shaderpacks").toFile();
    private static ShaderpackSource currentShaderSource = NullShaderpackSource.instance;
    public static boolean shaderPackLoaded = false;

    // debug info

    public static final String glVersionString = GL11.glGetString(GL11.GL_VERSION);
    public static final String glVendorString = GL11.glGetString(GL11.GL_VENDOR);
    public static final String glRendererString = GL11.glGetString(GL11.GL_RENDERER);

    // config stuff
    public static final File
        configDir = FabricLoader.getInstance().getConfigDir().resolve("macula").toFile(),
        shaderConfigFile = new File(configDir, "shaders.properties");

    public static final Properties shadersConfig = new Properties();

    public static float configShadowResMul = 1;

    private static final Logger logger = MaculaMod.LOGGER;

    static {
        if (!configDir.exists()) {
            if (!configDir.mkdirs()) {
                logger.warn("Failed to create config directory \"{}\".", configDir);
            }
        }
        loadConfig();

        Arrays.fill(programs, ShaderProgram.empty);
    }

    private Shaders() {
    }

    public static void init() {
        shaderPackLoaded = !(currentShaderSource instanceof NullShaderpackSource);
        if (!shaderPackLoaded) {
            return;
        }

        int maxDrawBuffers = glGetInteger(GL_MAX_DRAW_BUFFERS);
        logger.info("GL_MAX_DRAW_BUFFERS = {}", maxDrawBuffers);

        colorAttachments = 4;

        if (currentShaderSource instanceof ShaderpackFileSource fileSource) {
            String shadersDir = "shaders/";
            if (!fileSource.isDirectory(shadersDir)) {
                shadersDir = "";
            }

            String programDir = shadersDir + "world0/";
            if (!fileSource.isDirectory(programDir)) {
                programDir = shadersDir;
            }

            for (int i = 0; i < ProgramCount; ++i) {
                if (programNames[i].equals("")) {
                    programs[i] = ShaderProgram.empty;
                } else {
                    programs[i] = setupProgram(
                        fileSource,
                        shadersDir,
                        programDir + programNames[i] + ".vsh",
                        programDir + programNames[i] + ".fsh");
                }
            }
        }

        if (colorAttachments > maxDrawBuffers) {
            logger.error("Not enough draw buffers! ({} requested vs {} supported)", colorAttachments, maxDrawBuffers);
        }

        for (int i = 0; i < ProgramCount; ++i) {
            for (int n = i; programs[i].isEmpty(); n = programBackups[n]) {
                if (n == programBackups[n]) {
                    break;
                }
                programs[i] = programs[programBackups[n]];
            }
        }

        dfbDrawBuffers = BufferUtils.createIntBuffer(colorAttachments);
        for (int i = 0; i < colorAttachments; ++i) {
            dfbDrawBuffers.put(i, GL_COLOR_ATTACHMENT0_EXT + i);
        }

        dfbTextures = BufferUtils.createIntBuffer(colorAttachments);
        dfbRenderBuffers = BufferUtils.createIntBuffer(colorAttachments);

        resize();
        setupShadowMap();
        isInitialized = true;
    }

    public static void destroy() {
        for (int i = 0; i < ProgramCount; ++i) {
            programs[i].destroy();
            programs[i] = ShaderProgram.empty;
        }
    }

    public static void glEnableWrapper(int cap) {
        glEnable(cap);
        if (cap == GL_TEXTURE_2D) {
            if (activeProgram == ProgramBasic) useProgram(ProgramTextured);
        } else if (cap == GL_FOG) {
            fogEnabled = true;
            programs[activeProgram].uniform_fogMode.set1i(glGetInteger(GL_FOG_MODE));
        }
    }

    public static void glDisableWrapper(int cap) {
        glDisable(cap);
        if (cap == GL_TEXTURE_2D) {
            if (activeProgram == ProgramTextured || activeProgram == ProgramTexturedLit) useProgram(ProgramBasic);
        } else if (cap == GL_FOG) {
            fogEnabled = false;
            programs[activeProgram].uniform_fogMode.set1i(0);
        }
    }

    public static void setClearColor(float red, float green, float blue) {
        clearColor[0] = red;
        clearColor[1] = green;
        clearColor[2] = blue;

        if (isShadowPass) {
            glClearColor(clearColor[0], clearColor[1], clearColor[2], 1.0f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            return;
        }

        glDrawBuffers(dfbDrawBuffers);
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glDrawBuffers(GL_COLOR_ATTACHMENT0_EXT);
        glClearColor(clearColor[0], clearColor[1], clearColor[2], 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glDrawBuffers(GL_COLOR_ATTACHMENT1_EXT);
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glDrawBuffers(dfbDrawBuffers);
    }

    public static void setCamera(float f) {
        Mob viewEntity = MinecraftInstance.get().cameraEntity;

        double x = viewEntity.xOld + (viewEntity.x - viewEntity.xOld) * f;
        double y = viewEntity.yOld + (viewEntity.y - viewEntity.yOld) * f;
        double z = viewEntity.zOld + (viewEntity.z - viewEntity.zOld) * f;

        if (isShadowPass) {
            glViewport(0, 0, shadowMapWidth, shadowMapHeight);

            glMatrixMode(GL_PROJECTION);
            glLoadIdentity();

            // just backwards compatibility. it's only used when SHADOWFOV is set in the shaders.
            if (shadowMapIsOrtho)
                glOrtho(-shadowMapHalfPlane, shadowMapHalfPlane, -shadowMapHalfPlane, shadowMapHalfPlane, 0.05f, 256.0f);
            else
                gluPerspective(shadowMapFOV, (float) shadowMapWidth / (float) shadowMapHeight, 0.05f, 256.0f);

            glMatrixMode(GL_MODELVIEW);
            glLoadIdentity();
            glTranslatef(0.0f, 0.0f, -100.0f);
            glRotatef(90.0f, 0.0f, 0.0f, -1.0f);
            float angle = MinecraftInstance.get().level.getTimeOfDay(f) * 360.0f;
            // night time
            // day time
            if (angle < 90.0 || angle > 270.0)
                glRotatef(angle - 90.0f, -1.0f, 0.0f, 0.0f);
            else
                glRotatef(angle + 90.0f, -1.0f, 0.0f, 0.0f);

            // reduces jitter
            if (shadowMapIsOrtho)
                glTranslatef((float) x % 10.0f - 5.0f, (float) y % 10.0f - 5.0f, (float) z % 10.0f - 5.0f);

            glGetFloat(GL_PROJECTION_MATRIX, shadowProjection.clear());
            invertMat4x(shadowProjectionInverse.clear(), shadowProjection);

            glGetFloat(GL_MODELVIEW_MATRIX, shadowModelView.clear());
            invertMat4x(shadowModelViewInverse.clear(), shadowModelView);
            return;
        }

        // Swap buffers
        FloatBuffer nextProjection = previousProjection;
        previousProjection = projection;
        projection = nextProjection;
        glGetFloat(GL_PROJECTION_MATRIX, projection);
        invertMat4x(projectionInverse.clear(), projection);

        // Swap buffers
        FloatBuffer nextModelView = previousModelView;
        previousModelView = modelView;
        modelView = nextModelView;
        glGetFloat(GL_MODELVIEW_MATRIX, modelView);
        invertMat4x(modelViewInverse.clear(), modelView);

        previousCameraPosition[0] = cameraPosition[0];
        previousCameraPosition[1] = cameraPosition[1];
        previousCameraPosition[2] = cameraPosition[2];

        cameraPosition[0] = x;
        cameraPosition[1] = y;
        cameraPosition[2] = z;
    }

    public static void beginRender(Minecraft minecraft, float f, long l) {
        rainStrength = minecraft.level.getRainLevel(f);

        if (isShadowPass) return;

        if (!isInitialized) init();
        if (!shaderPackLoaded) return;

        if (minecraft.width != renderWidth || minecraft.height != renderHeight)
            resize();

        if (shadowPassInterval > 0 && --shadowPassCounter <= 0) {
            // do shadow pass
            boolean preShadowPassThirdPersonView = minecraft.options.thirdPersonView;

            minecraft.options.thirdPersonView = true;

            isShadowPass = true;
            shadowPassCounter = shadowPassInterval;

            glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, sfb);

            useProgram(ProgramNone);

            minecraft.gameRenderer.render(f, l);

            glFlush();

            isShadowPass = false;

            minecraft.options.thirdPersonView = preShadowPassThirdPersonView;
        }

        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, dfb);

        useProgram(ProgramTextured);
    }

    public static void endRender() {
        if (isShadowPass) return;

        glPushMatrix();

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        glDisable(GL_DEPTH_TEST);
        glEnable(GL_TEXTURE_2D);

        // composite

        glDisable(GL_BLEND);

        useProgram(ProgramComposite);

        glDrawBuffers(dfbDrawBuffers);

        glBindTexture(GL_TEXTURE_2D, dfbTextures.get(0));
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, dfbTextures.get(1));
        glActiveTexture(GL_TEXTURE2);
        glBindTexture(GL_TEXTURE_2D, dfbTextures.get(2));
        glActiveTexture(GL_TEXTURE3);
        glBindTexture(GL_TEXTURE_2D, dfbTextures.get(3));

        if (colorAttachments >= 5) {
            glActiveTexture(GL_TEXTURE4);
            glBindTexture(GL_TEXTURE_2D, dfbTextures.get(4));
            if (colorAttachments >= 6) {
                glActiveTexture(GL_TEXTURE5);
                glBindTexture(GL_TEXTURE_2D, dfbTextures.get(5));
                if (colorAttachments >= 7) {
                    glActiveTexture(GL_TEXTURE6);
                    glBindTexture(GL_TEXTURE_2D, dfbTextures.get(6));
                }
            }
        }

        if (shadowPassInterval > 0) {
            glActiveTexture(GL_TEXTURE7);
            glBindTexture(GL_TEXTURE_2D, sfbDepthTexture);
        }

        glActiveTexture(GL_TEXTURE0);

        glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        glBegin(GL_QUADS);
        glTexCoord2f(0.0f, 0.0f);
        glVertex3f(0.0f, 0.0f, 0.0f);
        glTexCoord2f(1.0f, 0.0f);
        glVertex3f(1.0f, 0.0f, 0.0f);
        glTexCoord2f(1.0f, 1.0f);
        glVertex3f(1.0f, 1.0f, 0.0f);
        glTexCoord2f(0.0f, 1.0f);
        glVertex3f(0.0f, 1.0f, 0.0f);
        glEnd();

        // final

        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);

        useProgram(ProgramFinal);

        glClearColor(clearColor[0], clearColor[1], clearColor[2], 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glBindTexture(GL_TEXTURE_2D, dfbTextures.get(0));
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, dfbTextures.get(1));
        glActiveTexture(GL_TEXTURE2);
        glBindTexture(GL_TEXTURE_2D, dfbTextures.get(2));
        glActiveTexture(GL_TEXTURE3);
        glBindTexture(GL_TEXTURE_2D, dfbTextures.get(3));

        if (colorAttachments >= 5) {
            glActiveTexture(GL_TEXTURE4);
            glBindTexture(GL_TEXTURE_2D, dfbTextures.get(4));
            if (colorAttachments >= 6) {
                glActiveTexture(GL_TEXTURE5);
                glBindTexture(GL_TEXTURE_2D, dfbTextures.get(5));
                if (colorAttachments >= 7) {
                    glActiveTexture(GL_TEXTURE6);
                    glBindTexture(GL_TEXTURE_2D, dfbTextures.get(6));
                }
            }
        }

        if (shadowPassInterval > 0) {
            glActiveTexture(GL_TEXTURE7);
            glBindTexture(GL_TEXTURE_2D, sfbDepthTexture);
        }

        glActiveTexture(GL_TEXTURE0);

        glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        glBegin(GL_QUADS);
        glTexCoord2f(0.0f, 0.0f);
        glVertex3f(0.0f, 0.0f, 0.0f);
        glTexCoord2f(1.0f, 0.0f);
        glVertex3f(1.0f, 0.0f, 0.0f);
        glTexCoord2f(1.0f, 1.0f);
        glVertex3f(1.0f, 1.0f, 0.0f);
        glTexCoord2f(0.0f, 1.0f);
        glVertex3f(0.0f, 1.0f, 0.0f);
        glEnd();

        glEnable(GL_BLEND);

        glPopMatrix();

        useProgram(ProgramNone);
    }

    public static void beginTerrain() {
        useProgram(Shaders.ProgramTerrain);
        glActiveTexture(GL_TEXTURE2);
        glBindTexture(GL_TEXTURE_2D, MinecraftInstance.get().textures.loadTexture("/terrain_nh.png"));
        glActiveTexture(GL_TEXTURE3);
        glBindTexture(GL_TEXTURE_2D, MinecraftInstance.get().textures.loadTexture("/terrain_s.png"));
        glActiveTexture(GL_TEXTURE0);
    }

    public static void endTerrain() {
        useProgram(ProgramTextured);
    }

    public static void beginWater() {
        useProgram(Shaders.ProgramWater);
        glActiveTexture(GL_TEXTURE2);
        glBindTexture(GL_TEXTURE_2D, MinecraftInstance.get().textures.loadTexture("/terrain_nh.png"));
        glActiveTexture(GL_TEXTURE3);
        glBindTexture(GL_TEXTURE_2D, MinecraftInstance.get().textures.loadTexture("/terrain_s.png"));
        glActiveTexture(GL_TEXTURE0);
    }

    public static void endWater() {
        useProgram(ProgramTextured);
    }

    public static void beginHand() {
        glEnable(GL_BLEND);
        useProgram(Shaders.ProgramHand);
    }

    public static void endHand() {
        glDisable(GL_BLEND);
        useProgram(ProgramTextured);

        if (isShadowPass)
            glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, sfb); // was set to 0 in beginWeather()
    }

    public static void beginWeather() {
        glEnable(GL_BLEND);
        useProgram(Shaders.ProgramWeather);

        if (isShadowPass)
            glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0); // will be set to sbf in endHand()
    }

    public static void endWeather() {
        glDisable(GL_BLEND);
        useProgram(ProgramTextured);
    }

    private static void resize() {
        renderWidth = MinecraftInstance.get().width;
        renderHeight = MinecraftInstance.get().height;
        setupFrameBuffer();
    }

    private static void setupShadowMap() {
        setupShadowFrameBuffer();
    }

    private static ShaderProgram setupProgram(
        ShaderpackFileSource source,
        String shadersDir,
        String vShaderPath,
        String fShaderPath) {
        int program = glCreateProgramObjectARB();

        int vShader = 0;
        int fShader = 0;

        if (program != 0) {
            vShader = createVertShader(source, shadersDir, vShaderPath);
            fShader = createFragShader(source, shadersDir, fShaderPath);
        }

        if (vShader != 0 || fShader != 0) {
            if (vShader != 0) glAttachObjectARB(program, vShader);
            if (fShader != 0) glAttachObjectARB(program, fShader);
            if (entityAttrib >= 0) glBindAttribLocationARB(program, entityAttrib, "mc_Entity");

            glLinkProgramARB(program);
            glValidateProgramARB(program);
            printLogInfo(program, vShaderPath + " + " + fShaderPath);

            if (glGetProgrami(program, GL_LINK_STATUS) == GL_TRUE) {
                return new ShaderProgram(program);
            }
        }

        if (program != 0) {
            glDeleteObjectARB(program);
        }
        return ShaderProgram.empty;
    }

    public static void useProgram(int program) {
        if (activeProgram == program) return;
        else if (isShadowPass) {
            activeProgram = ProgramNone;
            programs[ProgramNone].use();
            return;
        }
        activeProgram = program;
        ShaderProgram p = programs[program];
        p.use();
        if (p.isEmpty()) {
            return;
        } else if (program == ProgramTextured) {
            p.uniform_texture.set1i(0);
        } else if (program == ProgramTexturedLit || program == ProgramHand || program == ProgramWeather) {
            p.uniform_texture.set1i(0);
            p.uniform_lightmap.set1i(1);
        } else if (program == ProgramTerrain || program == ProgramWater) {
            p.uniform_texture.set1i(0);
            p.uniform_lightmap.set1i(1);
            p.uniform_normals.set1i(2);
            p.uniform_specular.set1i(3);
        } else if (program == ProgramComposite || program == ProgramFinal) {
            p.uniform_gcolor.set1i(0);
            p.uniform_gdepth.set1i(1);
            p.uniform_gnormal.set1i(2);
            p.uniform_composite.set1i(3);
            p.uniform_gaux1.set1i(4);
            p.uniform_gaux2.set1i(5);
            p.uniform_gaux3.set1i(6);
            p.uniform_shadow.set1i(7);
            p.uniform_gbufferPreviousProjection.setMat4(false, previousProjection);
            p.uniform_gbufferProjection.setMat4(false, projection);
            p.uniform_gbufferProjectionInverse.setMat4(false, projectionInverse);
            p.uniform_gbufferPreviousModelView.setMat4(false, previousModelView);
            if (shadowPassInterval > 0) {
                p.uniform_shadowProjection.setMat4(false, shadowProjection);
                p.uniform_shadowProjectionInverse.setMat4(false, shadowProjectionInverse);
                p.uniform_shadowModelView.setMat4(false, shadowModelView);
                p.uniform_shadowModelViewInverse.setMat4(false, shadowModelViewInverse);
            }
        }
        Minecraft minecraft = MinecraftInstance.get();
        ItemInstance stack = minecraft.player.inventory.getSelected();
        p.uniform_heldItemId.set1i((stack == null ? -1 : stack.id));
        p.uniform_heldBlockLightValue.set1i((stack == null || stack.id >= Tile.tiles.length ? 0 : Tile.lightEmission[stack.id]));
        p.uniform_fogMode.set1i((fogEnabled ? glGetInteger(GL_FOG_MODE) : 0));
        p.uniform_rainStrength.set1f(rainStrength);
        p.uniform_worldTime.set1i((int) (minecraft.level.getTime() % 24000L));
        p.uniform_aspectRatio.set1f((float) renderWidth / (float) renderHeight);
        p.uniform_viewWidth.set1f((float) renderWidth);
        p.uniform_viewHeight.set1f((float) renderHeight);
        p.uniform_near.set1f(0.05F);
        p.uniform_far.set1f(256 >> minecraft.options.viewDistance);
        p.uniform_sunPosition.set3f(sunPosition);
        p.uniform_moonPosition.set3f(moonPosition);
        p.uniform_previousCameraPosition.set3f(previousCameraPosition);
        p.uniform_cameraPosition.set3f(cameraPosition);
        p.uniform_gbufferModelView.setMat4(false, modelView);
        p.uniform_gbufferModelViewInverse.setMat4(false, modelViewInverse);
    }

    public static void setCelestialPosition() {
        // This is called when the current matrix is the modelview matrix based on the celestial angle.
        // The sun is at (0, 100, 0), and the moon is at (0, -100, 0).
        glGetFloat(GL_MODELVIEW_MATRIX, celestialModelView.clear());
        float[] mv = new float[16];
        celestialModelView.get(mv, 0, 16);
        multiplyMat4ByVec4ToVec3(sunPosition, mv, new float[]{0.0F, 100.0F, 0.0F, 0.0F});
        multiplyMat4ByVec4ToVec3(moonPosition, mv, new float[]{0.0F, -100.0F, 0.0F, 0.0F});
    }

    private static void multiplyMat4ByVec4ToVec3(float[] mout, float[] ta, float[] tb) {
        mout[0] = ta[0] * tb[0] + ta[4] * tb[1] + ta[8] * tb[2] + ta[12] * tb[3];
        mout[1] = ta[1] * tb[0] + ta[5] * tb[1] + ta[9] * tb[2] + ta[13] * tb[3];
        mout[2] = ta[2] * tb[0] + ta[6] * tb[1] + ta[10] * tb[2] + ta[14] * tb[3];
    }

    private static void invertMat4x(FloatBuffer dst, FloatBuffer matin) {
        float[] m = new float[16];
        float[] inv = new float[16];

        for (int i = 0; i < 16; ++i) {
            m[i] = matin.get(i);
        }

        inv[0] = m[5] * m[10] * m[15] - m[5] * m[11] * m[14] - m[9] * m[6] * m[15] + m[9] * m[7] * m[14] + m[13] * m[6] * m[11] - m[13] * m[7] * m[10];
        inv[4] = -m[4] * m[10] * m[15] + m[4] * m[11] * m[14] + m[8] * m[6] * m[15] - m[8] * m[7] * m[14] - m[12] * m[6] * m[11] + m[12] * m[7] * m[10];
        inv[8] = m[4] * m[9] * m[15] - m[4] * m[11] * m[13] - m[8] * m[5] * m[15] + m[8] * m[7] * m[13] + m[12] * m[5] * m[11] - m[12] * m[7] * m[9];
        inv[12] = -m[4] * m[9] * m[14] + m[4] * m[10] * m[13] + m[8] * m[5] * m[14] - m[8] * m[6] * m[13] - m[12] * m[5] * m[10] + m[12] * m[6] * m[9];
        inv[1] = -m[1] * m[10] * m[15] + m[1] * m[11] * m[14] + m[9] * m[2] * m[15] - m[9] * m[3] * m[14] - m[13] * m[2] * m[11] + m[13] * m[3] * m[10];
        inv[5] = m[0] * m[10] * m[15] - m[0] * m[11] * m[14] - m[8] * m[2] * m[15] + m[8] * m[3] * m[14] + m[12] * m[2] * m[11] - m[12] * m[3] * m[10];
        inv[9] = -m[0] * m[9] * m[15] + m[0] * m[11] * m[13] + m[8] * m[1] * m[15] - m[8] * m[3] * m[13] - m[12] * m[1] * m[11] + m[12] * m[3] * m[9];
        inv[13] = m[0] * m[9] * m[14] - m[0] * m[10] * m[13] - m[8] * m[1] * m[14] + m[8] * m[2] * m[13] + m[12] * m[1] * m[10] - m[12] * m[2] * m[9];
        inv[2] = m[1] * m[6] * m[15] - m[1] * m[7] * m[14] - m[5] * m[2] * m[15] + m[5] * m[3] * m[14] + m[13] * m[2] * m[7] - m[13] * m[3] * m[6];
        inv[6] = -m[0] * m[6] * m[15] + m[0] * m[7] * m[14] + m[4] * m[2] * m[15] - m[4] * m[3] * m[14] - m[12] * m[2] * m[7] + m[12] * m[3] * m[6];
        inv[10] = m[0] * m[5] * m[15] - m[0] * m[7] * m[13] - m[4] * m[1] * m[15] + m[4] * m[3] * m[13] + m[12] * m[1] * m[7] - m[12] * m[3] * m[5];
        inv[14] = -m[0] * m[5] * m[14] + m[0] * m[6] * m[13] + m[4] * m[1] * m[14] - m[4] * m[2] * m[13] - m[12] * m[1] * m[6] + m[12] * m[2] * m[5];
        inv[3] = -m[1] * m[6] * m[11] + m[1] * m[7] * m[10] + m[5] * m[2] * m[11] - m[5] * m[3] * m[10] - m[9] * m[2] * m[7] + m[9] * m[3] * m[6];
        inv[7] = m[0] * m[6] * m[11] - m[0] * m[7] * m[10] - m[4] * m[2] * m[11] + m[4] * m[3] * m[10] + m[8] * m[2] * m[7] - m[8] * m[3] * m[6];
        inv[11] = -m[0] * m[5] * m[11] + m[0] * m[7] * m[9] + m[4] * m[1] * m[11] - m[4] * m[3] * m[9] - m[8] * m[1] * m[7] + m[8] * m[3] * m[5];
        inv[15] = m[0] * m[5] * m[10] - m[0] * m[6] * m[9] - m[4] * m[1] * m[10] + m[4] * m[2] * m[9] + m[8] * m[1] * m[6] - m[8] * m[2] * m[5];

        float det = m[0] * inv[0] + m[1] * inv[4] + m[2] * inv[8] + m[3] * inv[12];

        // no inverse :(
        if (det == 0.0) {
            return; // not actually the inverse
        }

        for (int i = 0; i < 16; ++i) {
            dst.put(i, inv[i] / det);
        }
    }

    private static boolean processShaderFile(
        ShaderpackFileSource source,
        String shadersDir,
        StringBuilder output,
        String filename,
        Consumer<String> onLine) throws IOException {

        InputStream stream = source.getInputStream(filename);
        if (stream == null) {
            return false;
        }

        try (var reader = new BufferedReader(new InputStreamReader(stream))) {
            int lineIndex = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                lineIndex++;
                onLine.accept(line);

                String stripped = line.stripLeading();
                if (stripped.startsWith("#")) {
                    if (stripped.startsWith("#include")) {
                        processInclude(source, shadersDir, output, filename, onLine, line, lineIndex);
                        continue;
                    } else if (stripped.startsWith("#define")) {
                    } else if (stripped.startsWith("#undef")) {
                    } else if (stripped.startsWith("#if")) {
                    } else if (stripped.startsWith("#ifdef")) {
                    } else if (stripped.startsWith("#ifndef")) {
                    } else if (stripped.startsWith("#else")) {
                    } else if (stripped.startsWith("#elif")) {
                    } else if (stripped.startsWith("#endif")) {
                    } else if (stripped.startsWith("#error")) {
                    } else if (stripped.startsWith("#pragma")) {
                    } else if (stripped.startsWith("#extension")) {
                    } else if (stripped.startsWith("#version")) {
                    } else if (stripped.startsWith("#line")) {
                        // TODO
                    } else {
                        logger.warn(
                            "Unknown preprocessor directive \"{}\" in file \"{}\" at line {}.",
                            line, filename, lineIndex);
                    }
                }
                output.append(line).append("\n");
            }
        }
        return true;
    }

    private static void processInclude(
        ShaderpackFileSource source,
        String shadersDir,
        StringBuilder output,
        String filename,
        Consumer<String> onLine,
        String line,
        int lineIndex) throws IOException {

        int firstQuote = line.indexOf("\"");
        int secondQuote = line.indexOf("\"", firstQuote + 1);
        if (secondQuote == -1) {
            logger.warn(
                "Malformed include directive \"{}\" in file \"{}\" at line {}.",
                line, filename, lineIndex);
            return;
        }

        String includeStr = line.substring(firstQuote + 1, secondQuote);
        String includePath;
        if (includeStr.startsWith("/")) {
            includePath = shadersDir + includeStr.substring(1);
        } else {
            int lastFileSlash = filename.lastIndexOf("/");
            includePath = filename.substring(0, lastFileSlash + 1) + includeStr;
        }

        if (!processShaderFile(source, shadersDir, output, includePath, onLine)) {
            logger.warn(
                "Missing file \"{}\" for include directive in file \"{}\" at line {}.",
                includePath, filename, lineIndex);
        }
    }

    private static int createVertShader(ShaderpackFileSource source, String shadersDir, String filename) {
        var vertexCode = new StringBuilder();
        try {
            if (!processShaderFile(source, shadersDir, vertexCode, filename, (line) -> {
                if (line.matches("attribute [_a-zA-Z0-9]+ mc_Entity.*")) {
                    entityAttrib = 10;
                }
            })) {
                return 0;
            }
        } catch (Exception ex) {
            logger.error("Failed to read vertex shader \"{}\": ", filename, ex);
            return 0;
        }

        int vertShader = glCreateShaderObjectARB(GL_VERTEX_SHADER_ARB);
        if (vertShader == 0) {
            return 0;
        }

        glShaderSourceARB(vertShader, vertexCode);
        glCompileShaderARB(vertShader);
        if (printLogInfo(vertShader, filename)) {
            logger.trace("Source for vertex shader \"{}\": \n{}", filename, vertexCode);
        }
        return vertShader;
    }

    private static int createFragShader(ShaderpackFileSource source, String shadersDir, String filename) {
        var fragCode = new StringBuilder();
        try {
            if (!processShaderFile(source, shadersDir, fragCode, filename, (line) -> {
                if (colorAttachments < 5 && line.matches("uniform [ _a-zA-Z0-9]+ gaux1;.*")) colorAttachments = 5;
                else if (colorAttachments < 6 && line.matches("uniform [ _a-zA-Z0-9]+ gaux2;.*"))
                    colorAttachments = 6;
                else if (colorAttachments < 7 && line.matches("uniform [ _a-zA-Z0-9]+ gaux3;.*"))
                    colorAttachments = 7;
                else if (colorAttachments < 8 && line.matches("uniform [ _a-zA-Z0-9]+ shadow;.*")) {
                    shadowPassInterval = 1;
                    colorAttachments = 8;
                } else if (line.matches("/\\* SHADOWRES:[0-9]+ \\*/.*")) {
                    String[] parts = line.split("([: ])", 4);
                    shadowMapWidth = shadowMapHeight = Math.round(Integer.parseInt(parts[2]) * configShadowResMul);
                    logger.info("Shadow map resolution: {}", shadowMapWidth);
                } else if (line.matches("/\\* SHADOWFOV:[0-9.]+ \\*/.*")) {
                    String[] parts = line.split("([: ])", 4);
                    logger.info("Shadow map field of view: {}", parts[2]);
                    shadowMapFOV = Float.parseFloat(parts[2]);
                    shadowMapIsOrtho = false;
                } else if (line.matches("/\\* SHADOWHPL:[0-9.]+ \\*/.*")) {
                    String[] parts = line.split("([: ])", 4);
                    logger.info("Shadow map half-plane: {}", parts[2]);
                    shadowMapHalfPlane = Float.parseFloat(parts[2]);
                    shadowMapIsOrtho = true;
                }
            })) {
                return 0;
            }
        } catch (Exception ex) {
            logger.error("Failed to read frag shader \"{}\":", filename, ex);
            return 0;
        }

        int fragShader = glCreateShaderObjectARB(GL_FRAGMENT_SHADER_ARB);
        if (fragShader == 0) {
            return 0;
        }

        glShaderSourceARB(fragShader, fragCode);
        glCompileShaderARB(fragShader);
        if (printLogInfo(fragShader, filename)) {
            logger.trace("Source for frag shader \"{}\": \n{}", filename, fragCode);
        }
        return fragShader;
    }

    private static boolean printLogInfo(int obj, String fileName) {
        IntBuffer iVal = BufferUtils.createIntBuffer(1);
        glGetObjectParameterARB(obj, GL_OBJECT_INFO_LOG_LENGTH_ARB, iVal);

        int length = iVal.get();
        if (length > 1) {
            ByteBuffer infoLog = BufferUtils.createByteBuffer(length);
            iVal.flip();
            glGetInfoLogARB(obj, iVal, infoLog);
            CharBuffer out = StandardCharsets.UTF_8.decode(infoLog);
            logger.warn("Info log for \"{}\": \n{}", fileName, out);
            return true;
        }
        return false;
    }

    private static void setupFrameBuffer() {
        setupRenderTextures();

        if (dfb != 0) {
            glDeleteFramebuffersEXT(dfb);
            glDeleteRenderbuffersEXT(dfbRenderBuffers);
        }

        dfb = glGenFramebuffersEXT();
        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, dfb);

        glGenRenderbuffersEXT(dfbRenderBuffers);

        for (int i = 0; i < colorAttachments; ++i) {
            glBindRenderbufferEXT(GL_RENDERBUFFER_EXT, dfbRenderBuffers.get(i));
            // depth buffer
            if (i == 1) glRenderbufferStorageEXT(GL_RENDERBUFFER_EXT, GL_RGB32F_ARB, renderWidth, renderHeight);
            else glRenderbufferStorageEXT(GL_RENDERBUFFER_EXT, GL_RGBA, renderWidth, renderHeight);
            glFramebufferRenderbufferEXT(GL_FRAMEBUFFER_EXT, dfbDrawBuffers.get(i), GL_RENDERBUFFER_EXT, dfbRenderBuffers.get(i));
            glFramebufferTexture2DEXT(GL_FRAMEBUFFER_EXT, dfbDrawBuffers.get(i), GL_TEXTURE_2D, dfbTextures.get(i), 0);
        }

        glDeleteRenderbuffersEXT(dfbDepthBuffer);
        dfbDepthBuffer = glGenRenderbuffersEXT();
        glBindRenderbufferEXT(GL_RENDERBUFFER_EXT, dfbDepthBuffer);
        glRenderbufferStorageEXT(GL_RENDERBUFFER_EXT, GL_DEPTH_COMPONENT, renderWidth, renderHeight);
        glFramebufferRenderbufferEXT(GL_FRAMEBUFFER_EXT, GL_DEPTH_ATTACHMENT_EXT, GL_RENDERBUFFER_EXT, dfbDepthBuffer);

        int status = glCheckFramebufferStatusEXT(GL_FRAMEBUFFER_EXT);
        if (status != GL_FRAMEBUFFER_COMPLETE_EXT) {
            logger.error("Failed creating framebuffer! (Status {})", status);
        }
    }

    private static void setupShadowFrameBuffer() {
        if (shadowPassInterval <= 0) return;

        setupShadowRenderTexture();

        glDeleteFramebuffersEXT(sfb);

        sfb = glGenFramebuffersEXT();
        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, sfb);

        glDrawBuffer(GL_NONE);
        glReadBuffer(GL_NONE);

        glDeleteRenderbuffersEXT(sfbDepthBuffer);
        sfbDepthBuffer = glGenRenderbuffersEXT();
        glBindRenderbufferEXT(GL_RENDERBUFFER_EXT, sfbDepthBuffer);
        glRenderbufferStorageEXT(GL_RENDERBUFFER_EXT, GL_DEPTH_COMPONENT, shadowMapWidth, shadowMapHeight);
        glFramebufferRenderbufferEXT(GL_FRAMEBUFFER_EXT, GL_DEPTH_ATTACHMENT_EXT, GL_RENDERBUFFER_EXT, sfbDepthBuffer);
        glFramebufferTexture2DEXT(GL_FRAMEBUFFER_EXT, GL_DEPTH_ATTACHMENT_EXT, GL_TEXTURE_2D, sfbDepthTexture, 0);

        int status = glCheckFramebufferStatusEXT(GL_FRAMEBUFFER_EXT);
        if (status != GL_FRAMEBUFFER_COMPLETE_EXT) {
            logger.error("Failed creating shadow framebuffer! (Status {})", status);
        }
    }

    private static void setupRenderTextures() {
        glDeleteTextures(dfbTextures);
        glGenTextures(dfbTextures);

        for (int i = 0; i < colorAttachments; ++i) {
            glBindTexture(GL_TEXTURE_2D, dfbTextures.get(i));
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            if (i == 1) { // depth buffer
                ByteBuffer buffer = ByteBuffer.allocateDirect(renderWidth * renderHeight * 4 * 4);
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB32F_ARB, renderWidth, renderHeight, 0, GL_RGBA, GL11.GL_FLOAT, buffer);
            } else {
                ByteBuffer buffer = ByteBuffer.allocateDirect(renderWidth * renderHeight * 4);
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, renderWidth, renderHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
            }
        }
    }

    private static void setupShadowRenderTexture() {
        if (shadowPassInterval <= 0) return;

        // depth
        glDeleteTextures(sfbDepthTexture);
        sfbDepthTexture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, sfbDepthTexture);

        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        ByteBuffer buffer = ByteBuffer.allocateDirect(shadowMapWidth * shadowMapHeight * 4);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, shadowMapWidth, shadowMapHeight, 0, GL_DEPTH_COMPONENT, GL11.GL_FLOAT, buffer);
    }

    // shaderpacks

    public static List<ShaderpackSource> listOfShaderpacks() {
        var folderShaders = new ArrayList<ShaderpackDirSource>();
        var zipShaders = new ArrayList<ShaderpackZipSource>();

        try {
            if (!shaderPacksDir.exists()) {
                //noinspection ResultOfMethodCallIgnored
                shaderPacksDir.mkdir();
            } else {
                String[] files = shaderPacksDir.list();
                assert files != null;

                for (String filePath : files) {
                    File file = new File(shaderPacksDir, filePath);
                    String s = file.getName();

                    if (file.isDirectory()) {
                        if (!s.equals("debug")) {
                            folderShaders.add(new ShaderpackDirSource(file));
                        }
                    } else if (file.isFile() && s.toLowerCase().endsWith(".zip")) {
                        try {
                            zipShaders.add(new ShaderpackZipSource(file));
                        } catch (IOException ex) {
                            logger.error("Failed to open shaderpack zip \"{}\": ", file, ex);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("Failed to access shaderpacks: ", ex);
        }

        folderShaders.sort(Comparator.comparing(ShaderpackSource::getName, String.CASE_INSENSITIVE_ORDER));
        zipShaders.sort(Comparator.comparing(ShaderpackSource::getName, String.CASE_INSENSITIVE_ORDER));
        var packs = new ArrayList<ShaderpackSource>();
        packs.add(NullShaderpackSource.instance);
        packs.add(InternalShaderpackSource.instance);
        packs.addAll(folderShaders);
        packs.addAll(zipShaders);
        return packs;
    }

    public static void closeListOfShaderpacks(List<ShaderpackSource> sources) {
        for (ShaderpackSource source : sources) {
            closeShaderpack(source);
        }
    }

    public static void closeShaderpack(ShaderpackSource source) {
        try {
            source.close();
        } catch (IOException ex) {
            MaculaMod.LOGGER.warn("Failed to close shaderpack source: ", ex);
        }
    }

    public static void loadConfig() {
        try {
            if (!shaderPacksDir.exists()) {
                shaderPacksDir.mkdir();
            }
        } catch (Exception ex) {
            logger.warn("Failed to create shaderpacks directory \"{}\": ", shaderPacksDir, ex);
        }

        shadersConfig.setProperty(ShaderOption.SHADER_PACK.getPropertyKey(), "");

        if (shaderConfigFile.exists())
            try (FileReader filereader = new FileReader(shaderConfigFile)) {
                shadersConfig.load(filereader);
            } catch (Exception ex) {
                logger.warn("Failed to read config file \"{}\": ", shaderConfigFile, ex);
            }

        if (!shaderConfigFile.exists()) {
            storeConfig();
        }

        for (ShaderOption option : ShaderOption.values()) {
            setEnumShaderOption(option, shadersConfig.getProperty(option.getPropertyKey(), option.getValueDefault()));
        }
        //loadShaderPack();
    }

    private static void setEnumShaderOption(ShaderOption eso, String str) {
        if (str == null) {
            str = eso.getValueDefault();
        }

        switch (eso) {
            case SHADOW_RES_MUL -> {
                try {
                    configShadowResMul = Float.parseFloat(str);
                } catch (NumberFormatException ex) {
                    configShadowResMul = 1;
                    logger.warn("Failed to parse shader option {} with value \"{}\": ", eso, str, ex);
                }
            }
            case SHADER_PACK -> setShaderPack(str);
            default -> throw new IllegalArgumentException("Unknown option: " + eso);
        }
    }

    public static void storeConfig() {
        for (ShaderOption enumshaderoption : ShaderOption.values()) {
            shadersConfig.setProperty(enumshaderoption.getPropertyKey(), getEnumShaderOption(enumshaderoption));
        }

        try (FileWriter filewriter = new FileWriter(shaderConfigFile)) {
            shadersConfig.store(filewriter, null);
        } catch (Exception ex) {
            logger.warn("Failed to write config file \"{}\": ", shaderConfigFile, ex);
        }
    }

    public static String getEnumShaderOption(ShaderOption eso) {
        return switch (eso) {
            case SHADOW_RES_MUL -> Float.toString(configShadowResMul);
            case SHADER_PACK -> currentShaderSource.getName();
        };
    }

    public static void setShaderPack(String shaderPackName) {
        List<ShaderpackSource> sources = listOfShaderpacks();
        Stream<ShaderpackSource> filtered = sources.stream()
            .filter(src -> src.getName().equals(shaderPackName));
        ShaderpackSource foundSource = filtered.findFirst().orElse(null);
        setCurrentShaderSource(foundSource);
        sources.remove(foundSource);
        closeListOfShaderpacks(sources);
    }

    private static void setCurrentShaderSource(ShaderpackSource source) {
        ShaderpackSource newSource = source != null ? source : NullShaderpackSource.instance;
        if (currentShaderSource == newSource) {
            return;
        }
        closeShaderpack(currentShaderSource);
        currentShaderSource = newSource;
        shadersConfig.setProperty(ShaderOption.SHADER_PACK.getPropertyKey(), newSource.getName());
    }

    public static ShaderpackSource getCurrentShaderSource() {
        return currentShaderSource;
    }

    public static void loadShaderPack() {
        destroy();
        isInitialized = false;
        init();
        MinecraftInstance.get().levelRenderer.allChanged();
    }
}