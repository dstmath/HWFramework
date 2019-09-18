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
import android.view.DisplayCutout;
import android.view.IWindowId;
import android.view.WindowManager;

public interface IWindowSession extends IInterface {

    public static abstract class Stub extends Binder implements IWindowSession {
        private static final String DESCRIPTOR = "android.view.IWindowSession";
        static final int TRANSACTION_add = 1;
        static final int TRANSACTION_addToDisplay = 2;
        static final int TRANSACTION_addToDisplayWithoutInputChannel = 4;
        static final int TRANSACTION_addWithoutInputChannel = 3;
        static final int TRANSACTION_cancelDragAndDrop = 18;
        static final int TRANSACTION_dragRecipientEntered = 19;
        static final int TRANSACTION_dragRecipientExited = 20;
        static final int TRANSACTION_finishDrawing = 12;
        static final int TRANSACTION_getDisplayFrame = 11;
        static final int TRANSACTION_getInTouchMode = 14;
        static final int TRANSACTION_getWindowId = 27;
        static final int TRANSACTION_onRectangleOnScreenRequested = 26;
        static final int TRANSACTION_outOfMemory = 8;
        static final int TRANSACTION_performDrag = 16;
        static final int TRANSACTION_performHapticFeedback = 15;
        static final int TRANSACTION_pokeDrawLock = 28;
        static final int TRANSACTION_prepareToReplaceWindows = 7;
        static final int TRANSACTION_relayout = 6;
        static final int TRANSACTION_remove = 5;
        static final int TRANSACTION_reportDropResult = 17;
        static final int TRANSACTION_sendWallpaperCommand = 24;
        static final int TRANSACTION_setInTouchMode = 13;
        static final int TRANSACTION_setInsets = 10;
        static final int TRANSACTION_setTransparentRegion = 9;
        static final int TRANSACTION_setWallpaperDisplayOffset = 23;
        static final int TRANSACTION_setWallpaperPosition = 21;
        static final int TRANSACTION_startMovingTask = 29;
        static final int TRANSACTION_updatePointerIcon = 30;
        static final int TRANSACTION_updateTapExcludeRegion = 31;
        static final int TRANSACTION_wallpaperCommandComplete = 25;
        static final int TRANSACTION_wallpaperOffsetsComplete = 22;

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

