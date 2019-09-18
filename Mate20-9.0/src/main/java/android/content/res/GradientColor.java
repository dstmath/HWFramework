package android.content.res;

import android.content.res.Resources;
import android.graphics.LinearGradient;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.hwtheme.HwThemeManager;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import com.android.internal.R;
import com.android.internal.util.GrowingArrayUtils;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class GradientColor extends ComplexColor {
    private static final boolean DBG_GRADIENT = false;
    private static final String TAG = "GradientColor";
    private static final int TILE_MODE_CLAMP = 0;
    private static final int TILE_MODE_MIRROR = 2;
    private static final int TILE_MODE_REPEAT = 1;
    private int mCenterColor = 0;
    private float mCenterX = 0.0f;
    private float mCenterY = 0.0f;
    /* access modifiers changed from: private */
    public int mChangingConfigurations;
    private int mDefaultColor;
    private int mEndColor = 0;
    private float mEndX = 0.0f;
    private float mEndY = 0.0f;
    private GradientColorFactory mFactory;
    private float mGradientRadius = 0.0f;
    private int mGradientType = 0;
    private boolean mHasCenterColor = false;
    private int[] mItemColors;
    private float[] mItemOffsets;
    private int[][] mItemsThemeAttrs;
    private Shader mShader = null;
    private int mStartColor = 0;
    private float mStartX = 0.0f;
    private float mStartY = 0.0f;
    private int[] mThemeAttrs;
    private int mTileMode = 0;

    private static class GradientColorFactory extends ConstantState<ComplexColor> {
        private final GradientColor mSrc;

        public GradientColorFactory(GradientColor src) {
            this.mSrc = src;
        }

        public int getChangingConfigurations() {
            return this.mSrc.mChangingConfigurations;
        }

        public GradientColor newInstance() {
            return this.mSrc;
        }

        public GradientColor newInstance(Resources res, Resources.Theme theme) {
            return this.mSrc.obtainForTheme(theme);
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    private @interface GradientTileMode {
    }

    private GradientColor() {
    }

    private GradientColor(GradientColor copy) {
        if (copy != null) {
            this.mChangingConfigurations = copy.mChangingConfigurations;
            this.mDefaultColor = copy.mDefaultColor;
            this.mShader = copy.mShader;
            this.mGradientType = copy.mGradientType;
            this.mCenterX = copy.mCenterX;
            this.mCenterY = copy.mCenterY;
            this.mStartX = copy.mStartX;
            this.mStartY = copy.mStartY;
            this.mEndX = copy.mEndX;
            this.mEndY = copy.mEndY;
            this.mStartColor = copy.mStartColor;
            this.mCenterColor = copy.mCenterColor;
            this.mEndColor = copy.mEndColor;
            this.mHasCenterColor = copy.mHasCenterColor;
            this.mGradientRadius = copy.mGradientRadius;
            this.mTileMode = copy.mTileMode;
            if (copy.mItemColors != null) {
                this.mItemColors = (int[]) copy.mItemColors.clone();
            }
            if (copy.mItemOffsets != null) {
                this.mItemOffsets = (float[]) copy.mItemOffsets.clone();
            }
            if (copy.mThemeAttrs != null) {
                this.mThemeAttrs = (int[]) copy.mThemeAttrs.clone();
            }
            if (copy.mItemsThemeAttrs != null) {
                this.mItemsThemeAttrs = (int[][]) copy.mItemsThemeAttrs.clone();
            }
        }
    }

    private static Shader.TileMode parseTileMode(int tileMode) {
        switch (tileMode) {
            case 0:
                return Shader.TileMode.CLAMP;
            case 1:
                return Shader.TileMode.REPEAT;
            case 2:
                return Shader.TileMode.MIRROR;
            default:
                return Shader.TileMode.CLAMP;
        }
    }

    private void updateRootElementState(TypedArray a) {
        this.mThemeAttrs = a.extractThemeAttrs();
        this.mStartX = a.getFloat(8, this.mStartX);
        this.mStartY = a.getFloat(9, this.mStartY);
        this.mEndX = a.getFloat(10, this.mEndX);
        this.mEndY = a.getFloat(11, this.mEndY);
        this.mCenterX = a.getFloat(3, this.mCenterX);
        this.mCenterY = a.getFloat(4, this.mCenterY);
        this.mGradientType = a.getInt(2, this.mGradientType);
        this.mStartColor = a.getColor(0, this.mStartColor);
        this.mHasCenterColor |= a.hasValue(7);
        this.mCenterColor = a.getColor(7, this.mCenterColor);
        this.mEndColor = a.getColor(1, this.mEndColor);
        this.mTileMode = a.getInt(6, this.mTileMode);
        this.mGradientRadius = a.getFloat(5, this.mGradientRadius);
    }

    private void validateXmlContent() throws XmlPullParserException {
        if (this.mGradientRadius <= 0.0f && this.mGradientType == 1) {
            throw new XmlPullParserException("<gradient> tag requires 'gradientRadius' attribute with radial type");
        }
    }

    public Shader getShader() {
        return this.mShader;
    }

    public static GradientColor createFromXml(Resources r, XmlResourceParser parser, Resources.Theme theme) throws XmlPullParserException, IOException {
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

    static GradientColor createFromXmlInner(Resources r, XmlPullParser parser, AttributeSet attrs, Resources.Theme theme) throws XmlPullParserException, IOException {
        String name = parser.getName();
        if (name.equals("gradient")) {
            GradientColor gradientColor = new GradientColor();
            gradientColor.inflate(r, parser, attrs, theme);
            return gradientColor;
        }
        throw new XmlPullParserException(parser.getPositionDescription() + ": invalid gradient color tag " + name);
    }

    private void inflate(Resources r, XmlPullParser parser, AttributeSet attrs, Resources.Theme theme) throws XmlPullParserException, IOException {
        TypedArray a = Resources.obtainAttributes(r, theme, attrs, R.styleable.GradientColor);
        updateRootElementState(a);
        this.mChangingConfigurations |= a.getChangingConfigurations();
        a.recycle();
        validateXmlContent();
        inflateChildElements(r, parser, attrs, theme);
        onColorsChange();
    }

    /* JADX WARNING: type inference failed for: r10v4, types: [java.lang.Object[]] */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x00c2, code lost:
        throw new org.xmlpull.v1.XmlPullParserException(r23.getPositionDescription() + ": <item> tag requires a 'color' attribute and a 'offset' attribute!");
     */
    /* JADX WARNING: Multi-variable type inference failed */
    private void inflateChildElements(Resources r, XmlPullParser parser, AttributeSet attrs, Resources.Theme theme) throws XmlPullParserException, IOException {
        int i = 1;
        int innerDepth = parser.getDepth() + 1;
        float[] offsetList = new float[20];
        int[] colorList = new int[offsetList.length];
        int[][] themeAttrsList = new int[offsetList.length][];
        int listSize = 0;
        int i2 = 0;
        float[] offsetList2 = offsetList;
        boolean hasUnresolvedAttrs = false;
        while (true) {
            int next = parser.next();
            int type = next;
            if (next == i) {
                Resources resources = r;
                AttributeSet attributeSet = attrs;
                Resources.Theme theme2 = theme;
                int i3 = innerDepth;
                int i4 = type;
                break;
            }
            int depth = parser.getDepth();
            int depth2 = depth;
            if (depth < innerDepth && type == 3) {
                Resources resources2 = r;
                AttributeSet attributeSet2 = attrs;
                Resources.Theme theme3 = theme;
                int i5 = innerDepth;
                int i6 = type;
                break;
            } else if (type == 2 && depth2 <= innerDepth && parser.getName().equals(HwThemeManager.TAG_ITEM)) {
                TypedArray a = Resources.obtainAttributes(r, theme, attrs, R.styleable.GradientColorItem);
                boolean hasColor = a.hasValue(i2);
                boolean hasOffset = a.hasValue(i);
                if (!hasColor || !hasOffset) {
                    int i7 = type;
                } else {
                    int[] themeAttrs = a.extractThemeAttrs();
                    int innerDepth2 = innerDepth;
                    int innerDepth3 = a.getColor(i2, i2);
                    int i8 = type;
                    float offset = a.getFloat(1, 0.0f);
                    this.mChangingConfigurations |= a.getChangingConfigurations();
                    a.recycle();
                    if (themeAttrs != null) {
                        hasUnresolvedAttrs = true;
                    }
                    colorList = GrowingArrayUtils.append(colorList, listSize, innerDepth3);
                    offsetList2 = GrowingArrayUtils.append(offsetList2, listSize, offset);
                    themeAttrsList = GrowingArrayUtils.append(themeAttrsList, listSize, themeAttrs);
                    listSize++;
                    innerDepth = innerDepth2;
                    i = 1;
                    i2 = 0;
                }
            } else {
                Resources resources3 = r;
                AttributeSet attributeSet3 = attrs;
                Resources.Theme theme4 = theme;
                innerDepth = innerDepth;
                i = 1;
                i2 = 0;
            }
        }
        if (listSize > 0) {
            if (hasUnresolvedAttrs) {
                this.mItemsThemeAttrs = new int[listSize][];
                System.arraycopy(themeAttrsList, 0, this.mItemsThemeAttrs, 0, listSize);
            } else {
                this.mItemsThemeAttrs = null;
            }
            this.mItemColors = new int[listSize];
            this.mItemOffsets = new float[listSize];
            System.arraycopy(colorList, 0, this.mItemColors, 0, listSize);
            System.arraycopy(offsetList2, 0, this.mItemOffsets, 0, listSize);
        }
    }

    private void applyItemsAttrsTheme(Resources.Theme t) {
        if (this.mItemsThemeAttrs != null) {
            int[][] themeAttrsList = this.mItemsThemeAttrs;
            int N = themeAttrsList.length;
            boolean hasUnresolvedAttrs = false;
            for (int i = 0; i < N; i++) {
                if (themeAttrsList[i] != null) {
                    TypedArray a = t.resolveAttributes(themeAttrsList[i], R.styleable.GradientColorItem);
                    themeAttrsList[i] = a.extractThemeAttrs(themeAttrsList[i]);
                    if (themeAttrsList[i] != null) {
                        hasUnresolvedAttrs = true;
                    }
                    this.mItemColors[i] = a.getColor(0, this.mItemColors[i]);
                    this.mItemOffsets[i] = a.getFloat(1, this.mItemOffsets[i]);
                    this.mChangingConfigurations |= a.getChangingConfigurations();
                    a.recycle();
                }
            }
            if (!hasUnresolvedAttrs) {
                this.mItemsThemeAttrs = null;
            }
        }
    }

    private void onColorsChange() {
        int[] tempColors;
        float[] tempOffsets = null;
        if (this.mItemColors != null) {
            int length = this.mItemColors.length;
            tempColors = new int[length];
            tempOffsets = new float[length];
            for (int i = 0; i < length; i++) {
                tempColors[i] = this.mItemColors[i];
                tempOffsets[i] = this.mItemOffsets[i];
            }
        } else if (this.mHasCenterColor) {
            tempColors = new int[]{this.mStartColor, this.mCenterColor, this.mEndColor};
            tempOffsets = new float[]{0.0f, 0.5f, 1.0f};
        } else {
            tempColors = new int[]{this.mStartColor, this.mEndColor};
        }
        if (tempColors.length < 2) {
            Log.w(TAG, "<gradient> tag requires 2 color values specified!" + tempColors.length + " " + tempColors);
        }
        if (this.mGradientType == 0) {
            LinearGradient linearGradient = new LinearGradient(this.mStartX, this.mStartY, this.mEndX, this.mEndY, tempColors, tempOffsets, parseTileMode(this.mTileMode));
            this.mShader = linearGradient;
        } else if (this.mGradientType == 1) {
            RadialGradient radialGradient = new RadialGradient(this.mCenterX, this.mCenterY, this.mGradientRadius, tempColors, tempOffsets, parseTileMode(this.mTileMode));
            this.mShader = radialGradient;
        } else {
            this.mShader = new SweepGradient(this.mCenterX, this.mCenterY, tempColors, tempOffsets);
        }
        this.mDefaultColor = tempColors[0];
    }

    public int getDefaultColor() {
        return this.mDefaultColor;
    }

    public ConstantState<ComplexColor> getConstantState() {
        if (this.mFactory == null) {
            this.mFactory = new GradientColorFactory(this);
        }
        return this.mFactory;
    }

    public GradientColor obtainForTheme(Resources.Theme t) {
        if (t == null || !canApplyTheme()) {
            return this;
        }
        GradientColor clone = new GradientColor(this);
        clone.applyTheme(t);
        return clone;
    }

    public int getChangingConfigurations() {
        return super.getChangingConfigurations() | this.mChangingConfigurations;
    }

    private void applyTheme(Resources.Theme t) {
        if (this.mThemeAttrs != null) {
            applyRootAttrsTheme(t);
        }
        if (this.mItemsThemeAttrs != null) {
            applyItemsAttrsTheme(t);
        }
        onColorsChange();
    }

    private void applyRootAttrsTheme(Resources.Theme t) {
        TypedArray a = t.resolveAttributes(this.mThemeAttrs, R.styleable.GradientColor);
        this.mThemeAttrs = a.extractThemeAttrs(this.mThemeAttrs);
        updateRootElementState(a);
        this.mChangingConfigurations |= a.getChangingConfigurations();
        a.recycle();
    }

    public boolean canApplyTheme() {
        return (this.mThemeAttrs == null && this.mItemsThemeAttrs == null) ? false : true;
    }
}
