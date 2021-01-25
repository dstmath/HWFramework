package ohos.distributedschedule.scenario;

public abstract class ScenarioSubscriber {
    private final IScenarioSubscriber innerSubscriber;
    private SubscribeInfo subscribeInfo;

    public abstract void onScenarioNotify(ScenarioResult scenarioResult);

    public ScenarioSubscriber() {
        this(null);
    }

    public ScenarioSubscriber(SubscribeInfo subscribeInfo2) {
        this.innerSubscriber = new ScenarioSubscriberHost() {
            /* class ohos.distributedschedule.scenario.ScenarioSubscriber.AnonymousClass1 */

            @Override // ohos.distributedschedule.scenario.IScenarioSubscriber
            public void onScenarioNotify(ScenarioResult scenarioResult) {
                ScenarioSubscriber.this.onScenarioNotify(scenarioResult);
            }
        };
        this.subscribeInfo = subscribeInfo2;
    }

    public final void setSubscribeInfo(SubscribeInfo subscribeInfo2) {
        if (subscribeInfo2 != null) {
            this.subscribeInfo = subscribeInfo2;
        }
    }

    public final SubscribeInfo getSubscribeInfo() {
        return this.subscribeInfo;
    }

    /* access modifiers changed from: package-private */
    public final IScenarioSubscriber getSubscriber() {
        return this.innerSubscriber;
    }
}
