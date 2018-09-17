package android.rms.iaware;

import android.content.pm.ParceledListSlice;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.ArrayList;
import java.util.List;

public interface ICMSManager extends IInterface {

    public static abstract class Stub extends Binder implements ICMSManager {
        private static final String DESCRIPTOR = "android.rms.iaware.ICMSManager";
        static final int TRANSACTION_configUpdate = 1;
        static final int TRANSACTION_custConfigUpdate = 18;
        static final int TRANSACTION_deleteAppCmpTypeInfo = 17;
        static final int TRANSACTION_deleteCmpTypeInfo = 15;
        static final int TRANSACTION_disableFeature = 3;
        static final int TRANSACTION_enableFeature = 2;
        static final int TRANSACTION_getAllAppTypeInfo = 11;
        static final int TRANSACTION_getAppPreloadList = 12;
        static final int TRANSACTION_getAppType = 25;
        static final int TRANSACTION_getAppTypeInfo = 10;
        static final int TRANSACTION_getCmpTypeList = 16;
        static final int TRANSACTION_getConfig = 6;
        static final int TRANSACTION_getCustConfig = 13;
        static final int TRANSACTION_getDumpData = 7;
        static final int TRANSACTION_getInstalledGameList = 19;
        static final int TRANSACTION_getStatisticsData = 8;
        static final int TRANSACTION_getZipFiles = 9;
        static final int TRANSACTION_insertCmpTypeInfo = 14;
        static final int TRANSACTION_isAppRecognizedGame = 20;
        static final int TRANSACTION_isFeatureEnabled = 4;
        static final int TRANSACTION_isIAwareEnabled = 5;
        static final int TRANSACTION_isWinFrameOptEnable = 24;
        static final int TRANSACTION_registerTargetFpsObserver = 22;
        static final int TRANSACTION_unregisterTargetFpsObserver = 23;
        static final int TRANSACTION_updateSOCLevel = 21;

        private static class Proxy implements ICMSManager {
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

