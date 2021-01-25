package android.widget;

import android.content.Context;
import android.text.Layout;

public class HwCustTextView {
    public HwCustTextView() {
    }

    public HwCustTextView(Context context, int id) {
    }

    public boolean checkAlignmentChange() {
        return false;
    }

    public Layout.Alignment changeStartAlignment(Layout.Alignment alignment) {
        return alignment;
    }

    public Layout.Alignment changeEndAlignment(Layout.Alignment alignment) {
        return alignment;
    }
}
