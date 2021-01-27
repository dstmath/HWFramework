package ohos.aafwk.ability;

import java.io.PrintWriter;
import ohos.aafwk.utils.log.Log;
import ohos.aafwk.utils.log.LogLabel;
import ohos.app.AbilityContext;
import ohos.app.dispatcher.TaskDispatcher;
import ohos.bundle.ElementName;
import ohos.com.sun.org.apache.xpath.internal.compiler.PsuedoNames;
import ohos.rpc.IRemoteObject;

/* access modifiers changed from: package-private */
public class AbilityConnectionWrap implements IAbilityConnection {
    private static final LogLabel LABEL = LogLabel.create();
    private volatile boolean callerRunning = true;
    private final IAbilityConnection conn;
    private final ElementName elementName;
    private final TaskDispatcher taskDispatcher;

    AbilityConnectionWrap(AbilityContext abilityContext, IAbilityConnection iAbilityConnection, ElementName elementName2) {
        this.conn = iAbilityConnection;
        this.taskDispatcher = abilityContext.getUITaskDispatcher();
        this.elementName = new ElementName(elementName2);
    }

    /* access modifiers changed from: package-private */
    public void setCallerRunning(boolean z) {
        this.callerRunning = z;
    }

    /* access modifiers changed from: package-private */
    public IAbilityConnection getConn() {
        return this.conn;
    }

    @Override // ohos.aafwk.ability.IAbilityConnection
    public void onAbilityConnectDone(ElementName elementName2, IRemoteObject iRemoteObject, int i) {
        Log.info(LABEL, "start to notify connect done. element: %{public}s, resultCode: %{public}d", elementName2.getURI(), Integer.valueOf(i));
        if (this.callerRunning) {
            TaskDispatcher taskDispatcher2 = this.taskDispatcher;
            if (taskDispatcher2 != null) {
                taskDispatcher2.asyncDispatch(new Runnable(elementName2, i, iRemoteObject) {
                    /* class ohos.aafwk.ability.$$Lambda$AbilityConnectionWrap$AB6CmHVsbc_fDnfvocnugnVN9w */
                    private final /* synthetic */ ElementName f$1;
                    private final /* synthetic */ int f$2;
                    private final /* synthetic */ IRemoteObject f$3;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                        this.f$3 = r4;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        AbilityConnectionWrap.this.lambda$onAbilityConnectDone$0$AbilityConnectionWrap(this.f$1, this.f$2, this.f$3);
                    }
                });
                return;
            }
            throw new IllegalStateException("taskDispatcher is null, on Ability Connect Done failed.");
        } else if (Log.isDebuggable()) {
            Log.debug(LABEL, "Do not notify caller connect done when caller already stopped.", " element: %{public}s", elementName2.getURI());
        }
    }

    public /* synthetic */ void lambda$onAbilityConnectDone$0$AbilityConnectionWrap(ElementName elementName2, int i, IRemoteObject iRemoteObject) {
        if (Log.isDebuggable()) {
            Log.debug(LABEL, "start to notify connect done in async task. element: %{public}s, resultCode: %{public}d", elementName2.getURI(), Integer.valueOf(i));
        }
        if (i != 0) {
            Log.error(LABEL, "on ability connect done failed. element: %{public}s, resultCode: %{public}d, connInner:%{public}s", elementName2.getURI(), Integer.valueOf(i), this);
        }
        this.conn.onAbilityConnectDone(elementName2, iRemoteObject, i);
    }

    @Override // ohos.aafwk.ability.IAbilityConnection
    public void onAbilityDisconnectDone(ElementName elementName2, int i) {
        Log.info(LABEL, "start to notify disconnect done. element: %{public}s, resultCode: %{public}d", elementName2.getURI(), Integer.valueOf(i));
        if (this.callerRunning) {
            TaskDispatcher taskDispatcher2 = this.taskDispatcher;
            if (taskDispatcher2 != null) {
                taskDispatcher2.asyncDispatch(new Runnable(i, elementName2) {
                    /* class ohos.aafwk.ability.$$Lambda$AbilityConnectionWrap$SFkok1gbZp4T8UFzWTyDX1Gou9w */
                    private final /* synthetic */ int f$1;
                    private final /* synthetic */ ElementName f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        AbilityConnectionWrap.this.lambda$onAbilityDisconnectDone$1$AbilityConnectionWrap(this.f$1, this.f$2);
                    }
                });
                return;
            }
            throw new IllegalStateException("taskDispatcher is null, on Ability Disconnect Done failed.");
        } else if (Log.isDebuggable()) {
            Log.debug(LABEL, "Do not notify caller disconnect done when caller already stopped. element: %{public}s", elementName2.getURI());
        }
    }

    public /* synthetic */ void lambda$onAbilityDisconnectDone$1$AbilityConnectionWrap(int i, ElementName elementName2) {
        if (i != 0) {
            Log.error(LABEL, "on ability disconnect done failed. element: %{public}s, resultCode: %{public}d, connInner:%{public}s", elementName2.getURI(), Integer.valueOf(i), this);
        }
        this.conn.onAbilityDisconnectDone(elementName2, i);
    }

    /* access modifiers changed from: package-private */
    public void dump(String str, PrintWriter printWriter) {
        if (this.elementName == null) {
            printWriter.println(str + "none");
            return;
        }
        printWriter.println(str + this.elementName.getBundleName() + PsuedoNames.PSEUDONAME_ROOT + this.elementName.getAbilityName());
    }
}
