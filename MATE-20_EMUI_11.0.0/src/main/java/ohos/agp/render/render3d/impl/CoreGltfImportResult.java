package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public class CoreGltfImportResult {
    private transient long agpCptrCoreGltfImportResult;
    transient boolean isAgpCmemOwn;
    private final Object lock = new Object();

    CoreGltfImportResult(long j, boolean z) {
        this.isAgpCmemOwn = z;
        this.agpCptrCoreGltfImportResult = j;
    }

    static long getCptr(CoreGltfImportResult coreGltfImportResult) {
        if (coreGltfImportResult == null) {
            return 0;
        }
        return coreGltfImportResult.agpCptrCoreGltfImportResult;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.lock) {
            if (this.agpCptrCoreGltfImportResult != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreGltfImportResult(this.agpCptrCoreGltfImportResult);
                }
                this.agpCptrCoreGltfImportResult = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreGltfImportResult coreGltfImportResult, boolean z) {
        if (coreGltfImportResult != null) {
            synchronized (coreGltfImportResult.lock) {
                coreGltfImportResult.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreGltfImportResult);
    }

    /* access modifiers changed from: package-private */
    public boolean getSuccess() {
        return CoreJni.getVarsuccessCoreGltfImportResult(this.agpCptrCoreGltfImportResult, this);
    }

    /* access modifiers changed from: package-private */
    public String getError() {
        return CoreJni.getVarerrorCoreGltfImportResult(this.agpCptrCoreGltfImportResult, this);
    }

    /* access modifiers changed from: package-private */
    public CoreGltfResourceData getData() {
        long vardataCoreGltfImportResult = CoreJni.getVardataCoreGltfImportResult(this.agpCptrCoreGltfImportResult, this);
        if (vardataCoreGltfImportResult == 0) {
            return null;
        }
        return new CoreGltfResourceData(vardataCoreGltfImportResult, false);
    }
}
