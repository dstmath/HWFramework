package java.util;

/* compiled from: Timer */
class TaskQueue {
    static final /* synthetic */ boolean -assertionsDisabled = (TaskQueue.class.desiredAssertionStatus() ^ 1);
    private TimerTask[] queue = new TimerTask[128];
    private int size = 0;

    TaskQueue() {
    }

    int size() {
        return this.size;
    }

    void add(TimerTask task) {
        if (this.size + 1 == this.queue.length) {
            this.queue = (TimerTask[]) Arrays.copyOf(this.queue, this.queue.length * 2);
        }
        TimerTask[] timerTaskArr = this.queue;
        int i = this.size + 1;
        this.size = i;
        timerTaskArr[i] = task;
        fixUp(this.size);
    }

    TimerTask getMin() {
        return this.queue[1];
    }

    TimerTask get(int i) {
        return this.queue[i];
    }

    void removeMin() {
        this.queue[1] = this.queue[this.size];
        TimerTask[] timerTaskArr = this.queue;
        int i = this.size;
        this.size = i - 1;
        timerTaskArr[i] = null;
        fixDown(1);
    }

    void quickRemove(int i) {
        if (-assertionsDisabled || i <= this.size) {
            this.queue[i] = this.queue[this.size];
            TimerTask[] timerTaskArr = this.queue;
            int i2 = this.size;
            this.size = i2 - 1;
            timerTaskArr[i2] = null;
            return;
        }
        throw new AssertionError();
    }

    void rescheduleMin(long newTime) {
        this.queue[1].nextExecutionTime = newTime;
        fixDown(1);
    }

    boolean isEmpty() {
        return this.size == 0;
    }

    void clear() {
        for (int i = 1; i <= this.size; i++) {
            this.queue[i] = null;
        }
        this.size = 0;
    }

    private void fixUp(int k) {
        while (k > 1) {
            int j = k >> 1;
            if (this.queue[j].nextExecutionTime > this.queue[k].nextExecutionTime) {
                TimerTask tmp = this.queue[j];
                this.queue[j] = this.queue[k];
                this.queue[k] = tmp;
                k = j;
            } else {
                return;
            }
        }
    }

    private void fixDown(int k) {
        while (true) {
            int j = k << 1;
            if (j <= this.size && j > 0) {
                if (j < this.size && this.queue[j].nextExecutionTime > this.queue[j + 1].nextExecutionTime) {
                    j++;
                }
                if (this.queue[k].nextExecutionTime > this.queue[j].nextExecutionTime) {
                    TimerTask tmp = this.queue[j];
                    this.queue[j] = this.queue[k];
                    this.queue[k] = tmp;
                    k = j;
                } else {
                    return;
                }
            }
            return;
        }
    }

    void heapify() {
        for (int i = this.size / 2; i >= 1; i--) {
            fixDown(i);
        }
    }
}
