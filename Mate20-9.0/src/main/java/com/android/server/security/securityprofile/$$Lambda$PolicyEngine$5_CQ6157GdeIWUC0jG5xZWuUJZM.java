package com.android.server.security.securityprofile;

import com.android.server.security.securityprofile.PolicyEngine;
import org.json.JSONObject;

/* renamed from: com.android.server.security.securityprofile.-$$Lambda$PolicyEngine$5_CQ6157GdeIWUC0jG5xZWuUJZM  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$PolicyEngine$5_CQ6157GdeIWUC0jG5xZWuUJZM implements PolicyEngine.RuleToKey {
    public static final /* synthetic */ $$Lambda$PolicyEngine$5_CQ6157GdeIWUC0jG5xZWuUJZM INSTANCE = new $$Lambda$PolicyEngine$5_CQ6157GdeIWUC0jG5xZWuUJZM();

    private /* synthetic */ $$Lambda$PolicyEngine$5_CQ6157GdeIWUC0jG5xZWuUJZM() {
    }

    public final String getKey(JSONObject jSONObject) {
        return PolicyEngine.lambda$createLookup$3(jSONObject);
    }
}
