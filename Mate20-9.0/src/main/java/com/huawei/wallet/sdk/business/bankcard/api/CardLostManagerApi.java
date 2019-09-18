package com.huawei.wallet.sdk.business.bankcard.api;

public interface CardLostManagerApi {
    void clearAllNullifiedCardLocalInfo();

    void clearNullifiedCardLocalInfo(String str, String str2);

    void deleteLocalBankCards(HandleDeleteLocalCardsCallback handleDeleteLocalCardsCallback, String str);
}
