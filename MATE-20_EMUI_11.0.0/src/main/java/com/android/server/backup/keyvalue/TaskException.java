package com.android.server.backup.keyvalue;

import com.android.internal.util.Preconditions;

class TaskException extends BackupException {
    private static final int DEFAULT_STATUS = -1000;
    private final boolean mStateCompromised;
    private final int mStatus;

    static TaskException stateCompromised() {
        return new TaskException(true, -1000);
    }

    static TaskException stateCompromised(Exception cause) {
        if (cause instanceof TaskException) {
            return new TaskException(cause, true, ((TaskException) cause).getStatus());
        }
        return new TaskException(cause, true, -1000);
    }

    static TaskException forStatus(int status) {
        Preconditions.checkArgument(status != 0, "Exception based on TRANSPORT_OK");
        return new TaskException(false, status);
    }

    static TaskException causedBy(Exception cause) {
        if (cause instanceof TaskException) {
            return (TaskException) cause;
        }
        return new TaskException(cause, false, -1000);
    }

    static TaskException create() {
        return new TaskException(false, -1000);
    }

    private TaskException(Exception cause, boolean stateCompromised, int status) {
        super(cause);
        this.mStateCompromised = stateCompromised;
        this.mStatus = status;
    }

    private TaskException(boolean stateCompromised, int status) {
        this.mStateCompromised = stateCompromised;
        this.mStatus = status;
    }

    /* access modifiers changed from: package-private */
    public boolean isStateCompromised() {
        return this.mStateCompromised;
    }

    /* access modifiers changed from: package-private */
    public int getStatus() {
        return this.mStatus;
    }
}
