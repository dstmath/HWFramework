package com.android.server.mtm.iaware.policy;

import android.app.mtm.iaware.RSceneData;
import android.rms.iaware.RPolicyData;

public interface RPolicyCreator {
    RPolicyData createPolicyData(RSceneData rSceneData);

    void reportScene(RSceneData rSceneData);
}
