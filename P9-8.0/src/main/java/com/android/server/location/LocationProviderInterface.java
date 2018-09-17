package com.android.server.location;

import android.os.Bundle;
import android.os.WorkSource;
import com.android.internal.location.ProviderProperties;
import com.android.internal.location.ProviderRequest;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public interface LocationProviderInterface {
    void disable();

    void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr);

    void enable();

    String getName();

    ProviderProperties getProperties();

    int getStatus(Bundle bundle);

    long getStatusUpdateTime();

    boolean isEnabled();

    boolean sendExtraCommand(String str, Bundle bundle);

    void setRequest(ProviderRequest providerRequest, WorkSource workSource);
}
