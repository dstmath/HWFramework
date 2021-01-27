package ohos.backgroundtaskmgr;

public abstract class ExpiredCallback {
    private final ExpiredCallbackStub callback = new ExpiredCallbackStub() {
        /* class ohos.backgroundtaskmgr.ExpiredCallback.AnonymousClass1 */

        @Override // ohos.backgroundtaskmgr.IExpiredCallback
        public void onExpired() {
            ExpiredCallback.this.onExpired();
        }
    };

    public abstract void onExpired();

    /* access modifiers changed from: package-private */
    public IExpiredCallback getCallback() {
        return this.callback;
    }
}
