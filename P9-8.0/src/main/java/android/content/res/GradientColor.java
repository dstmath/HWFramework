package android.content.res;

import android.content.res.Resources.Theme;
import android.graphics.LinearGradient;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.graphics.SweepGradient;
import android.hardware.camera2.params.TonemapCurve;
import android.hwtheme.HwThemeManager;
import android.net.wifi.WifiEnterpriseConfig;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import com.android.internal.R;
import com.android.internal.util.GrowingArrayUtils;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class GradientColor extends ComplexColor {
    private static final boolean DBG_GRADIENT = false;
    private static final String TAG = "GradientColor";
    private static final int TILE_MODE_CLAMP = 0;
    private static final int TILE_MODE_MIRROR = 2;
    private static final int TILE_MODE_REPEAT = 1;
    private int mCenterColor = 0;
    private float mCenterX = TonemapCurve.LEVEL_BLACK;
    private float mCenterY = TonemapCurve.LEVEL_BLACK;
    private int mChangingConfigurations;
    private int mDefaultColor;
    private int mEndColor = 0;
    private float mEndX = TonemapCurve.LEVEL_BLACK;
    private float mEndY = TonemapCurve.LEVEL_BLACK;
    private GradientColorFactory mFactory;
    private float mGradientRadius = TonemapCurve.LEVEL_BLACK;
    private int mGradientType = 0;
    private boolean mHasCenterColor = false;
    private int[] mItemColors;
    private float[] mItemOffsets;
    private int[][] mItemsThemeAttrs;
    private Shader mShader = null;
    private int mStartColor = 0;
    private float mStartX = TonemapCurve.LEVEL_BLACK;
    private float mStartY = TonemapCurve.LEVEL_BLACK;
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

        public GradientColor newInstance(Resources res, Theme theme) {
            return this.mSrc.obtainForTheme(theme);
        }
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

    private static TileMode parseTileMode(int tileMode) {
        switch (tileMode) {
            case 0:
                return TileMode.CLAMP;
            case 1:
                return TileMode.REPEAT;
            case 2:
                return TileMode.MIRROR;
            default:
                return TileMode.CLAMP;
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
        if (this.mGradientRadius <= TonemapCurve.LEVEL_BLACK && this.mGradientType == 1) {
            throw new XmlPullParserException("<gradient> tag requires 'gradientRadius' attribute with radial type");
        }
    }

    public Shader getShader() {
        return this.mShader;
    }

    public static GradientColor createFromXml(Resources r, XmlResourceParser parser, Theme theme) throws XmlPullParserException, IOException {
        int type;
        AttributeSet attrs = Xml.asAttributeSet(parser);
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

    static GradientColor createFromXmlInner(Resources r, XmlPullParser parser, AttributeSet attrs, Theme theme) throws XmlPullParserException, IOException {
        String name = parser.getName();
        if (name.equals("gradient")) {
            GradientColor gradientColor = new GradientColor();
            gradientColor.inflate(r, parser, attrs, theme);
            return gradientColor;
        }
        throw new XmlPullParserException(parser.getPositionDescription() + ": invalid gradient color tag " + name);
    }

    private void inflate(Resources r, XmlPullParser parser, AttributeSet attrs, Theme theme) throws XmlPullParserException, IOException {
        TypedArray a = Resources.obtainAttributes(r, theme, attrs, R.styleable.GradientColor);
        updateRootElementState(a);
        this.mChangingConfigurations |= a.getChangingConfigurations();
        a.recycle();
        validateXmlContent();
        inflateChildElements(r, parser, attrs, theme);
        onColorsChange();
    }

    /* JADX WARNING: Missing block: B:17:0x0099, code:
            throw new org.xmlpull.v1.XmlPullParserException(r23.getPositionDescription() + ": <item> tag requires a 'color' attribute and a 'offset' " + "attribute!");
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void inflateChildElements(Resources r, XmlPullParser parser, AttributeSet attrs, Theme theme) throws XmlPullParserException, IOException {
        int innerDepth = parser.getDepth() + 1;
        float[] offsetList = new float[20];
        int[] colorList = new int[offsetList.length];
        int[][] themeAttrsList = new int[offsetList.length][];
        int listSize = 0;
        boolean hasUnresolvedAttrs = false;
        while (true) {
            int type = parser.next();
            if (type == 1) {
                break;
            }
            int depth = parser.getDepth();
            if (depth < innerDepth && type == 3) {
                break;
            } else if (type == 2 && depth <= innerDepth && (parser.getName().equals(HwThemeManager.TAG_ITEM) ^ 1) == 0) {
                TypedArray a = Resources.obtainAttributes(r, theme, attrs, R.styleable.GradientColorItem);
                boolean hasColor = a.hasValue(0);
                boolean hasOffset = a.hasValue(1);
                if (hasColor && (hasOffset ^ 1) == 0) {
                    int[] themeAttrs = a.extractThemeAttrs();
                    int color = a.getColor(0, 0);
                    float offset = a.getFloat(1, TonemapCurve.LEVEL_BLACK);
                    this.mChangingConfigurations |= a.getChangingConfigurations();
                    a.recycle();
                    if (themeAttrs != null) {
                        hasUnresolvedAttrs = true;
                    }
                    colorList = GrowingArrayUtils.append(colorList, listSize, color);
                    offsetList = GrowingArrayUtils.append(offsetList, listSize, offset);
                    themeAttrsList = (int[][]) GrowingArrayUtils.append(themeAttrsList, listSize, themeAttrs);
                    listSize++;
                }
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
            System.arraycopy(offsetList, 0, this.mItemOffsets, 0, listSize);
        }
    }

    private void applyItemsAttrsTheme(Theme t) {
        if (this.mItemsThemeAttrs != null) {
            boolean hasUnresolvedAttrs = false;
            int[][] themeAttrsList = this.mItemsThemeAttrs;
            int N = themeAttrsList.length;
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
            tempOffsets = new float[]{TonemapCurve.LEVEL_BLACK, 0.5f, 1.0f};
        } else {
            tempColors = new int[]{this.mStartColor, this.mEndColor};
        }
        if (tempColors.length < 2) {
            Log.w(TAG, "<gradient> tag requires 2 color values specified!" + tempColors.length + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + tempColors);
        }
        if (this.mGradientType == 0) {
            this.mShader = new LinearGradient(this.mStartX, this.mStartY, this.mEndX, this.mEndY, tempColors, tempOffsets, parseTileMode(this.mTileMode));
        } else if (this.mGradientType == 1) {
            this.mShader = new RadialGradient(this.mCenterX, this.mCenterY, this.mGradientRadius, tempColors, tempOffsets, parseTileMode(this.mTileMode));
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

    public GradientColor obtainForTheme(Theme t) {
        if (t == null || (canApplyTheme() ^ 1) != 0) {
            return this;
        }
        GradientColor clone = new GradientColor(this);
        clone.applyTheme(t);
        return clone;
    }

    public int getChangingConfigurations() {
        return super.getChangingConfigurations() | this.mChangingConfigurations;
    }

    private void applyTheme(Theme t) {
        if (this.mThemeAttrs != null) {
            applyRootAttrsTheme(t);
        }
        if (this.mItemsThemeAttrs != null) {
            applyItemsAttrsTheme(t);
        }
        onColorsChange();
    }

    private void applyRootAttrsTheme(Theme t) {
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
