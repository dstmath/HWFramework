package ohos.agp.window.wmc;

import android.view.ViewGroup;
import android.view.Window;
import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.window.wmc.AGPWindowManager;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class AGPCustomWindow extends AGPCommonDialogWindow {
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) LogDomain.END, "AGPWindow");

    public AGPCustomWindow(Context context, int i, int i2) {
        super(context, i);
        Window window = this.mAndroidWindow;
        HiLog.debug(LABEL, "AGPCustomWindow construct func args flag: %{public}d, typeFlag: %{public}d", new Object[]{Integer.valueOf(i), Integer.valueOf(i2)});
        if (i2 == 2000) {
            this.mAndroidParam.type = 2000;
            this.mAndroidParam.width = -1;
            this.mAndroidParam.height = -2;
        } else if (i2 == 2038) {
            this.mAndroidParam.type = 2038;
            this.mAndroidParam.width = 0;
            this.mAndroidParam.height = 0;
            ViewGroup.LayoutParams layoutParams = this.mSurfaceView.getLayoutParams();
            if (layoutParams != null) {
                layoutParams.height = -1;
                layoutParams.width = -1;
                this.mSurfaceView.setLayoutParams(layoutParams);
            }
        } else if (window != null) {
            this.mAndroidParam.token = window.getAttributes().token;
            this.mAndroidParam.type = 2;
            this.mAndroidParam.width = 0;
            this.mAndroidParam.height = 0;
            ViewGroup.LayoutParams layoutParams2 = this.mSurfaceView.getLayoutParams();
            if (layoutParams2 != null) {
                layoutParams2.height = -1;
                layoutParams2.width = -1;
                this.mSurfaceView.setLayoutParams(layoutParams2);
            }
        } else {
            HiLog.error(LABEL, "Only AbiliySlice can create APPLICATION window", new Object[0]);
            throw new AGPWindowManager.BadWindowException("Only AbiliySlice can create APPLICATION window");
        }
    }
}
