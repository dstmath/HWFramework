package com.android.internal.policy;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IKeyguardService extends IInterface {

    public static abstract class Stub extends Binder implements IKeyguardService {
        private static final String DESCRIPTOR = "com.android.internal.policy.IKeyguardService";
        static final int TRANSACTION_addStateMonitorCallback = 2;
        static final int TRANSACTION_dismiss = 4;
        static final int TRANSACTION_doKeyguardTimeout = 15;
        static final int TRANSACTION_onBootCompleted = 18;
        static final int TRANSACTION_onDreamingStarted = 5;
        static final int TRANSACTION_onDreamingStopped = 6;
        static final int TRANSACTION_onFinishedGoingToSleep = 8;
        static final int TRANSACTION_onScreenTurnedOff = 12;
        static final int TRANSACTION_onScreenTurnedOn = 11;
        static final int TRANSACTION_onScreenTurningOn = 10;
        static final int TRANSACTION_onShortPowerPressedGoHome = 20;
        static final int TRANSACTION_onStartedGoingToSleep = 7;
        static final int TRANSACTION_onStartedWakingUp = 9;
        static final int TRANSACTION_onSystemReady = 14;
        static final int TRANSACTION_setCurrentUser = 17;
        static final int TRANSACTION_setKeyguardEnabled = 13;
        static final int TRANSACTION_setOccluded = 1;
        static final int TRANSACTION_setSwitchingUser = 16;
        static final int TRANSACTION_startKeyguardExitAnimation = 19;
        static final int TRANSACTION_verifyUnlock = 3;

        private static class Proxy implements IKeyguardService {
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

            public void setOccluded(boolean isOccluded, boolean animate) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(isOccluded ? 1 : 0);
                    if (!animate) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void addStateMonitorCallback(IKeyguardStateCallback callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void verifyUnlock(IKeyguardExitCallback callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void dismiss(IKeyguardDismissCallback callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onDreamingStarted() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(5, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onDreamingStopped() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(6, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onStartedGoingToSleep(int reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(reason);
                    this.mRemote.transact(7, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onFinishedGoingToSleep(int reason, boolean cameraGestureTriggered) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(reason);
                    if (!cameraGestureTriggered) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(8, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onStartedWakingUp() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(9, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onScreenTurningOn(IKeyguardDrawnCallback callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(10, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onScreenTurnedOn() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(11, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onScreenTurnedOff() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(12, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void setKeyguardEnabled(boolean enabled) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!enabled) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(13, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onSystemReady() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(14, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void doKeyguardTimeout(Bundle options) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (options != null) {
                        _data.writeInt(1);
                        options.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(15, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void setSwitchingUser(boolean switching) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!switching) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(16, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void setCurrentUser(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(17, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onBootCompleted() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(18, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void startKeyguardExitAnimation(long startTime, long fadeoutDuration) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(startTime);
                    _data.writeLong(fadeoutDuration);
                    this.mRemote.transact(19, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onShortPowerPressedGoHome() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(20, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IKeyguardService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IKeyguardService)) {
                return new Proxy(obj);
            }
            return (IKeyguardService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    setOccluded(data.readInt() != 0, data.readInt() != 0);
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    addStateMonitorCallback(com.android.internal.policy.IKeyguardStateCallback.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    verifyUnlock(com.android.internal.policy.IKeyguardExitCallback.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    dismiss(com.android.internal.policy.IKeyguardDismissCallback.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    onDreamingStarted();
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    onDreamingStopped();
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    onStartedGoingToSleep(data.readInt());
                    return true;
                case 8:
                    data.enforceInterface(DESCRIPTOR);
                    onFinishedGoingToSleep(data.readInt(), data.readInt() != 0);
                    return true;
                case 9:
                    data.enforceInterface(DESCRIPTOR);
                    onStartedWakingUp();
                    return true;
                case 10:
                    data.enforceInterface(DESCRIPTOR);
                    onScreenTurningOn(com.android.internal.policy.IKeyguardDrawnCallback.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case 11:
                    data.enforceInterface(DESCRIPTOR);
                    onScreenTurnedOn();
                    return true;
                case 12:
                    data.enforceInterface(DESCRIPTOR);
                    onScreenTurnedOff();
                    return true;
                case 13:
                    data.enforceInterface(DESCRIPTOR);
                    setKeyguardEnabled(data.readInt() != 0);
                    return true;
                case 14:
                    data.enforceInterface(DESCRIPTOR);
                    onSystemReady();
                    return true;
                case 15:
                    Bundle _arg0;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    doKeyguardTimeout(_arg0);
                    return true;
                case 16:
                    data.enforceInterface(DESCRIPTOR);
                    setSwitchingUser(data.readInt() != 0);
                    return true;
                case 17:
                    data.enforceInterface(DESCRIPTOR);
                    setCurrentUser(data.readInt());
                    return true;
                case 18:
                    data.enforceInterface(DESCRIPTOR);
                    onBootCompleted();
                    return true;
                case 19:
                    data.enforceInterface(DESCRIPTOR);
                    startKeyguardExitAnimation(data.readLong(), data.readLong());
                    return true;
                case 20:
                    data.enforceInterface(DESCRIPTOR);
                    onShortPowerPressedGoHome();
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void addStateMonitorCallback(IKeyguardStateCallback iKeyguardStateCallback) throws RemoteException;

    void dismiss(IKeyguardDismissCallback iKeyguardDismissCallback) throws RemoteException;

    void doKeyguardTimeout(Bundle bundle) throws RemoteException;

    void onBootCompleted() throws RemoteException;

    void onDreamingStarted() throws RemoteException;

    void onDreamingStopped() throws RemoteException;

    void onFinishedGoingToSleep(int i, boolean z) throws RemoteException;

    void onScreenTurnedOff() throws RemoteException;

    void onScreenTurnedOn() throws RemoteException;

    void onScreenTurningOn(IKeyguardDrawnCallback iKeyguardDrawnCallback) throws RemoteException;

    void onShortPowerPressedGoHome() throws RemoteException;

    void onStartedGoingToSleep(int i) throws RemoteException;

    void onStartedWakingUp() throws RemoteException;

    void onSystemReady() throws RemoteException;

    void setCurrentUser(int i) throws RemoteException;

    void setKeyguardEnabled(boolean z) throws RemoteException;

    void setOccluded(boolean z, boolean z2) throws RemoteException;

    void setSwitchingUser(boolean z) throws RemoteException;

    void startKeyguardExitAnimation(long j, long j2) throws RemoteException;

    void verifyUnlock(IKeyguardExitCallback iKeyguardExitCallback) throws RemoteException;
}
