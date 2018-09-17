package android.content.res;

import android.content.res.Resources.Theme;
import android.graphics.LinearGradient;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.graphics.SweepGradient;
import android.hwtheme.HwThemeManager;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.wifipro.NetworkHistoryUtils;
import android.speech.tts.TextToSpeech.Engine;
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
    private int mCenterColor;
    private float mCenterX;
    private float mCenterY;
    private int mChangingConfigurations;
    private int mDefaultColor;
    private int mEndColor;
    private float mEndX;
    private float mEndY;
    private GradientColorFactory mFactory;
    private float mGradientRadius;
    private int mGradientType;
    private boolean mHasCenterColor;
    private int[] mItemColors;
    private float[] mItemOffsets;
    private int[][] mItemsThemeAttrs;
    private Shader mShader;
    private int mStartColor;
    private float mStartX;
    private float mStartY;
    private int[] mThemeAttrs;
    private int mTileMode;

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
        this.mShader = null;
        this.mGradientType = TILE_MODE_CLAMP;
        this.mCenterX = 0.0f;
        this.mCenterY = 0.0f;
        this.mStartX = 0.0f;
        this.mStartY = 0.0f;
        this.mEndX = 0.0f;
        this.mEndY = 0.0f;
        this.mStartColor = TILE_MODE_CLAMP;
        this.mCenterColor = TILE_MODE_CLAMP;
        this.mEndColor = TILE_MODE_CLAMP;
        this.mHasCenterColor = DBG_GRADIENT;
        this.mTileMode = TILE_MODE_CLAMP;
        this.mGradientRadius = 0.0f;
    }

    private GradientColor(GradientColor copy) {
        this.mShader = null;
        this.mGradientType = TILE_MODE_CLAMP;
        this.mCenterX = 0.0f;
        this.mCenterY = 0.0f;
        this.mStartX = 0.0f;
        this.mStartY = 0.0f;
        this.mEndX = 0.0f;
        this.mEndY = 0.0f;
        this.mStartColor = TILE_MODE_CLAMP;
        this.mCenterColor = TILE_MODE_CLAMP;
        this.mEndColor = TILE_MODE_CLAMP;
        this.mHasCenterColor = DBG_GRADIENT;
        this.mTileMode = TILE_MODE_CLAMP;
        this.mGradientRadius = 0.0f;
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
            case TILE_MODE_CLAMP /*0*/:
                return TileMode.CLAMP;
            case TILE_MODE_REPEAT /*1*/:
                return TileMode.REPEAT;
            case TILE_MODE_MIRROR /*2*/:
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
        this.mGradientType = a.getInt(TILE_MODE_MIRROR, this.mGradientType);
        this.mStartColor = a.getColor(TILE_MODE_CLAMP, this.mStartColor);
        this.mHasCenterColor |= a.hasValue(7);
        this.mCenterColor = a.getColor(7, this.mCenterColor);
        this.mEndColor = a.getColor(TILE_MODE_REPEAT, this.mEndColor);
        this.mTileMode = a.getInt(6, this.mTileMode);
        this.mGradientRadius = a.getFloat(5, this.mGradientRadius);
    }

    private void validateXmlContent() throws XmlPullParserException {
        if (this.mGradientRadius <= 0.0f && this.mGradientType == TILE_MODE_REPEAT) {
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
            if (type == TILE_MODE_MIRROR) {
                break;
            }
        } while (type != TILE_MODE_REPEAT);
        if (type == TILE_MODE_MIRROR) {
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void inflateChildElements(Resources r, XmlPullParser parser, AttributeSet attrs, Theme theme) throws XmlPullParserException, IOException {
        int innerDepth = parser.getDepth() + TILE_MODE_REPEAT;
        float[] offsetList = new float[20];
        int[] colorList = new int[offsetList.length];
        int[][] themeAttrsList = new int[offsetList.length][];
        int listSize = TILE_MODE_CLAMP;
        boolean hasUnresolvedAttrs = DBG_GRADIENT;
        while (true) {
            int type = parser.next();
            if (type == TILE_MODE_REPEAT) {
                break;
            }
            int depth = parser.getDepth();
            if (depth < innerDepth && type == 3) {
                break;
            } else if (type == TILE_MODE_MIRROR && depth <= innerDepth && parser.getName().equals(HwThemeManager.TAG_ITEM)) {
                TypedArray a = Resources.obtainAttributes(r, theme, attrs, R.styleable.GradientColorItem);
                boolean hasColor = a.hasValue(TILE_MODE_CLAMP);
                boolean hasOffset = a.hasValue(TILE_MODE_REPEAT);
                if (hasColor && hasOffset) {
                    int[] themeAttrs = a.extractThemeAttrs();
                    int color = a.getColor(TILE_MODE_CLAMP, TILE_MODE_CLAMP);
                    float offset = a.getFloat(TILE_MODE_REPEAT, 0.0f);
                    this.mChangingConfigurations |= a.getChangingConfigurations();
                    a.recycle();
                    if (themeAttrs != null) {
                        hasUnresolvedAttrs = true;
                    }
                    colorList = GrowingArrayUtils.append(colorList, listSize, color);
                    offsetList = GrowingArrayUtils.append(offsetList, listSize, offset);
                    themeAttrsList = (int[][]) GrowingArrayUtils.append(themeAttrsList, listSize, themeAttrs);
                    listSize += TILE_MODE_REPEAT;
                }
            }
        }
        if (listSize > 0) {
            if (hasUnresolvedAttrs) {
                this.mItemsThemeAttrs = new int[listSize][];
                System.arraycopy(themeAttrsList, TILE_MODE_CLAMP, this.mItemsThemeAttrs, TILE_MODE_CLAMP, listSize);
            } else {
                this.mItemsThemeAttrs = null;
            }
            this.mItemColors = new int[listSize];
            this.mItemOffsets = new float[listSize];
            System.arraycopy(colorList, TILE_MODE_CLAMP, this.mItemColors, TILE_MODE_CLAMP, listSize);
            System.arraycopy(offsetList, TILE_MODE_CLAMP, this.mItemOffsets, TILE_MODE_CLAMP, listSize);
        }
    }

    private void applyItemsAttrsTheme(Theme t) {
        if (this.mItemsThemeAttrs != null) {
            boolean hasUnresolvedAttrs = DBG_GRADIENT;
            int[][] themeAttrsList = this.mItemsThemeAttrs;
            int N = themeAttrsList.length;
            for (int i = TILE_MODE_CLAMP; i < N; i += TILE_MODE_REPEAT) {
                if (themeAttrsList[i] != null) {
                    TypedArray a = t.resolveAttributes(themeAttrsList[i], R.styleable.GradientColorItem);
                    themeAttrsList[i] = a.extractThemeAttrs(themeAttrsList[i]);
                    if (themeAttrsList[i] != null) {
                        hasUnresolvedAttrs = true;
                    }
                    this.mItemColors[i] = a.getColor(TILE_MODE_CLAMP, this.mItemColors[i]);
                    this.mItemOffsets[i] = a.getFloat(TILE_MODE_REPEAT, this.mItemOffsets[i]);
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
        float[] fArr = null;
        if (this.mItemColors != null) {
            int length = this.mItemColors.length;
            tempColors = new int[length];
            fArr = new float[length];
            for (int i = TILE_MODE_CLAMP; i < length; i += TILE_MODE_REPEAT) {
                tempColors[i] = this.mItemColors[i];
                fArr[i] = this.mItemOffsets[i];
            }
        } else if (this.mHasCenterColor) {
            tempColors = new int[]{this.mStartColor, this.mCenterColor, this.mEndColor};
            fArr = new float[]{0.0f, NetworkHistoryUtils.RECOVERY_PERCENTAGE, Engine.DEFAULT_VOLUME};
        } else {
            tempColors = new int[TILE_MODE_MIRROR];
            tempColors[TILE_MODE_CLAMP] = this.mStartColor;
            tempColors[TILE_MODE_REPEAT] = this.mEndColor;
        }
        if (tempColors.length < TILE_MODE_MIRROR) {
            Log.w(TAG, "<gradient> tag requires 2 color values specified!" + tempColors.length + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + tempColors);
        }
        if (this.mGradientType == 0) {
            this.mShader = new LinearGradient(this.mStartX, this.mStartY, this.mEndX, this.mEndY, tempColors, fArr, parseTileMode(this.mTileMode));
        } else if (this.mGradientType == TILE_MODE_REPEAT) {
            this.mShader = new RadialGradient(this.mCenterX, this.mCenterY, this.mGradientRadius, tempColors, fArr, parseTileMode(this.mTileMode));
        } else {
            this.mShader = new SweepGradient(this.mCenterX, this.mCenterY, tempColors, fArr);
        }
        this.mDefaultColor = tempColors[TILE_MODE_CLAMP];
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
        return (this.mThemeAttrs == null && this.mItemsThemeAttrs == null) ? DBG_GRADIENT : true;
    }
}
