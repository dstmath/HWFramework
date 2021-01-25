package ohos.event.commonevent;

import android.content.BroadcastReceiver;

/* access modifiers changed from: package-private */
public final class AsyncCommonEventResultProxy {
    private final BroadcastReceiver.PendingResult pendingResult;
    private final BroadcastReceiver receiver;

    AsyncCommonEventResultProxy(BroadcastReceiver broadcastReceiver, BroadcastReceiver.PendingResult pendingResult2) {
        this.receiver = broadcastReceiver;
        this.pendingResult = pendingResult2;
    }

    /* access modifiers changed from: package-private */
    public void setResultCode(int i) {
        checkState();
        try {
            this.pendingResult.setResultCode(i);
        } catch (RuntimeException unused) {
            throw new IllegalStateException("trying to call method during a non-ordered commonevent");
        }
    }

    /* access modifiers changed from: package-private */
    public int getResultCode() {
        BroadcastReceiver.PendingResult pendingResult2 = this.pendingResult;
        if (pendingResult2 != null) {
            return pendingResult2.getResultCode();
        }
        return 0;
    }

    /* access modifiers changed from: package-private */
    public void setResultData(String str) {
        checkState();
        try {
            this.pendingResult.setResultData(str);
        } catch (RuntimeException unused) {
            throw new IllegalStateException("trying to call method during a non-ordered commonevent");
        }
    }

    /* access modifiers changed from: package-private */
    public String getResultData() {
        BroadcastReceiver.PendingResult pendingResult2 = this.pendingResult;
        if (pendingResult2 != null) {
            return pendingResult2.getResultData();
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public void setResult(int i, String str) {
        checkState();
        try {
            this.pendingResult.setResult(i, str, this.pendingResult.getResultExtras(false));
        } catch (RuntimeException unused) {
            throw new IllegalStateException("trying to call method during a non-ordered commonevent");
        }
    }

    /* access modifiers changed from: package-private */
    public void abortCommonEvent() {
        checkState();
        try {
            this.pendingResult.abortBroadcast();
        } catch (RuntimeException unused) {
            throw new IllegalStateException("trying to call method during a non-ordered commonevent");
        }
    }

    /* access modifiers changed from: package-private */
    public boolean getAbortCommonEvent() {
        BroadcastReceiver.PendingResult pendingResult2 = this.pendingResult;
        return pendingResult2 != null && pendingResult2.getAbortBroadcast();
    }

    /* access modifiers changed from: package-private */
    public void clearAbortCommonEvent() {
        BroadcastReceiver.PendingResult pendingResult2 = this.pendingResult;
        if (pendingResult2 != null) {
            pendingResult2.clearAbortBroadcast();
        }
    }

    /* access modifiers changed from: package-private */
    public void finish() {
        checkState();
        try {
            this.pendingResult.finish();
        } catch (IllegalStateException unused) {
            throw new IllegalStateException("CommonEvent already finished");
        }
    }

    /* access modifiers changed from: package-private */
    public void async() {
        BroadcastReceiver broadcastReceiver = this.receiver;
        if (broadcastReceiver != null) {
            broadcastReceiver.goAsync();
        }
    }

    private void checkState() {
        if (this.receiver == null || this.pendingResult == null) {
            throw new IllegalStateException("illegal AsyncCommonEventResultProxy");
        }
    }
}
