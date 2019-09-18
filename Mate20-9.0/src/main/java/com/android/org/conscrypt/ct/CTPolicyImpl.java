package com.android.org.conscrypt.ct;

import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Set;

public class CTPolicyImpl implements CTPolicy {
    private final CTLogStore logStore;
    private final int minimumLogCount;

    public CTPolicyImpl(CTLogStore logStore2, int minimumLogCount2) {
        this.logStore = logStore2;
        this.minimumLogCount = minimumLogCount2;
    }

    public boolean doesResultConformToPolicy(CTVerificationResult result, String hostname, X509Certificate[] chain) {
        Set<CTLogInfo> logSet = new HashSet<>();
        for (VerifiedSCT verifiedSCT : result.getValidSCTs()) {
            CTLogInfo log = this.logStore.getKnownLog(verifiedSCT.sct.getLogID());
            if (log != null) {
                logSet.add(log);
            }
        }
        return logSet.size() >= this.minimumLogCount;
    }
}
