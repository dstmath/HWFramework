package huawei.android.widget.pattern;

public class HwTextParagraphBean {
    public static final int TEXT_TYPE_AUXILIARY = 4;
    public static final int TEXT_TYPE_FIRST_LEVEL = 1;
    public static final int TEXT_TYPE_SECONDARY = 2;
    public static final int TEXT_TYPE_TERTIARY = 3;
    public static final int TEXT_TYPE_TITLE = 0;
    private String mText;
    private int mType;

    public HwTextParagraphBean(int type, String text) {
        this.mType = type;
        this.mText = text;
    }

    /* access modifiers changed from: package-private */
    public int getType() {
        return this.mType;
    }

    /* access modifiers changed from: package-private */
    public void setType(int type) {
        this.mType = type;
    }

    /* access modifiers changed from: package-private */
    public String getText() {
        return this.mText;
    }

    /* access modifiers changed from: package-private */
    public void setText(String text) {
        this.mText = text;
    }
}
