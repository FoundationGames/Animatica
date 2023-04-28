package io.github.foundationgames.animatica.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.foundationgames.animatica.Animatica;
import io.github.foundationgames.animatica.animation.AnimationLoader;
import io.github.foundationgames.animatica.animation.BakedTextureAnimation;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(RenderSystem.class)
public class RenderSystemMixin {
    @ModifyVariable(method = "_setShaderTexture(ILnet/minecraft/util/Identifier;)V", at = @At("HEAD"), index = 1)
    private static Identifier animatica$replaceWithAnimatedTexture(Identifier old) {
        if (Animatica.CONFIG.animatedTextures) {
            var anim = AnimationLoader.INSTANCE.getAnimation(old);
            if (anim != null) {
                return anim.getTextureForFrame();
            }
        }
        return old;
    }
}
