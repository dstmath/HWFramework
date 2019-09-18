package com.android.server.pm;

import java.util.List;

class IntentFilterVerificationResponse {
    public final int callerUid;
    public final int code;
    public final List<String> failedDomains;

    public IntentFilterVerificationResponse(int callerUid2, int code2, List<String> failedDomains2) {
        this.callerUid = callerUid2;
        this.code = code2;
        this.failedDomains = failedDomains2;
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
