package android.widget;

import android.content.Context;
import android.os.SystemProperties;
import android.text.Layout;

public class HwCustTextViewImpl extends HwCustTextView {
    boolean mIsChromeUrlView = SystemProperties.getBoolean("ro.config.changeRtlAlignment", false);

    public HwCustTextViewImpl(Context context, int id) {
        boolean z = false;
        if (this.mIsChromeUrlView) {
            this.mIsChromeUrlView = (id == getId(context, "url_bar", "id", "com.android.chrome") || id == getId(context, "trailing_text", "id", "com.android.chrome")) ? true : z;
        }
    }

    public boolean checkAlignmentChange() {
        return this.mIsChromeUrlView;
    }

    public Layout.Alignment changeStartAlignment(Layout.Alignment alignment) {
        return Layout.Alignment.ALIGN_NORMAL;
    }

    public Layout.Alignment changeEndAlignment(Layout.Alignment alignment) {
        return Layout.Alignment.ALIGN_OPPOSITE;
    }

    public int getId(Context context, String name, String type, String packageName) {
        return context.getResources().getIdentifier(name, type, packageName);
    }
}
