package com.android.server.rog;

import android.app.ActivityManagerInternal;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Process;
import android.os.RemoteException;
import android.rog.AppRogInfo;
import android.rog.IHwRogListener;
import android.rog.IHwRogManager.Stub;
import android.rog.IRogManager;
import android.text.TextUtils;
import android.util.Slog;
import com.android.server.LocalServices;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class HwRogManagerService extends Stub implements DeathRecipient, IParseCallback {
    private static final String TAG = "HwRogManagerService";
    private ActivityManagerInternal mAmInternal;
    private Context mContext;
    private HashMap<IBinder, IHwRogListener> mListeners;
    private HashMap<IBinder, String> mListenersNameMap;
    private PackageManager mPM;
    private ArrayList<IHwRogListener> mPendingSwitchListeners;
    private IRogPolicy mPolicy;
    private HwRogInfosCollector mRogInfosCollector;

    private final class LocalService implements IRogManager {
        private LocalService() {
        }

        public boolean registerRogListener(IHwRogListener listener) {
            return false;
        }

        public void unRegisterRogListener(IHwRogListener listener) {
        }

        public void setRogSwitchState(boolean open) {
        }

        public boolean getRogSwitchState() {
            return HwRogManagerService.this.mRogInfosCollector.getRogSwitchState(HwRogManagerService.this.mContext);
        }

        public AppRogInfo getOwnAppRogInfo(IHwRogListener listener) {
            return null;
        }

        public AppRogInfo getSpecifiedAppRogInfo(String packageName) {
            return HwRogManagerService.this.getSpecifiedAppRogInfoInternal(packageName);
        }

        public List<AppRogInfo> getAppRogInfos() {
            return null;
        }

        public List<AppRogInfo> updateBatchAppRogInfo(List<AppRogInfo> list) {
            return null;
        }

        public AppRogInfo updateAppRogInfo(AppRogInfo newRogInfo) {
            return null;
        }

        public boolean isRogSupported() {
            return true;
        }
    }

    public HwRogManagerService(Context context) {
        this.mContext = context;
    }

    public void systemReady() {
        this.mListeners = new HashMap();
        this.mListenersNameMap = new HashMap();
        this.mPM = this.mContext.getPackageManager();
        this.mPolicy = new HwRogPolicy(this.mContext);
        this.mPolicy.calRogAppScale();
        this.mRogInfosCollector = new HwRogInfosCollector(this.mContext, this);
        this.mRogInfosCollector.systemReady(this.mContext, this.mPolicy.getAppRogScale());
        LocalServices.addService(IRogManager.class, new LocalService());
        Slog.i(TAG, "systemReady");
    }

    public boolean registerRogListener(IHwRogListener listener, String pkgName) throws RemoteException {
        if (listener == null) {
            Slog.w(TAG, "registerRogListener->listener is null");
            return false;
        }
        boolean rogSwitchState = this.mRogInfosCollector.getRogSwitchState(this.mContext);
        if (isOurObserver(listener)) {
            Slog.w(TAG, "registerRogListener->listener is already in list");
            return rogSwitchState;
        } else if (!checkUid(pkgName)) {
            return rogSwitchState;
        } else {
            listener.asBinder().linkToDeath(this, 0);
            IBinder client = listener.asBinder();
            this.mListeners.put(client, listener);
            this.mListenersNameMap.put(client, pkgName);
            Slog.i(TAG, "registerRogListener->pkgName:" + pkgName);
            return rogSwitchState;
        }
    }

    public void unRegisterRogListener(IHwRogListener listener) throws RemoteException {
        if (listener == null) {
            Slog.w(TAG, "unRegisterRogListener->listener is null or not in the list");
            return;
        }
        IBinder client = listener.asBinder();
        this.mListeners.remove(client);
        this.mListenersNameMap.remove(client);
    }

    public void setRogSwitchState(boolean open) throws RemoteException {
        if (!checkCallingPermission("com.huawei.rog.permission.UPDATE_ROG_INFO", "setRogSwitchState")) {
            return;
        }
        if (open == this.mRogInfosCollector.getRogSwitchState(this.mContext)) {
            Slog.i(TAG, "setRogSwitchState->current state is already:" + (open ? "open" : "off"));
            return;
        }
        this.mRogInfosCollector.setRogSwitchState(this.mContext, open);
        notifyRogSwitchStateChange(open);
    }

    public boolean getRogSwitchState() throws RemoteException {
        return this.mRogInfosCollector.getRogSwitchState(this.mContext);
    }

    public AppRogInfo getOwnAppRogInfo(IHwRogListener listener) throws RemoteException {
        if (listener == null) {
            Slog.w(TAG, "getOwnAppRogInfo->listener is null");
            return null;
        }
        String pkgName = getListenerPackageName(listener);
        if (!isOurObserver(listener)) {
            Slog.i(TAG, "getOwnAppRogInfo->this is not our observer" + pkgName);
            return null;
        } else if (this.mRogInfosCollector.isParseFinished()) {
            return this.mPolicy.getAppOwnInfo(this.mRogInfosCollector, pkgName);
        } else {
            if (this.mPendingSwitchListeners == null) {
                this.mPendingSwitchListeners = new ArrayList();
            }
            this.mPendingSwitchListeners.add(listener);
            Slog.i(TAG, "getOwnAppRogInfo->config has not been parsed finished");
            return null;
        }
    }

    public AppRogInfo getSpecifiedAppRogInfo(String packageName) throws RemoteException {
        if (checkCallingPermission("com.huawei.rog.permission.UPDATE_ROG_INFO", "getSpecifiedAppRogInfo")) {
            return getSpecifiedAppRogInfoInternal(packageName);
        }
        return null;
    }

    public List<AppRogInfo> getAppRogInfos() throws RemoteException {
        if (checkCallingPermission("com.huawei.rog.permission.UPDATE_ROG_INFO", "getAppRogInfos")) {
            return this.mRogInfosCollector.getAllAppRogInfos();
        }
        return null;
    }

    public List<AppRogInfo> updateBatchAppRogInfo(List<AppRogInfo> newRogInfos) throws RemoteException {
        if (checkCallingPermission("com.huawei.rog.permission.UPDATE_ROG_INFO", "updateBatchAppRogInfo") && (newRogInfos instanceof ArrayList)) {
            return this.mRogInfosCollector.updateBatchAppRogInfos(this.mContext, (ArrayList) newRogInfos);
        }
        return null;
    }

    public AppRogInfo updateAppRogInfo(AppRogInfo newRogInfo) throws RemoteException {
        if (!checkCallingPermission("com.huawei.rog.permission.UPDATE_ROG_INFO", "updateAppRogInfo")) {
            return null;
        }
        if (newRogInfo != null) {
            return this.mRogInfosCollector.updateAppRogInfo(this.mContext, newRogInfo);
        }
        Slog.w(TAG, "updateAppRogInfo->new info is null");
        return null;
    }

    public void binderDied() {
        ArrayList<IBinder> pengdingRemove = new ArrayList();
        for (Entry<IBinder, IHwRogListener> entry : this.mListeners.entrySet()) {
            IHwRogListener listener = (IHwRogListener) entry.getValue();
            IBinder client = listener.asBinder();
            if (!client.isBinderAlive()) {
                Slog.i(TAG, "binderDied->listener is dead, remove it from the listener list:" + getListenerPackageName(listener));
                client.unlinkToDeath(this, 0);
                pengdingRemove.add(client);
            }
        }
        for (IBinder client2 : pengdingRemove) {
            this.mListeners.remove(client2);
            this.mListenersNameMap.remove(client2);
        }
    }

    private AppRogInfo getSpecifiedAppRogInfoInternal(String packageName) {
        if (!TextUtils.isEmpty(packageName)) {
            return this.mRogInfosCollector.getAppRogInfo(packageName);
        }
        Slog.w(TAG, "getSpecifiedAppRogInfo->packageName is empty");
        return null;
    }

    private String getListenerPackageName(IHwRogListener listener) {
        return (String) this.mListenersNameMap.get(listener.asBinder());
    }

    private boolean checkCallingPermission(String permission, String func) {
        if (Binder.getCallingPid() == Process.myPid() || this.mContext.checkCallingPermission(permission) == 0) {
            return true;
        }
        Slog.w(TAG, "Permission Denial: " + func + " from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " requires " + permission);
        return false;
    }

    private boolean checkUid(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            Slog.w(TAG, "checkUid->package name is empty");
            return false;
        }
        String[] packages = this.mPM.getPackagesForUid(Binder.getCallingUid());
        for (String equalsIgnoreCase : packages) {
            if (packageName.equalsIgnoreCase(equalsIgnoreCase)) {
                return true;
            }
        }
        Slog.w(TAG, "checkUid->package name is invalid");
        return false;
    }

    private void notifyRogSwitchStateChange(boolean state) {
        checkAm();
        Slog.i(TAG, "notifyRogSwitchStateChange->started");
        for (Entry<IBinder, IHwRogListener> entry : this.mListeners.entrySet()) {
            IHwRogListener listener = (IHwRogListener) entry.getValue();
            try {
                this.mAmInternal.notifyRogSwitchStateChanged(listener, state, this.mPolicy.getAppOwnInfo(this.mRogInfosCollector, listener.getPackageName()));
            } catch (RemoteException e) {
                Slog.w(TAG, "notifyRogSwitchStateChange->remote exception:" + e);
            }
        }
        Slog.i(TAG, "notifyRogSwitchStateChange->finished");
    }

    private void checkAm() {
        if (this.mAmInternal == null) {
            this.mAmInternal = (ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class);
        }
    }

    public void onParseFinished() {
        if (this.mPendingSwitchListeners == null || this.mPendingSwitchListeners.size() == 0) {
            Slog.i(TAG, "onParseFinished-> parse finished but need to do nothing");
            return;
        }
        for (IHwRogListener listener : this.mPendingSwitchListeners) {
            String packageName = getListenerPackageName(listener);
            if (TextUtils.isEmpty(packageName)) {
                Slog.i(TAG, "onParseFinished->package name is empty:");
            } else if (this.mPolicy.getAppOwnInfo(this.mRogInfosCollector, packageName) == null) {
                Slog.i(TAG, "onParseFinished-> rog info for package is empty:" + packageName);
            } else {
                Slog.w(TAG, "onParseFinished->pkg need to notify when parse finished:" + packageName);
                notifyRogInfoUpdated(listener, this.mPolicy.getAppOwnInfo(this.mRogInfosCollector, packageName));
            }
        }
    }

    private void notifyRogInfoUpdated(IHwRogListener listener, AppRogInfo rogInfo) {
        checkAm();
        try {
            this.mAmInternal.notifyRogInfoUpdated(listener, rogInfo);
        } catch (RemoteException e) {
            Slog.w(TAG, "notifyRogInfoUpdated->remote exception:" + e);
        }
    }

    private boolean isOurObserver(IHwRogListener listener) {
        return this.mListeners.containsKey(listener.asBinder());
    }
}
