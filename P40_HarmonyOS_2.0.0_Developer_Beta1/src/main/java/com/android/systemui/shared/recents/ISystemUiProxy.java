package com.android.systemui.shared.recents;

import android.graphics.Rect;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.MotionEvent;

public interface ISystemUiProxy extends IInterface {
    Rect getNonMinimizedSplitScreenSecondaryBounds() throws RemoteException;

    Bundle monitorGestureInput(String str, int i) throws RemoteException;

    void notifyAccessibilityButtonClicked(int i) throws RemoteException;

    void notifyAccessibilityButtonLongClicked() throws RemoteException;

    void onAssistantGestureCompletion(float f) throws RemoteException;

    void onAssistantProgress(float f) throws RemoteException;

    void onOverviewShown(boolean z) throws RemoteException;

    void onSplitScreenInvoked() throws RemoteException;

    void onStatusBarMotionEvent(MotionEvent motionEvent) throws RemoteException;

    void setBackButtonAlpha(float f, boolean z) throws RemoteException;

    void setInteractionState(int i) throws RemoteException;

    void startAssistant(Bundle bundle) throws RemoteException;

    void startScreenPinning(int i) throws RemoteException;

    void stopScreenPinning() throws RemoteException;

    void updateFullScreenButton(boolean z) throws RemoteException;

    public static class Default implements ISystemUiProxy {
        @Override // com.android.systemui.shared.recents.ISystemUiProxy
        public void startScreenPinning(int taskId) throws RemoteException {
        }

        @Override // com.android.systemui.shared.recents.ISystemUiProxy
        public void onSplitScreenInvoked() throws RemoteException {
        }

        @Override // com.android.systemui.shared.recents.ISystemUiProxy
        public void onOverviewShown(boolean fromHome) throws RemoteException {
        }

        @Override // com.android.systemui.shared.recents.ISystemUiProxy
        public Rect getNonMinimizedSplitScreenSecondaryBounds() throws RemoteException {
            return null;
        }

        @Override // com.android.systemui.shared.recents.ISystemUiProxy
        public void setBackButtonAlpha(float alpha, boolean animate) throws RemoteException {
        }

        @Override // com.android.systemui.shared.recents.ISystemUiProxy
        public void onStatusBarMotionEvent(MotionEvent event) throws RemoteException {
        }

        @Override // com.android.systemui.shared.recents.ISystemUiProxy
        public void onAssistantProgress(float progress) throws RemoteException {
        }

        @Override // com.android.systemui.shared.recents.ISystemUiProxy
        public void onAssistantGestureCompletion(float velocity) throws RemoteException {
        }

        @Override // com.android.systemui.shared.recents.ISystemUiProxy
        public void startAssistant(Bundle bundle) throws RemoteException {
        }

        @Override // com.android.systemui.shared.recents.ISystemUiProxy
        public Bundle monitorGestureInput(String name, int displayId) throws RemoteException {
            return null;
        }

        @Override // com.android.systemui.shared.recents.ISystemUiProxy
        public void notifyAccessibilityButtonClicked(int displayId) throws RemoteException {
        }

        @Override // com.android.systemui.shared.recents.ISystemUiProxy
        public void notifyAccessibilityButtonLongClicked() throws RemoteException {
        }

        @Override // com.android.systemui.shared.recents.ISystemUiProxy
        public void stopScreenPinning() throws RemoteException {
        }

        @Override // com.android.systemui.shared.recents.ISystemUiProxy
        public void updateFullScreenButton(boolean show) throws RemoteException {
        }

