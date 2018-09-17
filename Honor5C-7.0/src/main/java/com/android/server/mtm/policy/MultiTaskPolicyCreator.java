package com.android.server.mtm.policy;

import android.app.mtm.MultiTaskPolicy;
import android.os.Bundle;

public interface MultiTaskPolicyCreator {
    MultiTaskPolicy getResourcePolicy(int i, String str, int i2, Bundle bundle);
}
