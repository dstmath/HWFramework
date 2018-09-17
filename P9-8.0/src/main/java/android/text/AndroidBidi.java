package android.text;

import android.text.Layout.Directions;

public class AndroidBidi {
    private static native int runBidi(int i, char[] cArr, byte[] bArr, int i2, boolean z);

    public static int bidi(int dir, char[] chs, byte[] chInfo, int n, boolean haveInfo) {
        if (chs == null || chInfo == null) {
            throw new NullPointerException();
        } else if (n < 0 || chs.length < n || chInfo.length < n) {
            throw new IndexOutOfBoundsException();
        } else {
            switch (dir) {
                case -2:
                    dir = -1;
                    break;
                case -1:
                    dir = 1;
                    break;
                case 1:
                    dir = 0;
                    break;
                case 2:
                    dir = -2;
                    break;
                default:
                    dir = 0;
                    break;
            }
            return (runBidi(dir, chs, chInfo, n, haveInfo) & 1) == 0 ? 1 : -1;
        }
    }

    public static Directions directions(int dir, byte[] levels, int lstart, char[] chars, int cstart, int len) {
        if (len == 0) {
            return Layout.DIRS_ALL_LEFT_TO_RIGHT;
        }
        int i;
        byte baseLevel = dir == 1 ? (byte) 0 : (byte) 1;
        int curLevel = levels[lstart];
        byte minLevel = curLevel;
        int runCount = 1;
        int e = lstart + len;
        for (i = lstart + 1; i < e; i++) {
            int level = levels[i];
            if (level != curLevel) {
                curLevel = level;
                runCount++;
            }
        }
        int visLen = len;
        if ((curLevel & 1) != (baseLevel & 1)) {
            while (true) {
                visLen--;
                if (visLen < 0) {
                    break;
                }
                char ch = chars[cstart + visLen];
                if (ch != 10) {
                    if (ch != ' ' && ch != 9) {
                        break;
                    }
                } else {
                    visLen--;
                    break;
                }
            }
            visLen++;
            if (visLen != len) {
                runCount++;
            }
        }
        if (runCount != 1 || minLevel != baseLevel) {
            byte level2;
            int i2;
            boolean swap;
            int[] ld = new int[(runCount * 2)];
            byte maxLevel = minLevel;
            int levelBits = minLevel << 26;
            int prev = lstart;
            curLevel = minLevel;
            i = lstart;
            e = lstart + visLen;
            int n = 1;
            while (i < e) {
                level2 = levels[i];
                if (level2 != curLevel) {
                    curLevel = level2;
                    if (level2 > maxLevel) {
                        maxLevel = level2;
                    } else if (level2 < minLevel) {
                        minLevel = level2;
                    }
                    i2 = n + 1;
                    ld[n] = (i - prev) | levelBits;
                    n = i2 + 1;
                    ld[i2] = i - lstart;
                    levelBits = level2 << 26;
                    prev = i;
                }
                i++;
                n = n;
            }
            ld[n] = ((lstart + visLen) - prev) | levelBits;
            if (visLen < len) {
                i2 = n + 1;
                ld[i2] = visLen;
                ld[i2 + 1] = (len - visLen) | (baseLevel << 26);
            }
            if ((minLevel & 1) == baseLevel) {
                minLevel++;
                swap = maxLevel > minLevel;
            } else {
                swap = runCount > 1;
            }
            if (swap) {
                level2 = maxLevel - 1;
                while (level2 >= minLevel) {
                    i = 0;
                    while (i < ld.length) {
                        if (levels[ld[i]] >= level2) {
                            e = i + 2;
                            while (e < ld.length && levels[ld[e]] >= level2) {
                                e += 2;
                            }
                            int low = i;
                            for (int hi = e - 2; low < hi; hi -= 2) {
                                int x = ld[low];
                                ld[low] = ld[hi];
                                ld[hi] = x;
                                x = ld[low + 1];
                                ld[low + 1] = ld[hi + 1];
                                ld[hi + 1] = x;
                                low += 2;
                            }
                            i = e + 2;
                        }
                        i += 2;
                    }
                    level2--;
                }
            }
            return new Directions(ld);
        } else if ((minLevel & 1) != 0) {
            return Layout.DIRS_ALL_RIGHT_TO_LEFT;
        } else {
            return Layout.DIRS_ALL_LEFT_TO_RIGHT;
        }
    }
}
