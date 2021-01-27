package com.android.server.hidata.wavemapping.util;

import java.util.ArrayList;
import java.util.List;

public class GetStd {
    private static final int DEFAULT_CAPACITY = 10;
    private static final int DEFAULT_VALUE = -100;

    public float getAverage(List<Float> array) {
        int len;
        float sum = 0.0f;
        if (array == null || (len = array.size()) == 0) {
            return 0.0f;
        }
        for (Float num : array) {
            sum += num.floatValue();
        }
        return sum / ((float) len);
    }

    public float getStandardDevition(List<Float> arr) {
        if (arr == null || arr.size() == 0) {
            return 0.0f;
        }
        double sum = 0.0d;
        try {
            List<Float> results = new ArrayList<>(10);
            for (Float ar : arr) {
                if (ar.floatValue() != -100.0f) {
                    results.add(ar);
                }
            }
            int size = results.size();
            if (size != 1) {
                if (size != 0) {
                    float avg = getAverage(results);
                    for (Float result : results) {
                        sum += (((double) result.floatValue()) - ((double) avg)) * (((double) result.floatValue()) - ((double) avg));
                    }
                    return (float) Math.sqrt(sum / ((double) (size - 1)));
                }
            }
            return 0.0f;
        } catch (ArithmeticException e) {
            LogUtil.e(false, "getStandardDeviation failed by Exception", new Object[0]);
            return 0.0f;
        }
    }
}
