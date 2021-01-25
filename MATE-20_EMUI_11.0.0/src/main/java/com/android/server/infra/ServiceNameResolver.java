package com.android.server.infra;

import java.io.PrintWriter;

public interface ServiceNameResolver {

    public interface NameResolverListener {
        void onNameResolved(int i, String str, boolean z);
    }

    void dumpShort(PrintWriter printWriter);

    void dumpShort(PrintWriter printWriter, int i);

    String getDefaultServiceName(int i);

    default void setOnTemporaryServiceNameChangedCallback(NameResolverListener callback) {
    }

    default String getServiceName(int userId) {
        return getDefaultServiceName(userId);
    }

    default boolean isTemporary(int userId) {
        return false;
    }

    default void setTemporaryService(int userId, String componentName, int durationMs) {
        throw new UnsupportedOperationException("temporary user not supported");
    }

    default void resetTemporaryService(int userId) {
        throw new UnsupportedOperationException("temporary user not supported");
    }

    default boolean setDefaultServiceEnabled(int userId, boolean enabled) {
        throw new UnsupportedOperationException("changing default service not supported");
    }

    default boolean isDefaultServiceEnabled(int userId) {
        throw new UnsupportedOperationException("checking default service not supported");
    }
}
