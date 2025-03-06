package net.mine_diver.macula.mixin;

import net.mine_diver.macula.Shaders;
import net.mine_diver.macula.util.TessellatorAccessor;
import net.minecraft.client.renderer.Tesselator;
import net.minecraft.client.renderer.TileRenderer;
import net.minecraft.world.level.tile.Tile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TileRenderer.class)
public class BlockRendererMixin {

    @Inject(
        method = "tesselateInWorld(Lnet/minecraft/world/level/tile/Tile;III)Z",
        at = @At("HEAD")
    )
    private void onRenderBlockByRenderType(Tile blockBase, int x, int y, int z, CallbackInfoReturnable<Boolean> cir) {
        if (Shaders.shaderPackLoaded) {
            if (Shaders.entityAttrib >= 0)
                ((TessellatorAccessor) Tesselator.instance).setEntity(blockBase.id);
        }
    }
}
