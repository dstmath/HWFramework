package huawei.com.android.internal.app;

import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import com.android.internal.app.AlertController;
import com.huawei.android.view.WindowManagerEx;

public class HwAlertController extends AlertController {
    private static final int DEVICE_TYPE_TELEVISION = 2;
    private static final String TAG = "AlertController";
    private final Context mContext;

    public HwAlertController(Context context, DialogInterface di, Window window) {
        super(context, di, window);
        this.mContext = context;
        if (WindowManagerEx.getBlurFeatureEnabled()) {
            WindowManager.LayoutParams layoutParams = this.mWindow.getAttributes();
            layoutParams.hwFlags |= 33554432;
            layoutParams.hwBehindLayerBlurStyle = 2;
            this.mWindow.setAttributes(layoutParams);
        }
    }

    /* access modifiers changed from: protected */
    public void setHuaweiScrollIndicators(boolean hasCustomPanel, boolean hasTopPanel, boolean hasButtonPanel) {
    }

    /* access modifiers changed from: protected */
    public void setupView() {
        ViewGroup.LayoutParams layoutParams;
        HwAlertController.super.setupView();
        if (!(this.mContext.getResources().getInteger(34275378) == 2 || !hasTextTitle() || this.mMessageView == null || (layoutParams = this.mMessageView.getLayoutParams()) == null)) {
            layoutParams.width = -1;
            this.mMessageView.setLayoutParams(layoutParams);
        }
        View decorView = this.mWindow.getDecorView();
        if (decorView != null) {
            decorView.setClipToOutline(true);
        }
        if (WindowManagerEx.getBlurFeatureEnabled()) {
            setBlurEffect();
        }
    }

    private void setBlurEffect() {
        View decorView = this.mWindow.getDecorView();
        if (decorView == null) {
            Log.e(TAG, "BlurFeature, decorView is null.");
        } else {
            decorView.setBlurEnabled(true);
        }
    }
}
