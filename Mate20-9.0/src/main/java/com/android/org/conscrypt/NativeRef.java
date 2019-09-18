package com.android.org.conscrypt;

abstract class NativeRef {
    final long context;

    static final class EC_GROUP extends NativeRef {
        EC_GROUP(long ctx) {
            super(ctx);
        }

        /* access modifiers changed from: package-private */
        public void doFree(long context) {
            NativeCrypto.EC_GROUP_clear_free(context);
        }
    }

    static final class EC_POINT extends NativeRef {
        EC_POINT(long nativePointer) {
            super(nativePointer);
        }

        /* access modifiers changed from: package-private */
        public void doFree(long context) {
            NativeCrypto.EC_POINT_clear_free(context);
        }
    }

    static final class EVP_CIPHER_CTX extends NativeRef {
        EVP_CIPHER_CTX(long nativePointer) {
            super(nativePointer);
        }

        /* access modifiers changed from: package-private */
        public void doFree(long context) {
            NativeCrypto.EVP_CIPHER_CTX_free(context);
        }
    }

    static final class EVP_MD_CTX extends NativeRef {
        EVP_MD_CTX(long nativePointer) {
            super(nativePointer);
        }

        /* access modifiers changed from: package-private */
        public void doFree(long context) {
            NativeCrypto.EVP_MD_CTX_destroy(context);
        }
    }

    static final class EVP_PKEY extends NativeRef {
        EVP_PKEY(long nativePointer) {
            super(nativePointer);
        }

        /* access modifiers changed from: package-private */
        public void doFree(long context) {
            NativeCrypto.EVP_PKEY_free(context);
        }
    }

    static final class EVP_PKEY_CTX extends NativeRef {
        EVP_PKEY_CTX(long nativePointer) {
            super(nativePointer);
        }

        /* access modifiers changed from: package-private */
        public void doFree(long context) {
            NativeCrypto.EVP_PKEY_CTX_free(context);
        }
    }

    static final class HMAC_CTX extends NativeRef {
        HMAC_CTX(long nativePointer) {
            super(nativePointer);
        }

        /* access modifiers changed from: package-private */
        public void doFree(long context) {
            NativeCrypto.HMAC_CTX_free(context);
        }
    }

    static final class SSL_SESSION extends NativeRef {
        SSL_SESSION(long nativePointer) {
            super(nativePointer);
        }

        /* access modifiers changed from: package-private */
        public void doFree(long context) {
            NativeCrypto.SSL_SESSION_free(context);
        }
    }

    /* access modifiers changed from: package-private */
    public abstract void doFree(long j);

    NativeRef(long context2) {
        if (context2 != 0) {
            this.context = context2;
            return;
        }
        throw new NullPointerException("context == 0");
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (!(o instanceof NativeRef)) {
            return false;
        }
        if (((NativeRef) o).context == this.context) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return (int) this.context;
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            if (this.context != 0) {
                doFree(this.context);
            }
        } finally {
            super.finalize();
        }
    }
}
