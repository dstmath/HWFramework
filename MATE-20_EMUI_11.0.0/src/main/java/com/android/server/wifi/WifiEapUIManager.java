package com.android.server.wifi;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.util.wifi.HwHiLog;
import android.view.WindowManager;

public class WifiEapUIManager {
    private static final int ERROR_CODE_MAX = 32766;
    private static final int ERROR_CODE_MIN = 32760;
    private static final String TAG = "WifiEapUIManager";
    private Context mContext;
    private Dialog mDialog;

    public WifiEapUIManager(Context context) {
        this.mContext = context;
    }

    public void showDialog(String title, String message) {
        Dialog dialog = this.mDialog;
        if (dialog != null && dialog.isShowing()) {
            this.mDialog.dismiss();
        }
        this.mDialog = new AlertDialog.Builder(this.mContext, 33947691).setTitle(title).setMessage(message).setCancelable(false).setPositiveButton(Resources.getSystem().getString(17039370), (DialogInterface.OnClickListener) null).create();
        this.mDialog.getWindow().setType(2003);
        WindowManager.LayoutParams attrs = this.mDialog.getWindow().getAttributes();
        attrs.privateFlags = 16;
        this.mDialog.getWindow().setAttributes(attrs);
        this.mDialog.show();
    }

    public void showDialog(int errorcode) {
        showDialog(Resources.getSystem().getString(33686254), getMessage(errorcode));
    }

    private String getMessage(int errorcode) {
        String[] errorMessages = Resources.getSystem().getStringArray(33816603);
        if (errorcode >= ERROR_CODE_MIN && errorcode <= ERROR_CODE_MAX) {
            return errorMessages[errorcode - 32760];
        }
        HwHiLog.e(TAG, false, "Error code is not supported. (Error = %{public}d)", new Object[]{Integer.valueOf(errorcode)});
        return "";
    }
}
