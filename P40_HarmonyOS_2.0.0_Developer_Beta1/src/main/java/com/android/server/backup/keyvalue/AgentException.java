package com.android.server.backup.keyvalue;

class AgentException extends BackupException {
    private final boolean mTransitory;

    static AgentException transitory() {
        return new AgentException(true);
    }

    static AgentException transitory(Exception cause) {
        return new AgentException(true, cause);
    }

    static AgentException permanent() {
        return new AgentException(false);
    }

    static AgentException permanent(Exception cause) {
        return new AgentException(false, cause);
    }

    private AgentException(boolean transitory) {
        this.mTransitory = transitory;
    }

    private AgentException(boolean transitory, Exception cause) {
        super(cause);
        this.mTransitory = transitory;
    }

    /* access modifiers changed from: package-private */
    public boolean isTransitory() {
        return this.mTransitory;
    }
}
