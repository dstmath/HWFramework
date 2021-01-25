package android.view;

import android.annotation.UnsupportedAppUsage;
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
import android.view.IWindow;
import android.view.IWindowId;
import android.view.WindowManager;
import java.util.List;

public interface IWindowSession extends IInterface {
    int addToDisplay(IWindow iWindow, int i, WindowManager.LayoutParams layoutParams, int i2, int i3, Rect rect, Rect rect2, Rect rect3, Rect rect4, DisplayCutout.ParcelableWrapper parcelableWrapper, InputChannel inputChannel, InsetsState insetsState) throws RemoteException;

    int addToDisplayWithoutInputChannel(IWindow iWindow, int i, WindowManager.LayoutParams layoutParams, int i2, int i3, Rect rect, Rect rect2, InsetsState insetsState) throws RemoteException;

    void cancelDragAndDrop(IBinder iBinder, boolean z) throws RemoteException;

    void dragRecipientEntered(IWindow iWindow) throws RemoteException;

    void dragRecipientExited(IWindow iWindow) throws RemoteException;

    void dragRecipientFeedback(IWindow iWindow, boolean z, int i, int i2) throws RemoteException;

    @UnsupportedAppUsage
    void finishDrawing(IWindow iWindow) throws RemoteException;

    void finishMovingTask(IWindow iWindow) throws RemoteException;

    void getDisplayFrame(IWindow iWindow, Rect rect) throws RemoteException;

    @UnsupportedAppUsage
    boolean getInTouchMode() throws RemoteException;

    IWindowId getWindowId(IBinder iBinder) throws RemoteException;

    void insetsModified(IWindow iWindow, InsetsState insetsState) throws RemoteException;

    void onRectangleOnScreenRequested(IBinder iBinder, Rect rect) throws RemoteException;

    boolean outOfMemory(IWindow iWindow) throws RemoteException;

    @UnsupportedAppUsage
    IBinder performDrag(IWindow iWindow, int i, SurfaceControl surfaceControl, int i2, float f, float f2, float f3, float f4, ClipData clipData) throws RemoteException;

    @UnsupportedAppUsage
    boolean performHapticFeedback(int i, boolean z) throws RemoteException;

    void pokeDrawLock(IBinder iBinder) throws RemoteException;

    void prepareToReplaceWindows(IBinder iBinder, boolean z) throws RemoteException;

    int relayout(IWindow iWindow, int i, WindowManager.LayoutParams layoutParams, int i2, int i3, int i4, int i5, long j, Rect rect, Rect rect2, Rect rect3, Rect rect4, Rect rect5, Rect rect6, Rect rect7, DisplayCutout.ParcelableWrapper parcelableWrapper, MergedConfiguration mergedConfiguration, SurfaceControl surfaceControl, InsetsState insetsState) throws RemoteException;

    @UnsupportedAppUsage
    void remove(IWindow iWindow) throws RemoteException;

    void reparentDisplayContent(IWindow iWindow, SurfaceControl surfaceControl, int i) throws RemoteException;

    void reportDropResult(IWindow iWindow, boolean z) throws RemoteException;

    void reportSystemGestureExclusionChanged(IWindow iWindow, List<Rect> list) throws RemoteException;

    Bundle sendWallpaperCommand(IBinder iBinder, String str, int i, int i2, int i3, Bundle bundle, boolean z) throws RemoteException;

    @UnsupportedAppUsage
    void setInTouchMode(boolean z) throws RemoteException;

    void setInsets(IWindow iWindow, int i, Rect rect, Rect rect2, Region region) throws RemoteException;

    void setOriginalDragViewCenter(IWindow iWindow, int i, int i2) throws RemoteException;

    void setPendingDragEndedLoc(IWindow iWindow, int i, int i2) throws RemoteException;

    @UnsupportedAppUsage
    void setTransparentRegion(IWindow iWindow, Region region) throws RemoteException;

    void setWallpaperDisplayOffset(IBinder iBinder, int i, int i2) throws RemoteException;

    void setWallpaperPosition(IBinder iBinder, float f, float f2, float f3, float f4) throws RemoteException;

    boolean startMovingTask(IWindow iWindow, float f, float f2) throws RemoteException;

    void updateDisplayContentLocation(IWindow iWindow, int i, int i2, int i3) throws RemoteException;

    void updatePointerIcon(IWindow iWindow) throws RemoteException;

    void updateTapExcludeRegion(IWindow iWindow, int i, Region region) throws RemoteException;

    @UnsupportedAppUsage
    void wallpaperCommandComplete(IBinder iBinder, Bundle bundle) throws RemoteException;

    @UnsupportedAppUsage
    void wallpaperOffsetsComplete(IBinder iBinder) throws RemoteException;

    public static class Default implements IWindowSession {
        @Override // android.view.IWindowSession
        public int addToDisplay(IWindow window, int seq, WindowManager.LayoutParams attrs, int viewVisibility, int layerStackId, Rect outFrame, Rect outContentInsets, Rect outStableInsets, Rect outOutsets, DisplayCutout.ParcelableWrapper displayCutout, InputChannel outInputChannel, InsetsState insetsState) throws RemoteException {
            return 0;
        }

        @Override // android.view.IWindowSession
        public int addToDisplayWithoutInputChannel(IWindow window, int seq, WindowManager.LayoutParams attrs, int viewVisibility, int layerStackId, Rect outContentInsets, Rect outStableInsets, InsetsState insetsState) throws RemoteException {
            return 0;
        }

        @Override // android.view.IWindowSession
        public void remove(IWindow window) throws RemoteException {
        }

