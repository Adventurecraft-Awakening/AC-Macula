package net.mine_diver.macula.mixin;

import net.mine_diver.macula.Shaders;
import net.mine_diver.macula.util.TessellatorAccessor;
import net.minecraft.block.BlockBase;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.block.BlockRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockRenderer.class)
public class BlockRendererMixin {

    @Inject(
        method = "render(Lnet/minecraft/block/BlockBase;III)Z",
        at = @At("HEAD")
    )
    private void onRenderBlockByRenderType(BlockBase blockBase, int x, int y, int z, CallbackInfoReturnable<Boolean> cir) {
        if (Shaders.shaderPackLoaded) {
            if (Shaders.entityAttrib >= 0)
                ((TessellatorAccessor) Tessellator.INSTANCE).setEntity(blockBase.id);
        }
    }
}
