package ohos.distributedschedule.scenario;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.Parcel;

public final class ScenarioResult {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109952, TAG);
    public static final int RESULT_LEVEL_ENTER = 1;
    public static final int RESULT_LEVEL_EXIT = 0;
    private static final String TAG = "ScenarioResult";
    private ScenarioInfo info;
    private int resultLevel;

    static ScenarioResult unmarshalling(Parcel parcel) {
        ScenarioInfo scenarioInfo = new ScenarioInfo();
        if (scenarioInfo.unmarshalling(parcel)) {
            return new ScenarioResult(parcel.readInt(), scenarioInfo);
        }
        HiLog.warn(LABEL, "read info failed.", new Object[0]);
        return null;
    }

    public int getResultLevel() {
        return this.resultLevel;
    }

    public ScenarioInfo getInfo() {
        return this.info;
    }

    private ScenarioResult(int i, ScenarioInfo scenarioInfo) {
        this.resultLevel = i;
        this.info = scenarioInfo;
    }
}
