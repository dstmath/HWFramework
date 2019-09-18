package com.huawei.wallet.sdk.business.buscard.base.model;

import com.huawei.wallet.sdk.business.buscard.base.operation.Operation;
import com.huawei.wallet.sdk.common.apdu.model.ApduCommand;
import java.util.List;

public class ApduCommandInfo extends ApduCommand {
    private List<Operation> operations;
    private String type;

    public ApduCommandInfo() {
    }

    public ApduCommandInfo(int index, String apdu, String checker, String type2, List<Operation> operations2) {
        super(index, apdu, checker);
        this.type = type2;
        this.operations = operations2;
    }

    public List<Operation> getOperations() {
        return this.operations;
    }

    public void setOperations(List<Operation> operations2) {
        this.operations = operations2;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type2) {
        this.type = type2;
    }
}
