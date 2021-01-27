package com.huawei.server.rme.hyperhold;

import android.os.FileUtils;
import android.util.Slog;
import com.huawei.displayengine.IDisplayEngineService;
import com.huawei.server.rme.hyperhold.AppInfo;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class KernelInterface {
    private static final String PATH_ACT = "activityApp";
    private static final String PATH_ORI = "/dev/memcg/apps/";
    private static final int PRIVILEGE = 504;
    private static final String TAG = "SWAP_KernelInterface";
    private static volatile KernelInterface kernelInterface;
    private static boolean logSwitch;

    private KernelInterface() {
    }

    public static KernelInterface getInstance() {
        if (kernelInterface == null) {
            synchronized (KernelInterface.class) {
                if (kernelInterface == null) {
                    logSwitch = ParaConfig.getInstance().getOtherParam().getLogEnable();
                    kernelInterface = new KernelInterface();
                }
            }
        }
        return kernelInterface;
    }

    public static long getBootDeviceSize() {
        StringBuilder sb;
        BufferedReader bfReader = null;
        try {
            BufferedReader bfReader2 = new BufferedReader(new FileReader("/proc/bootdevice/size"));
            String line = bfReader2.readLine();
            if (line != null) {
                long ufsSize = Long.parseLong(line.trim());
                try {
                    bfReader2.close();
                } catch (IOException ex) {
                    Slog.e(TAG, "nandlife::close bootdevice bfReader failed, ex:" + ex);
                }
                return ufsSize;
            }
            try {
                bfReader2.close();
                return 0;
            } catch (IOException e) {
                ex = e;
                sb = new StringBuilder();
            }
            sb.append("nandlife::close bootdevice bfReader failed, ex:");
            sb.append(ex);
            Slog.e(TAG, sb.toString());
            return 0;
        } catch (IOException | NumberFormatException ex2) {
            Slog.e(TAG, "nandlife::Can not open bootdevice, ex:" + ex2);
            if (0 == 0) {
                return 0;
            }
            try {
                bfReader.close();
                return 0;
            } catch (IOException e2) {
                ex = e2;
                sb = new StringBuilder();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    bfReader.close();
                } catch (IOException ex3) {
                    Slog.e(TAG, "nandlife::close bootdevice bfReader failed, ex:" + ex3);
                }
            }
            throw th;
        }
    }

    public void changeGroupToActivity(int pid) {
        if (!addToProcs(PATH_ACT, pid)) {
            Slog.e(TAG, "changeGroupToActivity error, pid = " + pid);
        }
        if (logSwitch) {
            Slog.i(TAG, "changeGroupToActivity pid =  " + pid);
        }
    }

    public boolean addToProcs(String pkg, int pid) {
        Slog.i(TAG, "addToProcs:" + pkg + " pid:" + pid);
        StringBuilder sb = new StringBuilder();
        sb.append(PATH_ORI);
        sb.append(pkg);
        sb.append("/cgroup.procs");
        String path = sb.toString();
        if (pid == 0) {
            return false;
        }
        return writeDevMemcg(path, pid + "");
    }

    public boolean mkDirectory(String pkgName) {
        File file = new File(PATH_ORI + pkgName);
        if (file.exists()) {
            return false;
        }
        boolean isSuccess = file.mkdirs();
        if (isSuccess) {
            setMemcgName(pkgName);
        }
        return isSuccess;
    }

    public void setScore(String pkgName, int score) {
        if (!writeDevMemcg(PATH_ORI + pkgName + "/memory.app_score", score + "")) {
            Slog.e(TAG, "setScore error, pkgname = " + pkgName + ", score = " + score);
        }
        if (logSwitch) {
            Slog.i(TAG, "setScore pkgName =  " + pkgName + " score = " + score);
        }
    }

    public void setRootAppScore(int score) {
        if (!writeDevMemcg("dev/memcg/memory.app_score", score + "")) {
            Slog.e(TAG, "set Root Score error, score = " + score);
        }
        if (logSwitch) {
            Slog.i(TAG, "set Root Score, score = " + score);
        }
    }

    public void writeRatioNew(String pkgName, int memZramRatio, int zramEswapRatio, int reclaimRefault) {
        if (!writeDevMemcg(PATH_ORI + pkgName + "/memory.zswapd_single_memcg_param", memZramRatio + " " + zramEswapRatio + " " + reclaimRefault)) {
            Slog.e(TAG, "write swap out Ratio error, pkgName = " + pkgName + ", memZramRatio = " + memZramRatio + ", zramEswapRatio = " + zramEswapRatio + ", reclaimRefault = " + reclaimRefault);
        }
        if (logSwitch) {
            Slog.i(TAG, "writeRatioNew, pkgName = " + pkgName + ", memZramRatio = " + memZramRatio + ", zramEswapRatio = " + zramEswapRatio + ", reclaimRefault = " + reclaimRefault);
        }
    }

    public void reclaimCurrentApp(String curApp) {
        String fileName = PATH_ORI + curApp + "/memory.force_shrink_anon";
        if (!writeDevMemcg(fileName, "anno")) {
            Slog.e(TAG, "write " + fileName + "failed");
        }
        Slog.i(TAG, "write " + fileName + " anno");
    }

    public void setRootRatio(int memZramRatio, int zramEswapRatio, int reclaimRefault) {
        if (!writeDevMemcg("dev/memcg/memory.zswapd_single_memcg_param", memZramRatio + " " + zramEswapRatio + " " + reclaimRefault)) {
            Slog.e(TAG, "write root swap out Ratio error, memZramRatio = " + memZramRatio + ", zramEswapRatio = " + zramEswapRatio + ", reclaimRefault = " + reclaimRefault);
        }
        if (logSwitch) {
            Slog.i(TAG, "write root Ratio, memZramRatio = " + memZramRatio + ", zramEswapRatio = " + zramEswapRatio + ", reclaimRefault = " + reclaimRefault);
        }
    }

    public void setSoftLimit(int limit) {
        if (!writeDevMemcg("dev/memcg/memory.soft_limit_in_bytes", limit + "")) {
            Slog.e(TAG, "setSoftLimit error, limit = " + limit);
        }
        if (logSwitch) {
            Slog.i(TAG, "setSoftLimit, limit = " + limit);
        }
    }

    public void setBuffer(int bufferSize, int lowBuffer, int highBuffer, int swapReserve) {
        if (!writeDevMemcg("/dev/memcg/memory.avail_buffers", bufferSize + " " + lowBuffer + " " + highBuffer + " " + swapReserve + "")) {
            Slog.e(TAG, "setBuffer error, bufferSize = " + bufferSize + " low: " + lowBuffer + " high:" + highBuffer);
        }
        if (logSwitch) {
            Slog.i(TAG, "setBuffer bufferSize = " + bufferSize + " low: " + lowBuffer + " high:" + highBuffer + " swapReserve:" + swapReserve);
        }
    }

    public void setEswap2Zram(String pkgName, int ratio) {
        if (!writeDevMemcg(PATH_ORI + pkgName + "/memory.ub_ufs2zram_ratio", ratio + "")) {
            Slog.e(TAG, "setEswap2Zram error, pkgname = " + pkgName + ", ratio = " + ratio);
        }
        if (logSwitch) {
            Slog.i(TAG, "seteSwap2Zram pkgName =  " + pkgName + " ratio = " + ratio);
        }
    }

    public void setForceSwapIn(String pkgName) {
        if (!writeDevMemcg(PATH_ORI + pkgName + "/memory.force_swapin", "0")) {
            Slog.e(TAG, "setForceSwapIn error, pkgname = " + pkgName);
        }
        if (logSwitch) {
            Slog.i(TAG, "setForceSwapIn pkgName =  " + pkgName);
        }
    }

    public boolean setMemcgName(String pkgName) {
        if (!writeDevMemcg(PATH_ORI + pkgName + "/memory.name", pkgName)) {
            Slog.e(TAG, "setMemcgName error, pkgname = " + pkgName);
            return false;
        } else if (!logSwitch) {
            return true;
        } else {
            Slog.i(TAG, "setMemcgName pkgName =  " + pkgName);
            return true;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:33:0x009f  */
    /* JADX WARNING: Removed duplicated region for block: B:43:? A[RETURN, SYNTHETIC] */
    public int getZswapdParamByName(String param) {
        StringBuilder sb;
        String line;
        BufferedReader bfReader = null;
        try {
            BufferedReader bfReader2 = new BufferedReader(new FileReader("/dev/memcg/memory.zswapd_presure_show"));
            StringBuffer strBuffer = new StringBuffer();
            do {
                line = bfReader2.readLine();
                if (line == null) {
                    try {
                        bfReader2.close();
                    } catch (IOException e) {
                        ex = e;
                        sb = new StringBuilder();
                    }
                    if (logSwitch) {
                        return IDisplayEngineService.DE_ACTION_PG_BROWSER_FRONT;
                    }
                    Slog.i(TAG, "getZswapdParamByName Finish");
                    return IDisplayEngineService.DE_ACTION_PG_BROWSER_FRONT;
                }
            } while (!line.contains(param));
            Matcher matcher = Pattern.compile("\\d").matcher(line);
            while (matcher.find()) {
                strBuffer.append(matcher.group());
            }
            int free = Integer.parseInt(strBuffer.toString());
            try {
                bfReader2.close();
            } catch (IOException ex) {
                Slog.e(TAG, "SWAPKILL::close bfReader failed, ex:" + ex);
            }
            return free;
            sb.append("SWAPKILL::close bfReader failed, ex:");
            sb.append(ex);
            Slog.e(TAG, sb.toString());
            if (logSwitch) {
            }
        } catch (IOException | NumberFormatException ex2) {
            Slog.e(TAG, "SWAPKILL::Can not open memory.zswapd_pressure, ex:" + ex2);
            if (0 != 0) {
                try {
                    bfReader.close();
                } catch (IOException e2) {
                    ex = e2;
                    sb = new StringBuilder();
                }
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    bfReader.close();
                } catch (IOException ex3) {
                    Slog.e(TAG, "SWAPKILL::close bfReader failed, ex:" + ex3);
                }
            }
            throw th;
        }
    }

    public int getZswapdPid() {
        StringBuilder sb;
        BufferedReader bfReader = null;
        try {
            BufferedReader bfReader2 = new BufferedReader(new FileReader("/dev/memcg/memory.zswapd_pid"));
            String line = bfReader2.readLine();
            if (line != null) {
                int parseInt = Integer.parseInt(line.trim());
                try {
                    bfReader2.close();
                } catch (IOException ex) {
                    Slog.e(TAG, "get zswapd pid fail, ex:" + ex);
                }
                return parseInt;
            }
            try {
                bfReader2.close();
                return -1;
            } catch (IOException e) {
                ex = e;
                sb = new StringBuilder();
            }
            sb.append("get zswapd pid fail, ex:");
            sb.append(ex);
            Slog.e(TAG, sb.toString());
            return -1;
        } catch (IOException | NumberFormatException ex2) {
            Slog.e(TAG, "get zswapd pid fail, ex:" + ex2);
            if (0 == 0) {
                return -1;
            }
            try {
                bfReader.close();
                return -1;
            } catch (IOException e2) {
                ex = e2;
                sb = new StringBuilder();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    bfReader.close();
                } catch (IOException ex3) {
                    Slog.e(TAG, "get zswapd pid fail, ex:" + ex3);
                }
            }
            throw th;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:22:0x00c8, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x00c9, code lost:
        $closeResource(r2, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x00cc, code lost:
        throw r3;
     */
    public AppInfo.MemcgInfo readMemcgInfo() {
        BufferedReader bfReader = new BufferedReader(new FileReader("/dev/memcg/memory.eswap_stat"));
        ArrayList<Integer> memcgInfoPara = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\d+");
        while (true) {
            String line = bfReader.readLine();
            if (line == null) {
                break;
            }
            Matcher matcher = pattern.matcher(line);
            while (matcher.find()) {
                memcgInfoPara.add(Integer.valueOf(matcher.group().toString()));
            }
        }
        if (memcgInfoPara.size() != 7) {
            try {
                $closeResource(null, bfReader);
                return null;
            } catch (IOException | NumberFormatException ex) {
                Slog.e(TAG, "readMemcgInfo Error:" + ex);
                return null;
            }
        } else {
            AppInfo.MemcgInfo memcgInfo = new AppInfo.MemcgInfo.Builder().swapOutTotal(memcgInfoPara.get(0).intValue()).swapOutSize(memcgInfoPara.get(1).intValue()).swapInSize(memcgInfoPara.get(2).intValue()).swapInTotal(memcgInfoPara.get(3).intValue()).pageInTotal(memcgInfoPara.get(4).intValue()).swapSizeCur(memcgInfoPara.get(5).intValue()).swapSizeMax(memcgInfoPara.get(6).intValue()).freezeTimes(0).unFreezeTimes(0).create();
            $closeResource(null, bfReader);
            return memcgInfo;
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

    /* JADX WARNING: Code restructure failed: missing block: B:22:0x00dc, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x00dd, code lost:
        $closeResource(r2, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x00e0, code lost:
        throw r3;
     */
    public AppInfo.MemcgInfo readAppMemcgInfo(String pkgName, int freezeTimes, int unFreezeTimes) {
        BufferedReader bfReader = new BufferedReader(new FileReader(PATH_ORI + pkgName + "/memory.eswap_stat"));
        ArrayList<Integer> memcgInfoPara = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\d+");
        while (true) {
            String line = bfReader.readLine();
            if (line == null) {
                break;
            }
            Matcher matcher = pattern.matcher(line);
            while (matcher.find()) {
                memcgInfoPara.add(Integer.valueOf(matcher.group().toString()));
            }
        }
        if (memcgInfoPara.size() != 7) {
            try {
                $closeResource(null, bfReader);
                return null;
            } catch (IOException | NumberFormatException ex) {
                Slog.e(TAG, "readAppMemcgInfo Error:" + ex);
                return null;
            }
        } else {
            AppInfo.MemcgInfo memcgInfo = new AppInfo.MemcgInfo.Builder().swapOutTotal(memcgInfoPara.get(0).intValue()).swapOutSize(memcgInfoPara.get(1).intValue()).swapInSize(memcgInfoPara.get(2).intValue()).swapInTotal(memcgInfoPara.get(3).intValue()).pageInTotal(memcgInfoPara.get(4).intValue()).swapSizeCur(memcgInfoPara.get(5).intValue()).swapSizeMax(memcgInfoPara.get(6).intValue()).freezeTimes(freezeTimes).unFreezeTimes(unFreezeTimes).create();
            $closeResource(null, bfReader);
            return memcgInfo;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x005e, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x005f, code lost:
        $closeResource(r3, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0062, code lost:
        throw r4;
     */
    public void printPsi() {
        try {
            BufferedReader bfReader = new BufferedReader(new FileReader("/dev/memcg/memory.psi_health_info"));
            String printInfo = "";
            Pattern pattern = Pattern.compile("\\d+\\.?\\d*");
            while (true) {
                String line = bfReader.readLine();
                if (line == null) {
                    break;
                }
                Matcher matcher = pattern.matcher(line);
                while (matcher.find()) {
                    printInfo = printInfo + matcher.group() + ",";
                }
            }
            Slog.i(TAG, "SWAP-psi:" + printInfo);
            $closeResource(null, bfReader);
        } catch (IOException ex) {
            Slog.e(TAG, "printPsi Error:" + ex);
        }
        if (logSwitch) {
            Slog.i(TAG, "printPsi Finish");
        }
    }

    public boolean delDirectory(String pkg) {
        String path = PATH_ORI + pkg;
        try {
            File file = new File(path);
            if (file.exists()) {
                return exeJavaDel(file.getCanonicalPath());
            }
            Slog.i(TAG, "Can not del unexists file." + path);
            return false;
        } catch (IOException | SecurityException ex) {
            Slog.i(TAG, "delDirectory error message:" + ex.toString());
            return false;
        }
    }

    private boolean exeJavaDel(String path) {
        try {
            File file = new File(path);
            if (!file.isDirectory() || "/dev/memcg/apps/com.android.deskclock".equals(path)) {
                return true;
            }
            file.delete();
            return true;
        } catch (SecurityException ex) {
            Slog.i(TAG, "Can not delete memcg." + path + " error message:" + ex.toString());
            return false;
        }
    }

    public boolean changeWriteBoostSwitch(boolean isOpenWriteBoost) {
        String content;
        if (isOpenWriteBoost) {
            content = "1";
        } else {
            content = "0";
        }
        if (!writeDevMemcg("/sys/bus/scsi/devices/host0/scsi_host/host0/wb_permanent_disable", content)) {
            Slog.e(TAG, "nandlife change write boost switch failed");
            return false;
        }
        Slog.i(TAG, "nandlife change write boost switch: " + isOpenWriteBoost);
        return true;
    }

    public boolean changeSwapOutSwitch(boolean isOpenSwapOut) {
        String fileNameOff = "/sys/block/zram0/hyperhold_enable";
        String fileNameOn = "/sys/block/zram0/dedup_enable";
        if (isOpenSwapOut) {
            fileNameOff = "/sys/block/zram0/dedup_enable";
            fileNameOn = "/sys/block/zram0/hyperhold_enable";
        }
        if (!writeDevMemcg(fileNameOff, "0")) {
            Slog.e(TAG, "nandlife close switch failed: " + fileNameOff);
        }
        if (!writeDevMemcg(fileNameOn, "1")) {
            Slog.e(TAG, "nandlife open switch failed: " + fileNameOn);
            return false;
        }
        Slog.i(TAG, "nandlife change swapout switch: " + isOpenSwapOut);
        return true;
    }

    public void feedDog(boolean isFeedDog) {
        if (!changeSwapOutSwitch(isFeedDog)) {
            Slog.e(TAG, "nandlife feed dog failed");
        } else if (isFeedDog) {
            Slog.e(TAG, "nandlife feed dog true");
        } else {
            Slog.e(TAG, "nandlife feed dog false");
        }
    }

    public double getSwapOutCountFromBoot() {
        StringBuilder sb;
        double ret = 0.0d;
        BufferedReader bfReader = null;
        try {
            BufferedReader bfReader2 = new BufferedReader(new FileReader("/dev/memcg/memory.psi_health_info"));
            StringBuffer strBuffer = new StringBuffer();
            while (true) {
                String line = bfReader2.readLine();
                if (line == null) {
                    try {
                        break;
                    } catch (IOException e) {
                        ex = e;
                        sb = new StringBuilder();
                    }
                } else if (line.contains("hyperhold_out_comp_size")) {
                    Matcher matcher = Pattern.compile("\\d").matcher(line);
                    while (matcher.find()) {
                        strBuffer.append(matcher.group());
                    }
                    ret = ((double) Integer.parseInt(strBuffer.toString())) / 1024.0d;
                }
            }
            bfReader2.close();
        } catch (IOException | NumberFormatException ex) {
            Slog.e(TAG, "nandlife ::Can not open /dev/memcg/memory.psi_health_info, ex:" + ex);
            if (0 != 0) {
                try {
                    bfReader.close();
                } catch (IOException e2) {
                    ex = e2;
                    sb = new StringBuilder();
                }
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    bfReader.close();
                } catch (IOException ex2) {
                    Slog.e(TAG, "nandlife::close bfReader failed, ex:" + ex2);
                }
            }
            throw th;
        }
        Slog.i(TAG, "nandlife from boot to now swap out count is" + ret);
        return ret;
        sb.append("nandlife::close bfReader failed, ex:");
        sb.append(ex);
        Slog.e(TAG, sb.toString());
        Slog.i(TAG, "nandlife from boot to now swap out count is" + ret);
        return ret;
    }

    public int getMemcgAnonTotal(String appName) {
        StringBuilder sb;
        int anonTotal = 0;
        BufferedReader bfReader = null;
        try {
            BufferedReader bfReader2 = new BufferedReader(new FileReader(PATH_ORI + appName + "/memory.stat"));
            StringBuffer strBuffer = new StringBuffer();
            while (true) {
                String line = bfReader2.readLine();
                if (line == null) {
                    try {
                        break;
                    } catch (IOException e) {
                        ex = e;
                        sb = new StringBuilder();
                    }
                } else if (line.contains("Anon:")) {
                    Matcher matcher = Pattern.compile("\\d").matcher(line);
                    while (matcher.find()) {
                        strBuffer.append(matcher.group());
                    }
                    anonTotal = Integer.parseInt(strBuffer.toString());
                }
            }
            bfReader2.close();
        } catch (IOException | NumberFormatException ex) {
            Slog.e(TAG, "getMemcgAnonTotal ::Can not open /dev/memcg/apps/" + appName + "/memory.stat, ex:" + ex);
            if (0 != 0) {
                try {
                    bfReader.close();
                } catch (IOException e2) {
                    ex = e2;
                    sb = new StringBuilder();
                }
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    bfReader.close();
                } catch (IOException ex2) {
                    Slog.e(TAG, "getMemcgAnonTotal::close bfReader failed, ex:" + ex2);
                }
            }
            throw th;
        }
        Slog.i(TAG, "getMemcgAnonTotal from boot to now swap out count is: " + appName + ", " + anonTotal);
        return anonTotal;
        sb.append("getMemcgAnonTotal::close bfReader failed, ex:");
        sb.append(ex);
        Slog.e(TAG, sb.toString());
        Slog.i(TAG, "getMemcgAnonTotal from boot to now swap out count is: " + appName + ", " + anonTotal);
        return anonTotal;
    }

    private boolean writeDevMemcg(String fileName, String content) {
        StringBuilder sb;
        boolean isSuccess = true;
        BufferedWriter br = null;
        try {
            br = new BufferedWriter(new FileWriter(fileName));
            br.write(content);
            br.flush();
            try {
                br.close();
            } catch (IOException e) {
                ex = e;
                sb = new StringBuilder();
            }
        } catch (IOException ex) {
            Slog.e(TAG, "writeDevMemcg::writeDevMemcg_ERROR:" + fileName + " content:" + content + ", ex:" + ex);
            isSuccess = false;
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e2) {
                    ex = e2;
                    sb = new StringBuilder();
                }
            }
        } catch (Throwable th) {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex2) {
                    Slog.e(TAG, "writeDevMemcg::writeDevMemcg_ERROR: br close failed, ex:" + ex2);
                }
            }
            throw th;
        }
        return isSuccess;
        sb.append("writeDevMemcg::writeDevMemcg_ERROR: br close failed, ex:");
        sb.append(ex);
        Slog.e(TAG, sb.toString());
        return isSuccess;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0096, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0097, code lost:
        $closeResource(r0, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x009b, code lost:
        throw r0;
     */
    public ConcurrentHashMap<String, AppInfo> getActiveRatioRefault(int snapIndex) {
        ConcurrentHashMap<String, AppInfo> resMap = new ConcurrentHashMap<>();
        try {
            BufferedReader bfReader = new BufferedReader(new FileReader("/dev/memcg/memory.active_app_info_list"));
            while (true) {
                String line = bfReader.readLine();
                if (line == null) {
                    $closeResource(null, bfReader);
                    break;
                }
                String[] buff = line.split(" ");
                if (buff.length == 6) {
                    String appName = buff[0];
                    int score = Integer.valueOf(buff[1]).intValue();
                    int memAnon = Integer.valueOf(buff[2]).intValue();
                    int zramAnon = Integer.valueOf(buff[3]).intValue();
                    int eswapAnon = Integer.valueOf(buff[4]).intValue();
                    int refault = Integer.valueOf(buff[5]).intValue();
                    if (score <= 300) {
                        if (score != 0) {
                            AppInfo appInfo = getActiveAppInfo(appName, memAnon, zramAnon + eswapAnon, refault, snapIndex);
                            if (!(appInfo == null || appName == null || appName.length() == 0)) {
                                resMap.put(appName, appInfo);
                            }
                        }
                    }
                }
            }
        } catch (IOException ex) {
            Slog.e(TAG, "getActiveRatioRefault IOException:" + ex);
        } catch (NumberFormatException ex2) {
            Slog.e(TAG, "getActiveRatioRefault NumberFormatException:" + ex2);
        }
        return resMap;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:22:0x005f, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0060, code lost:
        $closeResource(r5, r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0063, code lost:
        throw r6;
     */
    public int getAnnoRamSumByName(String appName) {
        if (appName == null) {
            return -1;
        }
        try {
            BufferedReader bfReader = new BufferedReader(new FileReader("/dev/memcg/memory.active_app_info_list"));
            while (true) {
                String line = bfReader.readLine();
                if (line == null) {
                    $closeResource(null, bfReader);
                    break;
                }
                String[] buff = line.split(" ");
                if (buff.length == 6) {
                    if (appName.equals(buff[0])) {
                        int intValue = Integer.valueOf(buff[2]).intValue() + Integer.valueOf(buff[3]).intValue() + Integer.valueOf(buff[4]).intValue();
                        $closeResource(null, bfReader);
                        return intValue;
                    }
                }
            }
        } catch (IOException ex) {
            Slog.e(TAG, "getAnnoRamSumByName fail, IOException:" + ex);
        } catch (NumberFormatException ex2) {
            Slog.e(TAG, "getActiveRatioRefault fail, NumberFormatException:" + ex2);
        }
        return -1;
    }

    private AppInfo getActiveAppInfo(String appName, int memAnon, int coldAnon, int refault, int snapIndex) {
        AppInfo appInfo = AppModel.getInstance().getAppInfoByAppName(appName);
        if (appInfo != null) {
            appInfo.setRealRatio((coldAnon * 100) / ((memAnon + coldAnon) + 1));
            appInfo.setAnonTotal(memAnon + coldAnon);
            int deltaRefault = refault - (appInfo.getRefault() * snapIndex);
            appInfo.setRefault(deltaRefault > 0 ? deltaRefault : 0);
        }
        return appInfo;
    }

    public void writeSwapFile(String fileName, String content, boolean isOverWrite) {
        FileOutputStream stream;
        Path path = Paths.get("/data/log/iaware/hyperhold/", new String[0]);
        if (!Files.exists(path, new LinkOption[0])) {
            try {
                Files.createDirectories(path, new FileAttribute[0]);
                Slog.i(TAG, "log_priv: hyperhold permission grant.");
                if (FileUtils.setPermissions("/data/log/iaware", PRIVILEGE, -1, -1) != 0) {
                    Slog.e(TAG, "log_priv: iaware folder permission grant failed.");
                }
                if (FileUtils.setPermissions(path.toFile(), PRIVILEGE, -1, -1) != 0) {
                    Slog.e(TAG, "log_priv: hyperhold permission grant failed.");
                }
            } catch (IOException e) {
                Slog.e(TAG, "Can't create hyperhold dir for SWAP log");
            }
        }
        File logFile = new File("/data/log/iaware/hyperhold/" + fileName + ".log");
        FileOutputStream stream2 = null;
        try {
            if (logFile.createNewFile()) {
                Slog.i(TAG, "log_priv: log permission grant.");
                if (FileUtils.setPermissions(logFile.getPath(), PRIVILEGE, -1, -1) != 0) {
                    Slog.e(TAG, "log_priv: " + fileName + " permission grant failed.");
                }
            }
            if (isOverWrite) {
                stream = new FileOutputStream(logFile);
            } else {
                stream = new FileOutputStream(logFile, true);
            }
            stream.write(content.getBytes(Charset.defaultCharset()));
            try {
                stream.close();
            } catch (IOException e2) {
                Slog.e(TAG, "Can't close file for SWAP log");
            }
        } catch (FileNotFoundException e3) {
            Slog.e(TAG, "log file for SWAP not found");
            if (0 != 0) {
                stream2.close();
            }
        } catch (IOException e4) {
            Slog.e(TAG, "Can't create file for SWAP log");
            if (0 != 0) {
                stream2.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    stream2.close();
                } catch (IOException e5) {
                    Slog.e(TAG, "Can't close file for SWAP log");
                }
            }
            throw th;
        }
    }

    public int readLifeTimeEstA() {
        StringBuilder sb;
        BufferedReader bfReader = null;
        try {
            BufferedReader bfReader2 = new BufferedReader(new FileReader("/proc/bootdevice/life_time_est_typ_a"));
            String line = bfReader2.readLine();
            if (line != null) {
                int res = Integer.parseInt(line.substring(2, line.length()), 16);
                Slog.e(TAG, "readLifeTimeEstA: " + res);
                try {
                    bfReader2.close();
                } catch (IOException ex) {
                    Slog.e(TAG, "close readLifeTimeEstA bfReader failed, ex:" + ex);
                }
                return res;
            }
            try {
                bfReader2.close();
                return 11;
            } catch (IOException e) {
                ex = e;
                sb = new StringBuilder();
            }
            sb.append("close readLifeTimeEstA bfReader failed, ex:");
            sb.append(ex);
            Slog.e(TAG, sb.toString());
            return 11;
        } catch (IOException | NumberFormatException ex2) {
            Slog.e(TAG, "readLifeTimeEstA Error:" + ex2);
            if (0 == 0) {
                return 11;
            }
            try {
                bfReader.close();
                return 11;
            } catch (IOException e2) {
                ex = e2;
                sb = new StringBuilder();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    bfReader.close();
                } catch (IOException ex3) {
                    Slog.e(TAG, "close readLifeTimeEstA bfReader failed, ex:" + ex3);
                }
            }
            throw th;
        }
    }

    public int readLifeTimeEstB() {
        StringBuilder sb;
        BufferedReader bfReader = null;
        try {
            BufferedReader bfReader2 = new BufferedReader(new FileReader("/proc/bootdevice/life_time_est_typ_b"));
            String line = bfReader2.readLine();
            if (line != null) {
                int res = Integer.parseInt(line.substring(2, line.length()), 16);
                Slog.e(TAG, "readLifeTimeEstB: " + res);
                try {
                    bfReader2.close();
                } catch (IOException ex) {
                    Slog.e(TAG, "close readLifeTimeEstB bfReader failed, ex:" + ex);
                }
                return res;
            }
            try {
                bfReader2.close();
                return 11;
            } catch (IOException e) {
                ex = e;
                sb = new StringBuilder();
            }
            sb.append("close readLifeTimeEstB bfReader failed, ex:");
            sb.append(ex);
            Slog.e(TAG, sb.toString());
            return 11;
        } catch (IOException | NumberFormatException ex2) {
            Slog.e(TAG, "readLifeTimeEstB Error:" + ex2);
            if (0 == 0) {
                return 11;
            }
            try {
                bfReader.close();
                return 11;
            } catch (IOException e2) {
                ex = e2;
                sb = new StringBuilder();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    bfReader.close();
                } catch (IOException ex3) {
                    Slog.e(TAG, "close readLifeTimeEstB bfReader failed, ex:" + ex3);
                }
            }
            throw th;
        }
    }

    public int readLifeTimePreEolInfo() {
        StringBuilder sb;
        BufferedReader bfReader = null;
        try {
            BufferedReader bfReader2 = new BufferedReader(new FileReader("/proc/bootdevice/pre_eol_info"));
            String line = bfReader2.readLine();
            if (line != null) {
                int res = Integer.parseInt(line.substring(2, line.length()), 16);
                Slog.e(TAG, "readLifeTimePreEolInfo: " + res);
                try {
                    bfReader2.close();
                } catch (IOException ex) {
                    Slog.e(TAG, "close readLifeTimePreEolInfo bfReader failed, ex:" + ex);
                }
                return res;
            }
            try {
                bfReader2.close();
                return 0;
            } catch (IOException e) {
                ex = e;
                sb = new StringBuilder();
            }
            sb.append("close readLifeTimePreEolInfo bfReader failed, ex:");
            sb.append(ex);
            Slog.e(TAG, sb.toString());
            return 0;
        } catch (IOException | NumberFormatException ex2) {
            Slog.e(TAG, "readLifeTimePreEolInfo Error:" + ex2);
            if (0 == 0) {
                return 0;
            }
            try {
                bfReader.close();
                return 0;
            } catch (IOException e2) {
                ex = e2;
                sb = new StringBuilder();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    bfReader.close();
                } catch (IOException ex3) {
                    Slog.e(TAG, "close readLifeTimePreEolInfo bfReader failed, ex:" + ex3);
                }
            }
            throw th;
        }
    }
}
