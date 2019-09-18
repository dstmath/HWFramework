package android.graphics.drawable;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Outline;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hwtheme.HwThemeManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import com.android.internal.R;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class LayerDrawable extends Drawable implements Drawable.Callback {
    public static final int INSET_UNDEFINED = Integer.MIN_VALUE;
    private static final String LOG_TAG = "LayerDrawable";
    public static final int PADDING_MODE_NEST = 0;
    public static final int PADDING_MODE_STACK = 1;
    private boolean mChildRequestedInvalidation;
    private Rect mHotspotBounds;
    LayerState mLayerState;
    private boolean mMutated;
    private int[] mPaddingB;
    private int[] mPaddingL;
    private int[] mPaddingR;
    private int[] mPaddingT;
    private boolean mSuspendChildInvalidation;
    private final Rect mTmpContainer;
    private final Rect mTmpOutRect;
    private final Rect mTmpRect;

    static class ChildDrawable {
        public int mDensity = 160;
        public Drawable mDrawable;
        public int mGravity = 0;
        public int mHeight = -1;
        public int mId = -1;
        public int mInsetB;
        public int mInsetE = Integer.MIN_VALUE;
        public int mInsetL;
        public int mInsetR;
        public int mInsetS = Integer.MIN_VALUE;
        public int mInsetT;
        public int[] mThemeAttrs;
        public int mWidth = -1;

        ChildDrawable(int density) {
            this.mDensity = density;
        }

        ChildDrawable(ChildDrawable orig, LayerDrawable owner, Resources res) {
            Drawable clone;
            Drawable dr = orig.mDrawable;
            if (dr != null) {
                Drawable.ConstantState cs = dr.getConstantState();
                if (cs == null) {
                    clone = dr;
                    if (dr.getCallback() != null) {
                        Log.w(LayerDrawable.LOG_TAG, "Invalid drawable added to LayerDrawable! Drawable already belongs to another owner but does not expose a constant state.", new RuntimeException());
                    }
                } else {
                    clone = res != null ? cs.newDrawable(res) : cs.newDrawable();
                }
                clone.setLayoutDirection(dr.getLayoutDirection());
                clone.setBounds(dr.getBounds());
                clone.setLevel(dr.getLevel());
                clone.setCallback(owner);
            } else {
                clone = null;
            }
            this.mDrawable = clone;
            this.mThemeAttrs = orig.mThemeAttrs;
            this.mInsetL = orig.mInsetL;
            this.mInsetT = orig.mInsetT;
            this.mInsetR = orig.mInsetR;
            this.mInsetB = orig.mInsetB;
            this.mInsetS = orig.mInsetS;
            this.mInsetE = orig.mInsetE;
            this.mWidth = orig.mWidth;
            this.mHeight = orig.mHeight;
            this.mGravity = orig.mGravity;
            this.mId = orig.mId;
            this.mDensity = Drawable.resolveDensity(res, orig.mDensity);
            if (orig.mDensity != this.mDensity) {
                applyDensityScaling(orig.mDensity, this.mDensity);
            }
        }

        public boolean canApplyTheme() {
            return this.mThemeAttrs != null || (this.mDrawable != null && this.mDrawable.canApplyTheme());
        }

        public final void setDensity(int targetDensity) {
            if (this.mDensity != targetDensity) {
                int sourceDensity = this.mDensity;
                this.mDensity = targetDensity;
                applyDensityScaling(sourceDensity, targetDensity);
            }
        }

        private void applyDensityScaling(int sourceDensity, int targetDensity) {
            this.mInsetL = Drawable.scaleFromDensity(this.mInsetL, sourceDensity, targetDensity, false);
            this.mInsetT = Drawable.scaleFromDensity(this.mInsetT, sourceDensity, targetDensity, false);
            this.mInsetR = Drawable.scaleFromDensity(this.mInsetR, sourceDensity, targetDensity, false);
            this.mInsetB = Drawable.scaleFromDensity(this.mInsetB, sourceDensity, targetDensity, false);
            if (this.mInsetS != Integer.MIN_VALUE) {
                this.mInsetS = Drawable.scaleFromDensity(this.mInsetS, sourceDensity, targetDensity, false);
            }
            if (this.mInsetE != Integer.MIN_VALUE) {
                this.mInsetE = Drawable.scaleFromDensity(this.mInsetE, sourceDensity, targetDensity, false);
            }
            if (this.mWidth > 0) {
                this.mWidth = Drawable.scaleFromDensity(this.mWidth, sourceDensity, targetDensity, true);
            }
            if (this.mHeight > 0) {
                this.mHeight = Drawable.scaleFromDensity(this.mHeight, sourceDensity, targetDensity, true);
            }
        }
    }

    static class LayerState extends Drawable.ConstantState {
        /* access modifiers changed from: private */
        public boolean mAutoMirrored;
        int mChangingConfigurations;
        private boolean mCheckedOpacity;
        private boolean mCheckedStateful;
        ChildDrawable[] mChildren;
        int mChildrenChangingConfigurations;
        int mDensity;
        private boolean mIsStateful;
        int mNumChildren;
        private int mOpacity;
        int mOpacityOverride;
        int mPaddingBottom = -1;
        int mPaddingEnd = -1;
        int mPaddingLeft = -1;
        /* access modifiers changed from: private */
        public int mPaddingMode;
        int mPaddingRight = -1;
        int mPaddingStart = -1;
        int mPaddingTop = -1;
        /* access modifiers changed from: private */
        public int[] mThemeAttrs;

        LayerState(LayerState orig, LayerDrawable owner, Resources res) {
            this.mOpacityOverride = 0;
            this.mAutoMirrored = false;
            this.mPaddingMode = 0;
            this.mDensity = Drawable.resolveDensity(res, orig != null ? orig.mDensity : 0);
            if (orig != null) {
                ChildDrawable[] origChildDrawable = orig.mChildren;
                int N = orig.mNumChildren;
                this.mNumChildren = N;
                this.mChildren = new ChildDrawable[N];
                this.mChangingConfigurations = orig.mChangingConfigurations;
                this.mChildrenChangingConfigurations = orig.mChildrenChangingConfigurations;
                for (int i = 0; i < N; i++) {
                    this.mChildren[i] = new ChildDrawable(origChildDrawable[i], owner, res);
                }
                this.mCheckedOpacity = orig.mCheckedOpacity;
                this.mOpacity = orig.mOpacity;
                this.mCheckedStateful = orig.mCheckedStateful;
                this.mIsStateful = orig.mIsStateful;
                this.mAutoMirrored = orig.mAutoMirrored;
                this.mPaddingMode = orig.mPaddingMode;
                this.mThemeAttrs = orig.mThemeAttrs;
                this.mPaddingTop = orig.mPaddingTop;
                this.mPaddingBottom = orig.mPaddingBottom;
                this.mPaddingLeft = orig.mPaddingLeft;
                this.mPaddingRight = orig.mPaddingRight;
                this.mPaddingStart = orig.mPaddingStart;
                this.mPaddingEnd = orig.mPaddingEnd;
                this.mOpacityOverride = orig.mOpacityOverride;
                if (orig.mDensity != this.mDensity) {
                    applyDensityScaling(orig.mDensity, this.mDensity);
                    return;
                }
                return;
            }
            this.mNumChildren = 0;
            this.mChildren = null;
        }

        public final void setDensity(int targetDensity) {
            if (this.mDensity != targetDensity) {
                int sourceDensity = this.mDensity;
                this.mDensity = targetDensity;
                onDensityChanged(sourceDensity, targetDensity);
            }
        }

        /* access modifiers changed from: protected */
        public void onDensityChanged(int sourceDensity, int targetDensity) {
            applyDensityScaling(sourceDensity, targetDensity);
        }

        private void applyDensityScaling(int sourceDensity, int targetDensity) {
            if (this.mPaddingLeft > 0) {
                this.mPaddingLeft = Drawable.scaleFromDensity(this.mPaddingLeft, sourceDensity, targetDensity, false);
            }
            if (this.mPaddingTop > 0) {
                this.mPaddingTop = Drawable.scaleFromDensity(this.mPaddingTop, sourceDensity, targetDensity, false);
            }
            if (this.mPaddingRight > 0) {
                this.mPaddingRight = Drawable.scaleFromDensity(this.mPaddingRight, sourceDensity, targetDensity, false);
            }
            if (this.mPaddingBottom > 0) {
                this.mPaddingBottom = Drawable.scaleFromDensity(this.mPaddingBottom, sourceDensity, targetDensity, false);
            }
            if (this.mPaddingStart > 0) {
                this.mPaddingStart = Drawable.scaleFromDensity(this.mPaddingStart, sourceDensity, targetDensity, false);
            }
            if (this.mPaddingEnd > 0) {
                this.mPaddingEnd = Drawable.scaleFromDensity(this.mPaddingEnd, sourceDensity, targetDensity, false);
            }
        }

        public boolean canApplyTheme() {
            if (this.mThemeAttrs != null || super.canApplyTheme()) {
                return true;
            }
            ChildDrawable[] array = this.mChildren;
            int N = this.mNumChildren;
            for (int i = 0; i < N; i++) {
                if (array[i].canApplyTheme()) {
                    return true;
                }
            }
            return false;
        }

        public Drawable newDrawable() {
            return new LayerDrawable(this, (Resources) null);
        }

        public Drawable newDrawable(Resources res) {
            return new LayerDrawable(this, res);
        }

        public int getChangingConfigurations() {
            return this.mChangingConfigurations | this.mChildrenChangingConfigurations;
        }

        public final int getOpacity() {
            int op;
            if (this.mCheckedOpacity) {
                return this.mOpacity;
            }
            int N = this.mNumChildren;
            ChildDrawable[] array = this.mChildren;
            int firstIndex = -1;
            int i = 0;
            while (true) {
                if (i >= N) {
                    break;
                } else if (array[i].mDrawable != null) {
                    firstIndex = i;
                    break;
                } else {
                    i++;
                }
            }
            if (firstIndex >= 0) {
                op = array[firstIndex].mDrawable.getOpacity();
            } else {
                op = -2;
            }
            for (int i2 = firstIndex + 1; i2 < N; i2++) {
                Drawable dr = array[i2].mDrawable;
                if (dr != null) {
                    op = Drawable.resolveOpacity(op, dr.getOpacity());
                }
            }
            this.mOpacity = op;
            this.mCheckedOpacity = true;
            return op;
        }

        public final boolean isStateful() {
            if (this.mCheckedStateful) {
                return this.mIsStateful;
            }
            int N = this.mNumChildren;
            ChildDrawable[] array = this.mChildren;
            boolean isStateful = false;
            int i = 0;
            while (true) {
                if (i >= N) {
                    break;
                }
                Drawable dr = array[i].mDrawable;
                if (dr != null && dr.isStateful()) {
                    isStateful = true;
                    break;
                }
                i++;
            }
            this.mIsStateful = isStateful;
            this.mCheckedStateful = true;
            return isStateful;
        }

        public final boolean hasFocusStateSpecified() {
            int N = this.mNumChildren;
            ChildDrawable[] array = this.mChildren;
            for (int i = 0; i < N; i++) {
                Drawable dr = array[i].mDrawable;
                if (dr != null && dr.hasFocusStateSpecified()) {
                    return true;
                }
            }
            return false;
        }

        public final boolean canConstantState() {
            ChildDrawable[] array = this.mChildren;
            int N = this.mNumChildren;
            for (int i = 0; i < N; i++) {
                Drawable dr = array[i].mDrawable;
                if (dr != null && dr.getConstantState() == null) {
                    return false;
                }
            }
            return true;
        }

        /* access modifiers changed from: package-private */
        public void invalidateCache() {
            this.mCheckedOpacity = false;
            this.mCheckedStateful = false;
        }
    }

    public LayerDrawable(Drawable[] layers) {
        this(layers, (LayerState) null);
    }

    LayerDrawable(Drawable[] layers, LayerState state) {
        this(state, (Resources) null);
        if (layers != null) {
            int length = layers.length;
            ChildDrawable[] r = new ChildDrawable[length];
            for (int i = 0; i < length; i++) {
                r[i] = new ChildDrawable(this.mLayerState.mDensity);
                r[i].mDrawable = layers[i];
                layers[i].setCallback(this);
                this.mLayerState.mChildrenChangingConfigurations |= layers[i].getChangingConfigurations();
            }
            this.mLayerState.mNumChildren = length;
            this.mLayerState.mChildren = r;
            ensurePadding();
            refreshPadding();
            return;
        }
        throw new IllegalArgumentException("layers must be non-null");
    }

    LayerDrawable() {
        this((LayerState) null, (Resources) null);
    }

    LayerDrawable(LayerState state, Resources res) {
        this.mTmpRect = new Rect();
        this.mTmpOutRect = new Rect();
        this.mTmpContainer = new Rect();
        this.mLayerState = createConstantState(state, res);
        if (this.mLayerState.mNumChildren > 0) {
            ensurePadding();
            refreshPadding();
        }
    }

    /* access modifiers changed from: package-private */
    public LayerState createConstantState(LayerState state, Resources res) {
        return new LayerState(state, this, res);
    }

    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs, Resources.Theme theme) throws XmlPullParserException, IOException {
        super.inflate(r, parser, attrs, theme);
        LayerState state = this.mLayerState;
        int density = Drawable.resolveDensity(r, 0);
        state.setDensity(density);
        TypedArray a = obtainAttributes(r, theme, attrs, R.styleable.LayerDrawable);
        updateStateFromTypedArray(a);
        a.recycle();
        ChildDrawable[] array = state.mChildren;
        int N = state.mNumChildren;
        for (int i = 0; i < N; i++) {
            array[i].setDensity(density);
        }
        inflateLayers(r, parser, attrs, theme);
        ensurePadding();
        refreshPadding();
    }

    public void applyTheme(Resources.Theme t) {
        super.applyTheme(t);
        LayerState state = this.mLayerState;
        int density = Drawable.resolveDensity(t.getResources(), 0);
        state.setDensity(density);
        if (state.mThemeAttrs != null) {
            TypedArray a = t.resolveAttributes(state.mThemeAttrs, R.styleable.LayerDrawable);
            updateStateFromTypedArray(a);
            a.recycle();
        }
        ChildDrawable[] array = state.mChildren;
        int N = state.mNumChildren;
        for (int i = 0; i < N; i++) {
            ChildDrawable layer = array[i];
            layer.setDensity(density);
            if (layer.mThemeAttrs != null) {
                TypedArray a2 = t.resolveAttributes(layer.mThemeAttrs, R.styleable.LayerDrawableItem);
                updateLayerFromTypedArray(layer, a2);
                a2.recycle();
            }
            Drawable d = layer.mDrawable;
            if (d != null && d.canApplyTheme()) {
                d.applyTheme(t);
                state.mChildrenChangingConfigurations |= d.getChangingConfigurations();
            }
        }
    }

    private void inflateLayers(Resources r, XmlPullParser parser, AttributeSet attrs, Resources.Theme theme) throws XmlPullParserException, IOException {
        int next;
        int type;
        LayerState state = this.mLayerState;
        int innerDepth = parser.getDepth() + 1;
        while (true) {
            int next2 = parser.next();
            int type2 = next2;
            if (next2 != 1) {
                int depth = parser.getDepth();
                int depth2 = depth;
                if (depth < innerDepth && type2 == 3) {
                    return;
                }
                if (type2 == 2 && depth2 <= innerDepth && parser.getName().equals(HwThemeManager.TAG_ITEM)) {
                    ChildDrawable layer = new ChildDrawable(state.mDensity);
                    TypedArray a = obtainAttributes(r, theme, attrs, R.styleable.LayerDrawableItem);
                    updateLayerFromTypedArray(layer, a);
                    a.recycle();
                    if (layer.mDrawable == null && (layer.mThemeAttrs == null || layer.mThemeAttrs[4] == 0)) {
                        do {
                            next = parser.next();
                            type = next;
                        } while (next == 4);
                        if (type == 2) {
                            layer.mDrawable = Drawable.createFromXmlInner(r, parser, attrs, theme);
                            layer.mDrawable.setCallback(this);
                            state.mChildrenChangingConfigurations |= layer.mDrawable.getChangingConfigurations();
                        } else {
                            throw new XmlPullParserException(parser.getPositionDescription() + ": <item> tag requires a 'drawable' attribute or child tag defining a drawable");
                        }
                    }
                    addLayer(layer);
                }
            } else {
                return;
            }
        }
    }

    private void updateStateFromTypedArray(TypedArray a) {
        LayerState state = this.mLayerState;
        state.mChangingConfigurations |= a.getChangingConfigurations();
        int[] unused = state.mThemeAttrs = a.extractThemeAttrs();
        int N = a.getIndexCount();
        for (int i = 0; i < N; i++) {
            int attr = a.getIndex(i);
            switch (attr) {
                case 0:
                    state.mPaddingLeft = a.getDimensionPixelOffset(attr, state.mPaddingLeft);
                    break;
                case 1:
                    state.mPaddingTop = a.getDimensionPixelOffset(attr, state.mPaddingTop);
                    break;
                case 2:
                    state.mPaddingRight = a.getDimensionPixelOffset(attr, state.mPaddingRight);
                    break;
                case 3:
                    state.mPaddingBottom = a.getDimensionPixelOffset(attr, state.mPaddingBottom);
                    break;
                case 4:
                    state.mOpacityOverride = a.getInt(attr, state.mOpacityOverride);
                    break;
                case 5:
                    state.mPaddingStart = a.getDimensionPixelOffset(attr, state.mPaddingStart);
                    break;
                case 6:
                    state.mPaddingEnd = a.getDimensionPixelOffset(attr, state.mPaddingEnd);
                    break;
                case 7:
                    boolean unused2 = state.mAutoMirrored = a.getBoolean(attr, state.mAutoMirrored);
                    break;
                case 8:
                    int unused3 = state.mPaddingMode = a.getInteger(attr, state.mPaddingMode);
                    break;
            }
        }
    }

    private void updateLayerFromTypedArray(ChildDrawable layer, TypedArray a) {
        LayerState state = this.mLayerState;
        state.mChildrenChangingConfigurations |= a.getChangingConfigurations();
        layer.mThemeAttrs = a.extractThemeAttrs();
        int N = a.getIndexCount();
        for (int i = 0; i < N; i++) {
            int attr = a.getIndex(i);
            switch (attr) {
                case 0:
                    layer.mGravity = a.getInteger(attr, layer.mGravity);
                    break;
                case 1:
                    layer.mId = a.getResourceId(attr, layer.mId);
                    break;
                case 2:
                    layer.mHeight = a.getDimensionPixelSize(attr, layer.mHeight);
                    break;
                case 3:
                    layer.mWidth = a.getDimensionPixelSize(attr, layer.mWidth);
                    break;
                case 5:
                    layer.mInsetL = a.getDimensionPixelOffset(attr, layer.mInsetL);
                    break;
                case 6:
                    layer.mInsetT = a.getDimensionPixelOffset(attr, layer.mInsetT);
                    break;
                case 7:
                    layer.mInsetR = a.getDimensionPixelOffset(attr, layer.mInsetR);
                    break;
                case 8:
                    layer.mInsetB = a.getDimensionPixelOffset(attr, layer.mInsetB);
                    break;
                case 9:
                    layer.mInsetS = a.getDimensionPixelOffset(attr, layer.mInsetS);
                    break;
                case 10:
                    layer.mInsetE = a.getDimensionPixelOffset(attr, layer.mInsetE);
                    break;
            }
        }
        Drawable dr = a.getDrawable(4);
        if (dr != null) {
            if (layer.mDrawable != null) {
                layer.mDrawable.setCallback(null);
            }
            layer.mDrawable = dr;
            layer.mDrawable.setCallback(this);
            state.mChildrenChangingConfigurations |= layer.mDrawable.getChangingConfigurations();
        }
    }

    public boolean canApplyTheme() {
        return this.mLayerState.canApplyTheme() || super.canApplyTheme();
    }

    public boolean isProjected() {
        if (super.isProjected()) {
            return true;
        }
        ChildDrawable[] layers = this.mLayerState.mChildren;
        int N = this.mLayerState.mNumChildren;
        for (int i = 0; i < N; i++) {
            if (layers[i].mDrawable.isProjected()) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public int addLayer(ChildDrawable layer) {
        LayerState st = this.mLayerState;
        int N = st.mChildren != null ? st.mChildren.length : 0;
        int i = st.mNumChildren;
        if (i >= N) {
            ChildDrawable[] nu = new ChildDrawable[(N + 10)];
            if (i > 0) {
                System.arraycopy(st.mChildren, 0, nu, 0, i);
            }
            st.mChildren = nu;
        }
        st.mChildren[i] = layer;
        st.mNumChildren++;
        st.invalidateCache();
        return i;
    }

    /* access modifiers changed from: package-private */
    public ChildDrawable addLayer(Drawable dr, int[] themeAttrs, int id, int left, int top, int right, int bottom) {
        ChildDrawable childDrawable = createLayer(dr);
        childDrawable.mId = id;
        childDrawable.mThemeAttrs = themeAttrs;
        childDrawable.mDrawable.setAutoMirrored(isAutoMirrored());
        childDrawable.mInsetL = left;
        childDrawable.mInsetT = top;
        childDrawable.mInsetR = right;
        childDrawable.mInsetB = bottom;
        addLayer(childDrawable);
        this.mLayerState.mChildrenChangingConfigurations |= dr.getChangingConfigurations();
        dr.setCallback(this);
        return childDrawable;
    }

    private ChildDrawable createLayer(Drawable dr) {
        ChildDrawable layer = new ChildDrawable(this.mLayerState.mDensity);
        layer.mDrawable = dr;
        return layer;
    }

    public int addLayer(Drawable dr) {
        ChildDrawable layer = createLayer(dr);
        int index = addLayer(layer);
        ensurePadding();
        refreshChildPadding(index, layer);
        return index;
    }

    public Drawable findDrawableByLayerId(int id) {
        ChildDrawable[] layers = this.mLayerState.mChildren;
        for (int i = this.mLayerState.mNumChildren - 1; i >= 0; i--) {
            if (layers[i].mId == id) {
                return layers[i].mDrawable;
            }
        }
        return null;
    }

    public void setId(int index, int id) {
        this.mLayerState.mChildren[index].mId = id;
    }

    public int getId(int index) {
        if (index < this.mLayerState.mNumChildren) {
            return this.mLayerState.mChildren[index].mId;
        }
        throw new IndexOutOfBoundsException();
    }

    public int getNumberOfLayers() {
        return this.mLayerState.mNumChildren;
    }

    public boolean setDrawableByLayerId(int id, Drawable drawable) {
        int index = findIndexByLayerId(id);
        if (index < 0) {
            return false;
        }
        setDrawable(index, drawable);
        return true;
    }

    public int findIndexByLayerId(int id) {
        ChildDrawable[] layers = this.mLayerState.mChildren;
        int N = this.mLayerState.mNumChildren;
        for (int i = 0; i < N; i++) {
            if (layers[i].mId == id) {
                return i;
            }
        }
        return -1;
    }

    public void setDrawable(int index, Drawable drawable) {
        if (index < this.mLayerState.mNumChildren) {
            ChildDrawable childDrawable = this.mLayerState.mChildren[index];
            if (childDrawable.mDrawable != null) {
                if (drawable != null) {
                    drawable.setBounds(childDrawable.mDrawable.getBounds());
                }
                childDrawable.mDrawable.setCallback(null);
            }
            if (drawable != null) {
                drawable.setCallback(this);
            }
            childDrawable.mDrawable = drawable;
            this.mLayerState.invalidateCache();
            refreshChildPadding(index, childDrawable);
            return;
        }
        throw new IndexOutOfBoundsException();
    }

    public Drawable getDrawable(int index) {
        if (index < this.mLayerState.mNumChildren) {
            return this.mLayerState.mChildren[index].mDrawable;
        }
        throw new IndexOutOfBoundsException();
    }

    public void setLayerSize(int index, int w, int h) {
        ChildDrawable childDrawable = this.mLayerState.mChildren[index];
        childDrawable.mWidth = w;
        childDrawable.mHeight = h;
    }

    public void setLayerWidth(int index, int w) {
        this.mLayerState.mChildren[index].mWidth = w;
    }

    public int getLayerWidth(int index) {
        return this.mLayerState.mChildren[index].mWidth;
    }

    public void setLayerHeight(int index, int h) {
        this.mLayerState.mChildren[index].mHeight = h;
    }

    public int getLayerHeight(int index) {
        return this.mLayerState.mChildren[index].mHeight;
    }

    public void setLayerGravity(int index, int gravity) {
        this.mLayerState.mChildren[index].mGravity = gravity;
    }

    public int getLayerGravity(int index) {
        return this.mLayerState.mChildren[index].mGravity;
    }

    public void setLayerInset(int index, int l, int t, int r, int b) {
        setLayerInsetInternal(index, l, t, r, b, Integer.MIN_VALUE, Integer.MIN_VALUE);
    }

    public void setLayerInsetRelative(int index, int s, int t, int e, int b) {
        setLayerInsetInternal(index, 0, t, 0, b, s, e);
    }

    public void setLayerInsetLeft(int index, int l) {
        this.mLayerState.mChildren[index].mInsetL = l;
    }

    public int getLayerInsetLeft(int index) {
        return this.mLayerState.mChildren[index].mInsetL;
    }

    public void setLayerInsetRight(int index, int r) {
        this.mLayerState.mChildren[index].mInsetR = r;
    }

    public int getLayerInsetRight(int index) {
        return this.mLayerState.mChildren[index].mInsetR;
    }

    public void setLayerInsetTop(int index, int t) {
        this.mLayerState.mChildren[index].mInsetT = t;
    }

    public int getLayerInsetTop(int index) {
        return this.mLayerState.mChildren[index].mInsetT;
    }

    public void setLayerInsetBottom(int index, int b) {
        this.mLayerState.mChildren[index].mInsetB = b;
    }

    public int getLayerInsetBottom(int index) {
        return this.mLayerState.mChildren[index].mInsetB;
    }

    public void setLayerInsetStart(int index, int s) {
        this.mLayerState.mChildren[index].mInsetS = s;
    }

    public int getLayerInsetStart(int index) {
        return this.mLayerState.mChildren[index].mInsetS;
    }

    public void setLayerInsetEnd(int index, int e) {
        this.mLayerState.mChildren[index].mInsetE = e;
    }

    public int getLayerInsetEnd(int index) {
        return this.mLayerState.mChildren[index].mInsetE;
    }

    private void setLayerInsetInternal(int index, int l, int t, int r, int b, int s, int e) {
        ChildDrawable childDrawable = this.mLayerState.mChildren[index];
        childDrawable.mInsetL = l;
        childDrawable.mInsetT = t;
        childDrawable.mInsetR = r;
        childDrawable.mInsetB = b;
        childDrawable.mInsetS = s;
        childDrawable.mInsetE = e;
    }

    public void setPaddingMode(int mode) {
        if (this.mLayerState.mPaddingMode != mode) {
            int unused = this.mLayerState.mPaddingMode = mode;
        }
    }

    public int getPaddingMode() {
        return this.mLayerState.mPaddingMode;
    }

    private void suspendChildInvalidation() {
        this.mSuspendChildInvalidation = true;
    }

    private void resumeChildInvalidation() {
        this.mSuspendChildInvalidation = false;
        if (this.mChildRequestedInvalidation) {
            this.mChildRequestedInvalidation = false;
            invalidateSelf();
        }
    }

    public void invalidateDrawable(Drawable who) {
        if (this.mSuspendChildInvalidation) {
            this.mChildRequestedInvalidation = true;
            return;
        }
        this.mLayerState.invalidateCache();
        invalidateSelf();
    }

    public void scheduleDrawable(Drawable who, Runnable what, long when) {
        scheduleSelf(what, when);
    }

    public void unscheduleDrawable(Drawable who, Runnable what) {
        unscheduleSelf(what);
    }

    public void draw(Canvas canvas) {
        ChildDrawable[] array = this.mLayerState.mChildren;
        int N = this.mLayerState.mNumChildren;
        for (int i = 0; i < N; i++) {
            Drawable dr = array[i].mDrawable;
            if (dr != null) {
                dr.draw(canvas);
            }
        }
    }

    public int getChangingConfigurations() {
        return super.getChangingConfigurations() | this.mLayerState.getChangingConfigurations();
    }

    public boolean getPadding(Rect padding) {
        LayerState layerState = this.mLayerState;
        if (layerState.mPaddingMode == 0) {
            computeNestedPadding(padding);
        } else {
            computeStackedPadding(padding);
        }
        int paddingT = layerState.mPaddingTop;
        int paddingB = layerState.mPaddingBottom;
        boolean isLayoutRtl = getLayoutDirection() == 1;
        int paddingRtlL = isLayoutRtl ? layerState.mPaddingEnd : layerState.mPaddingStart;
        int paddingRtlR = isLayoutRtl ? layerState.mPaddingStart : layerState.mPaddingEnd;
        int paddingL = paddingRtlL >= 0 ? paddingRtlL : layerState.mPaddingLeft;
        int paddingR = paddingRtlR >= 0 ? paddingRtlR : layerState.mPaddingRight;
        if (paddingL >= 0) {
            padding.left = paddingL;
        }
        if (paddingT >= 0) {
            padding.top = paddingT;
        }
        if (paddingR >= 0) {
            padding.right = paddingR;
        }
        if (paddingB >= 0) {
            padding.bottom = paddingB;
        }
        if (padding.left == 0 && padding.top == 0 && padding.right == 0 && padding.bottom == 0) {
            return false;
        }
        return true;
    }

    public void setPadding(int left, int top, int right, int bottom) {
        LayerState layerState = this.mLayerState;
        layerState.mPaddingLeft = left;
        layerState.mPaddingTop = top;
        layerState.mPaddingRight = right;
        layerState.mPaddingBottom = bottom;
        layerState.mPaddingStart = -1;
        layerState.mPaddingEnd = -1;
    }

    public void setPaddingRelative(int start, int top, int end, int bottom) {
        LayerState layerState = this.mLayerState;
        layerState.mPaddingStart = start;
        layerState.mPaddingTop = top;
        layerState.mPaddingEnd = end;
        layerState.mPaddingBottom = bottom;
        layerState.mPaddingLeft = -1;
        layerState.mPaddingRight = -1;
    }

    public int getLeftPadding() {
        return this.mLayerState.mPaddingLeft;
    }

    public int getRightPadding() {
        return this.mLayerState.mPaddingRight;
    }

    public int getStartPadding() {
        return this.mLayerState.mPaddingStart;
    }

    public int getEndPadding() {
        return this.mLayerState.mPaddingEnd;
    }

    public int getTopPadding() {
        return this.mLayerState.mPaddingTop;
    }

    public int getBottomPadding() {
        return this.mLayerState.mPaddingBottom;
    }

    private void computeNestedPadding(Rect padding) {
        padding.left = 0;
        padding.top = 0;
        padding.right = 0;
        padding.bottom = 0;
        ChildDrawable[] array = this.mLayerState.mChildren;
        int N = this.mLayerState.mNumChildren;
        for (int i = 0; i < N; i++) {
            refreshChildPadding(i, array[i]);
            padding.left += this.mPaddingL[i];
            padding.top += this.mPaddingT[i];
            padding.right += this.mPaddingR[i];
            padding.bottom += this.mPaddingB[i];
        }
    }

    private void computeStackedPadding(Rect padding) {
        padding.left = 0;
        padding.top = 0;
        padding.right = 0;
        padding.bottom = 0;
        ChildDrawable[] array = this.mLayerState.mChildren;
        int N = this.mLayerState.mNumChildren;
        for (int i = 0; i < N; i++) {
            refreshChildPadding(i, array[i]);
            padding.left = Math.max(padding.left, this.mPaddingL[i]);
            padding.top = Math.max(padding.top, this.mPaddingT[i]);
            padding.right = Math.max(padding.right, this.mPaddingR[i]);
            padding.bottom = Math.max(padding.bottom, this.mPaddingB[i]);
        }
    }

    public void getOutline(Outline outline) {
        ChildDrawable[] array = this.mLayerState.mChildren;
        int N = this.mLayerState.mNumChildren;
        for (int i = 0; i < N; i++) {
            Drawable dr = array[i].mDrawable;
            if (dr != null) {
                dr.getOutline(outline);
                if (!outline.isEmpty()) {
                    return;
                }
            }
        }
    }

    public void setHotspot(float x, float y) {
        ChildDrawable[] array = this.mLayerState.mChildren;
        int N = this.mLayerState.mNumChildren;
        for (int i = 0; i < N; i++) {
            Drawable dr = array[i].mDrawable;
            if (dr != null) {
                dr.setHotspot(x, y);
            }
        }
    }

    public void setHotspotBounds(int left, int top, int right, int bottom) {
        ChildDrawable[] array = this.mLayerState.mChildren;
        int N = this.mLayerState.mNumChildren;
        for (int i = 0; i < N; i++) {
            Drawable dr = array[i].mDrawable;
            if (dr != null) {
                dr.setHotspotBounds(left, top, right, bottom);
            }
        }
        if (this.mHotspotBounds == null) {
            this.mHotspotBounds = new Rect(left, top, right, bottom);
        } else {
            this.mHotspotBounds.set(left, top, right, bottom);
        }
    }

    public void getHotspotBounds(Rect outRect) {
        if (this.mHotspotBounds != null) {
            outRect.set(this.mHotspotBounds);
        } else {
            super.getHotspotBounds(outRect);
        }
    }

    public boolean setVisible(boolean visible, boolean restart) {
        boolean changed = super.setVisible(visible, restart);
        ChildDrawable[] array = this.mLayerState.mChildren;
        int N = this.mLayerState.mNumChildren;
        for (int i = 0; i < N; i++) {
            Drawable dr = array[i].mDrawable;
            if (dr != null) {
                dr.setVisible(visible, restart);
            }
        }
        return changed;
    }

    public void setDither(boolean dither) {
        ChildDrawable[] array = this.mLayerState.mChildren;
        int N = this.mLayerState.mNumChildren;
        for (int i = 0; i < N; i++) {
            Drawable dr = array[i].mDrawable;
            if (dr != null) {
                dr.setDither(dither);
            }
        }
    }

    public void setAlpha(int alpha) {
        ChildDrawable[] array = this.mLayerState.mChildren;
        int N = this.mLayerState.mNumChildren;
        for (int i = 0; i < N; i++) {
            Drawable dr = array[i].mDrawable;
            if (dr != null) {
                dr.setAlpha(alpha);
            }
        }
    }

    public int getAlpha() {
        Drawable dr = getFirstNonNullDrawable();
        if (dr != null) {
            return dr.getAlpha();
        }
        return super.getAlpha();
    }

    public void setColorFilter(ColorFilter colorFilter) {
        ChildDrawable[] array = this.mLayerState.mChildren;
        int N = this.mLayerState.mNumChildren;
        for (int i = 0; i < N; i++) {
            Drawable dr = array[i].mDrawable;
            if (dr != null) {
                dr.setColorFilter(colorFilter);
            }
        }
    }

    public void setTintList(ColorStateList tint) {
        ChildDrawable[] array = this.mLayerState.mChildren;
        int N = this.mLayerState.mNumChildren;
        for (int i = 0; i < N; i++) {
            Drawable dr = array[i].mDrawable;
            if (dr != null) {
                dr.setTintList(tint);
            }
        }
    }

    public void setTintMode(PorterDuff.Mode tintMode) {
        ChildDrawable[] array = this.mLayerState.mChildren;
        int N = this.mLayerState.mNumChildren;
        for (int i = 0; i < N; i++) {
            Drawable dr = array[i].mDrawable;
            if (dr != null) {
                dr.setTintMode(tintMode);
            }
        }
    }

    private Drawable getFirstNonNullDrawable() {
        ChildDrawable[] array = this.mLayerState.mChildren;
        int N = this.mLayerState.mNumChildren;
        for (int i = 0; i < N; i++) {
            Drawable dr = array[i].mDrawable;
            if (dr != null) {
                return dr;
            }
        }
        return null;
    }

    public void setOpacity(int opacity) {
        this.mLayerState.mOpacityOverride = opacity;
    }

    public int getOpacity() {
        if (this.mLayerState.mOpacityOverride != 0) {
            return this.mLayerState.mOpacityOverride;
        }
        return this.mLayerState.getOpacity();
    }

    public void setAutoMirrored(boolean mirrored) {
        boolean unused = this.mLayerState.mAutoMirrored = mirrored;
        ChildDrawable[] array = this.mLayerState.mChildren;
        int N = this.mLayerState.mNumChildren;
        for (int i = 0; i < N; i++) {
            Drawable dr = array[i].mDrawable;
            if (dr != null) {
                dr.setAutoMirrored(mirrored);
            }
        }
    }

    public boolean isAutoMirrored() {
        return this.mLayerState.mAutoMirrored;
    }

    public void jumpToCurrentState() {
        ChildDrawable[] array = this.mLayerState.mChildren;
        int N = this.mLayerState.mNumChildren;
        for (int i = 0; i < N; i++) {
            Drawable dr = array[i].mDrawable;
            if (dr != null) {
                dr.jumpToCurrentState();
            }
        }
    }

    public boolean isStateful() {
        return this.mLayerState.isStateful();
    }

    public boolean hasFocusStateSpecified() {
        return this.mLayerState.hasFocusStateSpecified();
    }

    /* access modifiers changed from: protected */
    public boolean onStateChange(int[] state) {
        boolean changed = false;
        ChildDrawable[] array = this.mLayerState.mChildren;
        int N = this.mLayerState.mNumChildren;
        for (int i = 0; i < N; i++) {
            Drawable dr = array[i].mDrawable;
            if (dr != null && dr.isStateful() && dr.setState(state)) {
                refreshChildPadding(i, array[i]);
                changed = true;
            }
        }
        if (changed) {
            updateLayerBounds(getBounds());
        }
        return changed;
    }

    /* access modifiers changed from: protected */
    public boolean onLevelChange(int level) {
        boolean changed = false;
        ChildDrawable[] array = this.mLayerState.mChildren;
        int N = this.mLayerState.mNumChildren;
        for (int i = 0; i < N; i++) {
            Drawable dr = array[i].mDrawable;
            if (dr != null && dr.setLevel(level)) {
                refreshChildPadding(i, array[i]);
                changed = true;
            }
        }
        if (changed) {
            updateLayerBounds(getBounds());
        }
        return changed;
    }

    /* access modifiers changed from: protected */
    public void onBoundsChange(Rect bounds) {
        updateLayerBounds(bounds);
    }

    private void updateLayerBounds(Rect bounds) {
        try {
            suspendChildInvalidation();
            updateLayerBoundsInternal(bounds);
        } finally {
            resumeChildInvalidation();
        }
    }

    private void updateLayerBoundsInternal(Rect bounds) {
        boolean isLayoutRtl;
        ChildDrawable[] array;
        int count;
        int paddingT;
        int count2;
        int insetRtlR;
        Rect rect = bounds;
        Rect outRect = this.mTmpOutRect;
        int layoutDirection = getLayoutDirection();
        boolean z = false;
        boolean isLayoutRtl2 = layoutDirection == 1;
        if (this.mLayerState.mPaddingMode == 0) {
            z = true;
        }
        boolean isPaddingNested = z;
        ChildDrawable[] array2 = this.mLayerState.mChildren;
        int count3 = this.mLayerState.mNumChildren;
        int paddingB = 0;
        int paddingR = 0;
        int paddingT2 = 0;
        int paddingL = 0;
        int i = 0;
        while (true) {
            int count4 = count3;
            if (i < count4) {
                ChildDrawable r = array2[i];
                Drawable d = r.mDrawable;
                if (d == null) {
                    paddingT = paddingT2;
                    count = count4;
                    array = array2;
                    isLayoutRtl = isLayoutRtl2;
                } else {
                    int insetT = r.mInsetT;
                    int insetB = r.mInsetB;
                    if (isLayoutRtl2) {
                        count = count4;
                        count2 = r.mInsetE;
                    } else {
                        count = count4;
                        count2 = r.mInsetS;
                    }
                    if (isLayoutRtl2) {
                        array = array2;
                        insetRtlR = r.mInsetS;
                    } else {
                        array = array2;
                        insetRtlR = r.mInsetE;
                    }
                    isLayoutRtl = isLayoutRtl2;
                    int insetL = count2 == Integer.MIN_VALUE ? r.mInsetL : count2;
                    int i2 = count2;
                    int insetR = insetRtlR == Integer.MIN_VALUE ? r.mInsetR : insetRtlR;
                    Rect container = this.mTmpContainer;
                    int i3 = insetRtlR;
                    int i4 = insetL;
                    int i5 = insetT;
                    paddingT = paddingT2;
                    container.set(rect.left + insetL + paddingL, rect.top + insetT + paddingT2, (rect.right - insetR) - paddingR, (rect.bottom - insetB) - paddingB);
                    int intrinsicW = d.getIntrinsicWidth();
                    int intrinsicH = d.getIntrinsicHeight();
                    int layerW = r.mWidth;
                    int layerH = r.mHeight;
                    int i6 = insetB;
                    int i7 = layerH;
                    int i8 = intrinsicW;
                    ChildDrawable childDrawable = r;
                    Rect rect2 = container;
                    int i9 = layerW;
                    Gravity.apply(resolveGravity(r.mGravity, layerW, layerH, intrinsicW, intrinsicH), layerW < 0 ? intrinsicW : layerW, layerH < 0 ? intrinsicH : layerH, container, outRect, layoutDirection);
                    d.setBounds(outRect);
                    if (isPaddingNested) {
                        paddingL += this.mPaddingL[i];
                        paddingR += this.mPaddingR[i];
                        paddingB += this.mPaddingB[i];
                        paddingT2 = paddingT + this.mPaddingT[i];
                        i++;
                        count3 = count;
                        array2 = array;
                        isLayoutRtl2 = isLayoutRtl;
                        rect = bounds;
                    }
                }
                paddingT2 = paddingT;
                i++;
                count3 = count;
                array2 = array;
                isLayoutRtl2 = isLayoutRtl;
                rect = bounds;
            } else {
                int i10 = paddingT2;
                ChildDrawable[] childDrawableArr = array2;
                boolean z2 = isLayoutRtl2;
                return;
            }
        }
    }

    private static int resolveGravity(int gravity, int width, int height, int intrinsicWidth, int intrinsicHeight) {
        if (!Gravity.isHorizontal(gravity)) {
            if (width < 0) {
                gravity |= 7;
            } else {
                gravity |= 8388611;
            }
        }
        if (!Gravity.isVertical(gravity)) {
            if (height < 0) {
                gravity |= 112;
            } else {
                gravity |= 48;
            }
        }
        if (width < 0 && intrinsicWidth < 0) {
            gravity |= 7;
        }
        if (height >= 0 || intrinsicHeight >= 0) {
            return gravity;
        }
        return gravity | 112;
    }

    public int getIntrinsicWidth() {
        int width = -1;
        int padL = 0;
        int padR = 0;
        boolean isLayoutRtl = true;
        boolean nest = this.mLayerState.mPaddingMode == 0;
        if (getLayoutDirection() != 1) {
            isLayoutRtl = false;
        }
        ChildDrawable[] array = this.mLayerState.mChildren;
        int N = this.mLayerState.mNumChildren;
        for (int i = 0; i < N; i++) {
            ChildDrawable r = array[i];
            if (r.mDrawable != null) {
                int insetRtlL = isLayoutRtl ? r.mInsetE : r.mInsetS;
                int insetRtlR = isLayoutRtl ? r.mInsetS : r.mInsetE;
                int insetL = insetRtlL == Integer.MIN_VALUE ? r.mInsetL : insetRtlL;
                int insetR = insetRtlR == Integer.MIN_VALUE ? r.mInsetR : insetRtlR;
                int minWidth = r.mWidth < 0 ? r.mDrawable.getIntrinsicWidth() : r.mWidth;
                int w = minWidth < 0 ? -1 : minWidth + insetL + insetR + padL + padR;
                if (w > width) {
                    width = w;
                }
                if (nest) {
                    padL += this.mPaddingL[i];
                    padR += this.mPaddingR[i];
                    width = width;
                }
            }
        }
        return width;
    }

    public int getIntrinsicHeight() {
        int height = -1;
        int padT = 0;
        int padB = 0;
        boolean nest = this.mLayerState.mPaddingMode == 0;
        ChildDrawable[] array = this.mLayerState.mChildren;
        int N = this.mLayerState.mNumChildren;
        for (int i = 0; i < N; i++) {
            ChildDrawable r = array[i];
            if (r.mDrawable != null) {
                int minHeight = r.mHeight < 0 ? r.mDrawable.getIntrinsicHeight() : r.mHeight;
                int h = minHeight < 0 ? -1 : r.mInsetT + minHeight + r.mInsetB + padT + padB;
                if (h > height) {
                    height = h;
                }
                if (nest) {
                    padT += this.mPaddingT[i];
                    padB += this.mPaddingB[i];
                }
            }
        }
        return height;
    }

    private boolean refreshChildPadding(int i, ChildDrawable r) {
        if (r.mDrawable != null) {
            Rect rect = this.mTmpRect;
            r.mDrawable.getPadding(rect);
            if (!(rect.left == this.mPaddingL[i] && rect.top == this.mPaddingT[i] && rect.right == this.mPaddingR[i] && rect.bottom == this.mPaddingB[i])) {
                this.mPaddingL[i] = rect.left;
                this.mPaddingT[i] = rect.top;
                this.mPaddingR[i] = rect.right;
                this.mPaddingB[i] = rect.bottom;
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void ensurePadding() {
        int N = this.mLayerState.mNumChildren;
        if (this.mPaddingL == null || this.mPaddingL.length < N) {
            this.mPaddingL = new int[N];
            this.mPaddingT = new int[N];
            this.mPaddingR = new int[N];
            this.mPaddingB = new int[N];
        }
    }

    /* access modifiers changed from: package-private */
    public void refreshPadding() {
        int N = this.mLayerState.mNumChildren;
        ChildDrawable[] array = this.mLayerState.mChildren;
        for (int i = 0; i < N; i++) {
            refreshChildPadding(i, array[i]);
        }
    }

    public Drawable.ConstantState getConstantState() {
        if (!this.mLayerState.canConstantState()) {
            return null;
        }
        this.mLayerState.mChangingConfigurations = getChangingConfigurations();
        return this.mLayerState;
    }

    public Drawable mutate() {
        if (!this.mMutated && super.mutate() == this) {
            this.mLayerState = createConstantState(this.mLayerState, null);
            ChildDrawable[] array = this.mLayerState.mChildren;
            int N = this.mLayerState.mNumChildren;
            for (int i = 0; i < N; i++) {
                Drawable dr = array[i].mDrawable;
                if (dr != null) {
                    dr.mutate();
                }
            }
            this.mMutated = true;
        }
        return this;
    }

    public void clearMutated() {
        super.clearMutated();
        ChildDrawable[] array = this.mLayerState.mChildren;
        int N = this.mLayerState.mNumChildren;
        for (int i = 0; i < N; i++) {
            Drawable dr = array[i].mDrawable;
            if (dr != null) {
                dr.clearMutated();
            }
        }
        this.mMutated = false;
    }

    public boolean onLayoutDirectionChanged(int layoutDirection) {
        boolean changed = false;
        ChildDrawable[] array = this.mLayerState.mChildren;
        int N = this.mLayerState.mNumChildren;
        for (int i = 0; i < N; i++) {
            Drawable dr = array[i].mDrawable;
            if (dr != null) {
                changed |= dr.setLayoutDirection(layoutDirection);
            }
        }
        updateLayerBounds(getBounds());
        return changed;
    }
}
