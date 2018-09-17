package com.android.internal.app.procstats;

import android.os.UserHandle;
import android.provider.CalendarContract.CalendarCache;
import android.rms.iaware.DataContract.Apps.LaunchMode;
import android.rms.iaware.DataContract.BaseProperty;
import android.service.quicksettings.TileService;
import android.util.LogException;
import android.util.TimeUtils;
import com.android.internal.app.DumpHeapActivity;
import com.android.internal.content.NativeLibraryHelper;
import java.io.PrintWriter;
import java.util.ArrayList;

public final class DumpUtils {
    public static final String[] ADJ_MEM_NAMES_CSV = new String[]{"norm", "mod", "low", "crit"};
    static final String[] ADJ_MEM_TAGS = new String[]{"n", "m", "l", "c"};
    public static final String[] ADJ_SCREEN_NAMES_CSV = new String[]{"off", "on"};
    static final String[] ADJ_SCREEN_TAGS = new String[]{"0", "1"};
    static final String CSV_SEP = "\t";
    public static final String[] STATE_NAMES = new String[]{"Persist", "Top    ", "ImpFg  ", "ImpBg  ", "Backup ", "HeavyWt", "Service", "ServRst", "Receivr", "Home   ", "LastAct", "CchAct ", "CchCAct", "CchEmty"};
    public static final String[] STATE_NAMES_CSV = new String[]{"pers", "top", "impfg", "impbg", LaunchMode.BACKUP, "heavy", TileService.EXTRA_SERVICE, "service-rs", "receiver", CalendarCache.TIMEZONE_TYPE_HOME, "lastact", "cch-activity", "cch-aclient", "cch-empty"};
    static final String[] STATE_TAGS = new String[]{"p", "t", "f", "b", "u", "w", "s", "x", "r", "h", "l", "a", "c", "e"};

    private DumpUtils() {
    }

    public static void printScreenLabel(PrintWriter pw, int offset) {
        switch (offset) {
            case -1:
                pw.print("     ");
                return;
            case 0:
                pw.print("SOff/");
                return;
            case 4:
                pw.print("SOn /");
                return;
            default:
                pw.print("????/");
                return;
        }
    }

    public static void printScreenLabelCsv(PrintWriter pw, int offset) {
        switch (offset) {
            case -1:
                return;
            case 0:
                pw.print(ADJ_SCREEN_NAMES_CSV[0]);
                return;
            case 4:
                pw.print(ADJ_SCREEN_NAMES_CSV[1]);
                return;
            default:
                pw.print("???");
                return;
        }
    }

    public static void printMemLabel(PrintWriter pw, int offset, char sep) {
        switch (offset) {
            case -1:
                pw.print("    ");
                if (sep != 0) {
                    pw.print(' ');
                    return;
                }
                return;
            case 0:
                pw.print("Norm");
                if (sep != 0) {
                    pw.print(sep);
                    return;
                }
                return;
            case 1:
                pw.print("Mod ");
                if (sep != 0) {
                    pw.print(sep);
                    return;
                }
                return;
            case 2:
                pw.print("Low ");
                if (sep != 0) {
                    pw.print(sep);
                    return;
                }
                return;
            case 3:
                pw.print("Crit");
                if (sep != 0) {
                    pw.print(sep);
                    return;
                }
                return;
            default:
                pw.print("????");
                if (sep != 0) {
                    pw.print(sep);
                    return;
                }
                return;
        }
    }

    public static void printMemLabelCsv(PrintWriter pw, int offset) {
        if (offset < 0) {
            return;
        }
        if (offset <= 3) {
            pw.print(ADJ_MEM_NAMES_CSV[offset]);
        } else {
            pw.print("???");
        }
    }

    public static void printPercent(PrintWriter pw, double fraction) {
        fraction *= 100.0d;
        if (fraction < 1.0d) {
            pw.print(String.format("%.2f", new Object[]{Double.valueOf(fraction)}));
        } else if (fraction < 10.0d) {
            pw.print(String.format("%.1f", new Object[]{Double.valueOf(fraction)}));
        } else {
            pw.print(String.format("%.0f", new Object[]{Double.valueOf(fraction)}));
        }
        pw.print("%");
    }

    public static void printProcStateTag(PrintWriter pw, int state) {
        printArrayEntry(pw, STATE_TAGS, printArrayEntry(pw, ADJ_MEM_TAGS, printArrayEntry(pw, ADJ_SCREEN_TAGS, state, 56), 14), 1);
    }

    public static void printAdjTag(PrintWriter pw, int state) {
        printArrayEntry(pw, ADJ_MEM_TAGS, printArrayEntry(pw, ADJ_SCREEN_TAGS, state, 4), 1);
    }

    public static void printProcStateTagAndValue(PrintWriter pw, int state, long value) {
        pw.print(',');
        printProcStateTag(pw, state);
        pw.print(':');
        pw.print(value);
    }

    public static void printAdjTagAndValue(PrintWriter pw, int state, long value) {
        pw.print(',');
        printAdjTag(pw, state);
        pw.print(':');
        pw.print(value);
    }

