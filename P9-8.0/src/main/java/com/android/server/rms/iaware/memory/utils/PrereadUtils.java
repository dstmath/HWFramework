package com.android.server.rms.iaware.memory.utils;

import android.app.ActivityManagerNative;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build.VERSION;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.UserHandle;
import android.rms.iaware.AwareLog;
import com.android.server.emcom.daemon.CommandsInterface;
import com.android.server.pm.InstructionSets;
import dalvik.system.DexFile;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class PrereadUtils {
    private static final int DATA_UPDATE_MSG = 1;
    private static final String DEX_SUFFIX = ".dex";
    private static final int MAX_DIR_LOOP = 5;
    private static final int MAX_PREREAD_PKG_NUM = 200;
    private static final int MSG_MEM_PREREAD_ID = 4662;
    private static final int PREREAD_FILE_NUM = 2;
    private static final int RECV_BYTE_BUFFER_LENTH = 8;
    private static final int SWITCH_ON = 1;
    private static final String TAG = "AwareMem_PrereadUtils";
    private static final String VDEX_SUFFIX = ".vdex";
    private static final AtomicBoolean mConfigUpdating = new AtomicBoolean(true);
    private static Context mContext;
    private static Set<String> mExcludedPkgSet = new HashSet();
    private static final AtomicBoolean mMemorySwitch = new AtomicBoolean(false);
    private static Boolean mPrereadOdex = Boolean.valueOf(false);
    private static List<String> mPrereadPkgList = new ArrayList();
    private static PrereadUtils mPrereadUtils;
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
        boolean z = true;
        if (VERSION.SDK_INT < 26) {
            mPrereadOdex = Boolean.valueOf(false);
            return;
        }
        if (1 != prereadSwitch) {
            z = false;
        }
        mPrereadOdex = Boolean.valueOf(z);
    }

    /* JADX WARNING: Missing block: B:4:0x0008, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:11:0x0023, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean addPkgFilesIfNecessary(String pkgName) {
        if (pkgName == null || mContext == null || !mMemorySwitch.get() || mConfigUpdating.get() || (mPrereadOdex.booleanValue() ^ 1) != 0 || mExcludedPkgSet.contains(pkgName)) {
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
            removePackageFiles((String) mPrereadPkgList.remove(0));
        }
        if (setPrereadPath(SetPathType.SET_PACKAGE.ordinal(), pkgName)) {
            for (String filePath : fileList) {
                if (filePath != null) {
                    setPrereadPath(SetPathType.SET_PATH.ordinal(), filePath);
                }
            }
            mPrereadPkgList.add(pkgName);
            return true;
        }
        mExcludedPkgSet.add(pkgName);
        return false;
    }

    private static List<String> getDexfilesFromPkg(String pkgName) {
        List<String> fileList = new ArrayList();
        try {
            Context context = mContext.createPackageContextAsUser(pkgName, 1152, new UserHandle(ActivityManagerNative.getDefault().getCurrentUser().id));
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
        } catch (NameNotFoundException e) {
            AwareLog.w(TAG, "getDexfilesFromPkg NameNotFoundException.");
        } catch (RemoteException e2) {
            AwareLog.w(TAG, "getDexfilesFromPkg remoteException.");
        }
    }

    private static List<String> getDexFileStatus(Context context) {
        String[] instructionSets = InstructionSets.getAppDexInstructionSets(context.getApplicationInfo());
        List<String> fileList = new ArrayList();
        if (instructionSets == null) {
            return fileList;
        }
        String[] dexCodeInstructionSets = InstructionSets.getDexCodeInstructionSets(instructionSets);
        if (dexCodeInstructionSets == null) {
            return fileList;
        }
        ArrayList<String> paths = new ArrayList();
        if (context.getApplicationInfo().sourceDir != null) {
            paths.add(context.getApplicationInfo().sourceDir);
        }
        String[] splitSourceDirs = context.getApplicationInfo().splitSourceDirs;
        if (splitSourceDirs != null) {
            for (int i = 0; i < splitSourceDirs.length; i++) {
                if (splitSourceDirs[i] != null) {
                    paths.add(splitSourceDirs[i]);
                }
            }
        }
        try {
            for (String instructionSet : dexCodeInstructionSets) {
                if (instructionSet != null) {
                    for (String dir : paths) {
                        if (dir.lastIndexOf(File.separator) > 0) {
                            String status = DexFile.getDexFileStatus(dir, instructionSet);
                            if (status != null && status.lastIndexOf(DEX_SUFFIX) > 0) {
                                String odexPath = status.substring(0, status.lastIndexOf(DEX_SUFFIX) + DEX_SUFFIX.length());
                                fileList.add(odexPath);
                                fileList.add(odexPath.substring(0, odexPath.lastIndexOf(".")) + VDEX_SUFFIX);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            AwareLog.w(TAG, "preread getDexFileStatus IOException ");
        }
        if (fileList.size() != 2) {
            fileList.clear();
        }
        return fileList;
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
            ByteBuffer buffer = ByteBuffer.allocate(CommandsInterface.EMCOM_SD_XENGINE_STOP_MPIP);
            try {
                byte[] stringBytes = pkgName.getBytes("UTF-8");
                if (stringBytes.length < 1 || stringBytes.length > 255) {
                    AwareLog.w(TAG, "sendPrereadMsg incorrect packageName");
                    return;
                }
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
            if (stringValueBytes.length < 1 || stringValueBytes.length > 255) {
                AwareLog.w(TAG, "setPrereadPath incorrect stringValueBytes");
                return false;
            }
            AwareLog.d(TAG, "setPrereadPath :opCode " + opCode + ", " + new File(stringValue).getName());
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
        } catch (UnsupportedEncodingException e) {
            AwareLog.w(TAG, "UnsupportedEncodingException!");
            return false;
        }
    }

    public void sendPrereadDataUpdateMsg() {
        mConfigUpdating.set(true);
        Message msg = Message.obtain();
        msg.what = 1;
        this.mPrereadHandler.sendMessageDelayed(msg, 20000);
    }

    private static void prereadDataUpdate() {
        mConfigUpdating.set(true);
        if (!mPrereadOdex.booleanValue()) {
            mPrereadPkgList.clear();
            mExcludedPkgSet.clear();
        }
        prereadDataRemove();
        for (Entry<String, ArrayList<String>> entry : MemoryConstant.getCameraPrereadFileMap().entrySet()) {
            List<String> filePathList = (List) entry.getValue();
            if (filePathList == null) {
                AwareLog.w(TAG, "prereadDataUpdate: filePathList is null");
                break;
            } else if (setPrereadPath(SetPathType.SET_PACKAGE.ordinal(), (String) entry.getKey())) {
                for (String filePath : filePathList) {
                    if (filePath != null) {
                        setPrereadPath(SetPathType.SET_PATH.ordinal(), filePath);
                    }
                }
            }
        }
        mConfigUpdating.set(false);
    }
}
