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
    private final ICarrierMessagingWrapper mWrapper;

    public interface ResultCallback<T> {
        void onReceiveResult(T t) throws RemoteException;
    }

    /* renamed from: android.service.carrier.CarrierMessagingService.1 */
    class AnonymousClass1 implements ResultCallback<Boolean> {
        final /* synthetic */ ResultCallback val$callback;

        AnonymousClass1(ResultCallback val$callback) {
            this.val$callback = val$callback;
        }

        public void onReceiveResult(Boolean result) throws RemoteException {
            this.val$callback.onReceiveResult(Integer.valueOf(result.booleanValue() ? CarrierMessagingService.SEND_STATUS_OK : 3));
        }
    }

    private class ICarrierMessagingWrapper extends Stub {

        /* renamed from: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.1 */
        class AnonymousClass1 implements ResultCallback<Integer> {
            final /* synthetic */ ICarrierMessagingCallback val$callback;

            AnonymousClass1(ICarrierMessagingCallback val$callback) {
                this.val$callback = val$callback;
            }

            public void onReceiveResult(Integer options) throws RemoteException {
                this.val$callback.onFilterComplete(options.intValue());
            }
        }

        /* renamed from: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.2 */
        class AnonymousClass2 implements ResultCallback<SendSmsResult> {
            final /* synthetic */ ICarrierMessagingCallback val$callback;

            AnonymousClass2(ICarrierMessagingCallback val$callback) {
                this.val$callback = val$callback;
            }

            public void onReceiveResult(SendSmsResult result) throws RemoteException {
                this.val$callback.onSendSmsComplete(result.getSendStatus(), result.getMessageRef());
            }
        }

        /* renamed from: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.3 */
        class AnonymousClass3 implements ResultCallback<SendSmsResult> {
            final /* synthetic */ ICarrierMessagingCallback val$callback;

            AnonymousClass3(ICarrierMessagingCallback val$callback) {
                this.val$callback = val$callback;
            }

            public void onReceiveResult(SendSmsResult result) throws RemoteException {
                this.val$callback.onSendSmsComplete(result.getSendStatus(), result.getMessageRef());
            }
        }

        /* renamed from: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.4 */
        class AnonymousClass4 implements ResultCallback<SendMultipartSmsResult> {
            final /* synthetic */ ICarrierMessagingCallback val$callback;

            AnonymousClass4(ICarrierMessagingCallback val$callback) {
                this.val$callback = val$callback;
            }

            public void onReceiveResult(SendMultipartSmsResult result) throws RemoteException {
                this.val$callback.onSendMultipartSmsComplete(result.getSendStatus(), result.getMessageRefs());
            }
        }

        /* renamed from: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.5 */
        class AnonymousClass5 implements ResultCallback<SendMmsResult> {
            final /* synthetic */ ICarrierMessagingCallback val$callback;

            AnonymousClass5(ICarrierMessagingCallback val$callback) {
                this.val$callback = val$callback;
            }

            public void onReceiveResult(SendMmsResult result) throws RemoteException {
                this.val$callback.onSendMmsComplete(result.getSendStatus(), result.getSendConfPdu());
            }
        }

        /* renamed from: android.service.carrier.CarrierMessagingService.ICarrierMessagingWrapper.6 */
        class AnonymousClass6 implements ResultCallback<Integer> {
            final /* synthetic */ ICarrierMessagingCallback val$callback;

            AnonymousClass6(ICarrierMessagingCallback val$callback) {
                this.val$callback = val$callback;
            }

            public void onReceiveResult(Integer result) throws RemoteException {
                this.val$callback.onDownloadMmsComplete(result.intValue());
            }
        }

        private ICarrierMessagingWrapper() {
        }

        public void filterSms(MessagePdu pdu, String format, int destPort, int subId, ICarrierMessagingCallback callback) {
            CarrierMessagingService.this.onReceiveTextSms(pdu, format, destPort, subId, new AnonymousClass1(callback));
        }

        public void sendTextSms(String text, int subId, String destAddress, int sendSmsFlag, ICarrierMessagingCallback callback) {
            CarrierMessagingService.this.onSendTextSms(text, subId, destAddress, sendSmsFlag, new AnonymousClass2(callback));
        }

        public void sendDataSms(byte[] data, int subId, String destAddress, int destPort, int sendSmsFlag, ICarrierMessagingCallback callback) {
            CarrierMessagingService.this.onSendDataSms(data, subId, destAddress, destPort, sendSmsFlag, new AnonymousClass3(callback));
        }

        public void sendMultipartTextSms(List<String> parts, int subId, String destAddress, int sendSmsFlag, ICarrierMessagingCallback callback) {
            CarrierMessagingService.this.onSendMultipartTextSms(parts, subId, destAddress, sendSmsFlag, new AnonymousClass4(callback));
        }

        public void sendMms(Uri pduUri, int subId, Uri location, ICarrierMessagingCallback callback) {
            CarrierMessagingService.this.onSendMms(pduUri, subId, location, new AnonymousClass5(callback));
        }

        public void downloadMms(Uri pduUri, int subId, Uri location, ICarrierMessagingCallback callback) {
            CarrierMessagingService.this.onDownloadMms(pduUri, subId, location, new AnonymousClass6(callback));
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

    public CarrierMessagingService() {
        this.mWrapper = new ICarrierMessagingWrapper();
    }

    @Deprecated
    public void onFilterSms(MessagePdu pdu, String format, int destPort, int subId, ResultCallback<Boolean> callback) {
        try {
            callback.onReceiveResult(Boolean.valueOf(true));
        } catch (RemoteException e) {
        }
    }

    public void onReceiveTextSms(MessagePdu pdu, String format, int destPort, int subId, ResultCallback<Integer> callback) {
        onFilterSms(pdu, format, destPort, subId, new AnonymousClass1(callback));
    }

    @Deprecated
    public void onSendTextSms(String text, int subId, String destAddress, ResultCallback<SendSmsResult> callback) {
        try {
            callback.onReceiveResult(new SendSmsResult(SEND_STATUS_RETRY_ON_CARRIER_NETWORK, SEND_STATUS_OK));
        } catch (RemoteException e) {
        }
    }

    public void onSendTextSms(String text, int subId, String destAddress, int sendSmsFlag, ResultCallback<SendSmsResult> callback) {
        onSendTextSms(text, subId, destAddress, callback);
    }

    @Deprecated
    public void onSendDataSms(byte[] data, int subId, String destAddress, int destPort, ResultCallback<SendSmsResult> callback) {
        try {
            callback.onReceiveResult(new SendSmsResult(SEND_STATUS_RETRY_ON_CARRIER_NETWORK, SEND_STATUS_OK));
        } catch (RemoteException e) {
        }
    }

    public void onSendDataSms(byte[] data, int subId, String destAddress, int destPort, int sendSmsFlag, ResultCallback<SendSmsResult> callback) {
        onSendDataSms(data, subId, destAddress, destPort, callback);
    }

    @Deprecated
    public void onSendMultipartTextSms(List<String> list, int subId, String destAddress, ResultCallback<SendMultipartSmsResult> callback) {
        try {
            callback.onReceiveResult(new SendMultipartSmsResult(SEND_STATUS_RETRY_ON_CARRIER_NETWORK, null));
        } catch (RemoteException e) {
        }
    }

    public void onSendMultipartTextSms(List<String> parts, int subId, String destAddress, int sendSmsFlag, ResultCallback<SendMultipartSmsResult> callback) {
        onSendMultipartTextSms(parts, subId, destAddress, callback);
    }

    public void onSendMms(Uri pduUri, int subId, Uri location, ResultCallback<SendMmsResult> callback) {
        try {
            callback.onReceiveResult(new SendMmsResult(SEND_STATUS_RETRY_ON_CARRIER_NETWORK, null));
        } catch (RemoteException e) {
        }
    }

    public void onDownloadMms(Uri contentUri, int subId, Uri location, ResultCallback<Integer> callback) {
        try {
            callback.onReceiveResult(Integer.valueOf(SEND_STATUS_RETRY_ON_CARRIER_NETWORK));
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
