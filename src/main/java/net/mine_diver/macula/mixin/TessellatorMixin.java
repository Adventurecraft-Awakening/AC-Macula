package net.mine_diver.macula.mixin;

import net.mine_diver.macula.Shaders;
import net.mine_diver.macula.util.TessellatorAccessor;
import net.minecraft.client.MemoryTracker;
import net.minecraft.client.renderer.Tesselator;
import org.lwjgl.opengl.ARBVertexProgram;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

@Mixin(Tesselator.class)
public class TessellatorMixin implements TessellatorAccessor {
    @Shadow
    private int mode;

    @Shadow
    private static boolean TRIANGLE_MODE;

    @Shadow
    private int vertices;

    @Shadow
    private boolean hasNormal;

    @Shadow
    private int[] array;

    @Shadow
    private int p;

    @Inject(
        method = "<init>(I)V",
        at = @At("RETURN")
    )
    private void onCor(int var1, CallbackInfo ci) {
        shadersBuffer = MemoryTracker.createByteBuffer(var1 / 8 * 4);
        shadersShortBuffer = shadersBuffer.asShortBuffer();
    }

    @Inject(
        method = "end",
        at = @At(
            value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glDrawArrays(III)V"
        )
    )
    private void onDraw1(CallbackInfo ci) {
        if (!Shaders.shaderPackLoaded) return;
        if (Shaders.entityAttrib >= 0) {
            ARBVertexProgram.glEnableVertexAttribArrayARB(Shaders.entityAttrib);
            ARBVertexProgram.glVertexAttribPointerARB(Shaders.entityAttrib, 2, false, false, 4, shadersShortBuffer.position(0));
        }
    }

    @Inject(
        method = "end",
        at = @At(
            value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glDrawArrays(III)V",
            shift = At.Shift.AFTER
        )
    )
    private void onDraw2(CallbackInfo ci) {
        if (!Shaders.shaderPackLoaded) return;
        if (Shaders.entityAttrib >= 0)
            ARBVertexProgram.glDisableVertexAttribArrayARB(Shaders.entityAttrib);
    }

    @Inject(
        method = "clear()V",
        at = @At(value = "RETURN")
    )
    private void onReset(CallbackInfo ci) {
        if (!Shaders.shaderPackLoaded) return;
        shadersBuffer.clear();
    }

    @Inject(
        method = "vertex",
        at = @At(value = "HEAD")
    )
    private void onAddVertex(CallbackInfo ci) {
        if (!Shaders.shaderPackLoaded) return;
        if (mode == 7 && TRIANGLE_MODE && (vertices + 1) % 4 == 0) {
            if (hasNormal) {
                array[p + 6] = array[(p - 24) + 6];
                array[p + 8 + 6] = array[(p + 8 - 16) + 6];
            }

            if (Shaders.entityAttrib >= 0) {
                shadersBuffer.putShort(this.entityId).putShort((short) 0);
                shadersBuffer.putShort(this.entityId).putShort((short) 0);
            }
        }

        if (Shaders.entityAttrib >= 0) {
            shadersBuffer.putShort(this.entityId).putShort((short) 0);
        }
    }

    @Override
    @Unique
    public void setEntity(int id) {
        this.entityId = (short) id;
    }

    public ByteBuffer shadersBuffer;
    public ShortBuffer shadersShortBuffer;
    private short entityId = -1;
}
