package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public class CoreNativeWindow {
    private transient long agpCptrCoreNativeWindow;
    private final Object delLock;
    transient boolean isAgpCmemOwn;

    CoreNativeWindow(long j, boolean z) {
        this.delLock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptrCoreNativeWindow = j;
    }

    static long getCptr(CoreNativeWindow coreNativeWindow) {
        if (coreNativeWindow == null) {
            return 0;
        }
        return coreNativeWindow.agpCptrCoreNativeWindow;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptrCoreNativeWindow != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreNativeWindow(this.agpCptrCoreNativeWindow);
                }
                this.agpCptrCoreNativeWindow = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreNativeWindow coreNativeWindow, boolean z) {
        if (coreNativeWindow != null) {
            synchronized (coreNativeWindow.delLock) {
                coreNativeWindow.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreNativeWindow);
    }

    /* access modifiers changed from: package-private */
    public boolean isValid() {
        return CoreJni.isValidInCoreNativeWindow(this.agpCptrCoreNativeWindow, this);
    }

    CoreNativeWindow() {
        this(CoreJni.newCoreNativeWindow(), true);
    }
}
