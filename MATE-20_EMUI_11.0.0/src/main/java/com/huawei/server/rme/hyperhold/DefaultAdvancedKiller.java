package com.huawei.server.rme.hyperhold;

import android.content.Context;
import android.os.Bundle;
import com.huawei.server.rme.collector.ResourceCollector;

public class DefaultAdvancedKiller {
    public DefaultAdvancedKiller() {
    }

    public DefaultAdvancedKiller(Context contextInit) {
    }

    public void updateModel(String pkg) {
    }

    public void serializeModel() {
        ResourceCollector.serializeKillModel();
    }

    public int execute(Bundle extras) {
        return 0;
    }

    public void setInterrupt(boolean interrupt) {
    }
}
