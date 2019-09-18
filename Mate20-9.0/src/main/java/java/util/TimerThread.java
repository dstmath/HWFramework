package java.util;

/* compiled from: Timer */
class TimerThread extends Thread {
    boolean newTasksMayBeScheduled = true;
    private TaskQueue queue;

    TimerThread(TaskQueue queue2) {
        this.queue = queue2;
    }

    public void run() {
        try {
            mainLoop();
            synchronized (this.queue) {
                this.newTasksMayBeScheduled = false;
                this.queue.clear();
            }
        } catch (Throwable th) {
            synchronized (this.queue) {
                this.newTasksMayBeScheduled = false;
                this.queue.clear();
                throw th;
            }
        }
    }

    private void mainLoop() {
        do {
        } while (processTask());
    }

    /* JADX WARNING: Code restructure failed: missing block: B:39:0x006b, code lost:
        r3 = r5;
        r5 = r7;
        r7 = r9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x006e, code lost:
        if (r7 != false) goto L_0x0077;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:?, code lost:
        r14.queue.wait(r5 - r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x0078, code lost:
        r1 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x0079, code lost:
        if (r7 == false) goto L_0x0086;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:?, code lost:
        r1.run();
     */
    private boolean processTask() {
        long j;
        try {
            synchronized (this.queue) {
                while (this.queue.isEmpty() && this.newTasksMayBeScheduled) {
                    this.queue.wait();
                }
                boolean z = false;
                if (this.queue.isEmpty()) {
                    return false;
                }
                TimerTask task = this.queue.getMin();
                synchronized (task.lock) {
                    if (task.state == 3) {
                        this.queue.removeMin();
                        return true;
                    }
                    long currentTime = System.currentTimeMillis();
                    long executionTime = task.nextExecutionTime;
                    if (executionTime <= currentTime) {
                        z = true;
                    }
                    boolean taskFired = z;
                    if (z) {
                        if (task.period == 0) {
                            this.queue.removeMin();
                            task.state = 2;
                        } else {
                            TaskQueue taskQueue = this.queue;
                            if (task.period < 0) {
                                j = currentTime - task.period;
                            } else {
                                j = task.period + executionTime;
                            }
                            taskQueue.rescheduleMin(j);
                        }
                    }
                }
            }
        } catch (InterruptedException e) {
        }
        return true;
    }
}
