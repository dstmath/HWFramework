package ohos.miscservices.pasteboard;

import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.sysability.samgr.SysAbilityManager;
import ohos.utils.system.safwk.java.SystemAbilityDefinition;

public class PasteboardDistributeSettings {
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "PasteboardDistributeSettings");
    private static final Object instanceLock = new Object();
    private static volatile PasteboardDistributeSettings sInstance;
    private Context abilityContext;
    private IPasteboardSysAbility pasteboardProxy = new PasteboardProxy(SysAbilityManager.getSysAbility(SystemAbilityDefinition.PASTEBOARD_SERVICE_ID), this.abilityContext);

    private PasteboardDistributeSettings(Context context) {
        this.abilityContext = context;
        HiLog.info(TAG, "PasteboardDistributeSettings constructed", new Object[0]);
    }

    public static PasteboardDistributeSettings getPasteboardDistributeSettings(Context context) {
        if (context == null) {
            return null;
        }
        if (sInstance == null) {
            synchronized (instanceLock) {
                if (sInstance == null) {
                    sInstance = new PasteboardDistributeSettings(context);
                }
            }
        }
        return sInstance;
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0012: APUT  (r0v1 java.lang.Object[]), (0 ??[int, short, byte, char]), (r3v1 java.lang.String) */
    public void setSysDistributedAttr(boolean z) {
        this.pasteboardProxy.setSysDistributedAttr(z);
        HiLogLabel hiLogLabel = TAG;
        Object[] objArr = new Object[1];
        objArr[0] = z ? "on" : "off";
        HiLog.info(hiLogLabel, "Pasteboard Distributed Setting turn %{public}s", objArr);
    }

    public boolean querySysDistributedAttr() {
        return this.pasteboardProxy.querySysDistributedAttr();
    }
}
