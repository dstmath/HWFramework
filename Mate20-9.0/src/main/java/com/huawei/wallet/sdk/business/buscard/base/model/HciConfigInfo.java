package com.huawei.wallet.sdk.business.buscard.base.model;

import com.huawei.wallet.sdk.business.buscard.base.operation.Operation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HciConfigInfo {
    public static final String HCI_DATA_TYPE_AFTER_TERMINAL_ID = "terminal";
    public static final String HCI_DATA_TYPE_AFTER_TRANSCTION_BALANCE = "balance";
    public static final String HCI_DATA_TYPE_TRANSCTION_AMOUNT = "amt";
    public static final String HCI_DATA_TYPE_TRANSCTION_DATE = "date";
    public static final String HCI_DATA_TYPE_TRANSCTION_TIME = "time";
    public static final String HCI_DATA_TYPE_TRANSCTION_TYPE = "trans_type";
    public static final String HCI_DATA_TYPE_VERSION = "version";
    private Map<String, List<Operation>> operations = new HashMap();
    private int tlvhcioffset = -1;
    private String version;

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version2) {
        this.version = version2;
    }

    public int getTlvhcioffset() {
        return this.tlvhcioffset;
    }

    public void setTlvhcioffset(int tlvhcioffset2) {
        this.tlvhcioffset = tlvhcioffset2;
    }

    public void addOperations(String type, List<Operation> ops) {
        this.operations.put(type, ops);
    }

    public List<Operation> getOperationsByType(String type) {
        return this.operations.get(type);
    }
}
