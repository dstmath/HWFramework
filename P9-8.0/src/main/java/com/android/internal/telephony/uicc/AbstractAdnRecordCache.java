package com.android.internal.telephony.uicc;

import android.os.Handler;
import android.os.Message;
import com.android.internal.telephony.gsm.UsimPhoneBookManager;
import java.util.ArrayList;

public class AbstractAdnRecordCache extends Handler {
    protected void updateAdnRecordId(AdnRecord adn, int efid, int index) {
    }

    public int getUsimExtensionEfForAdnEf(int AdnEfid) {
        return 0;
    }

    public void updateUsimAdnByIndexHW(int efid, AdnRecord newAdn, int sEf_id, int recordIndex, String pin2, Message response) {
    }

    public int getAnrCountHW() {
        return 0;
    }

    public int getEmailCountHW() {
        return 0;
    }

    public int getSpareAnrCountHW() {
        return 0;
    }

    public int getSpareEmailCountHW() {
        return 0;
    }

    public int getSpareExt1CountHW() {
        return 0;
    }

    public ArrayList<AdnRecord> getAdnFilesForSim() {
        return null;
    }

    public int getAdnCountHW() {
        return 0;
    }

    public void setAdnCountHW(int count) {
    }

    public int getUsimAdnCountHW() {
        return 0;
    }

    public int[] getRecordsSizeHW() {
        return new int[0];
    }

    public UsimPhoneBookManager getUsimPhoneBookManager() {
        return null;
    }

    public void updateUsimPhoneBookRecord(AdnRecord adn, int efid, int index) {
    }
}
