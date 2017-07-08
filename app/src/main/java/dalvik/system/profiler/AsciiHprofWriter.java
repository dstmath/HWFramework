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
    private static final Comparator<Sample> SAMPLE_COMPARATOR = null;
    private final HprofData data;
    private final PrintWriter out;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: dalvik.system.profiler.AsciiHprofWriter.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: dalvik.system.profiler.AsciiHprofWriter.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: dalvik.system.profiler.AsciiHprofWriter.<clinit>():void");
    }

    public static void write(HprofData data, OutputStream outputStream) throws IOException {
        new AsciiHprofWriter(data, outputStream).write();
    }

    private AsciiHprofWriter(HprofData data, OutputStream outputStream) {
        this.data = data;
        this.out = new PrintWriter(outputStream);
    }

    private void write() throws IOException {
        for (ThreadEvent e : this.data.getThreadHistory()) {
            this.out.println((ThreadEvent) e$iterator.next());
        }
        List<Sample> samples = new ArrayList(this.data.getSamples());
        Collections.sort(samples, SAMPLE_COMPARATOR);
        int total = 0;
        for (Sample sample : samples) {
            StackTrace stackTrace = sample.stackTrace;
            total += sample.count;
            PrintWriter printWriter = this.out;
            r19 = new Object[2];
            r19[0] = Integer.valueOf(stackTrace.stackTraceId);
            r19[1] = Integer.valueOf(stackTrace.threadId);
            r0.printf("TRACE %d: (thread=%d)\n", r19);
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
            int count = sample2.count;
            double self = ((double) count) / ((double) total);
            accum += self;
            printWriter = this.out;
            r19 = new Object[7];
            r19[0] = Integer.valueOf(rank);
            r19[1] = Double.valueOf(100.0d * self);
            r19[2] = Double.valueOf(100.0d * accum);
            r19[3] = Integer.valueOf(count);
            r19[4] = Integer.valueOf(stackTrace.stackTraceId);
            r19[5] = stackTrace.stackFrames[0].getClassName();
            r19[6] = stackTrace.stackFrames[0].getMethodName();
            r0.printf("% 4d% 6.2f%%% 6.2f%% % 7d % 5d %s.%s\n", r19);
        }
        this.out.printf("CPU SAMPLES END\n", new Object[0]);
        this.out.flush();
    }
}
