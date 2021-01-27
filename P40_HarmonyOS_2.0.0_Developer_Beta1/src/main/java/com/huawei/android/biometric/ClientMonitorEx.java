package com.huawei.android.biometric;

import android.os.IBinder;
import com.android.server.biometrics.AuthenticationClient;
import com.android.server.biometrics.ClientMonitor;
import com.android.server.biometrics.EnrollClient;

public class ClientMonitorEx {
    private static final int STOP_ERROR_CODE = 3;
    private ClientMonitor mClientMonitor;

    public void setClientMonitor(ClientMonitor clientMonitor) {
        this.mClientMonitor = clientMonitor;
    }

    public String getOwnerString() {
        ClientMonitor clientMonitor = this.mClientMonitor;
        if (clientMonitor != null) {
            return clientMonitor.getOwnerString();
        }
        return "";
    }

    public boolean isAuthenticationClient() {
        ClientMonitor clientMonitor = this.mClientMonitor;
        if (clientMonitor != null && (clientMonitor instanceof AuthenticationClient)) {
            return true;
        }
        return false;
    }

    public boolean isEnrollClient() {
        ClientMonitor clientMonitor = this.mClientMonitor;
        if (clientMonitor != null && (clientMonitor instanceof EnrollClient)) {
            return true;
        }
        return false;
    }

    public int stop(boolean isInitiatedByClient) {
        ClientMonitor clientMonitor = this.mClientMonitor;
        if (clientMonitor != null) {
            return clientMonitor.stop(isInitiatedByClient);
        }
        return 3;
    }

    public IBinder getToken() {
        ClientMonitor clientMonitor = this.mClientMonitor;
        if (clientMonitor != null) {
            return clientMonitor.getToken();
        }
        return null;
    }

    public void destroy() {
        ClientMonitor clientMonitor = this.mClientMonitor;
        if (clientMonitor != null) {
            clientMonitor.destroy();
        }
    }
}
