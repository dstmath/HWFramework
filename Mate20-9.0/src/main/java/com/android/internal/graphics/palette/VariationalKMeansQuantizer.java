package com.android.internal.graphics.palette;

import com.android.internal.graphics.ColorUtils;
import com.android.internal.graphics.palette.Palette;
import com.android.internal.ml.clustering.KMeans;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VariationalKMeansQuantizer implements Quantizer {
    private static final boolean DEBUG = false;
    private static final String TAG = "KMeansQuantizer";
    private final int mInitializations;
    private final KMeans mKMeans;
    private final float mMinClusterSqDistance;
    private List<Palette.Swatch> mQuantizedColors;

    public VariationalKMeansQuantizer() {
        this(0.25f);
    }

    public VariationalKMeansQuantizer(float minClusterDistance) {
        this(minClusterDistance, 1);
    }

    public VariationalKMeansQuantizer(float minClusterDistance, int initializations) {
        this.mKMeans = new KMeans(new Random(0), 30, 0.0f);
        this.mMinClusterSqDistance = minClusterDistance * minClusterDistance;
        this.mInitializations = initializations;
    }

    public void quantize(int[] pixels, int maxColors, Palette.Filter[] filters) {
        int i;
        float[] hsl;
        int[] iArr = pixels;
        float[] hsl2 = {0.0f, 0.0f, 0.0f};
        float[][] hslPixels = (float[][]) Array.newInstance(float.class, new int[]{iArr.length, 3});
        for (int i2 = 0; i2 < iArr.length; i2++) {
            ColorUtils.colorToHSL(iArr[i2], hsl2);
            hslPixels[i2][0] = hsl2[0] / 360.0f;
            hslPixels[i2][1] = hsl2[1];
            hslPixels[i2][2] = hsl2[2];
        }
        List<KMeans.Mean> optimalMeans = getOptimalKMeans(maxColors, hslPixels);
        int i3 = 0;
        while (i3 < optimalMeans.size()) {
            KMeans.Mean current = optimalMeans.get(i3);
            float[] currentCentroid = current.getCentroid();
            int j = i3 + 1;
            while (j < optimalMeans.size()) {
                KMeans.Mean compareTo = optimalMeans.get(j);
                float[] compareToCentroid = compareTo.getCentroid();
                if (KMeans.sqDistance(currentCentroid, compareToCentroid) < this.mMinClusterSqDistance) {
                    optimalMeans.remove(compareTo);
                    current.getItems().addAll(compareTo.getItems());
                    int k = 0;
                    while (k < currentCentroid.length) {
                        currentCentroid[k] = (float) (((double) currentCentroid[k]) + (((double) (compareToCentroid[k] - currentCentroid[k])) / 2.0d));
                        k++;
                        hsl2 = hsl2;
                        i3 = i3;
                    }
                    hsl = hsl2;
                    i = i3;
                    j--;
                } else {
                    hsl = hsl2;
                    i = i3;
                }
                j++;
                hsl2 = hsl;
                i3 = i;
            }
            i3++;
        }
        this.mQuantizedColors = new ArrayList();
        for (KMeans.Mean mean : optimalMeans) {
            if (mean.getItems().size() != 0) {
                float[] centroid = mean.getCentroid();
                this.mQuantizedColors.add(new Palette.Swatch(new float[]{centroid[0] * 360.0f, centroid[1], centroid[2]}, mean.getItems().size()));
            }
        }
    }

    private List<KMeans.Mean> getOptimalKMeans(int k, float[][] inputData) {
        List<KMeans.Mean> optimal = null;
        double optimalScore = -1.7976931348623157E308d;
        for (int runs = this.mInitializations; runs > 0; runs--) {
            List<KMeans.Mean> means = this.mKMeans.predict(k, inputData);
            double score = KMeans.score(means);
            if (optimal == null || score > optimalScore) {
                optimalScore = score;
                optimal = means;
            }
        }
        return optimal;
    }

    public List<Palette.Swatch> getQuantizedColors() {
        return this.mQuantizedColors;
    }
}
