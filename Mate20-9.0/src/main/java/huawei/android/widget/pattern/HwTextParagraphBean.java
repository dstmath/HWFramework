package huawei.android.widget.pattern;

public class HwTextParagraphBean {
    public static final int TEXT_TYPE_AUXILIARY = 4;
    public static final int TEXT_TYPE_FIRST_LEVEL = 1;
    public static final int TEXT_TYPE_SECONDARY = 2;
    public static final int TEXT_TYPE_TERTIARY = 3;
    public static final int TEXT_TYPE_TITLE = 0;
    private String text;
    private int type;

    public HwTextParagraphBean(int type2, String text2) {
        this.type = type2;
        this.text = text2;
    }

    /* access modifiers changed from: package-private */
    public int getType() {
        return this.type;
    }

    /* access modifiers changed from: package-private */
    public void setType(int type2) {
        this.type = type2;
    }

    /* access modifiers changed from: package-private */
    public String getText() {
        return this.text;
    }

    /* access modifiers changed from: package-private */
    public void setText(String text2) {
        this.text = text2;
    }
}
