package ohos.bundlemgr.freeinstall;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;
import ohos.appexecfwk.utils.AppLog;
import ohos.hiviewdfx.HiLogLabel;

public class ErrorReminder {
    private static final String BACKGROUND_DRAWABLE_NAME = "rectangle";
    private static final HiLogLabel ERROR_REMINDER = new HiLogLabel(3, 218108160, "ErrorReminder");
    private static final String ERROR_REMINDER_THREAD_NAME = "error reminder thread";
    private static final int FREE_INSTALL_ERROR_CODE_DOWNLOAD_FAILED = 2;
    private static final int FREE_INSTALL_ERROR_CODE_INSTALL_FAILED = 3;
    private static final int FREE_INSTALL_ERROR_CODE_QUERY_FAILED = 1;
    private static final String FREE_INSTALL_ERROR_LABEL_DOWNLOAD_FAILED = "free_install_download_failed";
    private static final String FREE_INSTALL_ERROR_LABEL_INSTALL_FAILED = "free_install_install_failed";
    private static final String FREE_INSTALL_ERROR_LABEL_QUERY_FAILED = "free_install_query_failed";
    private static final int INVALID_RESOURCE_ID = 0;
    private static final String PACKAGE_NAME = "com.huawei.harmonyos.foundation";
    private static final String RESOURCE_TYPE_DRAWABLE = "drawable";
    private static final String RESOURCE_TYPE_STRING = "string";
    private Context context;

    /* access modifiers changed from: private */
    public class ErrorMsg extends TextView {
        public ErrorMsg(Context context) {
            super(context);
        }

        /* access modifiers changed from: protected */
        @Override // android.view.View
        public void onWindowVisibilityChanged(int i) {
            if (i != 0) {
                Looper.myLooper().quit();
            }
        }
    }

    public ErrorReminder(Context context2) {
        this.context = context2;
    }

    private int getResourceID(String str, String str2) {
        Resources resources = this.context.getResources();
        if (resources != null) {
            return resources.getIdentifier(str, str2, PACKAGE_NAME);
        }
        AppLog.e(ERROR_REMINDER, "get resources failed", new Object[0]);
        return 0;
    }

    private String getErrorMsg(int i) {
        String str;
        if (i == 1) {
            str = FREE_INSTALL_ERROR_LABEL_QUERY_FAILED;
        } else if (i == 2) {
            str = FREE_INSTALL_ERROR_LABEL_DOWNLOAD_FAILED;
        } else if (i != 3) {
            AppLog.e(ERROR_REMINDER, "unsupported error code %{public}d", Integer.valueOf(i));
            return null;
        } else {
            str = FREE_INSTALL_ERROR_LABEL_INSTALL_FAILED;
        }
        int resourceID = getResourceID(str, "string");
        if (resourceID != 0) {
            return this.context.getString(resourceID);
        }
        AppLog.e(ERROR_REMINDER, "get resource id failed", new Object[0]);
        return null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean configureToast(Toast toast, int i) {
        int resourceID = getResourceID(BACKGROUND_DRAWABLE_NAME, RESOURCE_TYPE_DRAWABLE);
        if (resourceID == 0) {
            AppLog.e(ERROR_REMINDER, "get resource id failed", new Object[0]);
            return false;
        }
        Drawable drawable = this.context.getDrawable(resourceID);
        if (drawable == null) {
            AppLog.e(ERROR_REMINDER, "get drawable failed", new Object[0]);
            return false;
        }
        String errorMsg = getErrorMsg(i);
        if (errorMsg == null) {
            return false;
        }
        ErrorMsg errorMsg2 = new ErrorMsg(this.context);
        errorMsg2.setText(errorMsg);
        errorMsg2.setBackground(drawable);
        toast.setDuration(0);
        toast.setView(errorMsg2);
        return true;
    }

    public void showErrorMessage(final int i) {
        AppLog.d(ERROR_REMINDER, "errorCode is %{public}d", Integer.valueOf(i));
        if (this.context == null) {
            AppLog.e(ERROR_REMINDER, "context is not set", new Object[0]);
            return;
        }
        Thread thread = new Thread(new Runnable() {
            /* class ohos.bundlemgr.freeinstall.ErrorReminder.AnonymousClass1 */

            @Override // java.lang.Runnable
            public void run() {
                Looper.prepare();
                Toast toast = new Toast(ErrorReminder.this.context);
                if (ErrorReminder.this.configureToast(toast, i)) {
                    toast.show();
                    Looper.loop();
                    return;
                }
                AppLog.e(ErrorReminder.ERROR_REMINDER, "configure toast failed", new Object[0]);
            }
        });
        thread.setName(ERROR_REMINDER_THREAD_NAME);
        thread.setUncaughtExceptionHandler($$Lambda$ErrorReminder$OTUnrn4KXUvCNsb1otIeNL309Q.INSTANCE);
        thread.start();
    }
}
