package huawei.android.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.EditText;
import com.huawei.uikit.effect.BuildConfig;

public class CreditCardEditText extends EditText {
    public static final int INTERVAL_DISTANCE = 4;
    private StringBuilder mChangeText;
    private String mGetIntervalText;
    private int mLengthBefore;
    private int mSelectionBefore;

    public CreditCardEditText(Context context) {
        this(context, null);
    }

    public CreditCardEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setImeOptions(33554432);
        setInputType(2);
        this.mChangeText = new StringBuilder();
    }

    /* access modifiers changed from: protected */
    public void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        if ((text == null || this.mChangeText == null || text.toString().equals(this.mChangeText.toString())) ? false : true) {
            this.mChangeText.setLength(0);
            int textLength = text.length();
            for (int i = 0; i < textLength; i++) {
                char intervalChar = text.charAt(i);
                if (intervalChar != ' ') {
                    if ((this.mChangeText.length() - 4) % 5 == 0) {
                        this.mChangeText.append(' ');
                    }
                    this.mChangeText.append(intervalChar);
                }
            }
            this.mSelectionBefore = getSelectionStart();
            this.mLengthBefore = lengthBefore;
            setText(this.mChangeText);
            if (this.mLengthBefore > 0) {
                int i2 = this.mSelectionBefore;
                if (i2 % 5 != 0 || i2 - 1 <= 0) {
                    setSelection(this.mSelectionBefore);
                } else {
                    setSelection(i2 - 1);
                }
            } else {
                int i3 = this.mSelectionBefore;
                if (i3 % 5 == 0) {
                    setSelection(i3 + 1);
                } else {
                    setSelection(i3);
                }
            }
        }
    }

    public String getIntervalText() {
        this.mGetIntervalText = BuildConfig.FLAVOR;
        if (!TextUtils.isEmpty(this.mChangeText)) {
            this.mGetIntervalText = this.mChangeText.toString();
            this.mGetIntervalText = this.mGetIntervalText.replace(" ", BuildConfig.FLAVOR);
        }
        return this.mGetIntervalText;
    }
}
