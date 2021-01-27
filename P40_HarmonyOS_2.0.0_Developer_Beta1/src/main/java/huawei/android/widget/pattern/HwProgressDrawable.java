package huawei.android.widget.pattern;

import android.content.res.Resources;
import android.graphics.Bitmap;
import com.android.internal.view.menu.ProgressDrawable;

public class HwProgressDrawable extends ProgressDrawable {
    public HwProgressDrawable(Resources res, Bitmap bitmap) {
        super(res, bitmap);
    }

    /* access modifiers changed from: protected */
    public void setColor(int color) {
        HwProgressDrawable.super.setColor(color);
    }
}
