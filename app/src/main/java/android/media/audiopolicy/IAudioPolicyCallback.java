package android.media.audiopolicy;

import android.media.AudioFocusInfo;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IAudioPolicyCallback extends IInterface {

    public static abstract class Stub extends Binder implements IAudioPolicyCallback {
        private static final String DESCRIPTOR = "android.media.audiopolicy.IAudioPolicyCallback";
        static final int TRANSACTION_notifyAudioFocusGrant = 1;
        static final int TRANSACTION_notifyAudioFocusLoss = 2;
        static final int TRANSACTION_notifyMixStateUpdate = 3;

        private static class Proxy implements IAudioPolicyCallback {
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

            public void notifyAudioFocusGrant(AudioFocusInfo afi, int requestResult) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (afi != null) {
                        _data.writeInt(Stub.TRANSACTION_notifyAudioFocusGrant);
                        afi.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(requestResult);
                    this.mRemote.transact(Stub.TRANSACTION_notifyAudioFocusGrant, _data, null, Stub.TRANSACTION_notifyAudioFocusGrant);
                } finally {
                    _data.recycle();
                }
            }

            public void notifyAudioFocusLoss(AudioFocusInfo afi, boolean wasNotified) throws RemoteException {
                int i = Stub.TRANSACTION_notifyAudioFocusGrant;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (afi != null) {
                        _data.writeInt(Stub.TRANSACTION_notifyAudioFocusGrant);
                        afi.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!wasNotified) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_notifyAudioFocusLoss, _data, null, Stub.TRANSACTION_notifyAudioFocusGrant);
                } finally {
                    _data.recycle();
                }
            }

            public void notifyMixStateUpdate(String regId, int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(regId);
                    _data.writeInt(state);
                    this.mRemote.transact(Stub.TRANSACTION_notifyMixStateUpdate, _data, null, Stub.TRANSACTION_notifyAudioFocusGrant);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IAudioPolicyCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IAudioPolicyCallback)) {
                return new Proxy(obj);
            }
            return (IAudioPolicyCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            boolean _arg1 = false;
            AudioFocusInfo audioFocusInfo;
            switch (code) {
                case TRANSACTION_notifyAudioFocusGrant /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        audioFocusInfo = (AudioFocusInfo) AudioFocusInfo.CREATOR.createFromParcel(data);
                    } else {
                        audioFocusInfo = null;
                    }
                    notifyAudioFocusGrant(audioFocusInfo, data.readInt());
                    return true;
                case TRANSACTION_notifyAudioFocusLoss /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        audioFocusInfo = (AudioFocusInfo) AudioFocusInfo.CREATOR.createFromParcel(data);
                    } else {
                        audioFocusInfo = null;
                    }
                    if (data.readInt() != 0) {
                        _arg1 = true;
                    }
                    notifyAudioFocusLoss(audioFocusInfo, _arg1);
                    return true;
                case TRANSACTION_notifyMixStateUpdate /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    notifyMixStateUpdate(data.readString(), data.readInt());
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void notifyAudioFocusGrant(AudioFocusInfo audioFocusInfo, int i) throws RemoteException;

    void notifyAudioFocusLoss(AudioFocusInfo audioFocusInfo, boolean z) throws RemoteException;

    void notifyMixStateUpdate(String str, int i) throws RemoteException;
}
