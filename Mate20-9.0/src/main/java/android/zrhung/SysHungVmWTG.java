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
    private boolean configReady = false;
    private String[] daemonsToCheck = null;
    private boolean enable = false;
    private boolean waitedHalf = false;

    public SysHungVmWTG(String wpName) {
        super(wpName);
        Slog.i(TAG, "Init..");
    }

    public boolean check(ZrHungData args) {
        if (!this.configReady) {
            ZRHung.HungConfig cfg = getConfig();
            if (cfg == null || cfg.status != 0) {
                Slog.e(TAG, "Failed to get config from zrhung");
                return false;
            }
            String[] configs = cfg.value.split(",");
            Slog.i(TAG, "Config from zrhung:" + cfg.value);
            this.enable = configs[0].equals("1");
            this.daemonsToCheck = (String[]) Arrays.copyOfRange(configs, 1, configs.length);
            this.configReady = true;
        }
        if (!this.enable) {
            return false;
        }
        Slog.w(TAG, "System blocked, run checking!");
        for (int findBadProcess : Process.getPidsForCommands(this.daemonsToCheck)) {
            if (findBadProcess(findBadProcess)) {
                String msg = "Found process blocked: PID:" + pids[i] + " cmd: " + this.daemonsToCheck[i];
                Slog.e(TAG, msg);
                ZRHung.sendHungEvent(22, "p=" + Process.myPid() + ",p=" + pids[i], msg);
                if (args.getInt("waitState") == 3) {
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

    private int readTransactionLines(ArrayList<String> transLines) {
        int lineCount = 0;
        BufferedReader buff = null;
        File file = new File(BINDER_TRANS_PATH);
        InputStream in = null;
        Reader reader = null;
        if (!file.exists()) {
            Slog.w(TAG, "file not exists : /sys/kernel/debug/binder/transactions");
            file = new File(BINDER_TRANS_PATH_TRACING);
            if (!file.exists()) {
                Slog.w(TAG, "file not exists : /sys/kernel/tracing/binder/transactions");
                return -1;
            }
        }
        try {
            InputStream in2 = new FileInputStream(file);
            Reader reader2 = new InputStreamReader(in2, "UTF-8");
            BufferedReader buff2 = new BufferedReader(reader2);
            Pattern pattern = Pattern.compile("outgoing transaction");
            for (String readLine = buff2.readLine(); readLine != null; readLine = buff2.readLine()) {
                if (pattern.matcher(readLine).find()) {
                    transLines.add(readLine);
                    lineCount++;
                }
            }
            try {
                in2.close();
                buff2.close();
                reader2.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e2) {
            e2.printStackTrace();
            if (in != null) {
                in.close();
            }
            if (buff != null) {
                buff.close();
            }
            if (reader != null) {
                reader.close();
            }
        } catch (IOException e3) {
            e3.printStackTrace();
            if (in != null) {
                in.close();
            }
            if (buff != null) {
                buff.close();
            }
            if (reader != null) {
                reader.close();
            }
        } catch (Throwable th) {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e4) {
                    e4.printStackTrace();
                    throw th;
                }
            }
            if (buff != null) {
                buff.close();
            }
            if (reader != null) {
                reader.close();
            }
            throw th;
        }
        return lineCount;
    }

    private boolean findBadProcess(int pid) {
        ArrayList<String> transLines = new ArrayList<>();
        if (readTransactionLines(transLines) > 0) {
            Pattern pattern = Pattern.compile("outgoing transaction .+ from (\\d+):(\\d+) to (\\d+):(\\d+) code ([0-9a-f]+)");
            int size = transLines.size();
            for (int i = 0; i < size; i++) {
                Matcher matcher = pattern.matcher(transLines.get(i));
                Slog.i(TAG, "search blocked process: pid = " + pid);
                if (matcher.find() && pid == Integer.parseInt(matcher.group(3))) {
                    return true;
                }
            }
        }
        return false;
    }

    private void lowLevelReboot(String reason) {
        Slog.w(TAG, "Low level reboot!");
        SystemProperties.set("sys.powerctl", "reboot," + reason);
    }
}
