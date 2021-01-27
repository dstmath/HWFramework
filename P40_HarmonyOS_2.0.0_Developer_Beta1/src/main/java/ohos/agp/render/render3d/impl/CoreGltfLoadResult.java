package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public class CoreGltfLoadResult {
    private transient long agpCptrCoreGltfLoadResult;
    transient boolean isAgpCmemOwn;
    private final Object lock = new Object();

    CoreGltfLoadResult(long j, boolean z) {
        this.isAgpCmemOwn = z;
        this.agpCptrCoreGltfLoadResult = j;
    }

    static long getCptr(CoreGltfLoadResult coreGltfLoadResult) {
        if (coreGltfLoadResult == null) {
            return 0;
        }
        return coreGltfLoadResult.agpCptrCoreGltfLoadResult;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.lock) {
            if (this.agpCptrCoreGltfLoadResult != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreGltfLoadResult(this.agpCptrCoreGltfLoadResult);
                }
                this.agpCptrCoreGltfLoadResult = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreGltfLoadResult coreGltfLoadResult, boolean z) {
        if (coreGltfLoadResult != null) {
            synchronized (coreGltfLoadResult.lock) {
                coreGltfLoadResult.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreGltfLoadResult);
    }

    /* access modifiers changed from: package-private */
    public boolean getSuccess() {
        return CoreJni.getVarsuccessCoreGltfLoadResult(this.agpCptrCoreGltfLoadResult, this);
    }

    /* access modifiers changed from: package-private */
    public String getError() {
        return CoreJni.getVarerrorCoreGltfLoadResult(this.agpCptrCoreGltfLoadResult, this);
    }

    /* access modifiers changed from: package-private */
    public CoreGltfData getData() {
        long dataInCoreGltfLoadResult = CoreJni.getDataInCoreGltfLoadResult(this.agpCptrCoreGltfLoadResult, this);
        if (dataInCoreGltfLoadResult == 0) {
            return null;
        }
        return new CoreGltfData(dataInCoreGltfLoadResult, false);
    }
}
