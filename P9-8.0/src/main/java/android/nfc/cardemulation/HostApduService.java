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

    final class MsgHandler extends Handler {
        MsgHandler() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    Bundle dataBundle = msg.getData();
                    if (dataBundle != null) {
                        if (HostApduService.this.mNfcService == null) {
                            HostApduService.this.mNfcService = msg.replyTo;
                        }
                        byte[] apdu = dataBundle.getByteArray("data");
                        if (apdu == null) {
                            Log.e(HostApduService.TAG, "Received MSG_COMMAND_APDU without data.");
                            break;
                        }
                        byte[] responseApdu = HostApduService.this.processCommandApdu(apdu, null);
                        if (responseApdu != null) {
                            if (HostApduService.this.mNfcService != null) {
                                Message responseMsg = Message.obtain(null, 1);
                                Bundle responseBundle = new Bundle();
                                responseBundle.putByteArray("data", responseApdu);
                                responseMsg.setData(responseBundle);
                                responseMsg.replyTo = HostApduService.this.mMessenger;
                                try {
                                    HostApduService.this.mNfcService.send(responseMsg);
                                    break;
                                } catch (RemoteException e) {
                                    Log.e("TAG", "Response not sent; RemoteException calling into NfcService.");
                                    break;
                                }
                            }
                            Log.e(HostApduService.TAG, "Response not sent; service was deactivated.");
                            return;
                        }
                    }
                    return;
                    break;
                case 1:
                    if (HostApduService.this.mNfcService != null) {
                        try {
                            msg.replyTo = HostApduService.this.mMessenger;
                            HostApduService.this.mNfcService.send(msg);
                            break;
                        } catch (RemoteException e2) {
                            Log.e(HostApduService.TAG, "RemoteException calling into NfcService.");
                            break;
                        }
                    }
                    Log.e(HostApduService.TAG, "Response not sent; service was deactivated.");
                    return;
                case 2:
                    HostApduService.this.mNfcService = null;
                    HostApduService.this.onDeactivated(msg.arg1);
                    break;
                case 3:
                    if (HostApduService.this.mNfcService != null) {
                        try {
                            msg.replyTo = HostApduService.this.mMessenger;
                            HostApduService.this.mNfcService.send(msg);
                            break;
                        } catch (RemoteException e3) {
                            Log.e(HostApduService.TAG, "RemoteException calling into NfcService.");
                            break;
                        }
                    }
                    Log.e(HostApduService.TAG, "notifyUnhandled not sent; service was deactivated.");
                    return;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }

    public abstract void onDeactivated(int i);

    public abstract byte[] processCommandApdu(byte[] bArr, Bundle bundle);

    public final IBinder onBind(Intent intent) {
        return this.mMessenger.getBinder();
    }

    public final void sendResponseApdu(byte[] responseApdu) {
        Message responseMsg = Message.obtain(null, 1);
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
            this.mMessenger.send(Message.obtain(null, 3));
        } catch (RemoteException e) {
            Log.e("TAG", "Local messenger has died.");
        }
    }
}
