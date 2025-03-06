package net.mine_diver.macula.mixin;

import net.mine_diver.macula.Shaders;
import net.mine_diver.macula.util.TessellatorAccessor;
import net.minecraft.client.renderer.Chunk;
import net.minecraft.client.renderer.Tesselator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Chunk.class)
public class ChunkBuilderMixin {

    @Inject(method = "rebuild", at = @At(value = "RETURN"))
    private void onUpdateRenderer(CallbackInfo ci) {
        if (!Shaders.shaderPackLoaded) return;
        if (Shaders.entityAttrib >= 0)
            ((TessellatorAccessor) Tesselator.instance).setEntity(-1);
    }
}
