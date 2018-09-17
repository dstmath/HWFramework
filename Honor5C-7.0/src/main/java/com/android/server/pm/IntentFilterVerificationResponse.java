package com.android.server.pm;

import java.util.List;

class IntentFilterVerificationResponse {
    public final int callerUid;
    public final int code;
    public final List<String> failedDomains;

    public IntentFilterVerificationResponse(int callerUid, int code, List<String> failedDomains) {
        this.callerUid = callerUid;
        this.code = code;
        this.failedDomains = failedDomains;
    }

    public String getFailedDomainsString() {
        StringBuilder sb = new StringBuilder();
        for (String domain : this.failedDomains) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(domain);
        }
        return sb.toString();
    }
}
