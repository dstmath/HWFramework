package ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex;

public class CaseInsensitiveMap {
    private static int CHUNK_MASK = (CHUNK_SIZE - 1);
    private static int CHUNK_SHIFT = 10;
    private static int CHUNK_SIZE = (1 << CHUNK_SHIFT);
    private static int INITIAL_CHUNK_COUNT = 64;
    private static int LOWER_CASE_MATCH = 1;
    private static int UPPER_CASE_MATCH = 2;
    private static int[][][] caseInsensitiveMap;
    private static Boolean mapBuilt = Boolean.FALSE;

    public static int[] get(int i) {
        if (mapBuilt == Boolean.FALSE) {
            synchronized (mapBuilt) {
                if (mapBuilt == Boolean.FALSE) {
                    buildCaseInsensitiveMap();
                }
            }
        }
        if (i < 65536) {
            return getMapping(i);
        }
        return null;
    }

    private static int[] getMapping(int i) {
        int i2 = i >>> CHUNK_SHIFT;
        return caseInsensitiveMap[i2][i & CHUNK_MASK];
    }

    private static void buildCaseInsensitiveMap() {
        int i;
        caseInsensitiveMap = new int[INITIAL_CHUNK_COUNT][][];
        for (int i2 = 0; i2 < INITIAL_CHUNK_COUNT; i2++) {
            caseInsensitiveMap[i2] = new int[CHUNK_SIZE][];
        }
        for (int i3 = 0; i3 < 65536; i3++) {
            int lowerCase = Character.toLowerCase(i3);
            int upperCase = Character.toUpperCase(i3);
            if (lowerCase != upperCase || lowerCase != i3) {
                int[] iArr = new int[2];
                if (lowerCase != i3) {
                    iArr[0] = lowerCase;
                    iArr[1] = LOWER_CASE_MATCH;
                    int[] mapping = getMapping(lowerCase);
                    if (mapping != null) {
                        iArr = updateMap(i3, iArr, lowerCase, mapping, LOWER_CASE_MATCH);
                    }
                    i = 2;
                } else {
                    i = 0;
                }
                if (upperCase != i3) {
                    if (i == iArr.length) {
                        iArr = expandMap(iArr, 2);
                    }
                    iArr[i] = upperCase;
                    iArr[i + 1] = UPPER_CASE_MATCH;
                    int[] mapping2 = getMapping(upperCase);
                    if (mapping2 != null) {
                        iArr = updateMap(i3, iArr, upperCase, mapping2, UPPER_CASE_MATCH);
                    }
                }
                set(i3, iArr);
            }
        }
        mapBuilt = Boolean.TRUE;
    }

    private static int[] expandMap(int[] iArr, int i) {
        int length = iArr.length;
        int[] iArr2 = new int[(i + length)];
        System.arraycopy(iArr, 0, iArr2, 0, length);
        return iArr2;
    }

    private static void set(int i, int[] iArr) {
        int i2 = i >>> CHUNK_SHIFT;
        caseInsensitiveMap[i2][i & CHUNK_MASK] = iArr;
    }

    private static int[] updateMap(int i, int[] iArr, int i2, int[] iArr2, int i3) {
        for (int i4 = 0; i4 < iArr2.length; i4 += 2) {
            int i5 = iArr2[i4];
            int[] mapping = getMapping(i5);
            if (mapping != null && contains(mapping, i2, i3)) {
                if (!contains(mapping, i)) {
                    set(i5, expandAndAdd(mapping, i, i3));
                }
                if (!contains(iArr, i5)) {
                    iArr = expandAndAdd(iArr, i5, i3);
                }
            }
        }
        if (!contains(iArr2, i)) {
            set(i2, expandAndAdd(iArr2, i, i3));
        }
        return iArr;
    }

    private static boolean contains(int[] iArr, int i) {
        for (int i2 = 0; i2 < iArr.length; i2 += 2) {
            if (iArr[i2] == i) {
                return true;
            }
        }
        return false;
    }

    private static boolean contains(int[] iArr, int i, int i2) {
        for (int i3 = 0; i3 < iArr.length; i3 += 2) {
            if (iArr[i3] == i && iArr[i3 + 1] == i2) {
                return true;
            }
        }
        return false;
    }

    private static int[] expandAndAdd(int[] iArr, int i, int i2) {
        int length = iArr.length;
        int[] iArr2 = new int[(length + 2)];
        System.arraycopy(iArr, 0, iArr2, 0, length);
        iArr2[length] = i;
        iArr2[length + 1] = i2;
        return iArr2;
    }
}
