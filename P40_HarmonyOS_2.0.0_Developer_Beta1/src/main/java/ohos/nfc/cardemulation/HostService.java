package ohos.nfc.cardemulation;

import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.IntentParams;
import ohos.eventhandler.Courier;
import ohos.eventhandler.EventHandler;
import ohos.eventhandler.EventRunner;
import ohos.eventhandler.InnerEvent;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.nfc.NfcKitsUtils;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;
import ohos.utils.PacMap;

public abstract class HostService extends Ability {
    public static final int ERR_DESELECTED = 1;
    public static final int ERR_LINK_LOSS = 0;
    public static final String KEY_DATA = "data";
    private static final HiLogLabel LABEL = new HiLogLabel(3, NfcKitsUtils.NFC_DOMAIN_ID, "HostService");
    public static final String META_DATA_NAME = "ohos.nfc.cardemulation.data.host_service";
    public static final int MSG_APP_RESPONSE = 1;
    public static final int MSG_DISABLED_CALLBACK = 2;
    public static final int MSG_REMOTE_COMMAND = 0;
    public static final int MSG_UNHANDLED = 3;
    public static final String SERVICE_NAME = "ohos.nfc.cardemulation.action.HOST_SERVICE";
    final Courier mCourier = new Courier(new MsgHandler(EventRunner.create(true)));
    Courier mNfcService = null;

    public abstract void disabledCallback(int i);

    public abstract byte[] handleRemoteCommand(byte[] bArr, IntentParams intentParams);

    final class MsgHandler extends EventHandler {
        public MsgHandler(EventRunner eventRunner) {
            super(eventRunner);
        }

        public void processEvent(InnerEvent innerEvent) {
            Object obj = innerEvent.object;
            int i = innerEvent.eventId;
            if (i == 0) {
                HiLog.error(HostService.LABEL, "processEvent: %{public}d", 0);
                PacMap pacMap = innerEvent.getPacMap();
                if (pacMap != null) {
                    if (HostService.this.mNfcService == null) {
                        HostService.this.mNfcService = innerEvent.replyTo;
                    }
                    byte[] byteValueArray = pacMap.getByteValueArray("data");
                    if (byteValueArray != null) {
                        byte[] handleRemoteCommand = HostService.this.handleRemoteCommand(byteValueArray, null);
                        if (handleRemoteCommand == null) {
                            return;
                        }
                        if (HostService.this.mNfcService == null) {
                            HiLog.error(HostService.LABEL, "Response not sent; service was deactivated.", new Object[0]);
                            return;
                        }
                        InnerEvent innerEvent2 = InnerEvent.get(1);
                        PacMap pacMap2 = new PacMap();
                        pacMap2.putByteValueArray("data", handleRemoteCommand);
                        innerEvent2.setPacMap(pacMap2);
                        innerEvent2.replyTo = HostService.this.mCourier;
                        try {
                            HostService.this.mNfcService.send(innerEvent2);
                        } catch (RemoteException unused) {
                            HiLog.error(HostService.LABEL, "Response not sent; RemoteException calling into NfcService.", new Object[0]);
                        }
                    } else {
                        HiLog.error(HostService.LABEL, "Received MSG_COMMAND_APDU without data.", new Object[0]);
                    }
                }
            } else if (i == 1) {
                HiLog.error(HostService.LABEL, "processEvent: %{public}d", 1);
                if (HostService.this.mNfcService == null) {
                    HiLog.error(HostService.LABEL, "Response not sent; service was deactivated.", new Object[0]);
                    return;
                }
                try {
                    innerEvent.replyTo = HostService.this.mCourier;
                    HostService.this.mNfcService.send(innerEvent);
                } catch (RemoteException unused2) {
                    HiLog.error(HostService.LABEL, "RemoteException calling into NfcService.", new Object[0]);
                }
            } else if (i == 2) {
                HiLog.error(HostService.LABEL, "processEvent: %{public}d", 2);
                HiLog.info(HostService.LABEL, "Received MSG_DISABLED_CALLBACK", new Object[0]);
                HostService hostService = HostService.this;
                hostService.mNfcService = null;
                hostService.disabledCallback((int) innerEvent.param);
            } else if (i != 3) {
                HiLog.warn(HostService.LABEL, "ignored event: %{public}d", Integer.valueOf(innerEvent.eventId));
            } else {
                HiLog.error(HostService.LABEL, "processEvent: %{public}d", 3);
                if (HostService.this.mNfcService == null) {
                    HiLog.error(HostService.LABEL, "notifyUnhandled not sent; service was deactivated.", new Object[0]);
                    return;
                }
                try {
                    innerEvent.replyTo = HostService.this.mCourier;
                    HostService.this.mNfcService.send(innerEvent);
                } catch (RemoteException unused3) {
                    HiLog.error(HostService.LABEL, "RemoteException calling into NfcService.", new Object[0]);
                }
            }
        }
    }

    public final void sendResponse(byte[] bArr) {
        InnerEvent innerEvent = InnerEvent.get(1);
        PacMap pacMap = new PacMap();
        pacMap.putByteValueArray("data", bArr);
        innerEvent.setPacMap(pacMap);
        try {
            this.mCourier.send(innerEvent);
        } catch (RemoteException unused) {
            HiLog.error(LABEL, "Local messenger has died.", new Object[0]);
        }
    }

    public IRemoteObject onConnect(Intent intent) {
        HiLog.error(LABEL, "onConnect!", new Object[0]);
        HostService.super.onConnect(intent);
        return this.mCourier.getRemoteObject();
    }
}
