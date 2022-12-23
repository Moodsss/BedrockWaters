package retr0.bedrockwaters.compat.sodium;

import me.jellysquid.mods.sodium.client.world.WorldSlice;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import retr0.bedrockwaters.util.WaterPropertiesManager;

public class WaterOpacityCache {
    private static final int BORDER = 1;

    private final RegistryEntry<Biome>[][] biomes;
    private final float[][] opaques;

    private final int sizeHorizontal;
    private final int sizeVertical;

    private final int blurHorizontal;

    private final int baseX, baseY, baseZ;
    private final WorldSlice slice;

    public WaterOpacityCache(WorldSlice slice, int radius) {
        this.slice = slice;

        int borderXZ = radius + BORDER;
        int borderY = BORDER;

        this.sizeHorizontal = 16 + (borderXZ * 2);
        this.sizeVertical = 16 + (borderY * 2);

        this.blurHorizontal = radius;

        ChunkSectionPos pos = slice.getOrigin();

        this.baseX = pos.getMinX() - borderXZ;
        this.baseY = pos.getMinY() - borderY;
        this.baseZ = pos.getMinZ() - borderXZ;

        this.opaques = new float[this.sizeVertical][];
        this.biomes = new RegistryEntry[this.sizeVertical][];
    }

    public float getOpacity(int posX, int posY, int posZ) {
        var x = MathHelper.clamp(posX - this.baseX, 0, this.sizeHorizontal);
        var y = MathHelper.clamp(posY - this.baseY, 0, this.sizeVertical);
        var z = MathHelper.clamp(posZ - this.baseZ, 0, this.sizeHorizontal);

        var layer = this.opaques[y];

        if (layer == null) {
            this.opaques[y] = (layer = this.gatherOpaquesXZ(y));
        }

        return layer[this.indexXZ(x, z)];
    }

    private RegistryEntry<Biome>[] gatherBiomes(int level) {
        var biomeAccess = this.slice.getBiomeAccess();
        var biomeData = new RegistryEntry[this.sizeHorizontal * this.sizeHorizontal];

        var pos = new BlockPos.Mutable();

        for (int x = 0; x < this.sizeHorizontal; x++) {
            for (int z = 0; z < this.sizeHorizontal; z++) {
                biomeData[this.indexXZ(x, z)] = biomeAccess.getBiome(pos.set(x + this.baseX, level + this.baseY, z + this.baseZ));
            }
        }

        return biomeData;
    }

    private float[] gatherOpaquesXZ(int y) {
        RegistryEntry<Biome>[] biomeData = this.getBiomeData(y);
        var opacityData = new float[this.sizeHorizontal * this.sizeHorizontal];

        for (int x = 0; x < this.sizeHorizontal; x++) {
            for (int z = 0; z < this.sizeHorizontal; z++) {
                int index = this.indexXZ(x, z);

                opacityData[index] = WaterPropertiesManager.getWaterProperties(biomeData[index]).waterOpacity();
            }
        }

        OpaqueBoxBlur.blur(opacityData, this.sizeHorizontal, this.sizeHorizontal, this.blurHorizontal);

        return opacityData;
    }

    private RegistryEntry<Biome>[] getBiomeData(int y) {
        RegistryEntry<Biome>[] biomes = this.biomes[y];

        if (biomes == null) {
            this.biomes[y] = (biomes = this.gatherBiomes(y));
        }

        return biomes;
    }

    private int indexXZ(int x, int z) {
        return (x * this.sizeHorizontal) + z;
    }

}
