package com.android.commands.monkey;

import android.app.IActivityManager;
import android.os.Environment;
import android.util.Log;
import android.view.IWindowManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MonkeyGetAppFrameRateEvent extends MonkeyEvent {
    private static final String LOG_FILE = new File(Environment.getExternalStorageDirectory(), "avgAppFrameRateOut.txt").getAbsolutePath();
    private static final Pattern NO_OF_FRAMES_PATTERN = Pattern.compile(".* ([0-9]*) frames rendered");
    private static final String TAG = "MonkeyGetAppFrameRateEvent";
    private static String sActivityName = null;
    private static float sDuration;
    private static int sEndFrameNo;
    private static long sEndTime;
    private static int sStartFrameNo;
    private static long sStartTime;
    private static String sTestCaseName = null;
    private String GET_APP_FRAMERATE_TMPL = "dumpsys gfxinfo %s";
    private String mStatus;

    public MonkeyGetAppFrameRateEvent(String status, String activityName, String testCaseName) {
        super(4);
        this.mStatus = status;
        sActivityName = activityName;
        sTestCaseName = testCaseName;
    }

    public MonkeyGetAppFrameRateEvent(String status, String activityName) {
        super(4);
        this.mStatus = status;
        sActivityName = activityName;
    }

    public MonkeyGetAppFrameRateEvent(String status) {
        super(4);
        this.mStatus = status;
    }

    private float getAverageFrameRate(int totalNumberOfFrame, float duration) {
        if (duration > 0.0f) {
            return ((float) totalNumberOfFrame) / duration;
        }
        return 0.0f;
    }

    private void writeAverageFrameRate() {
        FileWriter writer = null;
        try {
            Log.w(TAG, "file: " + LOG_FILE);
            writer = new FileWriter(LOG_FILE, true);
            writer.write(String.format("%s:%.2f\n", sTestCaseName, Float.valueOf(getAverageFrameRate(sEndFrameNo - sStartFrameNo, sDuration))));
            try {
                writer.close();
            } catch (IOException e) {
                Log.e(TAG, "IOException " + e.toString());
            }
        } catch (IOException e2) {
            Log.w(TAG, "Can't write sdcard log file", e2);
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e3) {
                    Log.e(TAG, "IOException " + e3.toString());
                }
            }
        } catch (Throwable th) {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e4) {
                    Log.e(TAG, "IOException " + e4.toString());
                }
            }
            throw th;
        }
    }

    private String getNumberOfFrames(BufferedReader reader) throws IOException {
        Matcher m;
        do {
            String line = reader.readLine();
            if (line == null) {
                return null;
            }
            m = NO_OF_FRAMES_PATTERN.matcher(line);
        } while (!m.matches());
        return m.group(1);
    }

    @Override // com.android.commands.monkey.MonkeyEvent
    public int injectEvent(IWindowManager iwm, IActivityManager iam, int verbose) {
        Process p = null;
        BufferedReader result = null;
        String cmd = String.format(this.GET_APP_FRAMERATE_TMPL, sActivityName);
        try {
            Process p2 = Runtime.getRuntime().exec(cmd);
            int status = p2.waitFor();
            if (status != 0) {
                Logger.err.println(String.format("// Shell command %s status was %s", cmd, Integer.valueOf(status)));
            }
            BufferedReader result2 = new BufferedReader(new InputStreamReader(p2.getInputStream()));
            String output = getNumberOfFrames(result2);
            if (output != null) {
                if ("start".equals(this.mStatus)) {
                    sStartFrameNo = Integer.parseInt(output);
                    sStartTime = System.currentTimeMillis();
                } else if ("end".equals(this.mStatus)) {
                    sEndFrameNo = Integer.parseInt(output);
                    sEndTime = System.currentTimeMillis();
                    sDuration = (float) (((double) (sEndTime - sStartTime)) / 1000.0d);
                    writeAverageFrameRate();
                }
            }
            try {
                result2.close();
                p2.destroy();
            } catch (IOException e) {
                Logger.err.println(e.toString());
            }
        } catch (Exception e2) {
            Logger logger = Logger.err;
            logger.println("// Exception from " + cmd + ":");
            Logger.err.println(e2.toString());
            if (0 != 0) {
                result.close();
            }
            if (0 != 0) {
                p.destroy();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    result.close();
                } catch (IOException e3) {
                    Logger.err.println(e3.toString());
                    throw th;
                }
            }
            if (0 != 0) {
                p.destroy();
            }
            throw th;
        }
        return 1;
    }
}
