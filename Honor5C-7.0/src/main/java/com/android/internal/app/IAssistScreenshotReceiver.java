package com.android.internal.app;

import android.graphics.Bitmap;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IAssistScreenshotReceiver extends IInterface {

    public static abstract class Stub extends Binder implements IAssistScreenshotReceiver {
        private static final String DESCRIPTOR = "com.android.internal.app.IAssistScreenshotReceiver";
        static final int TRANSACTION_send = 1;

        private static class Proxy implements IAssistScreenshotReceiver {
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

            public void send(Bitmap screenshot) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (screenshot != null) {
                        _data.writeInt(Stub.TRANSACTION_send);
                        screenshot.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_send, _data, null, Stub.TRANSACTION_send);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IAssistScreenshotReceiver asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IAssistScreenshotReceiver)) {
                return new Proxy(obj);
            }
            return (IAssistScreenshotReceiver) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case TRANSACTION_send /*1*/:
                    Bitmap bitmap;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        bitmap = (Bitmap) Bitmap.CREATOR.createFromParcel(data);
                    } else {
                        bitmap = null;
                    }
                    send(bitmap);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void send(Bitmap bitmap) throws RemoteException;
}
