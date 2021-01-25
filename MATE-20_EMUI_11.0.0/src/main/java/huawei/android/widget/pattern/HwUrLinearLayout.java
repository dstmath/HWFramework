package huawei.android.widget.pattern;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import java.util.Locale;

public class HwUrLinearLayout extends LinearLayout {
    public HwUrLinearLayout(Context context) {
        super(context);
    }

    public HwUrLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HwUrLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public HwUrLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override // android.view.ViewParent, android.view.View
    public int getLayoutDirection() {
        if ("ur".equals(Locale.getDefault().getLanguage())) {
            return 0;
        }
        return super.getLayoutDirection();
    }
}
