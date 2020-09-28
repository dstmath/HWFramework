package huawei.com.android.internal.app;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import com.android.internal.app.AlertController;

public class HwAlertController extends AlertController {
    private static final int DEVICE_TYPE_TELEVISION = 2;
    private final Context mContext;

    public HwAlertController(Context context, DialogInterface di, Window window) {
        super(context, di, window);
        this.mContext = context;
    }

    /* access modifiers changed from: protected */
    public void setHuaweiScrollIndicators(boolean hasCustomPanel, boolean hasTopPanel, boolean hasButtonPanel) {
    }

    /* access modifiers changed from: protected */
    public void setupView() {
        ViewGroup.LayoutParams layoutParams;
        HwAlertController.super.setupView();
        if (!(this.mContext.getResources().getInteger(34275377) == 2 || !hasTextTitle() || this.mMessageView == null || (layoutParams = this.mMessageView.getLayoutParams()) == null)) {
            layoutParams.width = -1;
            this.mMessageView.setLayoutParams(layoutParams);
        }
        View decorView = this.mWindow.getDecorView();
        if (decorView != null) {
            decorView.setClipToOutline(true);
        }
    }
}
