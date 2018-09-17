package com.android.server;

public class NativeDaemonTimeoutException extends NativeDaemonConnectorException {
    public NativeDaemonTimeoutException(String command, NativeDaemonEvent event) {
        super(command, event);
    }
}