            public int add(IWindow window, int seq, WindowManager.LayoutParams attrs, int viewVisibility, Rect outContentInsets, Rect outStableInsets, InputChannel outInputChannel) throws RemoteException {
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
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int addToDisplay(IWindow window, int seq, WindowManager.LayoutParams attrs, int viewVisibility, int layerStackId, Rect outFrame, Rect outContentInsets, Rect outStableInsets, Rect outOutsets, DisplayCutout.ParcelableWrapper displayCutout, InputChannel outInputChannel) throws RemoteException {
                WindowManager.LayoutParams layoutParams = attrs;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(window != null ? window.asBinder() : null);
                    try {
                        _data.writeInt(seq);
                        if (layoutParams != null) {
                            _data.writeInt(1);
                            layoutParams.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                    } catch (Throwable th) {
                        th = th;
                        int i = viewVisibility;
                        int i2 = layerStackId;
                        Rect rect = outFrame;
                        Rect rect2 = outContentInsets;
                        Rect rect3 = outStableInsets;
                        Rect rect4 = outOutsets;
                        DisplayCutout.ParcelableWrapper parcelableWrapper = displayCutout;
                        InputChannel inputChannel = outInputChannel;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(viewVisibility);
                        try {
                            _data.writeInt(layerStackId);
                            try {
                                this.mRemote.transact(2, _data, _reply, 0);
                                _reply.readException();
                                int _result = _reply.readInt();
                                if (_reply.readInt() != 0) {
                                    try {
                                        outFrame.readFromParcel(_reply);
                                    } catch (Throwable th2) {
                                        th = th2;
                                        Rect rect22 = outContentInsets;
                                        Rect rect32 = outStableInsets;
                                        Rect rect42 = outOutsets;
                                        DisplayCutout.ParcelableWrapper parcelableWrapper2 = displayCutout;
                                        InputChannel inputChannel2 = outInputChannel;
                                        _reply.recycle();
                                        _data.recycle();
                                        throw th;
                                    }
                                } else {
                                    Rect rect5 = outFrame;
                                }
                                if (_reply.readInt() != 0) {
                                    try {
                                        outContentInsets.readFromParcel(_reply);
                                    } catch (Throwable th3) {
                                        th = th3;
                                        Rect rect322 = outStableInsets;
                                        Rect rect422 = outOutsets;
                                        DisplayCutout.ParcelableWrapper parcelableWrapper22 = displayCutout;
                                        InputChannel inputChannel22 = outInputChannel;
                                        _reply.recycle();
                                        _data.recycle();
                                        throw th;
                                    }
                                } else {
                                    Rect rect6 = outContentInsets;
                                }
                                if (_reply.readInt() != 0) {
                                    try {
                                        outStableInsets.readFromParcel(_reply);
                                    } catch (Throwable th4) {
                                        th = th4;
                                        Rect rect4222 = outOutsets;
                                        DisplayCutout.ParcelableWrapper parcelableWrapper222 = displayCutout;
                                        InputChannel inputChannel222 = outInputChannel;
                                        _reply.recycle();
                                        _data.recycle();
                                        throw th;
                                    }
                                } else {
                                    Rect rect7 = outStableInsets;
                                }
                                if (_reply.readInt() != 0) {
                                    try {
                                        outOutsets.readFromParcel(_reply);
                                    } catch (Throwable th5) {
                                        th = th5;
                                        DisplayCutout.ParcelableWrapper parcelableWrapper2222 = displayCutout;
                                        InputChannel inputChannel2222 = outInputChannel;
                                        _reply.recycle();
                                        _data.recycle();
                                        throw th;
                                    }
                                } else {
                                    Rect rect8 = outOutsets;
                                }
                                if (_reply.readInt() != 0) {
                                    try {
                                        displayCutout.readFromParcel(_reply);
                                    } catch (Throwable th6) {
                                        th = th6;
                                        InputChannel inputChannel22222 = outInputChannel;
                                        _reply.recycle();
                                        _data.recycle();
                                        throw th;
                                    }
                                } else {
                                    DisplayCutout.ParcelableWrapper parcelableWrapper3 = displayCutout;
                                }
                                if (_reply.readInt() != 0) {
                                    try {
                                        outInputChannel.readFromParcel(_reply);
                                    } catch (Throwable th7) {
                                        th = th7;
                                    }
                                } else {
                                    InputChannel inputChannel3 = outInputChannel;
                                }
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            } catch (Throwable th8) {
                                th = th8;
                                Rect rect9 = outFrame;
                                Rect rect222 = outContentInsets;
                                Rect rect3222 = outStableInsets;
                                Rect rect42222 = outOutsets;
                                DisplayCutout.ParcelableWrapper parcelableWrapper22222 = displayCutout;
                                InputChannel inputChannel222222 = outInputChannel;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th9) {
                            th = th9;
                            Rect rect92 = outFrame;
                            Rect rect2222 = outContentInsets;
                            Rect rect32222 = outStableInsets;
                            Rect rect422222 = outOutsets;
                            DisplayCutout.ParcelableWrapper parcelableWrapper222222 = displayCutout;
                            InputChannel inputChannel2222222 = outInputChannel;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th10) {
                        th = th10;
                        int i22 = layerStackId;
                        Rect rect922 = outFrame;
                        Rect rect22222 = outContentInsets;
                        Rect rect322222 = outStableInsets;
                        Rect rect4222222 = outOutsets;
                        DisplayCutout.ParcelableWrapper parcelableWrapper2222222 = displayCutout;
                        InputChannel inputChannel22222222 = outInputChannel;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th11) {
                    th = th11;
                    int i3 = seq;
                    int i4 = viewVisibility;
                    int i222 = layerStackId;
                    Rect rect9222 = outFrame;
                    Rect rect222222 = outContentInsets;
                    Rect rect3222222 = outStableInsets;
                    Rect rect42222222 = outOutsets;
                    DisplayCutout.ParcelableWrapper parcelableWrapper22222222 = displayCutout;
                    InputChannel inputChannel222222222 = outInputChannel;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            public int addWithoutInputChannel(IWindow window, int seq, WindowManager.LayoutParams attrs, int viewVisibility, Rect outContentInsets, Rect outStableInsets) throws RemoteException {
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
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int addToDisplayWithoutInputChannel(IWindow window, int seq, WindowManager.LayoutParams attrs, int viewVisibility, int layerStackId, Rect outContentInsets, Rect outStableInsets) throws RemoteException {
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
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void remove(IWindow window) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(window != null ? window.asBinder() : null);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int relayout(IWindow window, int seq, WindowManager.LayoutParams attrs, int requestedWidth, int requestedHeight, int viewVisibility, int flags, long frameNumber, Rect outFrame, Rect outOverscanInsets, Rect outContentInsets, Rect outVisibleInsets, Rect outStableInsets, Rect outOutsets, Rect outBackdropFrame, DisplayCutout.ParcelableWrapper displayCutout, MergedConfiguration outMergedConfiguration, Surface outSurface) throws RemoteException {
                WindowManager.LayoutParams layoutParams = attrs;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(window != null ? window.asBinder() : null);
                    try {
                        _data.writeInt(seq);
                        if (layoutParams != null) {
                            _data.writeInt(1);
                            layoutParams.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                    } catch (Throwable th) {
                        th = th;
                        int i = requestedWidth;
                        int i2 = requestedHeight;
                        int i3 = viewVisibility;
                        int i4 = flags;
                        long j = frameNumber;
                        Rect rect = outFrame;
                        Rect rect2 = outOverscanInsets;
                        Rect rect3 = outContentInsets;
                        Surface surface = outSurface;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(requestedWidth);
                        try {
                            _data.writeInt(requestedHeight);
                            try {
                                _data.writeInt(viewVisibility);
                                try {
                                    _data.writeInt(flags);
                                } catch (Throwable th2) {
                                    th = th2;
                                    long j2 = frameNumber;
                                    Rect rect4 = outFrame;
                                    Rect rect22 = outOverscanInsets;
                                    Rect rect32 = outContentInsets;
                                    Surface surface2 = outSurface;
                                    _reply.recycle();
                                    _data.recycle();
                                    throw th;
                                }
                            } catch (Throwable th3) {
                                th = th3;
                                int i42 = flags;
                                long j22 = frameNumber;
                                Rect rect42 = outFrame;
                                Rect rect222 = outOverscanInsets;
                                Rect rect322 = outContentInsets;
                                Surface surface22 = outSurface;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th4) {
                            th = th4;
                            int i32 = viewVisibility;
                            int i422 = flags;
                            long j222 = frameNumber;
                            Rect rect422 = outFrame;
                            Rect rect2222 = outOverscanInsets;
                            Rect rect3222 = outContentInsets;
                            Surface surface222 = outSurface;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeLong(frameNumber);
                            try {
                                this.mRemote.transact(6, _data, _reply, 0);
                                _reply.readException();
                                int _result = _reply.readInt();
                                if (_reply.readInt() != 0) {
                                    try {
                                        outFrame.readFromParcel(_reply);
                                    } catch (Throwable th5) {
                                        th = th5;
                                        Rect rect22222 = outOverscanInsets;
                                        Rect rect32222 = outContentInsets;
                                        Surface surface2222 = outSurface;
                                        _reply.recycle();
                                        _data.recycle();
                                        throw th;
                                    }
                                } else {
                                    Rect rect5 = outFrame;
                                }
                                if (_reply.readInt() != 0) {
                                    try {
                                        outOverscanInsets.readFromParcel(_reply);
                                    } catch (Throwable th6) {
                                        th = th6;
                                        Rect rect322222 = outContentInsets;
                                        Surface surface22222 = outSurface;
                                        _reply.recycle();
                                        _data.recycle();
                                        throw th;
                                    }
                                } else {
                                    Rect rect6 = outOverscanInsets;
                                }
                                if (_reply.readInt() != 0) {
                                    try {
                                        outContentInsets.readFromParcel(_reply);
                                    } catch (Throwable th7) {
                                        th = th7;
                                        Surface surface222222 = outSurface;
                                        _reply.recycle();
                                        _data.recycle();
                                        throw th;
                                    }
                                } else {
                                    Rect rect7 = outContentInsets;
                                }
                                if (_reply.readInt() != 0) {
                                    outVisibleInsets.readFromParcel(_reply);
                                } else {
                                    Rect rect8 = outVisibleInsets;
                                }
                                if (_reply.readInt() != 0) {
                                    outStableInsets.readFromParcel(_reply);
                                } else {
                                    Rect rect9 = outStableInsets;
                                }
                                if (_reply.readInt() != 0) {
                                    outOutsets.readFromParcel(_reply);
                                } else {
                                    Rect rect10 = outOutsets;
                                }
                                if (_reply.readInt() != 0) {
                                    outBackdropFrame.readFromParcel(_reply);
                                } else {
                                    Rect rect11 = outBackdropFrame;
                                }
                                if (_reply.readInt() != 0) {
                                    displayCutout.readFromParcel(_reply);
                                } else {
                                    DisplayCutout.ParcelableWrapper parcelableWrapper = displayCutout;
                                }
                                if (_reply.readInt() != 0) {
                                    outMergedConfiguration.readFromParcel(_reply);
                                } else {
                                    MergedConfiguration mergedConfiguration = outMergedConfiguration;
                                }
                                if (_reply.readInt() != 0) {
                                    try {
                                        outSurface.readFromParcel(_reply);
                                    } catch (Throwable th8) {
                                        th = th8;
                                    }
                                } else {
                                    Surface surface3 = outSurface;
                                }
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            } catch (Throwable th9) {
                                th = th9;
                                Rect rect4222 = outFrame;
                                Rect rect222222 = outOverscanInsets;
                                Rect rect3222222 = outContentInsets;
                                Surface surface2222222 = outSurface;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th10) {
                            th = th10;
                            Rect rect42222 = outFrame;
                            Rect rect2222222 = outOverscanInsets;
                            Rect rect32222222 = outContentInsets;
                            Surface surface22222222 = outSurface;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th11) {
                        th = th11;
                        int i22 = requestedHeight;
                        int i322 = viewVisibility;
                        int i4222 = flags;
                        long j2222 = frameNumber;
                        Rect rect422222 = outFrame;
                        Rect rect22222222 = outOverscanInsets;
                        Rect rect322222222 = outContentInsets;
                        Surface surface222222222 = outSurface;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th12) {
                    th = th12;
                    int i5 = seq;
                    int i6 = requestedWidth;
                    int i222 = requestedHeight;
                    int i3222 = viewVisibility;
                    int i42222 = flags;
                    long j22222 = frameNumber;
                    Rect rect4222222 = outFrame;
                    Rect rect222222222 = outOverscanInsets;
                    Rect rect3222222222 = outContentInsets;
                    Surface surface2222222222 = outSurface;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            public void prepareToReplaceWindows(IBinder appToken, boolean childrenOnly) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(appToken);
                    _data.writeInt(childrenOnly);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean outOfMemory(IWindow window) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(window != null ? window.asBinder() : null);
                    boolean _result = false;
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setTransparentRegion(IWindow window, Region region) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(window != null ? window.asBinder() : null);
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
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(window != null ? window.asBinder() : null);
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
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(window != null ? window.asBinder() : null);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        outDisplayFrame.readFromParcel(_reply);
                    }
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void finishDrawing(IWindow window) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(window != null ? window.asBinder() : null);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setInTouchMode(boolean showFocus) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(showFocus);
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
                    boolean _result = false;
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean performHapticFeedback(IWindow window, int effectId, boolean always) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(window != null ? window.asBinder() : null);
                    _data.writeInt(effectId);
                    _data.writeInt(always);
                    boolean _result = false;
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IBinder performDrag(IWindow window, int flags, SurfaceControl surface, int touchSource, float touchX, float touchY, float thumbCenterX, float thumbCenterY, ClipData data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(window != null ? window.asBinder() : null);
                    _data.writeInt(flags);
                    if (surface != null) {
                        _data.writeInt(1);
                        surface.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
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
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readStrongBinder();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void reportDropResult(IWindow window, boolean consumed) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(window != null ? window.asBinder() : null);
                    _data.writeInt(consumed);
                    this.mRemote.transact(17, _data, _reply, 0);
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
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void dragRecipientEntered(IWindow window) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(window != null ? window.asBinder() : null);
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void dragRecipientExited(IWindow window) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(window != null ? window.asBinder() : null);
                    this.mRemote.transact(20, _data, _reply, 0);
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
                    this.mRemote.transact(21, _data, _reply, 0);
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
                    this.mRemote.transact(22, _data, _reply, 0);
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
                    this.mRemote.transact(23, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Bundle sendWallpaperCommand(IBinder window, String action, int x, int y, int z, Bundle extras, boolean sync) throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
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
                    _data.writeInt(sync);
                    this.mRemote.transact(24, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Bundle.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
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
                    this.mRemote.transact(25, _data, _reply, 0);
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
                    this.mRemote.transact(26, _data, _reply, 0);
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
                    this.mRemote.transact(27, _data, _reply, 0);
                    _reply.readException();
                    return IWindowId.Stub.asInterface(_reply.readStrongBinder());
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
                    this.mRemote.transact(28, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean startMovingTask(IWindow window, float startX, float startY) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(window != null ? window.asBinder() : null);
                    _data.writeFloat(startX);
                    _data.writeFloat(startY);
                    boolean _result = false;
                    this.mRemote.transact(29, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void updatePointerIcon(IWindow window) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(window != null ? window.asBinder() : null);
                    this.mRemote.transact(30, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void updateTapExcludeRegion(IWindow window, int regionId, int left, int top, int width, int height) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(window != null ? window.asBinder() : null);
                    _data.writeInt(regionId);
                    _data.writeInt(left);
                    _data.writeInt(top);
                    _data.writeInt(width);
                    _data.writeInt(height);
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

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v59, resolved type: android.graphics.Region} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v100, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v104, resolved type: android.graphics.Rect} */
        /* JADX WARNING: type inference failed for: r0v1 */
        /* JADX WARNING: type inference failed for: r0v2 */
        /* JADX WARNING: type inference failed for: r0v11 */
        /* JADX WARNING: type inference failed for: r0v23 */
        /* JADX WARNING: type inference failed for: r0v31 */
        /* JADX WARNING: type inference failed for: r0v41 */
        /* JADX WARNING: type inference failed for: r0v63 */
        /* JADX WARNING: type inference failed for: r0v77 */
        /* JADX WARNING: type inference failed for: r0v93 */
        /* JADX WARNING: type inference failed for: r0v108, types: [android.os.IBinder] */
        /* JADX WARNING: type inference failed for: r0v118 */
        /* JADX WARNING: type inference failed for: r0v119 */
        /* JADX WARNING: type inference failed for: r0v120 */
        /* JADX WARNING: type inference failed for: r0v121 */
        /* JADX WARNING: type inference failed for: r0v122 */
        /* JADX WARNING: type inference failed for: r0v123 */
        /* JADX WARNING: type inference failed for: r0v124 */
        /* JADX WARNING: type inference failed for: r0v125 */
        /* JADX WARNING: type inference failed for: r0v126 */
        /* JADX WARNING: type inference failed for: r0v127 */
        /* JADX WARNING: type inference failed for: r0v128 */
        /* JADX WARNING: type inference failed for: r0v129 */
        /*  JADX ERROR: NullPointerException in pass: CodeShrinkVisitor
            java.lang.NullPointerException
            */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int r52, android.os.Parcel r53, android.os.Parcel r54, int r55) throws android.os.RemoteException {
            /*
                r51 = this;
                r15 = r51
                r14 = r52
                r13 = r53
                r12 = r54
                java.lang.String r11 = "android.view.IWindowSession"
                r0 = 1598968902(0x5f4e5446, float:1.4867585E19)
                r10 = 1
                if (r14 == r0) goto L_0x0633
                r8 = 0
                r0 = 0
                switch(r14) {
                    case 1: goto L_0x05c9;
                    case 2: goto L_0x0518;
                    case 3: goto L_0x04bf;
                    case 4: goto L_0x045a;
                    case 5: goto L_0x043f;
                    case 6: goto L_0x0330;
                    case 7: goto L_0x0319;
                    case 8: goto L_0x0303;
                    case 9: goto L_0x02e1;
                    case 10: goto L_0x0295;
                    case 11: goto L_0x0277;
                    case 12: goto L_0x0265;
                    case 13: goto L_0x0252;
                    case 14: goto L_0x0244;
                    case 15: goto L_0x0221;
                    case 16: goto L_0x01c2;
                    case 17: goto L_0x01a7;
                    case 18: goto L_0x0199;
                    case 19: goto L_0x0187;
                    case 20: goto L_0x0175;
                    case 21: goto L_0x0150;
                    case 22: goto L_0x0142;
                    case 23: goto L_0x012c;
                    case 24: goto L_0x00dd;
                    case 25: goto L_0x00bf;
                    case 26: goto L_0x00a1;
                    case 27: goto L_0x0088;
                    case 28: goto L_0x007a;
                    case 29: goto L_0x005c;
                    case 30: goto L_0x004a;
                    case 31: goto L_0x001a;
                    default: goto L_0x0015;
                }
            L_0x0015:
                boolean r0 = super.onTransact(r52, r53, r54, r55)
                return r0
            L_0x001a:
                r13.enforceInterface(r11)
                android.os.IBinder r0 = r53.readStrongBinder()
                android.view.IWindow r7 = android.view.IWindow.Stub.asInterface(r0)
                int r8 = r53.readInt()
                int r9 = r53.readInt()
                int r16 = r53.readInt()
                int r17 = r53.readInt()
                int r18 = r53.readInt()
                r0 = r15
                r1 = r7
                r2 = r8
                r3 = r9
                r4 = r16
                r5 = r17
                r6 = r18
                r0.updateTapExcludeRegion(r1, r2, r3, r4, r5, r6)
                r54.writeNoException()
                return r10
            L_0x004a:
                r13.enforceInterface(r11)
                android.os.IBinder r0 = r53.readStrongBinder()
                android.view.IWindow r0 = android.view.IWindow.Stub.asInterface(r0)
                r15.updatePointerIcon(r0)
                r54.writeNoException()
                return r10
            L_0x005c:
                r13.enforceInterface(r11)
                android.os.IBinder r0 = r53.readStrongBinder()
                android.view.IWindow r0 = android.view.IWindow.Stub.asInterface(r0)
                float r1 = r53.readFloat()
                float r2 = r53.readFloat()
                boolean r3 = r15.startMovingTask(r0, r1, r2)
                r54.writeNoException()
                r12.writeInt(r3)
                return r10
            L_0x007a:
                r13.enforceInterface(r11)
                android.os.IBinder r0 = r53.readStrongBinder()
                r15.pokeDrawLock(r0)
                r54.writeNoException()
                return r10
            L_0x0088:
                r13.enforceInterface(r11)
                android.os.IBinder r1 = r53.readStrongBinder()
                android.view.IWindowId r2 = r15.getWindowId(r1)
                r54.writeNoException()
                if (r2 == 0) goto L_0x009d
                android.os.IBinder r0 = r2.asBinder()
            L_0x009d:
                r12.writeStrongBinder(r0)
                return r10
            L_0x00a1:
                r13.enforceInterface(r11)
                android.os.IBinder r1 = r53.readStrongBinder()
                int r2 = r53.readInt()
                if (r2 == 0) goto L_0x00b7
                android.os.Parcelable$Creator r0 = android.graphics.Rect.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r13)
                android.graphics.Rect r0 = (android.graphics.Rect) r0
                goto L_0x00b8
            L_0x00b7:
            L_0x00b8:
                r15.onRectangleOnScreenRequested(r1, r0)
                r54.writeNoException()
                return r10
            L_0x00bf:
                r13.enforceInterface(r11)
                android.os.IBinder r1 = r53.readStrongBinder()
                int r2 = r53.readInt()
                if (r2 == 0) goto L_0x00d5
                android.os.Parcelable$Creator<android.os.Bundle> r0 = android.os.Bundle.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r13)
                android.os.Bundle r0 = (android.os.Bundle) r0
                goto L_0x00d6
            L_0x00d5:
            L_0x00d6:
                r15.wallpaperCommandComplete(r1, r0)
                r54.writeNoException()
                return r10
            L_0x00dd:
                r13.enforceInterface(r11)
                android.os.IBinder r9 = r53.readStrongBinder()
                java.lang.String r16 = r53.readString()
                int r17 = r53.readInt()
                int r18 = r53.readInt()
                int r19 = r53.readInt()
                int r1 = r53.readInt()
                if (r1 == 0) goto L_0x0104
                android.os.Parcelable$Creator<android.os.Bundle> r0 = android.os.Bundle.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r13)
                android.os.Bundle r0 = (android.os.Bundle) r0
            L_0x0102:
                r6 = r0
                goto L_0x0105
            L_0x0104:
                goto L_0x0102
            L_0x0105:
                int r0 = r53.readInt()
                if (r0 == 0) goto L_0x010d
                r7 = r10
                goto L_0x010e
            L_0x010d:
                r7 = r8
            L_0x010e:
                r0 = r15
                r1 = r9
                r2 = r16
                r3 = r17
                r4 = r18
                r5 = r19
                android.os.Bundle r0 = r0.sendWallpaperCommand(r1, r2, r3, r4, r5, r6, r7)
                r54.writeNoException()
                if (r0 == 0) goto L_0x0128
                r12.writeInt(r10)
                r0.writeToParcel(r12, r10)
                goto L_0x012b
            L_0x0128:
                r12.writeInt(r8)
            L_0x012b:
                return r10
            L_0x012c:
                r13.enforceInterface(r11)
                android.os.IBinder r0 = r53.readStrongBinder()
                int r1 = r53.readInt()
                int r2 = r53.readInt()
                r15.setWallpaperDisplayOffset(r0, r1, r2)
                r54.writeNoException()
                return r10
            L_0x0142:
                r13.enforceInterface(r11)
                android.os.IBinder r0 = r53.readStrongBinder()
                r15.wallpaperOffsetsComplete(r0)
                r54.writeNoException()
                return r10
            L_0x0150:
                r13.enforceInterface(r11)
                android.os.IBinder r6 = r53.readStrongBinder()
                float r7 = r53.readFloat()
                float r8 = r53.readFloat()
                float r9 = r53.readFloat()
                float r16 = r53.readFloat()
                r0 = r15
                r1 = r6
                r2 = r7
                r3 = r8
                r4 = r9
                r5 = r16
                r0.setWallpaperPosition(r1, r2, r3, r4, r5)
                r54.writeNoException()
                return r10
            L_0x0175:
                r13.enforceInterface(r11)
                android.os.IBinder r0 = r53.readStrongBinder()
                android.view.IWindow r0 = android.view.IWindow.Stub.asInterface(r0)
                r15.dragRecipientExited(r0)
                r54.writeNoException()
                return r10
            L_0x0187:
                r13.enforceInterface(r11)
                android.os.IBinder r0 = r53.readStrongBinder()
                android.view.IWindow r0 = android.view.IWindow.Stub.asInterface(r0)
                r15.dragRecipientEntered(r0)
                r54.writeNoException()
                return r10
            L_0x0199:
                r13.enforceInterface(r11)
                android.os.IBinder r0 = r53.readStrongBinder()
                r15.cancelDragAndDrop(r0)
                r54.writeNoException()
                return r10
            L_0x01a7:
                r13.enforceInterface(r11)
                android.os.IBinder r0 = r53.readStrongBinder()
                android.view.IWindow r0 = android.view.IWindow.Stub.asInterface(r0)
                int r1 = r53.readInt()
                if (r1 == 0) goto L_0x01ba
                r8 = r10
            L_0x01ba:
                r1 = r8
                r15.reportDropResult(r0, r1)
                r54.writeNoException()
                return r10
            L_0x01c2:
                r13.enforceInterface(r11)
                android.os.IBinder r1 = r53.readStrongBinder()
                android.view.IWindow r16 = android.view.IWindow.Stub.asInterface(r1)
                int r17 = r53.readInt()
                int r1 = r53.readInt()
                if (r1 == 0) goto L_0x01e1
                android.os.Parcelable$Creator<android.view.SurfaceControl> r1 = android.view.SurfaceControl.CREATOR
                java.lang.Object r1 = r1.createFromParcel(r13)
                android.view.SurfaceControl r1 = (android.view.SurfaceControl) r1
                r3 = r1
                goto L_0x01e2
            L_0x01e1:
                r3 = r0
            L_0x01e2:
                int r18 = r53.readInt()
                float r19 = r53.readFloat()
                float r20 = r53.readFloat()
                float r21 = r53.readFloat()
                float r22 = r53.readFloat()
                int r1 = r53.readInt()
                if (r1 == 0) goto L_0x0206
                android.os.Parcelable$Creator r0 = android.content.ClipData.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r13)
                android.content.ClipData r0 = (android.content.ClipData) r0
            L_0x0204:
                r9 = r0
                goto L_0x0207
            L_0x0206:
                goto L_0x0204
            L_0x0207:
                r0 = r15
                r1 = r16
                r2 = r17
                r4 = r18
                r5 = r19
                r6 = r20
                r7 = r21
                r8 = r22
                android.os.IBinder r0 = r0.performDrag(r1, r2, r3, r4, r5, r6, r7, r8, r9)
                r54.writeNoException()
                r12.writeStrongBinder(r0)
                return r10
            L_0x0221:
                r13.enforceInterface(r11)
                android.os.IBinder r0 = r53.readStrongBinder()
                android.view.IWindow r0 = android.view.IWindow.Stub.asInterface(r0)
                int r1 = r53.readInt()
                int r2 = r53.readInt()
                if (r2 == 0) goto L_0x0238
                r8 = r10
            L_0x0238:
                r2 = r8
                boolean r3 = r15.performHapticFeedback(r0, r1, r2)
                r54.writeNoException()
                r12.writeInt(r3)
                return r10
            L_0x0244:
                r13.enforceInterface(r11)
                boolean r0 = r51.getInTouchMode()
                r54.writeNoException()
                r12.writeInt(r0)
                return r10
            L_0x0252:
                r13.enforceInterface(r11)
                int r0 = r53.readInt()
                if (r0 == 0) goto L_0x025d
                r8 = r10
            L_0x025d:
                r0 = r8
                r15.setInTouchMode(r0)
                r54.writeNoException()
                return r10
            L_0x0265:
                r13.enforceInterface(r11)
                android.os.IBinder r0 = r53.readStrongBinder()
                android.view.IWindow r0 = android.view.IWindow.Stub.asInterface(r0)
                r15.finishDrawing(r0)
                r54.writeNoException()
                return r10
            L_0x0277:
                r13.enforceInterface(r11)
                android.os.IBinder r0 = r53.readStrongBinder()
                android.view.IWindow r0 = android.view.IWindow.Stub.asInterface(r0)
                android.graphics.Rect r1 = new android.graphics.Rect
                r1.<init>()
                r15.getDisplayFrame(r0, r1)
                r54.writeNoException()
                r12.writeInt(r10)
                r1.writeToParcel(r12, r10)
                return r10
            L_0x0295:
                r13.enforceInterface(r11)
                android.os.IBinder r1 = r53.readStrongBinder()
                android.view.IWindow r6 = android.view.IWindow.Stub.asInterface(r1)
                int r7 = r53.readInt()
                int r1 = r53.readInt()
                if (r1 == 0) goto L_0x02b4
                android.os.Parcelable$Creator r1 = android.graphics.Rect.CREATOR
                java.lang.Object r1 = r1.createFromParcel(r13)
                android.graphics.Rect r1 = (android.graphics.Rect) r1
                r3 = r1
                goto L_0x02b5
            L_0x02b4:
                r3 = r0
            L_0x02b5:
                int r1 = r53.readInt()
                if (r1 == 0) goto L_0x02c5
                android.os.Parcelable$Creator r1 = android.graphics.Rect.CREATOR
                java.lang.Object r1 = r1.createFromParcel(r13)
                android.graphics.Rect r1 = (android.graphics.Rect) r1
                r4 = r1
                goto L_0x02c6
            L_0x02c5:
                r4 = r0
            L_0x02c6:
                int r1 = r53.readInt()
                if (r1 == 0) goto L_0x02d6
                android.os.Parcelable$Creator r0 = android.graphics.Region.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r13)
                android.graphics.Region r0 = (android.graphics.Region) r0
            L_0x02d4:
                r5 = r0
                goto L_0x02d7
            L_0x02d6:
                goto L_0x02d4
            L_0x02d7:
                r0 = r15
                r1 = r6
                r2 = r7
                r0.setInsets(r1, r2, r3, r4, r5)
                r54.writeNoException()
                return r10
            L_0x02e1:
                r13.enforceInterface(r11)
                android.os.IBinder r1 = r53.readStrongBinder()
                android.view.IWindow r1 = android.view.IWindow.Stub.asInterface(r1)
                int r2 = r53.readInt()
                if (r2 == 0) goto L_0x02fb
                android.os.Parcelable$Creator r0 = android.graphics.Region.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r13)
                android.graphics.Region r0 = (android.graphics.Region) r0
                goto L_0x02fc
            L_0x02fb:
            L_0x02fc:
                r15.setTransparentRegion(r1, r0)
                r54.writeNoException()
                return r10
            L_0x0303:
                r13.enforceInterface(r11)
                android.os.IBinder r0 = r53.readStrongBinder()
                android.view.IWindow r0 = android.view.IWindow.Stub.asInterface(r0)
                boolean r1 = r15.outOfMemory(r0)
                r54.writeNoException()
                r12.writeInt(r1)
                return r10
            L_0x0319:
                r13.enforceInterface(r11)
                android.os.IBinder r0 = r53.readStrongBinder()
                int r1 = r53.readInt()
                if (r1 == 0) goto L_0x0328
                r8 = r10
            L_0x0328:
                r1 = r8
                r15.prepareToReplaceWindows(r0, r1)
                r54.writeNoException()
                return r10
            L_0x0330:
                r13.enforceInterface(r11)
                android.os.IBinder r1 = r53.readStrongBinder()
                android.view.IWindow r20 = android.view.IWindow.Stub.asInterface(r1)
                int r21 = r53.readInt()
                int r1 = r53.readInt()
                if (r1 == 0) goto L_0x034f
                android.os.Parcelable$Creator<android.view.WindowManager$LayoutParams> r0 = android.view.WindowManager.LayoutParams.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r13)
                android.view.WindowManager$LayoutParams r0 = (android.view.WindowManager.LayoutParams) r0
            L_0x034d:
                r3 = r0
                goto L_0x0350
            L_0x034f:
                goto L_0x034d
            L_0x0350:
                int r22 = r53.readInt()
                int r23 = r53.readInt()
                int r24 = r53.readInt()
                int r25 = r53.readInt()
                long r26 = r53.readLong()
                android.graphics.Rect r0 = new android.graphics.Rect
                r0.<init>()
                r8 = r0
                android.graphics.Rect r0 = new android.graphics.Rect
                r0.<init>()
                r9 = r0
                android.graphics.Rect r0 = new android.graphics.Rect
                r0.<init>()
                r7 = r0
                android.graphics.Rect r0 = new android.graphics.Rect
                r0.<init>()
                r6 = r0
                android.graphics.Rect r0 = new android.graphics.Rect
                r0.<init>()
                r5 = r0
                android.graphics.Rect r0 = new android.graphics.Rect
                r0.<init>()
                r4 = r0
                android.graphics.Rect r0 = new android.graphics.Rect
                r0.<init>()
                r2 = r0
                android.view.DisplayCutout$ParcelableWrapper r0 = new android.view.DisplayCutout$ParcelableWrapper
                r0.<init>()
                r1 = r0
                android.util.MergedConfiguration r0 = new android.util.MergedConfiguration
                r0.<init>()
                android.view.Surface r10 = new android.view.Surface
                r10.<init>()
                r28 = r0
                r0 = r15
                r29 = r1
                r1 = r20
                r30 = r2
                r2 = r21
                r31 = r4
                r4 = r22
                r32 = r5
                r5 = r23
                r33 = r6
                r6 = r24
                r34 = r7
                r7 = r25
                r35 = r8
                r36 = r9
                r8 = r26
                r37 = r10
                r10 = r35
                r38 = r11
                r11 = r36
                r12 = r34
                r13 = r33
                r14 = r32
                r15 = r31
                r16 = r30
                r17 = r29
                r18 = r28
                r19 = r37
                int r0 = r0.relayout(r1, r2, r3, r4, r5, r6, r7, r8, r10, r11, r12, r13, r14, r15, r16, r17, r18, r19)
                r54.writeNoException()
                r12 = r54
                r12.writeInt(r0)
                r13 = 1
                r12.writeInt(r13)
                r1 = r35
                r1.writeToParcel(r12, r13)
                r12.writeInt(r13)
                r2 = r36
                r2.writeToParcel(r12, r13)
                r12.writeInt(r13)
                r4 = r34
                r4.writeToParcel(r12, r13)
                r12.writeInt(r13)
                r5 = r33
                r5.writeToParcel(r12, r13)
                r12.writeInt(r13)
                r6 = r32
                r6.writeToParcel(r12, r13)
                r12.writeInt(r13)
                r7 = r31
                r7.writeToParcel(r12, r13)
                r12.writeInt(r13)
                r8 = r30
                r8.writeToParcel(r12, r13)
                r12.writeInt(r13)
                r9 = r29
                r9.writeToParcel(r12, r13)
                r12.writeInt(r13)
                r10 = r28
                r10.writeToParcel(r12, r13)
                r12.writeInt(r13)
                r11 = r37
                r11.writeToParcel(r12, r13)
                return r13
            L_0x043f:
                r13 = r10
                r38 = r11
                r15 = r38
                r14 = r53
                r14.enforceInterface(r15)
                android.os.IBinder r0 = r53.readStrongBinder()
                android.view.IWindow r0 = android.view.IWindow.Stub.asInterface(r0)
                r11 = r51
                r11.remove(r0)
                r54.writeNoException()
                return r13
            L_0x045a:
                r14 = r13
                r13 = r10
                r50 = r15
                r15 = r11
                r11 = r50
                r14.enforceInterface(r15)
                android.os.IBinder r1 = r53.readStrongBinder()
                android.view.IWindow r8 = android.view.IWindow.Stub.asInterface(r1)
                int r9 = r53.readInt()
                int r1 = r53.readInt()
                if (r1 == 0) goto L_0x0480
                android.os.Parcelable$Creator<android.view.WindowManager$LayoutParams> r0 = android.view.WindowManager.LayoutParams.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r14)
                android.view.WindowManager$LayoutParams r0 = (android.view.WindowManager.LayoutParams) r0
            L_0x047e:
                r3 = r0
                goto L_0x0481
            L_0x0480:
                goto L_0x047e
            L_0x0481:
                int r10 = r53.readInt()
                int r16 = r53.readInt()
                android.graphics.Rect r0 = new android.graphics.Rect
                r0.<init>()
                r7 = r0
                android.graphics.Rect r0 = new android.graphics.Rect
                r0.<init>()
                r6 = r0
                r0 = r11
                r1 = r8
                r2 = r9
                r4 = r10
                r5 = r16
                r39 = r6
                r6 = r7
                r40 = r7
                r7 = r39
                int r0 = r0.addToDisplayWithoutInputChannel(r1, r2, r3, r4, r5, r6, r7)
                r54.writeNoException()
                r12.writeInt(r0)
                r12.writeInt(r13)
                r1 = r40
                r1.writeToParcel(r12, r13)
                r12.writeInt(r13)
                r2 = r39
                r2.writeToParcel(r12, r13)
                return r13
            L_0x04bf:
                r14 = r13
                r13 = r10
                r50 = r15
                r15 = r11
                r11 = r50
                r14.enforceInterface(r15)
                android.os.IBinder r1 = r53.readStrongBinder()
                android.view.IWindow r7 = android.view.IWindow.Stub.asInterface(r1)
                int r8 = r53.readInt()
                int r1 = r53.readInt()
                if (r1 == 0) goto L_0x04e5
                android.os.Parcelable$Creator<android.view.WindowManager$LayoutParams> r0 = android.view.WindowManager.LayoutParams.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r14)
                android.view.WindowManager$LayoutParams r0 = (android.view.WindowManager.LayoutParams) r0
            L_0x04e3:
                r3 = r0
                goto L_0x04e6
            L_0x04e5:
                goto L_0x04e3
            L_0x04e6:
                int r9 = r53.readInt()
                android.graphics.Rect r0 = new android.graphics.Rect
                r0.<init>()
                r10 = r0
                android.graphics.Rect r0 = new android.graphics.Rect
                r0.<init>()
                r6 = r0
                r0 = r11
                r1 = r7
                r2 = r8
                r4 = r9
                r5 = r10
                r41 = r6
                int r0 = r0.addWithoutInputChannel(r1, r2, r3, r4, r5, r6)
                r54.writeNoException()
                r12.writeInt(r0)
                r12.writeInt(r13)
                r10.writeToParcel(r12, r13)
                r12.writeInt(r13)
                r1 = r41
                r1.writeToParcel(r12, r13)
                return r13
            L_0x0518:
                r14 = r13
                r13 = r10
                r50 = r15
                r15 = r11
                r11 = r50
                r14.enforceInterface(r15)
                android.os.IBinder r1 = r53.readStrongBinder()
                android.view.IWindow r16 = android.view.IWindow.Stub.asInterface(r1)
                int r17 = r53.readInt()
                int r1 = r53.readInt()
                if (r1 == 0) goto L_0x053e
                android.os.Parcelable$Creator<android.view.WindowManager$LayoutParams> r0 = android.view.WindowManager.LayoutParams.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r14)
                android.view.WindowManager$LayoutParams r0 = (android.view.WindowManager.LayoutParams) r0
            L_0x053c:
                r3 = r0
                goto L_0x053f
            L_0x053e:
                goto L_0x053c
            L_0x053f:
                int r18 = r53.readInt()
                int r19 = r53.readInt()
                android.graphics.Rect r0 = new android.graphics.Rect
                r0.<init>()
                r10 = r0
                android.graphics.Rect r0 = new android.graphics.Rect
                r0.<init>()
                r9 = r0
                android.graphics.Rect r0 = new android.graphics.Rect
                r0.<init>()
                r8 = r0
                android.graphics.Rect r0 = new android.graphics.Rect
                r0.<init>()
                r7 = r0
                android.view.DisplayCutout$ParcelableWrapper r0 = new android.view.DisplayCutout$ParcelableWrapper
                r0.<init>()
                r6 = r0
                android.view.InputChannel r0 = new android.view.InputChannel
                r0.<init>()
                r5 = r0
                r0 = r11
                r1 = r16
                r2 = r17
                r4 = r18
                r42 = r5
                r5 = r19
                r43 = r6
                r6 = r10
                r44 = r7
                r7 = r9
                r45 = r8
                r46 = r9
                r9 = r44
                r47 = r10
                r10 = r43
                r11 = r42
                int r0 = r0.addToDisplay(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11)
                r54.writeNoException()
                r12.writeInt(r0)
                r12.writeInt(r13)
                r1 = r47
                r1.writeToParcel(r12, r13)
                r12.writeInt(r13)
                r2 = r46
                r2.writeToParcel(r12, r13)
                r12.writeInt(r13)
                r4 = r45
                r4.writeToParcel(r12, r13)
                r12.writeInt(r13)
                r5 = r44
                r5.writeToParcel(r12, r13)
                r12.writeInt(r13)
                r6 = r43
                r6.writeToParcel(r12, r13)
                r12.writeInt(r13)
                r7 = r42
                r7.writeToParcel(r12, r13)
                return r13
            L_0x05c9:
                r15 = r11
                r14 = r13
                r13 = r10
                r14.enforceInterface(r15)
                android.os.IBinder r1 = r53.readStrongBinder()
                android.view.IWindow r8 = android.view.IWindow.Stub.asInterface(r1)
                int r9 = r53.readInt()
                int r1 = r53.readInt()
                if (r1 == 0) goto L_0x05eb
                android.os.Parcelable$Creator<android.view.WindowManager$LayoutParams> r0 = android.view.WindowManager.LayoutParams.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r14)
                android.view.WindowManager$LayoutParams r0 = (android.view.WindowManager.LayoutParams) r0
            L_0x05e9:
                r3 = r0
                goto L_0x05ec
            L_0x05eb:
                goto L_0x05e9
            L_0x05ec:
                int r10 = r53.readInt()
                android.graphics.Rect r0 = new android.graphics.Rect
                r0.<init>()
                r11 = r0
                android.graphics.Rect r0 = new android.graphics.Rect
                r0.<init>()
                r7 = r0
                android.view.InputChannel r0 = new android.view.InputChannel
                r0.<init>()
                r6 = r0
                r0 = r51
                r1 = r8
                r2 = r9
                r4 = r10
                r5 = r11
                r48 = r6
                r6 = r7
                r49 = r7
                r7 = r48
                int r0 = r0.add(r1, r2, r3, r4, r5, r6, r7)
                r54.writeNoException()
                r12.writeInt(r0)
                r12.writeInt(r13)
                r11.writeToParcel(r12, r13)
                r12.writeInt(r13)
                r1 = r49
                r1.writeToParcel(r12, r13)
                r12.writeInt(r13)
                r2 = r48
                r2.writeToParcel(r12, r13)
                return r13
            L_0x0633:
                r15 = r11
                r14 = r13
                r13 = r10
                r12.writeString(r15)
                return r13
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.IWindowSession.Stub.onTransact(int, android.os.Parcel, android.os.Parcel, int):boolean");
        }
    }

    int add(IWindow iWindow, int i, WindowManager.LayoutParams layoutParams, int i2, Rect rect, Rect rect2, InputChannel inputChannel) throws RemoteException;

    int addToDisplay(IWindow iWindow, int i, WindowManager.LayoutParams layoutParams, int i2, int i3, Rect rect, Rect rect2, Rect rect3, Rect rect4, DisplayCutout.ParcelableWrapper parcelableWrapper, InputChannel inputChannel) throws RemoteException;

    int addToDisplayWithoutInputChannel(IWindow iWindow, int i, WindowManager.LayoutParams layoutParams, int i2, int i3, Rect rect, Rect rect2) throws RemoteException;

    int addWithoutInputChannel(IWindow iWindow, int i, WindowManager.LayoutParams layoutParams, int i2, Rect rect, Rect rect2) throws RemoteException;

    void cancelDragAndDrop(IBinder iBinder) throws RemoteException;

    void dragRecipientEntered(IWindow iWindow) throws RemoteException;

    void dragRecipientExited(IWindow iWindow) throws RemoteException;

    void finishDrawing(IWindow iWindow) throws RemoteException;

    void getDisplayFrame(IWindow iWindow, Rect rect) throws RemoteException;

    boolean getInTouchMode() throws RemoteException;

    IWindowId getWindowId(IBinder iBinder) throws RemoteException;

    void onRectangleOnScreenRequested(IBinder iBinder, Rect rect) throws RemoteException;

    boolean outOfMemory(IWindow iWindow) throws RemoteException;

    IBinder performDrag(IWindow iWindow, int i, SurfaceControl surfaceControl, int i2, float f, float f2, float f3, float f4, ClipData clipData) throws RemoteException;

    boolean performHapticFeedback(IWindow iWindow, int i, boolean z) throws RemoteException;

    void pokeDrawLock(IBinder iBinder) throws RemoteException;

    void prepareToReplaceWindows(IBinder iBinder, boolean z) throws RemoteException;

    int relayout(IWindow iWindow, int i, WindowManager.LayoutParams layoutParams, int i2, int i3, int i4, int i5, long j, Rect rect, Rect rect2, Rect rect3, Rect rect4, Rect rect5, Rect rect6, Rect rect7, DisplayCutout.ParcelableWrapper parcelableWrapper, MergedConfiguration mergedConfiguration, Surface surface) throws RemoteException;

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

    void updateTapExcludeRegion(IWindow iWindow, int i, int i2, int i3, int i4, int i5) throws RemoteException;

    void wallpaperCommandComplete(IBinder iBinder, Bundle bundle) throws RemoteException;

    void wallpaperOffsetsComplete(IBinder iBinder) throws RemoteException;
}
