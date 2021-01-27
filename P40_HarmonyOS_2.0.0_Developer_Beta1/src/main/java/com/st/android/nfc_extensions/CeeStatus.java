package com.st.android.nfc_extensions;

import java.util.ArrayList;
import java.util.List;

public class CeeStatus {
    private static final boolean DBG = true;
    private String ceeSupport;
    private List<String> t4tRfTypes = new ArrayList();
    private String t4tStatus;
    String tag = "CeeStatus";

    public String getCeeSupport() {
        return this.ceeSupport;
    }

    public void setCeeSupport(String status) {
        this.ceeSupport = status;
    }

    public String getT4tStatus() {
        return this.t4tStatus;
    }

    public void setT4tStatus(String status) {
        this.t4tStatus = status;
    }

    public List<String> getT4tTfTypes() {
        return this.t4tRfTypes;
    }

    public void setT4tTfTypes(String rfType) {
        this.t4tRfTypes.add(rfType);
    }
}
