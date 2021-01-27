package ohos.distributedschedule.scenario;

public final class DistributedScenarioManager {
    public static boolean subscribe(ScenarioSubscriber scenarioSubscriber) {
        return DistributedScenarioManagerClient.getInstance().subscribe(scenarioSubscriber);
    }

    public static void unsubscribe(ScenarioSubscriber scenarioSubscriber) {
        DistributedScenarioManagerClient.getInstance().unsubscribe(scenarioSubscriber);
    }

    private DistributedScenarioManager() {
    }
}
