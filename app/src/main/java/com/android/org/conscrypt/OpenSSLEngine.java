package com.android.org.conscrypt;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;

public class OpenSSLEngine {
    private static final Object mLoadingLock = null;
    private final long ctx;

    private static class BoringSSL {
        public static final OpenSSLEngine INSTANCE = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.conscrypt.OpenSSLEngine.BoringSSL.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.conscrypt.OpenSSLEngine.BoringSSL.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.OpenSSLEngine.BoringSSL.<clinit>():void");
        }

        private BoringSSL() {
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.conscrypt.OpenSSLEngine.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.conscrypt.OpenSSLEngine.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.OpenSSLEngine.<clinit>():void");
    }

    /* synthetic */ OpenSSLEngine(OpenSSLEngine openSSLEngine) {
        this();
    }

    public static OpenSSLEngine getInstance(String engine) throws IllegalArgumentException {
        if (NativeCrypto.isBoringSSL) {
            return BoringSSL.INSTANCE;
        }
        if (engine == null) {
            throw new NullPointerException("engine == null");
        }
        long engineCtx;
        synchronized (mLoadingLock) {
            engineCtx = NativeCrypto.ENGINE_by_id(engine);
            if (engineCtx == 0) {
                throw new IllegalArgumentException("Unknown ENGINE id: " + engine);
            }
            NativeCrypto.ENGINE_add(engineCtx);
        }
        return new OpenSSLEngine(engineCtx);
    }

    private OpenSSLEngine() {
        this.ctx = 0;
    }

    private OpenSSLEngine(long engineCtx) {
        this.ctx = engineCtx;
        if (NativeCrypto.ENGINE_init(engineCtx) == 0) {
            NativeCrypto.ENGINE_free(engineCtx);
            throw new IllegalArgumentException("Could not initialize engine");
        }
    }

    public PrivateKey getPrivateKeyById(String id) throws InvalidKeyException {
        if (id == null) {
            throw new NullPointerException("id == null");
        }
        long keyRef = NativeCrypto.ENGINE_load_private_key(this.ctx, id);
        if (keyRef == 0) {
            return null;
        }
        try {
            return new OpenSSLKey(keyRef, this, id).getPrivateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new InvalidKeyException(e);
        }
    }

    long getEngineContext() {
        return this.ctx;
    }

    protected void finalize() throws Throwable {
        try {
            if (!NativeCrypto.isBoringSSL) {
                NativeCrypto.ENGINE_finish(this.ctx);
                NativeCrypto.ENGINE_free(this.ctx);
            }
            super.finalize();
        } catch (Throwable th) {
            super.finalize();
        }
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof OpenSSLEngine)) {
            return false;
        }
        OpenSSLEngine other = (OpenSSLEngine) o;
        if (other.getEngineContext() == this.ctx) {
            return true;
        }
        String id = NativeCrypto.ENGINE_get_id(this.ctx);
        if (id == null) {
            return false;
        }
        return id.equals(NativeCrypto.ENGINE_get_id(other.getEngineContext()));
    }

    public int hashCode() {
        return (int) this.ctx;
    }
}
