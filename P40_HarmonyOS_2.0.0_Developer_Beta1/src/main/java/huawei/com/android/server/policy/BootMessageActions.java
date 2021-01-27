package huawei.com.android.server.policy;

import android.content.Context;
import android.util.Flog;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.WindowManager;
import com.android.server.hidata.arbitration.HwArbitrationDefs;
import com.android.server.wifipro.WifiProCommonUtils;

public class BootMessageActions {
    private static final boolean IS_DEBUG = false;
    private static final String TAG = "BootMessageActions";
    private final Context mContext;
    private HwHotaView mHwHotaView;
    private WindowManager mWindowManager;

    public BootMessageActions(Context context) {
        this.mContext = context;
        createHwHotaView();
    }

    public void showBootMessage(int curr, int total) {
        if (this.mHwHotaView == null) {
            createHwHotaView();
        }
        HwHotaView hwHotaView = this.mHwHotaView;
        if (hwHotaView != null) {
            hwHotaView.setVisibility(0);
            int progress = (int) ((((float) curr) / ((float) total)) * 100.0f);
            String str = this.mContext.getResources().getString(33685801);
            Flog.i((int) WifiProCommonUtils.HTTP_REACHALBE_GOOLE, "showBootMessage progress" + progress);
            this.mHwHotaView.update(str, progress);
        }
    }

    public void hideBootMessage() {
        if (this.mHwHotaView != null) {
            this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
            Flog.i((int) WifiProCommonUtils.HTTP_REACHALBE_GOOLE, "hideBootMessage removeView");
            this.mWindowManager.removeView(this.mHwHotaView);
            this.mHwHotaView = null;
        }
    }

    private void createHwHotaView() {
        Flog.i((int) WifiProCommonUtils.HTTP_REACHALBE_GOOLE, "createHwHotaView");
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(-1, -1, HwArbitrationDefs.MSG_AIRPLANE_MODE_OFF, 16778499, -1);
        lp.screenOrientation = 5;
        this.mHwHotaView = (HwHotaView) LayoutInflater.from(this.mContext).inflate(34013225, (ViewGroup) null);
        this.mHwHotaView.init();
        this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
        this.mWindowManager.addView(this.mHwHotaView, lp);
        this.mHwHotaView.setSystemUiVisibility(16778499 | 4);
    }
}
