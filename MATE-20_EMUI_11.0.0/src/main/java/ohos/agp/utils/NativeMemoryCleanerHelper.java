package ohos.agp.utils;

public abstract class NativeMemoryCleanerHelper implements MemoryCleaner {
    protected long mNativeObject = 0;

    /* access modifiers changed from: protected */
    public abstract void releaseNativeMemory(long j);

    public NativeMemoryCleanerHelper(long j) {
        this.mNativeObject = j;
    }

    @Override // ohos.agp.utils.MemoryCleaner
    public void run() {
        long j = this.mNativeObject;
        if (j != 0) {
            releaseNativeMemory(j);
            this.mNativeObject = 0;
        }
    }
}
