package com.android.server.accounts;

import android.accounts.AuthenticatorDescription;
import android.content.pm.RegisteredServicesCache;
import android.content.pm.RegisteredServicesCacheListener;
import android.os.Handler;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Collection;

public interface IAccountAuthenticatorCache {
    void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr, int i);

    Collection<RegisteredServicesCache.ServiceInfo<AuthenticatorDescription>> getAllServices(int i);

    boolean getBindInstantServiceAllowed(int i);

    RegisteredServicesCache.ServiceInfo<AuthenticatorDescription> getServiceInfo(AuthenticatorDescription authenticatorDescription, int i);

    void invalidateCache(int i);

    void setBindInstantServiceAllowed(int i, boolean z);

    void setListener(RegisteredServicesCacheListener<AuthenticatorDescription> registeredServicesCacheListener, Handler handler);

    void updateServices(int i);
}
