package android.util;

import com.android.internal.os.HwBootFail;
import java.util.Random;

public final class MathUtils {
    private static final float DEG_TO_RAD = 0.017453292f;
    private static final float RAD_TO_DEG = 57.295784f;
    private static final Random sRandom = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.util.MathUtils.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.util.MathUtils.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.util.MathUtils.<clinit>():void");
    }

    private MathUtils() {
    }

    public static float abs(float v) {
        return v > 0.0f ? v : -v;
    }

    public static int constrain(int amount, int low, int high) {
        if (amount < low) {
            return low;
        }
        return amount > high ? high : amount;
    }

    public static long constrain(long amount, long low, long high) {
        if (amount < low) {
            return low;
        }
        return amount > high ? high : amount;
    }

    public static float constrain(float amount, float low, float high) {
        if (amount < low) {
            return low;
        }
        return amount > high ? high : amount;
    }

    public static float log(float a) {
        return (float) Math.log((double) a);
    }

    public static float exp(float a) {
        return (float) Math.exp((double) a);
    }

    public static float pow(float a, float b) {
        return (float) Math.pow((double) a, (double) b);
    }

    public static float max(float a, float b) {
        return a > b ? a : b;
    }

    public static float max(int a, int b) {
        if (a <= b) {
            a = b;
        }
        return (float) a;
    }

    public static float max(float a, float b, float c) {
        return a > b ? a > c ? a : c : b > c ? b : c;
    }

    public static float max(int a, int b, int c) {
        if (a > b) {
            if (a > c) {
                c = a;
            }
        } else if (b > c) {
            c = b;
        }
        return (float) c;
    }

    public static float min(float a, float b) {
        return a < b ? a : b;
    }

    public static float min(int a, int b) {
        if (a >= b) {
            a = b;
        }
        return (float) a;
    }

    public static float min(float a, float b, float c) {
        return a < b ? a < c ? a : c : b < c ? b : c;
    }

    public static float min(int a, int b, int c) {
        if (a < b) {
            if (a < c) {
                c = a;
            }
        } else if (b < c) {
            c = b;
        }
        return (float) c;
    }

    public static float dist(float x1, float y1, float x2, float y2) {
        return (float) Math.hypot((double) (x2 - x1), (double) (y2 - y1));
    }

    public static float dist(float x1, float y1, float z1, float x2, float y2, float z2) {
        float x = x2 - x1;
        float y = y2 - y1;
        float z = z2 - z1;
        return (float) Math.sqrt((double) (((x * x) + (y * y)) + (z * z)));
    }

    public static float mag(float a, float b) {
        return (float) Math.hypot((double) a, (double) b);
    }

    public static float mag(float a, float b, float c) {
        return (float) Math.sqrt((double) (((a * a) + (b * b)) + (c * c)));
    }

    public static float sq(float v) {
        return v * v;
    }

    public static float dot(float v1x, float v1y, float v2x, float v2y) {
        return (v1x * v2x) + (v1y * v2y);
    }

    public static float cross(float v1x, float v1y, float v2x, float v2y) {
        return (v1x * v2y) - (v1y * v2x);
    }

    public static float radians(float degrees) {
        return DEG_TO_RAD * degrees;
    }

    public static float degrees(float radians) {
        return RAD_TO_DEG * radians;
    }

    public static float acos(float value) {
        return (float) Math.acos((double) value);
    }

    public static float asin(float value) {
        return (float) Math.asin((double) value);
    }

    public static float atan(float value) {
        return (float) Math.atan((double) value);
    }

    public static float atan2(float a, float b) {
        return (float) Math.atan2((double) a, (double) b);
    }

    public static float tan(float angle) {
        return (float) Math.tan((double) angle);
    }

    public static float lerp(float start, float stop, float amount) {
        return ((stop - start) * amount) + start;
    }

    public static float lerpDeg(float start, float end, float amount) {
        return (((((end - start) + 180.0f) % 360.0f) - 180.0f) * amount) + start;
    }

    public static float norm(float start, float stop, float value) {
        return (value - start) / (stop - start);
    }

    public static float map(float minStart, float minStop, float maxStart, float maxStop, float value) {
        return ((maxStart - maxStop) * ((value - minStart) / (minStop - minStart))) + maxStart;
    }

    public static int random(int howbig) {
        return (int) (sRandom.nextFloat() * ((float) howbig));
    }

    public static int random(int howsmall, int howbig) {
        if (howsmall >= howbig) {
            return howsmall;
        }
        return (int) ((sRandom.nextFloat() * ((float) (howbig - howsmall))) + ((float) howsmall));
    }

    public static float random(float howbig) {
        return sRandom.nextFloat() * howbig;
    }

    public static float random(float howsmall, float howbig) {
        if (howsmall >= howbig) {
            return howsmall;
        }
        return (sRandom.nextFloat() * (howbig - howsmall)) + howsmall;
    }

    public static void randomSeed(long seed) {
        sRandom.setSeed(seed);
    }

    public static int addOrThrow(int a, int b) throws IllegalArgumentException {
        if (b == 0) {
            return a;
        }
        if (b > 0 && a <= HwBootFail.STAGE_BOOT_SUCCESS - b) {
            return a + b;
        }
        if (b < 0 && a >= RtlSpacingHelper.UNDEFINED - b) {
            return a + b;
        }
        throw new IllegalArgumentException("Addition overflow: " + a + " + " + b);
    }
}
