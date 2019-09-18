package com.android.server.security.securityprofile;

import com.android.server.security.securityprofile.PolicyEngine;
import org.json.JSONObject;

/* renamed from: com.android.server.security.securityprofile.-$$Lambda$PolicyEngine$y8ZWBmdZk6eAk13PmjJk7plltsI  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$PolicyEngine$y8ZWBmdZk6eAk13PmjJk7plltsI implements PolicyEngine.RuleToKey {
    public static final /* synthetic */ $$Lambda$PolicyEngine$y8ZWBmdZk6eAk13PmjJk7plltsI INSTANCE = new $$Lambda$PolicyEngine$y8ZWBmdZk6eAk13PmjJk7plltsI();

    private /* synthetic */ $$Lambda$PolicyEngine$y8ZWBmdZk6eAk13PmjJk7plltsI() {
    }

    public final String getKey(JSONObject jSONObject) {
        return PolicyEngine.lambda$createLookup$1(jSONObject);
    }
}
