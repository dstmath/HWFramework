package com.android.internal.telecom;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.telecom.CallAudioState;
import android.telecom.ParcelableCall;
import com.android.internal.telecom.IInCallAdapter;

public interface IInCallService extends IInterface {
    void addCall(ParcelableCall parcelableCall) throws RemoteException;

    void bringToForeground(boolean z) throws RemoteException;

    void onCallAudioStateChanged(CallAudioState callAudioState) throws RemoteException;

    void onCanAddCallChanged(boolean z) throws RemoteException;

    void onConnectionEvent(String str, String str2, Bundle bundle) throws RemoteException;

    void onHandoverComplete(String str) throws RemoteException;

    void onHandoverFailed(String str, int i) throws RemoteException;

    void onRttInitiationFailure(String str, int i) throws RemoteException;

    void onRttUpgradeRequest(String str, int i) throws RemoteException;

    void setInCallAdapter(IInCallAdapter iInCallAdapter) throws RemoteException;

    void setPostDial(String str, String str2) throws RemoteException;

    void setPostDialWait(String str, String str2) throws RemoteException;

    void silenceRinger() throws RemoteException;

    void updateCall(ParcelableCall parcelableCall) throws RemoteException;

    public static class Default implements IInCallService {
        @Override // com.android.internal.telecom.IInCallService
        public void setInCallAdapter(IInCallAdapter inCallAdapter) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IInCallService
        public void addCall(ParcelableCall call) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IInCallService
        public void updateCall(ParcelableCall call) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IInCallService
        public void setPostDial(String callId, String remaining) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IInCallService
        public void setPostDialWait(String callId, String remaining) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IInCallService
        public void onCallAudioStateChanged(CallAudioState callAudioState) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IInCallService
        public void bringToForeground(boolean showDialpad) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IInCallService
        public void onCanAddCallChanged(boolean canAddCall) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IInCallService
        public void silenceRinger() throws RemoteException {
        }

        @Override // com.android.internal.telecom.IInCallService
        public void onConnectionEvent(String callId, String event, Bundle extras) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IInCallService
        public void onRttUpgradeRequest(String callId, int id) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IInCallService
        public void onRttInitiationFailure(String callId, int reason) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IInCallService
        public void onHandoverFailed(String callId, int error) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IInCallService
        public void onHandoverComplete(String callId) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IInCallService {
        private static final String DESCRIPTOR = "com.android.internal.telecom.IInCallService";
        static final int TRANSACTION_addCall = 2;
        static final int TRANSACTION_bringToForeground = 7;
        static final int TRANSACTION_onCallAudioStateChanged = 6;
        static final int TRANSACTION_onCanAddCallChanged = 8;
        static final int TRANSACTION_onConnectionEvent = 10;
        static final int TRANSACTION_onHandoverComplete = 14;
        static final int TRANSACTION_onHandoverFailed = 13;
        static final int TRANSACTION_onRttInitiationFailure = 12;
        static final int TRANSACTION_onRttUpgradeRequest = 11;
        static final int TRANSACTION_setInCallAdapter = 1;
        static final int TRANSACTION_setPostDial = 4;
        static final int TRANSACTION_setPostDialWait = 5;
        static final int TRANSACTION_silenceRinger = 9;
        static final int TRANSACTION_updateCall = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IInCallService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IInCallService)) {
                return new Proxy(obj);
            }
            return (IInCallService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "setInCallAdapter";
                case 2:
                    return "addCall";
                case 3:
                    return "updateCall";
                case 4:
                    return "setPostDial";
                case 5:
                    return "setPostDialWait";
                case 6:
                    return "onCallAudioStateChanged";
                case 7:
                    return "bringToForeground";
                case 8:
                    return "onCanAddCallChanged";
                case 9:
                    return "silenceRinger";
                case 10:
                    return "onConnectionEvent";
                case 11:
                    return "onRttUpgradeRequest";
                case 12:
                    return "onRttInitiationFailure";
                case 13:
                    return "onHandoverFailed";
                case 14:
                    return "onHandoverComplete";
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
            ParcelableCall _arg0;
            ParcelableCall _arg02;
            CallAudioState _arg03;
            Bundle _arg2;
            if (code != 1598968902) {
                boolean _arg04 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        setInCallAdapter(IInCallAdapter.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = ParcelableCall.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        addCall(_arg0);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = ParcelableCall.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        updateCall(_arg02);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        setPostDial(data.readString(), data.readString());
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        setPostDialWait(data.readString(), data.readString());
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = CallAudioState.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        onCallAudioStateChanged(_arg03);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = true;
                        }
                        bringToForeground(_arg04);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = true;
                        }
                        onCanAddCallChanged(_arg04);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        silenceRinger();
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg05 = data.readString();
                        String _arg1 = data.readString();
                        if (data.readInt() != 0) {
                            _arg2 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        onConnectionEvent(_arg05, _arg1, _arg2);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        onRttUpgradeRequest(data.readString(), data.readInt());
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        onRttInitiationFailure(data.readString(), data.readInt());
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        onHandoverFailed(data.readString(), data.readInt());
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        onHandoverComplete(data.readString());
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
        public static class Proxy implements IInCallService {
            public static IInCallService sDefaultImpl;
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

            @Override // com.android.internal.telecom.IInCallService
            public void setInCallAdapter(IInCallAdapter inCallAdapter) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(inCallAdapter != null ? inCallAdapter.asBinder() : null);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setInCallAdapter(inCallAdapter);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IInCallService
            public void addCall(ParcelableCall call) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (call != null) {
                        _data.writeInt(1);
                        call.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().addCall(call);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IInCallService
            public void updateCall(ParcelableCall call) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (call != null) {
                        _data.writeInt(1);
                        call.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().updateCall(call);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IInCallService
            public void setPostDial(String callId, String remaining) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeString(remaining);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setPostDial(callId, remaining);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IInCallService
            public void setPostDialWait(String callId, String remaining) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeString(remaining);
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setPostDialWait(callId, remaining);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IInCallService
            public void onCallAudioStateChanged(CallAudioState callAudioState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callAudioState != null) {
                        _data.writeInt(1);
                        callAudioState.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(6, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onCallAudioStateChanged(callAudioState);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IInCallService
            public void bringToForeground(boolean showDialpad) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(showDialpad ? 1 : 0);
                    if (this.mRemote.transact(7, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().bringToForeground(showDialpad);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IInCallService
            public void onCanAddCallChanged(boolean canAddCall) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(canAddCall ? 1 : 0);
                    if (this.mRemote.transact(8, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onCanAddCallChanged(canAddCall);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IInCallService
            public void silenceRinger() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(9, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().silenceRinger();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IInCallService
            public void onConnectionEvent(String callId, String event, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeString(event);
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(10, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onConnectionEvent(callId, event, extras);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IInCallService
            public void onRttUpgradeRequest(String callId, int id) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeInt(id);
                    if (this.mRemote.transact(11, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onRttUpgradeRequest(callId, id);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IInCallService
            public void onRttInitiationFailure(String callId, int reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeInt(reason);
                    if (this.mRemote.transact(12, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onRttInitiationFailure(callId, reason);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IInCallService
            public void onHandoverFailed(String callId, int error) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeInt(error);
                    if (this.mRemote.transact(13, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onHandoverFailed(callId, error);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IInCallService
            public void onHandoverComplete(String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    if (this.mRemote.transact(14, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onHandoverComplete(callId);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IInCallService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IInCallService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
