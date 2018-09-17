package huawei.com.android.internal.telephony;

import android.content.SharedPreferences.Editor;
import android.os.SystemProperties;
import android.provider.SettingsEx.Systemex;
import android.util.Log;
import com.android.internal.telephony.PhoneFactory;
import java.util.ArrayList;

public class RoamingBroker {
    private static final boolean DBG = true;
    private static final String LOG_TAG = "GSM";
    public static final String PreviousIccId = "persist.radio.previousiccid";
    public static final String PreviousImsi = "persist.radio.previousimsi";
    public static final String PreviousOperator = "persist.radio.previousopcode";
    public static final String RBActivated = "gsm.RBActivated";
    private static final String RBActivated_flag_on = "true";
    private static ArrayList<RoamingBrokerSequence> mRBSequenceList;
    private String mCurrentIccid;
    private String mCurrentImsi;
    private String mCurrentOp;
    private String mPreviousIccid;
    private String mPreviousOp;
    private int mSlotId;
    private String mVoicemail;

    private static class HelperHolder {
        private static RoamingBroker mRoamingBroker0;
        private static RoamingBroker mRoamingBroker1;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: huawei.com.android.internal.telephony.RoamingBroker.HelperHolder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: huawei.com.android.internal.telephony.RoamingBroker.HelperHolder.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: huawei.com.android.internal.telephony.RoamingBroker.HelperHolder.<clinit>():void");
        }

