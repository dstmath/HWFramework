package com.android.systemui.shared.recents;

import android.graphics.Region;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.MotionEvent;
import com.android.systemui.shared.recents.ISystemUiProxy;

public interface IOverviewProxy extends IInterface {
    void onActiveNavBarRegionChanges(Region region) throws RemoteException;

    void onAssistantAvailable(boolean z) throws RemoteException;

    void onAssistantVisibilityChanged(float f) throws RemoteException;

    void onBackAction(boolean z, int i, int i2, boolean z2, boolean z3) throws RemoteException;

    void onBind(ISystemUiProxy iSystemUiProxy) throws RemoteException;

    void onInitialize(Bundle bundle) throws RemoteException;

    void onMotionEvent(MotionEvent motionEvent) throws RemoteException;

    void onOverviewHidden(boolean z, boolean z2) throws RemoteException;

    void onOverviewShown(boolean z) throws RemoteException;

    void onOverviewToggle() throws RemoteException;

    void onPreMotionEvent(int i) throws RemoteException;

    void onQuickScrubEnd() throws RemoteException;

    void onQuickScrubProgress(float f) throws RemoteException;

    void onQuickScrubStart() throws RemoteException;

    void onQuickStep(MotionEvent motionEvent) throws RemoteException;

    void onSystemUiStateChanged(int i) throws RemoteException;

    void onTip(int i, int i2) throws RemoteException;

    public static class Default implements IOverviewProxy {
        @Override // com.android.systemui.shared.recents.IOverviewProxy
        public void onActiveNavBarRegionChanges(Region activeRegion) throws RemoteException {
        }

        @Override // com.android.systemui.shared.recents.IOverviewProxy
        public void onInitialize(Bundle params) throws RemoteException {
        }

        @Override // com.android.systemui.shared.recents.IOverviewProxy
        public void onBind(ISystemUiProxy sysUiProxy) throws RemoteException {
        }

        @Override // com.android.systemui.shared.recents.IOverviewProxy
        public void onPreMotionEvent(int downHitTarget) throws RemoteException {
        }

        @Override // com.android.systemui.shared.recents.IOverviewProxy
        public void onMotionEvent(MotionEvent event) throws RemoteException {
        }

        @Override // com.android.systemui.shared.recents.IOverviewProxy
        public void onQuickScrubStart() throws RemoteException {
        }

        @Override // com.android.systemui.shared.recents.IOverviewProxy
        public void onQuickScrubEnd() throws RemoteException {
        }

        @Override // com.android.systemui.shared.recents.IOverviewProxy
        public void onQuickScrubProgress(float progress) throws RemoteException {
        }

        @Override // com.android.systemui.shared.recents.IOverviewProxy
        public void onOverviewToggle() throws RemoteException {
        }

        @Override // com.android.systemui.shared.recents.IOverviewProxy
        public void onOverviewShown(boolean triggeredFromAltTab) throws RemoteException {
        }

        @Override // com.android.systemui.shared.recents.IOverviewProxy
        public void onOverviewHidden(boolean triggeredFromAltTab, boolean triggeredFromHomeKey) throws RemoteException {
        }

        @Override // com.android.systemui.shared.recents.IOverviewProxy
        public void onQuickStep(MotionEvent event) throws RemoteException {
        }

        @Override // com.android.systemui.shared.recents.IOverviewProxy
        public void onTip(int actionType, int viewType) throws RemoteException {
        }

        @Override // com.android.systemui.shared.recents.IOverviewProxy
        public void onAssistantAvailable(boolean available) throws RemoteException {
        }

        @Override // com.android.systemui.shared.recents.IOverviewProxy
        public void onAssistantVisibilityChanged(float visibility) throws RemoteException {
        }

        @Override // com.android.systemui.shared.recents.IOverviewProxy
        public void onBackAction(boolean completed, int downX, int downY, boolean isButton, boolean gestureSwipeLeft) throws RemoteException {
        }

