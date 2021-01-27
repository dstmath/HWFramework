package ohos.abilityshell;

import android.os.Handler;
import ohos.aafwk.ability.IAbilityConnection;
import ohos.appexecfwk.utils.AppLog;
import ohos.bundle.ElementName;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;

public class DistributedConnection extends RemoteObject implements IRemoteBroker {
    private static final String DESCRIPTOR = "harmonyos.abilityshell.DistributedConnection";
    private static final int ON_ABILITY_CONNECT_DONE = 1;
    private static final int ON_ABILITY_DISCONNECT_DONE = 2;
    private static final HiLogLabel SHELL_LABEL = new HiLogLabel(3, 218108160, "AbilityShell");
    private IAbilityConnection abilityConnection;
    private Handler mainHandler;

    public IRemoteObject asObject() {
        return this;
    }

    public DistributedConnection(IAbilityConnection iAbilityConnection, Handler handler) {
        super("");
        this.abilityConnection = iAbilityConnection;
        this.mainHandler = handler;
    }

    public void onAbilityConnectDone(ElementName elementName, IRemoteObject iRemoteObject, int i) {
        if (elementName == null || iRemoteObject == null) {
            AppLog.e(SHELL_LABEL, "DistributedConnection::onAbilityConnectDone param invalid", new Object[0]);
        } else {
            this.abilityConnection.onAbilityConnectDone(elementName, iRemoteObject, i);
        }
    }

    public void onAbilityDisconnectDone(ElementName elementName, int i) {
        if (elementName == null) {
            AppLog.e(SHELL_LABEL, "DistributedConnection::onAbilityDisconnectDone param invalid", new Object[0]);
        } else {
            this.abilityConnection.onAbilityDisconnectDone(elementName, i);
        }
    }

    public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
        if (messageParcel == null || messageParcel2 == null) {
            AppLog.e(SHELL_LABEL, "DistributedConnection::onRemoteRequest param invalid", new Object[0]);
            return false;
        }
        String readInterfaceToken = messageParcel.readInterfaceToken();
        if (!readInterfaceToken.equals(DESCRIPTOR)) {
            AppLog.e("DistributedConnection::onRemoteRequest error interfaceToken:%{private}s", readInterfaceToken);
            return false;
        }
        if (i == 1) {
            AppLog.d(SHELL_LABEL, "DistributedConnection::ON_ABILITY_CONNECT_DONE receive", new Object[0]);
            ElementName elementName = new ElementName();
            if (!messageParcel.readSequenceable(elementName)) {
                return false;
            }
            this.mainHandler.post(new Runnable(elementName, messageParcel.readRemoteObject(), messageParcel.readInt()) {
                /* class ohos.abilityshell.$$Lambda$DistributedConnection$890KWZWmBwjC4QLrc0tjQIWQI */
                private final /* synthetic */ ElementName f$1;
                private final /* synthetic */ IRemoteObject f$2;
                private final /* synthetic */ int f$3;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    DistributedConnection.this.lambda$onRemoteRequest$0$DistributedConnection(this.f$1, this.f$2, this.f$3);
                }
            });
        } else if (i != 2) {
            AppLog.w(SHELL_LABEL, "DistributedConnection::OnRemoteRequest unknown code", new Object[0]);
            return DistributedConnection.super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
        } else {
            AppLog.d(SHELL_LABEL, "DistributedConnection::ON_ABILITY_DISCONNECT_DONE receive", new Object[0]);
            ElementName elementName2 = new ElementName();
            if (!messageParcel.readSequenceable(elementName2)) {
                return false;
            }
            this.mainHandler.post(new Runnable(elementName2, messageParcel.readInt()) {
                /* class ohos.abilityshell.$$Lambda$DistributedConnection$xWxAGjAt3QCnDTMFmSutNrNilQ */
                private final /* synthetic */ ElementName f$1;
                private final /* synthetic */ int f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    DistributedConnection.this.lambda$onRemoteRequest$1$DistributedConnection(this.f$1, this.f$2);
                }
            });
        }
        return true;
    }

    public /* synthetic */ void lambda$onRemoteRequest$0$DistributedConnection(ElementName elementName, IRemoteObject iRemoteObject, int i) {
        AppLog.i(SHELL_LABEL, "DistributedConnection::ON_ABILITY_CONNECT_DONE post to MainThread", new Object[0]);
        onAbilityConnectDone(elementName, iRemoteObject, i);
    }

    public /* synthetic */ void lambda$onRemoteRequest$1$DistributedConnection(ElementName elementName, int i) {
        AppLog.i(SHELL_LABEL, "DistributedConnection::ON_ABILITY_DISCONNECT_DONE post to MainThread", new Object[0]);
        onAbilityDisconnectDone(elementName, i);
    }
}
