package com.android.server.hidata.wavemapping.util;

import com.android.server.gesture.GestureNavConst;
import java.util.ArrayList;
import java.util.List;

public class GetStd {
    public float getAverage(List<Float> array) {
        float sum = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        if (array == null) {
            return GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        }
        int num = array.size();
        if (num == 0) {
            return GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        }
        int i = 0;
        while (i < num) {
            try {
                sum += array.get(i).floatValue();
                i++;
            } catch (Exception e) {
                LogUtil.e("LocatingState,e" + e.getMessage());
            }
        }
        return sum / ((float) num);
    }

    public float getStandardDevition(List<Float> arr) {
        float ret = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        if (arr == null || arr.size() == 0) {
            return GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        }
        double sum = 0.0d;
        try {
            List<Float> result = new ArrayList<>();
            for (Float ar : arr) {
                if (ar.floatValue() != -100.0f) {
                    result.add(ar);
                }
            }
            int size = result.size();
            if (size != 1) {
                if (size != 0) {
                    float avg = getAverage(result);
                    for (int i = 0; i < size; i++) {
                        sum += (((double) result.get(i).floatValue()) - ((double) avg)) * (((double) result.get(i).floatValue()) - ((double) avg));
                    }
                    ret = (float) Math.sqrt(sum / ((double) (size - 1)));
                    return ret;
                }
            }
            return GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        } catch (Exception e) {
            LogUtil.e("LocatingState,e" + e.getMessage());
        }
    }
}
