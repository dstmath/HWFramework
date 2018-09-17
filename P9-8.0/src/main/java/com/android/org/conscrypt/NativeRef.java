package com.android.org.conscrypt;

public abstract class NativeRef {
    final long context;

    public static class EC_GROUP extends NativeRef {
        public EC_GROUP(long ctx) {
            super(ctx);
        }

        protected void finalize() throws Throwable {
            try {
                NativeCrypto.EC_GROUP_clear_free(this.context);
            } finally {
                super.finalize();
            }
        }
    }

    public static class EC_POINT extends NativeRef {
        public EC_POINT(long ctx) {
            super(ctx);
        }

        protected void finalize() throws Throwable {
            try {
                NativeCrypto.EC_POINT_clear_free(this.context);
            } finally {
                super.finalize();
            }
        }
    }

    public static class EVP_CIPHER_CTX extends NativeRef {
        public EVP_CIPHER_CTX(long ctx) {
            super(ctx);
        }

        protected void finalize() throws Throwable {
            try {
                NativeCrypto.EVP_CIPHER_CTX_free(this.context);
            } finally {
                super.finalize();
            }
        }
    }

    public static class EVP_MD_CTX extends NativeRef {
        public EVP_MD_CTX(long ctx) {
            super(ctx);
        }

        protected void finalize() throws Throwable {
            try {
                NativeCrypto.EVP_MD_CTX_destroy(this.context);
            } finally {
                super.finalize();
            }
        }
    }

    public static class EVP_PKEY extends NativeRef {
        public EVP_PKEY(long ctx) {
            super(ctx);
        }

        protected void finalize() throws Throwable {
            try {
                NativeCrypto.EVP_PKEY_free(this.context);
            } finally {
                super.finalize();
            }
        }
    }

    public static class EVP_PKEY_CTX extends NativeRef {
        public EVP_PKEY_CTX(long ctx) {
            super(ctx);
        }

        protected void finalize() throws Throwable {
            try {
                NativeCrypto.EVP_PKEY_CTX_free(this.context);
            } finally {
                super.finalize();
            }
        }
    }

    public static class HMAC_CTX extends NativeRef {
        public HMAC_CTX(long ctx) {
            super(ctx);
        }

        protected void finalize() throws Throwable {
            try {
                NativeCrypto.HMAC_CTX_free(this.context);
            } finally {
                super.finalize();
            }
        }
    }

    public NativeRef(long ctx) {
        if (ctx == 0) {
            throw new NullPointerException("ctx == 0");
        }
        this.context = ctx;
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
}
