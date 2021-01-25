package ohos.aafwk.utils.dfx.time;

import java.io.File;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import ohos.aafwk.utils.log.Log;
import ohos.aafwk.utils.log.LogLabel;
import ohos.agp.styles.attributes.ViewAttrsConstants;

public class TimeCost {
    private static final String CLR_TIMECOST_INVALID_CALLER = "-clr";
    private static final String DUMP_TIMECOST_FORCE_CLOSE = "-force";
    private static final String GET_TIMECOST_ENABLE = "-get";
    private static final LogLabel LABEL = LogLabel.create();
    private static final String SET_TIMECOST_ENABLE = "-set";
    private static final File SWITCH_FILE = new File(Config.SWITCH_FILE.name);
    private static final String SWITCH_FILE_NAME = "/data/test/TimeCost";
    private static final String TIMECOST_COMMON = "-c";
    private static boolean enable;
    private static List<TimeCost> ownerMapping;
    private static boolean switchOn;
    private boolean available = true;
    private WeakReference<Object> owner;
    private Map<TimeEventType, RecordPool> records;
    private String strOwner;

    static {
        init();
    }

    enum Config {
        SWITCH_FILE(TimeCost.SWITCH_FILE_NAME);
        
        String name;

        private Config(String str) {
            this.name = str;
        }
    }

    private TimeCost(Object obj) {
        this.owner = new WeakReference<>(obj);
        this.strOwner = obj.toString();
        this.records = new HashMap();
    }

    private static void init() {
        boolean exists = SWITCH_FILE.exists();
        Log.info(LABEL, "time cost static init. switchOn: %{public}b", Boolean.valueOf(exists));
        init(exists);
    }

    private static void init(boolean z) {
        switchOn = z;
        if (z) {
            enable = true;
            ownerMapping = new ArrayList();
            return;
        }
        enable = false;
        clear();
        ownerMapping = null;
    }

    private boolean checkRecordsNull() {
        return this.records == null;
    }

    private void checkRecords() {
        if (checkRecordsNull()) {
            Log.error(LABEL, "records related vars are null, switchOn: %{public}s", Boolean.valueOf(isSwitchOn()));
            throw new IllegalStateException("records related vars are null, switchOn: " + isSwitchOn());
        }
    }

    private static boolean checkMappingNull() {
        return ownerMapping == null;
    }

    private static void checkMapping() {
        if (checkMappingNull()) {
            Log.error(LABEL, "records related vars are null, switchOn: %{public}s", Boolean.valueOf(isSwitchOn()));
            throw new IllegalStateException("records related vars are null, switchOn: " + isSwitchOn());
        }
    }

    public Optional<TimeRecord> getRecord(Object obj, TimeEventType timeEventType, String str) {
        if (obj == null || timeEventType == null) {
            Log.error(LABEL, "caller/type is null when get record. caller: %{public}s, type: %{public}s", obj, timeEventType);
            throw new IllegalArgumentException("caller/type is null when get record. caller: " + obj + " type: " + timeEventType);
        }
        checkRecords();
        RecordPool recordPool = this.records.get(timeEventType);
        if (recordPool == null) {
            recordPool = new RecordPool();
            recordPool.init(timeEventType.getSize());
            this.records.put(timeEventType, recordPool);
        }
        return recordPool.getRecord(obj, timeEventType, str);
    }

