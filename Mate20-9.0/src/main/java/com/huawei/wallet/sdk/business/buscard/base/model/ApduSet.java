package com.huawei.wallet.sdk.business.buscard.base.model;

import com.huawei.wallet.sdk.business.bankcard.constant.Constants;
import com.huawei.wallet.sdk.common.apdu.model.ApduCommand;
import java.util.HashMap;
import java.util.List;

public class ApduSet {
    private HashMap<String, List<ApduCommand>> apduTable = new HashMap<>();
    private boolean isSameApduNumAndDate;

    public void add(String type, List<ApduCommand> apdus) {
        this.apduTable.put(type, apdus);
    }

    public List<ApduCommand> getApduByType(String type) {
        return this.apduTable.get(type);
    }

    public boolean isSameApduNumAndDate() {
        return this.isSameApduNumAndDate;
    }

    public void compareCardNumAndDateApdus() {
        List<ApduCommand> cardNumApdus = this.apduTable.get(Constants.FIELD_APPLET_CONFIG_NUM);
        List<ApduCommand> cardDateApdus = this.apduTable.get("date");
        if (cardNumApdus == null || cardNumApdus.isEmpty() || cardDateApdus == null || cardDateApdus.isEmpty()) {
            this.isSameApduNumAndDate = false;
        } else if (cardNumApdus.size() != cardDateApdus.size()) {
            this.isSameApduNumAndDate = false;
        } else {
            for (int i = 0; i < cardNumApdus.size(); i++) {
                if (!cardNumApdus.get(i).getApdu().equalsIgnoreCase(cardDateApdus.get(i).getApdu())) {
                    this.isSameApduNumAndDate = false;
                    return;
                }
            }
            this.isSameApduNumAndDate = true;
        }
    }
}
