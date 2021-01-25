package ohos.nfc.cardemulation;

import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.IntentParams;
import ohos.eventhandler.EventHandler;
import ohos.eventhandler.EventRunner;
import ohos.eventhandler.InnerEvent;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.nfc.NfcKitsUtils;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteObject;

public abstract class HostService extends Ability {
    public static final int ERR_DESELECTED = 1;
    public static final int ERR_LINK_LOSS = 0;
    private static final HiLogLabel LABEL = new HiLogLabel(3, NfcKitsUtils.NFC_DOMAIN_ID, "HostService");
    public static final String META_DATA_NAME = "ohos.nfc.cardemulation.data.host_service";
    public static final int MSG_APP_RESPONSE = 1;
    public static final int MSG_DISABLED_CALLBACK = 2;
    public static final int MSG_REMOTE_COMMAND = 0;
    public static final String SERVICE_NAME = "ohos.nfc.cardemulation.action.HOST_SERVICE";
    private RemoteObject mRemoteHostService = new RemoteObject("HostService");

    public abstract void disabledCallback(int i);

    public abstract byte[] handleRemoteCommand(byte[] bArr, IntentParams intentParams);

    private static class MsgHandler extends EventHandler {
        public MsgHandler(EventRunner eventRunner) {
            super(eventRunner);
        }

        public void processEvent(InnerEvent innerEvent) {
            Object obj = innerEvent.object;
            int i = innerEvent.eventId;
            if (i != 0 && i != 1) {
                if (i != 2) {
                    HiLog.warn(HostService.LABEL, "ignored event: %{public}d", Integer.valueOf(innerEvent.eventId));
                } else {
                    HiLog.info(HostService.LABEL, "Received MSG_DISABLED_CALLBACK", new Object[0]);
                }
            }
        }
    }

    public final void sendResponse(byte[] bArr) {
        new MsgHandler(EventRunner.create(true)).sendEvent(InnerEvent.get(1));
    }

    public IRemoteObject onConnect(Intent intent) {
        HostService.super.onConnect(intent);
        return this.mRemoteHostService;
    }
}
