package com.huawei.systemserver.activityrecognition;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.systemserver.activityrecognition.IActivityRecognitionHardwareSink;

public interface IActivityRecognitionHardwareService extends IInterface {

    public static abstract class Stub extends Binder implements IActivityRecognitionHardwareService {
        private static final String DESCRIPTOR = "com.huawei.systemserver.activityrecognition.IActivityRecognitionHardwareService";
        static final int TRANSACTION_disableActivityEvent = 7;
        static final int TRANSACTION_disableEnvironmentEvent = 15;
        static final int TRANSACTION_enableActivityEvent = 5;
        static final int TRANSACTION_enableActivityExtendEvent = 6;
        static final int TRANSACTION_enableEnvironmentEvent = 14;
        static final int TRANSACTION_exitEnvironmentFunction = 12;
        static final int TRANSACTION_flush = 9;
        static final int TRANSACTION_getARVersion = 16;
        static final int TRANSACTION_getCurrentActivity = 8;
        static final int TRANSACTION_getCurrentActivityV1_1 = 17;
        static final int TRANSACTION_getCurrentEnvironment = 13;
        static final int TRANSACTION_getCurrentEnvironmentV1_1 = 18;
        static final int TRANSACTION_getSupportedActivities = 2;
        static final int TRANSACTION_getSupportedEnvironments = 10;
        static final int TRANSACTION_getSupportedModule = 1;
        static final int TRANSACTION_initEnvironmentFunction = 11;
        static final int TRANSACTION_registerSink = 3;
        static final int TRANSACTION_unregisterSink = 4;

        private static class Proxy implements IActivityRecognitionHardwareService {
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

