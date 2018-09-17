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
        static final int TRANSACTION_disableFeature = 3;
        static final int TRANSACTION_enableFeature = 2;
        static final int TRANSACTION_getAllAppTypeInfo = 11;
        static final int TRANSACTION_getAppPreloadList = 12;
        static final int TRANSACTION_getAppTypeInfo = 10;
        static final int TRANSACTION_getConfig = 6;
        static final int TRANSACTION_getCustConfig = 13;
        static final int TRANSACTION_getDumpData = 7;
        static final int TRANSACTION_getStatisticsData = 8;
        static final int TRANSACTION_getZipFiles = 9;
        static final int TRANSACTION_isFeatureEnabled = 4;
        static final int TRANSACTION_isIAwareEnabled = 5;

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
                    this.mRemote.transact(Stub.TRANSACTION_configUpdate, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_enableFeature, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_disableFeature, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_isFeatureEnabled, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_isIAwareEnabled, _data, _reply, 0);
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
                    AwareConfig awareConfig;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(featureName);
                    _data.writeString(configName);
                    this.mRemote.transact(Stub.TRANSACTION_getConfig, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        awareConfig = (AwareConfig) AwareConfig.CREATOR.createFromParcel(_reply);
                    } else {
                        awareConfig = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return awareConfig;
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
                    this.mRemote.transact(Stub.TRANSACTION_getDumpData, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_getStatisticsData, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_getZipFiles, _data, _reply, 0);
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
                    AppTypeInfo appTypeInfo;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    this.mRemote.transact(Stub.TRANSACTION_getAppTypeInfo, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        appTypeInfo = (AppTypeInfo) AppTypeInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        appTypeInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return appTypeInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParceledListSlice getAllAppTypeInfo() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ParceledListSlice parceledListSlice;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getAllAppTypeInfo, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        parceledListSlice = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        parceledListSlice = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return parceledListSlice;
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
                    this.mRemote.transact(Stub.TRANSACTION_getAppPreloadList, _data, _reply, 0);
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
                    AwareConfig awareConfig;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(featureName);
                    _data.writeString(configName);
                    this.mRemote.transact(Stub.TRANSACTION_getCustConfig, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        awareConfig = (AwareConfig) AwareConfig.CREATOR.createFromParcel(_reply);
                    } else {
                        awareConfig = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return awareConfig;
                } catch (Throwable th) {
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
            switch (code) {
                case TRANSACTION_configUpdate /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    configUpdate();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_enableFeature /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    enableFeature(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_disableFeature /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    disableFeature(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isFeatureEnabled /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isFeatureEnabled(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_configUpdate : 0);
                    return true;
                case TRANSACTION_isIAwareEnabled /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isIAwareEnabled();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_configUpdate : 0);
                    return true;
                case TRANSACTION_getConfig /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getConfig(data.readString(), data.readString());
                    reply.writeNoException();
                    if (_result2 != null) {
                        reply.writeInt(TRANSACTION_configUpdate);
                        _result2.writeToParcel(reply, TRANSACTION_configUpdate);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getDumpData /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    int _arg0 = data.readInt();
                    List<DumpData> _arg1 = new ArrayList();
                    _result3 = getDumpData(_arg0, _arg1);
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    reply.writeTypedList(_arg1);
                    return true;
                case TRANSACTION_getStatisticsData /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    List<StatisticsData> _arg02 = new ArrayList();
                    _result3 = getStatisticsData(_arg02);
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    reply.writeTypedList(_arg02);
                    return true;
                case TRANSACTION_getZipFiles /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    List<String> _result4 = getZipFiles();
                    reply.writeNoException();
                    reply.writeStringList(_result4);
                    return true;
                case TRANSACTION_getAppTypeInfo /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    AppTypeInfo _result5 = getAppTypeInfo(data.readString());
                    reply.writeNoException();
                    if (_result5 != null) {
                        reply.writeInt(TRANSACTION_configUpdate);
                        _result5.writeToParcel(reply, TRANSACTION_configUpdate);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getAllAppTypeInfo /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    ParceledListSlice _result6 = getAllAppTypeInfo();
                    reply.writeNoException();
                    if (_result6 != null) {
                        reply.writeInt(TRANSACTION_configUpdate);
                        _result6.writeToParcel(reply, TRANSACTION_configUpdate);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getAppPreloadList /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    List<AppPreloadInfo> _result7 = getAppPreloadList();
                    reply.writeNoException();
                    reply.writeTypedList(_result7);
                    return true;
                case TRANSACTION_getCustConfig /*13*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getCustConfig(data.readString(), data.readString());
                    reply.writeNoException();
                    if (_result2 != null) {
                        reply.writeInt(TRANSACTION_configUpdate);
                        _result2.writeToParcel(reply, TRANSACTION_configUpdate);
                    } else {
                        reply.writeInt(0);
                    }
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

    void disableFeature(int i) throws RemoteException;

    void enableFeature(int i) throws RemoteException;

    ParceledListSlice getAllAppTypeInfo() throws RemoteException;

    List<AppPreloadInfo> getAppPreloadList() throws RemoteException;

    AppTypeInfo getAppTypeInfo(String str) throws RemoteException;

    AwareConfig getConfig(String str, String str2) throws RemoteException;

    AwareConfig getCustConfig(String str, String str2) throws RemoteException;

    int getDumpData(int i, List<DumpData> list) throws RemoteException;

    int getStatisticsData(List<StatisticsData> list) throws RemoteException;

    List<String> getZipFiles() throws RemoteException;

    boolean isFeatureEnabled(int i) throws RemoteException;

    boolean isIAwareEnabled() throws RemoteException;
}
