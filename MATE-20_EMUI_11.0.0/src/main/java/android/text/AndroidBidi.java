package android.text;

import android.annotation.UnsupportedAppUsage;
import android.icu.lang.UCharacter;
import android.icu.text.Bidi;
import android.icu.text.BidiClassifier;
import android.text.Layout;
import com.android.internal.annotations.VisibleForTesting;
import java.util.Arrays;

@VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
public class AndroidBidi {
    private static final EmojiBidiOverride sEmojiBidiOverride = new EmojiBidiOverride();

    public static class EmojiBidiOverride extends BidiClassifier {
        private static final int NO_OVERRIDE = (UCharacter.getIntPropertyMaxValue(4096) + 1);

        public EmojiBidiOverride() {
            super(null);
        }

        @Override // android.icu.text.BidiClassifier
        public int classify(int c) {
            if (Emoji.isNewEmoji(c)) {
                return 10;
            }
            return NO_OVERRIDE;
        }
    }

    @UnsupportedAppUsage
    public static int bidi(int dir, char[] chs, byte[] chInfo) {
        byte paraLevel;
        if (chs == null || chInfo == null) {
            throw new NullPointerException();
        }
        int length = chs.length;
        if (chInfo.length >= length) {
            if (dir == -2) {
                paraLevel = Byte.MAX_VALUE;
            } else if (dir == -1) {
                paraLevel = 1;
            } else if (dir == 1) {
                paraLevel = 0;
            } else if (dir != 2) {
                paraLevel = 0;
            } else {
                paraLevel = 126;
            }
            Bidi icuBidi = new Bidi(length, 0);
            icuBidi.setCustomClassifier(sEmojiBidiOverride);
            icuBidi.setPara(chs, paraLevel, (byte[]) null);
            for (int i = 0; i < length; i++) {
                chInfo[i] = icuBidi.getLevelAt(i);
            }
            return (icuBidi.getParaLevel() & 1) == 0 ? 1 : -1;
        }
        throw new IndexOutOfBoundsException();
    }

    public static Layout.Directions directions(int dir, byte[] levels, int lstart, char[] chars, int cstart, int len) {
        boolean swap;
        if (len == 0) {
            return Layout.DIRS_ALL_LEFT_TO_RIGHT;
        }
        int baseLevel = dir == 1 ? 0 : 1;
        byte b = levels[lstart];
        int minLevel = b;
        int runCount = 1;
        int e = lstart + len;
        for (int i = lstart + 1; i < e; i++) {
            byte b2 = levels[i];
            if (b2 != b) {
                b = b2;
                runCount++;
            }
        }
        int visLen = len;
        if ((b & 1) != (baseLevel & 1)) {
            while (true) {
                visLen--;
                if (visLen < 0) {
                    break;
                }
                char ch = chars[cstart + visLen];
                if (ch == '\n') {
                    visLen--;
                    break;
                }
                if (!(ch == ' ' || ch == '\t')) {
                    break;
                }
            }
            visLen++;
            if (visLen != len) {
                runCount++;
            }
        }
        if (runCount != 1 || minLevel != baseLevel) {
            int[] ld = new int[(runCount * 2)];
            int maxLevel = minLevel;
            int levelBits = minLevel << 26;
            int n = 1;
            int prev = lstart;
            int curLevel = minLevel;
            int e2 = lstart + visLen;
            for (int i2 = lstart; i2 < e2; i2++) {
                byte b3 = levels[i2];
                if (b3 != curLevel) {
                    curLevel = b3;
                    if (b3 > maxLevel) {
                        maxLevel = b3;
                    } else if (b3 < minLevel) {
                        minLevel = b3;
                    }
                    int n2 = n + 1;
                    ld[n] = (i2 - prev) | levelBits;
                    n = n2 + 1;
                    ld[n2] = i2 - lstart;
                    levelBits = curLevel << 26;
                    prev = i2;
                }
            }
            ld[n] = ((lstart + visLen) - prev) | levelBits;
            if (visLen < len) {
                int n3 = n + 1;
                ld[n3] = visLen;
                n = n3 + 1;
                ld[n] = (len - visLen) | (baseLevel << 26);
            }
            int n4 = n + 1;
            if (n4 < ld.length) {
                ld = Arrays.copyOf(ld, n4);
            }
            if ((minLevel & 1) == baseLevel) {
                minLevel++;
                swap = maxLevel > minLevel;
            } else {
                swap = runCount > 1;
            }
            if (swap) {
                int level = maxLevel - 1;
                while (level >= minLevel) {
                    int i3 = 0;
                    while (i3 < ld.length) {
                        if (levels[ld[i3]] >= level) {
                            int e3 = i3 + 2;
                            while (e3 < ld.length && levels[ld[e3]] >= level) {
                                e3 += 2;
                            }
                            int low = i3;
                            for (int hi = e3 - 2; low < hi; hi -= 2) {
                                int x = ld[low];
                                ld[low] = ld[hi];
                                ld[hi] = x;
                                int x2 = ld[low + 1];
                                ld[low + 1] = ld[hi + 1];
                                ld[hi + 1] = x2;
                                low += 2;
                            }
                            i3 = e3 + 2;
                        }
                        i3 += 2;
                    }
                    level--;
                }
            }
            return new Layout.Directions(ld);
        } else if ((minLevel & 1) != 0) {
            return Layout.DIRS_ALL_RIGHT_TO_LEFT;
        } else {
            return Layout.DIRS_ALL_LEFT_TO_RIGHT;
        }
    }
}