            public int getSupportedModule() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String[] getSupportedActivities() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean registerSink(String packageName, IActivityRecognitionHardwareSink sink) throws RemoteException {
                boolean _result = false;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeStrongBinder(sink != null ? sink.asBinder() : null);
                    this.mRemote.transact(3, _data, _reply, 0);
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

            public boolean unregisterSink(String packageName, IActivityRecognitionHardwareSink sink) throws RemoteException {
                boolean _result = false;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeStrongBinder(sink != null ? sink.asBinder() : null);
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

            public boolean enableActivityEvent(String packageName, String activity, int eventType, long reportLatencyNs) throws RemoteException {
                boolean _result = false;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(activity);
                    _data.writeInt(eventType);
                    _data.writeLong(reportLatencyNs);
                    this.mRemote.transact(5, _data, _reply, 0);
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

            public boolean enableActivityExtendEvent(String packageName, String activity, int eventType, long reportLatencyNs, OtherParameters params) throws RemoteException {
                boolean _result = true;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(activity);
                    _data.writeInt(eventType);
                    _data.writeLong(reportLatencyNs);
                    if (params != null) {
                        _data.writeInt(1);
                        params.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(6, _data, _reply, 0);
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

            public boolean disableActivityEvent(String packageName, String activity, int eventType) throws RemoteException {
                boolean _result = false;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(activity);
                    _data.writeInt(eventType);
                    this.mRemote.transact(7, _data, _reply, 0);
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

            public HwActivityChangedExtendEvent getCurrentActivity() throws RemoteException {
                HwActivityChangedExtendEvent _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = HwActivityChangedExtendEvent.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean flush() throws RemoteException {
                boolean _result = false;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(9, _data, _reply, 0);
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

            public String[] getSupportedEnvironments() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean initEnvironmentFunction(String packageName, String environment, OtherParameters params) throws RemoteException {
                boolean _result = true;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(environment);
                    if (params != null) {
                        _data.writeInt(1);
                        params.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(11, _data, _reply, 0);
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

            public boolean exitEnvironmentFunction(String packageName, String environment, OtherParameters params) throws RemoteException {
                boolean _result = true;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(environment);
                    if (params != null) {
                        _data.writeInt(1);
                        params.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(12, _data, _reply, 0);
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

            public HwEnvironmentChangedEvent getCurrentEnvironment() throws RemoteException {
                HwEnvironmentChangedEvent _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = HwEnvironmentChangedEvent.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean enableEnvironmentEvent(String packageName, String environment, int eventType, long reportLatencyNs, OtherParameters params) throws RemoteException {
                boolean _result = true;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(environment);
                    _data.writeInt(eventType);
                    _data.writeLong(reportLatencyNs);
                    if (params != null) {
                        _data.writeInt(1);
                        params.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(14, _data, _reply, 0);
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

            public boolean disableEnvironmentEvent(String packageName, String environment, int eventType) throws RemoteException {
                boolean _result = false;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(environment);
                    _data.writeInt(eventType);
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

            public int getARVersion(String packageName, int sdkVersion) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(sdkVersion);
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public HwActivityChangedExtendEvent getCurrentActivityV1_1() throws RemoteException {
                HwActivityChangedExtendEvent _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getCurrentActivityV1_1, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = HwActivityChangedExtendEvent.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public HwEnvironmentChangedEvent getCurrentEnvironmentV1_1() throws RemoteException {
                HwEnvironmentChangedEvent _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getCurrentEnvironmentV1_1, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = HwEnvironmentChangedEvent.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
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

        public static IActivityRecognitionHardwareService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IActivityRecognitionHardwareService)) {
                return new Proxy(obj);
            }
            return (IActivityRecognitionHardwareService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            OtherParameters _arg4;
            OtherParameters _arg2;
            OtherParameters _arg22;
            OtherParameters _arg42;
            int i = 0;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    int _result = getSupportedModule();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    String[] _result2 = getSupportedActivities();
                    reply.writeNoException();
                    reply.writeStringArray(_result2);
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    boolean _result3 = registerSink(data.readString(), IActivityRecognitionHardwareSink.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    boolean _result4 = unregisterSink(data.readString(), IActivityRecognitionHardwareSink.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    if (_result4) {
                        i = 1;
                    }
                    reply.writeInt(i);
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    boolean _result5 = enableActivityEvent(data.readString(), data.readString(), data.readInt(), data.readLong());
                    reply.writeNoException();
                    if (_result5) {
                        i = 1;
                    }
                    reply.writeInt(i);
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    String _arg0 = data.readString();
                    String _arg1 = data.readString();
                    int _arg23 = data.readInt();
                    long _arg3 = data.readLong();
                    if (data.readInt() != 0) {
                        _arg42 = OtherParameters.CREATOR.createFromParcel(data);
                    } else {
                        _arg42 = null;
                    }
                    boolean _result6 = enableActivityExtendEvent(_arg0, _arg1, _arg23, _arg3, _arg42);
                    reply.writeNoException();
                    if (_result6) {
                        i = 1;
                    }
                    reply.writeInt(i);
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    boolean _result7 = disableActivityEvent(data.readString(), data.readString(), data.readInt());
                    reply.writeNoException();
                    if (_result7) {
                        i = 1;
                    }
                    reply.writeInt(i);
                    return true;
                case 8:
                    data.enforceInterface(DESCRIPTOR);
                    HwActivityChangedExtendEvent _result8 = getCurrentActivity();
                    reply.writeNoException();
                    if (_result8 != null) {
                        reply.writeInt(1);
                        _result8.writeToParcel(reply, 1);
                        return true;
                    }
                    reply.writeInt(0);
                    return true;
                case 9:
                    data.enforceInterface(DESCRIPTOR);
                    boolean _result9 = flush();
                    reply.writeNoException();
                    if (_result9) {
                        i = 1;
                    }
                    reply.writeInt(i);
                    return true;
                case 10:
                    data.enforceInterface(DESCRIPTOR);
                    String[] _result10 = getSupportedEnvironments();
                    reply.writeNoException();
                    reply.writeStringArray(_result10);
                    return true;
                case 11:
                    data.enforceInterface(DESCRIPTOR);
                    String _arg02 = data.readString();
                    String _arg12 = data.readString();
                    if (data.readInt() != 0) {
                        _arg22 = OtherParameters.CREATOR.createFromParcel(data);
                    } else {
                        _arg22 = null;
                    }
                    boolean _result11 = initEnvironmentFunction(_arg02, _arg12, _arg22);
                    reply.writeNoException();
                    if (_result11) {
                        i = 1;
                    }
                    reply.writeInt(i);
                    return true;
                case 12:
                    data.enforceInterface(DESCRIPTOR);
                    String _arg03 = data.readString();
                    String _arg13 = data.readString();
                    if (data.readInt() != 0) {
                        _arg2 = OtherParameters.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    boolean _result12 = exitEnvironmentFunction(_arg03, _arg13, _arg2);
                    reply.writeNoException();
                    if (_result12) {
                        i = 1;
                    }
                    reply.writeInt(i);
                    return true;
                case 13:
                    data.enforceInterface(DESCRIPTOR);
                    HwEnvironmentChangedEvent _result13 = getCurrentEnvironment();
                    reply.writeNoException();
                    if (_result13 != null) {
                        reply.writeInt(1);
                        _result13.writeToParcel(reply, 1);
                        return true;
                    }
                    reply.writeInt(0);
                    return true;
                case 14:
                    data.enforceInterface(DESCRIPTOR);
                    String _arg04 = data.readString();
                    String _arg14 = data.readString();
                    int _arg24 = data.readInt();
                    long _arg32 = data.readLong();
                    if (data.readInt() != 0) {
                        _arg4 = OtherParameters.CREATOR.createFromParcel(data);
                    } else {
                        _arg4 = null;
                    }
                    boolean _result14 = enableEnvironmentEvent(_arg04, _arg14, _arg24, _arg32, _arg4);
                    reply.writeNoException();
                    if (_result14) {
                        i = 1;
                    }
                    reply.writeInt(i);
                    return true;
                case 15:
                    data.enforceInterface(DESCRIPTOR);
                    boolean _result15 = disableEnvironmentEvent(data.readString(), data.readString(), data.readInt());
                    reply.writeNoException();
                    if (_result15) {
                        i = 1;
                    }
                    reply.writeInt(i);
                    return true;
                case 16:
                    data.enforceInterface(DESCRIPTOR);
                    int _result16 = getARVersion(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result16);
                    return true;
                case TRANSACTION_getCurrentActivityV1_1 /*17*/:
                    data.enforceInterface(DESCRIPTOR);
                    HwActivityChangedExtendEvent _result17 = getCurrentActivityV1_1();
                    reply.writeNoException();
                    if (_result17 != null) {
                        reply.writeInt(1);
                        _result17.writeToParcel(reply, 1);
                        return true;
                    }
                    reply.writeInt(0);
                    return true;
                case TRANSACTION_getCurrentEnvironmentV1_1 /*18*/:
                    data.enforceInterface(DESCRIPTOR);
                    HwEnvironmentChangedEvent _result18 = getCurrentEnvironmentV1_1();
                    reply.writeNoException();
                    if (_result18 != null) {
                        reply.writeInt(1);
                        _result18.writeToParcel(reply, 1);
                        return true;
                    }
                    reply.writeInt(0);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    boolean disableActivityEvent(String str, String str2, int i) throws RemoteException;

    boolean disableEnvironmentEvent(String str, String str2, int i) throws RemoteException;

    boolean enableActivityEvent(String str, String str2, int i, long j) throws RemoteException;

    boolean enableActivityExtendEvent(String str, String str2, int i, long j, OtherParameters otherParameters) throws RemoteException;

    boolean enableEnvironmentEvent(String str, String str2, int i, long j, OtherParameters otherParameters) throws RemoteException;

    boolean exitEnvironmentFunction(String str, String str2, OtherParameters otherParameters) throws RemoteException;

    boolean flush() throws RemoteException;

    int getARVersion(String str, int i) throws RemoteException;

    HwActivityChangedExtendEvent getCurrentActivity() throws RemoteException;

    HwActivityChangedExtendEvent getCurrentActivityV1_1() throws RemoteException;

    HwEnvironmentChangedEvent getCurrentEnvironment() throws RemoteException;

    HwEnvironmentChangedEvent getCurrentEnvironmentV1_1() throws RemoteException;

    String[] getSupportedActivities() throws RemoteException;

    String[] getSupportedEnvironments() throws RemoteException;

    int getSupportedModule() throws RemoteException;

    boolean initEnvironmentFunction(String str, String str2, OtherParameters otherParameters) throws RemoteException;

    boolean registerSink(String str, IActivityRecognitionHardwareSink iActivityRecognitionHardwareSink) throws RemoteException;

    boolean unregisterSink(String str, IActivityRecognitionHardwareSink iActivityRecognitionHardwareSink) throws RemoteException;
}
