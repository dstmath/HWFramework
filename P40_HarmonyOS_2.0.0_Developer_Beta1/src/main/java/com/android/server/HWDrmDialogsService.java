package com.android.server;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.drm.DrmErrorEvent;
import android.drm.DrmInfo;
import android.drm.DrmManagerClient;
import android.net.Uri;
import android.util.Log;
import android.util.Slog;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import com.android.server.appactcontrol.AppActConstant;

public class HWDrmDialogsService {
    private static final int MSG_SHOW_CD_NORIGHTS_DLG = 0;
    private static final int MSG_SHOW_NO_SECURITY_DLG = 2;
    private static final int MSG_SHOW_SD_RENEWAL_DLG = 1;
    private static final String TAG = "HWDrmDialogsService";
    private AlertDialog mAlertDlg = null;
    private Context mContext;
    private DrmManagerClient mDrmManagerClient;
    private String mRightsIssuer = "";
    private Toast mToast = null;

    public HWDrmDialogsService(Context context) {
        this.mContext = context;
    }

    public void start() {
        createDrmErrorListener();
    }

    private void createDrmErrorListener() {
        this.mDrmManagerClient = new DrmManagerClient(this.mContext);
        DrmInfo drmInfo = new DrmInfo(1, new byte[]{0}, "application/vnd.oma.drm.content");
        drmInfo.put("DialogServiceRegister", AppActConstant.VALUE_TRUE);
        this.mDrmManagerClient.processDrmInfo(drmInfo);
        DrmManagerClient.OnErrorListener errorListener = new DrmManagerClient.OnErrorListener() {
            /* class com.android.server.HWDrmDialogsService.AnonymousClass1 */

            @Override // android.drm.DrmManagerClient.OnErrorListener
            public void onError(DrmManagerClient client, DrmErrorEvent event) {
                if (event != null) {
                    String errStr = event.getMessage();
                    if (errStr == null) {
                        errStr = "";
                    }
                    HWDrmDialogsService.this.showDlgType(event, errStr);
                }
            }
        };
        this.mDrmManagerClient.setOnErrorListener(errorListener);
        Slog.i(TAG, "HWDrmDialogsService  errorListener = " + errorListener);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showDlgType(DrmErrorEvent event, String errStr) {
        Slog.i(TAG, "HWDrmDialogsService  start ........");
        Slog.i(TAG, "errStr = " + errStr);
        int type = event.getType();
        if (type == 2001) {
            Slog.i(TAG, "rightIssuer error ........");
            if (errStr.startsWith("showdialog#")) {
                this.mRightsIssuer = errStr.substring("showdialog#".length());
                Slog.i(TAG, "rightIssuer error : rightIssuer = " + this.mRightsIssuer);
                showDlg(1);
            }
        } else if (type == 2002) {
            Slog.i(TAG, "cdNoRights ........");
            if (errStr.startsWith("showdialog#")) {
                showDlg(0);
            }
        } else if (type != 2005) {
            Slog.i(TAG, "errorListener, type = " + event.getType());
        } else if (errStr.startsWith("showdialog#")) {
            showDlg(2);
        }
    }

    private void showDlg(int dialog) {
        if (dialog == 0) {
            showCdNorightsDlg();
        } else if (dialog == 1) {
            showSdRenewalDlg();
        } else if (dialog != 2) {
            Log.e(TAG, "showDlg default");
        } else {
            showNoSecurityDlg();
        }
    }

    private void showCdNorightsDlg() {
        Toast toast = this.mToast;
        if (toast == null) {
            this.mToast = Toast.makeText(this.mContext, 33685717, 1);
        } else {
            toast.setText(33685717);
        }
        this.mToast.show();
    }

    private void showSdRenewalDlg() {
        if (!"".equals(this.mRightsIssuer)) {
            AlertDialog alertDialog = this.mAlertDlg;
            if (alertDialog == null || !alertDialog.isShowing()) {
                createSdRenewalDlgAndShow();
            } else {
                Log.d(TAG, "MSG_SHOW_SD_RENEWAL_DLG dailog has show once");
            }
        } else {
            showRoupdateNullToast();
        }
    }

    private void createSdRenewalDlgAndShow() {
        this.mAlertDlg = new AlertDialog.Builder(this.mContext, 33947691).setCancelable(false).setTitle(17039380).setMessage(33685651).setPositiveButton(17039370, new DialogInterface.OnClickListener() {
            /* class com.android.server.HWDrmDialogsService.AnonymousClass3 */

            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int whichButton) {
                if (HWDrmDialogsService.this.mRightsIssuer.matches("http://.*") || HWDrmDialogsService.this.mRightsIssuer.matches("https://.*")) {
                    HWDrmDialogsService hWDrmDialogsService = HWDrmDialogsService.this;
                    hWDrmDialogsService.startBrowser(hWDrmDialogsService.mRightsIssuer);
                    return;
                }
                HWDrmDialogsService.this.showInvalidRightUrlToast();
            }
        }).setNegativeButton(17039360, new DialogInterface.OnClickListener() {
            /* class com.android.server.HWDrmDialogsService.AnonymousClass2 */

            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        }).create();
        Window window = this.mAlertDlg.getWindow();
        if (window != null) {
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.type = 2003;
            window.setAttributes(lp);
        }
        this.mAlertDlg.show();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showInvalidRightUrlToast() {
        Toast toast = this.mToast;
        if (toast == null) {
            this.mToast = Toast.makeText(this.mContext, 33685718, 1);
        } else {
            toast.setText(33685718);
        }
        this.mToast.show();
    }

    private void showRoupdateNullToast() {
        Toast toast = this.mToast;
        if (toast == null) {
            this.mToast = Toast.makeText(this.mContext, 33685716, 1);
        } else {
            toast.setText(33685716);
        }
        this.mToast.show();
    }

    private void showNoSecurityDlg() {
        Toast toast = this.mToast;
        if (toast == null) {
            this.mToast = Toast.makeText(this.mContext, 33685719, 1);
        } else {
            toast.setText(33685719);
        }
        this.mToast.show();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startBrowser(String rightIssuer) {
        Uri rightsUrl = Uri.parse(rightIssuer);
        Intent intent = new Intent();
        intent.setFlags(268435456);
        intent.setAction("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.BROWSABLE");
        intent.setData(rightsUrl);
        this.mContext.startActivity(intent);
    }
}
