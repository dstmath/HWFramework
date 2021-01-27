package com.huawei.lcagent.client;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.lcagent.client.ICaptureLogCallback;
import com.huawei.lcagent.client.IUploadLogCallback;

public interface ILogCollect extends IInterface {
    int allowUploadAlways(boolean z) throws RemoteException;

    int allowUploadInMobileNetwork(boolean z) throws RemoteException;

    int cancelRdebugProcess() throws RemoteException;

    LogMetricInfo captureAllLog() throws RemoteException;

    LogMetricInfo captureLogMetric(int i) throws RemoteException;

    LogMetricInfo captureLogMetricWithModule(int i, String str) throws RemoteException;

    LogMetricInfo captureLogMetricWithParameters(int i, String str) throws RemoteException;

    int captureRemoteDebugLog(ICaptureLogCallback iCaptureLogCallback) throws RemoteException;

    int captureRemoteDebugLogWithRemark(ICaptureLogCallback iCaptureLogCallback, String str, String str2) throws RemoteException;

    void clearLogMetric(long j) throws RemoteException;

    int closeRemoteDebug(int i) throws RemoteException;

    int configure(String str) throws RemoteException;

    int configureAPlogs(int i) throws RemoteException;

    int configureBluetoothlogcat(int i, String str) throws RemoteException;

    int configureCoredump(int i) throws RemoteException;

    int configureGPS(int i) throws RemoteException;

    int configureLogcat(int i, String str) throws RemoteException;

    int configureModemlogcat(int i, String str) throws RemoteException;

    int configureUserType(int i) throws RemoteException;

    void configureWithPara(String str, String str2) throws RemoteException;

    String doEncrypt(String str) throws RemoteException;

    int feedbackUploadResult(long j, int i) throws RemoteException;

    int forceUpload() throws RemoteException;

    CompressInfo getCompressInfo() throws RemoteException;

    long getFirstErrorTime() throws RemoteException;

    String getFirstErrorType() throws RemoteException;

    int getMaxSizeOfLogFile() throws RemoteException;

    int getUploadType(String str) throws RemoteException;

    int getUserType() throws RemoteException;

    long getValueByType(int i) throws RemoteException;

    int postRemoteDebugCmd(String str) throws RemoteException;

    int resetFirstErrorTime() throws RemoteException;

    int setMetricCommonHeader(int i, byte[] bArr, int i2) throws RemoteException;

    int setMetricCommonHeaderWithMcc(int i, byte[] bArr, int i2, String str) throws RemoteException;

    int setMetricStoargeHeader(int i, byte[] bArr, int i2) throws RemoteException;

    int setMetricStoargeHeaderWithMcc(int i, byte[] bArr, int i2, String str) throws RemoteException;

    int setMetricStoargeTail(int i, byte[] bArr, int i2) throws RemoteException;

    int setMetricStoargeTailWithMcc(int i, byte[] bArr, int i2, String str) throws RemoteException;

    boolean shouldSubmitMetric(int i, int i2) throws RemoteException;

    int submitMetric(int i, int i2, byte[] bArr, int i3) throws RemoteException;

    int submitMetricWithMcc(int i, int i2, byte[] bArr, int i3, String str) throws RemoteException;

    String testCheck(int i, String str) throws RemoteException;

    String testInit(int i, String str) throws RemoteException;

    String testPreset(int i, String str) throws RemoteException;

    String testTrigger(int i, String str) throws RemoteException;

    int uploadLogFile(String str, int i, int i2, IUploadLogCallback iUploadLogCallback) throws RemoteException;

    public static class Default implements ILogCollect {
        @Override // com.huawei.lcagent.client.ILogCollect
        public int setMetricStoargeHeader(int metricID, byte[] payloadBytes, int payloadLen) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.lcagent.client.ILogCollect
        public int setMetricCommonHeader(int metricID, byte[] payloadBytes, int payloadLen) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.lcagent.client.ILogCollect
        public int setMetricStoargeTail(int metricID, byte[] payloadBytes, int payloadLen) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.lcagent.client.ILogCollect
        public int submitMetric(int metricID, int level, byte[] payloadBytes, int payloadLen) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.lcagent.client.ILogCollect
        public boolean shouldSubmitMetric(int metricID, int level) throws RemoteException {
            return false;
        }

