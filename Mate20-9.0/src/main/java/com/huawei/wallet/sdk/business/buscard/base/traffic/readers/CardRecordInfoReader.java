package com.huawei.wallet.sdk.business.buscard.base.traffic.readers;

import com.huawei.wallet.sdk.business.buscard.base.model.ApduCommandInfo;
import com.huawei.wallet.sdk.business.buscard.base.model.TransactionRecord;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import com.huawei.wallet.sdk.common.apdu.IAPDUService;
import java.util.ArrayList;
import java.util.List;

public class CardRecordInfoReader extends InfoReader<List<TransactionRecord>> {
    private static final int RECORES_CNT = 10;
    private static final String SW_6A83 = "6A83";

    public CardRecordInfoReader(IAPDUService omaService) {
        super(omaService);
    }

    /* access modifiers changed from: protected */
    public List<TransactionRecord> handleResult(List<List<String>> results) {
        List<TransactionRecord> records = new ArrayList<>();
        for (List<String> recordDatas : results) {
            TransactionRecord record = parseRecord(recordDatas);
            if (record != null) {
                records.add(record);
            }
        }
        return records;
    }

    private TransactionRecord parseRecord(List<String> datas) {
        if (datas == null || datas.isEmpty() || datas.size() <= 4 || datas.get(0).matches("^0*$")) {
            return null;
        }
        TransactionRecord record = new TransactionRecord();
        try {
            record.setRecordAmount(Integer.parseInt(datas.get(1), 16));
        } catch (NumberFormatException e) {
            LogX.i("parseRecord parse amount failed. rapdu :  amountStr : ");
        }
        record.setRecordType(datas.get(2));
        record.setRecordTime(datas.get(3));
        if (datas.size() >= 5) {
            if ("1".equals(datas.get(4))) {
                record.setRecordType("1");
            } else {
                record.setRecordType("2");
            }
        }
        return record;
    }

    /* access modifiers changed from: protected */
    public int getNextStep(ApduCommandInfo dataCommandInfo, int currentStep, int totleStep) {
        if (SW_6A83.equals(dataCommandInfo.getSw())) {
            int dataTypeCnt = totleStep / 10;
            for (int i = 1; i <= dataTypeCnt; i++) {
                int maxIdx = i * 10;
                if (currentStep < maxIdx) {
                    LogX.i("readRecords for 6A83, skip steps(" + currentStep + " - " + (maxIdx - 1) + ", totle steps : " + totleStep);
                    return maxIdx;
                }
            }
        }
        return super.getNextStep(dataCommandInfo, currentStep, totleStep);
    }
}
