package android.view;

import android.content.ClipData;
import android.graphics.Rect;
import android.graphics.Region;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.MergedConfiguration;
import android.view.WindowManager.LayoutParams;

public interface IWindowSession extends IInterface {

    public static abstract class Stub extends Binder implements IWindowSession {
        private static final String DESCRIPTOR = "android.view.IWindowSession";
        static final int TRANSACTION_add = 1;
        static final int TRANSACTION_addToDisplay = 2;
        static final int TRANSACTION_addToDisplayWithoutInputChannel = 4;
        static final int TRANSACTION_addWithoutInputChannel = 3;
        static final int TRANSACTION_cancelDragAndDrop = 19;
        static final int TRANSACTION_dragRecipientEntered = 20;
        static final int TRANSACTION_dragRecipientExited = 21;
        static final int TRANSACTION_finishDrawing = 12;
        static final int TRANSACTION_getDisplayFrame = 11;
        static final int TRANSACTION_getInTouchMode = 14;
        static final int TRANSACTION_getWindowId = 28;
        static final int TRANSACTION_onRectangleOnScreenRequested = 27;
        static final int TRANSACTION_outOfMemory = 8;
        static final int TRANSACTION_performDrag = 17;
        static final int TRANSACTION_performHapticFeedback = 15;
        static final int TRANSACTION_pokeDrawLock = 29;
        static final int TRANSACTION_prepareDrag = 16;
        static final int TRANSACTION_prepareToReplaceWindows = 7;
        static final int TRANSACTION_relayout = 6;
        static final int TRANSACTION_remove = 5;
        static final int TRANSACTION_reportDropResult = 18;
        static final int TRANSACTION_sendWallpaperCommand = 25;
        static final int TRANSACTION_setInTouchMode = 13;
        static final int TRANSACTION_setInsets = 10;
        static final int TRANSACTION_setTransparentRegion = 9;
        static final int TRANSACTION_setWallpaperDisplayOffset = 24;
        static final int TRANSACTION_setWallpaperPosition = 22;
        static final int TRANSACTION_startMovingTask = 30;
        static final int TRANSACTION_updatePointerIcon = 31;
        static final int TRANSACTION_wallpaperCommandComplete = 26;
        static final int TRANSACTION_wallpaperOffsetsComplete = 23;

        private static class Proxy implements IWindowSession {
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