        @Override // com.android.systemui.shared.recents.IOverviewProxy
        public void onSystemUiStateChanged(int stateFlags) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IOverviewProxy {
        private static final String DESCRIPTOR = "com.android.systemui.shared.recents.IOverviewProxy";
        static final int TRANSACTION_onActiveNavBarRegionChanges = 12;
        static final int TRANSACTION_onAssistantAvailable = 14;
        static final int TRANSACTION_onAssistantVisibilityChanged = 15;
        static final int TRANSACTION_onBackAction = 16;
        static final int TRANSACTION_onBind = 1;
        static final int TRANSACTION_onInitialize = 13;
        static final int TRANSACTION_onMotionEvent = 3;
        static final int TRANSACTION_onOverviewHidden = 9;
        static final int TRANSACTION_onOverviewShown = 8;
        static final int TRANSACTION_onOverviewToggle = 7;
        static final int TRANSACTION_onPreMotionEvent = 2;
        static final int TRANSACTION_onQuickScrubEnd = 5;
        static final int TRANSACTION_onQuickScrubProgress = 6;
        static final int TRANSACTION_onQuickScrubStart = 4;
        static final int TRANSACTION_onQuickStep = 10;
        static final int TRANSACTION_onSystemUiStateChanged = 17;
        static final int TRANSACTION_onTip = 11;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IOverviewProxy asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IOverviewProxy)) {
                return new Proxy(obj);
            }
            return (IOverviewProxy) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            MotionEvent _arg0;
            MotionEvent _arg02;
            Region _arg03;
            Bundle _arg04;
            if (code != 1598968902) {
                boolean _arg05 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        onBind(ISystemUiProxy.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        onPreMotionEvent(data.readInt());
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (MotionEvent) MotionEvent.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        onMotionEvent(_arg0);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        onQuickScrubStart();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        onQuickScrubEnd();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        onQuickScrubProgress(data.readFloat());
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        onOverviewToggle();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg05 = true;
                        }
                        onOverviewShown(_arg05);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _arg06 = data.readInt() != 0;
                        if (data.readInt() != 0) {
                            _arg05 = true;
                        }
                        onOverviewHidden(_arg06, _arg05);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = (MotionEvent) MotionEvent.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        onQuickStep(_arg02);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        onTip(data.readInt(), data.readInt());
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = (Region) Region.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        onActiveNavBarRegionChanges(_arg03);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg04 = null;
                        }
                        onInitialize(_arg04);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg05 = true;
                        }
                        onAssistantAvailable(_arg05);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        onAssistantVisibilityChanged(data.readFloat());
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        onBackAction(data.readInt() != 0, data.readInt(), data.readInt(), data.readInt() != 0, data.readInt() != 0);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        onSystemUiStateChanged(data.readInt());
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
        public static class Proxy implements IOverviewProxy {
            public static IOverviewProxy sDefaultImpl;
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

            @Override // com.android.systemui.shared.recents.IOverviewProxy
            public void onActiveNavBarRegionChanges(Region activeRegion) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (activeRegion != null) {
                        _data.writeInt(1);
                        activeRegion.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(12, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onActiveNavBarRegionChanges(activeRegion);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.systemui.shared.recents.IOverviewProxy
            public void onInitialize(Bundle params) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (params != null) {
                        _data.writeInt(1);
                        params.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(13, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onInitialize(params);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.systemui.shared.recents.IOverviewProxy
            public void onBind(ISystemUiProxy sysUiProxy) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(sysUiProxy != null ? sysUiProxy.asBinder() : null);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onBind(sysUiProxy);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.systemui.shared.recents.IOverviewProxy
            public void onPreMotionEvent(int downHitTarget) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(downHitTarget);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onPreMotionEvent(downHitTarget);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.systemui.shared.recents.IOverviewProxy
            public void onMotionEvent(MotionEvent event) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (event != null) {
                        _data.writeInt(1);
                        event.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onMotionEvent(event);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.systemui.shared.recents.IOverviewProxy
            public void onQuickScrubStart() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onQuickScrubStart();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.systemui.shared.recents.IOverviewProxy
            public void onQuickScrubEnd() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onQuickScrubEnd();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.systemui.shared.recents.IOverviewProxy
            public void onQuickScrubProgress(float progress) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeFloat(progress);
                    if (this.mRemote.transact(6, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onQuickScrubProgress(progress);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.systemui.shared.recents.IOverviewProxy
            public void onOverviewToggle() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(7, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onOverviewToggle();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.systemui.shared.recents.IOverviewProxy
            public void onOverviewShown(boolean triggeredFromAltTab) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(triggeredFromAltTab ? 1 : 0);
                    if (this.mRemote.transact(8, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onOverviewShown(triggeredFromAltTab);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.systemui.shared.recents.IOverviewProxy
            public void onOverviewHidden(boolean triggeredFromAltTab, boolean triggeredFromHomeKey) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    int i = 0;
                    _data.writeInt(triggeredFromAltTab ? 1 : 0);
                    if (triggeredFromHomeKey) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    if (this.mRemote.transact(9, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onOverviewHidden(triggeredFromAltTab, triggeredFromHomeKey);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.systemui.shared.recents.IOverviewProxy
            public void onQuickStep(MotionEvent event) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (event != null) {
                        _data.writeInt(1);
                        event.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(10, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onQuickStep(event);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.systemui.shared.recents.IOverviewProxy
            public void onTip(int actionType, int viewType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(actionType);
                    _data.writeInt(viewType);
                    if (this.mRemote.transact(11, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onTip(actionType, viewType);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.systemui.shared.recents.IOverviewProxy
            public void onAssistantAvailable(boolean available) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(available ? 1 : 0);
                    if (this.mRemote.transact(14, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onAssistantAvailable(available);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.systemui.shared.recents.IOverviewProxy
            public void onAssistantVisibilityChanged(float visibility) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeFloat(visibility);
                    if (this.mRemote.transact(15, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onAssistantVisibilityChanged(visibility);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.systemui.shared.recents.IOverviewProxy
            public void onBackAction(boolean completed, int downX, int downY, boolean isButton, boolean gestureSwipeLeft) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    int i = 0;
                    _data.writeInt(completed ? 1 : 0);
                    _data.writeInt(downX);
                    _data.writeInt(downY);
                    _data.writeInt(isButton ? 1 : 0);
                    if (gestureSwipeLeft) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    if (this.mRemote.transact(16, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onBackAction(completed, downX, downY, isButton, gestureSwipeLeft);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.systemui.shared.recents.IOverviewProxy
            public void onSystemUiStateChanged(int stateFlags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(stateFlags);
                    if (this.mRemote.transact(17, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onSystemUiStateChanged(stateFlags);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IOverviewProxy impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IOverviewProxy getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
