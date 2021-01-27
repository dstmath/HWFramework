package android.os;

import android.util.Log;
import com.android.internal.util.FastPrintWriter;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TransactionTracker {
    private Map<String, Long> mTraces;

    private void resetTraces() {
        synchronized (this) {
            this.mTraces = new HashMap();
        }
    }

    TransactionTracker() {
        resetTraces();
    }

    public void addTrace(Throwable tr) {
        String trace = Log.getStackTraceString(tr);
        synchronized (this) {
            if (this.mTraces.containsKey(trace)) {
                this.mTraces.put(trace, Long.valueOf(this.mTraces.get(trace).longValue() + 1));
            } else {
                this.mTraces.put(trace, 1L);
            }
        }
    }

    public void writeTracesToFile(ParcelFileDescriptor fd) {
        if (!this.mTraces.isEmpty()) {
            PrintWriter pw = new FastPrintWriter(new FileOutputStream(fd.getFileDescriptor()));
            synchronized (this) {
                for (String trace : this.mTraces.keySet()) {
                    pw.println("Count: " + this.mTraces.get(trace));
                    pw.println("Trace: " + trace);
                    pw.println();
                }
            }
            pw.flush();
        }
    }

    public String dumpTopTrace(int topTrace) {
        StringBuilder sb = new StringBuilder();
        synchronized (this) {
            List<Map.Entry<String, Long>> traces = (List) this.mTraces.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).collect(Collectors.toList());
            int dumpSize = traces.size() > topTrace ? topTrace : traces.size();
            for (int i = 0; i < dumpSize; i++) {
                sb.append("Count: " + traces.get(i).getValue());
                sb.append(System.lineSeparator());
                sb.append("Trace: " + traces.get(i).getKey());
                sb.append(System.lineSeparator());
            }
        }
        return sb.toString();
    }

    public void clearTraces() {
        resetTraces();
    }
}
