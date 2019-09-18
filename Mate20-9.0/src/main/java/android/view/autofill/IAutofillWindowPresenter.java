package android.view.autofill;

import android.graphics.Rect;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.WindowManager;

public interface IAutofillWindowPresenter extends IInterface {

    public static abstract class Stub extends Binder implements IAutofillWindowPresenter {
        private static final String DESCRIPTOR = "android.view.autofill.IAutofillWindowPresenter";
        static final int TRANSACTION_hide = 2;
        static final int TRANSACTION_show = 1;

        private static class Proxy implements IAutofillWindowPresenter {
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

            public void show(WindowManager.LayoutParams p, Rect transitionEpicenter, boolean fitsSystemWindows, int layoutDirection) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (p != null) {
                        _data.writeInt(1);
                        p.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (transitionEpicenter != null) {
                        _data.writeInt(1);
                        transitionEpicenter.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(fitsSystemWindows);
                    _data.writeInt(layoutDirection);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void hide(Rect transitionEpicenter) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (transitionEpicenter != null) {
                        _data.writeInt(1);
                        transitionEpicenter.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IAutofillWindowPresenter asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IAutofillWindowPresenter)) {
                return new Proxy(obj);
            }
            return (IAutofillWindowPresenter) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            WindowManager.LayoutParams _arg0;
            if (code != 1598968902) {
                Rect _arg02 = null;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = WindowManager.LayoutParams.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg02 = (Rect) Rect.CREATOR.createFromParcel(data);
                        }
                        show(_arg0, _arg02, data.readInt() != 0, data.readInt());
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = (Rect) Rect.CREATOR.createFromParcel(data);
                        }
                        hide(_arg02);
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

    void hide(Rect rect) throws RemoteException;

    void show(WindowManager.LayoutParams layoutParams, Rect rect, boolean z, int i) throws RemoteException;
}
