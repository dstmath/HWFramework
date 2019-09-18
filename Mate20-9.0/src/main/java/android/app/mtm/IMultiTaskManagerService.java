package android.app.mtm;

import android.app.mtm.IMultiTaskProcessObserver;
import android.app.mtm.iaware.HwAppStartupSetting;
import android.app.mtm.iaware.HwAppStartupSettingFilter;
import android.app.mtm.iaware.RSceneData;
import android.app.mtm.iaware.appmng.AppCleanParam;
import android.app.mtm.iaware.appmng.IAppCleanCallback;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.rms.iaware.RPolicyData;
import java.util.List;

public interface IMultiTaskManagerService extends IInterface {

    public static abstract class Stub extends Binder implements IMultiTaskManagerService {
        private static final String DESCRIPTOR = "android.app.mtm.IMultiTaskManagerService";
        static final int TRANSACTION_acquirePolicyData = 10;
        static final int TRANSACTION_forcestopApps = 8;
        static final int TRANSACTION_killProcess = 4;
        static final int TRANSACTION_notifyProcessDiedChange = 7;
        static final int TRANSACTION_notifyProcessGroupChange = 5;
        static final int TRANSACTION_notifyProcessStatusChange = 6;
        static final int TRANSACTION_notifyResourceStatusOverload = 3;
        static final int TRANSACTION_registerObserver = 1;
        static final int TRANSACTION_removeAppStartupSetting = 14;
        static final int TRANSACTION_reportScene = 9;
        static final int TRANSACTION_requestAppCleanWithCallback = 16;
        static final int TRANSACTION_retrieveAppStartupPackages = 12;
        static final int TRANSACTION_retrieveAppStartupSettings = 11;
        static final int TRANSACTION_unregisterObserver = 2;
        static final int TRANSACTION_updateAppStartupSettings = 13;
        static final int TRANSACTION_updateCloudPolicy = 15;

        private static class Proxy implements IMultiTaskManagerService {
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