        @Override // android.view.IWindowSession
        public int relayout(IWindow window, int seq, WindowManager.LayoutParams attrs, int requestedWidth, int requestedHeight, int viewVisibility, int flags, long frameNumber, Rect outFrame, Rect outOverscanInsets, Rect outContentInsets, Rect outVisibleInsets, Rect outStableInsets, Rect outOutsets, Rect outBackdropFrame, DisplayCutout.ParcelableWrapper displayCutout, MergedConfiguration outMergedConfiguration, SurfaceControl outSurfaceControl, InsetsState insetsState) throws RemoteException {
            return 0;
        }

        @Override // android.view.IWindowSession
        public void prepareToReplaceWindows(IBinder appToken, boolean childrenOnly) throws RemoteException {
        }

        @Override // android.view.IWindowSession
        public boolean outOfMemory(IWindow window) throws RemoteException {
            return false;
        }

        @Override // android.view.IWindowSession
        public void setTransparentRegion(IWindow window, Region region) throws RemoteException {
        }

        @Override // android.view.IWindowSession
        public void setInsets(IWindow window, int touchableInsets, Rect contentInsets, Rect visibleInsets, Region touchableRegion) throws RemoteException {
        }

        @Override // android.view.IWindowSession
        public void getDisplayFrame(IWindow window, Rect outDisplayFrame) throws RemoteException {
        }

        @Override // android.view.IWindowSession
        public void finishDrawing(IWindow window) throws RemoteException {
        }

        @Override // android.view.IWindowSession
        public void setInTouchMode(boolean showFocus) throws RemoteException {
        }

        @Override // android.view.IWindowSession
        public boolean getInTouchMode() throws RemoteException {
            return false;
        }

        @Override // android.view.IWindowSession
        public boolean performHapticFeedback(int effectId, boolean always) throws RemoteException {
            return false;
        }

        @Override // android.view.IWindowSession
        public IBinder performDrag(IWindow window, int flags, SurfaceControl surface, int touchSource, float touchX, float touchY, float thumbCenterX, float thumbCenterY, ClipData data) throws RemoteException {
            return null;
        }

        @Override // android.view.IWindowSession
        public void reportDropResult(IWindow window, boolean consumed) throws RemoteException {
        }

        @Override // android.view.IWindowSession
        public void cancelDragAndDrop(IBinder dragToken, boolean skipAnimation) throws RemoteException {
        }

        @Override // android.view.IWindowSession
        public void dragRecipientEntered(IWindow window) throws RemoteException {
        }

        @Override // android.view.IWindowSession
        public void dragRecipientExited(IWindow window) throws RemoteException {
        }

        @Override // android.view.IWindowSession
        public void dragRecipientFeedback(IWindow window, boolean isEntered, int mimeTypeSupportState, int acceptableItemCnt) throws RemoteException {
        }

        @Override // android.view.IWindowSession
        public void setWallpaperPosition(IBinder windowToken, float x, float y, float xstep, float ystep) throws RemoteException {
        }

        @Override // android.view.IWindowSession
        public void wallpaperOffsetsComplete(IBinder window) throws RemoteException {
        }

        @Override // android.view.IWindowSession
        public void setWallpaperDisplayOffset(IBinder windowToken, int x, int y) throws RemoteException {
        }

        @Override // android.view.IWindowSession
        public Bundle sendWallpaperCommand(IBinder window, String action, int x, int y, int z, Bundle extras, boolean sync) throws RemoteException {
            return null;
        }

        @Override // android.view.IWindowSession
        public void wallpaperCommandComplete(IBinder window, Bundle result) throws RemoteException {
        }

        @Override // android.view.IWindowSession
        public void onRectangleOnScreenRequested(IBinder token, Rect rectangle) throws RemoteException {
        }

        @Override // android.view.IWindowSession
        public IWindowId getWindowId(IBinder window) throws RemoteException {
            return null;
        }

        @Override // android.view.IWindowSession
        public void pokeDrawLock(IBinder window) throws RemoteException {
        }

        @Override // android.view.IWindowSession
        public boolean startMovingTask(IWindow window, float startX, float startY) throws RemoteException {
            return false;
        }

        @Override // android.view.IWindowSession
        public void finishMovingTask(IWindow window) throws RemoteException {
        }

        @Override // android.view.IWindowSession
        public void updatePointerIcon(IWindow window) throws RemoteException {
        }

        @Override // android.view.IWindowSession
        public void reparentDisplayContent(IWindow window, SurfaceControl sc, int displayId) throws RemoteException {
        }

        @Override // android.view.IWindowSession
        public void updateDisplayContentLocation(IWindow window, int x, int y, int displayId) throws RemoteException {
        }

        @Override // android.view.IWindowSession
        public void updateTapExcludeRegion(IWindow window, int regionId, Region region) throws RemoteException {
        }

        @Override // android.view.IWindowSession
        public void insetsModified(IWindow window, InsetsState state) throws RemoteException {
        }

        @Override // android.view.IWindowSession
        public void reportSystemGestureExclusionChanged(IWindow window, List<Rect> list) throws RemoteException {
        }

        @Override // android.view.IWindowSession
        public void setOriginalDragViewCenter(IWindow window, int x, int y) throws RemoteException {
        }

