package ohos.accessibility.ability;

import java.util.ArrayList;
import java.util.List;
import ohos.accessibility.adapter.ability.AccessibleControlAdapter;
import ohos.accessibility.utils.LogUtil;

public class SoftKeyBoardController {
    private static final String TAG = "SoftKeyBoardController";
    private final int mConnectionId;
    private List<SoftKeyBoardListener> mListeners = new ArrayList();
    private final Object mLock;

    public SoftKeyBoardController(int i, Object obj) {
        this.mConnectionId = i;
        this.mLock = obj;
    }

    public void addListener(SoftKeyBoardListener softKeyBoardListener) {
        synchronized (this.mLock) {
            boolean isEmpty = this.mListeners.isEmpty();
            this.mListeners.add(softKeyBoardListener);
            if (isEmpty) {
                setSoftKeyboardCallbackEnabled(true);
            }
        }
    }

    public boolean deleteListener(SoftKeyBoardListener softKeyBoardListener) {
        synchronized (this.mLock) {
            boolean z = false;
            if (this.mListeners != null) {
                if (!this.mListeners.isEmpty()) {
                    int indexOf = this.mListeners.indexOf(softKeyBoardListener);
                    if (indexOf >= 0) {
                        this.mListeners.remove(indexOf);
                    }
                    if (indexOf >= 0 && this.mListeners.isEmpty()) {
                        setSoftKeyboardCallbackEnabled(false);
                    }
                    if (indexOf >= 0) {
                        z = true;
                    }
                    return z;
                }
            }
            return false;
        }
    }

    public int getShowMode() {
        return AccessibleControlAdapter.getSoftKeyBoardShowMode(this.mConnectionId);
    }

    public boolean setShowMode(int i) {
        return AccessibleControlAdapter.setSoftKeyBoardShowMode(this.mConnectionId, i);
    }

    /* access modifiers changed from: package-private */
    public void onServiceConnected() {
        synchronized (this.mLock) {
            if (this.mListeners != null && !this.mListeners.isEmpty()) {
                setSoftKeyboardCallbackEnabled(true);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void dispatchSoftKeyBoardListeners(int i) {
        synchronized (this.mLock) {
            if (this.mListeners != null) {
                if (!this.mListeners.isEmpty()) {
                    for (SoftKeyBoardListener softKeyBoardListener : this.mListeners) {
                        softKeyBoardListener.onSoftKeyBoardShowModeChanged(this, i);
                    }
                    return;
                }
            }
            LogUtil.info(TAG, "Received soft keyboard show mode changed callback with no listeners registered!");
            setSoftKeyboardCallbackEnabled(false);
        }
    }

    private void setSoftKeyboardCallbackEnabled(boolean z) {
        AccessibleControlAdapter.enableSoftKeyBoardCallback(this.mConnectionId, z);
    }
}
