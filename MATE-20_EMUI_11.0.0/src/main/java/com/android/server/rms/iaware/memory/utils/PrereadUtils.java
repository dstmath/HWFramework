package com.android.server.rms.iaware.memory.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.rms.iaware.AwareLog;
import com.huawei.android.app.ActivityManagerNativeExt;
import com.huawei.android.content.ContextEx;
import com.huawei.android.content.pm.ApplicationInfoExt;
import com.huawei.android.content.pm.UserInfoExAdapter;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.os.ZygoteInitEx;
import com.huawei.dalvik.system.DexFileEx;
import com.huawei.server.pm.InstallerEx;
import com.huawei.server.pm.InstructionSetsEx;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class PrereadUtils {
    private static final int BUFFER_ALLOCATE_FOUR_ARGS_LENGTH = 268;
    private static final int BUFFER_ALLOCATE_THREE_ARGS_LENGTH = 264;
    private static final int BUFFER_LENGTH_MAX = 255;
    private static final int BUFFER_LENGTH_MIN = 1;
    private static final int CAMERA_POWERUP_MEMORY_MAX = 256000;
    private static final int CAMERA_POWERUP_MEMORY_MIN = 0;
    private static final AtomicBoolean CONFIG_UPDATING = new AtomicBoolean(true);
    private static final int DATA_UPDATE_MSG = 1;
    private static final int DEV_SCREEN_OFF_PROCESS_CAMERA_MSG = 3;
    private static final long DEV_SCREEN_ON_CAMERA_MSG_DELAY = 2000;
    private static final int DEV_SCREEN_ON_PROCESS_CAMERA_MSG = 2;
    private static final String DEX_SUFFIX = ".dex";
    private static final int INTEGER_BYTES = 4;
    private static final Object LOCK = new Object();
    private static final int MAX_DIR_LOOP = 5;
    private static final int MAX_PREREAD_PKG_NUM = 200;
    private static final AtomicBoolean MEMORY_SWITCH = new AtomicBoolean(false);
    private static final int MSG_MEM_PREREAD_ID = 4662;
    private static final int PATH_LENGTH = 2;
    private static final int PRELAUNCH_FRONT = 1;
    private static final int PRELAUNCH_SCREEN_OFF = 1001;
    private static final int PRELAUNCH_SCREEN_ON = 1000;
    private static final long PREREAD_DATA_UPDATE_MSG_DELAY = 20000;
    private static final int PREREAD_FILE_NUM = 2;
    private static final int RECV_BYTE_BUFFER_LENTH = 8;
    private static final AtomicBoolean SCREEN_STATUS = new AtomicBoolean(true);
    private static final int SWITCH_ON = 1;
    private static final String TAG = "AwareMem_PrereadUtils";
    private static final String VDEX_SUFFIX = ".vdex";
    private static final int WAKE_UP_SCREEN_MSG = 4;
    private static boolean isPrereadOdex = false;
    private static Context sContext;
    private static Set<String> sExcludedPkgSet = new HashSet();
    private static InstallerEx sInstaller;
    private static List<String> sPrereadPkgList = new ArrayList();
    private static PrereadUtils sPrereadUtils;
    private PrereadHandler mPrereadHandler = new PrereadHandler();

    /* access modifiers changed from: private */
    public enum SetPathType {
        REMOVE_PATH,
        SET_PACKAGE,
        SET_PATH
    }

    private PrereadUtils() {
    }

    public static void setContext(Context context) {
        sContext = context;
        if (ZygoteInitEx.isMygote()) {
            sInstaller = new InstallerEx(sContext);
            sInstaller.onStart();
        }
    }

    public static PrereadUtils getInstance() {
        PrereadUtils prereadUtils;
        synchronized (LOCK) {
            if (sPrereadUtils == null) {
                sPrereadUtils = new PrereadUtils();
            }
            prereadUtils = sPrereadUtils;
        }
        return prereadUtils;
    }

    public static void start() {
        MEMORY_SWITCH.set(true);
    }

    public static void stop() {
        sPrereadPkgList.clear();
        sExcludedPkgSet.clear();
        MEMORY_SWITCH.set(false);
    }

    public static void setPrereadOdexSwitch(int prereadSwitch) {
        if (Build.VERSION.SDK_INT < 26) {
            isPrereadOdex = false;
            return;
        }
        boolean z = true;
        if (prereadSwitch != 1) {
            z = false;
        }
        isPrereadOdex = z;
    }

    private static boolean checkStatus(String pkgName) {
        if (pkgName == null || sContext == null) {
            return false;
        }
        if (!(!MEMORY_SWITCH.get() || CONFIG_UPDATING.get() || !isPrereadOdex) && !sExcludedPkgSet.contains(pkgName)) {
            return true;
        }
        return false;
    }

    public static boolean addPkgFilesIfNecessary(String pkgName) {
        if (!checkStatus(pkgName)) {
            return false;
        }
        if (sPrereadPkgList.contains(pkgName)) {
            return true;
        }
        List<String> fileList = getDexfilesFromPkg(pkgName);
        if (fileList.isEmpty() || fileList.size() > 2) {
            sExcludedPkgSet.add(pkgName);
            return false;
        }
        if (sPrereadPkgList.size() >= 200) {
            removePackageFiles(sPrereadPkgList.remove(0));
        }
        if (!setPrereadPath(SetPathType.SET_PACKAGE.ordinal(), pkgName)) {
            sExcludedPkgSet.add(pkgName);
            return false;
        }
        for (String filePath : fileList) {
            if (filePath != null) {
                setPrereadPath(SetPathType.SET_PATH.ordinal(), filePath);
            }
        }
        sPrereadPkgList.add(pkgName);
        return true;
    }

    private static List<String> getDexfilesFromPkg(String pkgName) {
        String sourceDir;
        List<String> fileList = new ArrayList<>();
        try {
            UserInfoExAdapter currentUser = ActivityManagerNativeExt.getCurrentUser();
            if (currentUser == null) {
                return fileList;
            }
            Context context = ContextEx.createPackageContextAsUser(sContext, pkgName, 1152, UserHandleEx.getUserHandle(currentUser.getUserId()));
            if ((context.getApplicationInfo() != null && (ApplicationInfoExt.getHwFlags(context.getApplicationInfo()) & 16777216) != 0) || (sourceDir = context.getApplicationInfo().sourceDir) == null) {
                return fileList;
            }
            if (sourceDir.lastIndexOf(File.separator) <= 0) {
                AwareLog.w(TAG, "source dir name error");
            } else {
                traverseFolder(sourceDir.substring(0, sourceDir.lastIndexOf(File.separator)), fileList, 0);
            }
            if (fileList.size() != 0) {
                return fileList;
            }
            return getDexFileStatus(context);
        } catch (PackageManager.NameNotFoundException e) {
            AwareLog.e(TAG, "getDexfilesFromPkg NameNotFoundException.");
            return fileList;
        } catch (RemoteException e2) {
            AwareLog.e(TAG, "getDexfilesFromPkg remoteException.");
            return fileList;
        }
    }

    private static void processPaths(ArrayList<String> paths, List<String> fileList, String[] instructionSets) throws FileNotFoundException {
        int pathSize = paths.size();
        for (String instructionSet : instructionSets) {
            if (instructionSet != null) {
                for (int i = 0; i < pathSize; i++) {
                    processPath(paths.get(i), fileList, instructionSet);
                }
            }
        }
    }

    private static void processPath(String dir, List<String> fileList, String instructionSet) throws FileNotFoundException {
        String[] outputPaths;
        if (dir.lastIndexOf(File.separator) > 0 && (outputPaths = DexFileEx.getDexFileOutputPaths(dir, instructionSet)) != null && outputPaths.length == 2 && outputPaths[0].lastIndexOf(DEX_SUFFIX) > 0 && outputPaths[1].lastIndexOf(VDEX_SUFFIX) > 0) {
            fileList.add(outputPaths[0]);
            fileList.add(outputPaths[1]);
        }
    }

    private static List<String> getDexFileStatus(Context context) {
        String[] dexCodeInstructionSets;
        ApplicationInfo info = context.getApplicationInfo();
        String[] instructionSets = InstructionSetsEx.getAppDexInstructionSets(info);
        List<String> fileList = new ArrayList<>();
        if (instructionSets == null || (dexCodeInstructionSets = InstructionSetsEx.getDexCodeInstructionSets(instructionSets)) == null) {
            return fileList;
        }
        ArrayList<String> paths = new ArrayList<>();
        if (info.sourceDir != null) {
            paths.add(info.sourceDir);
        }
        String[] splitSourceDirs = info.splitSourceDirs;
        if (splitSourceDirs != null) {
            for (String splitSourceDir : splitSourceDirs) {
                if (splitSourceDir != null) {
                    paths.add(splitSourceDir);
                }
            }
        }
        if (ZygoteInitEx.isMygote()) {
            fileList = getDexFileStatusForMaple(paths, dexCodeInstructionSets, info.uid);
        } else {
            try {
                processPaths(paths, fileList, dexCodeInstructionSets);
            } catch (FileNotFoundException e) {
                AwareLog.e(TAG, "preread getDexFileStatus FileNotFoundException ");
            }
        }
        if (fileList.size() != 2) {
            fileList.clear();
        }
        return fileList;
    }

    private static List<String> getDexFileStatusForMaple(List<String> paths, String[] dexCodeInstructionSets, int uid) {
        List<String> fileList = new ArrayList<>();
        InstallerEx installerEx = sInstaller;
        if (installerEx == null || installerEx.isInstallerNull()) {
            return fileList;
        }
        ArrayList<String> filePathList = new ArrayList<>();
        ArrayList<String> instructionSetlist = new ArrayList<>();
        for (String instructionSet : dexCodeInstructionSets) {
            for (String dir : paths) {
                if (!(instructionSet == null || dir == null)) {
                    filePathList.add(dir);
                    instructionSetlist.add(instructionSet);
                }
            }
        }
        if (filePathList.size() <= 0) {
            return fileList;
        }
        int size = filePathList.size();
        String[] dexPaths = new String[size];
        String[] isas = new String[size];
        int[] uids = new int[size];
        for (int i = 0; i < size; i++) {
            dexPaths[i] = filePathList.get(i);
            isas[i] = instructionSetlist.get(i);
            uids[i] = uid;
        }
        String[] retDexStatus = sInstaller.getDexFileStatus(dexPaths, isas, uids, TAG);
        if (retDexStatus == null || retDexStatus.length != size) {
            return fileList;
        }
        for (String status : retDexStatus) {
            if (status != null && status.lastIndexOf(DEX_SUFFIX) > 0) {
                String odexPath = status.substring(0, status.lastIndexOf(DEX_SUFFIX) + DEX_SUFFIX.length());
                fileList.add(odexPath);
                fileList.add(odexPath.substring(0, odexPath.lastIndexOf(".")) + VDEX_SUFFIX);
            }
        }
        return fileList;
    }

    public static void removePackageFiles(String pkgName) {
        if (pkgName != null) {
            setPrereadPath(SetPathType.REMOVE_PATH.ordinal(), pkgName);
            sPrereadPkgList.remove(pkgName);
            sExcludedPkgSet.remove(pkgName);
        }
    }

    private static void traverseFolder(String path, List<String> fileList, int loop) {
        File[] files;
        if (!(loop > 5 || path == null || fileList == null)) {
            File file = new File(path);
            if (!(!file.exists() || (files = file.listFiles()) == null || files.length == 0)) {
                for (File subFile : files) {
                    if (subFile != null) {
                        try {
                            String subFilePath = subFile.getCanonicalPath();
                            if (subFile.isDirectory()) {
                                traverseFolder(subFilePath, fileList, loop + 1);
                            } else if (filterFiles(subFilePath) && !fileList.contains(subFilePath)) {
                                fileList.add(subFilePath);
                            }
                        } catch (IOException e) {
                            AwareLog.e(TAG, "traverseFolder: getCanonicalPath IOException!");
                            return;
                        }
                    }
                }
            }
        }
    }

    private static boolean filterFiles(String fileName) {
        if (fileName == null) {
            return false;
        }
        if (fileName.endsWith(".odex") || fileName.endsWith(VDEX_SUFFIX)) {
            return true;
        }
        return false;
    }

    private static int parseRecvPacket(int byteSize) {
        byte[] recvByte = MemoryUtils.recvPacket(byteSize);
        int ret = -1;
        if (recvByte.length == 0) {
            return -1;
        }
        Parcel recvMsgParcel = Parcel.obtain();
        recvMsgParcel.unmarshall(recvByte, 0, recvByte.length);
        recvMsgParcel.setDataPosition(0);
        int funcId = recvMsgParcel.readInt();
        int status = recvMsgParcel.readInt();
        if (funcId == MSG_MEM_PREREAD_ID) {
            ret = status;
        }
        recvMsgParcel.recycle();
        return ret;
    }

    public static void sendPrereadMsg(String pkgName) {
        if (pkgName != null && MEMORY_SWITCH.get() && !CONFIG_UPDATING.get()) {
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_ALLOCATE_THREE_ARGS_LENGTH);
            try {
                byte[] stringBytes = pkgName.getBytes("UTF-8");
                if (stringBytes.length >= 1) {
                    if (stringBytes.length <= BUFFER_LENGTH_MAX) {
                        AwareLog.i(TAG, "sendPrereadMsg pkgname = " + pkgName);
                        buffer.clear();
                        buffer.putInt(MemoryConstant.MSG_PREREAD_FILE);
                        buffer.putInt(stringBytes.length);
                        buffer.put(stringBytes);
                        buffer.putChar(0);
                        if (MemoryUtils.sendPacket(buffer) == -1) {
                            AwareLog.w(TAG, "sendPrereadMsg sendPacket failed");
                            return;
                        }
                        if (parseRecvPacket(8) == 0) {
                            sPrereadPkgList.clear();
                            prereadDataUpdate();
                        }
                        return;
                    }
                }
                AwareLog.w(TAG, "sendPrereadMsg incorrect packageName");
            } catch (UnsupportedEncodingException e) {
                AwareLog.e(TAG, "UnsupportedEncodingException!");
            }
        }
    }

    private static void sendUnmapMsg(String pkgName) {
        if (pkgName != null && MEMORY_SWITCH.get() && !CONFIG_UPDATING.get()) {
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_ALLOCATE_THREE_ARGS_LENGTH);
            try {
                byte[] stringBytes = pkgName.getBytes("UTF-8");
                if (stringBytes.length >= 1) {
                    if (stringBytes.length <= BUFFER_LENGTH_MAX) {
                        buffer.clear();
                        buffer.putInt(MemoryConstant.MSG_UNMAP_FILE);
                        buffer.putInt(stringBytes.length);
                        buffer.put(stringBytes);
                        buffer.putChar(0);
                        if (MemoryUtils.sendPacket(buffer) == -1) {
                            AwareLog.w(TAG, "sendPrereadMsg sendPacket failed");
                            return;
                        }
                        return;
                    }
                }
                AwareLog.w(TAG, "sendPrereadMsg incorrect packageName");
            } catch (UnsupportedEncodingException e) {
                AwareLog.e(TAG, "UnsupportedEncodingException!");
            }
        }
    }

    private static void prereadDataRemove() {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.clear();
        buffer.putInt(MemoryConstant.MSG_PREREAD_DATA_REMOVE);
        if (MemoryUtils.sendPacket(buffer) == -1) {
            AwareLog.w(TAG, "prereadDataRemove sendPacket failed");
        }
    }

    private static boolean setPrereadPath(int opCode, String stringValue) {
        if (stringValue == null) {
            return false;
        }
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_ALLOCATE_FOUR_ARGS_LENGTH);
        try {
            byte[] stringValueBytes = stringValue.getBytes("UTF-8");
            if (stringValueBytes.length >= 1) {
                if (stringValueBytes.length <= BUFFER_LENGTH_MAX) {
                    String filename = new File(stringValue).getName();
                    if (AwareLog.getDebugLogSwitch()) {
                        AwareLog.d(TAG, "setPrereadPath :opCode " + opCode + ", " + filename);
                    }
                    buffer.clear();
                    buffer.putInt(MemoryConstant.MSG_SET_PREREAD_PATH);
                    buffer.putInt(opCode);
                    buffer.putInt(stringValueBytes.length);
                    buffer.put(stringValueBytes);
                    buffer.putChar(0);
                    if (MemoryUtils.sendPacket(buffer) != -1) {
                        return true;
                    }
                    AwareLog.w(TAG, "setPathPreread sendPacket failed");
                    return false;
                }
            }
            AwareLog.w(TAG, "setPrereadPath incorrect stringValueBytes");
            return false;
        } catch (UnsupportedEncodingException e) {
            AwareLog.e(TAG, "UnsupportedEncodingException!");
            return false;
        }
    }

    private static void preLaunchCamera(int id) {
        try {
            Object tmp = Class.forName("com.huawei.hwpostcamera.HwPostCamera").getMethod("preLaunch", Integer.TYPE).invoke(null, Integer.valueOf(id));
            boolean ret = false;
            if (tmp instanceof Boolean) {
                ret = ((Boolean) tmp).booleanValue();
            }
            if (!ret) {
                AwareLog.w(TAG, "preLaunchCamera Error!!");
            }
            AwareLog.d(TAG, "preLaunchCamera Success !!");
        } catch (InvocationTargetException e) {
            AwareLog.e(TAG, "InvocationTargetException: not set preLaunchCamera");
        } catch (ClassNotFoundException e2) {
            AwareLog.e(TAG, "ClassNotFoundException:");
        } catch (NoSuchMethodException e3) {
            AwareLog.e(TAG, "NoSuchMethodException:");
        } catch (IllegalAccessException e4) {
            AwareLog.e(TAG, "IllegalAccessException:");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void preLaunchCameraOnWakeUpScreen() {
        AwareLog.d(TAG, "preLaunchCameraOnWakeUpScreen preLaunchCamera");
        preLaunchCamera(1);
    }

    /* access modifiers changed from: private */
    public static void handleDevScreenOnForCamera() {
        if (SCREEN_STATUS.get()) {
            MemoryUtils.doExitSpecialSceneNotify();
            sendUnmapMsg(MemoryConstant.FACE_RECOGNIZE_CONFIGNAME);
            preLaunchCamera(1000);
        }
    }

    public void sendDevScreenForCameraMsg(Boolean isScreenOn) {
        if (isScreenOn.booleanValue()) {
            doDevScreenOnForCamera();
        } else {
            doDevScreenOffForCamera();
        }
    }

    private void doDevScreenOnForCamera() {
        if (checkCameraDeviceNeedPreload()) {
            SCREEN_STATUS.set(true);
            this.mPrereadHandler.removeMessages(2);
            Message msg = Message.obtain();
            msg.what = 2;
            this.mPrereadHandler.sendMessageDelayed(msg, DEV_SCREEN_ON_CAMERA_MSG_DELAY);
        }
    }

    /* access modifiers changed from: private */
    public static void handleDevScreenOffForCamera() {
        if (!SCREEN_STATUS.get()) {
            MemoryUtils.doEnterSpecialSceneNotify(MemoryConstant.getCameraPowerUpMemory(), 16746243, -1);
            sendPrereadMsg(MemoryConstant.FACE_RECOGNIZE_CONFIGNAME);
            preLaunchCamera(1001);
        }
    }

    private void doDevScreenOffForCamera() {
        if (checkCameraDeviceNeedPreload()) {
            SCREEN_STATUS.set(false);
            this.mPrereadHandler.removeMessages(3);
            Message msg = Message.obtain();
            msg.what = 3;
            this.mPrereadHandler.sendMessage(msg);
        }
    }

    public void sendPrereadDataUpdateMsg() {
        CONFIG_UPDATING.set(true);
        Message msg = Message.obtain();
        msg.what = 1;
        this.mPrereadHandler.sendMessageDelayed(msg, PREREAD_DATA_UPDATE_MSG_DELAY);
    }

    /* access modifiers changed from: private */
    public static void prereadDataUpdate() {
        CONFIG_UPDATING.set(true);
        if (!isPrereadOdex) {
            sPrereadPkgList.clear();
            sExcludedPkgSet.clear();
        }
        prereadDataRemove();
        Iterator<Map.Entry<String, ArrayList<String>>> it = MemoryConstant.getCameraPrereadFileMap().entrySet().iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            Map.Entry<String, ArrayList<String>> entry = it.next();
            List<String> filePathList = entry.getValue();
            if (filePathList == null) {
                AwareLog.w(TAG, "prereadDataUpdate: filePathList is null");
                break;
            } else if (setPrereadPath(SetPathType.SET_PACKAGE.ordinal(), entry.getKey())) {
                setPrereadPathDataUpdate(filePathList);
            }
        }
        CONFIG_UPDATING.set(false);
    }

    private static void setPrereadPathDataUpdate(List<String> filePathList) {
        for (String filePath : filePathList) {
            if (filePath != null) {
                setPrereadPath(SetPathType.SET_PATH.ordinal(), filePath);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public static final class PrereadHandler extends Handler {
        PrereadHandler() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg == null) {
                AwareLog.e(PrereadUtils.TAG, "msg == null");
                return;
            }
            int i = msg.what;
            if (i == 1) {
                PrereadUtils.prereadDataUpdate();
            } else if (i == 2) {
                PrereadUtils.handleDevScreenOnForCamera();
            } else if (i == 3) {
                PrereadUtils.handleDevScreenOffForCamera();
            } else if (i == 4) {
                PrereadUtils.getInstance().preLaunchCameraOnWakeUpScreen();
            }
            if (AwareLog.getDebugLogSwitch()) {
                AwareLog.d(PrereadUtils.TAG, "preread msg type:" + msg.what);
            }
        }
    }

    private boolean checkCameraDeviceNeedPreload() {
        if (MemoryConstant.getCameraPrereadFileMap().containsKey(MemoryConstant.FACE_RECOGNIZE_CONFIGNAME)) {
            return true;
        }
        AwareLog.w(TAG, "checkCameraDeviceNeedPreload:face regcognize not config");
        return false;
    }

    private boolean checkCameraPowerUpMemory() {
        int cameraPowerUpMemory = MemoryConstant.getCameraPowerUpMemory();
        if (cameraPowerUpMemory > 0 && cameraPowerUpMemory <= CAMERA_POWERUP_MEMORY_MAX) {
            return true;
        }
        AwareLog.e(TAG, "checkCameraPowerUpMemory illegal parameter cameraPowerUpMemory " + cameraPowerUpMemory);
        return false;
    }

    public void reportEvent(int eventId) {
        if (eventId == 20023) {
            sendDevScreenForCameraMsg(true);
        } else if (eventId == 20025) {
            this.mPrereadHandler.sendEmptyMessage(4);
        } else if (eventId == 90023) {
            sendDevScreenForCameraMsg(false);
        }
    }
}
