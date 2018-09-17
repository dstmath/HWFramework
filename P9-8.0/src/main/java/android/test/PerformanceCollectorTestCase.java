package android.test;

import android.os.PerformanceCollector;
import android.os.PerformanceCollector.PerformanceResultsWriter;

@Deprecated
public interface PerformanceCollectorTestCase {
    public static final PerformanceCollector mPerfCollector = new PerformanceCollector();

    void setPerformanceResultsWriter(PerformanceResultsWriter performanceResultsWriter);
}