        @Override // com.android.systemui.shared.recents.ISystemUiProxy
        public void setInteractionState(int flags) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ISystemUiProxy {
        private static final String DESCRIPTOR = "com.android.systemui.shared.recents.ISystemUiProxy";
        static final int TRANSACTION_getNonMinimizedSplitScreenSecondaryBounds = 8;
        static final int TRANSACTION_monitorGestureInput = 15;
        static final int TRANSACTION_notifyAccessibilityButtonClicked = 16;
        static final int TRANSACTION_notifyAccessibilityButtonLongClicked = 17;
        static final int TRANSACTION_onAssistantGestureCompletion = 19;
        static final int TRANSACTION_onAssistantProgress = 13;
        static final int TRANSACTION_onOverviewShown = 7;
        static final int TRANSACTION_onSplitScreenInvoked = 6;
        static final int TRANSACTION_onStatusBarMotionEvent = 10;
        static final int TRANSACTION_setBackButtonAlpha = 9;
        static final int TRANSACTION_setInteractionState = 102;
        static final int TRANSACTION_startAssistant = 14;
        static final int TRANSACTION_startScreenPinning = 2;
        static final int TRANSACTION_stopScreenPinning = 18;
        static final int TRANSACTION_updateFullScreenButton = 101;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ISystemUiProxy asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ISystemUiProxy)) {
                return new Proxy(obj);
            }
            return (ISystemUiProxy) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            MotionEvent _arg0;
            Bundle _arg02;
            if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                startScreenPinning(data.readInt());
                reply.writeNoException();
                return true;
            } else if (code != 1598968902) {
                boolean _arg1 = false;
                if (code == 101) {
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg1 = true;
                    }
                    updateFullScreenButton(_arg1);
                    reply.writeNoException();
                    return true;
                } else if (code != 102) {
                    switch (code) {
                        case 6:
                            data.enforceInterface(DESCRIPTOR);
                            onSplitScreenInvoked();
                            reply.writeNoException();
                            return true;
                        case 7:
                            data.enforceInterface(DESCRIPTOR);
                            if (data.readInt() != 0) {
                                _arg1 = true;
                            }
                            onOverviewShown(_arg1);
                            reply.writeNoException();
                            return true;
                        case 8:
                            data.enforceInterface(DESCRIPTOR);
                            Rect _result = getNonMinimizedSplitScreenSecondaryBounds();
                            reply.writeNoException();
                            if (_result != null) {
                                reply.writeInt(1);
                                _result.writeToParcel(reply, 1);
                            } else {
                                reply.writeInt(0);
                            }
                            return true;
                        case 9:
                            data.enforceInterface(DESCRIPTOR);
                            float _arg03 = data.readFloat();
                            if (data.readInt() != 0) {
                                _arg1 = true;
                            }
                            setBackButtonAlpha(_arg03, _arg1);
                            reply.writeNoException();
                            return true;
                        case 10:
                            data.enforceInterface(DESCRIPTOR);
                            if (data.readInt() != 0) {
                                _arg0 = (MotionEvent) MotionEvent.CREATOR.createFromParcel(data);
                            } else {
                                _arg0 = null;
                            }
                            onStatusBarMotionEvent(_arg0);
                            reply.writeNoException();
                            return true;
                        default:
                            switch (code) {
                                case 13:
                                    data.enforceInterface(DESCRIPTOR);
                                    onAssistantProgress(data.readFloat());
                                    reply.writeNoException();
                                    return true;
                                case 14:
                                    data.enforceInterface(DESCRIPTOR);
                                    if (data.readInt() != 0) {
                                        _arg02 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                                    } else {
                                        _arg02 = null;
                                    }
                                    startAssistant(_arg02);
                                    reply.writeNoException();
                                    return true;
                                case 15:
                                    data.enforceInterface(DESCRIPTOR);
                                    Bundle _result2 = monitorGestureInput(data.readString(), data.readInt());
                                    reply.writeNoException();
                                    if (_result2 != null) {
                                        reply.writeInt(1);
                                        _result2.writeToParcel(reply, 1);
                                    } else {
                                        reply.writeInt(0);
                                    }
                                    return true;
                                case 16:
                                    data.enforceInterface(DESCRIPTOR);
                                    notifyAccessibilityButtonClicked(data.readInt());
                                    reply.writeNoException();
                                    return true;
                                case 17:
                                    data.enforceInterface(DESCRIPTOR);
                                    notifyAccessibilityButtonLongClicked();
                                    reply.writeNoException();
                                    return true;
                                case 18:
                                    data.enforceInterface(DESCRIPTOR);
                                    stopScreenPinning();
                                    reply.writeNoException();
                                    return true;
                                case 19:
                                    data.enforceInterface(DESCRIPTOR);
                                    onAssistantGestureCompletion(data.readFloat());
                                    reply.writeNoException();
                                    return true;
                                default:
                                    return super.onTransact(code, data, reply, flags);
                            }
                    }
                } else {
                    data.enforceInterface(DESCRIPTOR);
                    setInteractionState(data.readInt());
                    reply.writeNoException();
                    return true;
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements ISystemUiProxy {
            public static ISystemUiProxy sDefaultImpl;
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

            @Override // com.android.systemui.shared.recents.ISystemUiProxy
            public void startScreenPinning(int taskId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().startScreenPinning(taskId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.systemui.shared.recents.ISystemUiProxy
            public void onSplitScreenInvoked() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onSplitScreenInvoked();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.systemui.shared.recents.ISystemUiProxy
            public void onOverviewShown(boolean fromHome) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(fromHome ? 1 : 0);
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onOverviewShown(fromHome);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.systemui.shared.recents.ISystemUiProxy
            public Rect getNonMinimizedSplitScreenSecondaryBounds() throws RemoteException {
                Rect _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNonMinimizedSplitScreenSecondaryBounds();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (Rect) Rect.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.systemui.shared.recents.ISystemUiProxy
            public void setBackButtonAlpha(float alpha, boolean animate) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeFloat(alpha);
                    _data.writeInt(animate ? 1 : 0);
                    if (this.mRemote.transact(9, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setBackButtonAlpha(alpha, animate);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.systemui.shared.recents.ISystemUiProxy
            public void onStatusBarMotionEvent(MotionEvent event) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (event != null) {
                        _data.writeInt(1);
                        event.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(10, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onStatusBarMotionEvent(event);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.systemui.shared.recents.ISystemUiProxy
            public void onAssistantProgress(float progress) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeFloat(progress);
                    if (this.mRemote.transact(13, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onAssistantProgress(progress);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.systemui.shared.recents.ISystemUiProxy
            public void onAssistantGestureCompletion(float velocity) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeFloat(velocity);
                    if (this.mRemote.transact(19, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onAssistantGestureCompletion(velocity);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.systemui.shared.recents.ISystemUiProxy
            public void startAssistant(Bundle bundle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(14, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().startAssistant(bundle);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.systemui.shared.recents.ISystemUiProxy
            public Bundle monitorGestureInput(String name, int displayId) throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeInt(displayId);
                    if (!this.mRemote.transact(15, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().monitorGestureInput(name, displayId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (Bundle) Bundle.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.systemui.shared.recents.ISystemUiProxy
            public void notifyAccessibilityButtonClicked(int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    if (this.mRemote.transact(16, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().notifyAccessibilityButtonClicked(displayId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.systemui.shared.recents.ISystemUiProxy
            public void notifyAccessibilityButtonLongClicked() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(17, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().notifyAccessibilityButtonLongClicked();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.systemui.shared.recents.ISystemUiProxy
            public void stopScreenPinning() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(18, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().stopScreenPinning();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.systemui.shared.recents.ISystemUiProxy
            public void updateFullScreenButton(boolean show) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(show ? 1 : 0);
                    if (this.mRemote.transact(101, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().updateFullScreenButton(show);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.systemui.shared.recents.ISystemUiProxy
            public void setInteractionState(int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(flags);
                    if (this.mRemote.transact(102, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setInteractionState(flags);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ISystemUiProxy impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ISystemUiProxy getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
