package com.android.server.mtm.iaware.policy;

import android.app.mtm.iaware.SceneData;
import android.rms.iaware.RPolicyData;

public interface PolicyCreator {
    RPolicyData createPolicyData(SceneData sceneData);

    void reportScene(SceneData sceneData);
}
