package android.hardware.location;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IActivityRecognitionHardwareSink extends IInterface {

    public static abstract class Stub extends Binder implements IActivityRecognitionHardwareSink {
        private static final String DESCRIPTOR = "android.hardware.location.IActivityRecognitionHardwareSink";
        static final int TRANSACTION_onActivityChanged = 1;

        private static class Proxy implements IActivityRecognitionHardwareSink {
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

            public void onActivityChanged(ActivityChangedEvent event) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (event != null) {
                        _data.writeInt(Stub.TRANSACTION_onActivityChanged);
                        event.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onActivityChanged, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IActivityRecognitionHardwareSink asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IActivityRecognitionHardwareSink)) {
                return new Proxy(obj);
            }
            return (IActivityRecognitionHardwareSink) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case TRANSACTION_onActivityChanged /*1*/:
                    ActivityChangedEvent activityChangedEvent;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        activityChangedEvent = (ActivityChangedEvent) ActivityChangedEvent.CREATOR.createFromParcel(data);
                    } else {
                        activityChangedEvent = null;
                    }
                    onActivityChanged(activityChangedEvent);
                    reply.writeNoException();
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onActivityChanged(ActivityChangedEvent activityChangedEvent) throws RemoteException;
}
