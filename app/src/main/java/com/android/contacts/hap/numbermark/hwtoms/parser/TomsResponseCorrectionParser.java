package com.android.contacts.hap.numbermark.hwtoms.parser;

import com.android.contacts.hap.numbermark.hwtoms.model.response.TomsResponseCorrection;

public class TomsResponseCorrectionParser {
    public TomsResponseCorrection objectParser(String errorCode) {
        TomsResponseCorrection bean = new TomsResponseCorrection();
        bean.setErrorCode(errorCode);
        return bean;
    }
}
