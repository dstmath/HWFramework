package android.view;

import android.graphics.Rect;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.MergedConfiguration;
import android.view.DisplayCutout;
import com.android.internal.os.IResultReceiver;

public interface IWindow extends IInterface {

    public static abstract class Stub extends Binder implements IWindow {
        private static final String DESCRIPTOR = "android.view.IWindow";
        static final int TRANSACTION_closeSystemDialogs = 7;
        static final int TRANSACTION_dispatchAppVisibility = 4;
        static final int TRANSACTION_dispatchDragEvent = 10;
        static final int TRANSACTION_dispatchGetNewSurface = 5;
        static final int TRANSACTION_dispatchPointerCaptureChanged = 15;
        static final int TRANSACTION_dispatchSystemUiVisibilityChanged = 12;
        static final int TRANSACTION_dispatchWallpaperCommand = 9;
        static final int TRANSACTION_dispatchWallpaperOffsets = 8;
        static final int TRANSACTION_dispatchWindowShown = 13;
        static final int TRANSACTION_executeCommand = 1;
        static final int TRANSACTION_moved = 3;
        static final int TRANSACTION_notifyFocusChanged = 18;
        static final int TRANSACTION_registerWindowObserver = 16;
        static final int TRANSACTION_requestAppKeyboardShortcuts = 14;
        static final int TRANSACTION_resized = 2;
        static final int TRANSACTION_unRegisterWindowObserver = 17;
        static final int TRANSACTION_updatePointerIcon = 11;
        static final int TRANSACTION_updateSurfaceStatus = 19;
        static final int TRANSACTION_windowFocusChanged = 6;

        private static class Proxy implements IWindow {
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

