package java.util;

/* compiled from: Timer */
class TimerThread extends Thread {
    boolean newTasksMayBeScheduled;
    private TaskQueue queue;

    TimerThread(TaskQueue queue) {
        this.newTasksMayBeScheduled = true;
        this.queue = queue;
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
            }
            this.newTasksMayBeScheduled = false;
            this.queue.clear();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void mainLoop() {
        while (true) {
            synchronized (this.queue) {
                while (true) {
                    if (this.queue.isEmpty() && this.newTasksMayBeScheduled) {
                        this.queue.wait();
                    } else if (this.queue.isEmpty()) {
                        break;
                    } else {
                        TimerTask task = this.queue.getMin();
                        synchronized (task.lock) {
                            if (task.state == 3) {
                                this.queue.removeMin();
                            } else {
                                long currentTime = System.currentTimeMillis();
                                long executionTime = task.nextExecutionTime;
                                boolean taskFired = executionTime <= currentTime;
                                if (taskFired) {
                                    if (task.period == 0) {
                                        this.queue.removeMin();
                                        task.state = 2;
                                    } else {
                                        long j;
                                        TaskQueue taskQueue = this.queue;
                                        if (task.period < 0) {
                                            j = currentTime - task.period;
                                        } else {
                                            j = task.period + executionTime;
                                        }
                                        taskQueue.rescheduleMin(j);
                                    }
                                }
                                if (!taskFired) {
                                    this.queue.wait(executionTime - currentTime);
                                }
                                try {
                                    if (taskFired) {
                                        task.run();
                                    }
                                } catch (InterruptedException e) {
                                }
                            }
                        }
                    }
                }
                break;
            }
        }
    }
}