            public int add(IWindow window, int seq, LayoutParams attrs, int viewVisibility, Rect outContentInsets, Rect outStableInsets, InputChannel outInputChannel) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (window != null) {
                        iBinder = window.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(seq);
                    if (attrs != null) {
                        _data.writeInt(1);
                        attrs.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(viewVisibility);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    if (_reply.readInt() != 0) {
                        outContentInsets.readFromParcel(_reply);
                    }
                    if (_reply.readInt() != 0) {
                        outStableInsets.readFromParcel(_reply);
                    }
                    if (_reply.readInt() != 0) {
                        outInputChannel.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int addToDisplay(IWindow window, int seq, LayoutParams attrs, int viewVisibility, int layerStackId, Rect outContentInsets, Rect outStableInsets, Rect outOutsets, InputChannel outInputChannel) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (window != null) {
                        iBinder = window.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(seq);
                    if (attrs != null) {
                        _data.writeInt(1);
                        attrs.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(viewVisibility);
                    _data.writeInt(layerStackId);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    if (_reply.readInt() != 0) {
                        outContentInsets.readFromParcel(_reply);
                    }
                    if (_reply.readInt() != 0) {
                        outStableInsets.readFromParcel(_reply);
                    }
                    if (_reply.readInt() != 0) {
                        outOutsets.readFromParcel(_reply);
                    }
                    if (_reply.readInt() != 0) {
                        outInputChannel.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int addWithoutInputChannel(IWindow window, int seq, LayoutParams attrs, int viewVisibility, Rect outContentInsets, Rect outStableInsets) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (window != null) {
                        iBinder = window.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(seq);
                    if (attrs != null) {
                        _data.writeInt(1);
                        attrs.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(viewVisibility);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    if (_reply.readInt() != 0) {
                        outContentInsets.readFromParcel(_reply);
                    }
                    if (_reply.readInt() != 0) {
                        outStableInsets.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int addToDisplayWithoutInputChannel(IWindow window, int seq, LayoutParams attrs, int viewVisibility, int layerStackId, Rect outContentInsets, Rect outStableInsets) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (window != null) {
                        iBinder = window.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(seq);
                    if (attrs != null) {
                        _data.writeInt(1);
                        attrs.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(viewVisibility);
                    _data.writeInt(layerStackId);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    if (_reply.readInt() != 0) {
                        outContentInsets.readFromParcel(_reply);
                    }
                    if (_reply.readInt() != 0) {
                        outStableInsets.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void remove(IWindow window) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (window != null) {
                        iBinder = window.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int relayout(IWindow window, int seq, LayoutParams attrs, int requestedWidth, int requestedHeight, int viewVisibility, int flags, Rect outFrame, Rect outOverscanInsets, Rect outContentInsets, Rect outVisibleInsets, Rect outStableInsets, Rect outOutsets, Rect outBackdropFrame, MergedConfiguration outMergedConfiguration, Surface outSurface) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(window != null ? window.asBinder() : null);
                    _data.writeInt(seq);
                    if (attrs != null) {
                        _data.writeInt(1);
                        attrs.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(requestedWidth);
                    _data.writeInt(requestedHeight);
                    _data.writeInt(viewVisibility);
                    _data.writeInt(flags);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    if (_reply.readInt() != 0) {
                        outFrame.readFromParcel(_reply);
                    }
                    if (_reply.readInt() != 0) {
                        outOverscanInsets.readFromParcel(_reply);
                    }
                    if (_reply.readInt() != 0) {
                        outContentInsets.readFromParcel(_reply);
                    }
                    if (_reply.readInt() != 0) {
                        outVisibleInsets.readFromParcel(_reply);
                    }
                    if (_reply.readInt() != 0) {
                        outStableInsets.readFromParcel(_reply);
                    }
                    if (_reply.readInt() != 0) {
                        outOutsets.readFromParcel(_reply);
                    }
                    if (_reply.readInt() != 0) {
                        outBackdropFrame.readFromParcel(_reply);
                    }
                    if (_reply.readInt() != 0) {
                        outMergedConfiguration.readFromParcel(_reply);
                    }
                    if (_reply.readInt() != 0) {
                        outSurface.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void prepareToReplaceWindows(IBinder appToken, boolean childrenOnly) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(appToken);
                    if (childrenOnly) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean outOfMemory(IWindow window) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (window != null) {
                        iBinder = window.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setTransparentRegion(IWindow window, Region region) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (window != null) {
                        iBinder = window.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (region != null) {
                        _data.writeInt(1);
                        region.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setInsets(IWindow window, int touchableInsets, Rect contentInsets, Rect visibleInsets, Region touchableRegion) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (window != null) {
                        iBinder = window.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(touchableInsets);
                    if (contentInsets != null) {
                        _data.writeInt(1);
                        contentInsets.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (visibleInsets != null) {
                        _data.writeInt(1);
                        visibleInsets.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (touchableRegion != null) {
                        _data.writeInt(1);
                        touchableRegion.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void getDisplayFrame(IWindow window, Rect outDisplayFrame) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (window != null) {
                        iBinder = window.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        outDisplayFrame.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void finishDrawing(IWindow window) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (window != null) {
                        iBinder = window.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setInTouchMode(boolean showFocus) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (showFocus) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean getInTouchMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean performHapticFeedback(IWindow window, int effectId, boolean always) throws RemoteException {
                IBinder iBinder = null;
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (window != null) {
                        iBinder = window.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(effectId);
                    if (always) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IBinder prepareDrag(IWindow window, int flags, int thumbnailWidth, int thumbnailHeight, Surface outSurface) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (window != null) {
                        iBinder = window.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(flags);
                    _data.writeInt(thumbnailWidth);
                    _data.writeInt(thumbnailHeight);
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                    IBinder _result = _reply.readStrongBinder();
                    if (_reply.readInt() != 0) {
                        outSurface.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean performDrag(IWindow window, IBinder dragToken, int touchSource, float touchX, float touchY, float thumbCenterX, float thumbCenterY, ClipData data) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (window != null) {
                        iBinder = window.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeStrongBinder(dragToken);
                    _data.writeInt(touchSource);
                    _data.writeFloat(touchX);
                    _data.writeFloat(touchY);
                    _data.writeFloat(thumbCenterX);
                    _data.writeFloat(thumbCenterY);
                    if (data != null) {
                        _data.writeInt(1);
                        data.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void reportDropResult(IWindow window, boolean consumed) throws RemoteException {
                IBinder iBinder = null;
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (window != null) {
                        iBinder = window.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (consumed) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void cancelDragAndDrop(IBinder dragToken) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(dragToken);
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void dragRecipientEntered(IWindow window) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (window != null) {
                        iBinder = window.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(20, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void dragRecipientExited(IWindow window) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (window != null) {
                        iBinder = window.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(21, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setWallpaperPosition(IBinder windowToken, float x, float y, float xstep, float ystep) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(windowToken);
                    _data.writeFloat(x);
                    _data.writeFloat(y);
                    _data.writeFloat(xstep);
                    _data.writeFloat(ystep);
                    this.mRemote.transact(22, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void wallpaperOffsetsComplete(IBinder window) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(window);
                    this.mRemote.transact(23, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setWallpaperDisplayOffset(IBinder windowToken, int x, int y) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(windowToken);
                    _data.writeInt(x);
                    _data.writeInt(y);
                    this.mRemote.transact(24, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Bundle sendWallpaperCommand(IBinder window, String action, int x, int y, int z, Bundle extras, boolean sync) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    Bundle _result;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(window);
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
                    if (!sync) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(25, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (Bundle) Bundle.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void wallpaperCommandComplete(IBinder window, Bundle result) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(window);
                    if (result != null) {
                        _data.writeInt(1);
                        result.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(26, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onRectangleOnScreenRequested(IBinder token, Rect rectangle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (rectangle != null) {
                        _data.writeInt(1);
                        rectangle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(27, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IWindowId getWindowId(IBinder window) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(window);
                    this.mRemote.transact(28, _data, _reply, 0);
                    _reply.readException();
                    IWindowId _result = android.view.IWindowId.Stub.asInterface(_reply.readStrongBinder());
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void pokeDrawLock(IBinder window) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(window);
                    this.mRemote.transact(29, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean startMovingTask(IWindow window, float startX, float startY) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (window != null) {
                        iBinder = window.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeFloat(startX);
                    _data.writeFloat(startY);
                    this.mRemote.transact(30, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void updatePointerIcon(IWindow window) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (window != null) {
                        iBinder = window.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(31, _data, _reply, 0);
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

        public static IWindowSession asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IWindowSession)) {
                return new Proxy(obj);
            }
            return (IWindowSession) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            IWindow _arg0;
            int _arg1;
            LayoutParams _arg2;
            int _arg3;
            Rect _arg4;
            Rect _arg5;
            int _result;
            int _arg42;
            Rect _arg6;
            Rect _arg7;
            boolean _result2;
            Rect _arg12;
            int _arg22;
            IBinder _arg02;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = android.view.IWindow.Stub.asInterface(data.readStrongBinder());
                    _arg1 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg2 = (LayoutParams) LayoutParams.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    _arg3 = data.readInt();
                    _arg4 = new Rect();
                    _arg5 = new Rect();
                    InputChannel _arg62 = new InputChannel();
                    _result = add(_arg0, _arg1, _arg2, _arg3, _arg4, _arg5, _arg62);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    if (_arg4 != null) {
                        reply.writeInt(1);
                        _arg4.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    if (_arg5 != null) {
                        reply.writeInt(1);
                        _arg5.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    if (_arg62 != null) {
                        reply.writeInt(1);
                        _arg62.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = android.view.IWindow.Stub.asInterface(data.readStrongBinder());
                    _arg1 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg2 = (LayoutParams) LayoutParams.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    _arg3 = data.readInt();
                    _arg42 = data.readInt();
                    _arg5 = new Rect();
                    _arg6 = new Rect();
                    _arg7 = new Rect();
                    InputChannel _arg8 = new InputChannel();
                    _result = addToDisplay(_arg0, _arg1, _arg2, _arg3, _arg42, _arg5, _arg6, _arg7, _arg8);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    if (_arg5 != null) {
                        reply.writeInt(1);
                        _arg5.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    if (_arg6 != null) {
                        reply.writeInt(1);
                        _arg6.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    if (_arg7 != null) {
                        reply.writeInt(1);
                        _arg7.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    if (_arg8 != null) {
                        reply.writeInt(1);
                        _arg8.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = android.view.IWindow.Stub.asInterface(data.readStrongBinder());
                    _arg1 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg2 = (LayoutParams) LayoutParams.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    _arg3 = data.readInt();
                    _arg4 = new Rect();
                    _arg5 = new Rect();
                    _result = addWithoutInputChannel(_arg0, _arg1, _arg2, _arg3, _arg4, _arg5);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    if (_arg4 != null) {
                        reply.writeInt(1);
                        _arg4.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    if (_arg5 != null) {
                        reply.writeInt(1);
                        _arg5.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = android.view.IWindow.Stub.asInterface(data.readStrongBinder());
                    _arg1 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg2 = (LayoutParams) LayoutParams.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    _arg3 = data.readInt();
                    _arg42 = data.readInt();
                    _arg5 = new Rect();
                    _arg6 = new Rect();
                    _result = addToDisplayWithoutInputChannel(_arg0, _arg1, _arg2, _arg3, _arg42, _arg5, _arg6);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    if (_arg5 != null) {
                        reply.writeInt(1);
                        _arg5.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    if (_arg6 != null) {
                        reply.writeInt(1);
                        _arg6.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    remove(android.view.IWindow.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = android.view.IWindow.Stub.asInterface(data.readStrongBinder());
                    _arg1 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg2 = (LayoutParams) LayoutParams.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    _arg3 = data.readInt();
                    _arg42 = data.readInt();
                    int _arg52 = data.readInt();
                    int _arg63 = data.readInt();
                    _arg7 = new Rect();
                    Rect _arg82 = new Rect();
                    Rect _arg9 = new Rect();
                    Rect _arg10 = new Rect();
                    Rect _arg11 = new Rect();
                    Rect _arg122 = new Rect();
                    Rect _arg13 = new Rect();
                    MergedConfiguration _arg14 = new MergedConfiguration();
                    Surface _arg15 = new Surface();
                    _result = relayout(_arg0, _arg1, _arg2, _arg3, _arg42, _arg52, _arg63, _arg7, _arg82, _arg9, _arg10, _arg11, _arg122, _arg13, _arg14, _arg15);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    if (_arg7 != null) {
                        reply.writeInt(1);
                        _arg7.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    if (_arg82 != null) {
                        reply.writeInt(1);
                        _arg82.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    if (_arg9 != null) {
                        reply.writeInt(1);
                        _arg9.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    if (_arg10 != null) {
                        reply.writeInt(1);
                        _arg10.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    if (_arg11 != null) {
                        reply.writeInt(1);
                        _arg11.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    if (_arg122 != null) {
                        reply.writeInt(1);
                        _arg122.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    if (_arg13 != null) {
                        reply.writeInt(1);
                        _arg13.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    if (_arg14 != null) {
                        reply.writeInt(1);
                        _arg14.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    if (_arg15 != null) {
                        reply.writeInt(1);
                        _arg15.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    prepareToReplaceWindows(data.readStrongBinder(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case 8:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = outOfMemory(android.view.IWindow.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case 9:
                    Region _arg16;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = android.view.IWindow.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        _arg16 = (Region) Region.CREATOR.createFromParcel(data);
                    } else {
                        _arg16 = null;
                    }
                    setTransparentRegion(_arg0, _arg16);
                    reply.writeNoException();
                    return true;
                case 10:
                    Rect _arg23;
                    Rect _arg32;
                    Region _arg43;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = android.view.IWindow.Stub.asInterface(data.readStrongBinder());
                    _arg1 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg23 = (Rect) Rect.CREATOR.createFromParcel(data);
                    } else {
                        _arg23 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg32 = (Rect) Rect.CREATOR.createFromParcel(data);
                    } else {
                        _arg32 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg43 = (Region) Region.CREATOR.createFromParcel(data);
                    } else {
                        _arg43 = null;
                    }
                    setInsets(_arg0, _arg1, _arg23, _arg32, _arg43);
                    reply.writeNoException();
                    return true;
                case 11:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = android.view.IWindow.Stub.asInterface(data.readStrongBinder());
                    _arg12 = new Rect();
                    getDisplayFrame(_arg0, _arg12);
                    reply.writeNoException();
                    if (_arg12 != null) {
                        reply.writeInt(1);
                        _arg12.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 12:
                    data.enforceInterface(DESCRIPTOR);
                    finishDrawing(android.view.IWindow.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 13:
                    data.enforceInterface(DESCRIPTOR);
                    setInTouchMode(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case 14:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getInTouchMode();
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case 15:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = performHapticFeedback(android.view.IWindow.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case 16:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = android.view.IWindow.Stub.asInterface(data.readStrongBinder());
                    _arg1 = data.readInt();
                    _arg22 = data.readInt();
                    _arg3 = data.readInt();
                    Surface _arg44 = new Surface();
                    IBinder _result3 = prepareDrag(_arg0, _arg1, _arg22, _arg3, _arg44);
                    reply.writeNoException();
                    reply.writeStrongBinder(_result3);
                    if (_arg44 != null) {
                        reply.writeInt(1);
                        _arg44.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 17:
                    ClipData _arg72;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = android.view.IWindow.Stub.asInterface(data.readStrongBinder());
                    IBinder _arg17 = data.readStrongBinder();
                    _arg22 = data.readInt();
                    float _arg33 = data.readFloat();
                    float _arg45 = data.readFloat();
                    float _arg53 = data.readFloat();
                    float _arg64 = data.readFloat();
                    if (data.readInt() != 0) {
                        _arg72 = (ClipData) ClipData.CREATOR.createFromParcel(data);
                    } else {
                        _arg72 = null;
                    }
                    _result2 = performDrag(_arg0, _arg17, _arg22, _arg33, _arg45, _arg53, _arg64, _arg72);
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case 18:
                    data.enforceInterface(DESCRIPTOR);
                    reportDropResult(android.view.IWindow.Stub.asInterface(data.readStrongBinder()), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case 19:
                    data.enforceInterface(DESCRIPTOR);
                    cancelDragAndDrop(data.readStrongBinder());
                    reply.writeNoException();
                    return true;
                case 20:
                    data.enforceInterface(DESCRIPTOR);
                    dragRecipientEntered(android.view.IWindow.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 21:
                    data.enforceInterface(DESCRIPTOR);
                    dragRecipientExited(android.view.IWindow.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 22:
                    data.enforceInterface(DESCRIPTOR);
                    setWallpaperPosition(data.readStrongBinder(), data.readFloat(), data.readFloat(), data.readFloat(), data.readFloat());
                    reply.writeNoException();
                    return true;
                case 23:
                    data.enforceInterface(DESCRIPTOR);
                    wallpaperOffsetsComplete(data.readStrongBinder());
                    reply.writeNoException();
                    return true;
                case 24:
                    data.enforceInterface(DESCRIPTOR);
                    setWallpaperDisplayOffset(data.readStrongBinder(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 25:
                    Bundle _arg54;
                    data.enforceInterface(DESCRIPTOR);
                    _arg02 = data.readStrongBinder();
                    String _arg18 = data.readString();
                    _arg22 = data.readInt();
                    _arg3 = data.readInt();
                    _arg42 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg54 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        _arg54 = null;
                    }
                    Bundle _result4 = sendWallpaperCommand(_arg02, _arg18, _arg22, _arg3, _arg42, _arg54, data.readInt() != 0);
                    reply.writeNoException();
                    if (_result4 != null) {
                        reply.writeInt(1);
                        _result4.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 26:
                    Bundle _arg19;
                    data.enforceInterface(DESCRIPTOR);
                    _arg02 = data.readStrongBinder();
                    if (data.readInt() != 0) {
                        _arg19 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        _arg19 = null;
                    }
                    wallpaperCommandComplete(_arg02, _arg19);
                    reply.writeNoException();
                    return true;
                case 27:
                    data.enforceInterface(DESCRIPTOR);
                    _arg02 = data.readStrongBinder();
                    if (data.readInt() != 0) {
                        _arg12 = (Rect) Rect.CREATOR.createFromParcel(data);
                    } else {
                        _arg12 = null;
                    }
                    onRectangleOnScreenRequested(_arg02, _arg12);
                    reply.writeNoException();
                    return true;
                case 28:
                    data.enforceInterface(DESCRIPTOR);
                    IWindowId _result5 = getWindowId(data.readStrongBinder());
                    reply.writeNoException();
                    reply.writeStrongBinder(_result5 != null ? _result5.asBinder() : null);
                    return true;
                case 29:
                    data.enforceInterface(DESCRIPTOR);
                    pokeDrawLock(data.readStrongBinder());
                    reply.writeNoException();
                    return true;
                case 30:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = startMovingTask(android.view.IWindow.Stub.asInterface(data.readStrongBinder()), data.readFloat(), data.readFloat());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case 31:
                    data.enforceInterface(DESCRIPTOR);
                    updatePointerIcon(android.view.IWindow.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    int add(IWindow iWindow, int i, LayoutParams layoutParams, int i2, Rect rect, Rect rect2, InputChannel inputChannel) throws RemoteException;

    int addToDisplay(IWindow iWindow, int i, LayoutParams layoutParams, int i2, int i3, Rect rect, Rect rect2, Rect rect3, InputChannel inputChannel) throws RemoteException;

    int addToDisplayWithoutInputChannel(IWindow iWindow, int i, LayoutParams layoutParams, int i2, int i3, Rect rect, Rect rect2) throws RemoteException;

    int addWithoutInputChannel(IWindow iWindow, int i, LayoutParams layoutParams, int i2, Rect rect, Rect rect2) throws RemoteException;

    void cancelDragAndDrop(IBinder iBinder) throws RemoteException;

    void dragRecipientEntered(IWindow iWindow) throws RemoteException;

    void dragRecipientExited(IWindow iWindow) throws RemoteException;

    void finishDrawing(IWindow iWindow) throws RemoteException;

    void getDisplayFrame(IWindow iWindow, Rect rect) throws RemoteException;

    boolean getInTouchMode() throws RemoteException;

    IWindowId getWindowId(IBinder iBinder) throws RemoteException;

    void onRectangleOnScreenRequested(IBinder iBinder, Rect rect) throws RemoteException;

    boolean outOfMemory(IWindow iWindow) throws RemoteException;

    boolean performDrag(IWindow iWindow, IBinder iBinder, int i, float f, float f2, float f3, float f4, ClipData clipData) throws RemoteException;

    boolean performHapticFeedback(IWindow iWindow, int i, boolean z) throws RemoteException;

    void pokeDrawLock(IBinder iBinder) throws RemoteException;

    IBinder prepareDrag(IWindow iWindow, int i, int i2, int i3, Surface surface) throws RemoteException;

    void prepareToReplaceWindows(IBinder iBinder, boolean z) throws RemoteException;

    int relayout(IWindow iWindow, int i, LayoutParams layoutParams, int i2, int i3, int i4, int i5, Rect rect, Rect rect2, Rect rect3, Rect rect4, Rect rect5, Rect rect6, Rect rect7, MergedConfiguration mergedConfiguration, Surface surface) throws RemoteException;

    void remove(IWindow iWindow) throws RemoteException;

    void reportDropResult(IWindow iWindow, boolean z) throws RemoteException;

    Bundle sendWallpaperCommand(IBinder iBinder, String str, int i, int i2, int i3, Bundle bundle, boolean z) throws RemoteException;

    void setInTouchMode(boolean z) throws RemoteException;

    void setInsets(IWindow iWindow, int i, Rect rect, Rect rect2, Region region) throws RemoteException;

    void setTransparentRegion(IWindow iWindow, Region region) throws RemoteException;

    void setWallpaperDisplayOffset(IBinder iBinder, int i, int i2) throws RemoteException;

    void setWallpaperPosition(IBinder iBinder, float f, float f2, float f3, float f4) throws RemoteException;

    boolean startMovingTask(IWindow iWindow, float f, float f2) throws RemoteException;

    void updatePointerIcon(IWindow iWindow) throws RemoteException;

    void wallpaperCommandComplete(IBinder iBinder, Bundle bundle) throws RemoteException;

    void wallpaperOffsetsComplete(IBinder iBinder) throws RemoteException;
}
