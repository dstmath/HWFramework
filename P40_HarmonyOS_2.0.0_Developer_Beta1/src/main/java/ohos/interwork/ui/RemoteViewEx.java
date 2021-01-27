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
    private final Context mContext;
    private final ComponentProvider mRemoteView;
    private final RemoteViewUtils mRemoteViewUtils;

    public RemoteViewEx(Context context, ComponentProvider componentProvider) {
        this.mContext = context;
        this.mRemoteView = componentProvider;
        this.mRemoteViewUtils = new RemoteViewUtils(context);
    }

    @Override // ohos.interwork.utils.ParcelableEx
    public void marshallingEx(Parcel parcel) {
        if (this.mContext == null || this.mRemoteView == null || this.mRemoteViewUtils == null || parcel == null) {
            HiLog.error(LABEL, "marshalling params unavailable.", new Object[0]);
            return;
        }
        HiLog.debug(LABEL, "marshalling enter.", new Object[0]);
        this.mRemoteViewUtils.marshallRemoteViewEx(this.mRemoteView, parcel);
    }

    public int getRemoteResourceId(int i) {
        RemoteViewUtils remoteViewUtils = this.mRemoteViewUtils;
        if (remoteViewUtils != null) {
            return remoteViewUtils.convertResourceId(i);
        }
        HiLog.error(LABEL, "RemoteViewUtils object unavailable.", new Object[0]);
        return 0;
    }
}
