package com.android.server.pc;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.provider.Settings;
import android.util.HwPCUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import com.android.server.hidata.arbitration.HwArbitrationDEFS;

public class BluetoothReminderDialog {
    private static final String KEY_CLOSE_BLUETOOTH = "show-close-bluetooth-tip";
    private static final String KEY_OPEN_BLUETOOTH = "show-open-bluetooth-tip";
    private static final int NOT_SHOW_TIP = 2;
    private static final int SHOW_TIP = 1;
    public static final String TAG = "BluetoothReminderDialog";
    public static final int TYPE_CLOSE_BLUETOOTH = 1;
    public static final int TYPE_OPEN_BLUETOOTH = 2;
    private AlertDialog mReminderDialog = null;

    private AlertDialog createDialog(final Context context, final int type) {
        if (context == null) {
            return null;
        }
        Resources res = context.getResources();
        AlertDialog.Builder builder = new AlertDialog.Builder(context, 33947691);
        View view = LayoutInflater.from(builder.getContext()).inflate(34013290, null);
        final CheckBox checkBox = (CheckBox) view.findViewById(34603347);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!checkBox.isChecked()) {
                    return;
                }
                if (type == 1) {
                    HwPCUtils.bdReport(context, 10032, "");
                } else if (type == 2) {
                    HwPCUtils.bdReport(context, 10035, "");
                }
            }
        });
        TextView textView = (TextView) view.findViewById(34603351);
        switch (type) {
            case 1:
                textView.setText(res.getString(33685943));
                builder.setTitle(res.getString(33685918));
                break;
            case 2:
                textView.setText(res.getString(33685949));
                builder.setTitle(res.getString(33685919));
                break;
        }
        builder.setView(view);
        builder.setPositiveButton(33685921, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (bluetoothAdapter != null) {
                    if (type == 1) {
                        bluetoothAdapter.disable();
                        HwPCUtils.bdReport(context, 10033, "");
                    } else if (type == 2) {
                        bluetoothAdapter.enable();
                        HwPCUtils.bdReport(context, 10036, "");
                    }
                }
                if (checkBox.isChecked()) {
                    BluetoothReminderDialog.this.updateSettingsData(type, context);
                }
            }
        });
        builder.setNegativeButton(33685917, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (checkBox.isChecked()) {
                    BluetoothReminderDialog.this.updateSettingsData(type, context);
                }
                if (type == 1) {
                    HwPCUtils.bdReport(context, 10034, "");
                } else if (type == 2) {
                    HwPCUtils.bdReport(context, 10037, "");
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.getWindow().setType(HwArbitrationDEFS.MSG_MPLINK_BIND_FAIL);
        return dialog;
    }

    public void dismissDialog() {
        if (this.mReminderDialog != null && this.mReminderDialog.isShowing()) {
            this.mReminderDialog.dismiss();
            this.mReminderDialog = null;
        }
    }

    public void showCloseBluetoothTip(Context context) {
        dismissDialog();
        if (context != null && isShowCloseBluetoothTip(context)) {
            this.mReminderDialog = createDialog(context, 1);
            this.mReminderDialog.show();
            this.mReminderDialog.getButton(-1).setText(33685921);
            this.mReminderDialog.getButton(-2).setText(33685917);
            HwPCUtils.log(TAG, "Close bluetooth tip is showing.");
        }
    }

    public void showOpenBluetoothTip(Context context) {
        dismissDialog();
        if (context != null && isShowOpenBluetoothTip(context)) {
            this.mReminderDialog = createDialog(context, 2);
            this.mReminderDialog.show();
            this.mReminderDialog.getButton(-1).setText(33685947);
            this.mReminderDialog.getButton(-2).setText(33685917);
            HwPCUtils.log(TAG, "Open bluetooth tip is showing.");
        }
    }

    /* access modifiers changed from: private */
    public void updateSettingsData(int type, Context context) {
        HwPCUtils.log(TAG, "updateSettingsData , type = " + type);
        switch (type) {
            case 1:
                Settings.Secure.putInt(context.getContentResolver(), KEY_CLOSE_BLUETOOTH, 2);
                return;
            case 2:
                Settings.Secure.putInt(context.getContentResolver(), KEY_OPEN_BLUETOOTH, 2);
                return;
            default:
                return;
        }
    }

    private boolean isShowOpenBluetoothTip(Context context) {
        boolean z = false;
        if (context == null) {
            return false;
        }
        if (Settings.Secure.getInt(context.getContentResolver(), KEY_OPEN_BLUETOOTH, 1) == 1) {
            z = true;
        }
        return z;
    }

    private boolean isShowCloseBluetoothTip(Context context) {
        boolean z = false;
        if (context == null) {
            return false;
        }
        if (Settings.Secure.getInt(context.getContentResolver(), KEY_CLOSE_BLUETOOTH, 1) == 1) {
            z = true;
        }
        return z;
    }
}
