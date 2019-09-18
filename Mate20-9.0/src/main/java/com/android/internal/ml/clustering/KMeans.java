package com.android.internal.ml.clustering;

import com.android.internal.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class KMeans {
    private static final boolean DEBUG = false;
    private static final String TAG = "KMeans";
    private final int mMaxIterations;
    private final Random mRandomState;
    private float mSqConvergenceEpsilon;

    public static class Mean {
        float[] mCentroid;
        final ArrayList<float[]> mClosestItems = new ArrayList<>();

        public Mean(int dimension) {
            this.mCentroid = new float[dimension];
        }

        public Mean(float... centroid) {
            this.mCentroid = centroid;
        }

        public float[] getCentroid() {
            return this.mCentroid;
        }

        public List<float[]> getItems() {
            return this.mClosestItems;
        }

        public String toString() {
            return "Mean(centroid: " + Arrays.toString(this.mCentroid) + ", size: " + this.mClosestItems.size() + ")";
        }
    }

    public KMeans() {
        this(new Random());
    }

    public KMeans(Random random) {
        this(random, 30, 0.005f);
    }

    public KMeans(Random random, int maxIterations, float convergenceEpsilon) {
        this.mRandomState = random;
        this.mMaxIterations = maxIterations;
        this.mSqConvergenceEpsilon = convergenceEpsilon * convergenceEpsilon;
    }

    public List<Mean> predict(int k, float[][] inputData) {
        checkDataSetSanity(inputData);
        int dimension = inputData[0].length;
        ArrayList<Mean> means = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            Mean m = new Mean(dimension);
            for (int j = 0; j < dimension; j++) {
                m.mCentroid[j] = this.mRandomState.nextFloat();
            }
            means.add(m);
        }
        for (int i2 = 0; i2 < this.mMaxIterations && !step(means, inputData); i2++) {
        }
        return means;
    }

    public static double score(List<Mean> means) {
        int meansSize = means.size();
        double score = 0.0d;
        int i = 0;
        while (i < meansSize) {
            Mean mean = means.get(i);
            double score2 = score;
            for (int j = 0; j < meansSize; j++) {
                Mean compareTo = means.get(j);
                if (mean != compareTo) {
                    score2 += Math.sqrt((double) sqDistance(mean.mCentroid, compareTo.mCentroid));
                }
            }
            i++;
            score = score2;
        }
        return score;
    }

    @VisibleForTesting
    public void checkDataSetSanity(float[][] inputData) {
        if (inputData == null) {
            throw new IllegalArgumentException("Data set is null.");
        } else if (inputData.length == 0) {
            throw new IllegalArgumentException("Data set is empty.");
        } else if (inputData[0] != null) {
            int dimension = inputData[0].length;
            int length = inputData.length;
            for (int i = 1; i < length; i++) {
                if (inputData[i] == null || inputData[i].length != dimension) {
                    throw new IllegalArgumentException("Bad data set format.");
                }
            }
        } else {
            throw new IllegalArgumentException("Bad data set format.");
        }
    }

    private boolean step(ArrayList<Mean> means, float[][] inputData) {
        for (int i = means.size() - 1; i >= 0; i--) {
            means.get(i).mClosestItems.clear();
        }
        for (int i2 = inputData.length - 1; i2 >= 0; i2--) {
            float[] current = inputData[i2];
            nearestMean(current, means).mClosestItems.add(current);
        }
        boolean converged = true;
        for (int i3 = means.size() - 1; i3 >= 0; i3--) {
            Mean mean = means.get(i3);
            if (mean.mClosestItems.size() != 0) {
                float[] oldCentroid = mean.mCentroid;
                mean.mCentroid = new float[oldCentroid.length];
                for (int j = 0; j < mean.mClosestItems.size(); j++) {
                    for (int p = 0; p < mean.mCentroid.length; p++) {
                        float[] fArr = mean.mCentroid;
                        fArr[p] = fArr[p] + mean.mClosestItems.get(j)[p];
                    }
                }
                for (int j2 = 0; j2 < mean.mCentroid.length; j2++) {
                    float[] fArr2 = mean.mCentroid;
                    fArr2[j2] = fArr2[j2] / ((float) mean.mClosestItems.size());
                }
                if (sqDistance(oldCentroid, mean.mCentroid) > this.mSqConvergenceEpsilon) {
                    converged = false;
                }
            }
        }
        return converged;
    }

    @VisibleForTesting
    public static Mean nearestMean(float[] point, List<Mean> means) {
        Mean nearest = null;
        float nearestDistance = Float.MAX_VALUE;
        int meanCount = means.size();
        for (int i = 0; i < meanCount; i++) {
            Mean next = means.get(i);
            float nextDistance = sqDistance(point, next.mCentroid);
            if (nextDistance < nearestDistance) {
                nearest = next;
                nearestDistance = nextDistance;
            }
        }
        return nearest;
    }

    @VisibleForTesting
    public static float sqDistance(float[] a, float[] b) {
        float dist = 0.0f;
        int length = a.length;
        for (int i = 0; i < length; i++) {
            dist += (a[i] - b[i]) * (a[i] - b[i]);
        }
        return dist;
    }
}
