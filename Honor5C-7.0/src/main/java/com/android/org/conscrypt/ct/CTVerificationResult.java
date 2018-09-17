package com.android.org.conscrypt.ct;

import com.android.org.conscrypt.ct.VerifiedSCT.Status;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CTVerificationResult {
    private final List<VerifiedSCT> invalidSCTs;
    private final List<VerifiedSCT> validSCTs;

    public CTVerificationResult() {
        this.validSCTs = new ArrayList();
        this.invalidSCTs = new ArrayList();
    }

    public void add(VerifiedSCT result) {
        if (result.status == Status.VALID) {
            this.validSCTs.add(result);
        } else {
            this.invalidSCTs.add(result);
        }
    }

    public List<VerifiedSCT> getValidSCTs() {
        return Collections.unmodifiableList(this.validSCTs);
    }

    public List<VerifiedSCT> getInvalidSCTs() {
        return Collections.unmodifiableList(this.invalidSCTs);
    }
}
