package sun.nio.ch;

interface Cancellable {
    void onCancel(PendingFuture<?, ?> pendingFuture);
}
