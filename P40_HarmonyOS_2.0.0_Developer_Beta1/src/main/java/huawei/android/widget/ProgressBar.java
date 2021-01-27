package huawei.android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.RemoteViews;
import java.util.Locale;

@RemoteViews.RemoteView
public class ProgressBar extends android.widget.ProgressBar {
    public ProgressBar(Context context) {
        this(context, null);
    }

    public ProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 16842871);
    }

    public ProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ProgressBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override // android.widget.ProgressBar, android.view.View
    public CharSequence getAccessibilityClassName() {
        if (!isIndeterminate()) {
            return ProgressBar.class.getName();
        }
        return "";
    }

    @Override // android.view.View
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        if (info != null) {
            super.onInitializeAccessibilityNodeInfo(info);
            if (!isIndeterminate()) {
                info.setFocusable(true);
                info.setContentDescription(String.format(Locale.ROOT, "%d%%", Integer.valueOf(getProgress())));
            }
        }
    }
}
