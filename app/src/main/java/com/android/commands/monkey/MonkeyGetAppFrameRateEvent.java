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

public class MonkeyGetAppFrameRateEvent extends MonkeyEvent {
    private static final String LOG_FILE = null;
    private static final Pattern NO_OF_FRAMES_PATTERN = null;
    private static final String TAG = "MonkeyGetAppFrameRateEvent";
    private static String sActivityName;
    private static float sDuration;
    private static int sEndFrameNo;
    private static long sEndTime;
    private static int sStartFrameNo;
    private static long sStartTime;
    private static String sTestCaseName;
    private String GET_APP_FRAMERATE_TMPL;
    private String mStatus;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.commands.monkey.MonkeyGetAppFrameRateEvent.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.commands.monkey.MonkeyGetAppFrameRateEvent.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.commands.monkey.MonkeyGetAppFrameRateEvent.<clinit>():void");
    }

    public MonkeyGetAppFrameRateEvent(String status, String activityName, String testCaseName) {
        super(4);
        this.GET_APP_FRAMERATE_TMPL = "dumpsys gfxinfo %s";
        this.mStatus = status;
        sActivityName = activityName;
        sTestCaseName = testCaseName;
    }

    public MonkeyGetAppFrameRateEvent(String status, String activityName) {
        super(4);
        this.GET_APP_FRAMERATE_TMPL = "dumpsys gfxinfo %s";
        this.mStatus = status;
        sActivityName = activityName;
    }

    public MonkeyGetAppFrameRateEvent(String status) {
        super(4);
        this.GET_APP_FRAMERATE_TMPL = "dumpsys gfxinfo %s";
        this.mStatus = status;
    }

    private float getAverageFrameRate(int totalNumberOfFrame, float duration) {
        if (duration > 0.0f) {
            return ((float) totalNumberOfFrame) / duration;
        }
        return 0.0f;
    }

    private void writeAverageFrameRate() {
        IOException e;
        Throwable th;
        FileWriter fileWriter = null;
        try {
            Log.w(TAG, "file: " + LOG_FILE);
            FileWriter writer = new FileWriter(LOG_FILE, true);
            try {
                float avgFrameRate = getAverageFrameRate(sEndFrameNo - sStartFrameNo, sDuration);
                writer.write(String.format("%s:%.2f\n", new Object[]{sTestCaseName, Float.valueOf(avgFrameRate)}));
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException e2) {
                        Log.e(TAG, "IOException " + e2.toString());
                    }
                }
                fileWriter = writer;
            } catch (IOException e3) {
                e2 = e3;
                fileWriter = writer;
                try {
                    Log.w(TAG, "Can't write sdcard log file", e2);
                    if (fileWriter != null) {
                        try {
                            fileWriter.close();
                        } catch (IOException e22) {
                            Log.e(TAG, "IOException " + e22.toString());
                        }
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (fileWriter != null) {
                        try {
                            fileWriter.close();
                        } catch (IOException e222) {
                            Log.e(TAG, "IOException " + e222.toString());
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                fileWriter = writer;
                if (fileWriter != null) {
                    fileWriter.close();
                }
                throw th;
            }
        } catch (IOException e4) {
            e222 = e4;
            Log.w(TAG, "Can't write sdcard log file", e222);
            if (fileWriter != null) {
                fileWriter.close();
            }
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

    public int injectEvent(IWindowManager iwm, IActivityManager iam, int verbose) {
        Exception e;
        Throwable th;
        Process process = null;
        BufferedReader bufferedReader = null;
        String cmd = String.format(this.GET_APP_FRAMERATE_TMPL, new Object[]{sActivityName});
        try {
            process = Runtime.getRuntime().exec(cmd);
            if (process.waitFor() != 0) {
                System.err.println(String.format("// Shell command %s status was %s", new Object[]{cmd, Integer.valueOf(status)}));
            }
            BufferedReader result = new BufferedReader(new InputStreamReader(process.getInputStream()));
            try {
                String output = getNumberOfFrames(result);
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
                if (result != null) {
                    try {
                        result.close();
                    } catch (IOException e2) {
                        System.err.println(e2.toString());
                    }
                }
                if (process != null) {
                    process.destroy();
                }
                bufferedReader = result;
            } catch (Exception e3) {
                e = e3;
                bufferedReader = result;
                try {
                    System.err.println("// Exception from " + cmd + ":");
                    System.err.println(e.toString());
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e22) {
                            System.err.println(e22.toString());
                        }
                    }
                    if (process != null) {
                        process.destroy();
                    }
                    return 1;
                } catch (Throwable th2) {
                    th = th2;
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e222) {
                            System.err.println(e222.toString());
                            throw th;
                        }
                    }
                    if (process != null) {
                        process.destroy();
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                bufferedReader = result;
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (process != null) {
                    process.destroy();
                }
                throw th;
            }
        } catch (Exception e4) {
            e = e4;
            System.err.println("// Exception from " + cmd + ":");
            System.err.println(e.toString());
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (process != null) {
                process.destroy();
            }
            return 1;
        }
        return 1;
    }
}
