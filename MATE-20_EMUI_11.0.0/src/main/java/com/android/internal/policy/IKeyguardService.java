package com.android.internal.policy;

import android.annotation.UnsupportedAppUsage;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.text.TextUtils;
import com.android.internal.policy.IKeyguardDismissCallback;
import com.android.internal.policy.IKeyguardDrawnCallback;
import com.android.internal.policy.IKeyguardExitCallback;
import com.android.internal.policy.IKeyguardStateCallback;

public interface IKeyguardService extends IInterface {
    void addStateMonitorCallback(IKeyguardStateCallback iKeyguardStateCallback) throws RemoteException;

    void dismiss(IKeyguardDismissCallback iKeyguardDismissCallback, CharSequence charSequence) throws RemoteException;

    void doFaceRecognize(boolean z, String str) throws RemoteException;

    @UnsupportedAppUsage
    void doKeyguardTimeout(Bundle bundle) throws RemoteException;

    void onBootCompleted() throws RemoteException;

    void onDreamingStarted() throws RemoteException;

    void onDreamingStopped() throws RemoteException;

    void onFinishedGoingToSleep(int i, boolean z) throws RemoteException;

    void onFinishedWakingUp() throws RemoteException;

    void onScreenTurnedOff() throws RemoteException;

    void onScreenTurnedOn() throws RemoteException;

    void onScreenTurningOff() throws RemoteException;

    void onScreenTurningOn(IKeyguardDrawnCallback iKeyguardDrawnCallback) throws RemoteException;

    void onShortPowerPressedGoHome() throws RemoteException;

    void onStartedGoingToSleep(int i) throws RemoteException;

    void onStartedWakingUp() throws RemoteException;

    void onSystemReady() throws RemoteException;

    void setCurrentUser(int i) throws RemoteException;

    @UnsupportedAppUsage
    void setKeyguardEnabled(boolean z) throws RemoteException;

    void setOccluded(boolean z, boolean z2) throws RemoteException;

    void setSwitchingUser(boolean z) throws RemoteException;

    void startKeyguardExitAnimation(long j, long j2) throws RemoteException;

    void verifyUnlock(IKeyguardExitCallback iKeyguardExitCallback) throws RemoteException;

    public static class Default implements IKeyguardService {
        @Override // com.android.internal.policy.IKeyguardService
        public void setOccluded(boolean isOccluded, boolean animate) throws RemoteException {
        }

        @Override // com.android.internal.policy.IKeyguardService
        public void addStateMonitorCallback(IKeyguardStateCallback callback) throws RemoteException {
        }

        @Override // com.android.internal.policy.IKeyguardService
        public void verifyUnlock(IKeyguardExitCallback callback) throws RemoteException {
        }

        @Override // com.android.internal.policy.IKeyguardService
        public void dismiss(IKeyguardDismissCallback callback, CharSequence message) throws RemoteException {
        }

        @Override // com.android.internal.policy.IKeyguardService
        public void onDreamingStarted() throws RemoteException {
        }

        @Override // com.android.internal.policy.IKeyguardService
        public void onDreamingStopped() throws RemoteException {
        }

        @Override // com.android.internal.policy.IKeyguardService
        public void onStartedGoingToSleep(int reason) throws RemoteException {
        }

        @Override // com.android.internal.policy.IKeyguardService
        public void onFinishedGoingToSleep(int reason, boolean cameraGestureTriggered) throws RemoteException {
        }

        @Override // com.android.internal.policy.IKeyguardService
        public void onStartedWakingUp() throws RemoteException {
        }

        @Override // com.android.internal.policy.IKeyguardService
        public void onFinishedWakingUp() throws RemoteException {
        }

        @Override // com.android.internal.policy.IKeyguardService
        public void onScreenTurningOn(IKeyguardDrawnCallback callback) throws RemoteException {
        }

        @Override // com.android.internal.policy.IKeyguardService
        public void onScreenTurnedOn() throws RemoteException {
        }

        @Override // com.android.internal.policy.IKeyguardService
        public void onScreenTurningOff() throws RemoteException {
        }

        @Override // com.android.internal.policy.IKeyguardService
        public void onScreenTurnedOff() throws RemoteException {
        }

        @Override // com.android.internal.policy.IKeyguardService
        public void setKeyguardEnabled(boolean enabled) throws RemoteException {
        }

        @Override // com.android.internal.policy.IKeyguardService
        public void onSystemReady() throws RemoteException {
        }

