package dalvik.system.profiler;

import dalvik.system.VMStack;

class DalvikThreadSampler implements ThreadSampler {
    private int depth;
    private StackTraceElement[][] mutableStackTraceElements;

    DalvikThreadSampler() {
    }

    public void setDepth(int depth) {
        this.depth = depth;
        this.mutableStackTraceElements = new StackTraceElement[(depth + 1)][];
        for (int i = 1; i < this.mutableStackTraceElements.length; i++) {
            this.mutableStackTraceElements[i] = new StackTraceElement[i];
        }
    }

    public StackTraceElement[] getStackTrace(Thread thread) {
        int count = VMStack.fillStackTraceElements(thread, this.mutableStackTraceElements[this.depth]);
        if (count == 0) {
            return null;
        }
        if (count < this.depth) {
            System.arraycopy(this.mutableStackTraceElements[this.depth], 0, this.mutableStackTraceElements[count], 0, count);
        }
        return this.mutableStackTraceElements[count];
    }
}
