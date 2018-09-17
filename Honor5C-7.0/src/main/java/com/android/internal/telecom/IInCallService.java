package com.android.internal.telecom;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.telecom.CallAudioState;
import android.telecom.ParcelableCall;

public interface IInCallService extends IInterface {

    public static abstract class Stub extends Binder implements IInCallService {
        private static final String DESCRIPTOR = "com.android.internal.telecom.IInCallService";
        static final int TRANSACTION_addCall = 2;
        static final int TRANSACTION_bringToForeground = 7;
        static final int TRANSACTION_onCallAudioStateChanged = 6;
        static final int TRANSACTION_onCanAddCallChanged = 8;
        static final int TRANSACTION_onConnectionEvent = 10;
        static final int TRANSACTION_setInCallAdapter = 1;
        static final int TRANSACTION_setPostDial = 4;
        static final int TRANSACTION_setPostDialWait = 5;
        static final int TRANSACTION_silenceRinger = 9;
        static final int TRANSACTION_updateCall = 3;

        private static class Proxy implements IInCallService {
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

            public void setInCallAdapter(IInCallAdapter inCallAdapter) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (inCallAdapter != null) {
                        iBinder = inCallAdapter.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_setInCallAdapter, _data, null, Stub.TRANSACTION_setInCallAdapter);
                } finally {
                    _data.recycle();
                }
            }

            public void addCall(ParcelableCall call) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (call != null) {
                        _data.writeInt(Stub.TRANSACTION_setInCallAdapter);
                        call.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_addCall, _data, null, Stub.TRANSACTION_setInCallAdapter);
                } finally {
                    _data.recycle();
                }
            }

            public void updateCall(ParcelableCall call) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (call != null) {
                        _data.writeInt(Stub.TRANSACTION_setInCallAdapter);
                        call.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_updateCall, _data, null, Stub.TRANSACTION_setInCallAdapter);
                } finally {
                    _data.recycle();
                }
            }

            public void setPostDial(String callId, String remaining) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeString(remaining);
                    this.mRemote.transact(Stub.TRANSACTION_setPostDial, _data, null, Stub.TRANSACTION_setInCallAdapter);
                } finally {
                    _data.recycle();
                }
            }

            public void setPostDialWait(String callId, String remaining) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeString(remaining);
                    this.mRemote.transact(Stub.TRANSACTION_setPostDialWait, _data, null, Stub.TRANSACTION_setInCallAdapter);
                } finally {
                    _data.recycle();
                }
            }

            public void onCallAudioStateChanged(CallAudioState callAudioState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callAudioState != null) {
                        _data.writeInt(Stub.TRANSACTION_setInCallAdapter);
                        callAudioState.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onCallAudioStateChanged, _data, null, Stub.TRANSACTION_setInCallAdapter);
                } finally {
                    _data.recycle();
                }
            }

            public void bringToForeground(boolean showDialpad) throws RemoteException {
                int i = Stub.TRANSACTION_setInCallAdapter;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!showDialpad) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_bringToForeground, _data, null, Stub.TRANSACTION_setInCallAdapter);
                } finally {
                    _data.recycle();
                }
            }

            public void onCanAddCallChanged(boolean canAddCall) throws RemoteException {
                int i = Stub.TRANSACTION_setInCallAdapter;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!canAddCall) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_onCanAddCallChanged, _data, null, Stub.TRANSACTION_setInCallAdapter);
                } finally {
                    _data.recycle();
                }
            }

            public void silenceRinger() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_silenceRinger, _data, null, Stub.TRANSACTION_setInCallAdapter);
                } finally {
                    _data.recycle();
                }
            }

            public void onConnectionEvent(String callId, String event, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeString(event);
                    if (extras != null) {
                        _data.writeInt(Stub.TRANSACTION_setInCallAdapter);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onConnectionEvent, _data, null, Stub.TRANSACTION_setInCallAdapter);
                } finally {
                    _data.recycle();
                }
            }
        }

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

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            boolean _arg0 = false;
            ParcelableCall parcelableCall;
            switch (code) {
                case TRANSACTION_setInCallAdapter /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    setInCallAdapter(com.android.internal.telecom.IInCallAdapter.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case TRANSACTION_addCall /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        parcelableCall = (ParcelableCall) ParcelableCall.CREATOR.createFromParcel(data);
                    } else {
                        parcelableCall = null;
                    }
                    addCall(parcelableCall);
                    return true;
                case TRANSACTION_updateCall /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        parcelableCall = (ParcelableCall) ParcelableCall.CREATOR.createFromParcel(data);
                    } else {
                        parcelableCall = null;
                    }
                    updateCall(parcelableCall);
                    return true;
                case TRANSACTION_setPostDial /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    setPostDial(data.readString(), data.readString());
                    return true;
                case TRANSACTION_setPostDialWait /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    setPostDialWait(data.readString(), data.readString());
                    return true;
                case TRANSACTION_onCallAudioStateChanged /*6*/:
                    CallAudioState callAudioState;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        callAudioState = (CallAudioState) CallAudioState.CREATOR.createFromParcel(data);
                    } else {
                        callAudioState = null;
                    }
                    onCallAudioStateChanged(callAudioState);
                    return true;
                case TRANSACTION_bringToForeground /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = true;
                    }
                    bringToForeground(_arg0);
                    return true;
                case TRANSACTION_onCanAddCallChanged /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = true;
                    }
                    onCanAddCallChanged(_arg0);
                    return true;
                case TRANSACTION_silenceRinger /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    silenceRinger();
                    return true;
                case TRANSACTION_onConnectionEvent /*10*/:
                    Bundle bundle;
                    data.enforceInterface(DESCRIPTOR);
                    String _arg02 = data.readString();
                    String _arg1 = data.readString();
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    onConnectionEvent(_arg02, _arg1, bundle);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void addCall(ParcelableCall parcelableCall) throws RemoteException;

    void bringToForeground(boolean z) throws RemoteException;

    void onCallAudioStateChanged(CallAudioState callAudioState) throws RemoteException;

    void onCanAddCallChanged(boolean z) throws RemoteException;

    void onConnectionEvent(String str, String str2, Bundle bundle) throws RemoteException;

    void setInCallAdapter(IInCallAdapter iInCallAdapter) throws RemoteException;

    void setPostDial(String str, String str2) throws RemoteException;

    void setPostDialWait(String str, String str2) throws RemoteException;

    void silenceRinger() throws RemoteException;

    void updateCall(ParcelableCall parcelableCall) throws RemoteException;
}
