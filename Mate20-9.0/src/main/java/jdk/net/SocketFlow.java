package jdk.net;

public class SocketFlow {
    public static final int HIGH_PRIORITY = 2;
    public static final int NORMAL_PRIORITY = 1;
    private static final int UNSET = -1;
    private long bandwidth = -1;
    private int priority = 1;
    private Status status = Status.NO_STATUS;

    public enum Status {
        NO_STATUS,
        OK,
        NO_PERMISSION,
        NOT_CONNECTED,
        NOT_SUPPORTED,
        ALREADY_CREATED,
        IN_PROGRESS,
        OTHER
    }

    private SocketFlow() {
    }

    public static SocketFlow create() {
        return new SocketFlow();
    }

    public SocketFlow priority(int priority2) {
        if (priority2 == 1 || priority2 == 2) {
            this.priority = priority2;
            return this;
        }
        throw new IllegalArgumentException("invalid priority");
    }

    public SocketFlow bandwidth(long bandwidth2) {
        if (bandwidth2 >= 0) {
            this.bandwidth = bandwidth2;
            return this;
        }
        throw new IllegalArgumentException("invalid bandwidth");
    }

    public int priority() {
        return this.priority;
    }

    public long bandwidth() {
        return this.bandwidth;
    }

    public Status status() {
        return this.status;
    }
}
