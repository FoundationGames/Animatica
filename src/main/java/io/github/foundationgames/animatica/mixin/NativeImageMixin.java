package io.github.foundationgames.animatica.mixin;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.foundationgames.animatica.accessor.NativeImageAccessor;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(NativeImage.class)
public class NativeImageMixin implements NativeImageAccessor {

    @Override
    public long animatica$getPixels() {
        NativeImage $this = (NativeImage) (Object) this;
        return $this.getPointer();
    }
}
