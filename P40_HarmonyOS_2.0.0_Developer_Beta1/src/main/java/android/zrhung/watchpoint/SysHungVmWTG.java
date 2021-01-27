package android.zrhung.watchpoint;

import android.os.Process;
import android.os.SystemClock;
import android.util.ERecovery;
import android.util.ERecoveryEvent;
import android.util.ZRHung;
import android.zrhung.ZrHungData;
import android.zrhung.ZrHungImpl;
import com.huawei.android.os.ProcessEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.util.SlogEx;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SysHungVmWTG extends ZrHungImpl {
    private static final String BINDER_TRANS_PATH = "/sys/kernel/debug/binder/transactions";
    private static final String BINDER_TRANS_PATH_TRACING = "/sys/kernel/tracing/binder/transactions";
    static final int COMPLETED = 0;
    static final int OVERDUE = 3;
    static final String TAG = "ZrHung.SysHungVmWTG";
    private static final long VMWTG_COLDBOOT_ERECOVERYID = 401006000;
    private static final long VMWTG_COLDBOOT_FAULTID = 901004000;
    static final int WAITED_HALF = 2;
    static final int WAITING = 1;
    private String[] daemonsToCheck = null;
    private boolean isConfigReady = false;
    private boolean isEnable = false;
    private boolean isWaitedHalf = false;

    public SysHungVmWTG(String wpName) {
        super(wpName);
        SlogEx.i(TAG, "Init..");
    }

    @Override // android.zrhung.ZrHungImpl
    public boolean check(ZrHungData zrHungData) {
        if (zrHungData == null) {
            return false;
        }
        if (!this.isConfigReady) {
            ZRHung.HungConfig cfg = getConfig();
            if (cfg == null || cfg.status != 0) {
                SlogEx.e(TAG, "Failed to get config from zrhung");
                return false;
            }
            String[] configs = cfg.value.split(",");
            SlogEx.i(TAG, "Config from zrhung:" + cfg.value);
            this.isEnable = configs[0].equals("1");
            this.daemonsToCheck = (String[]) Arrays.copyOfRange(configs, 1, configs.length);
            this.isConfigReady = true;
        }
        if (!this.isEnable) {
            return false;
        }
        SlogEx.w(TAG, "System blocked, run checking!");
        int[] pids = ProcessEx.getPidsForCommands(this.daemonsToCheck);
        int length = pids.length;
        for (int i = 0; i < length; i++) {
            if (findBadProcess(pids[i])) {
                String msg = "Found process blocked: PID:" + pids[i] + " cmd: " + this.daemonsToCheck[i];
                SlogEx.e(TAG, msg);
                ZRHung.sendHungEvent(22, "p=" + Process.myPid() + ",p=" + pids[i], msg);
                if (zrHungData.getInt("waitState") == OVERDUE) {
                    SlogEx.e(TAG, "Daemon(s) blocked for over 60s, reboot to recover!");
                    vmWTGReportRecoveryBegin();
                    SystemClock.sleep(2000);
                    lowLevelReboot("Daemon(s) blocked");
                    return true;
                }
            }
        }
        return false;
    }

    private void vmWTGReportRecoveryBegin() {
        ERecoveryEvent beginEvent = new ERecoveryEvent();
        beginEvent.setERecoveryID((long) VMWTG_COLDBOOT_ERECOVERYID);
        beginEvent.setFaultID((long) VMWTG_COLDBOOT_FAULTID);
        beginEvent.setState(0);
        ERecovery.eRecoveryReport(beginEvent);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x004e, code lost:
        r7 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:?, code lost:
        r5.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0053, code lost:
        r8 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0054, code lost:
        r6.addSuppressed(r8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0057, code lost:
        throw r7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x005a, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:?, code lost:
        r4.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x005f, code lost:
        r7 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0060, code lost:
        r5.addSuppressed(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0063, code lost:
        throw r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0066, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:?, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x006b, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x006c, code lost:
        r4.addSuppressed(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x006f, code lost:
        throw r5;
     */
    private int readTransactionLines(List<String> transLines) {
        File file = new File(BINDER_TRANS_PATH);
        if (!file.exists()) {
            SlogEx.w(TAG, "file not exists : /sys/kernel/debug/binder/transactions");
            file = new File(BINDER_TRANS_PATH_TRACING);
            if (!file.exists()) {
                SlogEx.w(TAG, "file not exists : /sys/kernel/tracing/binder/transactions");
                return -1;
            }
        }
        try {
            InputStream in = new FileInputStream(file);
            Reader reader = new InputStreamReader(in, "UTF-8");
            BufferedReader buff = new BufferedReader(reader);
            int lineCount = computeLineCount(buff, transLines);
            buff.close();
            reader.close();
            in.close();
            return lineCount;
        } catch (FileNotFoundException e) {
            SlogEx.e(TAG, "FileNotFoundException");
            return 0;
        } catch (IOException e2) {
            SlogEx.e(TAG, "IOException");
            return 0;
        }
    }

    private int computeLineCount(BufferedReader reader, List<String> transLines) throws IOException {
        Pattern pattern = Pattern.compile("outgoing transaction");
        String readLine = reader.readLine();
        int lineCount = 0;
        while (readLine != null) {
            if (pattern.matcher(readLine).find()) {
                transLines.add(readLine);
                lineCount++;
            }
            readLine = reader.readLine();
        }
        return lineCount;
    }

    private boolean findBadProcess(int pid) {
        List<String> transLines = new ArrayList<>(16);
        if (readTransactionLines(transLines) > 0) {
            Pattern pattern = Pattern.compile("outgoing transaction .+ from (\\d+):(\\d+) to (\\d+):(\\d+) code ([0-9a-f]+)");
            int size = transLines.size();
            for (int i = 0; i < size; i++) {
                Matcher matcher = pattern.matcher(transLines.get(i));
                SlogEx.i(TAG, "search blocked process: pid = " + pid);
                if (matcher.find()) {
                    if (pid == parseInt(matcher.group(OVERDUE))) {
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        }
        return false;
    }

    private int parseInt(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            SlogEx.e(TAG, "parseInt NumberFormatException");
            return -1;
        }
    }

    private void lowLevelReboot(String reason) {
        SlogEx.w(TAG, "Low level reboot!");
        SystemPropertiesEx.set("sys.powerctl", "reboot," + reason);
    }
}
