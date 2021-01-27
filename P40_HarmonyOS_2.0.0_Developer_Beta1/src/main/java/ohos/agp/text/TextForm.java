package ohos.agp.text;

public class TextForm {
    private static final Font DEFAULT_FONT = Font.DEFAULT;
    private static final float DEFAULT_LINE_HEIGHT = 0.0f;
    private static final int DEFAULT_TEXTBACKGROUND_COLOR = -1;
    private static final int DEFAULT_TEXT_COLOR = -16777216;
    private static final float DEFAULT_TEXT_RELATIVETEXTSIZE = 1.0f;
    private static final float DEFAULT_TEXT_SCALEX = 1.0f;
    private static final int DEFAULT_TEXT_SIZE = 60;
    private static final boolean DEFAULT_TEXT_STRIKETHROUGH = false;
    private static final boolean DEFAULT_TEXT_SUBSCRIPT = false;
    private static final boolean DEFAULT_TEXT_SUPERSCRIPT = false;
    private static final boolean DEFAULT_TEXT_UNDERLINE = false;
    private static final int TEXT_BGCOLOR_MASK = 32;
    private static final int TEXT_COLOR_MASK = 2;
    private static final int TEXT_FONT = 4;
    private static final int TEXT_LINEHEIGHT_MASK = 64;
    private static final int TEXT_RELATIVETEXTSIZE_MASK = 256;
    private static final int TEXT_SCALEX_MASK = 8;
    private static final int TEXT_SIZE_MASK = 1;
    private static final int TEXT_STRIKETHROUGH_MASK = 16;
    private static final int TEXT_SUBSCRIPT_MASK = 1024;
    private static final int TEXT_SUPERSCRIPT_MASK = 512;
    private static final int TEXT_UNDERLINE_MASK = 128;
    private int mDefaultDirtyFlag = 1951;
    private int mDirtyFlag = 0;
    private Font mFont = DEFAULT_FONT;
    private float mLineHeight = DEFAULT_LINE_HEIGHT;
    private float mRelativeTextSize = 1.0f;
    private float mScaleX = 1.0f;
    private boolean mStrikethrough = false;
    private boolean mSubscript = false;
    private boolean mSuperscript = false;
    private int mTextBGColor = -1;
    private int mTextColor = DEFAULT_TEXT_COLOR;
    private int mTextSize = 60;
    private boolean mUnderline = false;

    public int getDirtyFlag() {
        return this.mDirtyFlag;
    }

    public int getDefaultDirtyFlag() {
        return this.mDefaultDirtyFlag;
    }

    public TextForm setTextSize(int i) {
        if (i > 0) {
            this.mTextSize = i;
            this.mDirtyFlag |= 1;
        }
        return this;
    }

    public int getTextSize() {
        return this.mTextSize;
    }

    public TextForm setTextColor(int i) {
        this.mTextColor = i;
        this.mDirtyFlag |= 2;
        return this;
    }

    public int getTextColor() {
        return this.mTextColor;
    }

    public TextForm setTextFont(Font font) {
        this.mFont = font;
        this.mDirtyFlag |= 4;
        return this;
    }

    public Font getTextFont() {
        return this.mFont;
    }

    public TextForm setScaleX(float f) {
        this.mScaleX = f;
        this.mDirtyFlag |= 8;
        return this;
    }

    public float getScaleX() {
        return this.mScaleX;
    }

    public TextForm setStrikethrough(boolean z) {
        this.mDirtyFlag |= 16;
        this.mStrikethrough = z;
        return this;
    }

    public boolean getStrikethrough() {
        return this.mStrikethrough;
    }

    public TextForm setTextBackgroundColor(int i) {
        this.mTextBGColor = i;
        this.mDirtyFlag |= 32;
        return this;
    }

    public int getTextBackgroundColor() {
        return this.mTextBGColor;
    }

    public TextForm setLineHeight(float f) {
        this.mLineHeight = f;
        this.mDirtyFlag |= 64;
        return this;
    }

    public float getLineHeight() {
        return this.mLineHeight;
    }

    public TextForm setUnderline(boolean z) {
        this.mDirtyFlag |= 128;
        this.mUnderline = z;
        return this;
    }

    public boolean getUnderline() {
        return this.mUnderline;
    }

    public TextForm setRelativeTextSize(float f) {
        this.mRelativeTextSize = f;
        this.mDirtyFlag |= 256;
        return this;
    }

    public float getRelativeTextSize() {
        return this.mRelativeTextSize;
    }

    public TextForm setSuperscript(boolean z) {
        this.mDirtyFlag |= 512;
        this.mSuperscript = z;
        return this;
    }

    public boolean getSuperscript() {
        return this.mSuperscript;
    }

    public TextForm setSubscript(boolean z) {
        this.mDirtyFlag |= 1024;
        this.mSubscript = z;
        return this;
    }

    public boolean getSubscript() {
        return this.mSubscript;
    }
}
