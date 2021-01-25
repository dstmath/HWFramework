package com.android.internal.telephony;

import android.app.ActivityThread;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothMapClient;
import android.bluetooth.BluetoothProfile;
import android.net.Uri;
import android.telephony.SubscriptionInfo;
import android.util.Log;

public class BtSmsInterfaceManager {
    private static final String LOG_TAG = "BtSmsInterfaceManager";

    public void sendText(String destAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, SubscriptionInfo info) {
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            sendErrorInPendingIntent(sentIntent, 4);
            return;
        }
        BluetoothDevice device = btAdapter.getRemoteDevice(info.getIccId());
        if (device == null) {
            Log.d(LOG_TAG, "Bluetooth device addr invalid: " + info.getIccId());
            sendErrorInPendingIntent(sentIntent, 4);
            return;
        }
        btAdapter.getProfileProxy(ActivityThread.currentApplication().getApplicationContext(), new MapMessageSender(destAddr, text, device, sentIntent, deliveryIntent), 18);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendErrorInPendingIntent(PendingIntent intent, int errorCode) {
        if (intent != null) {
            try {
                intent.send(errorCode);
            } catch (PendingIntent.CanceledException e) {
                Log.d(LOG_TAG, "PendingIntent.CanceledException: " + e.getMessage());
            }
        }
    }

    private class MapMessageSender implements BluetoothProfile.ServiceListener {
        final PendingIntent mDeliveryIntent;
        final Uri[] mDestAddr;
        final BluetoothDevice mDevice;
        private String mMessage;
        final PendingIntent mSentIntent;

        MapMessageSender(String destAddr, String message, BluetoothDevice device, PendingIntent sentIntent, PendingIntent deliveryIntent) {
            this.mDestAddr = new Uri[]{new Uri.Builder().appendPath(destAddr).scheme("tel").build()};
            this.mMessage = message;
            this.mDevice = device;
            this.mSentIntent = sentIntent;
            this.mDeliveryIntent = deliveryIntent;
        }

        @Override // android.bluetooth.BluetoothProfile.ServiceListener
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            Log.d(BtSmsInterfaceManager.LOG_TAG, "Service connected");
            if (profile == 18) {
                BluetoothMapClient mapProfile = (BluetoothMapClient) proxy;
                if (this.mMessage != null) {
                    Log.d(BtSmsInterfaceManager.LOG_TAG, "Sending message thru bluetooth");
                    mapProfile.sendMessage(this.mDevice, this.mDestAddr, this.mMessage, this.mSentIntent, this.mDeliveryIntent);
                    this.mMessage = null;
                }
                BluetoothAdapter.getDefaultAdapter().closeProfileProxy(18, mapProfile);
            }
        }

        @Override // android.bluetooth.BluetoothProfile.ServiceListener
        public void onServiceDisconnected(int profile) {
            if (this.mMessage != null) {
                Log.d(BtSmsInterfaceManager.LOG_TAG, "Bluetooth disconnected before sending the message");
                BtSmsInterfaceManager.this.sendErrorInPendingIntent(this.mSentIntent, 4);
                this.mMessage = null;
            }
        }
    }
}
