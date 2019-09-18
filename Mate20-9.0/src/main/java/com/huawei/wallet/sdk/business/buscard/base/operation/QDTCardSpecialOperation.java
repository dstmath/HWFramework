package com.huawei.wallet.sdk.business.buscard.base.operation;

import com.huawei.wallet.sdk.business.buscard.base.util.AppletCardException;
import com.huawei.wallet.sdk.common.utils.StringUtil;

public class QDTCardSpecialOperation extends Operation {
    /* access modifiers changed from: protected */
    public String handleData(String data) throws AppletCardException {
        if (!StringUtil.isEmpty(this.param, true)) {
            return getCardFaceNo(data);
        }
        throw new AppletCardException(2, " QDTCardSpecialOperation param is null");
    }

    private String getCardFaceNo(String cardId) {
        int[] b4;
        if (cardId == null || cardId.trim().length() != 16) {
            return cardId;
        }
        String srdCardId = cardId.trim();
        try {
            String cardId2 = getCardId(srdCardId);
            try {
                StringBuilder cardFaceNo = new StringBuilder();
                cardFaceNo.append(cardId2.substring(4));
                char[] originalCard = cardFaceNo.toString().toCharArray();
                int[] b1 = {5, 6, 8, 1, 0, 2, 3, 7, 9, 4};
                int[] b2 = {4, 8, 1, 3, 7, 2, 5, 9, 0, 6};
                int[] b3 = {1, 7, 2, 6, 8, 5, 9, 3, 4, 0};
                int ct1 = 0;
                int i = 0;
                while (i < 12) {
                    try {
                        int bt = originalCard[i] - '0';
                        ct1 = ct1 + bt + b1[bt];
                        i++;
                    } catch (RuntimeException e) {
                        String str = cardId2;
                        return srdCardId;
                    } catch (Exception e2) {
                        String str2 = cardId2;
                        return srdCardId;
                    }
                }
                cardFaceNo.append((ct1 + 31) % 9);
                int ct2 = 0;
                for (int i2 = 0; i2 < 12; i2++) {
                    int bt2 = originalCard[i2] - '0';
                    ct2 = ct2 + bt2 + b2[bt2];
                }
                cardFaceNo.append((ct2 + 31) % 9);
                int ct3 = 0;
                int i3 = 0;
                while (true) {
                    String cardId3 = cardId2;
                    if (i3 >= 12) {
                        break;
                    }
                    try {
                        int bt3 = originalCard[i3] - '0';
                        ct3 = ct3 + bt3 + b3[bt3];
                        i3++;
                        cardId2 = cardId3;
                    } catch (RuntimeException e3) {
                        return srdCardId;
                    } catch (Exception e4) {
                        return srdCardId;
                    }
                }
                int C3 = (ct3 + 31) % 9;
                cardFaceNo.append(C3);
                int ct4 = 0;
                int i4 = 0;
                while (true) {
                    int C32 = C3;
                    if (i4 < 12) {
                        int bt4 = originalCard[i4] - '0';
                        ct4 = ct4 + bt4 + b4[bt4];
                        i4++;
                        C3 = C32;
                    } else {
                        int C4 = (ct4 + 31) % 9;
                        cardFaceNo.append(C4);
                        int i5 = C4;
                        return cardFaceNo.toString().trim();
                    }
                }
            } catch (RuntimeException e5) {
                String str3 = cardId2;
                return srdCardId;
            } catch (Exception e6) {
                String str4 = cardId2;
                return srdCardId;
            }
        } catch (RuntimeException e7) {
            String str5 = cardId;
            return srdCardId;
        } catch (Exception e8) {
            String str6 = cardId;
            return srdCardId;
        }
    }

    private String getCardId(String cardAsn) {
        String cardId1 = cardAsn.substring(0, 8);
        String cardId2 = cardAsn.substring(8, 16);
        return cardId1 + getCardId2(cardId2);
    }

    private String getCardId2(String cardid2) {
        String cardid = String.valueOf(Integer.parseInt(cardid2, 16));
        if (cardid.length() < 8) {
            int sum = 8 - cardid.length();
            for (int i = 0; i < sum; i++) {
                cardid = "0" + cardid;
            }
        }
        return cardid;
    }
}
