package com.android.server;

public class NativeDaemonConnectorException extends Exception {
    private String mCmd;
    private NativeDaemonEvent mEvent;

    public NativeDaemonConnectorException(String detailMessage) {
        super(detailMessage);
    }

    public NativeDaemonConnectorException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public NativeDaemonConnectorException(String cmd, NativeDaemonEvent event) {
        super("command '" + cmd + "' failed with '" + event + "'");
        this.mCmd = cmd;
        this.mEvent = event;
    }

    public int getCode() {
        return this.mEvent != null ? this.mEvent.getCode() : -1;
    }

    public String getCmd() {
        return this.mCmd;
    }

    public IllegalArgumentException rethrowAsParcelableException() {
        throw new IllegalStateException(getMessage(), this);
    }
}
