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

public abstract class HostNfcFService extends Service {
    public static final int DEACTIVATION_LINK_LOSS = 0;
    public static final String KEY_DATA = "data";
    public static final String KEY_MESSENGER = "messenger";
    public static final int MSG_COMMAND_PACKET = 0;
    public static final int MSG_DEACTIVATED = 2;
    public static final int MSG_RESPONSE_PACKET = 1;
    public static final String SERVICE_INTERFACE = "android.nfc.cardemulation.action.HOST_NFCF_SERVICE";
    public static final String SERVICE_META_DATA = "android.nfc.cardemulation.host_nfcf_service";
    static final String TAG = "NfcFService";
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
                        if (HostNfcFService.this.mNfcService == null) {
                            HostNfcFService.this.mNfcService = msg.replyTo;
                        }
                        byte[] packet = dataBundle.getByteArray("data");
                        if (packet == null) {
                            Log.e(HostNfcFService.TAG, "Received MSG_COMMAND_PACKET without data.");
                            break;
                        }
                        byte[] responsePacket = HostNfcFService.this.processNfcFPacket(packet, null);
                        if (HostNfcFService.this.mNfcService != null) {
                            Message responseMsg = Message.obtain(null, 1);
                            Bundle responseBundle = new Bundle();
                            responseBundle.putByteArray("data", responsePacket);
                            responseMsg.setData(responseBundle);
                            responseMsg.replyTo = HostNfcFService.this.mMessenger;
                            try {
                                HostNfcFService.this.mNfcService.send(responseMsg);
                                break;
                            } catch (RemoteException e) {
                                Log.e("TAG", "Response not sent; RemoteException calling into NfcService.");
                                break;
                            }
                        }
                        Log.e(HostNfcFService.TAG, "Response not sent; service was deactivated.");
                        return;
                    }
                    return;
                case 1:
                    if (HostNfcFService.this.mNfcService != null) {
                        try {
                            msg.replyTo = HostNfcFService.this.mMessenger;
                            HostNfcFService.this.mNfcService.send(msg);
                            break;
                        } catch (RemoteException e2) {
                            Log.e(HostNfcFService.TAG, "RemoteException calling into NfcService.");
                            break;
                        }
                    }
                    Log.e(HostNfcFService.TAG, "Response not sent; service was deactivated.");
                    return;
                case 2:
                    HostNfcFService.this.mNfcService = null;
                    HostNfcFService.this.onDeactivated(msg.arg1);
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }

    public abstract void onDeactivated(int i);

    public abstract byte[] processNfcFPacket(byte[] bArr, Bundle bundle);

    public final IBinder onBind(Intent intent) {
        return this.mMessenger.getBinder();
    }

    public final void sendResponsePacket(byte[] responsePacket) {
        Message responseMsg = Message.obtain(null, 1);
        Bundle dataBundle = new Bundle();
        dataBundle.putByteArray("data", responsePacket);
        responseMsg.setData(dataBundle);
        try {
            this.mMessenger.send(responseMsg);
        } catch (RemoteException e) {
            Log.e("TAG", "Local messenger has died.");
        }
    }
}
