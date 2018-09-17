package com.android.internal.telephony.uicc;

import com.google.android.mms.pdu.CharacterSets;
import com.google.android.mms.pdu.PduPersister;

public class IccCardStatus {
    public static final int CARD_MAX_APPS = 8;
    public IccCardApplicationStatus[] mApplications;
    public CardState mCardState;
    public int mCdmaSubscriptionAppIndex;
    public int mGsmUmtsSubscriptionAppIndex;
    public int mImsSubscriptionAppIndex;
    public PinState mUniversalPinState;

    public enum CardState {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.uicc.IccCardStatus.CardState.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.uicc.IccCardStatus.CardState.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.uicc.IccCardStatus.CardState.<clinit>():void");
        }

        boolean isCardPresent() {
            return this == CARDSTATE_PRESENT;
        }
    }

    public enum PinState {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.uicc.IccCardStatus.PinState.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.uicc.IccCardStatus.PinState.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.uicc.IccCardStatus.PinState.<clinit>():void");
        }

        boolean isPermBlocked() {
            return this == PINSTATE_ENABLED_PERM_BLOCKED;
        }

        boolean isPinRequired() {
            return this == PINSTATE_ENABLED_NOT_VERIFIED;
        }

        boolean isPukRequired() {
            return this == PINSTATE_ENABLED_BLOCKED;
        }
    }

    public IccCardStatus() {
    }

    public void setCardState(int state) {
        switch (state) {
            case PduPersister.LOAD_MODE_MMS_COMMON /*0*/:
                this.mCardState = CardState.CARDSTATE_ABSENT;
            case PduPersister.PROC_STATUS_TRANSIENT_FAILURE /*1*/:
                this.mCardState = CardState.CARDSTATE_PRESENT;
            case PduPersister.PROC_STATUS_PERMANENTLY_FAILURE /*2*/:
                this.mCardState = CardState.CARDSTATE_ERROR;
            default:
                throw new RuntimeException("Unrecognized RIL_CardState: " + state);
        }
    }

    public void setUniversalPinState(int state) {
        switch (state) {
            case PduPersister.LOAD_MODE_MMS_COMMON /*0*/:
                this.mUniversalPinState = PinState.PINSTATE_UNKNOWN;
            case PduPersister.PROC_STATUS_TRANSIENT_FAILURE /*1*/:
                this.mUniversalPinState = PinState.PINSTATE_ENABLED_NOT_VERIFIED;
            case PduPersister.PROC_STATUS_PERMANENTLY_FAILURE /*2*/:
                this.mUniversalPinState = PinState.PINSTATE_ENABLED_VERIFIED;
            case PduPersister.PROC_STATUS_COMPLETED /*3*/:
                this.mUniversalPinState = PinState.PINSTATE_DISABLED;
            case CharacterSets.ISO_8859_1 /*4*/:
                this.mUniversalPinState = PinState.PINSTATE_ENABLED_BLOCKED;
            case CharacterSets.ISO_8859_2 /*5*/:
                this.mUniversalPinState = PinState.PINSTATE_ENABLED_PERM_BLOCKED;
            default:
                throw new RuntimeException("Unrecognized RIL_PinState: " + state);
        }
    }

    public String toString() {
        IccCardApplicationStatus app;
        StringBuilder sb = new StringBuilder();
        sb.append("IccCardState {").append(this.mCardState).append(",").append(this.mUniversalPinState).append(",num_apps=").append(this.mApplications.length).append(",gsm_id=").append(this.mGsmUmtsSubscriptionAppIndex);
        if (this.mGsmUmtsSubscriptionAppIndex >= 0 && this.mGsmUmtsSubscriptionAppIndex < CARD_MAX_APPS) {
            app = this.mApplications[this.mGsmUmtsSubscriptionAppIndex];
            if (app == null) {
                app = "null";
            }
            sb.append(app);
        }
        sb.append(",cdma_id=").append(this.mCdmaSubscriptionAppIndex);
        if (this.mCdmaSubscriptionAppIndex >= 0 && this.mCdmaSubscriptionAppIndex < CARD_MAX_APPS) {
            app = this.mApplications[this.mCdmaSubscriptionAppIndex];
            if (app == null) {
                app = "null";
            }
            sb.append(app);
        }
        sb.append(",ims_id=").append(this.mImsSubscriptionAppIndex);
        if (this.mImsSubscriptionAppIndex >= 0 && this.mImsSubscriptionAppIndex < CARD_MAX_APPS) {
            app = this.mApplications[this.mImsSubscriptionAppIndex];
            if (app == null) {
                app = "null";
            }
            sb.append(app);
        }
        sb.append("}");
        return sb.toString();
    }
}
