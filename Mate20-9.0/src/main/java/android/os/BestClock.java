package android.os;

import android.util.Log;
import java.time.Clock;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.Arrays;

public class BestClock extends SimpleClock {
    private static final String TAG = "BestClock";
    private final Clock[] clocks;

    public BestClock(ZoneId zone, Clock... clocks2) {
        super(zone);
        this.clocks = clocks2;
    }

    public long millis() {
        Clock[] clockArr = this.clocks;
        int length = clockArr.length;
        int i = 0;
        while (i < length) {
            try {
                return clockArr[i].millis();
            } catch (DateTimeException e) {
                Log.w(TAG, e.toString());
                i++;
            }
        }
        throw new DateTimeException("No clocks in " + Arrays.toString(this.clocks) + " were able to provide time");
    }
}
