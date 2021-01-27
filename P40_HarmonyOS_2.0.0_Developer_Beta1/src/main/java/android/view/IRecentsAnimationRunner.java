package android.view;

import android.annotation.UnsupportedAppUsage;
import android.graphics.Rect;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.IRecentsAnimationController;

public interface IRecentsAnimationRunner extends IInterface {
    @UnsupportedAppUsage
    void onAnimationCanceled(boolean z) throws RemoteException;

    @UnsupportedAppUsage
    void onAnimationStart(IRecentsAnimationController iRecentsAnimationController, RemoteAnimationTarget[] remoteAnimationTargetArr, Rect rect, Rect rect2) throws RemoteException;

    public static class Default implements IRecentsAnimationRunner {
        @Override // android.view.IRecentsAnimationRunner
        public void onAnimationCanceled(boolean deferredWithScreenshot) throws RemoteException {
        }

        @Override // android.view.IRecentsAnimationRunner
        public void onAnimationStart(IRecentsAnimationController controller, RemoteAnimationTarget[] apps, Rect homeContentInsets, Rect minimizedHomeBounds) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IRecentsAnimationRunner {
        private static final String DESCRIPTOR = "android.view.IRecentsAnimationRunner";
        static final int TRANSACTION_onAnimationCanceled = 2;
        static final int TRANSACTION_onAnimationStart = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IRecentsAnimationRunner asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IRecentsAnimationRunner)) {
                return new Proxy(obj);
            }
            return (IRecentsAnimationRunner) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 2) {
                return "onAnimationCanceled";
            }
            if (transactionCode != 3) {
                return null;
            }
            return "onAnimationStart";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Rect _arg2;
            Rect _arg3;
            if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                onAnimationCanceled(data.readInt() != 0);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                IRecentsAnimationController _arg0 = IRecentsAnimationController.Stub.asInterface(data.readStrongBinder());
                RemoteAnimationTarget[] _arg1 = (RemoteAnimationTarget[]) data.createTypedArray(RemoteAnimationTarget.CREATOR);
                if (data.readInt() != 0) {
                    _arg2 = Rect.CREATOR.createFromParcel(data);
                } else {
                    _arg2 = null;
                }
                if (data.readInt() != 0) {
                    _arg3 = Rect.CREATOR.createFromParcel(data);
                } else {
                    _arg3 = null;
                }
                onAnimationStart(_arg0, _arg1, _arg2, _arg3);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IRecentsAnimationRunner {
            public static IRecentsAnimationRunner sDefaultImpl;
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

            @Override // android.view.IRecentsAnimationRunner
            public void onAnimationCanceled(boolean deferredWithScreenshot) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(deferredWithScreenshot ? 1 : 0);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onAnimationCanceled(deferredWithScreenshot);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.view.IRecentsAnimationRunner
            public void onAnimationStart(IRecentsAnimationController controller, RemoteAnimationTarget[] apps, Rect homeContentInsets, Rect minimizedHomeBounds) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(controller != null ? controller.asBinder() : null);
                    _data.writeTypedArray(apps, 0);
                    if (homeContentInsets != null) {
                        _data.writeInt(1);
                        homeContentInsets.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (minimizedHomeBounds != null) {
                        _data.writeInt(1);
                        minimizedHomeBounds.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onAnimationStart(controller, apps, homeContentInsets, minimizedHomeBounds);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IRecentsAnimationRunner impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IRecentsAnimationRunner getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
