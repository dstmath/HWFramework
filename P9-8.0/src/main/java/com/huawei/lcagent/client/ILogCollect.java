package com.huawei.lcagent.client;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ILogCollect extends IInterface {

    public static abstract class Stub extends Binder implements ILogCollect {
        private static final String DESCRIPTOR = "com.huawei.lcagent.client.ILogCollect";
        static final int TRANSACTION_allowUploadAlways = 11;
        static final int TRANSACTION_allowUploadInMobileNetwork = 10;
        static final int TRANSACTION_cancelRdebugProcess = 40;
        static final int TRANSACTION_captureAllLog = 16;
        static final int TRANSACTION_captureLogMetric = 6;
        static final int TRANSACTION_captureLogMetricWithModule = 28;
        static final int TRANSACTION_captureLogMetricWithParameters = 7;
        static final int TRANSACTION_captureRemoteDebugLog = 37;
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
        static final int TRANSACTION_getMaxSizeOfLogFile = 38;
        static final int TRANSACTION_getUploadType = 33;
        static final int TRANSACTION_getUserType = 13;
        static final int TRANSACTION_getValueByType = 41;
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
        static final int TRANSACTION_uploadLogFile = 39;

        private static class Proxy implements ILogCollect {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            public int setMetricStoargeHeader(int metricID, byte[] payloadBytes, int payloadLen) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(metricID);
                    _data.writeByteArray(payloadBytes);
                    _data.writeInt(payloadLen);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setMetricCommonHeader(int metricID, byte[] payloadBytes, int payloadLen) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(metricID);
                    _data.writeByteArray(payloadBytes);
                    _data.writeInt(payloadLen);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setMetricStoargeTail(int metricID, byte[] payloadBytes, int payloadLen) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(metricID);
                    _data.writeByteArray(payloadBytes);
                    _data.writeInt(payloadLen);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int submitMetric(int metricID, int level, byte[] payloadBytes, int payloadLen) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(metricID);
                    _data.writeInt(level);
                    _data.writeByteArray(payloadBytes);
                    _data.writeInt(payloadLen);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean shouldSubmitMetric(int metricID, int level) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(metricID);
                    _data.writeInt(level);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public LogMetricInfo captureLogMetric(int metricID) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    LogMetricInfo _result;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(metricID);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (LogMetricInfo) LogMetricInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public LogMetricInfo captureLogMetricWithParameters(int metricID, String keyValuePairs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    LogMetricInfo _result;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(metricID);
                    _data.writeString(keyValuePairs);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (LogMetricInfo) LogMetricInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void clearLogMetric(long id) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(id);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int feedbackUploadResult(long hashId, int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(hashId);
                    _data.writeInt(status);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int allowUploadInMobileNetwork(boolean allow) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (allow) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int allowUploadAlways(boolean allow) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (allow) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int configureUserType(int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getUserType() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int forceUpload() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int configure(String strCommand) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(strCommand);
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public LogMetricInfo captureAllLog() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    LogMetricInfo _result;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (LogMetricInfo) LogMetricInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long getFirstErrorTime() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int resetFirstErrorTime() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getFirstErrorType() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int configureModemlogcat(int mode, String parameters) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(mode);
                    _data.writeString(parameters);
                    this.mRemote.transact(20, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int configureBluetoothlogcat(int enable, String parameters) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable);
                    _data.writeString(parameters);
                    this.mRemote.transact(21, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int configureLogcat(int enable, String parameters) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable);
                    _data.writeString(parameters);
                    this.mRemote.transact(22, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int configureAPlogs(int enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable);
                    this.mRemote.transact(23, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int configureCoredump(int enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable);
                    this.mRemote.transact(Stub.TRANSACTION_configureCoredump, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int configureGPS(int enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable);
                    this.mRemote.transact(25, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void configureWithPara(String cmd, String parameters) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(cmd);
                    _data.writeString(parameters);
                    this.mRemote.transact(Stub.TRANSACTION_configureWithPara, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public CompressInfo getCompressInfo() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    CompressInfo _result;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(27, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (CompressInfo) CompressInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public LogMetricInfo captureLogMetricWithModule(int metricID, String module) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    LogMetricInfo _result;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(metricID);
                    _data.writeString(module);
                    this.mRemote.transact(28, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (LogMetricInfo) LogMetricInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setMetricStoargeHeaderWithMcc(int metricID, byte[] payloadBytes, int payloadLen, String mcc) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(metricID);
                    _data.writeByteArray(payloadBytes);
                    _data.writeInt(payloadLen);
                    _data.writeString(mcc);
                    this.mRemote.transact(29, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setMetricCommonHeaderWithMcc(int metricID, byte[] payloadBytes, int payloadLen, String mcc) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(metricID);
                    _data.writeByteArray(payloadBytes);
                    _data.writeInt(payloadLen);
                    _data.writeString(mcc);
                    this.mRemote.transact(30, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setMetricStoargeTailWithMcc(int metricID, byte[] payloadBytes, int payloadLen, String mcc) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(metricID);
                    _data.writeByteArray(payloadBytes);
                    _data.writeInt(payloadLen);
                    _data.writeString(mcc);
                    this.mRemote.transact(31, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(32, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getUploadType(String mcc) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(mcc);
                    this.mRemote.transact(33, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String doEncrypt(String src) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(src);
                    this.mRemote.transact(Stub.TRANSACTION_doEncrypt, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int postRemoteDebugCmd(String msg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(msg);
                    this.mRemote.transact(Stub.TRANSACTION_postRemoteDebugCmd, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int closeRemoteDebug(int reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(reason);
                    this.mRemote.transact(36, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int captureRemoteDebugLog(ICaptureLogCallback callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_captureRemoteDebugLog, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getMaxSizeOfLogFile() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getMaxSizeOfLogFile, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int uploadLogFile(String filename, int filetype, int uploadtime, IUploadLogCallback callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(filename);
                    _data.writeInt(filetype);
                    _data.writeInt(uploadtime);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_uploadLogFile, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int cancelRdebugProcess() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(40, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long getValueByType(int datatype) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(datatype);
                    this.mRemote.transact(41, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

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

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int _result;
            LogMetricInfo _result2;
            long _result3;
            String _result4;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    _result = setMetricStoargeHeader(data.readInt(), data.createByteArray(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    _result = setMetricCommonHeader(data.readInt(), data.createByteArray(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    _result = setMetricStoargeTail(data.readInt(), data.createByteArray(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    _result = submitMetric(data.readInt(), data.readInt(), data.createByteArray(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    boolean _result5 = shouldSubmitMetric(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result5 ? 1 : 0);
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = captureLogMetric(data.readInt());
                    reply.writeNoException();
                    if (_result2 != null) {
                        reply.writeInt(1);
                        _result2.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = captureLogMetricWithParameters(data.readInt(), data.readString());
                    reply.writeNoException();
                    if (_result2 != null) {
                        reply.writeInt(1);
                        _result2.writeToParcel(reply, 1);
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
                    _result = feedbackUploadResult(data.readLong(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 10:
                    data.enforceInterface(DESCRIPTOR);
                    _result = allowUploadInMobileNetwork(data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 11:
                    data.enforceInterface(DESCRIPTOR);
                    _result = allowUploadAlways(data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 12:
                    data.enforceInterface(DESCRIPTOR);
                    _result = configureUserType(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 13:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getUserType();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 14:
                    data.enforceInterface(DESCRIPTOR);
                    _result = forceUpload();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 15:
                    data.enforceInterface(DESCRIPTOR);
                    _result = configure(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 16:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = captureAllLog();
                    reply.writeNoException();
                    if (_result2 != null) {
                        reply.writeInt(1);
                        _result2.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 17:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getFirstErrorTime();
                    reply.writeNoException();
                    reply.writeLong(_result3);
                    return true;
                case 18:
                    data.enforceInterface(DESCRIPTOR);
                    _result = resetFirstErrorTime();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 19:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getFirstErrorType();
                    reply.writeNoException();
                    reply.writeString(_result4);
                    return true;
                case 20:
                    data.enforceInterface(DESCRIPTOR);
                    _result = configureModemlogcat(data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 21:
                    data.enforceInterface(DESCRIPTOR);
                    _result = configureBluetoothlogcat(data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 22:
                    data.enforceInterface(DESCRIPTOR);
                    _result = configureLogcat(data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 23:
                    data.enforceInterface(DESCRIPTOR);
                    _result = configureAPlogs(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_configureCoredump /*24*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = configureCoredump(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 25:
                    data.enforceInterface(DESCRIPTOR);
                    _result = configureGPS(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_configureWithPara /*26*/:
                    data.enforceInterface(DESCRIPTOR);
                    configureWithPara(data.readString(), data.readString());
                    reply.writeNoException();
                    return true;
                case 27:
                    data.enforceInterface(DESCRIPTOR);
                    CompressInfo _result6 = getCompressInfo();
                    reply.writeNoException();
                    if (_result6 != null) {
                        reply.writeInt(1);
                        _result6.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 28:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = captureLogMetricWithModule(data.readInt(), data.readString());
                    reply.writeNoException();
                    if (_result2 != null) {
                        reply.writeInt(1);
                        _result2.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 29:
                    data.enforceInterface(DESCRIPTOR);
                    _result = setMetricStoargeHeaderWithMcc(data.readInt(), data.createByteArray(), data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 30:
                    data.enforceInterface(DESCRIPTOR);
                    _result = setMetricCommonHeaderWithMcc(data.readInt(), data.createByteArray(), data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 31:
                    data.enforceInterface(DESCRIPTOR);
                    _result = setMetricStoargeTailWithMcc(data.readInt(), data.createByteArray(), data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 32:
                    data.enforceInterface(DESCRIPTOR);
                    _result = submitMetricWithMcc(data.readInt(), data.readInt(), data.createByteArray(), data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 33:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getUploadType(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_doEncrypt /*34*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = doEncrypt(data.readString());
                    reply.writeNoException();
                    reply.writeString(_result4);
                    return true;
                case TRANSACTION_postRemoteDebugCmd /*35*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = postRemoteDebugCmd(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 36:
                    data.enforceInterface(DESCRIPTOR);
                    _result = closeRemoteDebug(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_captureRemoteDebugLog /*37*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = captureRemoteDebugLog(com.huawei.lcagent.client.ICaptureLogCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_getMaxSizeOfLogFile /*38*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getMaxSizeOfLogFile();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_uploadLogFile /*39*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = uploadLogFile(data.readString(), data.readInt(), data.readInt(), com.huawei.lcagent.client.IUploadLogCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 40:
                    data.enforceInterface(DESCRIPTOR);
                    _result = cancelRdebugProcess();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 41:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getValueByType(data.readInt());
                    reply.writeNoException();
                    reply.writeLong(_result3);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    int allowUploadAlways(boolean z) throws RemoteException;

    int allowUploadInMobileNetwork(boolean z) throws RemoteException;

    int cancelRdebugProcess() throws RemoteException;

    LogMetricInfo captureAllLog() throws RemoteException;

    LogMetricInfo captureLogMetric(int i) throws RemoteException;

    LogMetricInfo captureLogMetricWithModule(int i, String str) throws RemoteException;

    LogMetricInfo captureLogMetricWithParameters(int i, String str) throws RemoteException;

    int captureRemoteDebugLog(ICaptureLogCallback iCaptureLogCallback) throws RemoteException;

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

    int uploadLogFile(String str, int i, int i2, IUploadLogCallback iUploadLogCallback) throws RemoteException;
}
