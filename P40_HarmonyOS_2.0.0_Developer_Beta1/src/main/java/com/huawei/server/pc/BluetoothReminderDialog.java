package com.huawei.server.pc;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.provider.Settings;
import android.util.HwPCUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import com.huawei.hwpartpowerofficeservices.BuildConfig;
import com.huawei.utils.HwPartResourceUtils;

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
        View view = LayoutInflater.from(builder.getContext()).inflate(HwPartResourceUtils.getResourceId("bluetooth_reminder_dialog"), (ViewGroup) null);
        final CheckBox checkBox = (CheckBox) view.findViewById(HwPartResourceUtils.getResourceId("never_notify"));
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            /* class com.huawei.server.pc.BluetoothReminderDialog.AnonymousClass1 */

            @Override // android.widget.CompoundButton.OnCheckedChangeListener
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (checkBox.isChecked()) {
                    int i = type;
                    if (i == 1) {
                        HwPCUtils.bdReport(context, 10032, BuildConfig.FLAVOR);
                    } else if (i == 2) {
                        HwPCUtils.bdReport(context, 10035, BuildConfig.FLAVOR);
                    }
                }
            }
        });
        builder.setView(view);
        setViewForAlertDialog(context, view, builder, res, type);
        setButtonForAlertDialog(context, view, builder, type, checkBox);
        AlertDialog dialog = builder.create();
        dialog.getWindow().setType(2008);
        return dialog;
    }

    private void setViewForAlertDialog(Context context, View view, AlertDialog.Builder builder, Resources res, int type) {
        TextView textView = (TextView) view.findViewById(HwPartResourceUtils.getResourceId("notify_detail"));
        if (type == 1) {
            textView.setText(res.getString(HwPartResourceUtils.getResourceId("bluetooth_notify_disable_bluetooth_tip")));
            builder.setTitle(res.getString(HwPartResourceUtils.getResourceId("bluetooth_notify_dialog_close_title")));
        } else if (type == 2) {
            textView.setText(res.getString(HwPartResourceUtils.getResourceId("bluetooth_notify_enable_bluetooth_tip")));
            builder.setTitle(res.getString(HwPartResourceUtils.getResourceId("bluetooth_notify_dialog_open_title")));
        }
    }

    private void setButtonForAlertDialog(final Context context, View view, AlertDialog.Builder builder, final int type, final CheckBox checkBox) {
        builder.setPositiveButton(HwPartResourceUtils.getResourceId("bluetooth_notify_disable"), new DialogInterface.OnClickListener() {
            /* class com.huawei.server.pc.BluetoothReminderDialog.AnonymousClass2 */

            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int which) {
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (bluetoothAdapter != null) {
                    int i = type;
                    if (i == 1) {
                        bluetoothAdapter.disable();
                        HwPCUtils.bdReport(context, 10033, BuildConfig.FLAVOR);
                    } else if (i == 2) {
                        bluetoothAdapter.enable();
                        HwPCUtils.bdReport(context, 10036, BuildConfig.FLAVOR);
                    }
                }
                if (checkBox.isChecked()) {
                    BluetoothReminderDialog.this.updateSettingsData(type, context);
                }
            }
        });
        builder.setNegativeButton(HwPartResourceUtils.getResourceId("bluetooth_notify_cancel"), new DialogInterface.OnClickListener() {
            /* class com.huawei.server.pc.BluetoothReminderDialog.AnonymousClass3 */

            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int which) {
                if (checkBox.isChecked()) {
                    BluetoothReminderDialog.this.updateSettingsData(type, context);
                }
                int i = type;
                if (i == 1) {
                    HwPCUtils.bdReport(context, 10034, BuildConfig.FLAVOR);
                } else if (i == 2) {
                    HwPCUtils.bdReport(context, 10037, BuildConfig.FLAVOR);
                }
            }
        });
    }

    public void dismissDialog() {
        AlertDialog alertDialog = this.mReminderDialog;
        if (alertDialog != null && alertDialog.isShowing()) {
            this.mReminderDialog.dismiss();
            this.mReminderDialog = null;
        }
    }

    public void showCloseBluetoothTip(Context context) {
        dismissDialog();
        if (context != null && isShowCloseBluetoothTip(context)) {
            this.mReminderDialog = createDialog(context, 1);
            this.mReminderDialog.show();
            this.mReminderDialog.getButton(-1).setText(HwPartResourceUtils.getResourceId("bluetooth_notify_disable"));
            this.mReminderDialog.getButton(-2).setText(HwPartResourceUtils.getResourceId("bluetooth_notify_cancel"));
            HwPCUtils.log(TAG, "Close bluetooth tip is showing.");
        }
    }

    public void showOpenBluetoothTip(Context context) {
        dismissDialog();
        if (context != null && isShowOpenBluetoothTip(context)) {
            this.mReminderDialog = createDialog(context, 2);
            this.mReminderDialog.show();
            this.mReminderDialog.getButton(-1).setText(HwPartResourceUtils.getResourceId("bluetooth_notify_enable"));
            this.mReminderDialog.getButton(-2).setText(HwPartResourceUtils.getResourceId("bluetooth_notify_cancel"));
            HwPCUtils.log(TAG, "Open bluetooth tip is showing.");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateSettingsData(int type, Context context) {
        HwPCUtils.log(TAG, "updateSettingsData , type = " + type);
        if (type == 1) {
            Settings.Secure.putInt(context.getContentResolver(), KEY_CLOSE_BLUETOOTH, 2);
        } else if (type == 2) {
            Settings.Secure.putInt(context.getContentResolver(), KEY_OPEN_BLUETOOTH, 2);
        }
    }

    private boolean isShowOpenBluetoothTip(Context context) {
        if (context != null && Settings.Secure.getInt(context.getContentResolver(), KEY_OPEN_BLUETOOTH, 1) == 1) {
            return true;
        }
        return false;
    }

    private boolean isShowCloseBluetoothTip(Context context) {
        if (context != null && Settings.Secure.getInt(context.getContentResolver(), KEY_CLOSE_BLUETOOTH, 1) == 1) {
            return true;
        }
        return false;
    }
}