            public void executeCommand(String command, String parameters, ParcelFileDescriptor descriptor) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(command);
                    _data.writeString(parameters);
                    if (descriptor != null) {
                        _data.writeInt(1);
                        descriptor.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void resized(Rect frame, Rect overscanInsets, Rect contentInsets, Rect visibleInsets, Rect stableInsets, Rect outsets, boolean reportDraw, MergedConfiguration newMergedConfiguration, Rect backDropFrame, boolean forceLayout, boolean alwaysConsumeNavBar, int displayId, DisplayCutout.ParcelableWrapper displayCutout) throws RemoteException {
                Rect rect = frame;
                Rect rect2 = overscanInsets;
                Rect rect3 = contentInsets;
                Rect rect4 = visibleInsets;
                Rect rect5 = stableInsets;
                Rect rect6 = outsets;
                MergedConfiguration mergedConfiguration = newMergedConfiguration;
                Rect rect7 = backDropFrame;
                DisplayCutout.ParcelableWrapper parcelableWrapper = displayCutout;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (rect != null) {
                        _data.writeInt(1);
                        rect.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (rect2 != null) {
                        _data.writeInt(1);
                        rect2.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (rect3 != null) {
                        _data.writeInt(1);
                        rect3.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (rect4 != null) {
                        _data.writeInt(1);
                        rect4.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (rect5 != null) {
                        _data.writeInt(1);
                        rect5.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (rect6 != null) {
                        _data.writeInt(1);
                        rect6.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    try {
                        _data.writeInt(reportDraw ? 1 : 0);
                        if (mergedConfiguration != null) {
                            _data.writeInt(1);
                            mergedConfiguration.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        if (rect7 != null) {
                            _data.writeInt(1);
                            rect7.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        try {
                            _data.writeInt(forceLayout ? 1 : 0);
                        } catch (Throwable th) {
                            th = th;
                            boolean z = alwaysConsumeNavBar;
                            int i = displayId;
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeInt(alwaysConsumeNavBar ? 1 : 0);
                            try {
                                _data.writeInt(displayId);
                                if (parcelableWrapper != null) {
                                    _data.writeInt(1);
                                    parcelableWrapper.writeToParcel(_data, 0);
                                } else {
                                    _data.writeInt(0);
                                }
                            } catch (Throwable th2) {
                                th = th2;
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            int i2 = displayId;
                            _data.recycle();
                            throw th;
                        }
                        try {
                            this.mRemote.transact(2, _data, null, 1);
                            _data.recycle();
                        } catch (Throwable th4) {
                            th = th4;
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        boolean z2 = forceLayout;
                        boolean z3 = alwaysConsumeNavBar;
                        int i22 = displayId;
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th6) {
                    th = th6;
                    boolean z4 = reportDraw;
                    boolean z22 = forceLayout;
                    boolean z32 = alwaysConsumeNavBar;
                    int i222 = displayId;
                    _data.recycle();
                    throw th;
                }
            }

            public void moved(int newX, int newY) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(newX);
                    _data.writeInt(newY);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void dispatchAppVisibility(boolean visible) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(visible);
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void dispatchGetNewSurface() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(5, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void windowFocusChanged(boolean hasFocus, boolean inTouchMode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(hasFocus);
                    _data.writeInt(inTouchMode);
                    this.mRemote.transact(6, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void closeSystemDialogs(String reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(reason);
                    this.mRemote.transact(7, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void dispatchWallpaperOffsets(float x, float y, float xStep, float yStep, boolean sync) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeFloat(x);
                    _data.writeFloat(y);
                    _data.writeFloat(xStep);
                    _data.writeFloat(yStep);
                    _data.writeInt(sync);
                    this.mRemote.transact(8, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void dispatchWallpaperCommand(String action, int x, int y, int z, Bundle extras, boolean sync) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(action);
                    _data.writeInt(x);
                    _data.writeInt(y);
                    _data.writeInt(z);
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(sync);
                    this.mRemote.transact(9, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void dispatchDragEvent(DragEvent event) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (event != null) {
                        _data.writeInt(1);
                        event.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(10, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void updatePointerIcon(float x, float y) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeFloat(x);
                    _data.writeFloat(y);
                    this.mRemote.transact(11, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void dispatchSystemUiVisibilityChanged(int seq, int globalVisibility, int localValue, int localChanges) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(seq);
                    _data.writeInt(globalVisibility);
                    _data.writeInt(localValue);
                    _data.writeInt(localChanges);
                    this.mRemote.transact(12, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void dispatchWindowShown() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(13, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void requestAppKeyboardShortcuts(IResultReceiver receiver, int deviceId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(receiver != null ? receiver.asBinder() : null);
                    _data.writeInt(deviceId);
                    this.mRemote.transact(14, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void dispatchPointerCaptureChanged(boolean hasCapture) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(hasCapture);
                    this.mRemote.transact(15, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void registerWindowObserver(IWindowLayoutObserver observer, long period) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    _data.writeLong(period);
                    this.mRemote.transact(16, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void unRegisterWindowObserver(IWindowLayoutObserver observer) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    this.mRemote.transact(17, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void notifyFocusChanged() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(18, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void updateSurfaceStatus(boolean status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(status);
                    this.mRemote.transact(19, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IWindow asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IWindow)) {
                return new Proxy(obj);
            }
            return (IWindow) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r17v0, resolved type: android.os.ParcelFileDescriptor} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r17v1, resolved type: android.os.ParcelFileDescriptor} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r17v4, resolved type: android.os.ParcelFileDescriptor} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v20, resolved type: android.view.DragEvent} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r17v7, resolved type: android.os.ParcelFileDescriptor} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r17v8, resolved type: android.os.ParcelFileDescriptor} */
        /* JADX WARNING: type inference failed for: r17v6, types: [android.view.DragEvent] */
        /*  JADX ERROR: NullPointerException in pass: CodeShrinkVisitor
            java.lang.NullPointerException
            */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int r21, android.os.Parcel r22, android.os.Parcel r23, int r24) throws android.os.RemoteException {
            /*
                r20 = this;
                r14 = r20
                r15 = r21
                r12 = r22
                java.lang.String r13 = "android.view.IWindow"
                r0 = 1598968902(0x5f4e5446, float:1.4867585E19)
                r16 = 1
                if (r15 == r0) goto L_0x0256
                r0 = 0
                r17 = 0
                switch(r15) {
                    case 1: goto L_0x022f;
                    case 2: goto L_0x0167;
                    case 3: goto L_0x0158;
                    case 4: goto L_0x0148;
                    case 5: goto L_0x0141;
                    case 6: goto L_0x0127;
                    case 7: goto L_0x011c;
                    case 8: goto L_0x00f6;
                    case 9: goto L_0x00be;
                    case 10: goto L_0x00a3;
                    case 11: goto L_0x0094;
                    case 12: goto L_0x007d;
                    case 13: goto L_0x0076;
                    case 14: goto L_0x0063;
                    case 15: goto L_0x0053;
                    case 16: goto L_0x0040;
                    case 17: goto L_0x0031;
                    case 18: goto L_0x002a;
                    case 19: goto L_0x001a;
                    default: goto L_0x0015;
                }
            L_0x0015:
                boolean r0 = super.onTransact(r21, r22, r23, r24)
                return r0
            L_0x001a:
                r12.enforceInterface(r13)
                int r1 = r22.readInt()
                if (r1 == 0) goto L_0x0026
                r0 = r16
            L_0x0026:
                r14.updateSurfaceStatus(r0)
                return r16
            L_0x002a:
                r12.enforceInterface(r13)
                r20.notifyFocusChanged()
                return r16
            L_0x0031:
                r12.enforceInterface(r13)
                android.os.IBinder r0 = r22.readStrongBinder()
                android.view.IWindowLayoutObserver r0 = android.view.IWindowLayoutObserver.Stub.asInterface(r0)
                r14.unRegisterWindowObserver(r0)
                return r16
            L_0x0040:
                r12.enforceInterface(r13)
                android.os.IBinder r0 = r22.readStrongBinder()
                android.view.IWindowLayoutObserver r0 = android.view.IWindowLayoutObserver.Stub.asInterface(r0)
                long r1 = r22.readLong()
                r14.registerWindowObserver(r0, r1)
                return r16
            L_0x0053:
                r12.enforceInterface(r13)
                int r1 = r22.readInt()
                if (r1 == 0) goto L_0x005f
                r0 = r16
            L_0x005f:
                r14.dispatchPointerCaptureChanged(r0)
                return r16
            L_0x0063:
                r12.enforceInterface(r13)
                android.os.IBinder r0 = r22.readStrongBinder()
                com.android.internal.os.IResultReceiver r0 = com.android.internal.os.IResultReceiver.Stub.asInterface(r0)
                int r1 = r22.readInt()
                r14.requestAppKeyboardShortcuts(r0, r1)
                return r16
            L_0x0076:
                r12.enforceInterface(r13)
                r20.dispatchWindowShown()
                return r16
            L_0x007d:
                r12.enforceInterface(r13)
                int r0 = r22.readInt()
                int r1 = r22.readInt()
                int r2 = r22.readInt()
                int r3 = r22.readInt()
                r14.dispatchSystemUiVisibilityChanged(r0, r1, r2, r3)
                return r16
            L_0x0094:
                r12.enforceInterface(r13)
                float r0 = r22.readFloat()
                float r1 = r22.readFloat()
                r14.updatePointerIcon(r0, r1)
                return r16
            L_0x00a3:
                r12.enforceInterface(r13)
                int r0 = r22.readInt()
                if (r0 == 0) goto L_0x00b7
                android.os.Parcelable$Creator<android.view.DragEvent> r0 = android.view.DragEvent.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r12)
                r17 = r0
                android.view.DragEvent r17 = (android.view.DragEvent) r17
                goto L_0x00b8
            L_0x00b7:
            L_0x00b8:
                r0 = r17
                r14.dispatchDragEvent(r0)
                return r16
            L_0x00be:
                r12.enforceInterface(r13)
                java.lang.String r7 = r22.readString()
                int r8 = r22.readInt()
                int r9 = r22.readInt()
                int r10 = r22.readInt()
                int r1 = r22.readInt()
                if (r1 == 0) goto L_0x00e1
                android.os.Parcelable$Creator<android.os.Bundle> r1 = android.os.Bundle.CREATOR
                java.lang.Object r1 = r1.createFromParcel(r12)
                android.os.Bundle r1 = (android.os.Bundle) r1
                r5 = r1
                goto L_0x00e3
            L_0x00e1:
                r5 = r17
            L_0x00e3:
                int r1 = r22.readInt()
                if (r1 == 0) goto L_0x00ec
                r6 = r16
                goto L_0x00ed
            L_0x00ec:
                r6 = r0
            L_0x00ed:
                r0 = r14
                r1 = r7
                r2 = r8
                r3 = r9
                r4 = r10
                r0.dispatchWallpaperCommand(r1, r2, r3, r4, r5, r6)
                return r16
            L_0x00f6:
                r12.enforceInterface(r13)
                float r6 = r22.readFloat()
                float r7 = r22.readFloat()
                float r8 = r22.readFloat()
                float r9 = r22.readFloat()
                int r1 = r22.readInt()
                if (r1 == 0) goto L_0x0112
                r5 = r16
                goto L_0x0113
            L_0x0112:
                r5 = r0
            L_0x0113:
                r0 = r14
                r1 = r6
                r2 = r7
                r3 = r8
                r4 = r9
                r0.dispatchWallpaperOffsets(r1, r2, r3, r4, r5)
                return r16
            L_0x011c:
                r12.enforceInterface(r13)
                java.lang.String r0 = r22.readString()
                r14.closeSystemDialogs(r0)
                return r16
            L_0x0127:
                r12.enforceInterface(r13)
                int r1 = r22.readInt()
                if (r1 == 0) goto L_0x0133
                r1 = r16
                goto L_0x0134
            L_0x0133:
                r1 = r0
            L_0x0134:
                int r2 = r22.readInt()
                if (r2 == 0) goto L_0x013d
                r0 = r16
            L_0x013d:
                r14.windowFocusChanged(r1, r0)
                return r16
            L_0x0141:
                r12.enforceInterface(r13)
                r20.dispatchGetNewSurface()
                return r16
            L_0x0148:
                r12.enforceInterface(r13)
                int r1 = r22.readInt()
                if (r1 == 0) goto L_0x0154
                r0 = r16
            L_0x0154:
                r14.dispatchAppVisibility(r0)
                return r16
            L_0x0158:
                r12.enforceInterface(r13)
                int r0 = r22.readInt()
                int r1 = r22.readInt()
                r14.moved(r0, r1)
                return r16
            L_0x0167:
                r12.enforceInterface(r13)
                int r1 = r22.readInt()
                if (r1 == 0) goto L_0x0179
                android.os.Parcelable$Creator r1 = android.graphics.Rect.CREATOR
                java.lang.Object r1 = r1.createFromParcel(r12)
                android.graphics.Rect r1 = (android.graphics.Rect) r1
                goto L_0x017b
            L_0x0179:
                r1 = r17
            L_0x017b:
                int r2 = r22.readInt()
                if (r2 == 0) goto L_0x018a
                android.os.Parcelable$Creator r2 = android.graphics.Rect.CREATOR
                java.lang.Object r2 = r2.createFromParcel(r12)
                android.graphics.Rect r2 = (android.graphics.Rect) r2
                goto L_0x018c
            L_0x018a:
                r2 = r17
            L_0x018c:
                int r3 = r22.readInt()
                if (r3 == 0) goto L_0x019b
                android.os.Parcelable$Creator r3 = android.graphics.Rect.CREATOR
                java.lang.Object r3 = r3.createFromParcel(r12)
                android.graphics.Rect r3 = (android.graphics.Rect) r3
                goto L_0x019d
            L_0x019b:
                r3 = r17
            L_0x019d:
                int r4 = r22.readInt()
                if (r4 == 0) goto L_0x01ac
                android.os.Parcelable$Creator r4 = android.graphics.Rect.CREATOR
                java.lang.Object r4 = r4.createFromParcel(r12)
                android.graphics.Rect r4 = (android.graphics.Rect) r4
                goto L_0x01ae
            L_0x01ac:
                r4 = r17
            L_0x01ae:
                int r5 = r22.readInt()
                if (r5 == 0) goto L_0x01bd
                android.os.Parcelable$Creator r5 = android.graphics.Rect.CREATOR
                java.lang.Object r5 = r5.createFromParcel(r12)
                android.graphics.Rect r5 = (android.graphics.Rect) r5
                goto L_0x01bf
            L_0x01bd:
                r5 = r17
            L_0x01bf:
                int r6 = r22.readInt()
                if (r6 == 0) goto L_0x01ce
                android.os.Parcelable$Creator r6 = android.graphics.Rect.CREATOR
                java.lang.Object r6 = r6.createFromParcel(r12)
                android.graphics.Rect r6 = (android.graphics.Rect) r6
                goto L_0x01d0
            L_0x01ce:
                r6 = r17
            L_0x01d0:
                int r7 = r22.readInt()
                if (r7 == 0) goto L_0x01d9
                r7 = r16
                goto L_0x01da
            L_0x01d9:
                r7 = r0
            L_0x01da:
                int r8 = r22.readInt()
                if (r8 == 0) goto L_0x01e9
                android.os.Parcelable$Creator<android.util.MergedConfiguration> r8 = android.util.MergedConfiguration.CREATOR
                java.lang.Object r8 = r8.createFromParcel(r12)
                android.util.MergedConfiguration r8 = (android.util.MergedConfiguration) r8
                goto L_0x01eb
            L_0x01e9:
                r8 = r17
            L_0x01eb:
                int r9 = r22.readInt()
                if (r9 == 0) goto L_0x01fa
                android.os.Parcelable$Creator r9 = android.graphics.Rect.CREATOR
                java.lang.Object r9 = r9.createFromParcel(r12)
                android.graphics.Rect r9 = (android.graphics.Rect) r9
                goto L_0x01fc
            L_0x01fa:
                r9 = r17
            L_0x01fc:
                int r10 = r22.readInt()
                if (r10 == 0) goto L_0x0205
                r10 = r16
                goto L_0x0206
            L_0x0205:
                r10 = r0
            L_0x0206:
                int r11 = r22.readInt()
                if (r11 == 0) goto L_0x020f
                r11 = r16
                goto L_0x0210
            L_0x020f:
                r11 = r0
            L_0x0210:
                int r18 = r22.readInt()
                int r0 = r22.readInt()
                if (r0 == 0) goto L_0x0223
                android.os.Parcelable$Creator<android.view.DisplayCutout$ParcelableWrapper> r0 = android.view.DisplayCutout.ParcelableWrapper.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r12)
                android.view.DisplayCutout$ParcelableWrapper r0 = (android.view.DisplayCutout.ParcelableWrapper) r0
                goto L_0x0225
            L_0x0223:
                r0 = r17
            L_0x0225:
                r15 = r13
                r13 = r0
                r0 = r14
                r14 = r12
                r12 = r18
                r0.resized(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13)
                return r16
            L_0x022f:
                r14 = r12
                r15 = r13
                r14.enforceInterface(r15)
                java.lang.String r0 = r22.readString()
                java.lang.String r1 = r22.readString()
                int r2 = r22.readInt()
                if (r2 == 0) goto L_0x024d
                android.os.Parcelable$Creator<android.os.ParcelFileDescriptor> r2 = android.os.ParcelFileDescriptor.CREATOR
                java.lang.Object r2 = r2.createFromParcel(r14)
                r17 = r2
                android.os.ParcelFileDescriptor r17 = (android.os.ParcelFileDescriptor) r17
                goto L_0x024e
            L_0x024d:
            L_0x024e:
                r2 = r17
                r3 = r20
                r3.executeCommand(r0, r1, r2)
                return r16
            L_0x0256:
                r15 = r13
                r3 = r14
                r0 = r23
                r0.writeString(r15)
                return r16
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.IWindow.Stub.onTransact(int, android.os.Parcel, android.os.Parcel, int):boolean");
        }
    }

    void closeSystemDialogs(String str) throws RemoteException;

    void dispatchAppVisibility(boolean z) throws RemoteException;

    void dispatchDragEvent(DragEvent dragEvent) throws RemoteException;

    void dispatchGetNewSurface() throws RemoteException;

    void dispatchPointerCaptureChanged(boolean z) throws RemoteException;

    void dispatchSystemUiVisibilityChanged(int i, int i2, int i3, int i4) throws RemoteException;

    void dispatchWallpaperCommand(String str, int i, int i2, int i3, Bundle bundle, boolean z) throws RemoteException;

    void dispatchWallpaperOffsets(float f, float f2, float f3, float f4, boolean z) throws RemoteException;

    void dispatchWindowShown() throws RemoteException;

    void executeCommand(String str, String str2, ParcelFileDescriptor parcelFileDescriptor) throws RemoteException;

    void moved(int i, int i2) throws RemoteException;

    void notifyFocusChanged() throws RemoteException;

    void registerWindowObserver(IWindowLayoutObserver iWindowLayoutObserver, long j) throws RemoteException;

    void requestAppKeyboardShortcuts(IResultReceiver iResultReceiver, int i) throws RemoteException;

    void resized(Rect rect, Rect rect2, Rect rect3, Rect rect4, Rect rect5, Rect rect6, boolean z, MergedConfiguration mergedConfiguration, Rect rect7, boolean z2, boolean z3, int i, DisplayCutout.ParcelableWrapper parcelableWrapper) throws RemoteException;

    void unRegisterWindowObserver(IWindowLayoutObserver iWindowLayoutObserver) throws RemoteException;

    void updatePointerIcon(float f, float f2) throws RemoteException;

    void updateSurfaceStatus(boolean z) throws RemoteException;

    void windowFocusChanged(boolean z, boolean z2) throws RemoteException;
}
