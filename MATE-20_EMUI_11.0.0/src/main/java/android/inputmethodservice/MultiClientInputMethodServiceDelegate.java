package android.inputmethodservice;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.os.ResultReceiver;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.CursorAnchorInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

public final class MultiClientInputMethodServiceDelegate {
    public static final int INVALID_CLIENT_ID = -1;
    public static final int INVALID_WINDOW_HANDLE = -1;
    public static final String SERVICE_INTERFACE = "android.inputmethodservice.MultiClientInputMethodService";
    private final MultiClientInputMethodServiceDelegateImpl mImpl;

    public interface ClientCallback {
        void onAppPrivateCommand(String str, Bundle bundle);

        void onDisplayCompletions(CompletionInfo[] completionInfoArr);

        void onFinishSession();

        boolean onGenericMotionEvent(MotionEvent motionEvent);

        void onHideSoftInput(int i, ResultReceiver resultReceiver);

        boolean onKeyDown(int i, KeyEvent keyEvent);

        boolean onKeyLongPress(int i, KeyEvent keyEvent);

        boolean onKeyMultiple(int i, KeyEvent keyEvent);

        boolean onKeyUp(int i, KeyEvent keyEvent);

        void onShowSoftInput(int i, ResultReceiver resultReceiver);

        void onStartInputOrWindowGainedFocus(InputConnection inputConnection, EditorInfo editorInfo, int i, int i2, int i3);

        void onToggleSoftInput(int i, int i2);

        boolean onTrackballEvent(MotionEvent motionEvent);

        void onUpdateCursorAnchorInfo(CursorAnchorInfo cursorAnchorInfo);

        void onUpdateSelection(int i, int i2, int i3, int i4, int i5, int i6);
    }

    public interface ServiceCallback {
        void addClient(int i, int i2, int i3, int i4);

        void initialized();

        void removeClient(int i);
    }

    private MultiClientInputMethodServiceDelegate(Context context, ServiceCallback serviceCallback) {
        this.mImpl = new MultiClientInputMethodServiceDelegateImpl(context, serviceCallback);
    }

    public static MultiClientInputMethodServiceDelegate create(Context context, ServiceCallback serviceCallback) {
        return new MultiClientInputMethodServiceDelegate(context, serviceCallback);
    }

    public void onDestroy() {
        this.mImpl.onDestroy();
    }

    public IBinder onBind(Intent intent) {
        return this.mImpl.onBind(intent);
    }

    public boolean onUnbind(Intent intent) {
        return this.mImpl.onUnbind(intent);
    }

    public IBinder createInputMethodWindowToken(int displayId) {
        return this.mImpl.createInputMethodWindowToken(displayId);
    }

    public void acceptClient(int clientId, ClientCallback clientCallback, KeyEvent.DispatcherState dispatcherState, Looper looper) {
        this.mImpl.acceptClient(clientId, clientCallback, dispatcherState, looper);
    }

    public void reportImeWindowTarget(int clientId, int targetWindowHandle, IBinder imeWindowToken) {
        this.mImpl.reportImeWindowTarget(clientId, targetWindowHandle, imeWindowToken);
    }

    public boolean isUidAllowedOnDisplay(int displayId, int uid) {
        return this.mImpl.isUidAllowedOnDisplay(displayId, uid);
    }

    public void setActive(int clientId, boolean active) {
        this.mImpl.setActive(clientId, active);
    }
}
