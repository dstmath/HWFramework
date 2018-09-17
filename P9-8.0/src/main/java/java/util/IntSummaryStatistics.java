package java.util;

import java.util.function.IntConsumer;

public class IntSummaryStatistics implements IntConsumer {
    private long count;
    private int max = Integer.MIN_VALUE;
    private int min = Integer.MAX_VALUE;
    private long sum;

    public void accept(int value) {
        this.count++;
        this.sum += (long) value;
        this.min = Math.min(this.min, value);
        this.max = Math.max(this.max, value);
    }

    public void combine(IntSummaryStatistics other) {
        this.count += other.count;
        this.sum += other.sum;
        this.min = Math.min(this.min, other.min);
        this.max = Math.max(this.max, other.max);
    }

    public final long getCount() {
        return this.count;
    }

    public final long getSum() {
        return this.sum;
    }

    public final int getMin() {
        return this.min;
    }

    public final int getMax() {
        return this.max;
    }

    public final double getAverage() {
        return getCount() > 0 ? ((double) getSum()) / ((double) getCount()) : 0.0d;
    }

    public String toString() {
        return String.format("%s{count=%d, sum=%d, min=%d, average=%f, max=%d}", getClass().getSimpleName(), Long.valueOf(getCount()), Long.valueOf(getSum()), Integer.valueOf(getMin()), Double.valueOf(getAverage()), Integer.valueOf(getMax()));
    }
}
