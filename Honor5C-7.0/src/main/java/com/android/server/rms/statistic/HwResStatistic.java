package com.android.server.rms.statistic;

import android.os.Bundle;
import com.android.server.rms.config.HwConfigReader;
import java.util.Map;

public interface HwResStatistic {
    boolean acquire(int i);

    boolean init(HwConfigReader hwConfigReader);

    Map<String, HwResRecord> obtainResRecordMap();

    boolean resetResRecordMap(Map<String, HwResRecord> map);

    Bundle sample(int i);

    boolean statistic(Bundle bundle);
}
