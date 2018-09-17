package android.widget;

import android.content.Context;
import android.text.Layout.Alignment;

public class HwCustTextView {
    public HwCustTextView(Context context, int id) {
    }

    public boolean checkAlignmentChange() {
        return false;
    }

    public Alignment changeStartAlignment(Alignment alignment) {
        return alignment;
    }

    public Alignment changeEndAlignment(Alignment alignment) {
        return alignment;
    }
}
