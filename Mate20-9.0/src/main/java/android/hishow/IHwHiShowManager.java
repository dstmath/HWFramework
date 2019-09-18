package android.hishow;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IHwHiShowManager extends IInterface {

    public static abstract class Stub extends Binder implements IHwHiShowManager {
        private static final String DESCRIPTOR = "android.hishow.IHwHiShowManager";
        static final int TRANSACTION_addNewAlarm = 11;
        static final int TRANSACTION_cancelActivityController = 18;
        static final int TRANSACTION_closeCurrentAlarm = 14;
        static final int TRANSACTION_controlAlarm = 13;
        static final int TRANSACTION_controlFloatButton = 5;
        static final int TRANSACTION_controlHomeButton = 3;
        static final int TRANSACTION_controlRecentButton = 4;
        static final int TRANSACTION_controlStatusBar = 2;
        static final int TRANSACTION_deleteAlarm = 12;
        static final int TRANSACTION_lightOffScreen = 9;
        static final int TRANSACTION_lockScreen = 1;
        static final int TRANSACTION_queryAllAlarmInfo = 10;
        static final int TRANSACTION_requestSpecialInfo = 19;
        static final int TRANSACTION_restorePreLauncher = 16;
        static final int TRANSACTION_setActivityController = 17;
        static final int TRANSACTION_setAsDefaultLauncher = 15;
        static final int TRANSACTION_startToCharge = 7;
        static final int TRANSACTION_stopCharging = 8;
        static final int TRANSACTION_switchDisturb = 6;

        private static class Proxy implements IHwHiShowManager {
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

            public void lockScreen() throws RemoteException {
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

            public void controlStatusBar(boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void controlHomeButton(boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void controlRecentButton(boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void controlFloatButton(boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void switchDisturb(boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void startToCharge() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void stopCharging() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void lightOffScreen(int brightness) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(brightness);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<AlarmInfo> queryAllAlarmInfo() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createTypedArrayList(AlarmInfo.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int addNewAlarm(AlarmInfo alarmInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (alarmInfo != null) {
                        _data.writeInt(1);
                        alarmInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean deleteAlarm(int alarmId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(alarmId);
                    boolean _result = false;
                    this.mRemote.transact(12, _data, _reply, 0);
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

            public boolean controlAlarm(boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable);
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

            public void closeCurrentAlarm() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setAsDefaultLauncher(String pkgName, String activityName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeString(activityName);
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

            public boolean restorePreLauncher() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(16, _data, _reply, 0);
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

            public void setActivityController(List<String> pkgWhitelist, List<String> actWhitelist, List<String> pkgBlacklist, List<String> actBlackList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(pkgWhitelist);
                    _data.writeStringList(actWhitelist);
                    _data.writeStringList(pkgBlacklist);
                    _data.writeStringList(actBlackList);
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void cancelActivityController() throws RemoteException {
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

            public String requestSpecialInfo(String key) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(key);
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwHiShowManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwHiShowManager)) {
                return new Proxy(obj);
            }
            return (IHwHiShowManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            AlarmInfo _arg0;
            if (code != 1598968902) {
                boolean _arg02 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        lockScreen();
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = true;
                        }
                        controlStatusBar(_arg02);
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = true;
                        }
                        controlHomeButton(_arg02);
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = true;
                        }
                        controlRecentButton(_arg02);
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = true;
                        }
                        controlFloatButton(_arg02);
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = true;
                        }
                        switchDisturb(_arg02);
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        startToCharge();
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        stopCharging();
                        reply.writeNoException();
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        lightOffScreen(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        List<AlarmInfo> _result = queryAllAlarmInfo();
                        reply.writeNoException();
                        reply.writeTypedList(_result);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = AlarmInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        int _result2 = addNewAlarm(_arg0);
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result3 = deleteAlarm(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = true;
                        }
                        boolean _result4 = controlAlarm(_arg02);
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        closeCurrentAlarm();
                        reply.writeNoException();
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result5 = setAsDefaultLauncher(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result6 = restorePreLauncher();
                        reply.writeNoException();
                        reply.writeInt(_result6);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        setActivityController(data.createStringArrayList(), data.createStringArrayList(), data.createStringArrayList(), data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        cancelActivityController();
                        reply.writeNoException();
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        String _result7 = requestSpecialInfo(data.readString());
                        reply.writeNoException();
                        reply.writeString(_result7);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    int addNewAlarm(AlarmInfo alarmInfo) throws RemoteException;

    void cancelActivityController() throws RemoteException;

    void closeCurrentAlarm() throws RemoteException;

    boolean controlAlarm(boolean z) throws RemoteException;

    void controlFloatButton(boolean z) throws RemoteException;

    void controlHomeButton(boolean z) throws RemoteException;

    void controlRecentButton(boolean z) throws RemoteException;

    void controlStatusBar(boolean z) throws RemoteException;

    boolean deleteAlarm(int i) throws RemoteException;

    void lightOffScreen(int i) throws RemoteException;

    void lockScreen() throws RemoteException;

    List<AlarmInfo> queryAllAlarmInfo() throws RemoteException;

    String requestSpecialInfo(String str) throws RemoteException;

    boolean restorePreLauncher() throws RemoteException;

    void setActivityController(List<String> list, List<String> list2, List<String> list3, List<String> list4) throws RemoteException;

    boolean setAsDefaultLauncher(String str, String str2) throws RemoteException;

    void startToCharge() throws RemoteException;

    void stopCharging() throws RemoteException;

    void switchDisturb(boolean z) throws RemoteException;
}
