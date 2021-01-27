package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public class CoreGltfData {
    private transient long agpCptrCoreGltfData;
    transient boolean isAgpCmemOwn;
    private final Object lock = new Object();

    CoreGltfData(long j, boolean z) {
        this.isAgpCmemOwn = z;
        this.agpCptrCoreGltfData = j;
    }

    static long getCptr(CoreGltfData coreGltfData) {
        if (coreGltfData == null) {
            return 0;
        }
        return coreGltfData.agpCptrCoreGltfData;
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.lock) {
            if (this.agpCptrCoreGltfData != 0) {
                if (!this.isAgpCmemOwn) {
                    this.agpCptrCoreGltfData = 0;
                } else {
                    this.isAgpCmemOwn = false;
                    throw new UnsupportedOperationException("C++ destructor does not have public access");
                }
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreGltfData coreGltfData, boolean z) {
        if (coreGltfData != null) {
            synchronized (coreGltfData.lock) {
                coreGltfData.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreGltfData);
    }

    /* access modifiers changed from: package-private */
    public boolean loadBuffers(CoreFileManager coreFileManager) {
        return CoreJni.loadBuffersInCoreGltfData(this.agpCptrCoreGltfData, this, CoreFileManager.getCptr(coreFileManager), coreFileManager);
    }

    /* access modifiers changed from: package-private */
    public void releaseBuffers() {
        CoreJni.releaseBuffersInCoreGltfData(this.agpCptrCoreGltfData, this);
    }

    /* access modifiers changed from: package-private */
    public CoreStringArray getExternalFileUris() {
        return new CoreStringArray(CoreJni.getExternalFileUrisInCoreGltfData(this.agpCptrCoreGltfData, this), true);
    }

    /* access modifiers changed from: package-private */
    public long getDefaultSceneIndex() {
        return CoreJni.getDefaultSceneIndexInCoreGltfData(this.agpCptrCoreGltfData, this);
    }

    /* access modifiers changed from: package-private */
    public long getSceneCount() {
        return CoreJni.getSceneCountInCoreGltfData(this.agpCptrCoreGltfData, this);
    }

    static class CoreThumbnailImage {
        private transient long agpCptrCoreThumbnailImage;
        transient boolean isAgpCmemOwn;
        private final Object lock2;

        CoreThumbnailImage(long j, boolean z) {
            this.lock2 = new Object();
            this.isAgpCmemOwn = z;
            this.agpCptrCoreThumbnailImage = j;
        }

        static long getCptr(CoreThumbnailImage coreThumbnailImage) {
            if (coreThumbnailImage == null) {
                return 0;
            }
            return coreThumbnailImage.agpCptrCoreThumbnailImage;
        }

        /* access modifiers changed from: protected */
        public void finalize() {
            delete();
        }

        /* access modifiers changed from: package-private */
        public void delete() {
            synchronized (this.lock2) {
                if (this.agpCptrCoreThumbnailImage != 0) {
                    if (this.isAgpCmemOwn) {
                        this.isAgpCmemOwn = false;
                        CoreJni.deleteCoreGltfDataCoreThumbnailImage(this.agpCptrCoreThumbnailImage);
                    }
                    this.agpCptrCoreThumbnailImage = 0;
                }
            }
        }

        static long getCptrAndSetMemOwn(CoreThumbnailImage coreThumbnailImage, boolean z) {
            if (coreThumbnailImage != null) {
                synchronized (coreThumbnailImage.lock2) {
                    coreThumbnailImage.isAgpCmemOwn = z;
                }
            }
            return getCptr(coreThumbnailImage);
        }

        /* access modifiers changed from: package-private */
        public void setExtension(String str) {
            CoreJni.setVarextensionCoreGltfDataCoreThumbnailImage(this.agpCptrCoreThumbnailImage, this, str);
        }

        /* access modifiers changed from: package-private */
        public String getExtension() {
            return CoreJni.getVarextensionCoreGltfDataCoreThumbnailImage(this.agpCptrCoreThumbnailImage, this);
        }

        /* access modifiers changed from: package-private */
        public void setData(CoreByteArrayView coreByteArrayView) {
            CoreJni.setVardataCoreGltfDataCoreThumbnailImage(this.agpCptrCoreThumbnailImage, this, CoreByteArrayView.getCptr(coreByteArrayView), coreByteArrayView);
        }

        /* access modifiers changed from: package-private */
        public CoreByteArrayView getData() {
            long vardataCoreGltfDataCoreThumbnailImage = CoreJni.getVardataCoreGltfDataCoreThumbnailImage(this.agpCptrCoreThumbnailImage, this);
            if (vardataCoreGltfDataCoreThumbnailImage == 0) {
                return null;
            }
            return new CoreByteArrayView(vardataCoreGltfDataCoreThumbnailImage, false);
        }

        CoreThumbnailImage() {
            this(CoreJni.newCoreThumbnailImage(), true);
        }
    }

    /* access modifiers changed from: package-private */
    public long getThumbnailImageCount() {
        return CoreJni.getThumbnailImageCountInCoreGltfData(this.agpCptrCoreGltfData, this);
    }

    /* access modifiers changed from: package-private */
    public CoreThumbnailImage getThumbnailImage(long j, CoreFileManager coreFileManager) {
        return new CoreThumbnailImage(CoreJni.getThumbnailImageInCoreGltfData(this.agpCptrCoreGltfData, this, j, CoreFileManager.getCptr(coreFileManager), coreFileManager), true);
    }

    static class CoreDeleter {
        private transient long agpCptrGltfDataThumbnailImage;
        transient boolean isAgpCmemOwn;
        private final Object lock3;

        CoreDeleter(long j, boolean z) {
            this.lock3 = new Object();
            this.isAgpCmemOwn = z;
            this.agpCptrGltfDataThumbnailImage = j;
        }

        static long getCptr(CoreDeleter coreDeleter) {
            if (coreDeleter == null) {
                return 0;
            }
            return coreDeleter.agpCptrGltfDataThumbnailImage;
        }

        /* access modifiers changed from: protected */
        public void finalize() {
            delete();
        }

        /* access modifiers changed from: package-private */
        public void delete() {
            synchronized (this.lock3) {
                if (this.agpCptrGltfDataThumbnailImage != 0) {
                    if (this.isAgpCmemOwn) {
                        this.isAgpCmemOwn = false;
                        CoreJni.deleteCoreGltfDataCoreDeleter(this.agpCptrGltfDataThumbnailImage);
                    }
                    this.agpCptrGltfDataThumbnailImage = 0;
                }
            }
        }

        static long getCptrAndSetMemOwn(CoreDeleter coreDeleter, boolean z) {
            if (coreDeleter != null) {
                synchronized (coreDeleter.lock3) {
                    coreDeleter.isAgpCmemOwn = z;
                }
            }
            return getCptr(coreDeleter);
        }

        CoreDeleter() {
            this(CoreJni.newCoreGltfDataCoreDeleter(), true);
        }
    }
}
