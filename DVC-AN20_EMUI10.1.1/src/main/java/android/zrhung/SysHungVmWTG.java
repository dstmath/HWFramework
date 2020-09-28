package android.zrhung;

import android.os.Process;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.ERecovery;
import android.util.ERecoveryEvent;
import android.util.Slog;
import android.util.ZRHung;
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
        Slog.i(TAG, "Init..");
    }

    @Override // android.zrhung.ZrHungImpl
    public boolean check(ZrHungData zrHungData) {
        if (zrHungData == null) {
            return false;
        }
        if (!this.isConfigReady) {
            ZRHung.HungConfig cfg = getConfig();
            if (cfg == null || cfg.status != 0) {
                Slog.e(TAG, "Failed to get config from zrhung");
                return false;
            }
            String[] configs = cfg.value.split(",");
            Slog.i(TAG, "Config from zrhung:" + cfg.value);
            this.isEnable = configs[0].equals("1");
            this.daemonsToCheck = (String[]) Arrays.copyOfRange(configs, 1, configs.length);
            this.isConfigReady = true;
        }
        if (!this.isEnable) {
            return false;
        }
        Slog.w(TAG, "System blocked, run checking!");
        int[] pids = Process.getPidsForCommands(this.daemonsToCheck);
        int length = pids.length;
        for (int i = 0; i < length; i++) {
            if (findBadProcess(pids[i])) {
                String msg = "Found process blocked: PID:" + pids[i] + " cmd: " + this.daemonsToCheck[i];
                Slog.e(TAG, msg);
                ZRHung.sendHungEvent(22, "p=" + Process.myPid() + ",p=" + pids[i], msg);
                if (zrHungData.getInt("waitState") == 3) {
                    Slog.e(TAG, "Daemon(s) blocked for over 60s, reboot to recover!");
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
        beginEvent.setERecoveryID(VMWTG_COLDBOOT_ERECOVERYID);
        beginEvent.setFaultID(VMWTG_COLDBOOT_FAULTID);
        beginEvent.setState(0);
        ERecovery.eRecoveryReport(beginEvent);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x004f, code lost:
        r7 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0050, code lost:
        $closeResource(r6, r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0053, code lost:
        throw r7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0056, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0057, code lost:
        $closeResource(r5, r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x005a, code lost:
        throw r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x005d, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x005e, code lost:
        $closeResource(r4, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0061, code lost:
        throw r5;
     */
    private int readTransactionLines(List<String> transLines) {
        File file = new File(BINDER_TRANS_PATH);
        if (!file.exists()) {
            Slog.w(TAG, "file not exists : /sys/kernel/debug/binder/transactions");
            file = new File(BINDER_TRANS_PATH_TRACING);
            if (!file.exists()) {
                Slog.w(TAG, "file not exists : /sys/kernel/tracing/binder/transactions");
                return -1;
            }
        }
        try {
            InputStream in = new FileInputStream(file);
            Reader reader = new InputStreamReader(in, "UTF-8");
            BufferedReader buff = new BufferedReader(reader);
            int lineCount = computeLineCount(buff, transLines);
            $closeResource(null, buff);
            $closeResource(null, reader);
            $closeResource(null, in);
            return lineCount;
        } catch (FileNotFoundException e) {
            Slog.e(TAG, "FileNotFoundException");
            return 0;
        } catch (IOException e2) {
            Slog.e(TAG, "IOException");
            return 0;
        }
    }

    private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
        if (x0 != null) {
            try {
                x1.close();
            } catch (Throwable th) {
                x0.addSuppressed(th);
            }
        } else {
            x1.close();
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
                Slog.i(TAG, "search blocked process: pid = " + pid);
                if (matcher.find()) {
                    if (pid == parseInt(matcher.group(3))) {
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
            Slog.e(TAG, "parseInt NumberFormatException");
            return -1;
        }
    }

    private void lowLevelReboot(String reason) {
        Slog.w(TAG, "Low level reboot!");
        SystemProperties.set("sys.powerctl", "reboot," + reason);
    }
}
