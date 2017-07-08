package java.util;

import java.util.regex.Pattern;

/* compiled from: Timer */
class TaskQueue {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private TimerTask[] queue;
    private int size;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.TaskQueue.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.TaskQueue.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.TaskQueue.<clinit>():void");
    }

    TaskQueue() {
        this.queue = new TimerTask[Pattern.CANON_EQ];
        this.size = 0;
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
        if (!-assertionsDisabled) {
            if ((i <= this.size ? 1 : null) == null) {
                throw new AssertionError();
            }
        }
        this.queue[i] = this.queue[this.size];
        TimerTask[] timerTaskArr = this.queue;
        int i2 = this.size;
        this.size = i2 - 1;
        timerTaskArr[i2] = null;
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
