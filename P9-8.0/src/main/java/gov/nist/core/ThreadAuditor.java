package gov.nist.core;

import java.util.HashMap;
import java.util.Map;

public class ThreadAuditor {
    private long pingIntervalInMillisecs = 0;
    private Map<Thread, ThreadHandle> threadHandles = new HashMap();

    public class ThreadHandle {
        private boolean isThreadActive = false;
        private Thread thread = Thread.currentThread();
        private ThreadAuditor threadAuditor;

        public ThreadHandle(ThreadAuditor aThreadAuditor) {
            this.threadAuditor = aThreadAuditor;
        }

        public boolean isThreadActive() {
            return this.isThreadActive;
        }

        protected void setThreadActive(boolean value) {
            this.isThreadActive = value;
        }

        public Thread getThread() {
            return this.thread;
        }

        public void ping() {
            this.threadAuditor.ping(this);
        }

        public long getPingIntervalInMillisecs() {
            return this.threadAuditor.getPingIntervalInMillisecs();
        }

        public String toString() {
            return new StringBuffer().append("Thread Name: ").append(this.thread.getName()).append(", Alive: ").append(this.thread.isAlive()).toString();
        }
    }

    public long getPingIntervalInMillisecs() {
        return this.pingIntervalInMillisecs;
    }

    public void setPingIntervalInMillisecs(long value) {
        this.pingIntervalInMillisecs = value;
    }

    public boolean isEnabled() {
        return this.pingIntervalInMillisecs > 0;
    }

    public synchronized ThreadHandle addCurrentThread() {
        ThreadHandle threadHandle;
        threadHandle = new ThreadHandle(this);
        if (isEnabled()) {
            this.threadHandles.put(Thread.currentThread(), threadHandle);
        }
        return threadHandle;
    }

    public synchronized void removeThread(Thread thread) {
        this.threadHandles.remove(thread);
    }

    public synchronized void ping(ThreadHandle threadHandle) {
        threadHandle.setThreadActive(true);
    }

    public synchronized void reset() {
        this.threadHandles.clear();
    }

    public synchronized String auditThreads() {
        String auditReport;
        auditReport = null;
        for (ThreadHandle threadHandle : this.threadHandles.values()) {
            if (!threadHandle.isThreadActive()) {
                Thread thread = threadHandle.getThread();
                if (auditReport == null) {
                    auditReport = "Thread Auditor Report:\n";
                }
                auditReport = auditReport + "   Thread [" + thread.getName() + "] has failed to respond to an audit request.\n";
            }
            threadHandle.setThreadActive(false);
        }
        return auditReport;
    }

    public synchronized String toString() {
        String toString;
        toString = "Thread Auditor - List of monitored threads:\n";
        for (ThreadHandle threadHandle : this.threadHandles.values()) {
            toString = toString + "   " + threadHandle.toString() + Separators.RETURN;
        }
        return toString;
    }
}
