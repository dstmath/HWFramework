package com.android.commands.monkey;

import android.content.ComponentName;
import android.graphics.PointF;
import android.hardware.display.DisplayManagerGlobal;
import android.os.SystemClock;
import android.view.Display;
import android.view.KeyCharacterMap;
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
    private static final int[] MAJOR_NAV_KEYS = new int[]{82, 23};
    private static final int[] NAV_KEYS = new int[]{19, 20, 21, 22};
    private static final boolean[] PHYSICAL_KEY_EXISTS = new boolean[(KeyEvent.getMaxKeyCode() + 1)];
    private static final int[] SCREEN_ROTATION_DEGREES = new int[]{0, 1, 2, 3};
    private static final int[] SYS_KEYS = new int[]{3, 4, 5, 6, 24, 25, 164, 91};
    private int mEventCount = 0;
    private float[] mFactors = new float[12];
    private boolean mKeyboardOpen = false;
    private List<ComponentName> mMainApps;
    private MonkeyPermissionUtil mPermissionUtil;
    private MonkeyEventQueue mQ;
    private Random mRandom;
    private long mThrottle = 0;
    private int mVerbose = 0;

    static {
        int i;
        for (i = 0; i < PHYSICAL_KEY_EXISTS.length; i++) {
            PHYSICAL_KEY_EXISTS[i] = true;
        }
        for (i = 0; i < SYS_KEYS.length; i++) {
            PHYSICAL_KEY_EXISTS[SYS_KEYS[i]] = KeyCharacterMap.deviceHasKey(SYS_KEYS[i]);
        }
    }

    public static String getKeyName(int keycode) {
        return KeyEvent.keyCodeToString(keycode);
    }

    public static int getKeyCode(String keyName) {
        return KeyEvent.keyCodeFromString(keyName);
    }

    public MonkeySourceRandom(Random random, List<ComponentName> MainApps, long throttle, boolean randomizeThrottle, boolean permissionTargetSystem) {
        this.mFactors[0] = 15.0f;
        this.mFactors[1] = 10.0f;
        this.mFactors[3] = 15.0f;
        this.mFactors[4] = 0.0f;
        this.mFactors[6] = 25.0f;
        this.mFactors[7] = 15.0f;
        this.mFactors[8] = 2.0f;
        this.mFactors[9] = 2.0f;
        this.mFactors[10] = 1.0f;
        this.mFactors[5] = 0.0f;
        this.mFactors[11] = 13.0f;
        this.mFactors[2] = 2.0f;
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
        int defaultCount = 0;
        for (i = 0; i < 12; i++) {
            if (this.mFactors[i] <= 0.0f) {
                userSum -= this.mFactors[i];
            } else {
                defaultSum += this.mFactors[i];
                defaultCount++;
            }
        }
        if (userSum > 100.0f) {
            Logger.err.println("** Event weights > 100%");
            return false;
        } else if (defaultCount != 0 || (userSum >= 99.9f && userSum <= 100.1f)) {
            float defaultsAdjustment = (100.0f - userSum) / defaultSum;
            for (i = 0; i < 12; i++) {
                if (this.mFactors[i] <= 0.0f) {
                    this.mFactors[i] = -this.mFactors[i];
                } else {
                    float[] fArr = this.mFactors;
                    fArr[i] = fArr[i] * defaultsAdjustment;
                }
            }
            if (this.mVerbose > 0) {
                Logger.out.println("// Event percentages:");
                for (i = 0; i < 12; i++) {
                    Logger.out.println("//   " + i + ": " + this.mFactors[i] + "%");
                }
            }
            if (!validateKeys()) {
                return false;
            }
            float sum = 0.0f;
            for (i = 0; i < 12; i++) {
                sum += this.mFactors[i] / 100.0f;
                this.mFactors[i] = sum;
            }
            return true;
        } else {
            Logger.err.println("** Event weights != 100%");
            return false;
        }
    }

    private static boolean validateKeyCategory(String catName, int[] keys, float factor) {
        if (factor < 0.1f) {
            return true;
        }
        for (int i : keys) {
            if (PHYSICAL_KEY_EXISTS[i]) {
                return true;
            }
        }
        Logger.err.println("** " + catName + " has no physical keys but with factor " + factor + "%.");
        return false;
    }

    private boolean validateKeys() {
        if (validateKeyCategory("NAV_KEYS", NAV_KEYS, this.mFactors[6]) && validateKeyCategory("MAJOR_NAV_KEYS", MAJOR_NAV_KEYS, this.mFactors[7])) {
            return validateKeyCategory("SYS_KEYS", SYS_KEYS, this.mFactors[8]);
        }
        return false;
    }

    public void setFactors(float[] factors) {
        int c = 12;
        if (factors.length < 12) {
            c = factors.length;
        }
        for (int i = 0; i < c; i++) {
            this.mFactors[i] = factors[i];
        }
    }

    public void setFactors(int index, float v) {
        this.mFactors[index] = v;
    }

    private void generatePointerEvent(Random random, int gesture) {
        Display display = DisplayManagerGlobal.getInstance().getRealDisplay(0);
        PointF p1 = randomPoint(random, display);
        PointF v1 = randomVector(random);
        long downAt = SystemClock.uptimeMillis();
        this.mQ.addLast(new MonkeyTouchEvent(0).setDownTime(downAt).addPointer(0, p1.x, p1.y).setIntermediateNote(false));
        int count;
        int i;
        if (gesture == 1) {
            count = random.nextInt(10);
            for (i = 0; i < count; i++) {
                randomWalk(random, display, p1, v1);
                this.mQ.addLast(new MonkeyTouchEvent(2).setDownTime(downAt).addPointer(0, p1.x, p1.y).setIntermediateNote(true));
            }
        } else if (gesture == 2) {
            PointF p2 = randomPoint(random, display);
            PointF v2 = randomVector(random);
            randomWalk(random, display, p1, v1);
            this.mQ.addLast(new MonkeyTouchEvent(261).setDownTime(downAt).addPointer(0, p1.x, p1.y).addPointer(1, p2.x, p2.y).setIntermediateNote(true));
            count = random.nextInt(10);
            for (i = 0; i < count; i++) {
                randomWalk(random, display, p1, v1);
                randomWalk(random, display, p2, v2);
                this.mQ.addLast(new MonkeyTouchEvent(2).setDownTime(downAt).addPointer(0, p1.x, p1.y).addPointer(1, p2.x, p2.y).setIntermediateNote(true));
            }
            randomWalk(random, display, p1, v1);
            randomWalk(random, display, p2, v2);
            this.mQ.addLast(new MonkeyTouchEvent(262).setDownTime(downAt).addPointer(0, p1.x, p1.y).addPointer(1, p2.x, p2.y).setIntermediateNote(true));
        }
        randomWalk(random, display, p1, v1);
        this.mQ.addLast(new MonkeyTouchEvent(1).setDownTime(downAt).addPointer(0, p1.x, p1.y).setIntermediateNote(false));
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
        for (int i = 0; i < 10; i++) {
            boolean z;
            int dX = random.nextInt(10) - 5;
            int dY = random.nextInt(10) - 5;
            MonkeyEventQueue monkeyEventQueue = this.mQ;
            MonkeyMotionEvent addPointer = new MonkeyTrackballEvent(2).addPointer(0, (float) dX, (float) dY);
            if (i > 0) {
                z = true;
            } else {
                z = false;
            }
            monkeyEventQueue.addLast(addPointer.setIntermediateNote(z));
        }
        if (random.nextInt(10) == 0) {
            long downAt = SystemClock.uptimeMillis();
            this.mQ.addLast(new MonkeyTrackballEvent(0).setDownTime(downAt).addPointer(0, 0.0f, 0.0f).setIntermediateNote(true));
            this.mQ.addLast(new MonkeyTrackballEvent(1).setDownTime(downAt).addPointer(0, 0.0f, 0.0f).setIntermediateNote(false));
        }
    }

    private void generateRotationEvent(Random random) {
        this.mQ.addLast(new MonkeyRotationEvent(SCREEN_ROTATION_DEGREES[random.nextInt(SCREEN_ROTATION_DEGREES.length)], random.nextBoolean()));
    }

    private void generateEvents() {
        float cls = this.mRandom.nextFloat();
        if (cls < this.mFactors[0]) {
            generatePointerEvent(this.mRandom, 0);
        } else if (cls < this.mFactors[1]) {
            generatePointerEvent(this.mRandom, 1);
        } else if (cls < this.mFactors[2]) {
            generatePointerEvent(this.mRandom, 2);
        } else if (cls < this.mFactors[3]) {
            generateTrackballEvent(this.mRandom);
        } else if (cls < this.mFactors[4]) {
            generateRotationEvent(this.mRandom);
        } else if (cls < this.mFactors[5]) {
            this.mQ.add(this.mPermissionUtil.generateRandomPermissionEvent(this.mRandom));
        } else {
            while (true) {
                int lastKey;
                if (cls < this.mFactors[6]) {
                    lastKey = NAV_KEYS[this.mRandom.nextInt(NAV_KEYS.length)];
                } else if (cls < this.mFactors[7]) {
                    lastKey = MAJOR_NAV_KEYS[this.mRandom.nextInt(MAJOR_NAV_KEYS.length)];
                } else if (cls < this.mFactors[8]) {
                    lastKey = SYS_KEYS[this.mRandom.nextInt(SYS_KEYS.length)];
                } else if (cls < this.mFactors[9]) {
                    this.mQ.addLast(new MonkeyActivityEvent((ComponentName) this.mMainApps.get(this.mRandom.nextInt(this.mMainApps.size()))));
                    return;
                } else if (cls < this.mFactors[10]) {
                    MonkeyEvent e = new MonkeyFlipEvent(this.mKeyboardOpen);
                    this.mKeyboardOpen ^= 1;
                    this.mQ.addLast(e);
                    return;
                } else {
                    lastKey = this.mRandom.nextInt(KeyEvent.getMaxKeyCode() - 1) + 1;
                }
                if (lastKey != 26 && lastKey != 6 && lastKey != 223 && lastKey != 276 && PHYSICAL_KEY_EXISTS[lastKey]) {
                    this.mQ.addLast(new MonkeyKeyEvent(0, lastKey));
                    this.mQ.addLast(new MonkeyKeyEvent(1, lastKey));
                    return;
                }
            }
        }
    }

    public boolean validate() {
        int ret = 1;
        if (this.mFactors[5] != 0.0f) {
            ret = this.mPermissionUtil.populatePermissionsMapping();
            if (ret != 0 && this.mVerbose >= 2) {
                this.mPermissionUtil.dump();
            }
        }
        return adjustEventFactors() & ret;
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
        this.mEventCount++;
        MonkeyEvent e = (MonkeyEvent) this.mQ.getFirst();
        this.mQ.removeFirst();
        return e;
    }
}
