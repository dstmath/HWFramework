package ohos.distributedschedule.scenario;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.Parcel;

public final class SubscribeInfo {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109952, TAG);
    private static final String TAG = "SubscribeInfo";
    private final List<ScenarioInfo> scenarioInfos = new ArrayList();

    public static /* synthetic */ ArrayList lambda$OGSS2qx6njxlnp0dnKb4lA3jnw8() {
        return new ArrayList();
    }

    public SubscribeInfo() {
    }

    SubscribeInfo(SubscribeInfo subscribeInfo) {
        if (subscribeInfo != null) {
            this.scenarioInfos.addAll(subscribeInfo.getScenarioInfos());
        }
    }

    static /* synthetic */ ScenarioInfo lambda$setScenarioInfos$0(ScenarioInfo scenarioInfo) {
        return new ScenarioInfo(scenarioInfo);
    }

    public void setScenarioInfos(List<ScenarioInfo> list) {
        if (list != null) {
            List list2 = (List) list.stream().map($$Lambda$SubscribeInfo$zvCUsx1sVDG1YfMUCqqPkQN5oDE.INSTANCE).collect(Collectors.toCollection($$Lambda$SubscribeInfo$OGSS2qx6njxlnp0dnKb4lA3jnw8.INSTANCE));
            synchronized (this.scenarioInfos) {
                this.scenarioInfos.clear();
                this.scenarioInfos.addAll(list2);
            }
        }
    }

    public List<ScenarioInfo> getScenarioInfos() {
        List<ScenarioInfo> list;
        synchronized (this.scenarioInfos) {
            list = (List) this.scenarioInfos.stream().map($$Lambda$SubscribeInfo$FkfvlLl4Wu3_VSQVRjBrzPw1uhI.INSTANCE).collect(Collectors.toCollection($$Lambda$SubscribeInfo$OGSS2qx6njxlnp0dnKb4lA3jnw8.INSTANCE));
        }
        return list;
    }

    static /* synthetic */ ScenarioInfo lambda$getScenarioInfos$1(ScenarioInfo scenarioInfo) {
        return new ScenarioInfo(scenarioInfo);
    }

    /* access modifiers changed from: package-private */
    public boolean marshalling(Parcel parcel) {
        synchronized (this.scenarioInfos) {
            if (this.scenarioInfos.isEmpty()) {
                HiLog.warn(LABEL, "write empty scenario infos failed.", new Object[0]);
                return false;
            } else if (!parcel.writeInt(this.scenarioInfos.size())) {
                HiLog.warn(LABEL, "write size of scenarioInfos failed.", new Object[0]);
                return false;
            } else {
                for (ScenarioInfo scenarioInfo : this.scenarioInfos) {
                    if (!scenarioInfo.marshalling(parcel)) {
                        HiLog.warn(LABEL, "write a info failed.", new Object[0]);
                        return false;
                    }
                }
                return true;
            }
        }
    }
}
