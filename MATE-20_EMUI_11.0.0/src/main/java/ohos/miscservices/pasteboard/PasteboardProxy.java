package ohos.miscservices.pasteboard;

import android.content.Context;
import android.content.IClipboard;
import android.content.IOnPrimaryClipChangedListener;
import android.os.ServiceManager;
import android.os.SystemProperties;
import java.util.ArrayList;
import java.util.List;
import ohos.app.dispatcher.TaskDispatcher;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.miscservices.pasteboard.PasteboardProxy;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.sysability.samgr.SysAbilityManager;
import ohos.utils.system.safwk.java.SystemAbilityDefinition;

public class PasteboardProxy implements IPasteboardSysAbility {
    private static final String CLIPBOARD_SERVICE = "clipboard";
    private static final String PASTEBOARD_DISTRIBUTE_OFF = "false";
    private static final String PASTEBOARD_DISTRIBUTE_ON = "true";
    private static final String SYS_PASTEBOARD_DISTRIBUTE = "persist.sys.pasteboard.distribute";
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "PasteboardProxy");
    private Context aContext;
    private IOnPrimaryClipChangedListener.Stub adapterListener = new IOnPrimaryClipChangedListener.Stub() {
        /* class ohos.miscservices.pasteboard.PasteboardProxy.AnonymousClass1 */

        public void dispatchPrimaryClipChanged() {
            if (PasteboardProxy.this.context == null) {
                HiLog.error(PasteboardProxy.TAG, "Context is not ready!", new Object[0]);
                return;
            }
            TaskDispatcher mainTaskDispatcher = PasteboardProxy.this.context.getMainTaskDispatcher();
            if (mainTaskDispatcher == null) {
                HiLog.error(PasteboardProxy.TAG, "Context dispatcher is not ready!", new Object[0]);
            } else {
                mainTaskDispatcher.asyncDispatch(new Runnable() {
                    /* class ohos.miscservices.pasteboard.$$Lambda$PasteboardProxy$1$hDe2PLbDcg30AbuoA2ZhRsAlE */

                    @Override // java.lang.Runnable
                    public final void run() {
                        PasteboardProxy.AnonymousClass1.this.lambda$dispatchPrimaryClipChanged$0$PasteboardProxy$1();
                    }
                });
            }
        }

        public /* synthetic */ void lambda$dispatchPrimaryClipChanged$0$PasteboardProxy$1() {
            synchronized (PasteboardProxy.this.mChangedListeners) {
                if (!PasteboardProxy.this.mChangedListeners.isEmpty()) {
                    HiLog.info(PasteboardProxy.TAG, "SystemPasteboard data changed, notify to %{public}d listeners", Integer.valueOf(PasteboardProxy.this.mChangedListeners.size()));
                    for (IPasteDataChangedListener iPasteDataChangedListener : PasteboardProxy.this.mChangedListeners) {
                        iPasteDataChangedListener.onChanged();
                    }
                }
            }
        }
    };
    private ohos.app.Context context;
    private final List<IPasteDataChangedListener> mChangedListeners = new ArrayList();
    private IRemoteObject remote;
    private IClipboard service;

    PasteboardProxy(IRemoteObject iRemoteObject, ohos.app.Context context2) {
        this.context = context2;
        this.remote = iRemoteObject;
        this.aContext = (Context) context2.getHostContext();
        HiLog.info(TAG, "Trying to get pasteboard service proxy", new Object[0]);
        this.service = IClipboard.Stub.asInterface(ServiceManager.getService(CLIPBOARD_SERVICE));
    }

    private void tryInit() throws RemoteException {
        if (this.service == null) {
            HiLog.info(TAG, "Trying to get pasteboard service proxy", new Object[0]);
            this.service = IClipboard.Stub.asInterface(ServiceManager.getService(CLIPBOARD_SERVICE));
            if (this.service == null) {
                HiLog.error(TAG, "Can not get pasteboard service proxy!", new Object[0]);
                throw new RemoteException();
            }
        }
    }

    @Override // ohos.miscservices.pasteboard.IPasteboardSysAbility
    public PasteData getPasteData() throws RemoteException {
        tryInit();
        try {
            return PasteboardUtils.convertFromClipData(this.service.getPrimaryClip(this.aContext.getOpPackageName(), this.aContext.getUserId()));
        } catch (android.os.RemoteException unused) {
            throw new RemoteException();
        }
    }

    @Override // ohos.miscservices.pasteboard.IPasteboardSysAbility
    public void setPasteData(PasteData pasteData) throws RemoteException {
        tryInit();
        try {
            this.service.setPrimaryClip(PasteboardUtils.convertFromPasteData(pasteData), this.aContext.getOpPackageName(), this.aContext.getUserId());
            if (querySysDistributedAttr() && !pasteData.getProperty().isLocalOnly()) {
                distributePasteData(pasteData);
            }
        } catch (android.os.RemoteException unused) {
            throw new RemoteException();
        }
    }

    public void distributePasteData(PasteData pasteData) throws RemoteException {
        if (this.remote == null) {
            HiLog.info(TAG, "distributePasteData: remote is null", new Object[0]);
            this.remote = SysAbilityManager.getSysAbility(SystemAbilityDefinition.PASTEBOARD_SERVICE_ID);
        }
        if (this.remote == null) {
            HiLog.error(TAG, "distributePasteData: can not get remote proxy", new Object[0]);
            return;
        }
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        obtain.writeString(this.aContext.getOpPackageName());
        obtain.writeInt(this.aContext.getUserId());
        pasteData.marshalling(obtain);
        this.remote.sendRequest(3, obtain, obtain2, messageOption);
        obtain2.reclaim();
        obtain.reclaim();
    }

    @Override // ohos.miscservices.pasteboard.IPasteboardSysAbility
    public boolean hasPasteData() throws RemoteException {
        tryInit();
        try {
            return this.service.hasPrimaryClip(this.aContext.getOpPackageName(), this.aContext.getUserId());
        } catch (android.os.RemoteException unused) {
            throw new RemoteException();
        }
    }

    @Override // ohos.miscservices.pasteboard.IPasteboardSysAbility
    public void clear() throws RemoteException {
        tryInit();
        try {
            this.service.clearPrimaryClip(this.aContext.getOpPackageName(), this.aContext.getUserId());
        } catch (android.os.RemoteException unused) {
            throw new RemoteException();
        }
    }

    @Override // ohos.miscservices.pasteboard.IPasteboardSysAbility
    public void addPasteDataChangedListener(IPasteDataChangedListener iPasteDataChangedListener) throws RemoteException {
        synchronized (this.mChangedListeners) {
            if (this.mChangedListeners.isEmpty()) {
                registerAdapterListener();
            }
            if (this.mChangedListeners.contains(iPasteDataChangedListener)) {
                HiLog.info(TAG, "duplicate listener", new Object[0]);
                return;
            }
            this.mChangedListeners.add(iPasteDataChangedListener);
            HiLog.info(TAG, "%{public}s successfully add pastedata listener", this.aContext.getOpPackageName());
            HiLog.info(TAG, "listener sum: %{public}d", Integer.valueOf(this.mChangedListeners.size()));
        }
    }

    @Override // ohos.miscservices.pasteboard.IPasteboardSysAbility
    public void removePasteDataChangedListener(IPasteDataChangedListener iPasteDataChangedListener) throws RemoteException {
        synchronized (this.mChangedListeners) {
            if (this.mChangedListeners.contains(iPasteDataChangedListener)) {
                this.mChangedListeners.remove(iPasteDataChangedListener);
                HiLog.info(TAG, "%{public}s successfully remove listener", this.aContext.getOpPackageName());
                HiLog.info(TAG, "listener sum: %{public}d", Integer.valueOf(this.mChangedListeners.size()));
            }
            if (this.mChangedListeners.isEmpty()) {
                removeAdapterListener();
            }
        }
    }

    private void registerAdapterListener() throws RemoteException {
        try {
            this.service.addPrimaryClipChangedListener(this.adapterListener, this.aContext.getOpPackageName(), this.aContext.getUserId());
        } catch (android.os.RemoteException unused) {
            throw new RemoteException();
        }
    }

    private void removeAdapterListener() throws RemoteException {
        try {
            this.service.removePrimaryClipChangedListener(this.adapterListener, this.aContext.getOpPackageName(), this.aContext.getUserId());
        } catch (android.os.RemoteException unused) {
            throw new RemoteException();
        }
    }

    @Override // ohos.rpc.IRemoteBroker
    public IRemoteObject asObject() {
        return this.remote;
    }

    @Override // ohos.miscservices.pasteboard.IPasteboardSysAbility
    public void setSysDistributedAttr(boolean z) {
        SystemProperties.set(SYS_PASTEBOARD_DISTRIBUTE, z ? "true" : "false");
    }

    @Override // ohos.miscservices.pasteboard.IPasteboardSysAbility
    public boolean querySysDistributedAttr() {
        return SystemProperties.get(SYS_PASTEBOARD_DISTRIBUTE).equals("true");
    }
}