    public static long dumpSingleTime(PrintWriter pw, String prefix, long[] durations, int curState, long curStartTime, long now) {
        long totalTime = 0;
        int printedScreen = -1;
        for (int iscreen = 0; iscreen < 8; iscreen += 4) {
            int printedMem = -1;
            int imem = 0;
            while (imem < 4) {
                int state = imem + iscreen;
                long time = durations[state];
                String running = LogException.NO_VALUE;
                if (curState == state) {
                    time += now - curStartTime;
                    if (pw != null) {
                        running = " (running)";
                    }
                }
                if (time != 0) {
                    if (pw != null) {
                        int i;
                        pw.print(prefix);
                        if (printedScreen != iscreen) {
                            i = iscreen;
                        } else {
                            i = -1;
                        }
                        printScreenLabel(pw, i);
                        printedScreen = iscreen;
                        printMemLabel(pw, printedMem != imem ? imem : -1, 0);
                        printedMem = imem;
                        pw.print(": ");
                        TimeUtils.formatDuration(time, pw);
                        pw.println(running);
                    }
                    totalTime += time;
                }
                imem++;
            }
        }
        if (!(totalTime == 0 || pw == null)) {
            pw.print(prefix);
            pw.print("    TOTAL: ");
            TimeUtils.formatDuration(totalTime, pw);
            pw.println();
        }
        return totalTime;
    }

    public static void dumpAdjTimesCheckin(PrintWriter pw, String sep, long[] durations, int curState, long curStartTime, long now) {
        for (int iscreen = 0; iscreen < 8; iscreen += 4) {
            for (int imem = 0; imem < 4; imem++) {
                int state = imem + iscreen;
                long time = durations[state];
                if (curState == state) {
                    time += now - curStartTime;
                }
                if (time != 0) {
                    printAdjTagAndValue(pw, state, time);
                }
            }
        }
    }

    private static void dumpStateHeadersCsv(PrintWriter pw, String sep, int[] screenStates, int[] memStates, int[] procStates) {
        int NS = screenStates != null ? screenStates.length : 1;
        int NM = memStates != null ? memStates.length : 1;
        int NP = procStates != null ? procStates.length : 1;
        for (int is = 0; is < NS; is++) {
            for (int im = 0; im < NM; im++) {
                for (int ip = 0; ip < NP; ip++) {
                    pw.print(sep);
                    boolean printed = false;
                    if (screenStates != null && screenStates.length > 1) {
                        printScreenLabelCsv(pw, screenStates[is]);
                        printed = true;
                    }
                    if (memStates != null && memStates.length > 1) {
                        if (printed) {
                            pw.print(NativeLibraryHelper.CLEAR_ABI_OVERRIDE);
                        }
                        printMemLabelCsv(pw, memStates[im]);
                        printed = true;
                    }
                    if (procStates != null && procStates.length > 1) {
                        if (printed) {
                            pw.print(NativeLibraryHelper.CLEAR_ABI_OVERRIDE);
                        }
                        pw.print(STATE_NAMES_CSV[procStates[ip]]);
                    }
                }
            }
        }
    }

    public static void dumpProcessSummaryLocked(PrintWriter pw, String prefix, ArrayList<ProcessState> procs, int[] screenStates, int[] memStates, int[] procStates, long now, long totalTime) {
        for (int i = procs.size() - 1; i >= 0; i--) {
            ((ProcessState) procs.get(i)).dumpSummary(pw, prefix, screenStates, memStates, procStates, now, totalTime);
        }
    }

    public static void dumpProcessListCsv(PrintWriter pw, ArrayList<ProcessState> procs, boolean sepScreenStates, int[] screenStates, boolean sepMemStates, int[] memStates, boolean sepProcStates, int[] procStates, long now) {
        pw.print(DumpHeapActivity.KEY_PROCESS);
        pw.print(CSV_SEP);
        pw.print(BaseProperty.UID);
        pw.print(CSV_SEP);
        pw.print("vers");
        dumpStateHeadersCsv(pw, CSV_SEP, sepScreenStates ? screenStates : null, sepMemStates ? memStates : null, sepProcStates ? procStates : null);
        pw.println();
        for (int i = procs.size() - 1; i >= 0; i--) {
            ProcessState proc = (ProcessState) procs.get(i);
            pw.print(proc.getName());
            pw.print(CSV_SEP);
            UserHandle.formatUid(pw, proc.getUid());
            pw.print(CSV_SEP);
            pw.print(proc.getVersion());
            proc.dumpCsv(pw, sepScreenStates, screenStates, sepMemStates, memStates, sepProcStates, procStates, now);
            pw.println();
        }
    }

    public static int printArrayEntry(PrintWriter pw, String[] array, int value, int mod) {
        int index = value / mod;
        if (index < 0 || index >= array.length) {
            pw.print('?');
        } else {
            pw.print(array[index]);
        }
        return value - (index * mod);
    }

    public static String collapseString(String pkgName, String itemName) {
        if (itemName.startsWith(pkgName)) {
            int ITEMLEN = itemName.length();
            int PKGLEN = pkgName.length();
            if (ITEMLEN == PKGLEN) {
                return LogException.NO_VALUE;
            }
            if (ITEMLEN >= PKGLEN && itemName.charAt(PKGLEN) == '.') {
                return itemName.substring(PKGLEN);
            }
        }
        return itemName;
    }
}
