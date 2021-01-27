package com.android.server.rms.iaware.appmng;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.rms.iaware.AwareConfig;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CmpTypeInfo;
import android.rms.iaware.ComponentRecoManager;
import android.rms.iaware.IAwareCMSManager;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import com.android.server.mtm.taskstatus.ProcessInfo;
import com.android.server.mtm.taskstatus.ProcessInfoCollector;
import com.android.server.mtm.utils.InnerUtils;
import com.huawei.android.os.UserHandleEx;
import com.huawei.iaware.AwareServiceThread;
import com.huawei.internal.os.BackgroundThreadEx;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class AwareComponentPreloadManager {
    private static final int CLEAR_APP_DATA_MSG = 3;
    private static final String CONFIG_FEATURE_NAME = "ComponentPreload";
    private static final String CONFIG_SERVICE_PRELOAD = "ServicePreload";
    private static final int DEFAULT_VALUE = -1;
    private static final String DELAY_TIME_ITEM = "DelayTime";
    private static final int INIT_MSG = 0;
    private static final int INSERT_DB_MSG = 4;
    private static final Object LOCK = new Object();
    private static final int REPORT_SERVICE_START_MSG = 5;
    private static final int START_SERVICES_DELAY = 1500;
    private static final int START_SERVICES_MSG = 1;
    private static final String SUB_SWITCH_ON = "1";
    private static final String SWITCH_ITEM = "Switch";
    private static final String TAG = "AwareComponentPreloadManager";
    private static final int UNINSTALL_APP_MSG = 2;
    private static AwareComponentPreloadManager sInstance = null;
    private final Map<String, ArraySet<String>> mBadServices = new ArrayMap();
    private ComponentPreloadHandler mComponentPreloadHandler;
    private Context mContext;
    private boolean mEnable = false;
    private final ArrayMap<String, ArrayMap<String, Boolean>> mGoodServices = new ArrayMap<>();
    private String mPkg = "";
    private boolean mServiceEnable = false;
    private int mStartServiceDelay = START_SERVICES_DELAY;

    /* access modifiers changed from: private */
    public class ComponentPreloadHandler extends Handler {
        public ComponentPreloadHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg == null) {
                AwareLog.w(AwareComponentPreloadManager.TAG, "msg is null");
                return;
            }
            int i = msg.what;
            if (i == 0) {
                AwareComponentPreloadManager.this.handlerInitConfig();
                AwareComponentPreloadManager.this.handlerInitCompConfig();
            } else if (i == 1) {
                AwareComponentPreloadManager.this.handlerStartServicesMsg(msg);
            } else if (i == 2) {
                AwareComponentPreloadManager.this.handlerUninstallApp(msg);
            } else if (i == 3) {
                AwareComponentPreloadManager.this.handlerClearAppData(msg);
            } else if (i == 4) {
                AwareComponentPreloadManager.this.handlerInsertDb(msg);
            } else if (i == 5) {
                AwareComponentPreloadManager.this.handlerServiceStart(msg);
            }
        }
    }

    private AwareComponentPreloadManager() {
        initHandler();
    }

    public static AwareComponentPreloadManager getInstance() {
        AwareComponentPreloadManager awareComponentPreloadManager;
        synchronized (LOCK) {
            if (sInstance == null) {
                sInstance = new AwareComponentPreloadManager();
            }
            awareComponentPreloadManager = sInstance;
        }
        return awareComponentPreloadManager;
    }

    public void enable(Context context) {
        AwareLog.i(TAG, " enable");
        this.mEnable = true;
        this.mContext = context;
        initConfig();
    }

    public void disable() {
        AwareLog.i(TAG, " disable");
        this.mEnable = false;
        this.mServiceEnable = false;
    }

    private void initHandler() {
        Looper looper = AwareServiceThread.getInstance().getLooper();
        if (looper != null) {
            this.mComponentPreloadHandler = new ComponentPreloadHandler(looper);
            return;
        }
        Looper looper2 = BackgroundThreadEx.getLooper();
        if (looper2 != null) {
            this.mComponentPreloadHandler = new ComponentPreloadHandler(looper2);
        }
    }

    private boolean isBadService(String pkg, String cls) {
        if (TextUtils.isEmpty(pkg) || TextUtils.isEmpty(cls)) {
            return true;
        }
        synchronized (this.mBadServices) {
            ArraySet<String> clses = this.mBadServices.get(pkg);
            if (clses == null) {
                return false;
            }
            return clses.contains(cls);
        }
    }

    private void preloadServicesInner(String pkg, String cls) {
        if (!isBadService(pkg, cls)) {
            ComponentName com2 = new ComponentName(pkg, cls);
            Intent intent = new Intent();
            intent.setComponent(com2);
            boolean isGoodService = false;
            try {
                if (this.mContext.startService(intent) != null) {
                    isGoodService = true;
                }
            } catch (SecurityException e) {
                AwareLog.w(TAG, " SecurityException");
            } catch (IllegalStateException e2) {
                AwareLog.w(TAG, " IllegalStateException");
            }
            if (!isGoodService) {
                synchronized (this.mBadServices) {
                    ArraySet<String> clses = this.mBadServices.get(pkg);
                    if (clses == null) {
                        clses = new ArraySet<>();
                    }
                    clses.add(cls);
                    this.mBadServices.put(pkg, clses);
                }
            }
        }
    }

    private void preloadServices(String pkg, ArrayMap<String, Boolean> clsInfos) {
        if (this.mContext != null) {
            for (Map.Entry<String, Boolean> entry : clsInfos.entrySet()) {
                if (entry != null && entry.getValue().booleanValue()) {
                    preloadServicesInner(pkg, entry.getKey());
                }
            }
        }
    }

    private void sendStartServicesMsg(int pid) {
        if (this.mComponentPreloadHandler != null && this.mServiceEnable) {
            Message msg = Message.obtain();
            msg.arg1 = pid;
            msg.what = 1;
            this.mComponentPreloadHandler.sendMessageDelayed(msg, (long) this.mStartServiceDelay);
        }
    }

    private String getPkgByPid(int pid) {
        ProcessInfo processInfo = ProcessInfoCollector.getInstance().getProcessInfo(pid);
        if (processInfo == null || processInfo.mPackageName == null) {
            return "";
        }
        return (String) processInfo.mPackageName.get(0);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlerStartServicesMsg(Message msg) {
        String pkg = getPkgByPid(msg.arg1);
        if (pkg != null && !pkg.equals(this.mPkg)) {
            this.mPkg = pkg;
            ArrayMap<String, Boolean> clsInfosTmp = null;
            synchronized (this.mGoodServices) {
                ArrayMap<String, Boolean> clsInfos = this.mGoodServices.get(pkg);
                if (clsInfos != null) {
                    clsInfosTmp = new ArrayMap<>();
                    clsInfosTmp.putAll((ArrayMap<? extends String, ? extends Boolean>) clsInfos);
                }
            }
            if (clsInfosTmp != null) {
                preloadServices(pkg, clsInfosTmp);
            }
        }
    }

    public void updateFgActivityChange(int pid, int uid, boolean foregroundActivities) {
        if (this.mEnable && isMainUserApp(uid) && foregroundActivities) {
            sendStartServicesMsg(pid);
        }
    }

    private boolean isMainUserApp(int uid) {
        if (AwareAppAssociate.getInstance().getCurUserId() == 0 && UserHandleEx.getUserId(uid) == 0) {
            return true;
        }
        return false;
    }

    public void dumpInfo(PrintWriter pw) {
        if (!this.mEnable) {
            pw.println("iaware switch of PreloadResourceFeature is false!");
            return;
        }
        pw.println("service preload switch is : " + this.mServiceEnable);
        pw.println("service delay time : " + this.mStartServiceDelay);
        pw.println("[dump bad services]");
        synchronized (this.mBadServices) {
            pw.println(this.mBadServices);
        }
        synchronized (this.mGoodServices) {
            pw.println(this.mGoodServices);
        }
    }

    private void initConfig() {
        if (this.mComponentPreloadHandler != null) {
            Message msg = Message.obtain();
            msg.what = 0;
            this.mComponentPreloadHandler.sendMessage(msg);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x008c A[Catch:{ NumberFormatException -> 0x00b1, RemoteException -> 0x00aa }] */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x009a A[Catch:{ NumberFormatException -> 0x00b1, RemoteException -> 0x00aa }] */
    private void handlerInitConfig() {
        try {
            IBinder awareService = IAwareCMSManager.getICMSManager();
            if (awareService == null) {
                AwareLog.i(TAG, "can not find service awareService.");
                return;
            }
            AwareConfig configList = IAwareCMSManager.getConfig(awareService, CONFIG_FEATURE_NAME, CONFIG_SERVICE_PRELOAD);
            if (configList == null) {
                AwareLog.i(TAG, "configList is null.");
                return;
            }
            List<AwareConfig.Item> itemList = configList.getConfigList();
            if (itemList == null) {
                AwareLog.i(TAG, "itemList is null.");
                return;
            }
            for (AwareConfig.Item item : itemList) {
                if (item != null) {
                    if (item.getSubItemList() != null) {
                        for (AwareConfig.SubItem subItem : item.getSubItemList()) {
                            String itemName = subItem.getName();
                            String itemValue = subItem.getValue();
                            if (itemName != null) {
                                if (itemValue != null) {
                                    char c = 65535;
                                    int hashCode = itemName.hashCode();
                                    if (hashCode != -1805606060) {
                                        if (hashCode == 1534863056 && itemName.equals(DELAY_TIME_ITEM)) {
                                            c = 1;
                                            if (c == 0) {
                                                this.mServiceEnable = SUB_SWITCH_ON.equals(itemValue.trim());
                                            } else if (c == 1) {
                                                this.mStartServiceDelay = Integer.parseInt(itemValue.trim());
                                            }
                                        }
                                    } else if (itemName.equals(SWITCH_ITEM)) {
                                        c = 0;
                                        if (c == 0) {
                                        }
                                    }
                                    if (c == 0) {
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "parse config error!");
        } catch (RemoteException e2) {
            AwareLog.e(TAG, "getConfig RemoteException");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlerInitCompConfig() {
        ArrayMap<String, ArraySet<String>> services = ComponentRecoManager.getInstance().getGoodServices();
        if (services != null) {
            List<CmpTypeInfo> list = loadGoodServicesFromDb();
            for (Map.Entry<String, ArraySet<String>> entry : services.entrySet()) {
                updateServicesFromComp(entry.getKey(), entry.getValue(), list);
            }
        }
    }

    private void updateServicesFromComp(String pkg, ArraySet<String> clses, List<CmpTypeInfo> list) {
        if (!(pkg == null || clses == null)) {
            ArraySet<String> cmps = new ArraySet<>();
            for (CmpTypeInfo cmpInfo : list) {
                if (cmpInfo != null && cmpInfo.getType() == 5 && pkg.equals(cmpInfo.getPkgName()) && cmpInfo.getCls() != null) {
                    cmps.add(cmpInfo.getCls());
                }
            }
            synchronized (this.mGoodServices) {
                ArrayMap<String, Boolean> clsInfos = new ArrayMap<>();
                Iterator<String> it = clses.iterator();
                while (it.hasNext()) {
                    String cls = it.next();
                    if (cls != null) {
                        clsInfos.put(cls, Boolean.valueOf(cmps.contains(cls)));
                    }
                }
                if (!clsInfos.isEmpty()) {
                    this.mGoodServices.put(pkg, clsInfos);
                }
            }
        }
    }

    private List<CmpTypeInfo> loadGoodServicesFromDb() {
        List<CmpTypeInfo> list = null;
        try {
            IBinder awareService = IAwareCMSManager.getICMSManager();
            if (awareService != null) {
                list = IAwareCMSManager.getCmpTypeList(awareService);
            } else {
                AwareLog.e(TAG, "can not find service IAwareCMSService.");
            }
        } catch (RemoteException e) {
            AwareLog.e(TAG, "loadGoodServices RemoteException");
        }
        return list == null ? new ArrayList() : list;
    }

    public void reportUninstallApp(String pkg, int userId) {
        if (this.mEnable && this.mServiceEnable && pkg != null && this.mComponentPreloadHandler != null && userId == 0) {
            Message msg = Message.obtain();
            msg.obj = pkg;
            msg.what = 2;
            this.mComponentPreloadHandler.sendMessage(msg);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlerUninstallApp(Message msg) {
        if (msg != null && (msg.obj instanceof String)) {
            String pkg = (String) msg.obj;
            synchronized (this.mBadServices) {
                this.mBadServices.remove(pkg);
            }
            clearData(pkg);
        }
    }

    public void reportPkgClearData(Bundle bundle) {
        int uid;
        if (bundle != null && this.mEnable && this.mServiceEnable && (uid = bundle.getInt("android.intent.extra.UID", -1)) != -1 && isMainUserApp(uid) && this.mComponentPreloadHandler != null) {
            Message msg = Message.obtain();
            msg.arg1 = uid;
            msg.what = 3;
            this.mComponentPreloadHandler.sendMessage(msg);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlerClearAppData(Message msg) {
        String pkg;
        if (msg != null && (pkg = InnerUtils.getPackageNameByUid(msg.arg1)) != null) {
            clearData(pkg);
        }
    }

    private void clearData(String pkg) {
        synchronized (this.mGoodServices) {
            ArrayMap<String, Boolean> clsInfos = this.mGoodServices.get(pkg);
            if (clsInfos != null) {
                for (Map.Entry<String, Boolean> entry : clsInfos.entrySet()) {
                    if (!(entry == null || entry.getKey() == null)) {
                        clsInfos.put(entry.getKey(), false);
                        CmpTypeInfo info = new CmpTypeInfo();
                        info.setPkgName(pkg);
                        info.setCls(entry.getKey());
                        info.setType(5);
                        updateServicesToDb(info, false);
                    }
                }
                this.mGoodServices.put(pkg, clsInfos);
            }
        }
    }

    private void updateServicesToDb(CmpTypeInfo info, boolean isAdd) {
        try {
            IBinder awareService = IAwareCMSManager.getICMSManager();
            if (awareService == null) {
                AwareLog.e(TAG, "can not find service IAwareCMSService.");
            } else if (isAdd) {
                IAwareCMSManager.insertCmpTypeInfo(awareService, info);
            } else {
                IAwareCMSManager.deleteCmpTypeInfo(awareService, info);
            }
        } catch (RemoteException e) {
            AwareLog.e(TAG, "deleteCmpRecgInfo RemoteException");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlerInsertDb(Message msg) {
        if (msg != null && (msg.obj instanceof CmpTypeInfo)) {
            updateServicesToDb((CmpTypeInfo) msg.obj, true);
        }
    }

    public void reportServiceStart(String calledPkg, String cls, String callerPkg, int uid) {
        if (this.mEnable && this.mServiceEnable && calledPkg != null && cls != null && calledPkg.equals(callerPkg) && isMainUserApp(uid) && this.mComponentPreloadHandler != null) {
            ComponentName componentName = new ComponentName(calledPkg, cls);
            Message msg = Message.obtain();
            msg.obj = componentName;
            msg.what = 5;
            this.mComponentPreloadHandler.sendMessage(msg);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlerServiceStart(Message message) {
        if (message != null) {
            ComponentName componentName = null;
            if (message.obj instanceof ComponentName) {
                componentName = (ComponentName) message.obj;
            }
            if (componentName != null) {
                String calledPkg = componentName.getPackageName();
                String cls = componentName.getClassName();
                if (calledPkg != null && cls != null) {
                    synchronized (this.mGoodServices) {
                        ArrayMap<String, Boolean> clsInfos = this.mGoodServices.get(calledPkg);
                        if (clsInfos != null) {
                            Boolean canStart = clsInfos.get(cls);
                            if (canStart != null) {
                                if (!canStart.booleanValue()) {
                                    clsInfos.put(cls, true);
                                    this.mGoodServices.put(calledPkg, clsInfos);
                                    if (this.mComponentPreloadHandler != null) {
                                        CmpTypeInfo info = new CmpTypeInfo();
                                        info.setPkgName(calledPkg);
                                        info.setCls(cls);
                                        info.setType(5);
                                        Message msg = Message.obtain();
                                        msg.obj = info;
                                        msg.what = 4;
                                        this.mComponentPreloadHandler.sendMessage(msg);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
