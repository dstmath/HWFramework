package ohos.net;

import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;
import ohos.account.AccountAbility;
import ohos.agp.window.dialog.CommonDialog;
import ohos.agp.window.dialog.IDialog;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IPCSkeleton;
import ohos.rpc.IRemoteObject;
import ohos.rpc.ReliableFileDescriptor;
import ohos.rpc.RemoteException;

public class VpnAbility extends Ability {
    private static final int BUTTON_ID_NEGATIVE = 0;
    private static final int BUTTON_ID_POSITIVE = 2;
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109360, "VpnAbility");

    public IRemoteObject onConnect(Intent intent) {
        HiLog.info(LABEL, "VpnAbility onConnect ok", new Object[0]);
        return null;
    }

    public void onDisconnect() {
        terminateAbility();
    }

    public static void preCreateVpn(Context context) {
        if (context != null) {
            NetManagerProxy instance = NetManagerProxy.getInstance();
            String bundleName = context.getBundleName();
            try {
                if (instance.preCreateVpn(context.getBundleName(), null, AccountAbility.getAccountAbility().getOsAccountLocalIdFromUid(IPCSkeleton.getCallingUid()))) {
                    return;
                }
            } catch (RemoteException unused) {
                HiLog.info(LABEL, "preCreateVpn error", new Object[0]);
            }
            addCommonDialog(context, bundleName);
            return;
        }
        throw new IllegalArgumentException("Bad context");
    }

    private static void addCommonDialog(Context context, final String str) {
        final CommonDialog commonDialog = new CommonDialog(context);
        commonDialog.setTitleText("Connect request");
        commonDialog.setContentText("Current app is trying to create vpn connection, please confirm or cancel it.");
        commonDialog.setButton(0, "OK", new IDialog.ClickedListener() {
            /* class ohos.net.VpnAbility.AnonymousClass1 */

            public void onClick(IDialog iDialog, int i) {
                HiLog.debug(VpnAbility.LABEL, "enter and authorize", new Object[0]);
                try {
                    NetManagerProxy.getInstance().setVpnPackageAuthorization(str, IPCSkeleton.getCallingUid(), true);
                } catch (RemoteException unused) {
                    HiLog.warn(VpnAbility.LABEL, "Failed to setVpnPackageAuthorization", new Object[0]);
                }
            }
        });
        commonDialog.setButton(2, "CANCEL", new IDialog.ClickedListener() {
            /* class ohos.net.VpnAbility.AnonymousClass2 */

            public void onClick(IDialog iDialog, int i) {
                HiLog.debug(VpnAbility.LABEL, "cancel and close dialog", new Object[0]);
                commonDialog.destroy();
            }
        });
        commonDialog.show();
    }

    public boolean protectFromSocket(int i) {
        return NetworkUtilsAdapter.protectFromVpn(i);
    }

    public ReliableFileDescriptor setUp(VpnProperty vpnProperty) {
        if (vpnProperty == null) {
            return null;
        }
        return vpnProperty.setUp();
    }
}
