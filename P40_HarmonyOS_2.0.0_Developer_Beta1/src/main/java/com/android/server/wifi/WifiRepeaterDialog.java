package com.android.server.wifi;

import android.app.AlertDialog;
import android.app.StatusBarManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.IWifiRepeaterConfirmListener;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.wifi.HwHiLog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

public class WifiRepeaterDialog {
    private static final int MSG_ALLOWED_BUT_P2P_CONNECTED = 1;
    private static final int MSG_ALLOWED_BUT_SSID_SAME_WITH_STA = 3;
    private static final int MSG_DISALLOWED_AS_WIFI_EAP_TLS = 2;
    private static final String NETSHARE_CANNOT_SHARE_SHOW_RECORD = "netshare_cannot_share_tip_show_record";
    private static final String TAG = "WifiRepeaterDialog";
    private static WifiRepeaterDialog mWifiRepeaterDialog = null;
    private boolean isDialogShowing = false;
    private Handler mConfirmWifiRepeaterHandler = new Handler() {
        /* class com.android.server.wifi.WifiRepeaterDialog.AnonymousClass1 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (WifiRepeaterDialog.this.mConfirmWifiRepeaterHandler.hasMessages(1)) {
                WifiRepeaterDialog.this.mConfirmWifiRepeaterHandler.removeMessages(1);
            }
            if (WifiRepeaterDialog.this.mConfirmWifiRepeaterHandler.hasMessages(2)) {
                WifiRepeaterDialog.this.mConfirmWifiRepeaterHandler.removeMessages(2);
            }
            if (WifiRepeaterDialog.this.mConfirmWifiRepeaterHandler.hasMessages(3)) {
                WifiRepeaterDialog.this.mConfirmWifiRepeaterHandler.removeMessages(3);
            }
            HwHiLog.i(WifiRepeaterDialog.TAG, false, "mConfirmWifiRepeaterHandler recieve msg=%{public}d", new Object[]{Integer.valueOf(msg.what)});
            WifiRepeaterDialog.this.showWifiRepeaterDialog(msg.what, (IWifiRepeaterConfirmListener) msg.obj);
        }
    };
    private Context mContext;
    private AlertDialog mWifiRepeaterConfirmDialog = null;
    private WifiStateReceiver mWifiStateReceiver;

    private WifiRepeaterDialog(Context context) {
        this.mContext = context;
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        filter.addAction("android.net.wifi.STATE_CHANGE");
        filter.addAction("android.intent.action.CLOSE_SYSTEM_DIALOGS");
        this.mWifiStateReceiver = new WifiStateReceiver();
        this.mContext.registerReceiver(this.mWifiStateReceiver, filter);
    }

    public static WifiRepeaterDialog createWifiRepeaterDialog(Context context) {
        if (mWifiRepeaterDialog == null) {
            mWifiRepeaterDialog = new WifiRepeaterDialog(context);
        }
        return mWifiRepeaterDialog;
    }

    private AlertDialog.Builder getDialogBuilder(int msgType, DialogInterface.OnClickListener positiveListener, DialogInterface.OnClickListener negativeListener) {
        int positiveTxtResId;
        int msgResId;
        AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext, 33947691);
        View view = LayoutInflater.from(builder.getContext()).inflate(34013447, (ViewGroup) null);
        ((TextView) view.findViewById(34603129)).setVisibility(8);
        CheckBox checkBox = (CheckBox) view.findViewById(34603224);
        if (msgType == 2) {
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                /* class com.android.server.wifi.WifiRepeaterDialog.AnonymousClass2 */

                @Override // android.widget.CompoundButton.OnCheckedChangeListener
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    HwHiLog.d(WifiRepeaterDialog.TAG, false, "click record isChecked:%{public}s", new Object[]{String.valueOf(isChecked)});
                    Settings.Global.putString(WifiRepeaterDialog.this.mContext.getContentResolver(), WifiRepeaterDialog.NETSHARE_CANNOT_SHARE_SHOW_RECORD, String.valueOf(isChecked));
                }
            });
            msgResId = 33686074;
            positiveTxtResId = 33686073;
        } else if (msgType == 1) {
            checkBox.setVisibility(8);
            msgResId = 33686071;
            positiveTxtResId = 33686070;
        } else if (msgType == 3) {
            checkBox.setVisibility(8);
            msgResId = 33686072;
            positiveTxtResId = 33686111;
        } else {
            HwHiLog.d(TAG, false, "mConfirmWifiRepeaterHandler recieve other msg", new Object[0]);
            return null;
        }
        builder.setView(view);
        builder.setMessage(msgResId);
        builder.setPositiveButton(positiveTxtResId, positiveListener);
        builder.setNegativeButton(33685887, negativeListener);
        return builder;
    }

    private AlertDialog getDialog(final int msgType, final IWifiRepeaterConfirmListener listener) {
        if (listener == null) {
            HwHiLog.e(TAG, false, "get dialog when listener is null", new Object[0]);
            return null;
        }
        AlertDialog.Builder builder = getDialogBuilder(msgType, new DialogInterface.OnClickListener() {
            /* class com.android.server.wifi.WifiRepeaterDialog.AnonymousClass3 */

            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int which) {
                WifiRepeaterDialog.this.isDialogShowing = false;
                try {
                    if (msgType == 2) {
                        listener.onShareMobile();
                    } else if (msgType == 1) {
                        listener.onDisconnectP2p();
                    } else if (msgType == 3) {
                        listener.onRenameSsid();
                    } else {
                        HwHiLog.d(WifiRepeaterDialog.TAG, false, "Other msg to click positive button", new Object[0]);
                    }
                } catch (RemoteException e) {
                    HwHiLog.e(WifiRepeaterDialog.TAG, false, "Exceptions happen when click positive button", new Object[0]);
                }
            }
        }, new DialogInterface.OnClickListener() {
            /* class com.android.server.wifi.WifiRepeaterDialog.AnonymousClass4 */

            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int which) {
                WifiRepeaterDialog.this.isDialogShowing = false;
                try {
                    listener.onCancel();
                } catch (RemoteException e) {
                    HwHiLog.e(WifiRepeaterDialog.TAG, false, "Exceptions happen when click negative button", new Object[0]);
                }
            }
        });
        if (builder != null) {
            return builder.create();
        }
        HwHiLog.e(TAG, false, "builder is null", new Object[0]);
        return null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dismissDialog() {
        this.mWifiRepeaterConfirmDialog.dismiss();
        this.isDialogShowing = false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showWifiRepeaterDialog(int msgType, IWifiRepeaterConfirmListener listener) {
        if (listener == null) {
            HwHiLog.e(TAG, false, "showWifiRepeaterDialog called when listener is null", new Object[0]);
            return;
        }
        AlertDialog dialog = getDialog(msgType, listener);
        if (dialog == null) {
            HwHiLog.e(TAG, false, "dialog is null", new Object[0]);
            return;
        }
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            /* class com.android.server.wifi.WifiRepeaterDialog.AnonymousClass5 */

            @Override // android.content.DialogInterface.OnKeyListener
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                HwHiLog.d(WifiRepeaterDialog.TAG, false, "KeyListener keyCode=%{public}d", new Object[]{Integer.valueOf(keyCode)});
                WifiRepeaterDialog.this.dismissDialog();
                return true;
            }
        });
        dialog.getWindow().setType(2003);
        WindowManager.LayoutParams attrs = dialog.getWindow().getAttributes();
        attrs.privateFlags = 16;
        dialog.getWindow().setAttributes(attrs);
        dialog.show();
        this.mWifiRepeaterConfirmDialog = dialog;
    }

    public void confirmWifiRepeater(int mode, IWifiRepeaterConfirmListener listener) {
        AlertDialog alertDialog;
        if (listener == null) {
            HwHiLog.e(TAG, false, "confirmWifiRepeater called when listener is null", new Object[0]);
            return;
        }
        StatusBarManager statusBarManager = (StatusBarManager) this.mContext.getSystemService("statusbar");
        if (statusBarManager != null) {
            statusBarManager.collapsePanels();
        }
        if (this.isDialogShowing || ((alertDialog = this.mWifiRepeaterConfirmDialog) != null && alertDialog.isShowing())) {
            HwHiLog.i(TAG, false, "WifiRepeater is showing, do not show again isDialogShowing=%{public}s", new Object[]{String.valueOf(this.isDialogShowing)});
            return;
        }
        this.isDialogShowing = true;
        if (mode != 2) {
            if (mode == 10) {
                Handler handler = this.mConfirmWifiRepeaterHandler;
                handler.sendMessage(handler.obtainMessage(1, 0, 0, listener));
            } else if (mode != 12) {
                HwHiLog.d(TAG, false, "confirmWifiRepeater mode=%{public}d", new Object[]{Integer.valueOf(mode)});
            } else {
                Handler handler2 = this.mConfirmWifiRepeaterHandler;
                handler2.sendMessage(handler2.obtainMessage(3, 0, 0, listener));
            }
        } else if (Boolean.parseBoolean(Settings.Global.getString(this.mContext.getContentResolver(), NETSHARE_CANNOT_SHARE_SHOW_RECORD))) {
            this.isDialogShowing = false;
            try {
                listener.onShareMobile();
            } catch (RemoteException e) {
                HwHiLog.e(TAG, false, "Exceptions happen onShareMobile", new Object[0]);
            }
        } else {
            Handler handler3 = this.mConfirmWifiRepeaterHandler;
            handler3.sendMessage(handler3.obtainMessage(2, 0, 0, listener));
        }
    }

    private class WifiStateReceiver extends BroadcastReceiver {
        private WifiStateReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null && WifiRepeaterDialog.this.mWifiRepeaterConfirmDialog != null && WifiRepeaterDialog.this.mWifiRepeaterConfirmDialog.isShowing()) {
                String action = intent.getAction();
                if ("android.net.wifi.WIFI_STATE_CHANGED".equals(action)) {
                    if (intent.getIntExtra("wifi_state", 4) == 1) {
                        WifiRepeaterDialog.this.dismissDialog();
                    }
                } else if ("android.net.wifi.STATE_CHANGE".equals(action)) {
                    NetworkInfo info = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    if (info == null) {
                        HwHiLog.w(WifiRepeaterDialog.TAG, false, "network info is null", new Object[0]);
                    } else if (NetworkInfo.DetailedState.DISCONNECTED.equals(info.getDetailedState())) {
                        WifiRepeaterDialog.this.dismissDialog();
                    }
                } else if ("android.intent.action.CLOSE_SYSTEM_DIALOGS".equals(action)) {
                    WifiRepeaterDialog.this.dismissDialog();
                } else {
                    HwHiLog.d(WifiRepeaterDialog.TAG, false, "ActionReceiver other action", new Object[0]);
                }
            }
        }
    }
}
