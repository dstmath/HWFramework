package android.view;

import android.common.HwFrameworkFactory;
import android.content.res.Resources;
import android.graphics.Insets;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.util.proto.ProtoOutputStream;
import com.android.internal.R;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class DisplayCutout {
    private static final String BOTTOM_MARKER = "@bottom";
    public static final int BOUNDS_POSITION_BOTTOM = 3;
    public static final int BOUNDS_POSITION_LEFT = 0;
    public static final int BOUNDS_POSITION_LENGTH = 4;
    public static final int BOUNDS_POSITION_RIGHT = 2;
    public static final int BOUNDS_POSITION_TOP = 1;
    private static final Object CACHE_LOCK = new Object();
    private static final String DP_MARKER = "@dp";
    public static final String EMULATION_OVERLAY_CATEGORY = "com.android.internal.display_cutout_emulation";
    public static final DisplayCutout NO_CUTOUT;
    private static final Pair<Path, DisplayCutout> NULL_PAIR = new Pair<>(null, null);
    private static final String RIGHT_MARKER = "@right";
    private static final String TAG = "DisplayCutout";
    private static final Rect ZERO_RECT = new Rect();
    public static float mCachedRatio;
    @GuardedBy({"CACHE_LOCK"})
    private static Pair<Path, DisplayCutout> sCachedCutout = NULL_PAIR;
    @GuardedBy({"CACHE_LOCK"})
    private static float sCachedDensity;
    @GuardedBy({"CACHE_LOCK"})
    private static int sCachedDisplayHeight;
    @GuardedBy({"CACHE_LOCK"})
    private static int sCachedDisplayWidth;
    @GuardedBy({"CACHE_LOCK"})
    private static String sCachedSpec;
    private final Bounds mBounds;
    private volatile Rect mDisplaySideSafeInsets;
    private final Rect mSafeInsets;

    @Retention(RetentionPolicy.SOURCE)
    public @interface BoundsPosition {
    }

    static {
        Rect rect = ZERO_RECT;
        NO_CUTOUT = new DisplayCutout(rect, rect, rect, rect, rect, false);
    }

    public void setDisplaySideSafeInsets(Rect dsr) {
        if (dsr != null) {
            synchronized (DisplayCutout.class) {
                if (mCachedRatio == 0.0f) {
                    this.mDisplaySideSafeInsets = dsr;
                } else {
                    this.mDisplaySideSafeInsets = new Rect((int) (((float) dsr.left) * mCachedRatio), (int) (((float) dsr.top) * mCachedRatio), (int) (((float) dsr.right) * mCachedRatio), (int) (((float) dsr.bottom) * mCachedRatio));
                }
            }
        }
    }

    public Rect getDisplaySideSafeInsets() {
        return this.mDisplaySideSafeInsets;
    }

    /* access modifiers changed from: private */
    public static class Bounds {
        private final Rect[] mRects;

        private Bounds(Rect left, Rect top, Rect right, Rect bottom, boolean copyArguments) {
            this.mRects = new Rect[4];
            this.mRects[0] = DisplayCutout.getCopyOrRef(left, copyArguments);
            this.mRects[1] = DisplayCutout.getCopyOrRef(top, copyArguments);
            this.mRects[2] = DisplayCutout.getCopyOrRef(right, copyArguments);
            this.mRects[3] = DisplayCutout.getCopyOrRef(bottom, copyArguments);
        }

        private Bounds(Rect[] rects, boolean copyArguments) {
            if (rects.length != 4) {
                throw new IllegalArgumentException("rects must have exactly 4 elements: rects=" + Arrays.toString(rects));
            } else if (copyArguments) {
                this.mRects = new Rect[4];
                for (int i = 0; i < 4; i++) {
                    this.mRects[i] = new Rect(rects[i]);
                }
            } else {
                for (Rect rect : rects) {
                    if (rect == null) {
                        throw new IllegalArgumentException("rects must have non-null elements: rects=" + Arrays.toString(rects));
                    }
                }
                this.mRects = rects;
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean isEmpty() {
            for (Rect rect : this.mRects) {
                if (!rect.isEmpty()) {
                    return false;
                }
            }
            return true;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private Rect getRect(int pos) {
            return new Rect(this.mRects[pos]);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private Rect[] getRects() {
            Rect[] rects = new Rect[4];
            for (int i = 0; i < 4; i++) {
                rects[i] = new Rect(this.mRects[i]);
            }
            return rects;
        }

        public int hashCode() {
            int result = 0;
            for (Rect rect : this.mRects) {
                result = (48271 * result) + rect.hashCode();
            }
            return result;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (o instanceof Bounds) {
                return Arrays.deepEquals(this.mRects, ((Bounds) o).mRects);
            }
            return false;
        }

        public String toString() {
            return "Bounds=" + Arrays.toString(this.mRects);
        }
    }

    public DisplayCutout(Insets safeInsets, Rect boundLeft, Rect boundTop, Rect boundRight, Rect boundBottom) {
        this(safeInsets.toRect(), boundLeft, boundTop, boundRight, boundBottom, true);
    }

    @Deprecated
    public DisplayCutout(Rect safeInsets, List<Rect> boundingRects) {
        this(safeInsets, extractBoundsFromList(safeInsets, boundingRects), true);
    }

    private DisplayCutout(Rect safeInsets, Rect boundLeft, Rect boundTop, Rect boundRight, Rect boundBottom, boolean copyArguments) {
        this.mSafeInsets = getCopyOrRef(safeInsets, copyArguments);
        this.mBounds = new Bounds(boundLeft, boundTop, boundRight, boundBottom, copyArguments);
    }

    private DisplayCutout(Rect safeInsets, Rect[] bounds, boolean copyArguments) {
        this.mSafeInsets = getCopyOrRef(safeInsets, copyArguments);
        this.mBounds = new Bounds(bounds, copyArguments);
    }

    private DisplayCutout(Rect safeInsets, Bounds bounds) {
        this.mSafeInsets = safeInsets;
        this.mBounds = bounds;
    }

    /* access modifiers changed from: private */
    public static Rect getCopyOrRef(Rect r, boolean copyArguments) {
        if (r == null) {
            return ZERO_RECT;
        }
        if (copyArguments) {
            return new Rect(r);
        }
        return r;
    }

    public static Rect[] extractBoundsFromList(Rect safeInsets, List<Rect> boundingRects) {
        Rect[] sortedBounds = new Rect[4];
        for (int i = 0; i < sortedBounds.length; i++) {
            sortedBounds[i] = ZERO_RECT;
        }
        if (!(safeInsets == null || boundingRects == null)) {
            for (Rect bound : boundingRects) {
                if (bound.left == 0) {
                    sortedBounds[0] = bound;
                } else if (bound.top == 0) {
                    sortedBounds[1] = bound;
                } else if (safeInsets.right > 0) {
                    sortedBounds[2] = bound;
                } else if (safeInsets.bottom > 0) {
                    sortedBounds[3] = bound;
                }
            }
        }
        return sortedBounds;
    }

    public boolean isBoundsEmpty() {
        return this.mBounds.isEmpty();
    }

    public boolean isEmpty() {
        return this.mSafeInsets.equals(ZERO_RECT);
    }

    public int getSafeInsetTop() {
        return this.mSafeInsets.top;
    }

    public int getSafeInsetBottom() {
        return this.mSafeInsets.bottom;
    }

    public int getSafeInsetLeft() {
        return this.mSafeInsets.left;
    }

    public int getSafeInsetRight() {
        return this.mSafeInsets.right;
    }

    public Rect getSafeInsets() {
        return new Rect(this.mSafeInsets);
    }

    public List<Rect> getBoundingRects() {
        List<Rect> result = new ArrayList<>();
        Rect[] boundingRectsAll = getBoundingRectsAll();
        for (Rect bound : boundingRectsAll) {
            if (!bound.isEmpty()) {
                result.add(new Rect(bound));
            }
        }
        return result;
    }

    public Rect[] getBoundingRectsAll() {
        return this.mBounds.getRects();
    }

    public Rect getBoundingRectLeft() {
        return this.mBounds.getRect(0);
    }

    public Rect getBoundingRectTop() {
        return this.mBounds.getRect(1);
    }

    public Rect getBoundingRectRight() {
        return this.mBounds.getRect(2);
    }

    public Rect getBoundingRectBottom() {
        return this.mBounds.getRect(3);
    }

    public int hashCode() {
        return (this.mSafeInsets.hashCode() * 48271) + this.mBounds.hashCode();
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof DisplayCutout)) {
            return false;
        }
        DisplayCutout c = (DisplayCutout) o;
        if (!this.mSafeInsets.equals(c.mSafeInsets) || !this.mBounds.equals(c.mBounds)) {
            return false;
        }
        return true;
    }

    public String toString() {
        return "DisplayCutout{insets=" + this.mSafeInsets + " boundingRect={" + this.mBounds + "} sideInsets=" + this.mDisplaySideSafeInsets + "}";
    }

    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        long token = proto.start(fieldId);
        this.mSafeInsets.writeToProto(proto, 1146756268033L);
        this.mBounds.getRect(0).writeToProto(proto, 1146756268035L);
        this.mBounds.getRect(1).writeToProto(proto, 1146756268036L);
        this.mBounds.getRect(2).writeToProto(proto, 1146756268037L);
        this.mBounds.getRect(3).writeToProto(proto, 1146756268038L);
        proto.end(token);
    }

    public DisplayCutout inset(int insetLeft, int insetTop, int insetRight, int insetBottom) {
        if ((insetLeft == 0 && insetTop == 0 && insetRight == 0 && insetBottom == 0) || isBoundsEmpty()) {
            return this;
        }
        Rect safeInsets = new Rect(this.mSafeInsets);
        if (insetTop > 0 || safeInsets.top > 0) {
            safeInsets.top = atLeastZero(safeInsets.top - insetTop);
        }
        if (insetBottom > 0 || safeInsets.bottom > 0) {
            safeInsets.bottom = atLeastZero(safeInsets.bottom - insetBottom);
        }
        if (insetLeft > 0 || safeInsets.left > 0) {
            safeInsets.left = atLeastZero(safeInsets.left - insetLeft);
        }
        if (insetRight > 0 || safeInsets.right > 0) {
            safeInsets.right = atLeastZero(safeInsets.right - insetRight);
        }
        if (!HwFrameworkFactory.getHwExtDisplaySizeUtil().hasSideInScreen() && insetLeft == 0 && insetTop == 0 && this.mSafeInsets.equals(safeInsets)) {
            return this;
        }
        Rect[] bounds = this.mBounds.getRects();
        for (int i = 0; i < bounds.length; i++) {
            if (!bounds[i].equals(ZERO_RECT)) {
                bounds[i].offset(-insetLeft, -insetTop);
            }
        }
        return new DisplayCutout(safeInsets, bounds, false);
    }

    public DisplayCutout replaceSafeInsets(Rect safeInsets) {
        return new DisplayCutout(new Rect(safeInsets), this.mBounds);
    }

    private static int atLeastZero(int value) {
        if (value < 0) {
            return 0;
        }
        return value;
    }

    @VisibleForTesting
    public static DisplayCutout fromBoundingRect(int left, int top, int right, int bottom, int pos) {
        Rect rect;
        Rect[] bounds = new Rect[4];
        for (int i = 0; i < 4; i++) {
            if (pos != i) {
                rect = new Rect();
            }
            bounds[i] = rect;
        }
        return new DisplayCutout(ZERO_RECT, bounds, false);
    }

    public static DisplayCutout fromBounds(Rect[] bounds) {
        return new DisplayCutout(ZERO_RECT, bounds, false);
    }

    public static DisplayCutout fromResourcesRectApproximation(Resources res, int displayWidth, int displayHeight) {
        return fromSpec(res.getString(R.string.config_mainBuiltInDisplayCutoutRectApproximation), displayWidth, displayHeight, ((float) DisplayMetrics.DENSITY_DEVICE_STABLE) / 160.0f);
    }

    public static Path pathFromResources(Resources res, int displayWidth, int displayHeight) {
        return pathAndDisplayCutoutFromSpec(res.getString(R.string.config_mainBuiltInDisplayCutout), displayWidth, displayHeight, ((float) DisplayMetrics.DENSITY_DEVICE_STABLE) / 160.0f).first;
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PRIVATE)
    public static DisplayCutout fromSpec(String spec, int displayWidth, int displayHeight, float density) {
        return pathAndDisplayCutoutFromSpec(spec, displayWidth, displayHeight, density).second;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0074, code lost:
        r0 = r25.trim();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x007f, code lost:
        if (r0.endsWith(android.view.DisplayCutout.RIGHT_MARKER) == false) goto L_0x0096;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0081, code lost:
        r4 = (float) r26;
        r0 = r0.substring(0, r0.length() - android.view.DisplayCutout.RIGHT_MARKER.length()).trim();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0096, code lost:
        r4 = ((float) r26) / 2.0f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x009a, code lost:
        r6 = r0.endsWith(android.view.DisplayCutout.DP_MARKER);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x00a0, code lost:
        if (r6 == false) goto L_0x00b1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x00a2, code lost:
        r0 = r0.substring(0, r0.length() - android.view.DisplayCutout.DP_MARKER.length());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x00b8, code lost:
        if (r0.contains(android.view.DisplayCutout.BOTTOM_MARKER) == false) goto L_0x00d1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x00ba, code lost:
        r8 = r0.split(android.view.DisplayCutout.BOTTOM_MARKER, 2);
        r0 = r8[0].trim();
        r8 = r8[1].trim();
        r7 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00d1, code lost:
        r8 = null;
        r7 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00d3, code lost:
        r9 = android.graphics.Region.obtain();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:?, code lost:
        r0 = android.util.PathParser.createPathFromPathData(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00db, code lost:
        r0 = new android.graphics.Matrix();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00e3, code lost:
        if (r6 == false) goto L_0x00e8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x00e5, code lost:
        r0.postScale(r28, r28);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00ea, code lost:
        if (r6 != false) goto L_0x0199;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00f0, code lost:
        if (isRogOn() == false) goto L_0x0199;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x00f2, code lost:
        r12 = android.view.SurfaceControl.getInternalDisplayToken();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x00f6, code lost:
        if (r12 == null) goto L_0x0196;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x00f8, code lost:
        r13 = android.view.SurfaceControl.getDisplayConfigs(r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x00fc, code lost:
        if (r13 == null) goto L_0x0193;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x00ff, code lost:
        if (r13.length == 0) goto L_0x0193;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x0105, code lost:
        if (r13[0].width == 0) goto L_0x0190;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x010b, code lost:
        if (r13[0].height == 0) goto L_0x0190;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x010d, code lost:
        r0.postScale((((float) r26) * 1.0f) / ((float) r13[0].width), (((float) r27) * 1.0f) / ((float) r13[0].height));
        r0 = (((float) r26) * 1.0f) / ((float) r13[0].width);
        android.util.Log.d(android.view.DisplayCutout.TAG, "pathAndDisplayCutoutFromSpec ,displayWidth=" + r26 + ",realWidth =" + r13[0].width + ",displayWidth =" + r27 + ",realHeight =" + r13[0].height + ",scale1 " + ((((float) r26) * 1.0f) / ((float) r13[0].width)) + ",scale2 " + ((((float) r27) * 1.0f) / ((float) r13[0].height)));
        r17 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x0190, code lost:
        r17 = 1.0f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x0193, code lost:
        r17 = 1.0f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x0196, code lost:
        r17 = 1.0f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x0199, code lost:
        r17 = 1.0f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x019b, code lost:
        r0.postTranslate(r4, 0.0f);
        r0.transform(r0);
        r12 = new android.graphics.Rect();
        toRectAndAddToRegion(r0, r9, r12);
        r13 = r12.bottom;
        r14 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x01ad, code lost:
        if (r8 == null) goto L_0x01d9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:?, code lost:
        r15 = android.util.PathParser.createPathFromPathData(r8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x01b3, code lost:
        r0.postTranslate(0.0f, (float) r27);
        r15.transform(r0);
        r0.addPath(r15);
        r0 = new android.graphics.Rect();
        toRectAndAddToRegion(r15, r9, r0);
        r5 = r27 - r0.top;
        r14 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x01cc, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x01cd, code lost:
        android.util.Log.wtf(android.view.DisplayCutout.TAG, "Could not inflate bottom cutout: ", r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x01d8, code lost:
        return android.view.DisplayCutout.NULL_PAIR;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x01d9, code lost:
        r5 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x01da, code lost:
        r0 = new android.util.Pair<>(r0, new android.view.DisplayCutout(new android.graphics.Rect(0, r13, 0, r5), null, r12, null, r14, false));
        r18 = android.view.DisplayCutout.CACHE_LOCK;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:0x01fd, code lost:
        monitor-enter(r18);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:71:?, code lost:
        android.view.DisplayCutout.sCachedSpec = r7;
        android.view.DisplayCutout.sCachedDisplayWidth = r26;
        android.view.DisplayCutout.sCachedDisplayHeight = r27;
        android.view.DisplayCutout.sCachedDensity = r28;
        android.view.DisplayCutout.sCachedCutout = r0;
        android.view.DisplayCutout.mCachedRatio = r17;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:72:0x020a, code lost:
        monitor-exit(r18);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:73:0x020b, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:77:0x020f, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:78:0x0210, code lost:
        android.util.Log.wtf(android.view.DisplayCutout.TAG, "Could not inflate cutout: ", r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:79:0x021b, code lost:
        return android.view.DisplayCutout.NULL_PAIR;
     */
    private static Pair<Path, DisplayCutout> pathAndDisplayCutoutFromSpec(String spec, int displayWidth, int displayHeight, float density) {
        if (TextUtils.isEmpty(spec)) {
            return NULL_PAIR;
        }
        synchronized (CACHE_LOCK) {
            try {
                Log.d(TAG, "pathAndDisplayCutoutFromSpec sCachedDensity=" + sCachedDensity + ",sCachedDisplayWidth=" + sCachedDisplayWidth + ",sCachedDisplayHeight=" + sCachedDisplayHeight + ",density=" + density + " displayWidth " + displayWidth + " displayHeight " + displayHeight);
                try {
                    if (spec.equals(sCachedSpec) && sCachedDisplayWidth == displayWidth && sCachedDisplayHeight == displayHeight && sCachedDensity == density) {
                        return sCachedCutout;
                    }
                } catch (Throwable th) {
                    e = th;
                    throw e;
                }
            } catch (Throwable th2) {
                e = th2;
                throw e;
            }
        }
    }

    public static boolean isRogOn() {
        return SystemProperties.getInt("persist.sys.rog.configmode", 0) == 1;
    }

    private static void toRectAndAddToRegion(Path p, Region inoutRegion, Rect inoutRect) {
        RectF rectF = new RectF();
        p.computeBounds(rectF, false);
        Region clipRegion = Region.obtain();
        if (!isRogOn()) {
            clipRegion.set((int) rectF.left, (int) rectF.top, (int) rectF.right, (int) rectF.bottom);
        } else {
            clipRegion.set((int) (rectF.left + 0.5f), (int) (rectF.top + 0.5f), (int) (rectF.right + 0.5f), (int) (rectF.bottom + 0.5f));
        }
        Region bounds = new Region();
        bounds.setPath(p, clipRegion);
        inoutRect.set(bounds.getBounds());
    }

    public static final class ParcelableWrapper implements Parcelable {
        public static final Parcelable.Creator<ParcelableWrapper> CREATOR = new Parcelable.Creator<ParcelableWrapper>() {
            /* class android.view.DisplayCutout.ParcelableWrapper.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public ParcelableWrapper createFromParcel(Parcel in) {
                return new ParcelableWrapper(ParcelableWrapper.readCutoutFromParcel(in));
            }

            @Override // android.os.Parcelable.Creator
            public ParcelableWrapper[] newArray(int size) {
                return new ParcelableWrapper[size];
            }
        };
        private DisplayCutout mInner;

        public ParcelableWrapper() {
            this(DisplayCutout.NO_CUTOUT);
        }

        public ParcelableWrapper(DisplayCutout cutout) {
            this.mInner = cutout;
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel out, int flags) {
            writeCutoutToParcel(this.mInner, out, flags);
        }

        public static void writeCutoutToParcel(DisplayCutout cutout, Parcel out, int flags) {
            if (cutout == null) {
                out.writeInt(-1);
            } else if (cutout == DisplayCutout.NO_CUTOUT) {
                out.writeInt(0);
            } else {
                out.writeInt(1);
                out.writeTypedObject(cutout.mSafeInsets, flags);
                out.writeTypedArray(cutout.mBounds.getRects(), flags);
                synchronized (DisplayCutout.class) {
                    out.writeTypedObject(cutout.mDisplaySideSafeInsets, flags);
                }
            }
        }

        public void readFromParcel(Parcel in) {
            this.mInner = readCutoutFromParcel(in);
        }

        public static DisplayCutout readCutoutFromParcel(Parcel in) {
            int variant = in.readInt();
            if (variant == -1) {
                return null;
            }
            if (variant == 0) {
                return DisplayCutout.NO_CUTOUT;
            }
            Rect[] bounds = new Rect[4];
            in.readTypedArray(bounds, Rect.CREATOR);
            DisplayCutout dc = new DisplayCutout((Rect) in.readTypedObject(Rect.CREATOR), bounds, false);
            dc.setDisplaySideSafeInsets((Rect) in.readTypedObject(Rect.CREATOR));
            return dc;
        }

        public DisplayCutout get() {
            return this.mInner;
        }

        public void set(ParcelableWrapper cutout) {
            this.mInner = cutout.get();
        }

        public void set(DisplayCutout cutout) {
            this.mInner = cutout;
        }

        public int hashCode() {
            return this.mInner.hashCode();
        }

        public boolean equals(Object o) {
            return (o instanceof ParcelableWrapper) && this.mInner.equals(((ParcelableWrapper) o).mInner);
        }

        public String toString() {
            return String.valueOf(this.mInner);
        }
    }
}