        @Override // com.android.internal.policy.IKeyguardService
        public void doKeyguardTimeout(Bundle options) throws RemoteException {
        }

        @Override // com.android.internal.policy.IKeyguardService
        public void setSwitchingUser(boolean switching) throws RemoteException {
        }

        @Override // com.android.internal.policy.IKeyguardService
        public void setCurrentUser(int userId) throws RemoteException {
        }

        @Override // com.android.internal.policy.IKeyguardService
        public void onBootCompleted() throws RemoteException {
        }

        @Override // com.android.internal.policy.IKeyguardService
        public void startKeyguardExitAnimation(long startTime, long fadeoutDuration) throws RemoteException {
        }

        @Override // com.android.internal.policy.IKeyguardService
        public void onShortPowerPressedGoHome() throws RemoteException {
        }

        @Override // com.android.internal.policy.IKeyguardService
        public void doFaceRecognize(boolean detect, String reason) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IKeyguardService {
        private static final String DESCRIPTOR = "com.android.internal.policy.IKeyguardService";
        static final int TRANSACTION_addStateMonitorCallback = 2;
        static final int TRANSACTION_dismiss = 4;
        static final int TRANSACTION_doFaceRecognize = 23;
        static final int TRANSACTION_doKeyguardTimeout = 17;
        static final int TRANSACTION_onBootCompleted = 20;
        static final int TRANSACTION_onDreamingStarted = 5;
        static final int TRANSACTION_onDreamingStopped = 6;
        static final int TRANSACTION_onFinishedGoingToSleep = 8;
        static final int TRANSACTION_onFinishedWakingUp = 10;
        static final int TRANSACTION_onScreenTurnedOff = 14;
        static final int TRANSACTION_onScreenTurnedOn = 12;
        static final int TRANSACTION_onScreenTurningOff = 13;
        static final int TRANSACTION_onScreenTurningOn = 11;
        static final int TRANSACTION_onShortPowerPressedGoHome = 22;
        static final int TRANSACTION_onStartedGoingToSleep = 7;
        static final int TRANSACTION_onStartedWakingUp = 9;
        static final int TRANSACTION_onSystemReady = 16;
        static final int TRANSACTION_setCurrentUser = 19;
        static final int TRANSACTION_setKeyguardEnabled = 15;
        static final int TRANSACTION_setOccluded = 1;
        static final int TRANSACTION_setSwitchingUser = 18;
        static final int TRANSACTION_startKeyguardExitAnimation = 21;
        static final int TRANSACTION_verifyUnlock = 3;

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "setOccluded";
                case 2:
                    return "addStateMonitorCallback";
                case 3:
                    return "verifyUnlock";
                case 4:
                    return "dismiss";
                case 5:
                    return "onDreamingStarted";
                case 6:
                    return "onDreamingStopped";
                case 7:
                    return "onStartedGoingToSleep";
                case 8:
                    return "onFinishedGoingToSleep";
                case 9:
                    return "onStartedWakingUp";
                case 10:
                    return "onFinishedWakingUp";
                case 11:
                    return "onScreenTurningOn";
                case 12:
                    return "onScreenTurnedOn";
                case 13:
                    return "onScreenTurningOff";
                case 14:
                    return "onScreenTurnedOff";
                case 15:
                    return "setKeyguardEnabled";
                case 16:
                    return "onSystemReady";
                case 17:
                    return "doKeyguardTimeout";
                case 18:
                    return "setSwitchingUser";
                case 19:
                    return "setCurrentUser";
                case 20:
                    return "onBootCompleted";
                case 21:
                    return "startKeyguardExitAnimation";
                case 22:
                    return "onShortPowerPressedGoHome";
                case 23:
                    return "doFaceRecognize";
                default:
                    return null;
            }
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            CharSequence _arg1;
            Bundle _arg0;
            if (code != 1598968902) {
                boolean _arg02 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _arg03 = data.readInt() != 0;
                        if (data.readInt() != 0) {
                            _arg02 = true;
                        }
                        setOccluded(_arg03, _arg02);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        addStateMonitorCallback(IKeyguardStateCallback.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        verifyUnlock(IKeyguardExitCallback.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        IKeyguardDismissCallback _arg04 = IKeyguardDismissCallback.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg1 = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        dismiss(_arg04, _arg1);
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
                        int _arg05 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg02 = true;
                        }
                        onFinishedGoingToSleep(_arg05, _arg02);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        onStartedWakingUp();
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        onFinishedWakingUp();
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        onScreenTurningOn(IKeyguardDrawnCallback.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        onScreenTurnedOn();
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        onScreenTurningOff();
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        onScreenTurnedOff();
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = true;
                        }
                        setKeyguardEnabled(_arg02);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        onSystemReady();
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        doKeyguardTimeout(_arg0);
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = true;
                        }
                        setSwitchingUser(_arg02);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        setCurrentUser(data.readInt());
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        onBootCompleted();
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        startKeyguardExitAnimation(data.readLong(), data.readLong());
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        onShortPowerPressedGoHome();
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = true;
                        }
                        doFaceRecognize(_arg02, data.readString());
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
        public static class Proxy implements IKeyguardService {
            public static IKeyguardService sDefaultImpl;
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

