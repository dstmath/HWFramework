package android.content.res;

import android.bluetooth.le.AdvertisingSetParameters;
import android.content.res.Resources;
import android.graphics.Color;
import android.hwtheme.HwThemeManager;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.MathUtils;
import android.util.SparseArray;
import android.util.StateSet;
import android.util.Xml;
import com.android.internal.R;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.GrowingArrayUtils;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class ColorStateList extends ComplexColor implements Parcelable {
    public static final Parcelable.Creator<ColorStateList> CREATOR = new Parcelable.Creator<ColorStateList>() {
        public ColorStateList[] newArray(int size) {
            return new ColorStateList[size];
        }

        public ColorStateList createFromParcel(Parcel source) {
            int N = source.readInt();
            int[][] stateSpecs = new int[N][];
            for (int i = 0; i < N; i++) {
                stateSpecs[i] = source.createIntArray();
            }
            return new ColorStateList(stateSpecs, source.createIntArray());
        }
    };
    private static final int DEFAULT_COLOR = -65536;
    private static final int[][] EMPTY = {new int[0]};
    private static final String TAG = "ColorStateList";
    private static final SparseArray<WeakReference<ColorStateList>> sCache = new SparseArray<>();
    /* access modifiers changed from: private */
    public int mChangingConfigurations;
    private int[] mColors;
    private int mDefaultColor;
    private ColorStateListFactory mFactory;
    private boolean mIsOpaque;
    private int[][] mStateSpecs;
    private int[][] mThemeAttrs;

    private static class ColorStateListFactory extends ConstantState<ComplexColor> {
        private final ColorStateList mSrc;

        public ColorStateListFactory(ColorStateList src) {
            this.mSrc = src;
        }

        public int getChangingConfigurations() {
            return this.mSrc.mChangingConfigurations;
        }

        public ColorStateList newInstance() {
            return this.mSrc;
        }

        public ColorStateList newInstance(Resources res, Resources.Theme theme) {
            return this.mSrc.obtainForTheme(theme);
        }
    }

    private ColorStateList() {
    }

    public ColorStateList(int[][] states, int[] colors) {
        this.mStateSpecs = states;
        this.mColors = colors;
        onColorsChanged();
    }

    public static ColorStateList valueOf(int color) {
        synchronized (sCache) {
            int index = sCache.indexOfKey(color);
            if (index >= 0) {
                ColorStateList cached = (ColorStateList) sCache.valueAt(index).get();
                if (cached != null) {
                    return cached;
                }
                sCache.removeAt(index);
            }
            for (int i = sCache.size() - 1; i >= 0; i--) {
                if (sCache.valueAt(i).get() == null) {
                    sCache.removeAt(i);
                }
            }
            ColorStateList csl = new ColorStateList(EMPTY, new int[]{color});
            sCache.put(color, new WeakReference(csl));
            return csl;
        }
    }

    private ColorStateList(ColorStateList orig) {
        if (orig != null) {
            this.mChangingConfigurations = orig.mChangingConfigurations;
            this.mStateSpecs = orig.mStateSpecs;
            this.mDefaultColor = orig.mDefaultColor;
            this.mIsOpaque = orig.mIsOpaque;
            this.mThemeAttrs = (int[][]) orig.mThemeAttrs.clone();
            this.mColors = (int[]) orig.mColors.clone();
        }
    }

    @Deprecated
    public static ColorStateList createFromXml(Resources r, XmlPullParser parser) throws XmlPullParserException, IOException {
        return createFromXml(r, parser, null);
    }

    public static ColorStateList createFromXml(Resources r, XmlPullParser parser, Resources.Theme theme) throws XmlPullParserException, IOException {
        int type;
        AttributeSet attrs = Xml.asAttributeSet(parser);
        do {
            int next = parser.next();
            type = next;
            if (next == 2) {
                break;
            }
        } while (type != 1);
        if (type == 2) {
            return createFromXmlInner(r, parser, attrs, theme);
        }
        throw new XmlPullParserException("No start tag found");
    }

    static ColorStateList createFromXmlInner(Resources r, XmlPullParser parser, AttributeSet attrs, Resources.Theme theme) throws XmlPullParserException, IOException {
        String name = parser.getName();
        if (name.equals("selector")) {
            ColorStateList colorStateList = new ColorStateList();
            colorStateList.inflate(r, parser, attrs, theme);
            return colorStateList;
        }
        throw new XmlPullParserException(parser.getPositionDescription() + ": invalid color state list tag " + name);
    }

    public ColorStateList withAlpha(int alpha) {
        int[] colors = new int[this.mColors.length];
        int len = colors.length;
        for (int i = 0; i < len; i++) {
            colors[i] = (this.mColors[i] & AdvertisingSetParameters.INTERVAL_MAX) | (alpha << 24);
        }
        return new ColorStateList(this.mStateSpecs, colors);
    }

    /* JADX WARNING: type inference failed for: r14v3, types: [java.lang.Object[]] */
    /* JADX WARNING: type inference failed for: r14v4, types: [java.lang.Object[]] */
    /* JADX WARNING: Multi-variable type inference failed */
    private void inflate(Resources r, XmlPullParser parser, AttributeSet attrs, Resources.Theme theme) throws XmlPullParserException, IOException {
        int innerDepth;
        int i;
        AttributeSet attributeSet = attrs;
        int i2 = 1;
        int innerDepth2 = parser.getDepth() + 1;
        int[][] stateSpecList = (int[][]) ArrayUtils.newUnpaddedArray(int[].class, 20);
        int[][] themeAttrsList = new int[stateSpecList.length][];
        int[] colorList = new int[stateSpecList.length];
        int[][] stateSpecList2 = stateSpecList;
        boolean hasUnresolvedAttrs = false;
        int defaultColor = -65536;
        int changingConfigurations = 0;
        int listSize = 0;
        while (true) {
            int next = parser.next();
            int type = next;
            if (next == i2) {
                int i3 = innerDepth2;
                int i4 = type;
                break;
            }
            int depth = parser.getDepth();
            int i5 = depth;
            if (depth < innerDepth2 && type == 3) {
                int i6 = innerDepth2;
                int i7 = type;
                break;
            }
            if (type != 2 || i5 > innerDepth2) {
                innerDepth = innerDepth2;
            } else if (!parser.getName().equals(HwThemeManager.TAG_ITEM)) {
                innerDepth = innerDepth2;
            } else {
                TypedArray a = Resources.obtainAttributes(r, theme, attributeSet, R.styleable.ColorStateListItem);
                int[] themeAttrs = a.extractThemeAttrs();
                int innerDepth3 = innerDepth2;
                int baseColor = a.getColor(0, Color.MAGENTA);
                int i8 = type;
                float alphaMod = a.getFloat(1, 1.0f);
                a.recycle();
                int numAttrs = attrs.getAttributeCount();
                int changingConfigurations2 = changingConfigurations | a.getChangingConfigurations();
                int[] stateSpec = new int[numAttrs];
                TypedArray typedArray = a;
                int j = 0;
                int j2 = 0;
                while (true) {
                    int depth2 = i5;
                    int i9 = j2;
                    if (i9 >= numAttrs) {
                        break;
                    }
                    int numAttrs2 = numAttrs;
                    int stateResId = attributeSet.getAttributeNameResource(i9);
                    if (!(stateResId == 16843173 || stateResId == 16843551)) {
                        int j3 = j + 1;
                        if (attributeSet.getAttributeBooleanValue(i9, false)) {
                            i = stateResId;
                        } else {
                            i = -stateResId;
                        }
                        stateSpec[j] = i;
                        j = j3;
                    }
                    j2 = i9 + 1;
                    i5 = depth2;
                    numAttrs = numAttrs2;
                    Resources resources = r;
                }
                int[] stateSpec2 = StateSet.trimStateSet(stateSpec, j);
                int color = modulateColorAlpha(baseColor, alphaMod);
                if (listSize == 0 || stateSpec2.length == 0) {
                    defaultColor = color;
                }
                if (themeAttrs != null) {
                    hasUnresolvedAttrs = true;
                }
                colorList = GrowingArrayUtils.append(colorList, listSize, color);
                themeAttrsList = GrowingArrayUtils.append(themeAttrsList, listSize, themeAttrs);
                stateSpecList2 = GrowingArrayUtils.append(stateSpecList2, listSize, stateSpec2);
                listSize++;
                innerDepth2 = innerDepth3;
                changingConfigurations = changingConfigurations2;
                i2 = 1;
            }
            innerDepth2 = innerDepth;
            i2 = 1;
        }
        this.mChangingConfigurations = changingConfigurations;
        this.mDefaultColor = defaultColor;
        if (hasUnresolvedAttrs) {
            this.mThemeAttrs = new int[listSize][];
            System.arraycopy(themeAttrsList, 0, this.mThemeAttrs, 0, listSize);
        } else {
            this.mThemeAttrs = null;
        }
        this.mColors = new int[listSize];
        this.mStateSpecs = new int[listSize][];
        System.arraycopy(colorList, 0, this.mColors, 0, listSize);
        System.arraycopy(stateSpecList2, 0, this.mStateSpecs, 0, listSize);
        onColorsChanged();
    }

    public boolean canApplyTheme() {
        return this.mThemeAttrs != null;
    }

    private void applyTheme(Resources.Theme t) {
        float defaultAlphaMod;
        if (this.mThemeAttrs != null) {
            int[][] themeAttrsList = this.mThemeAttrs;
            int N = themeAttrsList.length;
            boolean hasUnresolvedAttrs = false;
            for (int i = 0; i < N; i++) {
                if (themeAttrsList[i] != null) {
                    TypedArray a = t.resolveAttributes(themeAttrsList[i], R.styleable.ColorStateListItem);
                    if (themeAttrsList[i][0] != 0) {
                        defaultAlphaMod = ((float) Color.alpha(this.mColors[i])) / 255.0f;
                    } else {
                        defaultAlphaMod = 1.0f;
                    }
                    themeAttrsList[i] = a.extractThemeAttrs(themeAttrsList[i]);
                    if (themeAttrsList[i] != null) {
                        hasUnresolvedAttrs = true;
                    }
                    this.mColors[i] = modulateColorAlpha(a.getColor(0, this.mColors[i]), a.getFloat(1, defaultAlphaMod));
                    this.mChangingConfigurations |= a.getChangingConfigurations();
                    a.recycle();
                }
            }
            if (!hasUnresolvedAttrs) {
                this.mThemeAttrs = null;
            }
            onColorsChanged();
        }
    }

    public ColorStateList obtainForTheme(Resources.Theme t) {
        if (t == null || !canApplyTheme()) {
            return this;
        }
        ColorStateList clone = new ColorStateList(this);
        clone.applyTheme(t);
        return clone;
    }

    public int getChangingConfigurations() {
        return super.getChangingConfigurations() | this.mChangingConfigurations;
    }

    private int modulateColorAlpha(int baseColor, float alphaMod) {
        if (alphaMod == 1.0f) {
            return baseColor;
        }
        return (16777215 & baseColor) | (MathUtils.constrain((int) ((((float) Color.alpha(baseColor)) * alphaMod) + 0.5f), 0, 255) << 24);
    }

    public boolean isStateful() {
        return this.mStateSpecs.length >= 1 && this.mStateSpecs[0].length > 0;
    }

    public boolean hasFocusStateSpecified() {
        return StateSet.containsAttribute(this.mStateSpecs, 16842908);
    }

    public boolean isOpaque() {
        return this.mIsOpaque;
    }

    public int getColorForState(int[] stateSet, int defaultColor) {
        int setLength = this.mStateSpecs.length;
        for (int i = 0; i < setLength; i++) {
            if (StateSet.stateSetMatches(this.mStateSpecs[i], stateSet)) {
                return this.mColors[i];
            }
        }
        return defaultColor;
    }

    public int getDefaultColor() {
        return this.mDefaultColor;
    }

    public int[][] getStates() {
        return this.mStateSpecs;
    }

    public int[] getColors() {
        return this.mColors;
    }

    public boolean hasState(int state) {
        for (int[] states : this.mStateSpecs) {
            int stateCount = states.length;
            for (int stateIndex = 0; stateIndex < stateCount; stateIndex++) {
                if (states[stateIndex] == state || states[stateIndex] == (~state)) {
                    return true;
                }
            }
        }
        return false;
    }

    public String toString() {
        return "ColorStateList{mThemeAttrs=" + Arrays.deepToString(this.mThemeAttrs) + "mChangingConfigurations=" + this.mChangingConfigurations + "mStateSpecs=" + Arrays.deepToString(this.mStateSpecs) + "mColors=" + Arrays.toString(this.mColors) + "mDefaultColor=" + this.mDefaultColor + '}';
    }

    private void onColorsChanged() {
        int defaultColor = -65536;
        boolean isOpaque = true;
        int[][] states = this.mStateSpecs;
        int[] colors = this.mColors;
        int N = states.length;
        if (N > 0) {
            int i = 0;
            defaultColor = colors[0];
            int i2 = N - 1;
            while (true) {
                if (i2 <= 0) {
                    break;
                } else if (states[i2].length == 0) {
                    defaultColor = colors[i2];
                    break;
                } else {
                    i2--;
                }
            }
            while (true) {
                if (i >= N) {
                    break;
                } else if (Color.alpha(colors[i]) != 255) {
                    isOpaque = false;
                    break;
                } else {
                    i++;
                }
            }
        }
        this.mDefaultColor = defaultColor;
        this.mIsOpaque = isOpaque;
    }

    public ConstantState<ComplexColor> getConstantState() {
        if (this.mFactory == null) {
            this.mFactory = new ColorStateListFactory(this);
        }
        return this.mFactory;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        if (canApplyTheme()) {
            Log.w(TAG, "Wrote partially-resolved ColorStateList to parcel!");
        }
        dest.writeInt(N);
        for (int[] writeIntArray : this.mStateSpecs) {
            dest.writeIntArray(writeIntArray);
        }
        dest.writeIntArray(this.mColors);
    }
}
