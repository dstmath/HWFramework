package com.android.internal.app.procstats;

import android.app.backup.FullBackup;
import android.content.Context;
import android.hardware.Camera;
import android.media.TtmlUtils;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.IncidentManager;
import android.os.UserHandle;
import android.provider.CalendarContract;
import android.util.TimeUtils;
import android.util.proto.ProtoOutputStream;
import com.android.internal.app.DumpHeapActivity;
import com.android.internal.content.NativeLibraryHelper;
import java.io.PrintWriter;
import java.util.ArrayList;

public final class DumpUtils {
    public static final String[] ADJ_MEM_NAMES_CSV = {"norm", "mod", "low", "crit"};
    static final int[] ADJ_MEM_PROTO_ENUMS = {1, 2, 3, 4};
    static final String[] ADJ_MEM_TAGS = {"n", "m", "l", FullBackup.CACHE_TREE_TOKEN};
    public static final String[] ADJ_SCREEN_NAMES_CSV = {"off", Camera.Parameters.FLASH_MODE_ON};
    static final int[] ADJ_SCREEN_PROTO_ENUMS = {1, 2};
    static final String[] ADJ_SCREEN_TAGS = {WifiEnterpriseConfig.ENGINE_DISABLE, "1"};
    static final String CSV_SEP = "\t";
    public static final String[] STATE_LABELS = new String[14];
    public static final String STATE_LABEL_CACHED = "  (Cached)";
    public static final String STATE_LABEL_TOTAL = "     TOTAL";
    public static final String[] STATE_NAMES = new String[14];
    public static final String[] STATE_NAMES_CSV = new String[14];
    static final int[] STATE_PROTO_ENUMS = new int[14];
    static final String[] STATE_TAGS = new String[14];

    static {
        String[] strArr = STATE_NAMES;
        strArr[0] = "Persist";
        strArr[1] = "Top";
        strArr[2] = "ImpFg";
        strArr[3] = "ImpBg";
        strArr[4] = "Backup";
        strArr[5] = "Service";
        strArr[6] = "ServRst";
        strArr[7] = "Receivr";
        strArr[8] = "HeavyWt";
        strArr[9] = "Home";
        strArr[10] = "LastAct";
        strArr[11] = "CchAct";
        strArr[12] = "CchCAct";
        strArr[13] = "CchEmty";
        String[] strArr2 = STATE_LABELS;
        strArr2[0] = "Persistent";
        strArr2[1] = "       Top";
        strArr2[2] = "    Imp Fg";
        strArr2[3] = "    Imp Bg";
        strArr2[4] = "    Backup";
        strArr2[5] = "   Service";
        strArr2[6] = "Service Rs";
        strArr2[7] = "  Receiver";
        strArr2[8] = " Heavy Wgt";
        strArr2[9] = "    (Home)";
        strArr2[10] = "(Last Act)";
        strArr2[11] = " (Cch Act)";
        strArr2[12] = "(Cch CAct)";
        strArr2[13] = "(Cch Emty)";
        String[] strArr3 = STATE_NAMES_CSV;
        strArr3[0] = "pers";
        strArr3[1] = "top";
        strArr3[2] = "impfg";
        strArr3[3] = "impbg";
        strArr3[4] = Context.BACKUP_SERVICE;
        strArr3[5] = "service";
        strArr3[6] = "service-rs";
        strArr3[7] = "receiver";
        strArr3[8] = "heavy";
        strArr3[9] = CalendarContract.CalendarCache.TIMEZONE_TYPE_HOME;
        strArr3[10] = "lastact";
        strArr3[11] = "cch-activity";
        strArr3[12] = "cch-aclient";
        strArr3[13] = "cch-empty";
        String[] strArr4 = STATE_TAGS;
        strArr4[0] = TtmlUtils.TAG_P;
        strArr4[1] = IncidentManager.URI_PARAM_TIMESTAMP;
        strArr4[2] = FullBackup.FILES_TREE_TOKEN;
        strArr4[3] = "b";
        strArr4[4] = "u";
        strArr4[5] = "s";
        strArr4[6] = "x";
        strArr4[7] = "r";
        strArr4[8] = "w";
        strArr4[9] = "h";
        strArr4[10] = "l";
        strArr4[11] = FullBackup.APK_TREE_TOKEN;
        strArr4[12] = FullBackup.CACHE_TREE_TOKEN;
        strArr4[13] = "e";
        int[] iArr = STATE_PROTO_ENUMS;
        iArr[0] = 1;
        iArr[1] = 2;
        iArr[2] = 3;
        iArr[3] = 4;
        iArr[4] = 5;
        iArr[5] = 6;
        iArr[6] = 7;
        iArr[7] = 8;
        iArr[8] = 9;
        iArr[9] = 10;
        iArr[10] = 11;
        iArr[11] = 12;
        iArr[12] = 13;
        iArr[13] = 14;
    }

    private DumpUtils() {
    }

    public static void printScreenLabel(PrintWriter pw, int offset) {
        if (offset == -1) {
            pw.print("     ");
        } else if (offset == 0) {
            pw.print("SOff/");
        } else if (offset != 4) {
            pw.print("????/");
        } else {
            pw.print(" SOn/");
        }
    }

    public static void printScreenLabelCsv(PrintWriter pw, int offset) {
        if (offset == -1) {
            return;
        }
        if (offset == 0) {
            pw.print(ADJ_SCREEN_NAMES_CSV[0]);
        } else if (offset != 4) {
            pw.print("???");
        } else {
            pw.print(ADJ_SCREEN_NAMES_CSV[1]);
        }
    }

