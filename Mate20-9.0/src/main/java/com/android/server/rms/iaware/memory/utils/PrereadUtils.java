package com.android.server.rms.iaware.memory.utils;

import android.app.ActivityManagerNative;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.UserHandle;
import android.rms.iaware.AwareLog;
import com.android.server.pm.Installer;
import com.android.server.pm.InstructionSets;
import com.android.server.pm.PackageManagerService;
import com.android.systemui.shared.recents.hwutil.HwRecentsTaskUtils;
import dalvik.system.DexFile;
import java.io.File;
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
    private static final int BUFFER_ALLOCATE_LENGTH = 264;
    private static final int BUFFER_LENGTH_MAX = 255;
    private static final int BUFFER_LENGTH_MIN = 1;
    private static final int CAMERA_POWERUP_MEMORY_MAX = 256000;
    private static final int CAMERA_POWERUP_MEMORY_MIN = 0;
    private static final int DATA_UPDATE_MSG = 1;
    private static final int DEV_SCREEN_OFF_PROCESS_CAMERA_MSG = 3;
    private static final int DEV_SCREEN_ON_PROCESS_CAMERA_MSG = 2;
    private static final String DEX_SUFFIX = ".dex";
    private static final int MAX_DIR_LOOP = 5;
    private static final int MAX_PREREAD_PKG_NUM = 200;
    private static final int MSG_MEM_PREREAD_ID = 4662;
    private static final int PRELAUNCH_FRONT = 1;
    private static final int PRELAUNCH_SCREENOFF = 1001;
    private static final int PRELAUNCH_SCREENON = 1000;
    private static final int PREREAD_FILE_NUM = 2;
    private static final int RECV_BYTE_BUFFER_LENTH = 8;
    private static final int SWITCH_ON = 1;
    private static final String TAG = "AwareMem_PrereadUtils";
    private static final String VDEX_SUFFIX = ".vdex";
    private static final int WAKE_UP_SCREEN_MSG = 4;
    private static final AtomicBoolean mConfigUpdating = new AtomicBoolean(true);
    private static Context mContext;
    private static Set<String> mExcludedPkgSet = new HashSet();
    private static Installer mInstaller;
    private static final AtomicBoolean mMemorySwitch = new AtomicBoolean(false);
    private static Boolean mPrereadOdex = false;
    private static List<String> mPrereadPkgList = new ArrayList();
    private static PrereadUtils mPrereadUtils;
    private static volatile int screenStatus = 1;
    private PrereadHandler mPrereadHandler = new PrereadHandler();

    static final class PrereadHandler extends Handler {
        PrereadHandler() {
        }

        public void handleMessage(Message msg) {
            if (msg == null) {
                AwareLog.e(PrereadUtils.TAG, "null == msg");
                return;
            }
            switch (msg.what) {
                case 1:
                    AwareLog.i(PrereadUtils.TAG, "DATA_UPDATE_MSG");
                    PrereadUtils.prereadDataUpdate();
                    break;
                case 2:
                    AwareLog.i(PrereadUtils.TAG, "DEV_SCREEN_ON_PROCESS_CAMERA_MSG");
                    PrereadUtils.handleDevScreenOnForCamera();
                    break;
                case 3:
                    AwareLog.i(PrereadUtils.TAG, "DEV_SCREEN_OFF_PROCESS_CAMERA_MSG");
                    PrereadUtils.handleDevScreenOffForCamera();
                    break;
                case 4:
                    AwareLog.i(PrereadUtils.TAG, "WAKE_UP_SCREEN_MSG");
                    PrereadUtils.getInstance().preLaunchCameraOnWakeUpScreen();
                    break;
            }
        }
    }

    private enum SetPathType {
        REMOVE_PATH,
        SET_PACKAGE,
        SET_PATH
    }

    private PrereadUtils() {
    }

    public static void setContext(Context context) {
        mContext = context;
        if (PackageManagerService.sIsMygote) {
            mInstaller = new Installer(mContext);
            mInstaller.onStart();
        }
    }

    public static PrereadUtils getInstance() {
        PrereadUtils prereadUtils;
        synchronized (PrereadUtils.class) {
            if (mPrereadUtils == null) {
                mPrereadUtils = new PrereadUtils();
            }
            prereadUtils = mPrereadUtils;
        }
        return prereadUtils;
    }

    public static void start() {
        mMemorySwitch.set(true);
    }

    public static void stop() {
        mPrereadPkgList.clear();
        mExcludedPkgSet.clear();
        mMemorySwitch.set(false);
    }

    public static void setPrereadOdexSwitch(int prereadSwitch) {
        if (Build.VERSION.SDK_INT < 26) {
            mPrereadOdex = false;
            return;
        }
        boolean z = true;
        if (1 != prereadSwitch) {
            z = false;
        }
        mPrereadOdex = Boolean.valueOf(z);
    }

    public static boolean addPkgFilesIfNecessary(String pkgName) {
        if (pkgName == null || mContext == null || !mMemorySwitch.get() || mConfigUpdating.get() || !mPrereadOdex.booleanValue() || mExcludedPkgSet.contains(pkgName)) {
            return false;
        }
        if (mPrereadPkgList.contains(pkgName)) {
            return true;
        }
        List<String> fileList = getDexfilesFromPkg(pkgName);
        if (fileList.isEmpty() || fileList.size() > 2) {
            mExcludedPkgSet.add(pkgName);
            return false;
        }
        if (mPrereadPkgList.size() >= 200) {
            removePackageFiles(mPrereadPkgList.remove(0));
        }
        if (!setPrereadPath(SetPathType.SET_PACKAGE.ordinal(), pkgName)) {
            mExcludedPkgSet.add(pkgName);
            return false;
        }
        for (String filePath : fileList) {
            if (filePath != null) {
                setPrereadPath(SetPathType.SET_PATH.ordinal(), filePath);
            }
        }
        mPrereadPkgList.add(pkgName);
        return true;
    }

    private static List<String> getDexfilesFromPkg(String pkgName) {
        List<String> fileList = new ArrayList<>();
        try {
            UserInfo currentUser = ActivityManagerNative.getDefault().getCurrentUser();
            if (currentUser == null) {
                return fileList;
            }
            Context context = mContext.createPackageContextAsUser(pkgName, 1152, new UserHandle(currentUser.id));
            if (context.getApplicationInfo() != null && (context.getApplicationInfo().hwFlags & 16777216) != 0) {
                return fileList;
            }
            String sourceDir = context.getApplicationInfo().sourceDir;
            if (sourceDir == null) {
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
            fileList = getDexFileStatus(context);
            return fileList;
        } catch (PackageManager.NameNotFoundException e) {
            AwareLog.w(TAG, "getDexfilesFromPkg NameNotFoundException.");
        } catch (RemoteException e2) {
            AwareLog.w(TAG, "getDexfilesFromPkg remoteException.");
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:56:0x00f7  */
    private static List<String> getDexFileStatus(Context context) {
        String[] instructionSets;
        ApplicationInfo info;
        String[] instructionSets2;
        ApplicationInfo info2;
        ApplicationInfo info3 = context.getApplicationInfo();
        String[] instructionSets3 = InstructionSets.getAppDexInstructionSets(info3);
        List<String> fileList = new ArrayList<>();
        if (instructionSets3 == null) {
            return fileList;
        }
        String[] dexCodeInstructionSets = InstructionSets.getDexCodeInstructionSets(instructionSets3);
        if (dexCodeInstructionSets == null) {
            return fileList;
        }
        ArrayList arrayList = new ArrayList();
        if (info3.sourceDir != null) {
            arrayList.add(info3.sourceDir);
        }
        String[] splitSourceDirs = info3.splitSourceDirs;
        if (splitSourceDirs != null) {
            for (int i = 0; i < splitSourceDirs.length; i++) {
                if (splitSourceDirs[i] != null) {
                    arrayList.add(splitSourceDirs[i]);
                }
            }
        }
        if (PackageManagerService.sIsMygote != 0) {
            fileList = getDexFileStatusForMaple(arrayList, dexCodeInstructionSets, info3.uid);
            ApplicationInfo applicationInfo = info3;
            String[] strArr = instructionSets3;
        } else {
            try {
                int pathSize = arrayList.size();
                int length = dexCodeInstructionSets.length;
                int i2 = 0;
                while (i2 < length) {
                    String instructionSet = dexCodeInstructionSets[i2];
                    if (instructionSet == null) {
                        info = info3;
                        instructionSets = instructionSets3;
                    } else {
                        int i3 = 0;
                        while (i3 < pathSize) {
                            String dir = (String) arrayList.get(i3);
                            if (dir.lastIndexOf(File.separator) > 0) {
                                String status = DexFile.getDexFileStatus(dir, instructionSet);
                                if (status != null) {
                                    if (status.lastIndexOf(DEX_SUFFIX) <= 0) {
                                        info2 = info3;
                                        instructionSets2 = instructionSets3;
                                    } else {
                                        String odexPath = status.substring(0, status.lastIndexOf(DEX_SUFFIX) + DEX_SUFFIX.length());
                                        fileList.add(odexPath);
                                        StringBuilder sb = new StringBuilder();
                                        info2 = info3;
                                        try {
                                            instructionSets2 = instructionSets3;
                                            try {
                                                sb.append(odexPath.substring(0, odexPath.lastIndexOf(".")));
                                                sb.append(VDEX_SUFFIX);
                                                fileList.add(sb.toString());
                                            } catch (IOException e) {
                                            }
                                        } catch (IOException e2) {
                                            String[] strArr2 = instructionSets3;
                                            AwareLog.w(TAG, "preread getDexFileStatus IOException ");
                                            if (fileList.size() != 2) {
                                            }
                                            return fileList;
                                        }
                                    }
                                    i3++;
                                    info3 = info2;
                                    instructionSets3 = instructionSets2;
                                }
                            }
                            info2 = info3;
                            instructionSets2 = instructionSets3;
                            i3++;
                            info3 = info2;
                            instructionSets3 = instructionSets2;
                        }
                        info = info3;
                        instructionSets = instructionSets3;
                    }
                    i2++;
                    info3 = info;
                    instructionSets3 = instructionSets;
                }
                String[] strArr3 = instructionSets3;
            } catch (IOException e3) {
                ApplicationInfo applicationInfo2 = info3;
                String[] strArr4 = instructionSets3;
                AwareLog.w(TAG, "preread getDexFileStatus IOException ");
                if (fileList.size() != 2) {
                }
                return fileList;
            }
        }
        if (fileList.size() != 2) {
            fileList.clear();
        }
        return fileList;
    }

    private static List<String> getDexFileStatusForMaple(List<String> paths, String[] dexCodeInstructionSets, int uid) {
        String[] strArr = dexCodeInstructionSets;
        List<String> fileList = new ArrayList<>();
        if (mInstaller == null) {
            return fileList;
        }
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        for (String instructionSet : strArr) {
            for (String dir : paths) {
                if (!(instructionSet == null || dir == null)) {
                    arrayList.add(dir);
                    arrayList2.add(instructionSet);
                }
            }
        }
        if (arrayList.size() <= 0) {
            return fileList;
        }
        try {
            int size = arrayList.size();
            String[] dexPaths = new String[size];
            String[] isas = new String[size];
            int[] uids = new int[size];
            for (int i = 0; i < size; i++) {
                dexPaths[i] = (String) arrayList.get(i);
                isas[i] = (String) arrayList2.get(i);
                uids[i] = uid;
            }
            String[] retDexStatus = mInstaller.getDexFileStatus(dexPaths, isas, uids);
            if (retDexStatus != null) {
                if (retDexStatus.length == size) {
                    for (int i2 = 0; i2 < size; i2++) {
                        String status = retDexStatus[i2];
                        if (status != null) {
                            if (status.lastIndexOf(DEX_SUFFIX) > 0) {
                                fileList.add(status.substring(0, status.lastIndexOf(DEX_SUFFIX) + DEX_SUFFIX.length()));
                                fileList.add(odexPath.substring(0, odexPath.lastIndexOf(".")) + VDEX_SUFFIX);
                            }
                        }
                    }
                    return fileList;
                }
            }
            return fileList;
        } catch (Installer.InstallerException e) {
            AwareLog.w(TAG, "preread getDexFileStatus Installer.InstallerException!");
        }
    }

    public static void removePackageFiles(String pkgName) {
        if (pkgName != null) {
            setPrereadPath(SetPathType.REMOVE_PATH.ordinal(), pkgName);
            mPrereadPkgList.remove(pkgName);
            mExcludedPkgSet.remove(pkgName);
        }
    }

    private static void traverseFolder(String path, List<String> fileList, int loop) {
        if (loop <= 5 && path != null && fileList != null) {
            File file = new File(path);
            if (file.exists()) {
                File[] files = file.listFiles();
                if (files != null && files.length != 0) {
                    for (File subfile : files) {
                        if (subfile != null) {
                            try {
                                String subfilePath = subfile.getCanonicalPath();
                                if (subfile.isDirectory()) {
                                    traverseFolder(subfilePath, fileList, loop + 1);
                                } else if (filterFiles(subfilePath) && !fileList.contains(subfilePath)) {
                                    fileList.add(subfilePath);
                                }
                            } catch (IOException e) {
                                AwareLog.w(TAG, "traverseFolder: getCanonicalPath IOException!");
                                return;
                            }
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
        int func_Id = recvMsgParcel.readInt();
        int status = recvMsgParcel.readInt();
        if (func_Id == MSG_MEM_PREREAD_ID) {
            ret = status;
        }
        recvMsgParcel.recycle();
        return ret;
    }

    public static void sendPrereadMsg(String pkgName) {
        if (pkgName != null && mMemorySwitch.get() && !mConfigUpdating.get()) {
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_ALLOCATE_LENGTH);
            try {
                byte[] stringBytes = pkgName.getBytes("UTF-8");
                if (stringBytes.length >= 1) {
                    if (stringBytes.length <= 255) {
                        AwareLog.i(TAG, "sendPrereadMsg pkgname = " + pkgName);
                        buffer.clear();
                        buffer.putInt(MemoryConstant.MSG_PREREAD_FILE);
                        buffer.putInt(stringBytes.length);
                        buffer.put(stringBytes);
                        buffer.putChar(0);
                        if (-1 == MemoryUtils.sendPacket(buffer)) {
                            AwareLog.w(TAG, "sendPrereadMsg sendPacket failed");
                            return;
                        }
                        if (parseRecvPacket(8) == 0) {
                            mPrereadPkgList.clear();
                            prereadDataUpdate();
                        }
                        return;
                    }
                }
                AwareLog.w(TAG, "sendPrereadMsg incorrect packageName");
            } catch (UnsupportedEncodingException e) {
                AwareLog.w(TAG, "UnsupportedEncodingException!");
            }
        }
    }

    public static void sendUnmapMsg(String pkgName) {
        if (pkgName != null && mMemorySwitch.get() && !mConfigUpdating.get()) {
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_ALLOCATE_LENGTH);
            try {
                byte[] stringBytes = pkgName.getBytes("UTF-8");
                if (stringBytes.length >= 1) {
                    if (stringBytes.length <= 255) {
                        buffer.clear();
                        buffer.putInt(MemoryConstant.MSG_UNMAP_FILE);
                        buffer.putInt(stringBytes.length);
                        buffer.put(stringBytes);
                        buffer.putChar(0);
                        if (-1 == MemoryUtils.sendPacket(buffer)) {
                            AwareLog.w(TAG, "sendPrereadMsg sendPacket failed");
                        }
                        return;
                    }
                }
                AwareLog.w(TAG, "sendPrereadMsg incorrect packageName");
            } catch (UnsupportedEncodingException e) {
                AwareLog.w(TAG, "UnsupportedEncodingException!");
            }
        }
    }

    private static void prereadDataRemove() {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.clear();
        buffer.putInt(MemoryConstant.MSG_PREREAD_DATA_REMOVE);
        if (-1 == MemoryUtils.sendPacket(buffer)) {
            AwareLog.w(TAG, "prereadDataRemove sendPacket failed");
        }
    }

    private static boolean setPrereadPath(int opCode, String stringValue) {
        if (stringValue == null) {
            return false;
        }
        ByteBuffer buffer = ByteBuffer.allocate(268);
        try {
            byte[] stringValueBytes = stringValue.getBytes("UTF-8");
            if (stringValueBytes.length >= 1) {
                if (stringValueBytes.length <= 255) {
                    String filename = new File(stringValue).getName();
                    AwareLog.d(TAG, "setPrereadPath :opCode " + opCode + ", " + filename);
                    buffer.clear();
                    buffer.putInt(MemoryConstant.MSG_SET_PREREAD_PATH);
                    buffer.putInt(opCode);
                    buffer.putInt(stringValueBytes.length);
                    buffer.put(stringValueBytes);
                    buffer.putChar(0);
                    if (-1 != MemoryUtils.sendPacket(buffer)) {
                        return true;
                    }
                    AwareLog.w(TAG, "setPathPreread sendPacket failed");
                    return false;
                }
            }
            AwareLog.w(TAG, "setPrereadPath incorrect stringValueBytes");
            return false;
        } catch (UnsupportedEncodingException e) {
            AwareLog.w(TAG, "UnsupportedEncodingException!");
            return false;
        }
    }

    private static void preLaunchCamera(int id) {
        try {
            if (!((Boolean) Class.forName("com.huawei.hwpostcamera.HwPostCamera").getMethod("preLaunch", new Class[]{Integer.TYPE}).invoke(null, new Object[]{Integer.valueOf(id)})).booleanValue()) {
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
    public void preLaunchCameraOnWakeUpScreen() {
        AwareLog.d(TAG, "preLaunchCameraOnWakeUpScreen preLaunchCamera");
        preLaunchCamera(1);
    }

    /* access modifiers changed from: private */
    public static void handleDevScreenOnForCamera() {
        if (screenStatus == 1) {
            MemoryUtils.exitSpecialSceneNotify();
            sendUnmapMsg(MemoryConstant.FACE_RECOGNIZE_CONFIGNAME);
            preLaunchCamera(1000);
        }
    }

    public void sendDevScreenForCameraMsg(Boolean screenOn) {
        if (screenOn.booleanValue()) {
            doDevScrrenOnForCamera();
        } else {
            doDevScrrenOffForCamera();
        }
    }

    private void doDevScrrenOnForCamera() {
        if (checkCameraDeviceNeedPreload().booleanValue()) {
            screenStatus = 1;
            this.mPrereadHandler.removeMessages(2);
            Message msg = Message.obtain();
            msg.what = 2;
            this.mPrereadHandler.sendMessageDelayed(msg, 2000);
        }
    }

    /* access modifiers changed from: private */
    public static void handleDevScreenOffForCamera() {
        if (screenStatus == 0) {
            MemoryUtils.enterSpecialSceneNotify(MemoryConstant.getCameraPowerUPMemory(), 16746243, -1);
            sendPrereadMsg(MemoryConstant.FACE_RECOGNIZE_CONFIGNAME);
            preLaunchCamera(1001);
        }
    }

    private void doDevScrrenOffForCamera() {
        if (checkCameraDeviceNeedPreload().booleanValue()) {
            screenStatus = 0;
            this.mPrereadHandler.removeMessages(3);
            Message msg = Message.obtain();
            msg.what = 3;
            this.mPrereadHandler.sendMessage(msg);
        }
    }

    public void sendPrereadDataUpdateMsg() {
        mConfigUpdating.set(true);
        Message msg = Message.obtain();
        msg.what = 1;
        this.mPrereadHandler.sendMessageDelayed(msg, HwRecentsTaskUtils.MAX_REMOVE_TASK_TIME);
    }

    /* access modifiers changed from: private */
    public static void prereadDataUpdate() {
        mConfigUpdating.set(true);
        if (!mPrereadOdex.booleanValue()) {
            mPrereadPkgList.clear();
            mExcludedPkgSet.clear();
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
                for (String filePath : filePathList) {
                    if (filePath != null) {
                        setPrereadPath(SetPathType.SET_PATH.ordinal(), filePath);
                    }
                }
            }
        }
        mConfigUpdating.set(false);
    }

    private Boolean checkCameraDeviceNeedPreload() {
        if (MemoryConstant.getCameraPrereadFileMap().containsKey(MemoryConstant.FACE_RECOGNIZE_CONFIGNAME)) {
            return Boolean.valueOf(checkCameraPowerUPMemory());
        }
        AwareLog.w(TAG, "checkCameraDeviceNeedPreload:face regcognize switch is off");
        return false;
    }

    private boolean checkCameraPowerUPMemory() {
        int cameraPowerUpMemory = MemoryConstant.getCameraPowerUPMemory();
        if (cameraPowerUpMemory > 0 && cameraPowerUpMemory <= CAMERA_POWERUP_MEMORY_MAX) {
            return true;
        }
        AwareLog.e(TAG, "checkCameraPowerUPMemory illegal parameter cameraPowerUpMemory " + cameraPowerUpMemory);
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
