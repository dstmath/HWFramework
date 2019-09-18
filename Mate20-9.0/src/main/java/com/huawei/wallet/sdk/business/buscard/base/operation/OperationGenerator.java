package com.huawei.wallet.sdk.business.buscard.base.operation;

import com.huawei.wallet.sdk.business.buscard.base.util.AppletCardException;
import com.huawei.wallet.sdk.common.utils.StringUtil;
import java.util.ArrayList;
import java.util.List;

public class OperationGenerator {
    private static Operation initHandler(String type) {
        if ("cut".equals(type)) {
            return new CutStringOperation();
        }
        if ("match".equals(type)) {
            return new MatchConditionOperation();
        }
        if ("parseInt".equals(type)) {
            return new ParseIntOperation();
        }
        if ("reverse".equals(type)) {
            return new ReverseOperation();
        }
        if ("minus".equals(type)) {
            return new MinusOperation();
        }
        if ("xor".equals(type)) {
            return new XorOperation();
        }
        if ("mod".equals(type)) {
            return new ModOperation();
        }
        if ("cat".equals(type)) {
            return new CatStringOperation();
        }
        if ("qdtcardno".equals(type)) {
            return new QDTCardSpecialOperation();
        }
        if ("fmcardno".equals(type)) {
            return new FMSpecialOperation();
        }
        return null;
    }

    public static List<Operation> parseOperations(String data) throws AppletCardException {
        if (StringUtil.isEmpty(data, true)) {
            return null;
        }
        List<Operation> operations = new ArrayList<>();
        for (String op : data.split(";")) {
            String[] arrays = op.split(",");
            Operation operation = initHandler(arrays[0]);
            if (operation != null) {
                if (arrays.length >= 3) {
                    operation.init(arrays[1], arrays[2]);
                    operations.add(operation);
                } else {
                    throw new AppletCardException(2, "operation config error. config data : " + op);
                }
            }
        }
        return operations;
    }
}