            public void configUpdate() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void enableFeature(int featureId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(featureId);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void disableFeature(int featureId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(featureId);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isFeatureEnabled(int featureId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(featureId);
                    this.mRemote.transact(4, _data, _reply, 0);
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

            public boolean isIAwareEnabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
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

            public AwareConfig getConfig(String featureName, String configName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    AwareConfig _result;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(featureName);
                    _data.writeString(configName);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (AwareConfig) AwareConfig.CREATOR.createFromParcel(_reply);
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

            public int getDumpData(int time, List<DumpData> dumpData) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(time);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.readTypedList(dumpData, DumpData.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getStatisticsData(List<StatisticsData> statisticsData) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.readTypedList(statisticsData, StatisticsData.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getZipFiles() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public AppTypeInfo getAppTypeInfo(String pkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    AppTypeInfo _result;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (AppTypeInfo) AppTypeInfo.CREATOR.createFromParcel(_reply);
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

            public ParceledListSlice getAllAppTypeInfo() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ParceledListSlice _result;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
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

            public List<AppPreloadInfo> getAppPreloadList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                    List<AppPreloadInfo> _result = _reply.createTypedArrayList(AppPreloadInfo.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public AwareConfig getCustConfig(String featureName, String configName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    AwareConfig _result;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(featureName);
                    _data.writeString(configName);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (AwareConfig) AwareConfig.CREATOR.createFromParcel(_reply);
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

            public boolean insertCmpTypeInfo(CmpTypeInfo info) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (info != null) {
                        _data.writeInt(1);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(14, _data, _reply, 0);
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

            public boolean deleteCmpTypeInfo(CmpTypeInfo info) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (info != null) {
                        _data.writeInt(1);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(15, _data, _reply, 0);
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

            public List<CmpTypeInfo> getCmpTypeList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                    List<CmpTypeInfo> _result = _reply.createTypedArrayList(CmpTypeInfo.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean deleteAppCmpTypeInfo(String pkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    this.mRemote.transact(17, _data, _reply, 0);
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

            public void custConfigUpdate() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getInstalledGameList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isAppRecognizedGame(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(20, _data, _reply, 0);
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

            public void updateSOCLevel(int level) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(level);
                    this.mRemote.transact(21, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registerTargetFpsObserver(IReportFpsCallback callback, long callbackId) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeLong(callbackId);
                    this.mRemote.transact(22, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregisterTargetFpsObserver(long callbackId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(callbackId);
                    this.mRemote.transact(23, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int isWinFrameOptEnable(String data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(data);
                    this.mRemote.transact(24, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getAppType(String data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(data);
                    this.mRemote.transact(25, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
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

        public static ICMSManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ICMSManager)) {
                return new Proxy(obj);
            }
            return (ICMSManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            boolean _result;
            AwareConfig _result2;
            int _result3;
            List<String> _result4;
            CmpTypeInfo _arg0;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    configUpdate();
                    reply.writeNoException();
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    enableFeature(data.readInt());
                    reply.writeNoException();
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    disableFeature(data.readInt());
                    reply.writeNoException();
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isFeatureEnabled(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isIAwareEnabled();
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getConfig(data.readString(), data.readString());
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
                    int _arg02 = data.readInt();
                    List<DumpData> _arg1 = new ArrayList();
                    _result3 = getDumpData(_arg02, _arg1);
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    reply.writeTypedList(_arg1);
                    return true;
                case 8:
                    data.enforceInterface(DESCRIPTOR);
                    List<StatisticsData> _arg03 = new ArrayList();
                    _result3 = getStatisticsData(_arg03);
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    reply.writeTypedList(_arg03);
                    return true;
                case 9:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getZipFiles();
                    reply.writeNoException();
                    reply.writeStringList(_result4);
                    return true;
                case 10:
                    data.enforceInterface(DESCRIPTOR);
                    AppTypeInfo _result5 = getAppTypeInfo(data.readString());
                    reply.writeNoException();
                    if (_result5 != null) {
                        reply.writeInt(1);
                        _result5.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 11:
                    data.enforceInterface(DESCRIPTOR);
                    ParceledListSlice _result6 = getAllAppTypeInfo();
                    reply.writeNoException();
                    if (_result6 != null) {
                        reply.writeInt(1);
                        _result6.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 12:
                    data.enforceInterface(DESCRIPTOR);
                    List<AppPreloadInfo> _result7 = getAppPreloadList();
                    reply.writeNoException();
                    reply.writeTypedList(_result7);
                    return true;
                case 13:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getCustConfig(data.readString(), data.readString());
                    reply.writeNoException();
                    if (_result2 != null) {
                        reply.writeInt(1);
                        _result2.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 14:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = (CmpTypeInfo) CmpTypeInfo.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    _result = insertCmpTypeInfo(_arg0);
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 15:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = (CmpTypeInfo) CmpTypeInfo.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    _result = deleteCmpTypeInfo(_arg0);
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 16:
                    data.enforceInterface(DESCRIPTOR);
                    List<CmpTypeInfo> _result8 = getCmpTypeList();
                    reply.writeNoException();
                    reply.writeTypedList(_result8);
                    return true;
                case 17:
                    data.enforceInterface(DESCRIPTOR);
                    _result = deleteAppCmpTypeInfo(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 18:
                    data.enforceInterface(DESCRIPTOR);
                    custConfigUpdate();
                    reply.writeNoException();
                    return true;
                case 19:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getInstalledGameList();
                    reply.writeNoException();
                    reply.writeStringList(_result4);
                    return true;
                case 20:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isAppRecognizedGame(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 21:
                    data.enforceInterface(DESCRIPTOR);
                    updateSOCLevel(data.readInt());
                    reply.writeNoException();
                    return true;
                case 22:
                    data.enforceInterface(DESCRIPTOR);
                    registerTargetFpsObserver(android.rms.iaware.IReportFpsCallback.Stub.asInterface(data.readStrongBinder()), data.readLong());
                    reply.writeNoException();
                    return true;
                case 23:
                    data.enforceInterface(DESCRIPTOR);
                    unregisterTargetFpsObserver(data.readLong());
                    reply.writeNoException();
                    return true;
                case 24:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = isWinFrameOptEnable(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case 25:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getAppType(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void configUpdate() throws RemoteException;

    void custConfigUpdate() throws RemoteException;

    boolean deleteAppCmpTypeInfo(String str) throws RemoteException;

    boolean deleteCmpTypeInfo(CmpTypeInfo cmpTypeInfo) throws RemoteException;

    void disableFeature(int i) throws RemoteException;

    void enableFeature(int i) throws RemoteException;

    ParceledListSlice getAllAppTypeInfo() throws RemoteException;

    List<AppPreloadInfo> getAppPreloadList() throws RemoteException;

    int getAppType(String str) throws RemoteException;

    AppTypeInfo getAppTypeInfo(String str) throws RemoteException;

    List<CmpTypeInfo> getCmpTypeList() throws RemoteException;

    AwareConfig getConfig(String str, String str2) throws RemoteException;

    AwareConfig getCustConfig(String str, String str2) throws RemoteException;

    int getDumpData(int i, List<DumpData> list) throws RemoteException;

    List<String> getInstalledGameList() throws RemoteException;

    int getStatisticsData(List<StatisticsData> list) throws RemoteException;

    List<String> getZipFiles() throws RemoteException;

    boolean insertCmpTypeInfo(CmpTypeInfo cmpTypeInfo) throws RemoteException;

    boolean isAppRecognizedGame(String str) throws RemoteException;

    boolean isFeatureEnabled(int i) throws RemoteException;

    boolean isIAwareEnabled() throws RemoteException;

    int isWinFrameOptEnable(String str) throws RemoteException;

    void registerTargetFpsObserver(IReportFpsCallback iReportFpsCallback, long j) throws RemoteException;

    void unregisterTargetFpsObserver(long j) throws RemoteException;

    void updateSOCLevel(int i) throws RemoteException;
}