        @Override // android.view.IWindowSession
        public void setPendingDragEndedLoc(IWindow window, int x, int y) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IWindowSession {
        private static final String DESCRIPTOR = "android.view.IWindowSession";
        static final int TRANSACTION_addToDisplay = 1;
        static final int TRANSACTION_addToDisplayWithoutInputChannel = 2;
        static final int TRANSACTION_cancelDragAndDrop = 16;
        static final int TRANSACTION_dragRecipientEntered = 17;
        static final int TRANSACTION_dragRecipientExited = 18;
        static final int TRANSACTION_dragRecipientFeedback = 19;
        static final int TRANSACTION_finishDrawing = 10;
        static final int TRANSACTION_finishMovingTask = 29;
        static final int TRANSACTION_getDisplayFrame = 9;
        static final int TRANSACTION_getInTouchMode = 12;
        static final int TRANSACTION_getWindowId = 26;
        static final int TRANSACTION_insetsModified = 34;
        static final int TRANSACTION_onRectangleOnScreenRequested = 25;
        static final int TRANSACTION_outOfMemory = 6;
        static final int TRANSACTION_performDrag = 14;
        static final int TRANSACTION_performHapticFeedback = 13;
        static final int TRANSACTION_pokeDrawLock = 27;
        static final int TRANSACTION_prepareToReplaceWindows = 5;
        static final int TRANSACTION_relayout = 4;
        static final int TRANSACTION_remove = 3;
        static final int TRANSACTION_reparentDisplayContent = 31;
        static final int TRANSACTION_reportDropResult = 15;
        static final int TRANSACTION_reportSystemGestureExclusionChanged = 35;
        static final int TRANSACTION_sendWallpaperCommand = 23;
        static final int TRANSACTION_setInTouchMode = 11;
        static final int TRANSACTION_setInsets = 8;
        static final int TRANSACTION_setOriginalDragViewCenter = 36;
        static final int TRANSACTION_setPendingDragEndedLoc = 37;
        static final int TRANSACTION_setTransparentRegion = 7;
        static final int TRANSACTION_setWallpaperDisplayOffset = 22;
        static final int TRANSACTION_setWallpaperPosition = 20;
        static final int TRANSACTION_startMovingTask = 28;
        static final int TRANSACTION_updateDisplayContentLocation = 32;
        static final int TRANSACTION_updatePointerIcon = 30;
        static final int TRANSACTION_updateTapExcludeRegion = 33;
        static final int TRANSACTION_wallpaperCommandComplete = 24;
        static final int TRANSACTION_wallpaperOffsetsComplete = 21;

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "addToDisplay";
                case 2:
                    return "addToDisplayWithoutInputChannel";
                case 3:
                    return "remove";
                case 4:
                    return "relayout";
                case 5:
                    return "prepareToReplaceWindows";
                case 6:
                    return "outOfMemory";
                case 7:
                    return "setTransparentRegion";
                case 8:
                    return "setInsets";
                case 9:
                    return "getDisplayFrame";
                case 10:
                    return "finishDrawing";
                case 11:
                    return "setInTouchMode";
                case 12:
                    return "getInTouchMode";
                case 13:
                    return "performHapticFeedback";
                case 14:
                    return "performDrag";
                case 15:
                    return "reportDropResult";
                case 16:
                    return "cancelDragAndDrop";
                case 17:
                    return "dragRecipientEntered";
                case 18:
                    return "dragRecipientExited";
                case 19:
                    return "dragRecipientFeedback";
                case 20:
                    return "setWallpaperPosition";
                case 21:
                    return "wallpaperOffsetsComplete";
                case 22:
                    return "setWallpaperDisplayOffset";
                case 23:
                    return "sendWallpaperCommand";
                case 24:
                    return "wallpaperCommandComplete";
                case 25:
                    return "onRectangleOnScreenRequested";
                case 26:
                    return "getWindowId";
                case 27:
                    return "pokeDrawLock";
                case 28:
                    return "startMovingTask";
                case 29:
                    return "finishMovingTask";
                case 30:
                    return "updatePointerIcon";
                case 31:
                    return "reparentDisplayContent";
                case 32:
                    return "updateDisplayContentLocation";
                case 33:
                    return "updateTapExcludeRegion";
                case 34:
                    return "insetsModified";
                case 35:
                    return "reportSystemGestureExclusionChanged";
                case 36:
                    return "setOriginalDragViewCenter";
                case 37:
                    return "setPendingDragEndedLoc";
                default:
                    return null;
            }
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            WindowManager.LayoutParams _arg2;
            WindowManager.LayoutParams _arg22;
            WindowManager.LayoutParams _arg23;
            Region _arg1;
            Rect _arg24;
            Rect _arg3;
            Region _arg4;
            SurfaceControl _arg25;
            ClipData _arg8;
            Bundle _arg5;
            Bundle _arg12;
            Rect _arg13;
            SurfaceControl _arg14;
            Region _arg26;
            InsetsState _arg15;
            if (code != 1598968902) {
                boolean _arg16 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        IWindow _arg0 = IWindow.Stub.asInterface(data.readStrongBinder());
                        int _arg17 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg2 = WindowManager.LayoutParams.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        int _arg32 = data.readInt();
                        int _arg42 = data.readInt();
                        Rect _arg52 = new Rect();
                        Rect _arg6 = new Rect();
                        Rect _arg7 = new Rect();
                        Rect _arg82 = new Rect();
                        DisplayCutout.ParcelableWrapper _arg9 = new DisplayCutout.ParcelableWrapper();
                        InputChannel _arg10 = new InputChannel();
                        InsetsState _arg11 = new InsetsState();
                        int _result = addToDisplay(_arg0, _arg17, _arg2, _arg32, _arg42, _arg52, _arg6, _arg7, _arg82, _arg9, _arg10, _arg11);
                        reply.writeNoException();
                        reply.writeInt(_result);
                        reply.writeInt(1);
                        _arg52.writeToParcel(reply, 1);
                        reply.writeInt(1);
                        _arg6.writeToParcel(reply, 1);
                        reply.writeInt(1);
                        _arg7.writeToParcel(reply, 1);
                        reply.writeInt(1);
                        _arg82.writeToParcel(reply, 1);
                        reply.writeInt(1);
                        _arg9.writeToParcel(reply, 1);
                        reply.writeInt(1);
                        _arg10.writeToParcel(reply, 1);
                        reply.writeInt(1);
                        _arg11.writeToParcel(reply, 1);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        IWindow _arg02 = IWindow.Stub.asInterface(data.readStrongBinder());
                        int _arg18 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg22 = WindowManager.LayoutParams.CREATOR.createFromParcel(data);
                        } else {
                            _arg22 = null;
                        }
                        int _arg33 = data.readInt();
                        int _arg43 = data.readInt();
                        Rect _arg53 = new Rect();
                        Rect _arg62 = new Rect();
                        InsetsState _arg72 = new InsetsState();
                        int _result2 = addToDisplayWithoutInputChannel(_arg02, _arg18, _arg22, _arg33, _arg43, _arg53, _arg62, _arg72);
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        reply.writeInt(1);
                        _arg53.writeToParcel(reply, 1);
                        reply.writeInt(1);
                        _arg62.writeToParcel(reply, 1);
                        reply.writeInt(1);
                        _arg72.writeToParcel(reply, 1);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        remove(IWindow.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        IWindow _arg03 = IWindow.Stub.asInterface(data.readStrongBinder());
                        int _arg19 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg23 = WindowManager.LayoutParams.CREATOR.createFromParcel(data);
                        } else {
                            _arg23 = null;
                        }
                        int _arg34 = data.readInt();
                        int _arg44 = data.readInt();
                        int _arg54 = data.readInt();
                        int _arg63 = data.readInt();
                        long _arg73 = data.readLong();
                        Rect _arg83 = new Rect();
                        Rect _arg92 = new Rect();
                        Rect _arg102 = new Rect();
                        Rect _arg112 = new Rect();
                        Rect _arg122 = new Rect();
                        Rect _arg132 = new Rect();
                        Rect _arg142 = new Rect();
                        DisplayCutout.ParcelableWrapper _arg152 = new DisplayCutout.ParcelableWrapper();
                        MergedConfiguration _arg162 = new MergedConfiguration();
                        SurfaceControl _arg172 = new SurfaceControl();
                        InsetsState _arg182 = new InsetsState();
                        int _result3 = relayout(_arg03, _arg19, _arg23, _arg34, _arg44, _arg54, _arg63, _arg73, _arg83, _arg92, _arg102, _arg112, _arg122, _arg132, _arg142, _arg152, _arg162, _arg172, _arg182);
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        reply.writeInt(1);
                        _arg83.writeToParcel(reply, 1);
                        reply.writeInt(1);
                        _arg92.writeToParcel(reply, 1);
                        reply.writeInt(1);
                        _arg102.writeToParcel(reply, 1);
                        reply.writeInt(1);
                        _arg112.writeToParcel(reply, 1);
                        reply.writeInt(1);
                        _arg122.writeToParcel(reply, 1);
                        reply.writeInt(1);
                        _arg132.writeToParcel(reply, 1);
                        reply.writeInt(1);
                        _arg142.writeToParcel(reply, 1);
                        reply.writeInt(1);
                        _arg152.writeToParcel(reply, 1);
                        reply.writeInt(1);
                        _arg162.writeToParcel(reply, 1);
                        reply.writeInt(1);
                        _arg172.writeToParcel(reply, 1);
                        reply.writeInt(1);
                        _arg182.writeToParcel(reply, 1);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg04 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg16 = true;
                        }
                        prepareToReplaceWindows(_arg04, _arg16);
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        boolean outOfMemory = outOfMemory(IWindow.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(outOfMemory ? 1 : 0);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        IWindow _arg05 = IWindow.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg1 = Region.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        setTransparentRegion(_arg05, _arg1);
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        IWindow _arg06 = IWindow.Stub.asInterface(data.readStrongBinder());
                        int _arg110 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg24 = Rect.CREATOR.createFromParcel(data);
                        } else {
                            _arg24 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg3 = Rect.CREATOR.createFromParcel(data);
                        } else {
                            _arg3 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg4 = Region.CREATOR.createFromParcel(data);
                        } else {
                            _arg4 = null;
                        }
                        setInsets(_arg06, _arg110, _arg24, _arg3, _arg4);
                        reply.writeNoException();
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        IWindow _arg07 = IWindow.Stub.asInterface(data.readStrongBinder());
                        Rect _arg111 = new Rect();
                        getDisplayFrame(_arg07, _arg111);
                        reply.writeNoException();
                        reply.writeInt(1);
                        _arg111.writeToParcel(reply, 1);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        finishDrawing(IWindow.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg16 = true;
                        }
                        setInTouchMode(_arg16);
                        reply.writeNoException();
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        boolean inTouchMode = getInTouchMode();
                        reply.writeNoException();
                        reply.writeInt(inTouchMode ? 1 : 0);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg08 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg16 = true;
                        }
                        boolean performHapticFeedback = performHapticFeedback(_arg08, _arg16);
                        reply.writeNoException();
                        reply.writeInt(performHapticFeedback ? 1 : 0);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        IWindow _arg09 = IWindow.Stub.asInterface(data.readStrongBinder());
                        int _arg113 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg25 = SurfaceControl.CREATOR.createFromParcel(data);
                        } else {
                            _arg25 = null;
                        }
                        int _arg35 = data.readInt();
                        float _arg45 = data.readFloat();
                        float _arg55 = data.readFloat();
                        float _arg64 = data.readFloat();
                        float _arg74 = data.readFloat();
                        if (data.readInt() != 0) {
                            _arg8 = ClipData.CREATOR.createFromParcel(data);
                        } else {
                            _arg8 = null;
                        }
                        IBinder _result4 = performDrag(_arg09, _arg113, _arg25, _arg35, _arg45, _arg55, _arg64, _arg74, _arg8);
                        reply.writeNoException();
                        reply.writeStrongBinder(_result4);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        IWindow _arg010 = IWindow.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg16 = true;
                        }
                        reportDropResult(_arg010, _arg16);
                        reply.writeNoException();
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg011 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg16 = true;
                        }
                        cancelDragAndDrop(_arg011, _arg16);
                        reply.writeNoException();
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        dragRecipientEntered(IWindow.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        dragRecipientExited(IWindow.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        IWindow _arg012 = IWindow.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg16 = true;
                        }
                        dragRecipientFeedback(_arg012, _arg16, data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        setWallpaperPosition(data.readStrongBinder(), data.readFloat(), data.readFloat(), data.readFloat(), data.readFloat());
                        reply.writeNoException();
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        wallpaperOffsetsComplete(data.readStrongBinder());
                        reply.writeNoException();
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        setWallpaperDisplayOffset(data.readStrongBinder(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg013 = data.readStrongBinder();
                        String _arg114 = data.readString();
                        int _arg27 = data.readInt();
                        int _arg36 = data.readInt();
                        int _arg46 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg5 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg5 = null;
                        }
                        Bundle _result5 = sendWallpaperCommand(_arg013, _arg114, _arg27, _arg36, _arg46, _arg5, data.readInt() != 0);
                        reply.writeNoException();
                        if (_result5 != null) {
                            reply.writeInt(1);
                            _result5.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg014 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg12 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg12 = null;
                        }
                        wallpaperCommandComplete(_arg014, _arg12);
                        reply.writeNoException();
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg015 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg13 = Rect.CREATOR.createFromParcel(data);
                        } else {
                            _arg13 = null;
                        }
                        onRectangleOnScreenRequested(_arg015, _arg13);
                        reply.writeNoException();
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        IWindowId _result6 = getWindowId(data.readStrongBinder());
                        reply.writeNoException();
                        reply.writeStrongBinder(_result6 != null ? _result6.asBinder() : null);
                        return true;
                    case 27:
                        data.enforceInterface(DESCRIPTOR);
                        pokeDrawLock(data.readStrongBinder());
                        reply.writeNoException();
                        return true;
                    case 28:
                        data.enforceInterface(DESCRIPTOR);
                        boolean startMovingTask = startMovingTask(IWindow.Stub.asInterface(data.readStrongBinder()), data.readFloat(), data.readFloat());
                        reply.writeNoException();
                        reply.writeInt(startMovingTask ? 1 : 0);
                        return true;
                    case 29:
                        data.enforceInterface(DESCRIPTOR);
                        finishMovingTask(IWindow.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 30:
                        data.enforceInterface(DESCRIPTOR);
                        updatePointerIcon(IWindow.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 31:
                        data.enforceInterface(DESCRIPTOR);
                        IWindow _arg016 = IWindow.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg14 = SurfaceControl.CREATOR.createFromParcel(data);
                        } else {
                            _arg14 = null;
                        }
                        reparentDisplayContent(_arg016, _arg14, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 32:
                        data.enforceInterface(DESCRIPTOR);
                        updateDisplayContentLocation(IWindow.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 33:
                        data.enforceInterface(DESCRIPTOR);
                        IWindow _arg017 = IWindow.Stub.asInterface(data.readStrongBinder());
                        int _arg115 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg26 = Region.CREATOR.createFromParcel(data);
                        } else {
                            _arg26 = null;
                        }
                        updateTapExcludeRegion(_arg017, _arg115, _arg26);
                        reply.writeNoException();
                        return true;
                    case 34:
                        data.enforceInterface(DESCRIPTOR);
                        IWindow _arg018 = IWindow.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg15 = InsetsState.CREATOR.createFromParcel(data);
                        } else {
                            _arg15 = null;
                        }
                        insetsModified(_arg018, _arg15);
                        reply.writeNoException();
                        return true;
                    case 35:
                        data.enforceInterface(DESCRIPTOR);
                        reportSystemGestureExclusionChanged(IWindow.Stub.asInterface(data.readStrongBinder()), data.createTypedArrayList(Rect.CREATOR));
                        return true;
                    case 36:
                        data.enforceInterface(DESCRIPTOR);
                        setOriginalDragViewCenter(IWindow.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 37:
                        data.enforceInterface(DESCRIPTOR);
                        setPendingDragEndedLoc(IWindow.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readInt());
                        reply.writeNoException();
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
        public static class Proxy implements IWindowSession {
            public static IWindowSession sDefaultImpl;
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

            @Override // android.view.IWindowSession
            public int addToDisplay(IWindow window, int seq, WindowManager.LayoutParams attrs, int viewVisibility, int layerStackId, Rect outFrame, Rect outContentInsets, Rect outStableInsets, Rect outOutsets, DisplayCutout.ParcelableWrapper displayCutout, InputChannel outInputChannel, InsetsState insetsState) throws RemoteException {
                Parcel _reply;
                Throwable th;
                IBinder iBinder;
                Parcel _data = Parcel.obtain();
                Parcel _reply2 = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (window != null) {
                        try {
                            iBinder = window.asBinder();
                        } catch (Throwable th2) {
                            th = th2;
                            _reply = _reply2;
                        }
                    } else {
                        iBinder = null;
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
                    if (!this.mRemote.transact(1, _data, _reply2, 0)) {
                        try {
                            if (Stub.getDefaultImpl() != null) {
                                try {
                                    int addToDisplay = Stub.getDefaultImpl().addToDisplay(window, seq, attrs, viewVisibility, layerStackId, outFrame, outContentInsets, outStableInsets, outOutsets, displayCutout, outInputChannel, insetsState);
                                    _reply2.recycle();
                                    _data.recycle();
                                    return addToDisplay;
                                } catch (Throwable th3) {
                                    th = th3;
                                    _reply = _reply2;
                                    _reply.recycle();
                                    _data.recycle();
                                    throw th;
                                }
                            }
                        } catch (Throwable th4) {
                            th = th4;
                            _reply = _reply2;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    }
                    try {
                        _reply2.readException();
                        int _result = _reply2.readInt();
                        if (_reply2.readInt() != 0) {
                            _reply = _reply2;
                            try {
                                outFrame.readFromParcel(_reply);
                            } catch (Throwable th5) {
                                th = th5;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } else {
                            _reply = _reply2;
                        }
                        if (_reply.readInt() != 0) {
                            try {
                                outContentInsets.readFromParcel(_reply);
                            } catch (Throwable th6) {
                                th = th6;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        }
                        if (_reply.readInt() != 0) {
                            try {
                                outStableInsets.readFromParcel(_reply);
                            } catch (Throwable th7) {
                                th = th7;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        }
                        if (_reply.readInt() != 0) {
                            try {
                                outOutsets.readFromParcel(_reply);
                            } catch (Throwable th8) {
                                th = th8;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        }
                        if (_reply.readInt() != 0) {
                            try {
                                displayCutout.readFromParcel(_reply);
                            } catch (Throwable th9) {
                                th = th9;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        }
                        if (_reply.readInt() != 0) {
                            try {
                                outInputChannel.readFromParcel(_reply);
                            } catch (Throwable th10) {
                                th = th10;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        }
                        if (_reply.readInt() != 0) {
                            try {
                                insetsState.readFromParcel(_reply);
                            } catch (Throwable th11) {
                                th = th11;
                            }
                        }
                        _reply.recycle();
                        _data.recycle();
                        return _result;
                    } catch (Throwable th12) {
                        th = th12;
                        _reply = _reply2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th13) {
                    th = th13;
                    _reply = _reply2;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.view.IWindowSession
            public int addToDisplayWithoutInputChannel(IWindow window, int seq, WindowManager.LayoutParams attrs, int viewVisibility, int layerStackId, Rect outContentInsets, Rect outStableInsets, InsetsState insetsState) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(window != null ? window.asBinder() : null);
                    try {
                        _data.writeInt(seq);
                        if (attrs != null) {
                            _data.writeInt(1);
                            attrs.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        try {
                            _data.writeInt(viewVisibility);
                        } catch (Throwable th2) {
                            th = th2;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(layerStackId);
                        if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            int _result = _reply.readInt();
                            if (_reply.readInt() != 0) {
                                try {
                                    outContentInsets.readFromParcel(_reply);
                                } catch (Throwable th4) {
                                    th = th4;
                                    _reply.recycle();
                                    _data.recycle();
                                    throw th;
                                }
                            }
                            if (_reply.readInt() != 0) {
                                try {
                                    outStableInsets.readFromParcel(_reply);
                                } catch (Throwable th5) {
                                    th = th5;
                                    _reply.recycle();
                                    _data.recycle();
                                    throw th;
                                }
                            }
                            if (_reply.readInt() != 0) {
                                try {
                                    insetsState.readFromParcel(_reply);
                                } catch (Throwable th6) {
                                    th = th6;
                                }
                            }
                            _reply.recycle();
                            _data.recycle();
                            return _result;
                        }
                        int addToDisplayWithoutInputChannel = Stub.getDefaultImpl().addToDisplayWithoutInputChannel(window, seq, attrs, viewVisibility, layerStackId, outContentInsets, outStableInsets, insetsState);
                        _reply.recycle();
                        _data.recycle();
                        return addToDisplayWithoutInputChannel;
                    } catch (Throwable th7) {
                        th = th7;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th8) {
                    th = th8;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.view.IWindowSession
            public void remove(IWindow window) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(window != null ? window.asBinder() : null);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().remove(window);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowSession
            public int relayout(IWindow window, int seq, WindowManager.LayoutParams attrs, int requestedWidth, int requestedHeight, int viewVisibility, int flags, long frameNumber, Rect outFrame, Rect outOverscanInsets, Rect outContentInsets, Rect outVisibleInsets, Rect outStableInsets, Rect outOutsets, Rect outBackdropFrame, DisplayCutout.ParcelableWrapper displayCutout, MergedConfiguration outMergedConfiguration, SurfaceControl outSurfaceControl, InsetsState insetsState) throws RemoteException {
                Parcel _data;
                Parcel _reply;
                Throwable th;
                IBinder iBinder;
                Parcel _data2 = Parcel.obtain();
                Parcel _reply2 = Parcel.obtain();
                try {
                    _data2.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (window != null) {
                        try {
                            iBinder = window.asBinder();
                        } catch (Throwable th2) {
                            th = th2;
                            _reply = _reply2;
                            _data = _data2;
                        }
                    } else {
                        iBinder = null;
                    }
                    _data2.writeStrongBinder(iBinder);
                    _data2.writeInt(seq);
                    if (attrs != null) {
                        _data2.writeInt(1);
                        attrs.writeToParcel(_data2, 0);
                    } else {
                        _data2.writeInt(0);
                    }
                    _data2.writeInt(requestedWidth);
                    _data2.writeInt(requestedHeight);
                    _data2.writeInt(viewVisibility);
                    _data2.writeInt(flags);
                    _data2.writeLong(frameNumber);
                    if (!this.mRemote.transact(4, _data2, _reply2, 0)) {
                        try {
                            if (Stub.getDefaultImpl() != null) {
                                _data = _data2;
                                try {
                                    int relayout = Stub.getDefaultImpl().relayout(window, seq, attrs, requestedWidth, requestedHeight, viewVisibility, flags, frameNumber, outFrame, outOverscanInsets, outContentInsets, outVisibleInsets, outStableInsets, outOutsets, outBackdropFrame, displayCutout, outMergedConfiguration, outSurfaceControl, insetsState);
                                    _reply2.recycle();
                                    _data.recycle();
                                    return relayout;
                                } catch (Throwable th3) {
                                    th = th3;
                                    _reply = _reply2;
                                    _reply.recycle();
                                    _data.recycle();
                                    throw th;
                                }
                            }
                        } catch (Throwable th4) {
                            th = th4;
                            _data = _data2;
                            _reply = _reply2;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    }
                    _data = _data2;
                    try {
                        _reply2.readException();
                        int _result = _reply2.readInt();
                        if (_reply2.readInt() != 0) {
                            _reply = _reply2;
                            try {
                                outFrame.readFromParcel(_reply);
                            } catch (Throwable th5) {
                                th = th5;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } else {
                            _reply = _reply2;
                        }
                        if (_reply.readInt() != 0) {
                            try {
                                outOverscanInsets.readFromParcel(_reply);
                            } catch (Throwable th6) {
                                th = th6;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        }
                        if (_reply.readInt() != 0) {
                            try {
                                outContentInsets.readFromParcel(_reply);
                            } catch (Throwable th7) {
                                th = th7;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        }
                        if (_reply.readInt() != 0) {
                            try {
                                outVisibleInsets.readFromParcel(_reply);
                            } catch (Throwable th8) {
                                th = th8;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        }
                        if (_reply.readInt() != 0) {
                            try {
                                outStableInsets.readFromParcel(_reply);
                            } catch (Throwable th9) {
                                th = th9;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        }
                        if (_reply.readInt() != 0) {
                            try {
                                outOutsets.readFromParcel(_reply);
                            } catch (Throwable th10) {
                                th = th10;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        }
                        if (_reply.readInt() != 0) {
                            try {
                                outBackdropFrame.readFromParcel(_reply);
                            } catch (Throwable th11) {
                                th = th11;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        }
                        if (_reply.readInt() != 0) {
                            try {
                                displayCutout.readFromParcel(_reply);
                            } catch (Throwable th12) {
                                th = th12;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        }
                        if (_reply.readInt() != 0) {
                            try {
                                outMergedConfiguration.readFromParcel(_reply);
                            } catch (Throwable th13) {
                                th = th13;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        }
                        if (_reply.readInt() != 0) {
                            try {
                                outSurfaceControl.readFromParcel(_reply);
                            } catch (Throwable th14) {
                                th = th14;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        }
                        if (_reply.readInt() != 0) {
                            try {
                                insetsState.readFromParcel(_reply);
                            } catch (Throwable th15) {
                                th = th15;
                            }
                        }
                        _reply.recycle();
                        _data.recycle();
                        return _result;
                    } catch (Throwable th16) {
                        th = th16;
                        _reply = _reply2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th17) {
                    th = th17;
                    _reply = _reply2;
                    _data = _data2;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.view.IWindowSession
            public void prepareToReplaceWindows(IBinder appToken, boolean childrenOnly) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(appToken);
                    _data.writeInt(childrenOnly ? 1 : 0);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().prepareToReplaceWindows(appToken, childrenOnly);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowSession
            public boolean outOfMemory(IWindow window) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(window != null ? window.asBinder() : null);
                    boolean _result = false;
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().outOfMemory(window);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowSession
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
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setTransparentRegion(window, region);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowSession
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
                    if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setInsets(window, touchableInsets, contentInsets, visibleInsets, touchableRegion);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowSession
            public void getDisplayFrame(IWindow window, Rect outDisplayFrame) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(window != null ? window.asBinder() : null);
                    if (this.mRemote.transact(9, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        if (_reply.readInt() != 0) {
                            outDisplayFrame.readFromParcel(_reply);
                        }
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().getDisplayFrame(window, outDisplayFrame);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowSession
            public void finishDrawing(IWindow window) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(window != null ? window.asBinder() : null);
                    if (this.mRemote.transact(10, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().finishDrawing(window);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowSession
            public void setInTouchMode(boolean showFocus) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(showFocus ? 1 : 0);
                    if (this.mRemote.transact(11, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setInTouchMode(showFocus);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowSession
            public boolean getInTouchMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getInTouchMode();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowSession
            public boolean performHapticFeedback(int effectId, boolean always) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(effectId);
                    boolean _result = true;
                    _data.writeInt(always ? 1 : 0);
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().performHapticFeedback(effectId, always);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowSession
            public IBinder performDrag(IWindow window, int flags, SurfaceControl surface, int touchSource, float touchX, float touchY, float thumbCenterX, float thumbCenterY, ClipData data) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(window != null ? window.asBinder() : null);
                    try {
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
                        if (this.mRemote.transact(14, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            IBinder _result = _reply.readStrongBinder();
                            _reply.recycle();
                            _data.recycle();
                            return _result;
                        }
                        IBinder performDrag = Stub.getDefaultImpl().performDrag(window, flags, surface, touchSource, touchX, touchY, thumbCenterX, thumbCenterY, data);
                        _reply.recycle();
                        _data.recycle();
                        return performDrag;
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.view.IWindowSession
            public void reportDropResult(IWindow window, boolean consumed) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(window != null ? window.asBinder() : null);
                    _data.writeInt(consumed ? 1 : 0);
                    if (this.mRemote.transact(15, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().reportDropResult(window, consumed);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowSession
            public void cancelDragAndDrop(IBinder dragToken, boolean skipAnimation) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(dragToken);
                    _data.writeInt(skipAnimation ? 1 : 0);
                    if (this.mRemote.transact(16, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().cancelDragAndDrop(dragToken, skipAnimation);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowSession
            public void dragRecipientEntered(IWindow window) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(window != null ? window.asBinder() : null);
                    if (this.mRemote.transact(17, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().dragRecipientEntered(window);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowSession
            public void dragRecipientExited(IWindow window) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(window != null ? window.asBinder() : null);
                    if (this.mRemote.transact(18, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().dragRecipientExited(window);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowSession
            public void dragRecipientFeedback(IWindow window, boolean isEntered, int mimeTypeSupportState, int acceptableItemCnt) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(window != null ? window.asBinder() : null);
                    _data.writeInt(isEntered ? 1 : 0);
                    _data.writeInt(mimeTypeSupportState);
                    _data.writeInt(acceptableItemCnt);
                    if (this.mRemote.transact(19, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().dragRecipientFeedback(window, isEntered, mimeTypeSupportState, acceptableItemCnt);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowSession
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
                    if (this.mRemote.transact(20, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setWallpaperPosition(windowToken, x, y, xstep, ystep);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowSession
            public void wallpaperOffsetsComplete(IBinder window) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(window);
                    if (this.mRemote.transact(21, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().wallpaperOffsetsComplete(window);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowSession
            public void setWallpaperDisplayOffset(IBinder windowToken, int x, int y) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(windowToken);
                    _data.writeInt(x);
                    _data.writeInt(y);
                    if (this.mRemote.transact(22, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setWallpaperDisplayOffset(windowToken, x, y);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowSession
            public Bundle sendWallpaperCommand(IBinder window, String action, int x, int y, int z, Bundle extras, boolean sync) throws RemoteException {
                Throwable th;
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeStrongBinder(window);
                        try {
                            _data.writeString(action);
                        } catch (Throwable th2) {
                            th = th2;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeInt(x);
                        } catch (Throwable th3) {
                            th = th3;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(y);
                        _data.writeInt(z);
                        int i = 1;
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
                        if (this.mRemote.transact(23, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            if (_reply.readInt() != 0) {
                                _result = Bundle.CREATOR.createFromParcel(_reply);
                            } else {
                                _result = null;
                            }
                            _reply.recycle();
                            _data.recycle();
                            return _result;
                        }
                        Bundle sendWallpaperCommand = Stub.getDefaultImpl().sendWallpaperCommand(window, action, x, y, z, extras, sync);
                        _reply.recycle();
                        _data.recycle();
                        return sendWallpaperCommand;
                    } catch (Throwable th5) {
                        th = th5;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th6) {
                    th = th6;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.view.IWindowSession
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
                    if (this.mRemote.transact(24, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().wallpaperCommandComplete(window, result);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowSession
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
                    if (this.mRemote.transact(25, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onRectangleOnScreenRequested(token, rectangle);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowSession
            public IWindowId getWindowId(IBinder window) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(window);
                    if (!this.mRemote.transact(26, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getWindowId(window);
                    }
                    _reply.readException();
                    IWindowId _result = IWindowId.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowSession
            public void pokeDrawLock(IBinder window) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(window);
                    if (this.mRemote.transact(27, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().pokeDrawLock(window);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowSession
            public boolean startMovingTask(IWindow window, float startX, float startY) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(window != null ? window.asBinder() : null);
                    _data.writeFloat(startX);
                    _data.writeFloat(startY);
                    boolean _result = false;
                    if (!this.mRemote.transact(28, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().startMovingTask(window, startX, startY);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowSession
            public void finishMovingTask(IWindow window) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(window != null ? window.asBinder() : null);
                    if (this.mRemote.transact(29, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().finishMovingTask(window);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowSession
            public void updatePointerIcon(IWindow window) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(window != null ? window.asBinder() : null);
                    if (this.mRemote.transact(30, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().updatePointerIcon(window);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowSession
            public void reparentDisplayContent(IWindow window, SurfaceControl sc, int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(window != null ? window.asBinder() : null);
                    if (sc != null) {
                        _data.writeInt(1);
                        sc.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(displayId);
                    if (this.mRemote.transact(31, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().reparentDisplayContent(window, sc, displayId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowSession
            public void updateDisplayContentLocation(IWindow window, int x, int y, int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(window != null ? window.asBinder() : null);
                    _data.writeInt(x);
                    _data.writeInt(y);
                    _data.writeInt(displayId);
                    if (this.mRemote.transact(32, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().updateDisplayContentLocation(window, x, y, displayId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowSession
            public void updateTapExcludeRegion(IWindow window, int regionId, Region region) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(window != null ? window.asBinder() : null);
                    _data.writeInt(regionId);
                    if (region != null) {
                        _data.writeInt(1);
                        region.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(33, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().updateTapExcludeRegion(window, regionId, region);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowSession
            public void insetsModified(IWindow window, InsetsState state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(window != null ? window.asBinder() : null);
                    if (state != null) {
                        _data.writeInt(1);
                        state.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(34, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().insetsModified(window, state);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowSession
            public void reportSystemGestureExclusionChanged(IWindow window, List<Rect> exclusionRects) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(window != null ? window.asBinder() : null);
                    _data.writeTypedList(exclusionRects);
                    if (this.mRemote.transact(35, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().reportSystemGestureExclusionChanged(window, exclusionRects);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowSession
            public void setOriginalDragViewCenter(IWindow window, int x, int y) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(window != null ? window.asBinder() : null);
                    _data.writeInt(x);
                    _data.writeInt(y);
                    if (this.mRemote.transact(36, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setOriginalDragViewCenter(window, x, y);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowSession
            public void setPendingDragEndedLoc(IWindow window, int x, int y) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(window != null ? window.asBinder() : null);
                    _data.writeInt(x);
                    _data.writeInt(y);
                    if (this.mRemote.transact(37, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setPendingDragEndedLoc(window, x, y);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IWindowSession impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IWindowSession getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
