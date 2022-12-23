package retr0.bedrockwaters.compat.sodium.mixin;

import me.jellysquid.mods.sodium.client.world.WorldSlice;
import me.jellysquid.mods.sodium.client.world.cloned.ChunkRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import retr0.bedrockwaters.compat.sodium.WaterOpacityCache;
import retr0.bedrockwaters.extension.ExtensionClientWorld;

@Mixin(value = WorldSlice.class, remap = false)
public class MixinWorldSlice implements ExtensionClientWorld {

    private WaterOpacityCache waterOpacityCache;

    @Inject(method = "copyData", at = @At("RETURN"))
    private void onCopyData(ChunkRenderContext context, CallbackInfo ci) {
        this.waterOpacityCache = new WaterOpacityCache((WorldSlice) (Object) this, MinecraftClient.getInstance().options.getBiomeBlendRadius().getValue());
    }

    @Override
    public float getOpacity(BlockPos pos) {
        return this.waterOpacityCache.getOpacity(pos.getX(), pos.getY(), pos.getZ());
    }
}
