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
        static final int TRANSACTION_dismiss = 5;
        static final int TRANSACTION_doKeyguardTimeout = 16;
        static final int TRANSACTION_keyguardDone = 4;
        static final int TRANSACTION_onActivityDrawn = 20;
        static final int TRANSACTION_onBootCompleted = 18;
        static final int TRANSACTION_onDreamingStarted = 6;
        static final int TRANSACTION_onDreamingStopped = 7;
        static final int TRANSACTION_onFinishedGoingToSleep = 9;
        static final int TRANSACTION_onScreenTurnedOff = 13;
        static final int TRANSACTION_onScreenTurnedOn = 12;
        static final int TRANSACTION_onScreenTurningOn = 11;
        static final int TRANSACTION_onStartedGoingToSleep = 8;
        static final int TRANSACTION_onStartedWakingUp = 10;
        static final int TRANSACTION_onSystemReady = 15;
        static final int TRANSACTION_setCurrentUser = 17;
        static final int TRANSACTION_setKeyguardEnabled = 14;
        static final int TRANSACTION_setOccluded = 1;
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

            public void setOccluded(boolean isOccluded) throws RemoteException {
                int i = Stub.TRANSACTION_setOccluded;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!isOccluded) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setOccluded, _data, null, Stub.TRANSACTION_setOccluded);
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
                    this.mRemote.transact(Stub.TRANSACTION_addStateMonitorCallback, _data, null, Stub.TRANSACTION_setOccluded);
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
                    this.mRemote.transact(Stub.TRANSACTION_verifyUnlock, _data, null, Stub.TRANSACTION_setOccluded);
                } finally {
                    _data.recycle();
                }
            }

            public void keyguardDone(boolean authenticated, boolean wakeup) throws RemoteException {
                int i = Stub.TRANSACTION_setOccluded;
                Parcel _data = Parcel.obtain();
                try {
                    int i2;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (authenticated) {
                        i2 = Stub.TRANSACTION_setOccluded;
                    } else {
                        i2 = 0;
                    }
                    _data.writeInt(i2);
                    if (!wakeup) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_keyguardDone, _data, null, Stub.TRANSACTION_setOccluded);
                } finally {
                    _data.recycle();
                }
            }

            public void dismiss() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_dismiss, _data, null, Stub.TRANSACTION_setOccluded);
                } finally {
                    _data.recycle();
                }
            }

            public void onDreamingStarted() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_onDreamingStarted, _data, null, Stub.TRANSACTION_setOccluded);
                } finally {
                    _data.recycle();
                }
            }

            public void onDreamingStopped() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_onDreamingStopped, _data, null, Stub.TRANSACTION_setOccluded);
                } finally {
                    _data.recycle();
                }
            }

            public void onStartedGoingToSleep(int reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(reason);
                    this.mRemote.transact(Stub.TRANSACTION_onStartedGoingToSleep, _data, null, Stub.TRANSACTION_setOccluded);
                } finally {
                    _data.recycle();
                }
            }

            public void onFinishedGoingToSleep(int reason, boolean cameraGestureTriggered) throws RemoteException {
                int i = Stub.TRANSACTION_setOccluded;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(reason);
                    if (!cameraGestureTriggered) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_onFinishedGoingToSleep, _data, null, Stub.TRANSACTION_setOccluded);
                } finally {
                    _data.recycle();
                }
            }

            public void onStartedWakingUp() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_onStartedWakingUp, _data, null, Stub.TRANSACTION_setOccluded);
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
                    this.mRemote.transact(Stub.TRANSACTION_onScreenTurningOn, _data, null, Stub.TRANSACTION_setOccluded);
                } finally {
                    _data.recycle();
                }
            }

            public void onScreenTurnedOn() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_onScreenTurnedOn, _data, null, Stub.TRANSACTION_setOccluded);
                } finally {
                    _data.recycle();
                }
            }

            public void onScreenTurnedOff() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_onScreenTurnedOff, _data, null, Stub.TRANSACTION_setOccluded);
                } finally {
                    _data.recycle();
                }
            }

            public void setKeyguardEnabled(boolean enabled) throws RemoteException {
                int i = Stub.TRANSACTION_setOccluded;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!enabled) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setKeyguardEnabled, _data, null, Stub.TRANSACTION_setOccluded);
                } finally {
                    _data.recycle();
                }
            }

            public void onSystemReady() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_onSystemReady, _data, null, Stub.TRANSACTION_setOccluded);
                } finally {
                    _data.recycle();
                }
            }

            public void doKeyguardTimeout(Bundle options) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (options != null) {
                        _data.writeInt(Stub.TRANSACTION_setOccluded);
                        options.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_doKeyguardTimeout, _data, null, Stub.TRANSACTION_setOccluded);
                } finally {
                    _data.recycle();
                }
            }

            public void setCurrentUser(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_setCurrentUser, _data, null, Stub.TRANSACTION_setOccluded);
                } finally {
                    _data.recycle();
                }
            }

            public void onBootCompleted() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_onBootCompleted, _data, null, Stub.TRANSACTION_setOccluded);
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
                    this.mRemote.transact(Stub.TRANSACTION_startKeyguardExitAnimation, _data, null, Stub.TRANSACTION_setOccluded);
                } finally {
                    _data.recycle();
                }
            }

            public void onActivityDrawn() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_onActivityDrawn, _data, null, Stub.TRANSACTION_setOccluded);
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
                case TRANSACTION_setOccluded /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    setOccluded(data.readInt() != 0);
                    return true;
                case TRANSACTION_addStateMonitorCallback /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    addStateMonitorCallback(com.android.internal.policy.IKeyguardStateCallback.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case TRANSACTION_verifyUnlock /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    verifyUnlock(com.android.internal.policy.IKeyguardExitCallback.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case TRANSACTION_keyguardDone /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    keyguardDone(data.readInt() != 0, data.readInt() != 0);
                    return true;
                case TRANSACTION_dismiss /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    dismiss();
                    return true;
                case TRANSACTION_onDreamingStarted /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    onDreamingStarted();
                    return true;
                case TRANSACTION_onDreamingStopped /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    onDreamingStopped();
                    return true;
                case TRANSACTION_onStartedGoingToSleep /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    onStartedGoingToSleep(data.readInt());
                    return true;
                case TRANSACTION_onFinishedGoingToSleep /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    onFinishedGoingToSleep(data.readInt(), data.readInt() != 0);
                    return true;
                case TRANSACTION_onStartedWakingUp /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    onStartedWakingUp();
                    return true;
                case TRANSACTION_onScreenTurningOn /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    onScreenTurningOn(com.android.internal.policy.IKeyguardDrawnCallback.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case TRANSACTION_onScreenTurnedOn /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    onScreenTurnedOn();
                    return true;
                case TRANSACTION_onScreenTurnedOff /*13*/:
                    data.enforceInterface(DESCRIPTOR);
                    onScreenTurnedOff();
                    return true;
                case TRANSACTION_setKeyguardEnabled /*14*/:
                    data.enforceInterface(DESCRIPTOR);
                    setKeyguardEnabled(data.readInt() != 0);
                    return true;
                case TRANSACTION_onSystemReady /*15*/:
                    data.enforceInterface(DESCRIPTOR);
                    onSystemReady();
                    return true;
                case TRANSACTION_doKeyguardTimeout /*16*/:
                    Bundle bundle;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    doKeyguardTimeout(bundle);
                    return true;
                case TRANSACTION_setCurrentUser /*17*/:
                    data.enforceInterface(DESCRIPTOR);
                    setCurrentUser(data.readInt());
                    return true;
                case TRANSACTION_onBootCompleted /*18*/:
                    data.enforceInterface(DESCRIPTOR);
                    onBootCompleted();
                    return true;
                case TRANSACTION_startKeyguardExitAnimation /*19*/:
                    data.enforceInterface(DESCRIPTOR);
                    startKeyguardExitAnimation(data.readLong(), data.readLong());
                    return true;
                case TRANSACTION_onActivityDrawn /*20*/:
                    data.enforceInterface(DESCRIPTOR);
                    onActivityDrawn();
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

    void dismiss() throws RemoteException;

    void doKeyguardTimeout(Bundle bundle) throws RemoteException;

    void keyguardDone(boolean z, boolean z2) throws RemoteException;

    void onActivityDrawn() throws RemoteException;

    void onBootCompleted() throws RemoteException;

    void onDreamingStarted() throws RemoteException;

    void onDreamingStopped() throws RemoteException;

    void onFinishedGoingToSleep(int i, boolean z) throws RemoteException;

    void onScreenTurnedOff() throws RemoteException;

    void onScreenTurnedOn() throws RemoteException;

    void onScreenTurningOn(IKeyguardDrawnCallback iKeyguardDrawnCallback) throws RemoteException;

    void onStartedGoingToSleep(int i) throws RemoteException;

    void onStartedWakingUp() throws RemoteException;

    void onSystemReady() throws RemoteException;

    void setCurrentUser(int i) throws RemoteException;

    void setKeyguardEnabled(boolean z) throws RemoteException;

    void setOccluded(boolean z) throws RemoteException;

    void startKeyguardExitAnimation(long j, long j2) throws RemoteException;

    void verifyUnlock(IKeyguardExitCallback iKeyguardExitCallback) throws RemoteException;
}
