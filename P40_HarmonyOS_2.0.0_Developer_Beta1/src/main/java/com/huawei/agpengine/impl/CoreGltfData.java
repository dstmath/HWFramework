package com.huawei.agpengine.impl;

class CoreGltfData {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreGltfData(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreGltfData obj) {
        long j;
        if (obj == null) {
            return 0;
        }
        synchronized (obj) {
            j = obj.agpCptr;
        }
        return j;
    }

    /* access modifiers changed from: package-private */
    public synchronized void delete() {
        if (this.agpCptr != 0) {
            if (!this.isAgpCmemOwn) {
                this.agpCptr = 0;
            } else {
                this.isAgpCmemOwn = false;
                throw new UnsupportedOperationException("C++ destructor does not have public access");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean loadBuffers() {
        return CoreJni.loadBuffersInCoreGltfData(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void releaseBuffers() {
        CoreJni.releaseBuffersInCoreGltfData(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public CoreStringArray getExternalFileUris() {
        return new CoreStringArray(CoreJni.getExternalFileUrisInCoreGltfData(this.agpCptr, this), true);
    }

    /* access modifiers changed from: package-private */
    public long getDefaultSceneIndex() {
        return CoreJni.getDefaultSceneIndexInCoreGltfData(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public long getSceneCount() {
        return CoreJni.getSceneCountInCoreGltfData(this.agpCptr, this);
    }

    static class CoreThumbnailImage {
        private transient long agpCptr;
        transient boolean isAgpCmemOwn;

        CoreThumbnailImage(long cptr, boolean isCmemoryOwn) {
            this.isAgpCmemOwn = isCmemoryOwn;
            this.agpCptr = cptr;
        }

        static long getCptr(CoreThumbnailImage obj) {
            long j;
            if (obj == null) {
                return 0;
            }
            synchronized (obj) {
                j = obj.agpCptr;
            }
            return j;
        }

        /* access modifiers changed from: protected */
        public void finalize() {
            delete();
        }

        /* access modifiers changed from: package-private */
        public synchronized void delete() {
            if (this.agpCptr != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreGltfDataCoreThumbnailImage(this.agpCptr);
                }
                this.agpCptr = 0;
            }
        }

        /* access modifiers changed from: package-private */
        public void setExtension(String value) {
            CoreJni.setVarextensionCoreGltfDataCoreThumbnailImage(this.agpCptr, this, value);
        }

        /* access modifiers changed from: package-private */
        public String getExtension() {
            return CoreJni.getVarextensionCoreGltfDataCoreThumbnailImage(this.agpCptr, this);
        }

        /* access modifiers changed from: package-private */
        public void setData(CoreByteArrayView value) {
            CoreJni.setVardataCoreGltfDataCoreThumbnailImage(this.agpCptr, this, CoreByteArrayView.getCptr(value), value);
        }

        /* access modifiers changed from: package-private */
        public CoreByteArrayView getData() {
            long cptr = CoreJni.getVardataCoreGltfDataCoreThumbnailImage(this.agpCptr, this);
            if (cptr == 0) {
                return null;
            }
            return new CoreByteArrayView(cptr, false);
        }

        CoreThumbnailImage() {
            this(CoreJni.newCoreThumbnailImage(), true);
        }
    }

    /* access modifiers changed from: package-private */
    public long getThumbnailImageCount() {
        return CoreJni.getThumbnailImageCountInCoreGltfData(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public CoreThumbnailImage getThumbnailImage(long thumbnailIndex) {
        return new CoreThumbnailImage(CoreJni.getThumbnailImageInCoreGltfData(this.agpCptr, this, thumbnailIndex), true);
    }
}