        @Override // com.huawei.lcagent.client.ILogCollect
        public LogMetricInfo captureLogMetric(int metricID) throws RemoteException {
            return null;
        }

        @Override // com.huawei.lcagent.client.ILogCollect
        public LogMetricInfo captureLogMetricWithParameters(int metricID, String keyValuePairs) throws RemoteException {
            return null;
        }

        @Override // com.huawei.lcagent.client.ILogCollect
        public void clearLogMetric(long id) throws RemoteException {
        }

        @Override // com.huawei.lcagent.client.ILogCollect
        public int feedbackUploadResult(long hashId, int status) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.lcagent.client.ILogCollect
        public int allowUploadInMobileNetwork(boolean allow) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.lcagent.client.ILogCollect
        public int allowUploadAlways(boolean allow) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.lcagent.client.ILogCollect
        public int configureUserType(int type) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.lcagent.client.ILogCollect
        public int getUserType() throws RemoteException {
            return 0;
        }

        @Override // com.huawei.lcagent.client.ILogCollect
        public int forceUpload() throws RemoteException {
            return 0;
        }

        @Override // com.huawei.lcagent.client.ILogCollect
        public int configure(String strCommand) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.lcagent.client.ILogCollect
        public LogMetricInfo captureAllLog() throws RemoteException {
            return null;
        }

        @Override // com.huawei.lcagent.client.ILogCollect
        public long getFirstErrorTime() throws RemoteException {
            return 0;
        }

        @Override // com.huawei.lcagent.client.ILogCollect
        public int resetFirstErrorTime() throws RemoteException {
            return 0;
        }

        @Override // com.huawei.lcagent.client.ILogCollect
        public String getFirstErrorType() throws RemoteException {
            return null;
        }

        @Override // com.huawei.lcagent.client.ILogCollect
        public int configureModemlogcat(int mode, String parameters) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.lcagent.client.ILogCollect
        public int configureBluetoothlogcat(int enable, String parameters) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.lcagent.client.ILogCollect
        public int configureLogcat(int enable, String parameters) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.lcagent.client.ILogCollect
        public int configureAPlogs(int enable) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.lcagent.client.ILogCollect
        public int configureCoredump(int enable) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.lcagent.client.ILogCollect
        public int configureGPS(int enable) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.lcagent.client.ILogCollect
        public void configureWithPara(String cmd, String parameters) throws RemoteException {
        }

        @Override // com.huawei.lcagent.client.ILogCollect
        public CompressInfo getCompressInfo() throws RemoteException {
            return null;
        }

        @Override // com.huawei.lcagent.client.ILogCollect
        public LogMetricInfo captureLogMetricWithModule(int metricID, String module) throws RemoteException {
            return null;
        }

        @Override // com.huawei.lcagent.client.ILogCollect
        public int setMetricStoargeHeaderWithMcc(int metricID, byte[] payloadBytes, int payloadLen, String mcc) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.lcagent.client.ILogCollect
        public int setMetricCommonHeaderWithMcc(int metricID, byte[] payloadBytes, int payloadLen, String mcc) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.lcagent.client.ILogCollect
        public int setMetricStoargeTailWithMcc(int metricID, byte[] payloadBytes, int payloadLen, String mcc) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.lcagent.client.ILogCollect
        public int submitMetricWithMcc(int metricID, int level, byte[] payloadBytes, int payloadLen, String mcc) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.lcagent.client.ILogCollect
        public int getUploadType(String mcc) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.lcagent.client.ILogCollect
        public String doEncrypt(String src) throws RemoteException {
            return null;
        }

        @Override // com.huawei.lcagent.client.ILogCollect
        public int postRemoteDebugCmd(String msg) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.lcagent.client.ILogCollect
        public int closeRemoteDebug(int reason) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.lcagent.client.ILogCollect
        public int captureRemoteDebugLogWithRemark(ICaptureLogCallback callback, String remarkPath, String patchFilespath) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.lcagent.client.ILogCollect
        public int captureRemoteDebugLog(ICaptureLogCallback callback) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.lcagent.client.ILogCollect
        public int getMaxSizeOfLogFile() throws RemoteException {
            return 0;
        }

