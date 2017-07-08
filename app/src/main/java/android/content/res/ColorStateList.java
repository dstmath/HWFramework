package android.content.res;

import android.content.res.Resources.Theme;
import android.graphics.Color;
import android.hwtheme.HwThemeManager;
import android.net.wifi.wifipro.NetworkHistoryUtils;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.Process;
import android.speech.tts.TextToSpeech.Engine;
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
    public static final Creator<ColorStateList> CREATOR = null;
    private static final int DEFAULT_COLOR = -65536;
    private static final int[][] EMPTY = null;
    private static final String TAG = "ColorStateList";
    private static final SparseArray<WeakReference<ColorStateList>> sCache = null;
    private int mChangingConfigurations;
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

        public ColorStateList newInstance(Resources res, Theme theme) {
            return this.mSrc.obtainForTheme(theme);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.content.res.ColorStateList.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.content.res.ColorStateList.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.content.res.ColorStateList.<clinit>():void");
    }

    public boolean hasState(int r1) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.content.res.ColorStateList.hasState(int):boolean
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.content.res.ColorStateList.hasState(int):boolean");
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
                ColorStateList cached = (ColorStateList) ((WeakReference) sCache.valueAt(index)).get();
                if (cached != null) {
                    return cached;
                }
                sCache.removeAt(index);
            }
            for (int i = sCache.size() - 1; i >= 0; i--) {
                if (((WeakReference) sCache.valueAt(i)).get() == null) {
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

    public static ColorStateList createFromXml(Resources r, XmlPullParser parser, Theme theme) throws XmlPullParserException, IOException {
        AttributeSet attrs = Xml.asAttributeSet(parser);
        int type;
        do {
            type = parser.next();
            if (type == 2) {
                break;
            }
        } while (type != 1);
        if (type == 2) {
            return createFromXmlInner(r, parser, attrs, theme);
        }
        throw new XmlPullParserException("No start tag found");
    }

    static ColorStateList createFromXmlInner(Resources r, XmlPullParser parser, AttributeSet attrs, Theme theme) throws XmlPullParserException, IOException {
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
            colors[i] = (this.mColors[i] & IBinder.LAST_CALL_TRANSACTION) | (alpha << 24);
        }
        return new ColorStateList(this.mStateSpecs, colors);
    }

    private void inflate(Resources r, XmlPullParser parser, AttributeSet attrs, Theme theme) throws XmlPullParserException, IOException {
        int innerDepth = parser.getDepth() + 1;
        int changingConfigurations = 0;
        int defaultColor = DEFAULT_COLOR;
        boolean hasUnresolvedAttrs = false;
        int[][] stateSpecList = (int[][]) ArrayUtils.newUnpaddedArray(int[].class, 20);
        int[][] themeAttrsList = new int[stateSpecList.length][];
        int[] colorList = new int[stateSpecList.length];
        int listSize = 0;
        while (true) {
            int type = parser.next();
            if (type != 1) {
                int depth = parser.getDepth();
                if (depth >= innerDepth || type != 3) {
                    if (type == 2 && depth <= innerDepth && parser.getName().equals(HwThemeManager.TAG_ITEM)) {
                        TypedArray a = Resources.obtainAttributes(r, theme, attrs, R.styleable.ColorStateListItem);
                        Object themeAttrs = a.extractThemeAttrs();
                        int baseColor = a.getColor(0, Color.MAGENTA);
                        float alphaMod = a.getFloat(1, Engine.DEFAULT_VOLUME);
                        changingConfigurations |= a.getChangingConfigurations();
                        a.recycle();
                        int numAttrs = attrs.getAttributeCount();
                        int[] stateSpec = new int[numAttrs];
                        int i = 0;
                        int j = 0;
                        while (i < numAttrs) {
                            int j2;
                            int stateResId = attrs.getAttributeNameResource(i);
                            switch (stateResId) {
                                case android.R.attr.color /*16843173*/:
                                case android.R.attr.alpha /*16843551*/:
                                    j2 = j;
                                    break;
                                default:
                                    j2 = j + 1;
                                    if (!attrs.getAttributeBooleanValue(i, false)) {
                                        stateResId = -stateResId;
                                    }
                                    stateSpec[j] = stateResId;
                                    break;
                            }
                            i++;
                            j = j2;
                        }
                        Object stateSpec2 = StateSet.trimStateSet(stateSpec, j);
                        int color = modulateColorAlpha(baseColor, alphaMod);
                        if (listSize == 0 || stateSpec2.length == 0) {
                            defaultColor = color;
                        }
                        if (themeAttrs != null) {
                            hasUnresolvedAttrs = true;
                        }
                        colorList = GrowingArrayUtils.append(colorList, listSize, color);
                        themeAttrsList = (int[][]) GrowingArrayUtils.append(themeAttrsList, listSize, themeAttrs);
                        stateSpecList = (int[][]) GrowingArrayUtils.append(stateSpecList, listSize, stateSpec2);
                        listSize++;
                    }
                }
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
            System.arraycopy(stateSpecList, 0, this.mStateSpecs, 0, listSize);
            onColorsChanged();
            return;
        }
    }

    public boolean canApplyTheme() {
        return this.mThemeAttrs != null;
    }

    private void applyTheme(Theme t) {
        if (this.mThemeAttrs != null) {
            boolean hasUnresolvedAttrs = false;
            int[][] themeAttrsList = this.mThemeAttrs;
            int N = themeAttrsList.length;
            for (int i = 0; i < N; i++) {
                if (themeAttrsList[i] != null) {
                    float defaultAlphaMod;
                    TypedArray a = t.resolveAttributes(themeAttrsList[i], R.styleable.ColorStateListItem);
                    if (themeAttrsList[i][0] != 0) {
                        defaultAlphaMod = ((float) Color.alpha(this.mColors[i])) / 255.0f;
                    } else {
                        defaultAlphaMod = Engine.DEFAULT_VOLUME;
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

    public ColorStateList obtainForTheme(Theme t) {
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
        if (alphaMod == Engine.DEFAULT_VOLUME) {
            return baseColor;
        }
        return (IBinder.LAST_CALL_TRANSACTION & baseColor) | (MathUtils.constrain((int) ((((float) Color.alpha(baseColor)) * alphaMod) + NetworkHistoryUtils.RECOVERY_PERCENTAGE), 0, Process.PROC_TERM_MASK) << 24);
    }

    public boolean isStateful() {
        return this.mStateSpecs.length > 1;
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

    public String toString() {
        return "ColorStateList{mThemeAttrs=" + Arrays.deepToString(this.mThemeAttrs) + "mChangingConfigurations=" + this.mChangingConfigurations + "mStateSpecs=" + Arrays.deepToString(this.mStateSpecs) + "mColors=" + Arrays.toString(this.mColors) + "mDefaultColor=" + this.mDefaultColor + '}';
    }

    private void onColorsChanged() {
        int defaultColor = DEFAULT_COLOR;
        boolean isOpaque = true;
        int[][] states = this.mStateSpecs;
        int[] colors = this.mColors;
        int N = states.length;
        if (N > 0) {
            int i;
            defaultColor = colors[0];
            for (i = N - 1; i > 0; i--) {
                if (states[i].length == 0) {
                    defaultColor = colors[i];
                    break;
                }
            }
            for (i = 0; i < N; i++) {
                if (Color.alpha(colors[i]) != Process.PROC_TERM_MASK) {
                    isOpaque = false;
                    break;
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