    public static void printMemLabel(PrintWriter pw, int offset, char sep) {
        if (offset == -1) {
            pw.print("    ");
            if (sep != 0) {
                pw.print(' ');
            }
        } else if (offset == 0) {
            pw.print("Norm");
            if (sep != 0) {
                pw.print(sep);
            }
        } else if (offset == 1) {
            pw.print(" Mod");
            if (sep != 0) {
                pw.print(sep);
            }
        } else if (offset == 2) {
            pw.print(" Low");
            if (sep != 0) {
                pw.print(sep);
            }
        } else if (offset != 3) {
            pw.print("????");
            if (sep != 0) {
                pw.print(sep);
            }
        } else {
            pw.print("Crit");
            if (sep != 0) {
                pw.print(sep);
            }
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
        double fraction2 = fraction * 100.0d;
        if (fraction2 < 1.0d) {
            pw.print(String.format("%.2f", Double.valueOf(fraction2)));
        } else if (fraction2 < 10.0d) {
            pw.print(String.format("%.1f", Double.valueOf(fraction2)));
        } else {
            pw.print(String.format("%.0f", Double.valueOf(fraction2)));
        }
        pw.print("%");
    }

    public static void printProcStateTag(PrintWriter pw, int state) {
        printArrayEntry(pw, STATE_TAGS, printArrayEntry(pw, ADJ_MEM_TAGS, printArrayEntry(pw, ADJ_SCREEN_TAGS, state, 56), 14), 1);
    }

    public static void printProcStateTagProto(ProtoOutputStream proto, long screenId, long memId, long stateId, int state) {
        printProto(proto, stateId, STATE_PROTO_ENUMS, printProto(proto, memId, ADJ_MEM_PROTO_ENUMS, printProto(proto, screenId, ADJ_SCREEN_PROTO_ENUMS, state, 56), 14), 1);
    }

    public static void printAdjTag(PrintWriter pw, int state) {
        printArrayEntry(pw, ADJ_MEM_TAGS, printArrayEntry(pw, ADJ_SCREEN_TAGS, state, 4), 1);
    }

    public static void printProcStateAdjTagProto(ProtoOutputStream proto, long screenId, long memId, int state) {
        printProto(proto, memId, ADJ_MEM_PROTO_ENUMS, printProto(proto, screenId, ADJ_SCREEN_PROTO_ENUMS, state, 56), 14);
    }

    public static void printProcStateDurationProto(ProtoOutputStream proto, long fieldId, int procState, long duration) {
        long stateToken = proto.start(fieldId);
        printProto(proto, 1159641169923L, STATE_PROTO_ENUMS, procState, 1);
        proto.write(1112396529668L, duration);
        proto.end(stateToken);
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
        int iscreen = 0;
        while (iscreen < 8) {
            int printedMem = -1;
            for (int imem = 0; imem < 4; imem++) {
                int state = imem + iscreen;
                long time = durations[state];
                String running = "";
                if (curState == state) {
                    time += now - curStartTime;
                    if (pw != null) {
                        running = " (running)";
                    }
                }
                if (time != 0) {
                    if (pw != null) {
                        pw.print(prefix);
                        int i = -1;
                        printScreenLabel(pw, printedScreen != iscreen ? iscreen : -1);
                        printedScreen = iscreen;
                        if (printedMem != imem) {
                            i = imem;
                        }
                        printMemLabel(pw, i, 0);
                        printedMem = imem;
                        pw.print(": ");
                        TimeUtils.formatDuration(time, pw);
                        pw.println(running);
                    }
                    totalTime += time;
                }
            }
            iscreen += 4;
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

    public static void dumpProcessSummaryLocked(PrintWriter pw, String prefix, String header, ArrayList<ProcessState> procs, int[] screenStates, int[] memStates, int[] procStates, long now, long totalTime) {
        for (int i = procs.size() - 1; i >= 0; i--) {
            procs.get(i).dumpSummary(pw, prefix, header, screenStates, memStates, procStates, now, totalTime);
        }
    }

    public static void dumpProcessListCsv(PrintWriter pw, ArrayList<ProcessState> procs, boolean sepScreenStates, int[] screenStates, boolean sepMemStates, int[] memStates, boolean sepProcStates, int[] procStates, long now) {
        pw.print(DumpHeapActivity.KEY_PROCESS);
        pw.print(CSV_SEP);
        pw.print("uid");
        pw.print(CSV_SEP);
        pw.print("vers");
        int[] iArr = null;
        int[] iArr2 = sepScreenStates ? screenStates : null;
        int[] iArr3 = sepMemStates ? memStates : null;
        if (sepProcStates) {
            iArr = procStates;
        }
        dumpStateHeadersCsv(pw, CSV_SEP, iArr2, iArr3, iArr);
        pw.println();
        for (int i = procs.size() - 1; i >= 0; i--) {
            ProcessState proc = procs.get(i);
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

    public static int printProto(ProtoOutputStream proto, long fieldId, int[] enums, int value, int mod) {
        int index = value / mod;
        if (index >= 0 && index < enums.length) {
            proto.write(fieldId, enums[index]);
        }
        return value - (index * mod);
    }

    public static String collapseString(String pkgName, String itemName) {
        if (itemName.startsWith(pkgName)) {
            int ITEMLEN = itemName.length();
            int PKGLEN = pkgName.length();
            if (ITEMLEN == PKGLEN) {
                return "";
            }
            if (ITEMLEN >= PKGLEN && itemName.charAt(PKGLEN) == '.') {
                return itemName.substring(PKGLEN);
            }
        }
        return itemName;
    }
}
