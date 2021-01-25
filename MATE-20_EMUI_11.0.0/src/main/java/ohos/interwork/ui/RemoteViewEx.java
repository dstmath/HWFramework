package ohos.interwork.ui;

import ohos.agp.components.ComponentProvider;
import ohos.agp.components.surfaceview.adapter.RemoteViewUtils;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.interwork.utils.ParcelableEx;
import ohos.utils.Parcel;

public class RemoteViewEx implements ParcelableEx {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218108928, "RemoteViewEx");
    private Context mContext;
    private ComponentProvider mRemoteView;

    public RemoteViewEx(Context context, ComponentProvider componentProvider) {
        this.mContext = context;
        this.mRemoteView = componentProvider;
    }

    @Override // ohos.interwork.utils.ParcelableEx
    public void marshallingEx(Parcel parcel) {
        if (this.mContext == null || this.mRemoteView == null || parcel == null) {
            HiLog.error(LABEL, "marshalling the param available.", new Object[0]);
            return;
        }
        HiLog.debug(LABEL, "marshalling enter.", new Object[0]);
        RemoteViewUtils remoteViewUtils = new RemoteViewUtils(this.mContext);
        parcel.writeString(remoteViewUtils.getARemoteViewClass());
        parcel.writeBytes(remoteViewUtils.getARemoteViewBytes(this.mRemoteView, false));
    }
}
