package android.icu.impl.coll;

import android.icu.util.ByteArrayWrapper;

public class BOCSU {
    private static final int SLOPE_LEAD_2_ = 42;
    private static final int SLOPE_LEAD_3_ = 3;
    private static final int SLOPE_MAX_ = 255;
    private static final int SLOPE_MAX_BYTES_ = 4;
    private static final int SLOPE_MIDDLE_ = 129;
    private static final int SLOPE_MIN_ = 3;
    private static final int SLOPE_REACH_NEG_1_ = -80;
    private static final int SLOPE_REACH_NEG_2_ = -10668;
    private static final int SLOPE_REACH_NEG_3_ = -192786;
    private static final int SLOPE_REACH_POS_1_ = 80;
    private static final int SLOPE_REACH_POS_2_ = 10667;
    private static final int SLOPE_REACH_POS_3_ = 192785;
    private static final int SLOPE_SINGLE_ = 80;
    private static final int SLOPE_START_NEG_2_ = 49;
    private static final int SLOPE_START_NEG_3_ = 7;
    private static final int SLOPE_START_POS_2_ = 210;
    private static final int SLOPE_START_POS_3_ = 252;
    private static final int SLOPE_TAIL_COUNT_ = 253;

    public static int writeIdenticalLevelRun(int prev, CharSequence s, int i, int length, ByteArrayWrapper sink) {
        while (i < length) {
            ensureAppendCapacity(sink, 16, s.length() * 2);
            byte[] buffer = sink.bytes;
            int lastSafe = buffer.length - 4;
            int p = sink.size;
            while (i < length && p <= lastSafe) {
                int p2;
                if (prev < 19968 || prev >= 40960) {
                    prev = (prev & -128) + SLOPE_SINGLE_;
                } else {
                    prev = 30292;
                }
                int c = Character.codePointAt(s, i);
                i += Character.charCount(c);
                if (c == 65534) {
                    p2 = p + 1;
                    buffer[p] = (byte) 2;
                    prev = 0;
                } else {
                    p2 = writeDiff(c - prev, buffer, p);
                    prev = c;
                }
                p = p2;
            }
            sink.size = p;
        }
        return prev;
    }

    private static void ensureAppendCapacity(ByteArrayWrapper sink, int minCapacity, int desiredCapacity) {
        if (sink.bytes.length - sink.size < minCapacity) {
            if (desiredCapacity < minCapacity) {
                desiredCapacity = minCapacity;
            }
            sink.ensureCapacity(sink.size + desiredCapacity);
        }
    }

    private BOCSU() {
    }

    private static final long getNegDivMod(int number, int factor) {
        int modulo = number % factor;
        long result = (long) (number / factor);
        if (modulo < 0) {
            result--;
            modulo += factor;
        }
        return (result << 32) | ((long) modulo);
    }

    private static final int writeDiff(int diff, byte[] buffer, int offset) {
        int i;
        if (diff < SLOPE_REACH_NEG_1_) {
            long division = getNegDivMod(diff, SLOPE_TAIL_COUNT_);
            int modulo = (int) division;
            if (diff >= SLOPE_REACH_NEG_2_) {
                i = offset + 1;
                buffer[offset] = (byte) (((int) (division >> 32)) + SLOPE_START_NEG_2_);
                offset = i + 1;
                buffer[i] = (byte) (modulo + SLOPE_MIN_);
                return offset;
            } else if (diff >= SLOPE_REACH_NEG_3_) {
                buffer[offset + 2] = (byte) (modulo + SLOPE_MIN_);
                division = getNegDivMod((int) (division >> 32), SLOPE_TAIL_COUNT_);
                diff = (int) (division >> 32);
                buffer[offset + 1] = (byte) (((int) division) + SLOPE_MIN_);
                buffer[offset] = (byte) (diff + SLOPE_START_NEG_3_);
                return offset + SLOPE_MIN_;
            } else {
                buffer[offset + SLOPE_MIN_] = (byte) (modulo + SLOPE_MIN_);
                division = getNegDivMod((int) (division >> 32), SLOPE_TAIL_COUNT_);
                diff = (int) (division >> 32);
                buffer[offset + 2] = (byte) (((int) division) + SLOPE_MIN_);
                buffer[offset + 1] = (byte) (((int) getNegDivMod(diff, SLOPE_TAIL_COUNT_)) + SLOPE_MIN_);
                buffer[offset] = (byte) 3;
                return offset + SLOPE_MAX_BYTES_;
            }
        } else if (diff <= SLOPE_SINGLE_) {
            i = offset + 1;
            buffer[offset] = (byte) (diff + SLOPE_MIDDLE_);
            return i;
        } else if (diff <= SLOPE_REACH_POS_2_) {
            i = offset + 1;
            buffer[offset] = (byte) ((diff / SLOPE_TAIL_COUNT_) + SLOPE_START_POS_2_);
            offset = i + 1;
            buffer[i] = (byte) ((diff % SLOPE_TAIL_COUNT_) + SLOPE_MIN_);
            return offset;
        } else if (diff <= SLOPE_REACH_POS_3_) {
            buffer[offset + 2] = (byte) ((diff % SLOPE_TAIL_COUNT_) + SLOPE_MIN_);
            diff /= SLOPE_TAIL_COUNT_;
            buffer[offset + 1] = (byte) ((diff % SLOPE_TAIL_COUNT_) + SLOPE_MIN_);
            buffer[offset] = (byte) ((diff / SLOPE_TAIL_COUNT_) + SLOPE_START_POS_3_);
            return offset + SLOPE_MIN_;
        } else {
            buffer[offset + SLOPE_MIN_] = (byte) ((diff % SLOPE_TAIL_COUNT_) + SLOPE_MIN_);
            diff /= SLOPE_TAIL_COUNT_;
            buffer[offset + 2] = (byte) ((diff % SLOPE_TAIL_COUNT_) + SLOPE_MIN_);
            buffer[offset + 1] = (byte) (((diff / SLOPE_TAIL_COUNT_) % SLOPE_TAIL_COUNT_) + SLOPE_MIN_);
            buffer[offset] = (byte) -1;
            return offset + SLOPE_MAX_BYTES_;
        }
    }
}
