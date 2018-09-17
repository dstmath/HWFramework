package com.android.commands.monkey;

import android.content.ComponentName;
import android.graphics.PointF;
import android.hardware.display.DisplayManagerGlobal;
import android.os.SystemClock;
import android.view.Display;
import android.view.KeyEvent;
import java.util.List;
import java.util.Random;

public class MonkeySourceRandom implements MonkeyEventSource {
    public static final int FACTORZ_COUNT = 12;
    public static final int FACTOR_ANYTHING = 11;
    public static final int FACTOR_APPSWITCH = 9;
    public static final int FACTOR_FLIP = 10;
    public static final int FACTOR_MAJORNAV = 7;
    public static final int FACTOR_MOTION = 1;
    public static final int FACTOR_NAV = 6;
    public static final int FACTOR_PERMISSION = 5;
    public static final int FACTOR_PINCHZOOM = 2;
    public static final int FACTOR_ROTATION = 4;
    public static final int FACTOR_SYSOPS = 8;
    public static final int FACTOR_TOUCH = 0;
    public static final int FACTOR_TRACKBALL = 3;
    private static final int GESTURE_DRAG = 1;
    private static final int GESTURE_PINCH_OR_ZOOM = 2;
    private static final int GESTURE_TAP = 0;
    private static final int[] MAJOR_NAV_KEYS = null;
    private static final int[] NAV_KEYS = null;
    private static final boolean[] PHYSICAL_KEY_EXISTS = null;
    private static final int[] SCREEN_ROTATION_DEGREES = null;
    private static final int[] SYS_KEYS = null;
    private int mEventCount;
    private float[] mFactors;
    private boolean mKeyboardOpen;
    private List<ComponentName> mMainApps;
    private MonkeyPermissionUtil mPermissionUtil;
    private MonkeyEventQueue mQ;
    private Random mRandom;
    private long mThrottle;
    private int mVerbose;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.commands.monkey.MonkeySourceRandom.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.commands.monkey.MonkeySourceRandom.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.commands.monkey.MonkeySourceRandom.<clinit>():void");
    }

    public static String getKeyName(int keycode) {
        return KeyEvent.keyCodeToString(keycode);
    }

    public static int getKeyCode(String keyName) {
        return KeyEvent.keyCodeFromString(keyName);
    }

    public MonkeySourceRandom(Random random, List<ComponentName> MainApps, long throttle, boolean randomizeThrottle, boolean permissionTargetSystem) {
        this.mFactors = new float[FACTORZ_COUNT];
        this.mEventCount = FACTOR_TOUCH;
        this.mVerbose = FACTOR_TOUCH;
        this.mThrottle = 0;
        this.mKeyboardOpen = false;
        this.mFactors[FACTOR_TOUCH] = 15.0f;
        this.mFactors[GESTURE_DRAG] = 10.0f;
        this.mFactors[FACTOR_TRACKBALL] = 15.0f;
        this.mFactors[FACTOR_ROTATION] = 0.0f;
        this.mFactors[FACTOR_NAV] = 25.0f;
        this.mFactors[FACTOR_MAJORNAV] = 15.0f;
        this.mFactors[FACTOR_SYSOPS] = 2.0f;
        this.mFactors[FACTOR_APPSWITCH] = 2.0f;
        this.mFactors[FACTOR_FLIP] = 1.0f;
        this.mFactors[FACTOR_PERMISSION] = 0.0f;
        this.mFactors[FACTOR_ANYTHING] = 13.0f;
        this.mFactors[GESTURE_PINCH_OR_ZOOM] = 2.0f;
        this.mRandom = random;
        this.mMainApps = MainApps;
        this.mQ = new MonkeyEventQueue(random, throttle, randomizeThrottle);
        this.mPermissionUtil = new MonkeyPermissionUtil();
        this.mPermissionUtil.setTargetSystemPackages(permissionTargetSystem);
    }

    private boolean adjustEventFactors() {
        int i;
        float userSum = 0.0f;
        float defaultSum = 0.0f;
        int defaultCount = FACTOR_TOUCH;
        for (i = FACTOR_TOUCH; i < FACTORZ_COUNT; i += GESTURE_DRAG) {
            if (this.mFactors[i] <= 0.0f) {
                userSum -= this.mFactors[i];
            } else {
                defaultSum += this.mFactors[i];
                defaultCount += GESTURE_DRAG;
            }
        }
        if (userSum > 100.0f) {
            System.err.println("** Event weights > 100%");
            return false;
        } else if (defaultCount != 0 || (userSum >= 99.9f && userSum <= 100.1f)) {
            float defaultsAdjustment = (100.0f - userSum) / defaultSum;
            for (i = FACTOR_TOUCH; i < FACTORZ_COUNT; i += GESTURE_DRAG) {
                if (this.mFactors[i] <= 0.0f) {
                    this.mFactors[i] = -this.mFactors[i];
                } else {
                    float[] fArr = this.mFactors;
                    fArr[i] = fArr[i] * defaultsAdjustment;
                }
            }
            if (this.mVerbose > 0) {
                System.out.println("// Event percentages:");
                for (i = FACTOR_TOUCH; i < FACTORZ_COUNT; i += GESTURE_DRAG) {
                    System.out.println("//   " + i + ": " + this.mFactors[i] + "%");
                }
            }
            if (!validateKeys()) {
                return false;
            }
            float sum = 0.0f;
            for (i = FACTOR_TOUCH; i < FACTORZ_COUNT; i += GESTURE_DRAG) {
                sum += this.mFactors[i] / 100.0f;
                this.mFactors[i] = sum;
            }
            return true;
        } else {
            System.err.println("** Event weights != 100%");
            return false;
        }
    }

    private static boolean validateKeyCategory(String catName, int[] keys, float factor) {
        if (factor < 0.1f) {
            return true;
        }
        for (int i = FACTOR_TOUCH; i < keys.length; i += GESTURE_DRAG) {
            if (PHYSICAL_KEY_EXISTS[keys[i]]) {
                return true;
            }
        }
        System.err.println("** " + catName + " has no physical keys but with factor " + factor + "%.");
        return false;
    }

    private boolean validateKeys() {
        if (validateKeyCategory("NAV_KEYS", NAV_KEYS, this.mFactors[FACTOR_NAV]) && validateKeyCategory("MAJOR_NAV_KEYS", MAJOR_NAV_KEYS, this.mFactors[FACTOR_MAJORNAV])) {
            return validateKeyCategory("SYS_KEYS", SYS_KEYS, this.mFactors[FACTOR_SYSOPS]);
        }
        return false;
    }

    public void setFactors(float[] factors) {
        int c = FACTORZ_COUNT;
        if (factors.length < FACTORZ_COUNT) {
            c = factors.length;
        }
        for (int i = FACTOR_TOUCH; i < c; i += GESTURE_DRAG) {
            this.mFactors[i] = factors[i];
        }
    }

    public void setFactors(int index, float v) {
        this.mFactors[index] = v;
    }

    private void generatePointerEvent(Random random, int gesture) {
        Display display = DisplayManagerGlobal.getInstance().getRealDisplay(FACTOR_TOUCH);
        PointF p1 = randomPoint(random, display);
        PointF v1 = randomVector(random);
        long downAt = SystemClock.uptimeMillis();
        this.mQ.addLast(new MonkeyTouchEvent(FACTOR_TOUCH).setDownTime(downAt).addPointer(FACTOR_TOUCH, p1.x, p1.y).setIntermediateNote(false));
        int count;
        int i;
        if (gesture == GESTURE_DRAG) {
            count = random.nextInt(FACTOR_FLIP);
            for (i = FACTOR_TOUCH; i < count; i += GESTURE_DRAG) {
                randomWalk(random, display, p1, v1);
                this.mQ.addLast(new MonkeyTouchEvent(GESTURE_PINCH_OR_ZOOM).setDownTime(downAt).addPointer(FACTOR_TOUCH, p1.x, p1.y).setIntermediateNote(true));
            }
        } else if (gesture == GESTURE_PINCH_OR_ZOOM) {
            PointF p2 = randomPoint(random, display);
            PointF v2 = randomVector(random);
            randomWalk(random, display, p1, v1);
            this.mQ.addLast(new MonkeyTouchEvent(261).setDownTime(downAt).addPointer(FACTOR_TOUCH, p1.x, p1.y).addPointer(GESTURE_DRAG, p2.x, p2.y).setIntermediateNote(true));
            count = random.nextInt(FACTOR_FLIP);
            for (i = FACTOR_TOUCH; i < count; i += GESTURE_DRAG) {
                randomWalk(random, display, p1, v1);
                randomWalk(random, display, p2, v2);
                this.mQ.addLast(new MonkeyTouchEvent(GESTURE_PINCH_OR_ZOOM).setDownTime(downAt).addPointer(FACTOR_TOUCH, p1.x, p1.y).addPointer(GESTURE_DRAG, p2.x, p2.y).setIntermediateNote(true));
            }
            randomWalk(random, display, p1, v1);
            randomWalk(random, display, p2, v2);
            this.mQ.addLast(new MonkeyTouchEvent(262).setDownTime(downAt).addPointer(FACTOR_TOUCH, p1.x, p1.y).addPointer(GESTURE_DRAG, p2.x, p2.y).setIntermediateNote(true));
        }
        randomWalk(random, display, p1, v1);
        this.mQ.addLast(new MonkeyTouchEvent(GESTURE_DRAG).setDownTime(downAt).addPointer(FACTOR_TOUCH, p1.x, p1.y).setIntermediateNote(false));
    }

    private PointF randomPoint(Random random, Display display) {
        return new PointF((float) random.nextInt(display.getWidth()), (float) random.nextInt(display.getHeight()));
    }

    private PointF randomVector(Random random) {
        return new PointF((random.nextFloat() - 0.5f) * 50.0f, (random.nextFloat() - 0.5f) * 50.0f);
    }

    private void randomWalk(Random random, Display display, PointF point, PointF vector) {
        point.x = Math.max(Math.min(point.x + (random.nextFloat() * vector.x), (float) display.getWidth()), 0.0f);
        point.y = Math.max(Math.min(point.y + (random.nextFloat() * vector.y), (float) display.getHeight()), 0.0f);
    }

    private void generateTrackballEvent(Random random) {
        for (int i = FACTOR_TOUCH; i < FACTOR_FLIP; i += GESTURE_DRAG) {
            boolean z;
            int dX = random.nextInt(FACTOR_FLIP) - 5;
            int dY = random.nextInt(FACTOR_FLIP) - 5;
            MonkeyEventQueue monkeyEventQueue = this.mQ;
            MonkeyMotionEvent addPointer = new MonkeyTrackballEvent(GESTURE_PINCH_OR_ZOOM).addPointer(FACTOR_TOUCH, (float) dX, (float) dY);
            if (i > 0) {
                z = true;
            } else {
                z = false;
            }
            monkeyEventQueue.addLast(addPointer.setIntermediateNote(z));
        }
        if (random.nextInt(FACTOR_FLIP) == 0) {
            long downAt = SystemClock.uptimeMillis();
            this.mQ.addLast(new MonkeyTrackballEvent(FACTOR_TOUCH).setDownTime(downAt).addPointer(FACTOR_TOUCH, 0.0f, 0.0f).setIntermediateNote(true));
            this.mQ.addLast(new MonkeyTrackballEvent(GESTURE_DRAG).setDownTime(downAt).addPointer(FACTOR_TOUCH, 0.0f, 0.0f).setIntermediateNote(false));
        }
    }

    private void generateRotationEvent(Random random) {
        this.mQ.addLast(new MonkeyRotationEvent(SCREEN_ROTATION_DEGREES[random.nextInt(SCREEN_ROTATION_DEGREES.length)], random.nextBoolean()));
    }

    private void generateEvents() {
        boolean z = false;
        float cls = this.mRandom.nextFloat();
        if (cls < this.mFactors[FACTOR_TOUCH]) {
            generatePointerEvent(this.mRandom, FACTOR_TOUCH);
        } else if (cls < this.mFactors[GESTURE_DRAG]) {
            generatePointerEvent(this.mRandom, GESTURE_DRAG);
        } else if (cls < this.mFactors[GESTURE_PINCH_OR_ZOOM]) {
            generatePointerEvent(this.mRandom, GESTURE_PINCH_OR_ZOOM);
        } else if (cls < this.mFactors[FACTOR_TRACKBALL]) {
            generateTrackballEvent(this.mRandom);
        } else if (cls < this.mFactors[FACTOR_ROTATION]) {
            generateRotationEvent(this.mRandom);
        } else if (cls < this.mFactors[FACTOR_PERMISSION]) {
            this.mQ.add(this.mPermissionUtil.generateRandomPermissionEvent(this.mRandom));
        } else {
            while (true) {
                int lastKey;
                if (cls < this.mFactors[FACTOR_NAV]) {
                    lastKey = NAV_KEYS[this.mRandom.nextInt(NAV_KEYS.length)];
                } else if (cls < this.mFactors[FACTOR_MAJORNAV]) {
                    lastKey = MAJOR_NAV_KEYS[this.mRandom.nextInt(MAJOR_NAV_KEYS.length)];
                } else if (cls < this.mFactors[FACTOR_SYSOPS]) {
                    lastKey = SYS_KEYS[this.mRandom.nextInt(SYS_KEYS.length)];
                } else if (cls < this.mFactors[FACTOR_APPSWITCH]) {
                    this.mQ.addLast(new MonkeyActivityEvent((ComponentName) this.mMainApps.get(this.mRandom.nextInt(this.mMainApps.size()))));
                    return;
                } else if (cls < this.mFactors[FACTOR_FLIP]) {
                    break;
                } else {
                    lastKey = this.mRandom.nextInt(KeyEvent.getMaxKeyCode() - 1) + GESTURE_DRAG;
                }
                if (lastKey != 26 && lastKey != FACTOR_NAV && lastKey != 223 && lastKey != 276 && PHYSICAL_KEY_EXISTS[lastKey]) {
                    this.mQ.addLast(new MonkeyKeyEvent(FACTOR_TOUCH, lastKey));
                    this.mQ.addLast(new MonkeyKeyEvent(GESTURE_DRAG, lastKey));
                    return;
                }
            }
            MonkeyEvent e = new MonkeyFlipEvent(this.mKeyboardOpen);
            if (!this.mKeyboardOpen) {
                z = true;
            }
            this.mKeyboardOpen = z;
            this.mQ.addLast(e);
        }
    }

    public boolean validate() {
        int i = GESTURE_DRAG;
        if (this.mFactors[FACTOR_PERMISSION] != 0.0f) {
            i = this.mPermissionUtil.populatePermissionsMapping();
            if (i != 0 && this.mVerbose >= GESTURE_PINCH_OR_ZOOM) {
                this.mPermissionUtil.dump();
            }
        }
        return adjustEventFactors() & i;
    }

    public void setVerbose(int verbose) {
        this.mVerbose = verbose;
    }

    public void generateActivity() {
        this.mQ.addLast(new MonkeyActivityEvent((ComponentName) this.mMainApps.get(this.mRandom.nextInt(this.mMainApps.size()))));
    }

    public MonkeyEvent getNextEvent() {
        if (this.mQ.isEmpty()) {
            generateEvents();
        }
        this.mEventCount += GESTURE_DRAG;
        MonkeyEvent e = (MonkeyEvent) this.mQ.getFirst();
        this.mQ.removeFirst();
        return e;
    }
}
