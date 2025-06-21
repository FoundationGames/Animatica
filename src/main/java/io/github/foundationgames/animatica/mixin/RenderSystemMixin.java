package io.github.foundationgames.animatica.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.foundationgames.animatica.Animatica;
import io.github.foundationgames.animatica.animation.AnimationLoader;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(RenderSystem.class)
public class RenderSystemMixin {
    @ModifyVariable(method = "setShaderTexture(II)V", at = @At("HEAD"), index = 1)
    private static int animatica$replaceWithAnimatedTexture(int old) {
        if (Animatica.CONFIG.animatedTextures && AnimationLoader.INSTANCE.isAnimated(old)) {
            return AnimationLoader.INSTANCE.getAnimationId(old);
        }
        return old;
    }
}