            @Override // com.android.internal.policy.IKeyguardService
            public void setOccluded(boolean isOccluded, boolean animate) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    int i = 0;
                    _data.writeInt(isOccluded ? 1 : 0);
                    if (animate) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setOccluded(isOccluded, animate);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.policy.IKeyguardService
            public void addStateMonitorCallback(IKeyguardStateCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().addStateMonitorCallback(callback);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.policy.IKeyguardService
            public void verifyUnlock(IKeyguardExitCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().verifyUnlock(callback);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.policy.IKeyguardService
            public void dismiss(IKeyguardDismissCallback callback, CharSequence message) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (message != null) {
                        _data.writeInt(1);
                        TextUtils.writeToParcel(message, _data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().dismiss(callback, message);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.policy.IKeyguardService
            public void onDreamingStarted() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onDreamingStarted();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.policy.IKeyguardService
            public void onDreamingStopped() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(6, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onDreamingStopped();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.policy.IKeyguardService
            public void onStartedGoingToSleep(int reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(reason);
                    if (this.mRemote.transact(7, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onStartedGoingToSleep(reason);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.policy.IKeyguardService
            public void onFinishedGoingToSleep(int reason, boolean cameraGestureTriggered) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(reason);
                    _data.writeInt(cameraGestureTriggered ? 1 : 0);
                    if (this.mRemote.transact(8, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onFinishedGoingToSleep(reason, cameraGestureTriggered);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.policy.IKeyguardService
            public void onStartedWakingUp() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(9, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onStartedWakingUp();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.policy.IKeyguardService
            public void onFinishedWakingUp() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(10, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onFinishedWakingUp();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.policy.IKeyguardService
            public void onScreenTurningOn(IKeyguardDrawnCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(11, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onScreenTurningOn(callback);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.policy.IKeyguardService
            public void onScreenTurnedOn() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(12, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onScreenTurnedOn();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.policy.IKeyguardService
            public void onScreenTurningOff() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(13, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onScreenTurningOff();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.policy.IKeyguardService
            public void onScreenTurnedOff() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(14, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onScreenTurnedOff();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.policy.IKeyguardService
            public void setKeyguardEnabled(boolean enabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enabled ? 1 : 0);
                    if (this.mRemote.transact(15, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setKeyguardEnabled(enabled);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.policy.IKeyguardService
            public void onSystemReady() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(16, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onSystemReady();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.policy.IKeyguardService
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
                    if (this.mRemote.transact(17, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().doKeyguardTimeout(options);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.policy.IKeyguardService
            public void setSwitchingUser(boolean switching) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(switching ? 1 : 0);
                    if (this.mRemote.transact(18, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setSwitchingUser(switching);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.policy.IKeyguardService
            public void setCurrentUser(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(19, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setCurrentUser(userId);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.policy.IKeyguardService
            public void onBootCompleted() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(20, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onBootCompleted();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.policy.IKeyguardService
            public void startKeyguardExitAnimation(long startTime, long fadeoutDuration) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(startTime);
                    _data.writeLong(fadeoutDuration);
                    if (this.mRemote.transact(21, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().startKeyguardExitAnimation(startTime, fadeoutDuration);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.policy.IKeyguardService
            public void onShortPowerPressedGoHome() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(22, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onShortPowerPressedGoHome();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.policy.IKeyguardService
            public void doFaceRecognize(boolean detect, String reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(detect ? 1 : 0);
                    _data.writeString(reason);
                    if (this.mRemote.transact(23, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().doFaceRecognize(detect, reason);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IKeyguardService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IKeyguardService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
