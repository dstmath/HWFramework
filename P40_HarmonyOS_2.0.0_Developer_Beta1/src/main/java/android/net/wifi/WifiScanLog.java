package android.net.wifi;

import android.util.ArrayMap;
import android.util.Log;
import android.util.Singleton;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

public class WifiScanLog {
    public static final String COM_KEY = "Key#00:";
    private static final Boolean DBG = false;
    public static final String EVENT_KEY1 = "1";
    public static final String EVENT_KEY10 = "10";
    public static final String EVENT_KEY11 = "11";
    public static final String EVENT_KEY12 = "12";
    public static final String EVENT_KEY13 = "13";
    public static final String EVENT_KEY14 = "14";
    public static final String EVENT_KEY15 = "15";
    public static final String EVENT_KEY16 = "16";
    public static final String EVENT_KEY17 = "17";
    public static final String EVENT_KEY18 = "18";
    public static final String EVENT_KEY19 = "19";
    public static final String EVENT_KEY2 = "2";
    public static final String EVENT_KEY20 = "20";
    public static final String EVENT_KEY21 = "21";
    public static final String EVENT_KEY22 = "22";
    public static final String EVENT_KEY23 = "23";
    public static final String EVENT_KEY24 = "24";
    public static final String EVENT_KEY25 = "25";
    public static final String EVENT_KEY26 = "26";
    public static final String EVENT_KEY27 = "27";
    public static final String EVENT_KEY28 = "28";
    public static final String EVENT_KEY29 = "29";
    public static final String EVENT_KEY3 = "3";
    public static final String EVENT_KEY30 = "30";
    public static final String EVENT_KEY31 = "31";
    public static final String EVENT_KEY32 = "32";
    public static final String EVENT_KEY33 = "33";
    public static final String EVENT_KEY34 = "34";
    public static final String EVENT_KEY35 = "35";
    public static final String EVENT_KEY36 = "36";
    public static final String EVENT_KEY37 = "37";
    public static final String EVENT_KEY38 = "38";
    public static final String EVENT_KEY39 = "39";
    public static final String EVENT_KEY4 = "4";
    public static final String EVENT_KEY40 = "40";
    public static final String EVENT_KEY41 = "41";
    public static final String EVENT_KEY42 = "42";
    public static final String EVENT_KEY43 = "43";
    public static final String EVENT_KEY44 = "44";
    public static final String EVENT_KEY45 = "45";
    public static final String EVENT_KEY46 = "46";
    public static final String EVENT_KEY47 = "47";
    public static final String EVENT_KEY48 = "48";
    public static final String EVENT_KEY49 = "49";
    public static final String EVENT_KEY5 = "5";
    public static final String EVENT_KEY50 = "50";
    public static final String EVENT_KEY51 = "51";
    public static final String EVENT_KEY52 = "52";
    public static final String EVENT_KEY53 = "53";
    public static final String EVENT_KEY54 = "54";
    public static final String EVENT_KEY55 = "55";
    public static final String EVENT_KEY56 = "56";
    public static final String EVENT_KEY57 = "57";
    public static final String EVENT_KEY58 = "58";
    public static final String EVENT_KEY59 = "59";
    public static final String EVENT_KEY6 = "6";
    public static final String EVENT_KEY7 = "7";
    public static final String EVENT_KEY8 = "8";
    public static final String EVENT_KEY9 = "9";
    private static final boolean HWFLOW;
    public static final String TAG = "WifiScanLog";
    private static final Singleton<WifiScanLog> gDefault = new Singleton<WifiScanLog>() {
        /* class android.net.wifi.WifiScanLog.AnonymousClass1 */

        /* access modifiers changed from: protected */
        @Override // android.util.Singleton
        public WifiScanLog create() {
            return new WifiScanLog();
        }
    };
    private ArrayMap<String, Integer> eventLogDuration;
    private ArrayMap<String, Long> eventLogStartTime;
    private ArrayMap<String, StringBuilder> eventLogs;

    static {
        boolean z = false;
        if (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4))) {
            z = true;
        }
        HWFLOW = z;
    }

    public static WifiScanLog getDefault() {
        return gDefault.get();
    }

    private WifiScanLog() {
        this.eventLogs = new ArrayMap<>();
        this.eventLogStartTime = new ArrayMap<>();
        this.eventLogDuration = new ArrayMap<>();
    }

    public synchronized void addEvent(String scanKey, String eventKey, String log, Object... params) {
        addEvent(scanKey, eventKey, params);
    }

    private synchronized void addEvent(String scanKey, String eventKey, Object... params) {
        long startTime;
        if (HWFLOW) {
            StringBuilder eventLog = this.eventLogs.get(scanKey);
            int lastTempDiffTime = -1;
            if (eventLog == null) {
                eventLog = new StringBuilder();
                startTime = System.currentTimeMillis();
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(startTime);
                eventLog.append(String.format(Locale.ROOT, " %tM:%tS.%tL", c, c, c));
                this.eventLogStartTime.put(scanKey, Long.valueOf(startTime));
                this.eventLogDuration.put(scanKey, 0);
            } else {
                startTime = this.eventLogStartTime.get(scanKey).longValue();
                lastTempDiffTime = this.eventLogDuration.get(scanKey).intValue();
            }
            int tempDiffTime = (int) (System.currentTimeMillis() - startTime);
            if (lastTempDiffTime != tempDiffTime) {
                eventLog.append("#");
                eventLog.append(tempDiffTime);
                this.eventLogDuration.put(scanKey, Integer.valueOf(tempDiffTime));
            }
            eventLog.append("$");
            eventLog.append(eventKey);
            if (params != null) {
                for (Object param : params) {
                    if (param == null) {
                        eventLog.append(',');
                        eventLog.append(param);
                    } else {
                        eventLog.append(',');
                        eventLog.append(param.toString().replaceAll("[$#,\n]", "*"));
                    }
                }
            }
            this.eventLogs.put(scanKey, eventLog);
            if (DBG.booleanValue()) {
                Log.i("WifiScanLogDBG", scanKey + eventLog.toString());
            }
        }
    }

    public synchronized void flush() {
        if (HWFLOW) {
            for (Map.Entry<String, StringBuilder> entry : this.eventLogs.entrySet()) {
                Log.i(TAG, entry.getKey() + entry.getValue().toString());
            }
            this.eventLogs.clear();
            this.eventLogStartTime.clear();
            this.eventLogDuration.clear();
        }
    }
}
