package huawei.com.android.internal.app;

import android.content.Context;
import android.content.DialogInterface;
import android.view.ViewGroup;
import android.view.Window;
import com.android.internal.app.AlertController;

public class HwAlertController extends AlertController {
    public HwAlertController(Context context, DialogInterface di, Window window) {
        super(context, di, window);
    }

    /* access modifiers changed from: protected */
    public void setHuaweiScrollIndicators(boolean hasCustomPanel, boolean hasTopPanel, boolean hasButtonPanel) {
    }

    /* access modifiers changed from: protected */
    public void setupView() {
        HwAlertController.super.setupView();
        if (hasTextTitle() && this.mMessageView != null) {
            ViewGroup.LayoutParams lp = this.mMessageView.getLayoutParams();
            if (lp != null) {
                lp.width = -1;
                this.mMessageView.setLayoutParams(lp);
            }
        }
    }
}
