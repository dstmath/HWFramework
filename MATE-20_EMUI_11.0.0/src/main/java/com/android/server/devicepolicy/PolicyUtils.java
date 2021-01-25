package com.android.server.devicepolicy;

public class PolicyUtils {
    private static final String TAG = "PolicyUtils";

    public static int getIndexFromArray(int value, int[] array) {
        for (int index = 0; index < array.length; index++) {
            if (array[index] == value) {
                return index;
            }
        }
        return -1;
    }
}
