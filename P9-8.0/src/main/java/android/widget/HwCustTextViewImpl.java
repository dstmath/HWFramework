package android.widget;

import android.content.Context;
import android.os.SystemProperties;
import android.text.Layout.Alignment;

public class HwCustTextViewImpl extends HwCustTextView {
    boolean mIsChromeUrlView = SystemProperties.getBoolean("ro.config.changeRtlAlignment", false);

    public HwCustTextViewImpl(Context context, int id) {
        boolean z = true;
        if (this.mIsChromeUrlView) {
            if (!(id == getId(context, "url_bar", "id", "com.android.chrome") || id == getId(context, "trailing_text", "id", "com.android.chrome"))) {
                z = false;
            }
            this.mIsChromeUrlView = z;
        }
    }

    public boolean checkAlignmentChange() {
        return this.mIsChromeUrlView;
    }

    public Alignment changeStartAlignment(Alignment alignment) {
        return Alignment.ALIGN_NORMAL;
    }

    public Alignment changeEndAlignment(Alignment alignment) {
        return Alignment.ALIGN_OPPOSITE;
    }

    public int getId(Context context, String name, String type, String packageName) {
        return context.getResources().getIdentifier(name, type, packageName);
    }
}
