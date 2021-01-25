package com.huawei.internal.telephony.uicc;

import com.android.internal.telephony.uicc.IccCardStatus;

public class IccCardStatusExt {
    private IccCardStatus mIccCardStatus;

    public static IccCardStatusExt from(Object result) {
        if (!(result instanceof IccCardStatus)) {
            return null;
        }
        IccCardStatusExt iccCardStatusExt = new IccCardStatusExt();
        iccCardStatusExt.setIccCardStatus((IccCardStatus) result);
        return iccCardStatusExt;
    }

    public IccCardStatus getIccCardStatus() {
        return this.mIccCardStatus;
    }

    public void setIccCardStatus(IccCardStatus iccCardStatus) {
        this.mIccCardStatus = iccCardStatus;
    }

    public CardStateEx getCardState() {
        IccCardStatus iccCardStatus = this.mIccCardStatus;
        if (iccCardStatus != null) {
            return CardStateEx.getCardStateExByCardState(iccCardStatus.mCardState);
        }
        return null;
    }

    public enum CardStateEx {
        CARDSTATE_ABSENT(IccCardStatus.CardState.CARDSTATE_ABSENT),
        CARDSTATE_PRESENT(IccCardStatus.CardState.CARDSTATE_PRESENT),
        CARDSTATE_ERROR(IccCardStatus.CardState.CARDSTATE_ERROR),
        CARDSTATE_RESTRICTED(IccCardStatus.CardState.CARDSTATE_RESTRICTED);
        
        private final IccCardStatus.CardState value;

        private CardStateEx(IccCardStatus.CardState value2) {
            this.value = value2;
        }

        public static CardStateEx getCardStateExByCardState(IccCardStatus.CardState cardState) {
            if (cardState == null) {
                return null;
            }
            int i = AnonymousClass1.$SwitchMap$com$android$internal$telephony$uicc$IccCardStatus$CardState[cardState.ordinal()];
            if (i == 1) {
                return CARDSTATE_ABSENT;
            }
            if (i == 2) {
                return CARDSTATE_PRESENT;
            }
            if (i == 3) {
                return CARDSTATE_ERROR;
            }
            if (i != 4) {
                return null;
            }
            return CARDSTATE_RESTRICTED;
        }

        public boolean isCardPresent() {
            return this == CARDSTATE_PRESENT || this == CARDSTATE_RESTRICTED;
        }

        public IccCardStatus.CardState getValue() {
            return this.value;
        }
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.huawei.internal.telephony.uicc.IccCardStatusExt$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$android$internal$telephony$uicc$IccCardStatus$CardState = new int[IccCardStatus.CardState.values().length];

        static {
            try {
                $SwitchMap$com$android$internal$telephony$uicc$IccCardStatus$CardState[IccCardStatus.CardState.CARDSTATE_ABSENT.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$uicc$IccCardStatus$CardState[IccCardStatus.CardState.CARDSTATE_PRESENT.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$uicc$IccCardStatus$CardState[IccCardStatus.CardState.CARDSTATE_ERROR.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$uicc$IccCardStatus$CardState[IccCardStatus.CardState.CARDSTATE_RESTRICTED.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
    }
}