        private HelperHolder() {
        }
    }

    private static class RoamingBrokerSequence {
        static final int RBSequenceLength = 3;
        String before_rb_mccmnc;
        String name;
        String rb_mccmnc;
        String rb_voicemail;

        /* synthetic */ RoamingBrokerSequence(RoamingBrokerSequence roamingBrokerSequence) {
            this();
        }

        private RoamingBrokerSequence() {
            this.name = "";
            this.before_rb_mccmnc = "";
            this.rb_mccmnc = "";
            this.rb_voicemail = "";
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: huawei.com.android.internal.telephony.RoamingBroker.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: huawei.com.android.internal.telephony.RoamingBroker.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: huawei.com.android.internal.telephony.RoamingBroker.<clinit>():void");
    }

    /* synthetic */ RoamingBroker(int slotId, RoamingBroker roamingBroker) {
        this(slotId);
    }

    private static void log(String text) {
        Log.d(LOG_TAG, "[RoamingBroker] " + text);
    }

    private static void loge(String text, Exception e) {
        Log.e(LOG_TAG, "[RoamingBroker] " + text + e);
    }

    private String printIccid(String iccid) {
        if (this.mPreviousIccid == null) {
            return "null";
        }
        if (this.mPreviousIccid.length() <= 6) {
            return "less than 6 digits";
        }
        return this.mPreviousIccid.substring(0, 6) + new String(new char[(this.mPreviousIccid.length() - 6)]).replace('\u0000', '*');
    }

    private RoamingBroker() {
        this.mCurrentImsi = null;
        this.mSlotId = 0;
        this.mPreviousIccid = null;
        this.mPreviousOp = null;
        this.mCurrentIccid = null;
        this.mCurrentOp = null;
        this.mVoicemail = null;
        this.mPreviousOp = SystemProperties.get(PreviousOperator, "");
        log(String.format("Previously saved operator code is %s", new Object[]{this.mPreviousOp}));
        this.mPreviousIccid = SystemProperties.get(PreviousIccId, "");
        log(String.format("Previously saved Iccid is %s", new Object[]{printIccid(this.mPreviousIccid)}));
    }

    private void loadRBSequenceMap() {
        mRBSequenceList.clear();
        try {
            String data = Systemex.getString(PhoneFactory.getDefaultPhone().getContext().getContentResolver(), "roamingBrokerSequenceList");
            log("get raw RB list with voicemail" + data);
            if (data != null) {
                for (String s : data.split("\\|")) {
                    if (s != null) {
                        String[] tmp = s.split(",");
                        if (tmp.length >= 3) {
                            RoamingBrokerSequence rbs = new RoamingBrokerSequence();
                            rbs.name = tmp[0];
                            rbs.before_rb_mccmnc = tmp[1];
                            rbs.rb_mccmnc = tmp[2];
                            if (tmp.length > 3) {
                                rbs.rb_voicemail = tmp[3];
                            }
                            mRBSequenceList.add(rbs);
                        } else {
                            log("RB list contains invalid config: " + s);
                        }
                    }
                }
            }
        } catch (Exception e) {
            loge("Failed to load RB Sequence list. ", e);
        }
    }

    private void unloadRBSequenceMap() {
        mRBSequenceList.clear();
    }

    public static RoamingBroker getDefault() {
        return HelperHolder.mRoamingBroker0;
    }

    public void setOperator(String operatorCode) {
        log("received operatorCode of value: " + operatorCode);
        if (operatorCode != null && !operatorCode.equals(this.mCurrentOp)) {
            this.mCurrentOp = operatorCode;
        }
    }

    public void setIccId(String IccId) {
        log("received IccId of value: " + printIccid(IccId));
        if (IccId != null && !IccId.equals(this.mCurrentIccid)) {
            this.mCurrentIccid = IccId;
        }
    }

    private void setData() {
        if (this.mCurrentIccid != null && this.mCurrentOp != null && this.mCurrentImsi != null) {
            SystemProperties.set(RBActivated + this.mSlotId, "");
            Editor editor = PhoneFactory.getDefaultPhone().getContext().getSharedPreferences("imsi", 0).edit();
            if (this.mCurrentOp.equals(this.mPreviousOp)) {
                if (!this.mCurrentIccid.equals(this.mPreviousIccid)) {
                    SystemProperties.set(PreviousIccId + this.mSlotId, this.mCurrentIccid);
                    editor.putString(PreviousImsi + this.mSlotId, this.mCurrentImsi);
                    editor.commit();
                    this.mPreviousIccid = this.mCurrentIccid;
                    log(String.format("different sim card with same operatorCode %s. Set iccId: %s for roaming broker", new Object[]{this.mPreviousOp, printIccid(this.mPreviousIccid)}));
                }
            } else if (!this.mCurrentIccid.equals(this.mPreviousIccid)) {
                SystemProperties.set(PreviousOperator + this.mSlotId, this.mCurrentOp);
                SystemProperties.set(PreviousIccId + this.mSlotId, this.mCurrentIccid);
                editor.putString(PreviousImsi + this.mSlotId, this.mCurrentImsi);
                editor.commit();
                this.mPreviousIccid = this.mCurrentIccid;
                this.mPreviousOp = this.mCurrentOp;
                log(String.format("different sim card. Set operatorCode: %s, iccId: %s for roaming broker", new Object[]{this.mPreviousOp, printIccid(this.mPreviousIccid)}));
            } else if (isValidRBSequence()) {
                SystemProperties.set(RBActivated + this.mSlotId, RBActivated_flag_on);
            } else {
                SystemProperties.set(PreviousOperator + this.mSlotId, this.mCurrentOp);
                editor.putString(PreviousImsi + this.mSlotId, this.mCurrentImsi);
                editor.commit();
                this.mPreviousOp = this.mCurrentOp;
                log(String.format("same sim card. Set operatorCode: %s for roaming broker", new Object[]{this.mPreviousOp}));
            }
        }
    }

    private boolean isValidRBSequence() {
        boolean result = false;
        if (!(this.mPreviousOp == null || this.mCurrentOp == null)) {
            loadRBSequenceMap();
            for (RoamingBrokerSequence rbs : mRBSequenceList) {
                if (this.mCurrentOp.equals(rbs.rb_mccmnc) && this.mPreviousOp.equals(rbs.before_rb_mccmnc)) {
                    result = DBG;
                    this.mVoicemail = rbs.rb_voicemail;
                    log(rbs.name + " Roaming broker is activated");
                    break;
                }
            }
            unloadRBSequenceMap();
        }
        log("isValidRBSequence: " + result);
        return result;
    }

    public static boolean isRoamingBrokerActivated() {
        boolean result = RBActivated_flag_on.equals(SystemProperties.get("gsm.RBActivated0"));
        log("isRoamingBrokerActivated returns " + result);
        return result;
    }

    public static String updateSelectionForRoamingBroker(String selection) {
        log("updateSelection for: " + selection);
        String result = selection.replaceAll("numeric[ ]*=[ ]*[\"|']" + getDefault().mCurrentOp + "[\"|']", "numeric=\"" + getDefault().mPreviousOp + "\"");
        log("updated Selection: " + result);
        return result;
    }

    public static String getRBOperatorNumeric() {
        return getDefault().mPreviousOp;
    }

    public static String getRBVoicemail() {
        return getDefault().mVoicemail;
    }

    private RoamingBroker(int slotId) {
        this.mCurrentImsi = null;
        this.mSlotId = 0;
        this.mPreviousIccid = null;
        this.mPreviousOp = null;
        this.mCurrentIccid = null;
        this.mCurrentOp = null;
        this.mVoicemail = null;
        this.mSlotId = slotId;
        log("###RoamingBroker init,mSlotId = " + slotId);
        this.mPreviousOp = SystemProperties.get(PreviousOperator + slotId, "");
        log(String.format("Previously saved operator code is %s", new Object[]{this.mPreviousOp}));
        this.mPreviousIccid = SystemProperties.get(PreviousIccId + slotId, "");
        log(String.format("Previously saved Iccid is %s", new Object[]{printIccid(this.mPreviousIccid)}));
    }

    public static RoamingBroker getDefault(Integer slotId) {
        if (slotId.intValue() == 0) {
            return HelperHolder.mRoamingBroker0;
        }
        return HelperHolder.mRoamingBroker1;
    }

    public void setImsi(String Imsi) {
        if (Imsi != null && !Imsi.equals(this.mCurrentImsi)) {
            this.mCurrentImsi = Imsi;
            setData();
        }
    }

    public boolean isRoamingBrokerActivated(Integer slotId) {
        boolean result = RBActivated_flag_on.equals(SystemProperties.get(RBActivated + slotId));
        log("###isRoamingBrokerActivated returns " + result);
        return result;
    }

    public String updateSelectionForRoamingBroker(String selection, int slotId) {
        log("###updateSelection for: " + selection);
        String result = selection.replaceAll("numeric[ ]*=[ ]*[\"|']" + getDefault(Integer.valueOf(slotId)).mCurrentOp + "[\"|']", "numeric=\"" + getDefault(Integer.valueOf(slotId)).mPreviousOp + "\"");
        log("###updated Selection: " + result);
        return result;
    }

    public String getRBImsi() {
        return PhoneFactory.getDefaultPhone().getContext().getSharedPreferences("imsi", 0).getString(PreviousImsi + this.mSlotId, null);
    }

    public String getRBOperatorNumeric(Integer slotId) {
        return getDefault(slotId).mPreviousOp;
    }

    public String getRBVoicemail(Integer slotId) {
        return getDefault(slotId).mVoicemail;
    }
}
