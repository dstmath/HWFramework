package android.service.carrier;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.service.carrier.ICarrierMessagingService.Stub;
import java.util.List;

public abstract class CarrierMessagingService extends Service {
    public static final int DOWNLOAD_STATUS_ERROR = 2;
    public static final int DOWNLOAD_STATUS_OK = 0;
    public static final int DOWNLOAD_STATUS_RETRY_ON_CARRIER_NETWORK = 1;
    public static final int RECEIVE_OPTIONS_DEFAULT = 0;
    public static final int RECEIVE_OPTIONS_DROP = 1;
    public static final int RECEIVE_OPTIONS_SKIP_NOTIFY_WHEN_CREDENTIAL_PROTECTED_STORAGE_UNAVAILABLE = 2;
    public static final int SEND_FLAG_REQUEST_DELIVERY_STATUS = 1;
    public static final int SEND_STATUS_ERROR = 2;
    public static final int SEND_STATUS_OK = 0;
    public static final int SEND_STATUS_RETRY_ON_CARRIER_NETWORK = 1;
    public static final String SERVICE_INTERFACE = "android.service.carrier.CarrierMessagingService";
    private final ICarrierMessagingWrapper mWrapper = new ICarrierMessagingWrapper(this, null);

    public interface ResultCallback<T> {
        void onReceiveResult(T t) throws RemoteException;
    }

    private class ICarrierMessagingWrapper extends Stub {
        /* synthetic */ ICarrierMessagingWrapper(CarrierMessagingService this$0, ICarrierMessagingWrapper -this1) {
            this();
        }

        private ICarrierMessagingWrapper() {
        }

        public void filterSms(MessagePdu pdu, String format, int destPort, int subId, final ICarrierMessagingCallback callback) {
            CarrierMessagingService.this.onReceiveTextSms(pdu, format, destPort, subId, new ResultCallback<Integer>() {
                public void onReceiveResult(Integer options) throws RemoteException {
                    callback.onFilterComplete(options.intValue());
                }
            });
        }

        public void sendTextSms(String text, int subId, String destAddress, int sendSmsFlag, final ICarrierMessagingCallback callback) {
            CarrierMessagingService.this.onSendTextSms(text, subId, destAddress, sendSmsFlag, new ResultCallback<SendSmsResult>() {
                public void onReceiveResult(SendSmsResult result) throws RemoteException {
                    callback.onSendSmsComplete(result.getSendStatus(), result.getMessageRef());
                }
            });
        }

        public void sendDataSms(byte[] data, int subId, String destAddress, int destPort, int sendSmsFlag, final ICarrierMessagingCallback callback) {
            CarrierMessagingService.this.onSendDataSms(data, subId, destAddress, destPort, sendSmsFlag, new ResultCallback<SendSmsResult>() {
                public void onReceiveResult(SendSmsResult result) throws RemoteException {
                    callback.onSendSmsComplete(result.getSendStatus(), result.getMessageRef());
                }
            });
        }

        public void sendMultipartTextSms(List<String> parts, int subId, String destAddress, int sendSmsFlag, final ICarrierMessagingCallback callback) {
            CarrierMessagingService.this.onSendMultipartTextSms(parts, subId, destAddress, sendSmsFlag, new ResultCallback<SendMultipartSmsResult>() {
                public void onReceiveResult(SendMultipartSmsResult result) throws RemoteException {
                    callback.onSendMultipartSmsComplete(result.getSendStatus(), result.getMessageRefs());
                }
            });
        }

        public void sendMms(Uri pduUri, int subId, Uri location, final ICarrierMessagingCallback callback) {
            CarrierMessagingService.this.onSendMms(pduUri, subId, location, new ResultCallback<SendMmsResult>() {
                public void onReceiveResult(SendMmsResult result) throws RemoteException {
                    callback.onSendMmsComplete(result.getSendStatus(), result.getSendConfPdu());
                }
            });
        }

        public void downloadMms(Uri pduUri, int subId, Uri location, final ICarrierMessagingCallback callback) {
            CarrierMessagingService.this.onDownloadMms(pduUri, subId, location, new ResultCallback<Integer>() {
                public void onReceiveResult(Integer result) throws RemoteException {
                    callback.onDownloadMmsComplete(result.intValue());
                }
            });
        }
    }

