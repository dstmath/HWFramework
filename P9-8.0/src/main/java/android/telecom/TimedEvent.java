package android.telecom;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public abstract class TimedEvent<T> {
    public abstract T getKey();

    public abstract long getTime();

    public static <T> Map<T, Double> averageTimings(Collection<? extends TimedEvent<T>> events) {
        HashMap<T, Integer> counts = new HashMap();
        HashMap<T, Double> result = new HashMap();
        for (TimedEvent<T> entry : events) {
            if (counts.containsKey(entry.getKey())) {
                counts.put(entry.getKey(), Integer.valueOf(((Integer) counts.get(entry.getKey())).intValue() + 1));
                result.put(entry.getKey(), Double.valueOf(((Double) result.get(entry.getKey())).doubleValue() + ((double) entry.getTime())));
            } else {
                counts.put(entry.getKey(), Integer.valueOf(1));
                result.put(entry.getKey(), Double.valueOf((double) entry.getTime()));
            }
        }
        for (Entry<T, Double> entry2 : result.entrySet()) {
            result.put(entry2.getKey(), Double.valueOf(((Double) entry2.getValue()).doubleValue() / ((double) ((Integer) counts.get(entry2.getKey())).intValue())));
        }
        return result;
    }
}