    public static TimeCost reg(Object obj) {
        if (obj != null) {
            checkMapping();
            TimeCost timeCost = null;
            Iterator<TimeCost> it = ownerMapping.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                TimeCost next = it.next();
                if (next.owner.get() == obj) {
                    timeCost = next;
                    break;
                }
            }
            if (timeCost != null) {
                return timeCost;
            }
            TimeCost timeCost2 = new TimeCost(obj);
            ownerMapping.add(timeCost2);
            return timeCost2;
        }
        Log.error(LABEL, "owner is null for register time cost", new Object[0]);
        throw new IllegalArgumentException("caller is null for register time cost");
    }

    public void setAvailable(boolean z) {
        this.available = z;
    }

    private boolean isAvailable() {
        return this.owner.get() != null && this.available;
    }

    private static void unReg(TimeCost timeCost) {
        if (timeCost != null) {
            timeCost.release();
        }
    }

    public static void unReg(Object obj) {
        if (obj != null && !checkMappingNull()) {
            Iterator<TimeCost> it = ownerMapping.iterator();
            while (it.hasNext()) {
                TimeCost next = it.next();
                if (next.owner.get() == obj) {
                    it.remove();
                    unReg(next);
                    return;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void clearDeadRecords() {
        this.records.forEach($$Lambda$TimeCost$088mgIlhgPJKndyMhZnOyyL6gKc.INSTANCE);
    }

    private static void clearRecords(boolean z) {
        if (!checkMappingNull()) {
            Iterator<TimeCost> it = ownerMapping.iterator();
            while (it.hasNext()) {
                TimeCost next = it.next();
                if (!next.isAvailable()) {
                    next.release();
                    it.remove();
                }
            }
            if (z) {
                ownerMapping.forEach($$Lambda$TimeCost$iUQlwbqwmHHSRFe5OcfZcrAWn4U.INSTANCE);
            } else {
                ownerMapping.forEach($$Lambda$TimeCost$97Adywy1UXC0QwlURBPTpwd5rfM.INSTANCE);
            }
        }
    }

    private void release() {
        if (!checkRecordsNull()) {
            this.records.forEach($$Lambda$TimeCost$Kbsli7S0L4VNC2lpJSZIFizR78.INSTANCE);
            this.records.clear();
        }
    }

    public static void clear() {
        if (!checkMappingNull()) {
            Iterator<TimeCost> it = ownerMapping.iterator();
            while (it.hasNext()) {
                TimeCost next = it.next();
                it.remove();
                unReg((Object) next);
            }
        }
    }

    public static boolean isEnable() {
        return switchOn && enable;
    }

    public static boolean isSwitchOn() {
        return switchOn;
    }

    private static boolean setEnable(boolean z) {
        if (!switchOn) {
            return false;
        }
        enable = z;
        return true;
    }

    /* access modifiers changed from: private */
    public void forceDoneAllPools() {
        this.records.values().forEach($$Lambda$TimeCost$jCrkZ6SDAKM_cIodheCHK9gDxBE.INSTANCE);
    }

    private static void forceDoneOwnerPools() {
        if (!checkMappingNull()) {
            ownerMapping.forEach($$Lambda$TimeCost$WgCRqXPN5elkWmMJx6vUYdueZGU.INSTANCE);
        }
    }

    /* access modifiers changed from: private */
    public void dumpTimeCost(String str, PrintWriter printWriter) {
        printWriter.println(str + "Owner: " + this.strOwner);
        printWriter.println(str + "Available: " + isAvailable());
        Map<TimeEventType, RecordPool> map = this.records;
        if (map == null || map.size() == 0) {
            printWriter.println(str + "All time cost data has been cleared.");
        } else if (this.records.values().stream().noneMatch($$Lambda$TimeCost$X00EdnkWGDV6XQrUxM0J0oj0OLA.INSTANCE)) {
            printWriter.println(str + "All time cost data has been cleared.");
        } else {
            this.records.values().forEach(new Consumer(str, printWriter) {
                /* class ohos.aafwk.utils.dfx.time.$$Lambda$TimeCost$3TVF74OpLflyVebctD0JvBzHnfA */
                private final /* synthetic */ String f$0;
                private final /* synthetic */ PrintWriter f$1;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    ((RecordPool) obj).dump(this.f$0, this.f$1);
                }
            });
        }
    }

    static /* synthetic */ boolean lambda$dumpTimeCost$7(RecordPool recordPool) {
        return recordPool.size() > 0;
    }

    private static void dumpTimeCosts(String str, PrintWriter printWriter, boolean z) {
        if (str != null && printWriter != null) {
            if (ownerMapping == null) {
                printWriter.println(str + "No data to display.");
                dumpIsSwitchOn(str, printWriter);
                return;
            }
            boolean isEnable = isEnable();
            if (z) {
                setEnable(false);
                forceDoneOwnerPools();
            }
            ownerMapping.forEach(new Consumer(str, printWriter) {
                /* class ohos.aafwk.utils.dfx.time.$$Lambda$TimeCost$EYBqxoMPPAPZB9fvkF_x7txYoJs */
                private final /* synthetic */ String f$0;
                private final /* synthetic */ PrintWriter f$1;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    ((TimeCost) obj).dumpTimeCost(this.f$0, this.f$1);
                }
            });
            if (z) {
                setEnable(isEnable);
            }
        }
    }

    private static void dumpIsEnabled(String str, PrintWriter printWriter) {
        StringBuilder sb = new StringBuilder();
        sb.append(str);
        sb.append("Enabled Status for Aafwk Time Cost: ");
        sb.append(isEnable() ? "Enable" : "Disable");
        printWriter.println(sb.toString());
    }

    private static void dumpIsSwitchOn(String str, PrintWriter printWriter) {
        StringBuilder sb = new StringBuilder();
        sb.append(str);
        sb.append("SwitchOn Status for Aafwk Time Cost: ");
        sb.append(isSwitchOn() ? "Switch On" : "Switch Off");
        printWriter.println(sb.toString());
    }

    private static void dumpClrDeadRecords(String str, PrintWriter printWriter) {
        clearRecords(true);
        printWriter.println(str + "clear dead caller(s) record success.");
    }

    private static void dumpClrRecords(String str, PrintWriter printWriter) {
        clearRecords(false);
        printWriter.println(str + "clear all records success.");
    }

    private static void dumpSetEnabled(String str, PrintWriter printWriter, boolean z) {
        if (setEnable(z)) {
            printWriter.println(str + "Set Success.");
        } else {
            printWriter.println(str + "Set failed.");
            dumpIsSwitchOn(str, printWriter);
        }
        dumpIsEnabled(str, printWriter);
    }

    public static boolean isKnownDumpCmdOpt(String str) {
        return TIMECOST_COMMON.equals(str);
    }

    public static void dumpHelp(String str, PrintWriter printWriter) {
        if (str != null && printWriter != null) {
            printWriter.println(str + "[-c]                  get aafwk kit subsystem TimeCost record(s)");
            printWriter.println(str + "[-c] [-force]         get aafwk kit subsystem TimeCost record(s) with force close.");
            printWriter.println(str + "[-c] [-clr]           clear all records. no lock in time cost, this is dangerous.");
            printWriter.println(str + "                      should disable time cost and wait a while then do clear");
            printWriter.println(str + "[-c] [-clr] [dead]    clear all records whose caller already dead");
            printWriter.println(str + "[-c] [-get]           get aafwk kit subsystem TimeCost enabled status");
            printWriter.println(str + "[-c] [-set] [enable]  set aafwk kit subsystem TimeCost enable");
            printWriter.println(str + "[-c] [-set] [disable] set aafwk kit subsystem TimeCost disable");
        }
    }

    private static int handleDumpSetEnable(String str, PrintWriter printWriter, String[] strArr, int i) {
        boolean z = true;
        int i2 = i + 1;
        try {
            String str2 = strArr[i2];
            String lowerCase = ((String) Optional.ofNullable(str2).orElse("")).toLowerCase(Locale.ENGLISH);
            char c = 65535;
            int hashCode = lowerCase.hashCode();
            if (hashCode != -1298848381) {
                if (hashCode == 1671308008) {
                    if (lowerCase.equals("disable")) {
                        c = 1;
                    }
                }
            } else if (lowerCase.equals(ViewAttrsConstants.ENABLED)) {
                c = 0;
            }
            if (c != 0) {
                if (c != 1) {
                    printWriter.println("Error: aafwk time cost set must be enable or disable. input: " + str2);
                    return i2;
                }
                z = false;
            }
            dumpSetEnabled(str, printWriter, z);
            return i2;
        } catch (IndexOutOfBoundsException unused) {
            printWriter.println("Error: aafwk time cost set must give target status.");
            return i2;
        }
    }

    private static int handleDumpClr(String str, PrintWriter printWriter, String[] strArr, int i) {
        if (i == strArr.length - 1) {
            dumpClrRecords(str, printWriter);
        } else {
            i++;
            if ("dead".equals(strArr[i])) {
                dumpClrDeadRecords(str, printWriter);
            } else {
                printWriter.println("Error: aafwk time cost clr argument error.");
            }
        }
        return i;
    }

    public static void dump(String str, PrintWriter printWriter, String[] strArr) {
        if (!(str == null || printWriter == null || strArr == null || strArr.length <= 1)) {
            boolean z = false;
            for (int i = 1; i < strArr.length; i++) {
                String str2 = strArr[i];
                if (str2.length() > 0) {
                    if (TIMECOST_COMMON.equals(str2)) {
                        z = true;
                    } else if (GET_TIMECOST_ENABLE.equals(str2)) {
                        dumpIsEnabled(str, printWriter);
                        return;
                    } else if (SET_TIMECOST_ENABLE.equals(str2)) {
                        handleDumpSetEnable(str, printWriter, strArr, i);
                        return;
                    } else if (CLR_TIMECOST_INVALID_CALLER.equals(str2)) {
                        handleDumpClr(str, printWriter, strArr, i);
                        return;
                    } else if (DUMP_TIMECOST_FORCE_CLOSE.equals(str2)) {
                        dumpTimeCosts(str, printWriter, true);
                        return;
                    } else {
                        printWriter.println("Error: arg: [" + str2 + "] is not support, use -h for help");
                        return;
                    }
                }
            }
            if (z) {
                dumpTimeCosts(str, printWriter, false);
            }
        }
    }
}
