package com.android.org.conscrypt.ct;

import com.android.org.conscrypt.ct.VerifiedSCT.Status;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CTVerificationResult {
    private final ArrayList<VerifiedSCT> invalidSCTs = new ArrayList();
    private final ArrayList<VerifiedSCT> validSCTs = new ArrayList();

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
