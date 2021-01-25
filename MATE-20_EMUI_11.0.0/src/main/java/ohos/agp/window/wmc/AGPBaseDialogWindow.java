package ohos.agp.window.wmc;

import android.view.WindowManager;
import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.window.wmc.AGPWindowManager;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class AGPBaseDialogWindow extends AGPWindow {
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) LogDomain.END, "AGPWindow");
    protected WindowManager mAndroidWindowManager;
    protected DialogDestoryListener mDialogDestoryListener;
    protected DialogListener mListener;

    public interface DialogDestoryListener {
        void dialogDestroy();
    }

    public interface DialogListener {
        boolean isTouchOutside();
    }

    public AGPBaseDialogWindow(Context context, int i) {
        super(context, i);
        if (this.mAndroidContext != null) {
            Object systemService = this.mAndroidContext.getSystemService("window");
            if (systemService instanceof WindowManager) {
                this.mAndroidWindowManager = (WindowManager) systemService;
                return;
            }
            throw new AGPWindowManager.BadWindowException("Obj getSystemService is not instance of WindowManager");
        }
        throw new AGPWindowManager.BadWindowException("AGPBaseDialogWindow: mAndroidContext is null.");
    }

    public void setDialogListener(DialogListener dialogListener) {
        this.mListener = dialogListener;
    }

    public void setDialogSize(int i, int i2) {
        HiLog.debug(LABEL, "enter setDialogSize", new Object[0]);
        if (this.mAndroidParam == null) {
            HiLog.error(LABEL, "setDialogSize failed for mAndroidParam is null", new Object[0]);
            return;
        }
        this.mAndroidParam.width = i;
        this.mAndroidParam.height = i2;
    }

    public void setDialogDestoryListener(DialogDestoryListener dialogDestoryListener) {
        this.mDialogDestoryListener = dialogDestoryListener;
    }
}
