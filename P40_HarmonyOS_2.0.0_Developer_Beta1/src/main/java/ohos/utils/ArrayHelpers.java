package ohos.utils;

import java.util.Arrays;
import java.util.Objects;

public class ArrayHelpers {
    public static int binarySearchIntArrayMatchObject(int[] iArr, Object[] objArr, int i, int i2, Object obj) {
        int binarySearch = Arrays.binarySearch(iArr, 0, i, i2);
        if (binarySearch < 0) {
            return binarySearch;
        }
        if (binarySearch < i && Objects.equals(objArr[binarySearch], obj)) {
            return binarySearch;
        }
        int i3 = binarySearch;
        while (i3 < i && iArr[i3] == i2) {
            if (Objects.equals(objArr[i3], obj)) {
                return i3;
            }
            i3++;
        }
        int i4 = binarySearch - 1;
        while (i4 >= 0 && iArr[i4] == i2) {
            if (Objects.equals(objArr[i4], obj)) {
                return i4;
            }
            i4--;
        }
        return ~i3;
    }
}
