package ohos.data.distributed.sys.start;

import android.content.Context;
import ohos.event.commonevent.CommonEventManager;
import ohos.event.commonevent.CommonEventSubscribeInfo;
import ohos.event.commonevent.MatchingSkills;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.RemoteException;

public class DistributedDataFramework {
    private static final String DISTRIBUTED_DATA_SERVICE = "DistributedDataMgrStarter";
    private static final DistributedDataFramework INSTANCE = new DistributedDataFramework();
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109456, "ZDDSJ");
    private Context mContext;
    private ServiceEventSubscriber subscriber;

    private DistributedDataFramework() {
    }

    public static DistributedDataFramework getInstance() {
        return INSTANCE;
    }

    public void init(Context context) {
        if (context == null) {
            HiLog.warn(LABEL, "register service subscriber failed, context is null.", new Object[0]);
            return;
        }
        this.mContext = context;
        registerSubscriber();
    }

    private void registerSubscriber() {
        MatchingSkills matchingSkills = new MatchingSkills();
        matchingSkills.addEvent(DISTRIBUTED_DATA_SERVICE);
        HiLog.info(LABEL, "register service subscriber", new Object[0]);
        CommonEventSubscribeInfo commonEventSubscribeInfo = new CommonEventSubscribeInfo(matchingSkills);
        commonEventSubscribeInfo.setPriority(100);
        if (this.subscriber == null) {
            this.subscriber = new ServiceEventSubscriber(this.mContext, commonEventSubscribeInfo);
        }
        try {
            CommonEventManager.subscribeCommonEvent(this.subscriber);
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "distributed subscribeCommonEvent occur exception.", new Object[0]);
        }
        HiLog.info(LABEL, "register service subscriber over", new Object[0]);
    }
}
