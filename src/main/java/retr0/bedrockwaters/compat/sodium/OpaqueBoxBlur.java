package retr0.bedrockwaters.compat.sodium;

import org.apache.commons.lang3.Validate;

public class OpaqueBoxBlur {
    /**
     * Performs a box blur with the specified radius on the 2D array of alpha values.
     *
     * @param data The array of alpha values to blur in-place
     * @param width The width of the array
     * @param height The height of the array
     * @param radius The radius to blur with
     */
    public static void blur(float[] data, int width, int height, int radius) {
        Validate.isTrue(data.length == (width * height), "data.length != (width * height)");

        // TODO: Re-use allocations between invocations
        var tmp = new float[width * height];
        blur(data, tmp, width, height, radius); // X-axis
        blur(tmp, data, width, height, radius); // Y-axis
    }

    private static void blur(float[] src, float[] dst, final int width, final int height, int radius) {
        final int windowSize = (radius * 2) + 1;
        final int edgeExtendAmount = radius + 1;

        int srcIndex = 0;

        // TODO: SIMD
        for (int y = 0; y < height; y++) {
            int dstIndex = y;
            float alpha = src[srcIndex];

            // Extend the window backwards by repeating the alphas at the edge N times
            alpha *= edgeExtendAmount;

            // Extend the window forwards by sampling ahead N times
            for (int i = 1; i <= radius; i++) {
                float neighborAlpha = src[srcIndex + i];

                alpha += neighborAlpha;
            }

            for (int x = 0; x < width; x++) {
                // TODO: Avoid division
                dst[dstIndex] = alpha / windowSize;

                int previousPixelIndex = Math.max(x - radius, 0);
                float previousAlpha = src[srcIndex + previousPixelIndex];

                // Remove the alpha values that are behind the window
                alpha -= previousAlpha;

                int nextPixelIndex = Math.min(x + edgeExtendAmount, width - 1);
                float nextAlpha = src[srcIndex + nextPixelIndex];

                // Add the alpha values that are ahead of the window
                alpha += nextAlpha;

                // Move the window forward
                dstIndex += width;
            }

            srcIndex += width;
        }
    }


}
