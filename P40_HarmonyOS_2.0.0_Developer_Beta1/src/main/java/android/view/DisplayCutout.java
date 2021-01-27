package android.view;

import android.common.HwFrameworkFactory;
import android.content.res.Resources;
import android.graphics.Insets;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.util.PathParser;
import android.util.proto.ProtoOutputStream;
import android.view.SurfaceControl;
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
    @GuardedBy({"CACHE_LOCK"})
    private static Pair<Path, DisplayCutout> sCachedCutout = NULL_PAIR;
    @GuardedBy({"CACHE_LOCK"})
    private static float sCachedDensity;
    @GuardedBy({"CACHE_LOCK"})
    private static int sCachedDisplayHeight;
    @GuardedBy({"CACHE_LOCK"})
    private static int sCachedDisplayWidth;
    private static float sCachedRatio;
    @GuardedBy({"CACHE_LOCK"})
    private static String sCachedSpec;
    private final Bounds mBounds;
    private volatile Rect mDisplaySideSafeInsets;
    private final Rect mSafeInsets;
    private volatile Rect mWaterfallInsets;

    @Retention(RetentionPolicy.SOURCE)
    public @interface BoundsPosition {
    }

    static {
        Rect rect = ZERO_RECT;
        NO_CUTOUT = new DisplayCutout(rect, rect, rect, rect, rect, false);
    }

    public void setDisplaySideSafeInsets(Rect rect) {
        if (rect != null) {
            synchronized (DisplayCutout.class) {
                if (sCachedRatio == 0.0f) {
                    this.mDisplaySideSafeInsets = rect;
                } else {
                    this.mDisplaySideSafeInsets = new Rect((int) (((float) rect.left) * sCachedRatio), (int) (((float) rect.top) * sCachedRatio), (int) (((float) rect.right) * sCachedRatio), (int) (((float) rect.bottom) * sCachedRatio));
                }
            }
        }
    }

    public Rect getDisplaySideSafeInsets() {
        return this.mDisplaySideSafeInsets;
    }

    public void setDisplayWaterfallInsets(Rect rect) {
        if (rect != null) {
            synchronized (DisplayCutout.class) {
                if (sCachedRatio == 0.0f) {
                    this.mWaterfallInsets.left = rect.left;
                    this.mWaterfallInsets.top = rect.top;
                    this.mWaterfallInsets.right = rect.right;
                    this.mWaterfallInsets.bottom = rect.bottom;
                    return;
                }
                this.mWaterfallInsets.left = (int) (((float) rect.left) * sCachedRatio);
                this.mWaterfallInsets.top = (int) (((float) rect.top) * sCachedRatio);
                this.mWaterfallInsets.right = (int) (((float) rect.right) * sCachedRatio);
                this.mWaterfallInsets.bottom = (int) (((float) rect.bottom) * sCachedRatio);
            }
        }
    }

    public void updateSafeInsets(Rect rect) {
        if (rect != null && !rect.equals(ZERO_RECT)) {
            Rect rect2 = this.mSafeInsets;
            rect2.left = Math.max(rect2.left, this.mWaterfallInsets.left);
            Rect rect3 = this.mSafeInsets;
            rect3.top = Math.max(rect3.top, this.mWaterfallInsets.top);
            Rect rect4 = this.mSafeInsets;
            rect4.right = Math.max(rect4.right, this.mWaterfallInsets.right);
            Rect rect5 = this.mSafeInsets;
            rect5.bottom = Math.max(rect5.bottom, this.mWaterfallInsets.bottom);
        }
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

    public DisplayCutout(Insets safeInsets, Rect boundLeft, Rect boundTop, Rect boundRight, Rect boundBottom, Rect waterfallInsets, boolean copyArguments) {
        this(safeInsets.toRect(), boundLeft, boundTop, boundRight, boundBottom, copyArguments);
        if (waterfallInsets != null) {
            synchronized (DisplayCutout.class) {
                if (sCachedRatio == 0.0f) {
                    this.mWaterfallInsets.left = waterfallInsets.left;
                    this.mWaterfallInsets.top = waterfallInsets.top;
                    this.mWaterfallInsets.right = waterfallInsets.right;
                    this.mWaterfallInsets.bottom = waterfallInsets.bottom;
                    return;
                }
                this.mWaterfallInsets.left = (int) (((float) waterfallInsets.left) * sCachedRatio);
                this.mWaterfallInsets.top = (int) (((float) waterfallInsets.top) * sCachedRatio);
                this.mWaterfallInsets.right = (int) (((float) waterfallInsets.right) * sCachedRatio);
                this.mWaterfallInsets.bottom = (int) (((float) waterfallInsets.bottom) * sCachedRatio);
            }
        }
    }

    @Deprecated
    public DisplayCutout(Rect safeInsets, List<Rect> boundingRects) {
        this(safeInsets, extractBoundsFromList(safeInsets, boundingRects), true);
    }

    private DisplayCutout(Rect safeInsets, Rect boundLeft, Rect boundTop, Rect boundRight, Rect boundBottom, boolean copyArguments) {
        this.mWaterfallInsets = new Rect();
        this.mSafeInsets = getCopyOrRef(safeInsets, copyArguments);
        this.mBounds = new Bounds(boundLeft, boundTop, boundRight, boundBottom, copyArguments);
    }

    private DisplayCutout(Rect safeInsets, Rect[] bounds, boolean copyArguments) {
        this.mWaterfallInsets = new Rect();
        this.mSafeInsets = getCopyOrRef(safeInsets, copyArguments);
        this.mBounds = new Bounds(bounds, copyArguments);
    }

    private DisplayCutout(Rect safeInsets, Bounds bounds) {
        this.mWaterfallInsets = new Rect();
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

    public Insets getWaterfallInsets() {
        return Insets.of(this.mWaterfallInsets.left, this.mWaterfallInsets.top, this.mWaterfallInsets.right, this.mWaterfallInsets.bottom);
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
        return "DisplayCutout{insets=" + this.mSafeInsets + " waterfall=" + Insets.of(this.mWaterfallInsets) + " boundingRect={" + this.mBounds + "}}";
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

    private static Pair<Path, DisplayCutout> pathAndDisplayCutoutFromSpec(String spec, int displayWidth, int displayHeight, float density) {
        Throwable e;
        float offsetX;
        String bottomSpec;
        String bottomSpec2;
        float ratio;
        int bottomInset;
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
        String spec2 = spec.trim();
        if (spec2.endsWith(RIGHT_MARKER)) {
            offsetX = (float) displayWidth;
            spec2 = spec2.substring(0, spec2.length() - RIGHT_MARKER.length()).trim();
        } else {
            offsetX = ((float) displayWidth) / 2.0f;
        }
        boolean inDp = spec2.endsWith(DP_MARKER);
        if (inDp) {
            spec2 = spec2.substring(0, spec2.length() - DP_MARKER.length());
        }
        if (spec2.contains(BOTTOM_MARKER)) {
            String[] splits = spec2.split(BOTTOM_MARKER, 2);
            String spec3 = splits[0].trim();
            bottomSpec = splits[1].trim();
            bottomSpec2 = spec3;
        } else {
            bottomSpec = null;
            bottomSpec2 = spec2;
        }
        Region r = Region.obtain();
        try {
            Path p = PathParser.createPathFromPathData(bottomSpec2);
            Matrix m = new Matrix();
            if (inDp) {
                m.postScale(density, density);
            }
            if (inDp || !isRogOn()) {
                ratio = 1.0f;
            } else {
                IBinder displayToken = SurfaceControl.getInternalDisplayToken();
                if (displayToken != null) {
                    SurfaceControl.PhysicalDisplayInfo[] configs = SurfaceControl.getDisplayConfigs(displayToken);
                    if (configs == null || configs.length == 0 || configs[0].width == 0 || configs[0].height == 0) {
                        ratio = 1.0f;
                    } else {
                        m.postScale((((float) displayWidth) * 1.0f) / ((float) configs[0].width), (((float) displayHeight) * 1.0f) / ((float) configs[0].height));
                        float ratio2 = (((float) displayWidth) * 1.0f) / ((float) configs[0].width);
                        Log.d(TAG, "pathAndDisplayCutoutFromSpec ,displayWidth=" + displayWidth + ",realWidth =" + configs[0].width + ",displayWidth =" + displayHeight + ",realHeight =" + configs[0].height + ",scale1 " + ((((float) displayWidth) * 1.0f) / ((float) configs[0].width)) + ",scale2 " + ((((float) displayHeight) * 1.0f) / ((float) configs[0].height)));
                        ratio = ratio2;
                    }
                } else {
                    ratio = 1.0f;
                }
            }
            m.postTranslate(offsetX, 0.0f);
            p.transform(m);
            Rect boundTop = new Rect();
            toRectAndAddToRegion(p, r, boundTop);
            int topInset = boundTop.bottom;
            Rect boundBottom = null;
            if (bottomSpec != null) {
                try {
                    Path bottomPath = PathParser.createPathFromPathData(bottomSpec);
                    m.postTranslate(0.0f, (float) displayHeight);
                    bottomPath.transform(m);
                    p.addPath(bottomPath);
                    Rect boundBottom2 = new Rect();
                    toRectAndAddToRegion(bottomPath, r, boundBottom2);
                    bottomInset = displayHeight - boundBottom2.top;
                    boundBottom = boundBottom2;
                } catch (Throwable e2) {
                    Log.wtf(TAG, "Could not inflate bottom cutout: ", e2);
                    return NULL_PAIR;
                }
            } else {
                bottomInset = 0;
            }
            Pair<Path, DisplayCutout> result = new Pair<>(p, new DisplayCutout(new Rect(0, topInset, 0, bottomInset), null, boundTop, null, boundBottom, false));
            synchronized (CACHE_LOCK) {
                sCachedSpec = bottomSpec2;
                sCachedDisplayWidth = displayWidth;
                sCachedDisplayHeight = displayHeight;
                sCachedDensity = density;
                sCachedCutout = result;
                sCachedRatio = ratio;
            }
            return result;
        } catch (Throwable e3) {
            Log.wtf(TAG, "Could not inflate cutout: ", e3);
            return NULL_PAIR;
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
                    out.writeTypedObject(cutout.mWaterfallInsets, flags);
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
            DisplayCutout dc = new DisplayCutout(Insets.of((Rect) in.readTypedObject(Rect.CREATOR)), bounds[0], bounds[1], bounds[2], bounds[3], (Rect) in.readTypedObject(Rect.CREATOR), false);
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