    public static final class SendMmsResult {
        private byte[] mSendConfPdu;
        private int mSendStatus;

        public SendMmsResult(int sendStatus, byte[] sendConfPdu) {
            this.mSendStatus = sendStatus;
            this.mSendConfPdu = sendConfPdu;
        }

        public int getSendStatus() {
            return this.mSendStatus;
        }

        public byte[] getSendConfPdu() {
            return this.mSendConfPdu;
        }
    }

    public static final class SendMultipartSmsResult {
        private final int[] mMessageRefs;
        private final int mSendStatus;

        public SendMultipartSmsResult(int sendStatus, int[] messageRefs) {
            this.mSendStatus = sendStatus;
            this.mMessageRefs = messageRefs;
        }

        public int[] getMessageRefs() {
            return this.mMessageRefs;
        }

        public int getSendStatus() {
            return this.mSendStatus;
        }
    }

    public static final class SendSmsResult {
        private final int mMessageRef;
        private final int mSendStatus;

        public SendSmsResult(int sendStatus, int messageRef) {
            this.mSendStatus = sendStatus;
            this.mMessageRef = messageRef;
        }

        public int getMessageRef() {
            return this.mMessageRef;
        }

        public int getSendStatus() {
            return this.mSendStatus;
        }
    }

    @Deprecated
    public void onFilterSms(MessagePdu pdu, String format, int destPort, int subId, ResultCallback<Boolean> callback) {
        try {
            callback.onReceiveResult(Boolean.valueOf(true));
        } catch (RemoteException e) {
        }
    }

    public void onReceiveTextSms(MessagePdu pdu, String format, int destPort, int subId, final ResultCallback<Integer> callback) {
        onFilterSms(pdu, format, destPort, subId, new ResultCallback<Boolean>() {
            public void onReceiveResult(Boolean result) throws RemoteException {
                callback.onReceiveResult(Integer.valueOf(result.booleanValue() ? 0 : 3));
            }
        });
    }

    @Deprecated
    public void onSendTextSms(String text, int subId, String destAddress, ResultCallback<SendSmsResult> callback) {
        try {
            callback.onReceiveResult(new SendSmsResult(1, 0));
        } catch (RemoteException e) {
        }
    }

    public void onSendTextSms(String text, int subId, String destAddress, int sendSmsFlag, ResultCallback<SendSmsResult> callback) {
        onSendTextSms(text, subId, destAddress, callback);
    }

    @Deprecated
    public void onSendDataSms(byte[] data, int subId, String destAddress, int destPort, ResultCallback<SendSmsResult> callback) {
        try {
            callback.onReceiveResult(new SendSmsResult(1, 0));
        } catch (RemoteException e) {
        }
    }

    public void onSendDataSms(byte[] data, int subId, String destAddress, int destPort, int sendSmsFlag, ResultCallback<SendSmsResult> callback) {
        onSendDataSms(data, subId, destAddress, destPort, callback);
    }

    @Deprecated
    public void onSendMultipartTextSms(List<String> list, int subId, String destAddress, ResultCallback<SendMultipartSmsResult> callback) {
        try {
            callback.onReceiveResult(new SendMultipartSmsResult(1, null));
        } catch (RemoteException e) {
        }
    }

    public void onSendMultipartTextSms(List<String> parts, int subId, String destAddress, int sendSmsFlag, ResultCallback<SendMultipartSmsResult> callback) {
        onSendMultipartTextSms(parts, subId, destAddress, callback);
    }

    public void onSendMms(Uri pduUri, int subId, Uri location, ResultCallback<SendMmsResult> callback) {
        try {
            callback.onReceiveResult(new SendMmsResult(1, null));
        } catch (RemoteException e) {
        }
    }

    public void onDownloadMms(Uri contentUri, int subId, Uri location, ResultCallback<Integer> callback) {
        try {
            callback.onReceiveResult(Integer.valueOf(1));
        } catch (RemoteException e) {
        }
    }

    public IBinder onBind(Intent intent) {
        if (SERVICE_INTERFACE.equals(intent.getAction())) {
            return this.mWrapper;
        }
        return null;
    }
}