        @Override // com.huawei.lcagent.client.ILogCollect
        public int uploadLogFile(String filename, int filetype, int uploadtime, IUploadLogCallback callback) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.lcagent.client.ILogCollect
        public int cancelRdebugProcess() throws RemoteException {
            return 0;
        }

        @Override // com.huawei.lcagent.client.ILogCollect
        public long getValueByType(int datatype) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.lcagent.client.ILogCollect
        public String testInit(int testID, String jsonval) throws RemoteException {
            return null;
        }

        @Override // com.huawei.lcagent.client.ILogCollect
        public String testPreset(int testID, String jsonval) throws RemoteException {
            return null;
        }

        @Override // com.huawei.lcagent.client.ILogCollect
        public String testTrigger(int testID, String jsonval) throws RemoteException {
            return null;
        }

        @Override // com.huawei.lcagent.client.ILogCollect
        public String testCheck(int testID, String jsonval) throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ILogCollect {
        private static final String DESCRIPTOR = "com.huawei.lcagent.client.ILogCollect";
        static final int TRANSACTION_allowUploadAlways = 11;
        static final int TRANSACTION_allowUploadInMobileNetwork = 10;
        static final int TRANSACTION_cancelRdebugProcess = 41;
        static final int TRANSACTION_captureAllLog = 16;
        static final int TRANSACTION_captureLogMetric = 6;
        static final int TRANSACTION_captureLogMetricWithModule = 28;
        static final int TRANSACTION_captureLogMetricWithParameters = 7;
        static final int TRANSACTION_captureRemoteDebugLog = 38;
        static final int TRANSACTION_captureRemoteDebugLogWithRemark = 37;
        static final int TRANSACTION_clearLogMetric = 8;
        static final int TRANSACTION_closeRemoteDebug = 36;
        static final int TRANSACTION_configure = 15;
        static final int TRANSACTION_configureAPlogs = 23;
        static final int TRANSACTION_configureBluetoothlogcat = 21;
        static final int TRANSACTION_configureCoredump = 24;
        static final int TRANSACTION_configureGPS = 25;
        static final int TRANSACTION_configureLogcat = 22;
        static final int TRANSACTION_configureModemlogcat = 20;
        static final int TRANSACTION_configureUserType = 12;
        static final int TRANSACTION_configureWithPara = 26;
        static final int TRANSACTION_doEncrypt = 34;
        static final int TRANSACTION_feedbackUploadResult = 9;
        static final int TRANSACTION_forceUpload = 14;
        static final int TRANSACTION_getCompressInfo = 27;
        static final int TRANSACTION_getFirstErrorTime = 17;
        static final int TRANSACTION_getFirstErrorType = 19;
        static final int TRANSACTION_getMaxSizeOfLogFile = 39;
        static final int TRANSACTION_getUploadType = 33;
        static final int TRANSACTION_getUserType = 13;
        static final int TRANSACTION_getValueByType = 42;
        static final int TRANSACTION_postRemoteDebugCmd = 35;
        static final int TRANSACTION_resetFirstErrorTime = 18;
        static final int TRANSACTION_setMetricCommonHeader = 2;
        static final int TRANSACTION_setMetricCommonHeaderWithMcc = 30;
        static final int TRANSACTION_setMetricStoargeHeader = 1;
        static final int TRANSACTION_setMetricStoargeHeaderWithMcc = 29;
        static final int TRANSACTION_setMetricStoargeTail = 3;
        static final int TRANSACTION_setMetricStoargeTailWithMcc = 31;
        static final int TRANSACTION_shouldSubmitMetric = 5;
        static final int TRANSACTION_submitMetric = 4;
        static final int TRANSACTION_submitMetricWithMcc = 32;
        static final int TRANSACTION_testCheck = 46;
        static final int TRANSACTION_testInit = 43;
        static final int TRANSACTION_testPreset = 44;
        static final int TRANSACTION_testTrigger = 45;
        static final int TRANSACTION_uploadLogFile = 40;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ILogCollect asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ILogCollect)) {
                return new Proxy(obj);
            }
            return (ILogCollect) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                boolean _arg0 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        int _result = setMetricStoargeHeader(data.readInt(), data.createByteArray(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        int _result2 = setMetricCommonHeader(data.readInt(), data.createByteArray(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        int _result3 = setMetricStoargeTail(data.readInt(), data.createByteArray(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        int _result4 = submitMetric(data.readInt(), data.readInt(), data.createByteArray(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        boolean shouldSubmitMetric = shouldSubmitMetric(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(shouldSubmitMetric ? 1 : 0);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        LogMetricInfo _result5 = captureLogMetric(data.readInt());
                        reply.writeNoException();
                        if (_result5 != null) {
                            reply.writeInt(1);
                            _result5.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        LogMetricInfo _result6 = captureLogMetricWithParameters(data.readInt(), data.readString());
                        reply.writeNoException();
                        if (_result6 != null) {
                            reply.writeInt(1);
                            _result6.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        clearLogMetric(data.readLong());
                        reply.writeNoException();
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        int _result7 = feedbackUploadResult(data.readLong(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result7);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = true;
                        }
                        int _result8 = allowUploadInMobileNetwork(_arg0);
                        reply.writeNoException();
                        reply.writeInt(_result8);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = true;
                        }
                        int _result9 = allowUploadAlways(_arg0);
                        reply.writeNoException();
                        reply.writeInt(_result9);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        int _result10 = configureUserType(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result10);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        int _result11 = getUserType();
                        reply.writeNoException();
                        reply.writeInt(_result11);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        int _result12 = forceUpload();
                        reply.writeNoException();
                        reply.writeInt(_result12);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        int _result13 = configure(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result13);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        LogMetricInfo _result14 = captureAllLog();
                        reply.writeNoException();
                        if (_result14 != null) {
                            reply.writeInt(1);
                            _result14.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        long _result15 = getFirstErrorTime();
                        reply.writeNoException();
                        reply.writeLong(_result15);
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        int _result16 = resetFirstErrorTime();
                        reply.writeNoException();
                        reply.writeInt(_result16);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        String _result17 = getFirstErrorType();
                        reply.writeNoException();
                        reply.writeString(_result17);
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        int _result18 = configureModemlogcat(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result18);
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        int _result19 = configureBluetoothlogcat(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result19);
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        int _result20 = configureLogcat(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result20);
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        int _result21 = configureAPlogs(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result21);
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        int _result22 = configureCoredump(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result22);
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        int _result23 = configureGPS(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result23);
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        configureWithPara(data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 27:
                        data.enforceInterface(DESCRIPTOR);
                        CompressInfo _result24 = getCompressInfo();
                        reply.writeNoException();
                        if (_result24 != null) {
                            reply.writeInt(1);
                            _result24.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 28:
                        data.enforceInterface(DESCRIPTOR);
                        LogMetricInfo _result25 = captureLogMetricWithModule(data.readInt(), data.readString());
                        reply.writeNoException();
                        if (_result25 != null) {
                            reply.writeInt(1);
                            _result25.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 29:
                        data.enforceInterface(DESCRIPTOR);
                        int _result26 = setMetricStoargeHeaderWithMcc(data.readInt(), data.createByteArray(), data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result26);
                        return true;
                    case 30:
                        data.enforceInterface(DESCRIPTOR);
                        int _result27 = setMetricCommonHeaderWithMcc(data.readInt(), data.createByteArray(), data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result27);
                        return true;
                    case 31:
                        data.enforceInterface(DESCRIPTOR);
                        int _result28 = setMetricStoargeTailWithMcc(data.readInt(), data.createByteArray(), data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result28);
                        return true;
                    case 32:
                        data.enforceInterface(DESCRIPTOR);
                        int _result29 = submitMetricWithMcc(data.readInt(), data.readInt(), data.createByteArray(), data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result29);
                        return true;
                    case 33:
                        data.enforceInterface(DESCRIPTOR);
                        int _result30 = getUploadType(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result30);
                        return true;
                    case 34:
                        data.enforceInterface(DESCRIPTOR);
                        String _result31 = doEncrypt(data.readString());
                        reply.writeNoException();
                        reply.writeString(_result31);
                        return true;
                    case 35:
                        data.enforceInterface(DESCRIPTOR);
                        int _result32 = postRemoteDebugCmd(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result32);
                        return true;
                    case 36:
                        data.enforceInterface(DESCRIPTOR);
                        int _result33 = closeRemoteDebug(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result33);
                        return true;
                    case 37:
                        data.enforceInterface(DESCRIPTOR);
                        int _result34 = captureRemoteDebugLogWithRemark(ICaptureLogCallback.Stub.asInterface(data.readStrongBinder()), data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result34);
                        return true;
                    case 38:
                        data.enforceInterface(DESCRIPTOR);
                        int _result35 = captureRemoteDebugLog(ICaptureLogCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result35);
                        return true;
                    case 39:
                        data.enforceInterface(DESCRIPTOR);
                        int _result36 = getMaxSizeOfLogFile();
                        reply.writeNoException();
                        reply.writeInt(_result36);
                        return true;
                    case 40:
                        data.enforceInterface(DESCRIPTOR);
                        int _result37 = uploadLogFile(data.readString(), data.readInt(), data.readInt(), IUploadLogCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result37);
                        return true;
                    case 41:
                        data.enforceInterface(DESCRIPTOR);
                        int _result38 = cancelRdebugProcess();
                        reply.writeNoException();
                        reply.writeInt(_result38);
                        return true;
                    case 42:
                        data.enforceInterface(DESCRIPTOR);
                        long _result39 = getValueByType(data.readInt());
                        reply.writeNoException();
                        reply.writeLong(_result39);
                        return true;
                    case 43:
                        data.enforceInterface(DESCRIPTOR);
                        String _result40 = testInit(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeString(_result40);
                        return true;
                    case TRANSACTION_testPreset /* 44 */:
                        data.enforceInterface(DESCRIPTOR);
                        String _result41 = testPreset(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeString(_result41);
                        return true;
                    case TRANSACTION_testTrigger /* 45 */:
                        data.enforceInterface(DESCRIPTOR);
                        String _result42 = testTrigger(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeString(_result42);
                        return true;
                    case 46:
                        data.enforceInterface(DESCRIPTOR);
                        String _result43 = testCheck(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeString(_result43);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements ILogCollect {
            public static ILogCollect sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            @Override // com.huawei.lcagent.client.ILogCollect
            public int setMetricStoargeHeader(int metricID, byte[] payloadBytes, int payloadLen) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(metricID);
                    _data.writeByteArray(payloadBytes);
                    _data.writeInt(payloadLen);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setMetricStoargeHeader(metricID, payloadBytes, payloadLen);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.lcagent.client.ILogCollect
            public int setMetricCommonHeader(int metricID, byte[] payloadBytes, int payloadLen) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(metricID);
                    _data.writeByteArray(payloadBytes);
                    _data.writeInt(payloadLen);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setMetricCommonHeader(metricID, payloadBytes, payloadLen);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.lcagent.client.ILogCollect
            public int setMetricStoargeTail(int metricID, byte[] payloadBytes, int payloadLen) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(metricID);
                    _data.writeByteArray(payloadBytes);
                    _data.writeInt(payloadLen);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setMetricStoargeTail(metricID, payloadBytes, payloadLen);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.lcagent.client.ILogCollect
            public int submitMetric(int metricID, int level, byte[] payloadBytes, int payloadLen) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(metricID);
                    _data.writeInt(level);
                    _data.writeByteArray(payloadBytes);
                    _data.writeInt(payloadLen);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().submitMetric(metricID, level, payloadBytes, payloadLen);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.lcagent.client.ILogCollect
            public boolean shouldSubmitMetric(int metricID, int level) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(metricID);
                    _data.writeInt(level);
                    boolean _result = false;
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().shouldSubmitMetric(metricID, level);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.lcagent.client.ILogCollect
            public LogMetricInfo captureLogMetric(int metricID) throws RemoteException {
                LogMetricInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(metricID);
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().captureLogMetric(metricID);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = LogMetricInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.lcagent.client.ILogCollect
            public LogMetricInfo captureLogMetricWithParameters(int metricID, String keyValuePairs) throws RemoteException {
                LogMetricInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(metricID);
                    _data.writeString(keyValuePairs);
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().captureLogMetricWithParameters(metricID, keyValuePairs);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = LogMetricInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.lcagent.client.ILogCollect
            public void clearLogMetric(long id) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(id);
                    if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().clearLogMetric(id);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.lcagent.client.ILogCollect
            public int feedbackUploadResult(long hashId, int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(hashId);
                    _data.writeInt(status);
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().feedbackUploadResult(hashId, status);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.lcagent.client.ILogCollect
            public int allowUploadInMobileNetwork(boolean allow) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(allow ? 1 : 0);
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().allowUploadInMobileNetwork(allow);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.lcagent.client.ILogCollect
            public int allowUploadAlways(boolean allow) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(allow ? 1 : 0);
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().allowUploadAlways(allow);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.lcagent.client.ILogCollect
            public int configureUserType(int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().configureUserType(type);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.lcagent.client.ILogCollect
            public int getUserType() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getUserType();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.lcagent.client.ILogCollect
            public int forceUpload() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(14, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().forceUpload();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.lcagent.client.ILogCollect
            public int configure(String strCommand) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(strCommand);
                    if (!this.mRemote.transact(15, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().configure(strCommand);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.lcagent.client.ILogCollect
            public LogMetricInfo captureAllLog() throws RemoteException {
                LogMetricInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(16, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().captureAllLog();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = LogMetricInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.lcagent.client.ILogCollect
            public long getFirstErrorTime() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(17, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getFirstErrorTime();
                    }
                    _reply.readException();
                    long _result = _reply.readLong();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.lcagent.client.ILogCollect
            public int resetFirstErrorTime() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(18, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().resetFirstErrorTime();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.lcagent.client.ILogCollect
            public String getFirstErrorType() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(19, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getFirstErrorType();
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.lcagent.client.ILogCollect
            public int configureModemlogcat(int mode, String parameters) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(mode);
                    _data.writeString(parameters);
                    if (!this.mRemote.transact(20, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().configureModemlogcat(mode, parameters);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.lcagent.client.ILogCollect
            public int configureBluetoothlogcat(int enable, String parameters) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable);
                    _data.writeString(parameters);
                    if (!this.mRemote.transact(21, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().configureBluetoothlogcat(enable, parameters);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.lcagent.client.ILogCollect
            public int configureLogcat(int enable, String parameters) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable);
                    _data.writeString(parameters);
                    if (!this.mRemote.transact(22, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().configureLogcat(enable, parameters);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.lcagent.client.ILogCollect
            public int configureAPlogs(int enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable);
                    if (!this.mRemote.transact(23, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().configureAPlogs(enable);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.lcagent.client.ILogCollect
            public int configureCoredump(int enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable);
                    if (!this.mRemote.transact(24, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().configureCoredump(enable);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.lcagent.client.ILogCollect
            public int configureGPS(int enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable);
                    if (!this.mRemote.transact(25, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().configureGPS(enable);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.lcagent.client.ILogCollect
            public void configureWithPara(String cmd, String parameters) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(cmd);
                    _data.writeString(parameters);
                    if (this.mRemote.transact(26, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().configureWithPara(cmd, parameters);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.lcagent.client.ILogCollect
            public CompressInfo getCompressInfo() throws RemoteException {
                CompressInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(27, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCompressInfo();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = CompressInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.lcagent.client.ILogCollect
            public LogMetricInfo captureLogMetricWithModule(int metricID, String module) throws RemoteException {
                LogMetricInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(metricID);
                    _data.writeString(module);
                    if (!this.mRemote.transact(28, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().captureLogMetricWithModule(metricID, module);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = LogMetricInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.lcagent.client.ILogCollect
            public int setMetricStoargeHeaderWithMcc(int metricID, byte[] payloadBytes, int payloadLen, String mcc) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(metricID);
                    _data.writeByteArray(payloadBytes);
                    _data.writeInt(payloadLen);
                    _data.writeString(mcc);
                    if (!this.mRemote.transact(29, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setMetricStoargeHeaderWithMcc(metricID, payloadBytes, payloadLen, mcc);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.lcagent.client.ILogCollect
            public int setMetricCommonHeaderWithMcc(int metricID, byte[] payloadBytes, int payloadLen, String mcc) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(metricID);
                    _data.writeByteArray(payloadBytes);
                    _data.writeInt(payloadLen);
                    _data.writeString(mcc);
                    if (!this.mRemote.transact(30, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setMetricCommonHeaderWithMcc(metricID, payloadBytes, payloadLen, mcc);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.lcagent.client.ILogCollect
            public int setMetricStoargeTailWithMcc(int metricID, byte[] payloadBytes, int payloadLen, String mcc) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(metricID);
                    _data.writeByteArray(payloadBytes);
                    _data.writeInt(payloadLen);
                    _data.writeString(mcc);
                    if (!this.mRemote.transact(31, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setMetricStoargeTailWithMcc(metricID, payloadBytes, payloadLen, mcc);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.lcagent.client.ILogCollect
            public int submitMetricWithMcc(int metricID, int level, byte[] payloadBytes, int payloadLen, String mcc) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(metricID);
                    _data.writeInt(level);
                    _data.writeByteArray(payloadBytes);
                    _data.writeInt(payloadLen);
                    _data.writeString(mcc);
                    if (!this.mRemote.transact(32, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().submitMetricWithMcc(metricID, level, payloadBytes, payloadLen, mcc);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.lcagent.client.ILogCollect
            public int getUploadType(String mcc) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(mcc);
                    if (!this.mRemote.transact(33, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getUploadType(mcc);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.lcagent.client.ILogCollect
            public String doEncrypt(String src) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(src);
                    if (!this.mRemote.transact(34, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().doEncrypt(src);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.lcagent.client.ILogCollect
            public int postRemoteDebugCmd(String msg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(msg);
                    if (!this.mRemote.transact(35, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().postRemoteDebugCmd(msg);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.lcagent.client.ILogCollect
            public int closeRemoteDebug(int reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(reason);
                    if (!this.mRemote.transact(36, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().closeRemoteDebug(reason);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.lcagent.client.ILogCollect
            public int captureRemoteDebugLogWithRemark(ICaptureLogCallback callback, String remarkPath, String patchFilespath) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeString(remarkPath);
                    _data.writeString(patchFilespath);
                    if (!this.mRemote.transact(37, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().captureRemoteDebugLogWithRemark(callback, remarkPath, patchFilespath);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.lcagent.client.ILogCollect
            public int captureRemoteDebugLog(ICaptureLogCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (!this.mRemote.transact(38, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().captureRemoteDebugLog(callback);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.lcagent.client.ILogCollect
            public int getMaxSizeOfLogFile() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(39, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMaxSizeOfLogFile();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.lcagent.client.ILogCollect
            public int uploadLogFile(String filename, int filetype, int uploadtime, IUploadLogCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(filename);
                    _data.writeInt(filetype);
                    _data.writeInt(uploadtime);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (!this.mRemote.transact(40, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().uploadLogFile(filename, filetype, uploadtime, callback);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.lcagent.client.ILogCollect
            public int cancelRdebugProcess() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(41, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().cancelRdebugProcess();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.lcagent.client.ILogCollect
            public long getValueByType(int datatype) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(datatype);
                    if (!this.mRemote.transact(42, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getValueByType(datatype);
                    }
                    _reply.readException();
                    long _result = _reply.readLong();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.lcagent.client.ILogCollect
            public String testInit(int testID, String jsonval) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(testID);
                    _data.writeString(jsonval);
                    if (!this.mRemote.transact(43, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().testInit(testID, jsonval);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.lcagent.client.ILogCollect
            public String testPreset(int testID, String jsonval) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(testID);
                    _data.writeString(jsonval);
                    if (!this.mRemote.transact(Stub.TRANSACTION_testPreset, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().testPreset(testID, jsonval);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.lcagent.client.ILogCollect
            public String testTrigger(int testID, String jsonval) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(testID);
                    _data.writeString(jsonval);
                    if (!this.mRemote.transact(Stub.TRANSACTION_testTrigger, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().testTrigger(testID, jsonval);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.lcagent.client.ILogCollect
            public String testCheck(int testID, String jsonval) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(testID);
                    _data.writeString(jsonval);
                    if (!this.mRemote.transact(46, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().testCheck(testID, jsonval);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ILogCollect impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ILogCollect getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
