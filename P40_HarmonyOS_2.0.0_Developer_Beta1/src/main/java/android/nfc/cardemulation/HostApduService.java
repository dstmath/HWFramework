package android.nfc.cardemulation;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public abstract class HostApduService extends Service {
    public static final int DEACTIVATION_DESELECTED = 1;
    public static final int DEACTIVATION_LINK_LOSS = 0;
    public static final String KEY_DATA = "data";
    public static final int MSG_COMMAND_APDU = 0;
    public static final int MSG_DEACTIVATED = 2;
    public static final int MSG_RESPONSE_APDU = 1;
    public static final int MSG_UNHANDLED = 3;
    public static final String SERVICE_INTERFACE = "android.nfc.cardemulation.action.HOST_APDU_SERVICE";
    public static final String SERVICE_META_DATA = "android.nfc.cardemulation.host_apdu_service";
    static final String TAG = "ApduService";
    final Messenger mMessenger = new Messenger(new MsgHandler());
    Messenger mNfcService = null;

    public abstract void onDeactivated(int i);

    public abstract byte[] processCommandApdu(byte[] bArr, Bundle bundle);

    final class MsgHandler extends Handler {
        MsgHandler() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 0) {
                Bundle dataBundle = msg.getData();
                if (dataBundle != null) {
                    if (HostApduService.this.mNfcService == null) {
                        HostApduService.this.mNfcService = msg.replyTo;
                    }
                    byte[] apdu = dataBundle.getByteArray("data");
                    if (apdu != null) {
                        byte[] responseApdu = HostApduService.this.processCommandApdu(apdu, null);
                        if (responseApdu == null) {
                            return;
                        }
                        if (HostApduService.this.mNfcService == null) {
                            Log.e(HostApduService.TAG, "Response not sent; service was deactivated.");
                            return;
                        }
                        Message responseMsg = Message.obtain((Handler) null, 1);
                        Bundle responseBundle = new Bundle();
                        responseBundle.putByteArray("data", responseApdu);
                        responseMsg.setData(responseBundle);
                        responseMsg.replyTo = HostApduService.this.mMessenger;
                        try {
                            HostApduService.this.mNfcService.send(responseMsg);
                        } catch (RemoteException e) {
                            Log.e("TAG", "Response not sent; RemoteException calling into NfcService.");
                        }
                    } else {
                        Log.e(HostApduService.TAG, "Received MSG_COMMAND_APDU without data.");
                    }
                }
            } else if (i != 1) {
                if (i == 2) {
                    HostApduService hostApduService = HostApduService.this;
                    hostApduService.mNfcService = null;
                    hostApduService.onDeactivated(msg.arg1);
                } else if (i != 3) {
                    super.handleMessage(msg);
                } else if (HostApduService.this.mNfcService == null) {
                    Log.e(HostApduService.TAG, "notifyUnhandled not sent; service was deactivated.");
                } else {
                    try {
                        msg.replyTo = HostApduService.this.mMessenger;
                        HostApduService.this.mNfcService.send(msg);
                    } catch (RemoteException e2) {
                        Log.e(HostApduService.TAG, "RemoteException calling into NfcService.");
                    }
                }
            } else if (HostApduService.this.mNfcService == null) {
                Log.e(HostApduService.TAG, "Response not sent; service was deactivated.");
            } else {
                try {
                    msg.replyTo = HostApduService.this.mMessenger;
                    HostApduService.this.mNfcService.send(msg);
                } catch (RemoteException e3) {
                    Log.e(HostApduService.TAG, "RemoteException calling into NfcService.");
                }
            }
        }
    }

    @Override // android.app.Service
    public final IBinder onBind(Intent intent) {
        return this.mMessenger.getBinder();
    }

    public final void sendResponseApdu(byte[] responseApdu) {
        Message responseMsg = Message.obtain((Handler) null, 1);
        Bundle dataBundle = new Bundle();
        dataBundle.putByteArray("data", responseApdu);
        responseMsg.setData(dataBundle);
        try {
            this.mMessenger.send(responseMsg);
        } catch (RemoteException e) {
            Log.e("TAG", "Local messenger has died.");
        }
    }

    public final void notifyUnhandled() {
        try {
            this.mMessenger.send(Message.obtain((Handler) null, 3));
        } catch (RemoteException e) {
            Log.e("TAG", "Local messenger has died.");
        }
    }
}
