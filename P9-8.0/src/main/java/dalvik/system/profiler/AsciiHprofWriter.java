package dalvik.system.profiler;

import dalvik.system.profiler.HprofData.Sample;
import dalvik.system.profiler.HprofData.StackTrace;
import dalvik.system.profiler.HprofData.ThreadEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public final class AsciiHprofWriter {
    private static final Comparator<Sample> SAMPLE_COMPARATOR = new Comparator<Sample>() {
        public int compare(Sample s1, Sample s2) {
            return s2.count - s1.count;
        }
    };
    private final HprofData data;
    private final PrintWriter out;

    public static void write(HprofData data, OutputStream outputStream) throws IOException {
        new AsciiHprofWriter(data, outputStream).write();
    }

    private AsciiHprofWriter(HprofData data, OutputStream outputStream) {
        this.data = data;
        this.out = new PrintWriter(outputStream);
    }

    private void write() throws IOException {
        StackTrace stackTrace;
        for (ThreadEvent e : this.data.getThreadHistory()) {
            this.out.println(e);
        }
        List<Sample> samples = new ArrayList(this.data.getSamples());
        Collections.sort(samples, SAMPLE_COMPARATOR);
        int total = 0;
        for (Sample sample : samples) {
            total += sample.count;
            this.out.printf("TRACE %d: (thread=%d)\n", new Object[]{Integer.valueOf(stackTrace.stackTraceId), Integer.valueOf(sample.stackTrace.threadId)});
            for (StackTraceElement e2 : stackTrace.stackFrames) {
                this.out.printf("\t%s\n", new Object[]{e2});
            }
        }
        Date now = new Date(this.data.getStartMillis());
        this.out.printf("CPU SAMPLES BEGIN (total = %d) %ta %tb %td %tT %tY\n", new Object[]{Integer.valueOf(total), now, now, now, now, now});
        this.out.printf("rank   self  accum   count trace method\n", new Object[0]);
        int rank = 0;
        double accum = 0.0d;
        for (Sample sample2 : samples) {
            rank++;
            stackTrace = sample2.stackTrace;
            accum += ((double) sample2.count) / ((double) total);
            this.out.printf("% 4d% 6.2f%%% 6.2f%% % 7d % 5d %s.%s\n", new Object[]{Integer.valueOf(rank), Double.valueOf(100.0d * self), Double.valueOf(100.0d * accum), Integer.valueOf(sample2.count), Integer.valueOf(stackTrace.stackTraceId), stackTrace.stackFrames[0].getClassName(), stackTrace.stackFrames[0].getMethodName()});
        }
        this.out.printf("CPU SAMPLES END\n", new Object[0]);
        this.out.flush();
    }
}
