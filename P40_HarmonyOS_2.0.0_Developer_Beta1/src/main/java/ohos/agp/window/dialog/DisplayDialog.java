package ohos.agp.window.dialog;

import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.components.DirectionalLayout;
import ohos.agp.window.wmc.AGPPresentationWindow;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class DisplayDialog extends BaseDialog {
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) LogDomain.END, "DisplayDialog");

    public DisplayDialog(Context context) {
        super(context, 7);
    }

    public void setContentComponent(DirectionalLayout directionalLayout) {
        if (directionalLayout != null) {
            this.mContentLayout = directionalLayout;
        }
    }

    public void showOnRemoteScreen(DialogProvider dialogProvider) {
        if (dialogProvider == null) {
            HiLog.error(LABEL, "showOnRemoteScreen failed for remoteScreen is null.", new Object[0]);
            return;
        }
        super.create();
        if (this.mWindow instanceof AGPPresentationWindow) {
            ((AGPPresentationWindow) this.mWindow).showOnRemoteScreen(dialogProvider.ip, dialogProvider.port, dialogProvider.width, dialogProvider.height, dialogProvider.densityDpi);
        }
        super.show();
    }

    public static class DialogProvider {
        public int densityDpi;
        public int height;
        public String ip;
        public int port;
        public int width;

        public DialogProvider(String str, int i, int i2, int i3, int i4) {
            this.ip = str;
            this.port = i;
            this.width = i2;
            this.height = i3;
            this.densityDpi = i4;
        }
    }
}
