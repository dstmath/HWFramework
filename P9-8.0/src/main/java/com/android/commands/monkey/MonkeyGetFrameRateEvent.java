package com.android.commands.monkey;

import android.app.IActivityManager;
import android.util.Log;
import android.view.IWindowManager;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MonkeyGetFrameRateEvent extends MonkeyEvent {
    private static final String LOG_FILE = "/sdcard/avgFrameRateOut.txt";
    private static final Pattern NO_OF_FRAMES_PATTERN = Pattern.compile(".*\\(([a-f[A-F][0-9]].*?)\\s.*\\)");
    private static final String TAG = "MonkeyGetFrameRateEvent";
    private static float mDuration;
    private static int mEndFrameNo;
    private static long mEndTime;
    private static int mStartFrameNo;
    private static long mStartTime;
    private static String mTestCaseName = null;
    private String GET_FRAMERATE_CMD = "service call SurfaceFlinger 1013";
    private String mStatus;

    public MonkeyGetFrameRateEvent(String status, String testCaseName) {
        super(4);
        this.mStatus = status;
        mTestCaseName = testCaseName;
    }

    public MonkeyGetFrameRateEvent(String status) {
        super(4);
        this.mStatus = status;
    }

    private float getAverageFrameRate(int totalNumberOfFrame, float duration) {
        if (duration > 0.0f) {
            return ((float) totalNumberOfFrame) / duration;
        }
        return 0.0f;
    }

    /* JADX WARNING: Removed duplicated region for block: B:31:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0066 A:{SYNTHETIC, Splitter: B:15:0x0066} */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x008d A:{SYNTHETIC, Splitter: B:21:0x008d} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void writeAverageFrameRate() {
        IOException e;
        Throwable th;
        FileWriter writer = null;
        try {
            FileWriter writer2 = new FileWriter(LOG_FILE, true);
            try {
                float avgFrameRate = getAverageFrameRate(mEndFrameNo - mStartFrameNo, mDuration);
                writer2.write(String.format("%s:%.2f\n", new Object[]{mTestCaseName, Float.valueOf(avgFrameRate)}));
                writer2.close();
                if (writer2 != null) {
                    try {
                        writer2.close();
                    } catch (IOException e2) {
                        Log.e(TAG, "IOException " + e2.toString());
                    }
                }
                writer = writer2;
            } catch (IOException e3) {
                e2 = e3;
                writer = writer2;
                try {
                    Log.w(TAG, "Can't write sdcard log file", e2);
                    if (writer == null) {
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (writer != null) {
                        try {
                            writer.close();
                        } catch (IOException e22) {
                            Log.e(TAG, "IOException " + e22.toString());
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                writer = writer2;
                if (writer != null) {
                }
                throw th;
            }
        } catch (IOException e4) {
            e22 = e4;
            Log.w(TAG, "Can't write sdcard log file", e22);
            if (writer == null) {
                try {
                    writer.close();
                } catch (IOException e222) {
                    Log.e(TAG, "IOException " + e222.toString());
                }
            }
        }
    }

    private String getNumberOfFrames(String input) {
        Matcher m = NO_OF_FRAMES_PATTERN.matcher(input);
        if (m.matches()) {
            return m.group(1);
        }
        return null;
    }

    /* JADX WARNING: Removed duplicated region for block: B:39:0x00e5 A:{SYNTHETIC, Splitter: B:39:0x00e5} */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x00ea A:{Catch:{ IOException -> 0x00ee }} */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x00c3 A:{SYNTHETIC, Splitter: B:29:0x00c3} */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x00c8 A:{Catch:{ IOException -> 0x00cc }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int injectEvent(IWindowManager iwm, IActivityManager iam, int verbose) {
        Exception e;
        Throwable th;
        Process process = null;
        BufferedReader result = null;
        try {
            process = Runtime.getRuntime().exec(this.GET_FRAMERATE_CMD);
            if (process.waitFor() != 0) {
                Logger.err.println(String.format("// Shell command %s status was %s", new Object[]{this.GET_FRAMERATE_CMD, Integer.valueOf(status)}));
            }
            BufferedReader result2 = new BufferedReader(new InputStreamReader(process.getInputStream()));
            try {
                String output = result2.readLine();
                if (output != null) {
                    if (this.mStatus == "start") {
                        mStartFrameNo = Integer.parseInt(getNumberOfFrames(output), 16);
                        mStartTime = System.currentTimeMillis();
                    } else if (this.mStatus == "end") {
                        mEndFrameNo = Integer.parseInt(getNumberOfFrames(output), 16);
                        mEndTime = System.currentTimeMillis();
                        mDuration = (float) (((double) (mEndTime - mStartTime)) / 1000.0d);
                        writeAverageFrameRate();
                    }
                }
                if (result2 != null) {
                    try {
                        result2.close();
                    } catch (IOException e2) {
                        Logger.err.println(e2.toString());
                    }
                }
                if (process != null) {
                    process.destroy();
                }
                result = result2;
            } catch (Exception e3) {
                e = e3;
                result = result2;
                try {
                    Logger.err.println("// Exception from " + this.GET_FRAMERATE_CMD + ":");
                    Logger.err.println(e.toString());
                    if (result != null) {
                        try {
                            result.close();
                        } catch (IOException e22) {
                            Logger.err.println(e22.toString());
                        }
                    }
                    if (process != null) {
                        process.destroy();
                    }
                    return 1;
                } catch (Throwable th2) {
                    th = th2;
                    if (result != null) {
                    }
                    if (process != null) {
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                result = result2;
                if (result != null) {
                    try {
                        result.close();
                    } catch (IOException e222) {
                        Logger.err.println(e222.toString());
                        throw th;
                    }
                }
                if (process != null) {
                    process.destroy();
                }
                throw th;
            }
        } catch (Exception e4) {
            e = e4;
            Logger.err.println("// Exception from " + this.GET_FRAMERATE_CMD + ":");
            Logger.err.println(e.toString());
            if (result != null) {
            }
            if (process != null) {
            }
            return 1;
        }
        return 1;
    }
}
