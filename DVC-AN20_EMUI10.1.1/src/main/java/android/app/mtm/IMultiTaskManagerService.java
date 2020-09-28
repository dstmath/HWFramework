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

    public static class Default implements IMultiTaskManagerService {
        @Override // android.app.mtm.IMultiTaskManagerService
        public void registerObserver(IMultiTaskProcessObserver observer) throws RemoteException {
        }

        @Override // android.app.mtm.IMultiTaskManagerService
        public void unregisterObserver(IMultiTaskProcessObserver observer) throws RemoteException {
        }

        @Override // android.app.mtm.IMultiTaskManagerService
        public void notifyResourceStatusOverload(int resourcetype, String resourceextend, int resourcestatus, Bundle args) throws RemoteException {
        }

        @Override // android.app.mtm.IMultiTaskManagerService
        public boolean killProcess(int pid, boolean restartservice) throws RemoteException {
            return false;
        }

        @Override // android.app.mtm.IMultiTaskManagerService
        public void notifyProcessGroupChange(int pid, int uid) throws RemoteException {
        }

        @Override // android.app.mtm.IMultiTaskManagerService
        public void notifyProcessStatusChange(String pkg, String process, String hostingType, int pid, int uid) throws RemoteException {
        }

        @Override // android.app.mtm.IMultiTaskManagerService
        public void notifyProcessDiedChange(int pid, int uid) throws RemoteException {
        }

        @Override // android.app.mtm.IMultiTaskManagerService
        public boolean forcestopApps(int pid) throws RemoteException {
            return false;
        }

        @Override // android.app.mtm.IMultiTaskManagerService
        public boolean reportScene(int featureId, RSceneData scene) throws RemoteException {
            return false;
        }

        @Override // android.app.mtm.IMultiTaskManagerService
        public RPolicyData acquirePolicyData(int featureId, RSceneData scene) throws RemoteException {
            return null;
        }

        @Override // android.app.mtm.IMultiTaskManagerService
        public List<HwAppStartupSetting> retrieveAppStartupSettings(List<String> list, HwAppStartupSettingFilter filter) throws RemoteException {
            return null;
        }

        @Override // android.app.mtm.IMultiTaskManagerService
        public List<String> retrieveAppStartupPackages(List<String> list, int[] policy, int[] modifier, int[] show) throws RemoteException {
            return null;
        }

        @Override // android.app.mtm.IMultiTaskManagerService
        public boolean updateAppStartupSettings(List<HwAppStartupSetting> list, boolean clearFirst) throws RemoteException {
            return false;
        }

        @Override // android.app.mtm.IMultiTaskManagerService
        public boolean removeAppStartupSetting(String pkgName) throws RemoteException {
            return false;
        }

        @Override // android.app.mtm.IMultiTaskManagerService
        public boolean updateCloudPolicy(String filePath) throws RemoteException {
            return false;
        }

        @Override // android.app.mtm.IMultiTaskManagerService
        public void requestAppCleanWithCallback(AppCleanParam param, IAppCleanCallback callback) throws RemoteException {
        }

        public IBinder asBinder() {
            return null;
        }
    }

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

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Bundle _arg3;
            RSceneData _arg1;
            RSceneData _arg12;
            HwAppStartupSettingFilter _arg13;
            AppCleanParam _arg0;
            if (code != 1598968902) {
                boolean _arg14 = false;
                boolean _arg15 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        registerObserver(IMultiTaskProcessObserver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterObserver(IMultiTaskProcessObserver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg02 = data.readInt();
                        String _arg16 = data.readString();
                        int _arg2 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg3 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg3 = null;
                        }
                        notifyResourceStatusOverload(_arg02, _arg16, _arg2, _arg3);
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg03 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg14 = true;
                        }
                        boolean killProcess = killProcess(_arg03, _arg14);
                        reply.writeNoException();
                        reply.writeInt(killProcess ? 1 : 0);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        notifyProcessGroupChange(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        notifyProcessStatusChange(data.readString(), data.readString(), data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        notifyProcessDiedChange(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        boolean forcestopApps = forcestopApps(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(forcestopApps ? 1 : 0);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg04 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = RSceneData.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        boolean reportScene = reportScene(_arg04, _arg1);
                        reply.writeNoException();
                        reply.writeInt(reportScene ? 1 : 0);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg05 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg12 = RSceneData.CREATOR.createFromParcel(data);
                        } else {
                            _arg12 = null;
                        }
                        RPolicyData _result = acquirePolicyData(_arg05, _arg12);
                        reply.writeNoException();
                        if (_result != null) {
                            reply.writeInt(1);
                            _result.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _arg06 = data.createStringArrayList();
                        if (data.readInt() != 0) {
                            _arg13 = HwAppStartupSettingFilter.CREATOR.createFromParcel(data);
                        } else {
                            _arg13 = null;
                        }
                        List<HwAppStartupSetting> _result2 = retrieveAppStartupSettings(_arg06, _arg13);
                        reply.writeNoException();
                        reply.writeTypedList(_result2);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result3 = retrieveAppStartupPackages(data.createStringArrayList(), data.createIntArray(), data.createIntArray(), data.createIntArray());
                        reply.writeNoException();
                        reply.writeStringList(_result3);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        List<HwAppStartupSetting> _arg07 = data.createTypedArrayList(HwAppStartupSetting.CREATOR);
                        if (data.readInt() != 0) {
                            _arg15 = true;
                        }
                        boolean updateAppStartupSettings = updateAppStartupSettings(_arg07, _arg15);
                        reply.writeNoException();
                        reply.writeInt(updateAppStartupSettings ? 1 : 0);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        boolean removeAppStartupSetting = removeAppStartupSetting(data.readString());
                        reply.writeNoException();
                        reply.writeInt(removeAppStartupSetting ? 1 : 0);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        boolean updateCloudPolicy = updateCloudPolicy(data.readString());
                        reply.writeNoException();
                        reply.writeInt(updateCloudPolicy ? 1 : 0);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = AppCleanParam.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        requestAppCleanWithCallback(_arg0, IAppCleanCallback.Stub.asInterface(data.readStrongBinder()));
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
        public static class Proxy implements IMultiTaskManagerService {
            public static IMultiTaskManagerService sDefaultImpl;
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

            @Override // android.app.mtm.IMultiTaskManagerService
            public void registerObserver(IMultiTaskProcessObserver observer) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerObserver(observer);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.mtm.IMultiTaskManagerService
            public void unregisterObserver(IMultiTaskProcessObserver observer) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterObserver(observer);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.mtm.IMultiTaskManagerService
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
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().notifyResourceStatusOverload(resourcetype, resourceextend, resourcestatus, args);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.mtm.IMultiTaskManagerService
            public boolean killProcess(int pid, boolean restartservice) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    boolean _result = true;
                    _data.writeInt(restartservice ? 1 : 0);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().killProcess(pid, restartservice);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.mtm.IMultiTaskManagerService
            public void notifyProcessGroupChange(int pid, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().notifyProcessGroupChange(pid, uid);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.mtm.IMultiTaskManagerService
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
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().notifyProcessStatusChange(pkg, process, hostingType, pid, uid);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.mtm.IMultiTaskManagerService
            public void notifyProcessDiedChange(int pid, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().notifyProcessDiedChange(pid, uid);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.mtm.IMultiTaskManagerService
            public boolean forcestopApps(int pid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    boolean _result = false;
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().forcestopApps(pid);
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

            @Override // android.app.mtm.IMultiTaskManagerService
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
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().reportScene(featureId, scene);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.mtm.IMultiTaskManagerService
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
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().acquirePolicyData(featureId, scene);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = RPolicyData.CREATOR.createFromParcel(_reply);
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

            @Override // android.app.mtm.IMultiTaskManagerService
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
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().retrieveAppStartupSettings(pkgList, filter);
                    }
                    _reply.readException();
                    List<HwAppStartupSetting> _result = _reply.createTypedArrayList(HwAppStartupSetting.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.mtm.IMultiTaskManagerService
            public List<String> retrieveAppStartupPackages(List<String> pkgList, int[] policy, int[] modifier, int[] show) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(pkgList);
                    _data.writeIntArray(policy);
                    _data.writeIntArray(modifier);
                    _data.writeIntArray(show);
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().retrieveAppStartupPackages(pkgList, policy, modifier, show);
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.mtm.IMultiTaskManagerService
            public boolean updateAppStartupSettings(List<HwAppStartupSetting> settingList, boolean clearFirst) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedList(settingList);
                    boolean _result = true;
                    _data.writeInt(clearFirst ? 1 : 0);
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().updateAppStartupSettings(settingList, clearFirst);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.mtm.IMultiTaskManagerService
            public boolean removeAppStartupSetting(String pkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    boolean _result = false;
                    if (!this.mRemote.transact(14, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().removeAppStartupSetting(pkgName);
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

            @Override // android.app.mtm.IMultiTaskManagerService
            public boolean updateCloudPolicy(String filePath) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(filePath);
                    boolean _result = false;
                    if (!this.mRemote.transact(15, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().updateCloudPolicy(filePath);
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

            @Override // android.app.mtm.IMultiTaskManagerService
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
                    if (this.mRemote.transact(16, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().requestAppCleanWithCallback(param, callback);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IMultiTaskManagerService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IMultiTaskManagerService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
