package ohos.miscservices.pasteboard;

import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.RemoteException;
import ohos.sysability.samgr.SysAbilityManager;
import ohos.utils.system.safwk.java.SystemAbilityDefinition;

public class SystemPasteboard {
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "SystemPasteboard");
    private static final Object instanceLock = new Object();
    private static volatile SystemPasteboard sInstance;
    private Context abilityContext;
    private IPasteboardSysAbility pasteboardProxy;
    private String pkgName = "";

    private SystemPasteboard(Context context) {
        this.abilityContext = context.getApplicationContext();
        if (this.abilityContext.getApplicationInfo() != null) {
            this.pkgName = this.abilityContext.getApplicationInfo().getName();
        }
        this.pasteboardProxy = new PasteboardProxy(SysAbilityManager.getSysAbility(SystemAbilityDefinition.PASTEBOARD_SERVICE_ID), this.abilityContext);
        HiLog.info(TAG, "SystemPasteboard constructed", new Object[0]);
    }

    public static SystemPasteboard getSystemPasteboard(Context context) {
        if (context == null) {
            return null;
        }
        if (sInstance == null) {
            synchronized (instanceLock) {
                if (sInstance == null) {
                    sInstance = new SystemPasteboard(context);
                }
            }
        }
        return sInstance;
    }

    public PasteData getPasteData() {
        try {
            PasteData pasteData = this.pasteboardProxy.getPasteData();
            HiLog.info(TAG, "%{public}s read Paste Data succeeds", this.pkgName);
            return pasteData;
        } catch (RemoteException unused) {
            HiLog.error(TAG, "%{public}s get PasteData failed due to IPC error!", this.pkgName);
            return null;
        }
    }

    public void setPasteData(PasteData pasteData) {
        if (pasteData != null) {
            try {
                this.pasteboardProxy.setPasteData(pasteData);
                HiLog.info(TAG, "%{public}s set Paste Data succeeds", this.pkgName);
            } catch (RemoteException unused) {
                HiLog.error(TAG, "%{public}s set PasteData failed due to IPC error!", this.pkgName);
            }
        } else {
            throw new NullPointerException();
        }
    }

    public boolean hasPasteData() {
        try {
            return this.pasteboardProxy.hasPasteData();
        } catch (RemoteException unused) {
            HiLog.error(TAG, "%{public}s check if Pasteboard has data failed due to IPC error!", this.pkgName);
            return false;
        }
    }

    public void clear() {
        try {
            this.pasteboardProxy.clear();
            HiLog.info(TAG, "%{public}s clear pasteboard succeeds", this.pkgName);
        } catch (RemoteException unused) {
            HiLog.error(TAG, "%{public}s clear pasteboard failed due to IPC error!", this.pkgName);
        }
    }

    public void addPasteDataChangedListener(IPasteDataChangedListener iPasteDataChangedListener) {
        if (iPasteDataChangedListener == null) {
            HiLog.info(TAG, "Invaild listener object", new Object[0]);
            return;
        }
        try {
            this.pasteboardProxy.addPasteDataChangedListener(iPasteDataChangedListener);
        } catch (RemoteException unused) {
            HiLog.error(TAG, "%{public}s add pasteboard listener failed due to IPC error!", this.pkgName);
        }
    }

    public void removePasteDataChangedListener(IPasteDataChangedListener iPasteDataChangedListener) {
        if (iPasteDataChangedListener == null) {
            HiLog.info(TAG, "Invaild listener object", new Object[0]);
            return;
        }
        try {
            this.pasteboardProxy.removePasteDataChangedListener(iPasteDataChangedListener);
        } catch (RemoteException unused) {
            HiLog.error(TAG, "%{public}s remove pasteboard listener failed due to IPC error!", this.pkgName);
        }
    }
}