            public void registerObserver(IMultiTaskProcessObserver observer) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregisterObserver(IMultiTaskProcessObserver observer) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void notifyResourceStatusOverload(int resourcetype, String resourceextend, int resourcestatus, Bundle args) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(resourcetype);
                    _data.writeString(resourceextend);
                    _data.writeInt(resourcestatus);
                    if (args != null) {
                        _data.writeInt(1);
                        args.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean killProcess(int pid, boolean restartservice) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    _data.writeInt(restartservice);
                    boolean _result = false;
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void notifyProcessGroupChange(int pid, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void notifyProcessStatusChange(String pkg, String process, String hostingType, int pid, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeString(process);
                    _data.writeString(hostingType);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void notifyProcessDiedChange(int pid, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean forcestopApps(int pid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    boolean _result = false;
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean reportScene(int featureId, RSceneData scene) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(featureId);
                    boolean _result = true;
                    if (scene != null) {
                        _data.writeInt(1);
                        scene.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public RPolicyData acquirePolicyData(int featureId, RSceneData scene) throws RemoteException {
                RPolicyData _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(featureId);
                    if (scene != null) {
                        _data.writeInt(1);
                        scene.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = RPolicyData.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<HwAppStartupSetting> retrieveAppStartupSettings(List<String> pkgList, HwAppStartupSettingFilter filter) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(pkgList);
                    if (filter != null) {
                        _data.writeInt(1);
                        filter.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createTypedArrayList(HwAppStartupSetting.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> retrieveAppStartupPackages(List<String> pkgList, int[] policy, int[] modifier, int[] show) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(pkgList);
                    _data.writeIntArray(policy);
                    _data.writeIntArray(modifier);
                    _data.writeIntArray(show);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArrayList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean updateAppStartupSettings(List<HwAppStartupSetting> settingList, boolean clearFirst) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedList(settingList);
                    _data.writeInt(clearFirst);
                    boolean _result = false;
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean removeAppStartupSetting(String pkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    boolean _result = false;
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean updateCloudPolicy(String filePath) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(filePath);
                    boolean _result = false;
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void requestAppCleanWithCallback(AppCleanParam param, IAppCleanCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (param != null) {
                        _data.writeInt(1);
                        param.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(16, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IMultiTaskManagerService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IMultiTaskManagerService)) {
                return new Proxy(obj);
            }
            return (IMultiTaskManagerService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v1, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v10, resolved type: android.app.mtm.iaware.RSceneData} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v14, resolved type: android.app.mtm.iaware.RSceneData} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v18, resolved type: android.app.mtm.iaware.HwAppStartupSettingFilter} */
        /* JADX WARNING: type inference failed for: r1v0 */
        /* JADX WARNING: type inference failed for: r1v27 */
        /* JADX WARNING: type inference failed for: r1v32 */
        /* JADX WARNING: type inference failed for: r1v33 */
        /* JADX WARNING: type inference failed for: r1v34 */
        /* JADX WARNING: type inference failed for: r1v35 */
        /* JADX WARNING: type inference failed for: r1v36 */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int i = code;
            Parcel parcel = data;
            Parcel parcel2 = reply;
            if (i != 1598968902) {
                boolean _arg1 = false;
                ? _arg12 = 0;
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        registerObserver(IMultiTaskProcessObserver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        unregisterObserver(IMultiTaskProcessObserver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 3:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg0 = data.readInt();
                        String _arg13 = data.readString();
                        int _arg2 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg12 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        notifyResourceStatusOverload(_arg0, _arg13, _arg2, _arg12);
                        reply.writeNoException();
                        return true;
                    case 4:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg02 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        boolean _result = killProcess(_arg02, _arg1);
                        reply.writeNoException();
                        parcel2.writeInt(_result);
                        return true;
                    case 5:
                        parcel.enforceInterface(DESCRIPTOR);
                        notifyProcessGroupChange(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 6:
                        parcel.enforceInterface(DESCRIPTOR);
                        notifyProcessStatusChange(data.readString(), data.readString(), data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 7:
                        parcel.enforceInterface(DESCRIPTOR);
                        notifyProcessDiedChange(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 8:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result2 = forcestopApps(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result2);
                        return true;
                    case 9:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg03 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg12 = RSceneData.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result3 = reportScene(_arg03, _arg12);
                        reply.writeNoException();
                        parcel2.writeInt(_result3);
                        return true;
                    case 10:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg04 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg12 = RSceneData.CREATOR.createFromParcel(parcel);
                        }
                        RPolicyData _result4 = acquirePolicyData(_arg04, _arg12);
                        reply.writeNoException();
                        if (_result4 != null) {
                            parcel2.writeInt(1);
                            _result4.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 11:
                        parcel.enforceInterface(DESCRIPTOR);
                        List<String> _arg05 = data.createStringArrayList();
                        if (data.readInt() != 0) {
                            _arg12 = HwAppStartupSettingFilter.CREATOR.createFromParcel(parcel);
                        }
                        List<HwAppStartupSetting> _result5 = retrieveAppStartupSettings(_arg05, _arg12);
                        reply.writeNoException();
                        parcel2.writeTypedList(_result5);
                        return true;
                    case 12:
                        parcel.enforceInterface(DESCRIPTOR);
                        List<String> _result6 = retrieveAppStartupPackages(data.createStringArrayList(), data.createIntArray(), data.createIntArray(), data.createIntArray());
                        reply.writeNoException();
                        parcel2.writeStringList(_result6);
                        return true;
                    case 13:
                        parcel.enforceInterface(DESCRIPTOR);
                        List<HwAppStartupSetting> _arg06 = parcel.createTypedArrayList(HwAppStartupSetting.CREATOR);
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        boolean _result7 = updateAppStartupSettings(_arg06, _arg1);
                        reply.writeNoException();
                        parcel2.writeInt(_result7);
                        return true;
                    case 14:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result8 = removeAppStartupSetting(data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result8);
                        return true;
                    case 15:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result9 = updateCloudPolicy(data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result9);
                        return true;
                    case 16:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg12 = AppCleanParam.CREATOR.createFromParcel(parcel);
                        }
                        requestAppCleanWithCallback(_arg12, IAppCleanCallback.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                parcel2.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    RPolicyData acquirePolicyData(int i, RSceneData rSceneData) throws RemoteException;

    boolean forcestopApps(int i) throws RemoteException;

    boolean killProcess(int i, boolean z) throws RemoteException;

    void notifyProcessDiedChange(int i, int i2) throws RemoteException;

    void notifyProcessGroupChange(int i, int i2) throws RemoteException;

    void notifyProcessStatusChange(String str, String str2, String str3, int i, int i2) throws RemoteException;

    void notifyResourceStatusOverload(int i, String str, int i2, Bundle bundle) throws RemoteException;

    void registerObserver(IMultiTaskProcessObserver iMultiTaskProcessObserver) throws RemoteException;

    boolean removeAppStartupSetting(String str) throws RemoteException;

    boolean reportScene(int i, RSceneData rSceneData) throws RemoteException;

    void requestAppCleanWithCallback(AppCleanParam appCleanParam, IAppCleanCallback iAppCleanCallback) throws RemoteException;

    List<String> retrieveAppStartupPackages(List<String> list, int[] iArr, int[] iArr2, int[] iArr3) throws RemoteException;

    List<HwAppStartupSetting> retrieveAppStartupSettings(List<String> list, HwAppStartupSettingFilter hwAppStartupSettingFilter) throws RemoteException;

    void unregisterObserver(IMultiTaskProcessObserver iMultiTaskProcessObserver) throws RemoteException;

    boolean updateAppStartupSettings(List<HwAppStartupSetting> list, boolean z) throws RemoteException;

    boolean updateCloudPolicy(String str) throws RemoteException;
}
