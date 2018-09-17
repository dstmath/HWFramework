package com.android.org.conscrypt;

import java.lang.reflect.Method;
import java.net.Socket;
import javax.crypto.SecretKey;
import javax.net.ssl.SSLEngine;

@Deprecated
public class DuckTypedPSKKeyManager implements PSKKeyManager {
    private final Object mDelegate;

    private DuckTypedPSKKeyManager(Object delegate) {
        this.mDelegate = delegate;
    }

    public static DuckTypedPSKKeyManager getInstance(Object obj) throws NoSuchMethodException {
        Class<?> sourceClass = obj.getClass();
        for (Method targetMethod : PSKKeyManager.class.getMethods()) {
            if (!targetMethod.isSynthetic()) {
                Method sourceMethod = sourceClass.getMethod(targetMethod.getName(), targetMethod.getParameterTypes());
                Class<?> sourceReturnType = sourceMethod.getReturnType();
                Class<?> targetReturnType = targetMethod.getReturnType();
                if (!targetReturnType.isAssignableFrom(sourceReturnType)) {
                    throw new NoSuchMethodException(sourceMethod + " return value (" + sourceReturnType + ") incompatible with target return value (" + targetReturnType + ")");
                }
            }
        }
        return new DuckTypedPSKKeyManager(obj);
    }

    public String chooseServerKeyIdentityHint(Socket socket) {
        try {
            return (String) this.mDelegate.getClass().getMethod("chooseServerKeyIdentityHint", new Class[]{Socket.class}).invoke(this.mDelegate, new Object[]{socket});
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke chooseServerKeyIdentityHint", e);
        }
    }

    public String chooseServerKeyIdentityHint(SSLEngine engine) {
        try {
            return (String) this.mDelegate.getClass().getMethod("chooseServerKeyIdentityHint", new Class[]{SSLEngine.class}).invoke(this.mDelegate, new Object[]{engine});
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke chooseServerKeyIdentityHint", e);
        }
    }

    public String chooseClientKeyIdentity(String identityHint, Socket socket) {
        try {
            return (String) this.mDelegate.getClass().getMethod("chooseClientKeyIdentity", new Class[]{String.class, Socket.class}).invoke(this.mDelegate, new Object[]{identityHint, socket});
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke chooseClientKeyIdentity", e);
        }
    }

    public String chooseClientKeyIdentity(String identityHint, SSLEngine engine) {
        try {
            return (String) this.mDelegate.getClass().getMethod("chooseClientKeyIdentity", new Class[]{String.class, SSLEngine.class}).invoke(this.mDelegate, new Object[]{identityHint, engine});
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke chooseClientKeyIdentity", e);
        }
    }

    public SecretKey getKey(String identityHint, String identity, Socket socket) {
        try {
            return (SecretKey) this.mDelegate.getClass().getMethod("getKey", new Class[]{String.class, String.class, Socket.class}).invoke(this.mDelegate, new Object[]{identityHint, identity, socket});
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke getKey", e);
        }
    }

    public SecretKey getKey(String identityHint, String identity, SSLEngine engine) {
        try {
            return (SecretKey) this.mDelegate.getClass().getMethod("getKey", new Class[]{String.class, String.class, SSLEngine.class}).invoke(this.mDelegate, new Object[]{identityHint, identity, engine});
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke getKey", e);
        }
    }
}
