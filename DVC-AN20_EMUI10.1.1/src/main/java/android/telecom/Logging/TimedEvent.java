package android.telecom.Logging;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class TimedEvent<T> {
    public abstract T getKey();

    public abstract long getTime();

    public static <T> Map<T, Double> averageTimings(Collection<? extends TimedEvent<T>> events) {
        HashMap hashMap = new HashMap();
        HashMap hashMap2 = new HashMap();
        for (TimedEvent<T> entry : events) {
            if (hashMap.containsKey(entry.getKey())) {
                hashMap.put(entry.getKey(), Integer.valueOf(((Integer) hashMap.get(entry.getKey())).intValue() + 1));
                hashMap2.put(entry.getKey(), Double.valueOf(((Double) hashMap2.get(entry.getKey())).doubleValue() + ((double) entry.getTime())));
            } else {
                hashMap.put(entry.getKey(), 1);
                hashMap2.put(entry.getKey(), Double.valueOf((double) entry.getTime()));
            }
        }
        for (Map.Entry<T, Double> entry2 : hashMap2.entrySet()) {
            hashMap2.put(entry2.getKey(), Double.valueOf(entry2.getValue().doubleValue() / ((double) ((Integer) hashMap.get(entry2.getKey())).intValue())));
        }
        return hashMap2;
    }
}
