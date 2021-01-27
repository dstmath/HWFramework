package ohos.miscservices.pasteboard;

import android.content.Context;
import android.content.IClipboard;
import android.content.IOnPrimaryClipChangedListener;
import android.os.ServiceManager;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import libcore.io.IoUtils;
import ohos.aafwk.ability.DataAbilityHelper;
import ohos.aafwk.ability.DataAbilityRemoteException;
import ohos.app.dispatcher.TaskDispatcher;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.miscservices.pasteboard.PasteboardProxy;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.sysability.samgr.SysAbilityManager;
import ohos.system.Parameters;
import ohos.utils.net.Uri;
import ohos.utils.system.safwk.java.SystemAbilityDefinition;

public class PasteboardProxy implements IPasteboardSysAbility {
    private static final String CLIPBOARD_SERVICE = "clipboard";
    private static final String DESCRIPTOR = "ohos.ipc.IPasteboard";
    private static final String PASTEBOARD_DISTRIBUTE_OFF = "false";
    private static final String PASTEBOARD_DISTRIBUTE_ON = "true";
    private static final String REGEX_BRACKETS = "[ ]+";
    private static final String REGEX_HTML = "<[^>]+>";
    private static final String REGEX_SCRIPT = "<[\\s]*?script[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?script[\\s]*?>";
    private static final String REGEX_SIGNS = "(?m)^\\s*$(\\n|\\r\\n)";
    private static final String REGEX_STYLE = "<[\\s]*?style[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?style[\\s]*?>";
    private static final String SYS_PASTEBOARD_DISTRIBUTE = "persist.sys.pasteboard.distribute";
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "PasteboardProxy");
    private static final int URI_BUFFER_CAPACITY = 8192;
    private static final int URI_BUILDER_CAPACITY = 128;
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
        obtain.writeInterfaceToken(DESCRIPTOR);
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
        Parameters.set(SYS_PASTEBOARD_DISTRIBUTE, z ? "true" : "false");
    }

    @Override // ohos.miscservices.pasteboard.IPasteboardSysAbility
    public boolean querySysDistributedAttr() {
        return Parameters.get(SYS_PASTEBOARD_DISTRIBUTE).equals("true");
    }

    /* JADX WARNING: Removed duplicated region for block: B:16:0x003b A[SYNTHETIC, Splitter:B:16:0x003b] */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x0085  */
    static CharSequence uriToPlainText(ohos.app.Context context2, Uri uri) {
        InputStreamReader inputStreamReader;
        Throwable th;
        FileDescriptor fileDescriptor;
        InputStreamReader inputStreamReader2;
        FileInputStream fileInputStream;
        FileInputStream fileInputStream2 = null;
        try {
            fileDescriptor = DataAbilityHelper.creator(context2).openFile(uri, "r");
        } catch (IllegalArgumentException unused) {
            HiLog.warn(TAG, "Scheme is illegal, open file failed", new Object[0]);
        } catch (IllegalStateException unused2) {
            HiLog.warn(TAG, "No corresponding dataAbility, open file failed", new Object[0]);
        } catch (SecurityException unused3) {
            HiLog.warn(TAG, "Failure opening stream", new Object[0]);
        } catch (FileNotFoundException | DataAbilityRemoteException unused4) {
            HiLog.warn(TAG, "Unable to open content URI as text", new Object[0]);
        } catch (Throwable th2) {
            th = th2;
            inputStreamReader = null;
            IoUtils.closeQuietly(fileInputStream2);
            IoUtils.closeQuietly(inputStreamReader);
            throw th;
        }
        if (fileDescriptor == null) {
            try {
                fileInputStream = new FileInputStream(fileDescriptor);
            } catch (IOException unused5) {
                inputStreamReader2 = null;
                try {
                    HiLog.warn(TAG, "Failure loading text", new Object[0]);
                    IoUtils.closeQuietly(fileInputStream2);
                    IoUtils.closeQuietly(inputStreamReader2);
                    return "";
                } catch (Throwable th3) {
                    inputStreamReader = inputStreamReader2;
                    th = th3;
                }
            }
            try {
                inputStreamReader2 = new InputStreamReader(fileInputStream, "UTF-8");
                try {
                    StringBuilder sb = new StringBuilder(128);
                    char[] cArr = new char[8192];
                    while (true) {
                        int read = inputStreamReader2.read(cArr);
                        if (read > 0) {
                            sb.append(cArr, 0, read);
                        } else {
                            String sb2 = sb.toString();
                            IoUtils.closeQuietly(fileInputStream);
                            IoUtils.closeQuietly(inputStreamReader2);
                            return sb2;
                        }
                    }
                } catch (IOException unused6) {
                    fileInputStream2 = fileInputStream;
                    HiLog.warn(TAG, "Failure loading text", new Object[0]);
                    IoUtils.closeQuietly(fileInputStream2);
                    IoUtils.closeQuietly(inputStreamReader2);
                    return "";
                } catch (Throwable th4) {
                    inputStreamReader = inputStreamReader2;
                    th = th4;
                    fileInputStream2 = fileInputStream;
                    IoUtils.closeQuietly(fileInputStream2);
                    IoUtils.closeQuietly(inputStreamReader);
                    throw th;
                }
            } catch (IOException unused7) {
                inputStreamReader2 = null;
                fileInputStream2 = fileInputStream;
                HiLog.warn(TAG, "Failure loading text", new Object[0]);
                IoUtils.closeQuietly(fileInputStream2);
                IoUtils.closeQuietly(inputStreamReader2);
                return "";
            } catch (Throwable th5) {
                th = th5;
                fileInputStream2 = fileInputStream;
                inputStreamReader = null;
                IoUtils.closeQuietly(fileInputStream2);
                IoUtils.closeQuietly(inputStreamReader);
                throw th;
            }
        } else {
            inputStreamReader2 = null;
            IoUtils.closeQuietly(fileInputStream2);
            IoUtils.closeQuietly(inputStreamReader2);
            return "";
        }
        fileDescriptor = null;
        if (fileDescriptor == null) {
        }
        IoUtils.closeQuietly(fileInputStream2);
        IoUtils.closeQuietly(inputStreamReader2);
        return "";
    }

    static CharSequence htmlToPlainText(String str) {
        return Pattern.compile(REGEX_HTML, 2).matcher(Pattern.compile(REGEX_STYLE, 2).matcher(Pattern.compile(REGEX_SCRIPT, 2).matcher(str).replaceAll("")).replaceAll("")).replaceAll("").replaceAll(REGEX_BRACKETS, " ").replaceAll(REGEX_SIGNS, "");
    }
}
