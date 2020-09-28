package com.huawei.android.hardware.qcomfmradio;

import java.util.ArrayList;
import java.util.List;

/* access modifiers changed from: package-private */
public class Spur {
    private byte NoOfSpursToTrack;
    private int SpurFreq;
    private List<SpurDetails> spurDetailsList;

    Spur() {
    }

    Spur(int SpurFreq2, byte NoOfSpursToTrack2, List<SpurDetails> spurDetailsList2) {
        this.SpurFreq = SpurFreq2;
        this.NoOfSpursToTrack = NoOfSpursToTrack2;
        this.spurDetailsList = spurDetailsList2;
    }

    public int getSpurFreq() {
        return this.SpurFreq;
    }

    public void setSpurFreq(int spurFreq) {
        this.SpurFreq = spurFreq;
    }

    public byte getNoOfSpursToTrack() {
        return this.NoOfSpursToTrack;
    }

    public void setNoOfSpursToTrack(byte noOfSpursToTrack) {
        this.NoOfSpursToTrack = noOfSpursToTrack;
    }

    public List<SpurDetails> getSpurDetailsList() {
        return this.spurDetailsList;
    }

    public void setSpurDetailsList(List<SpurDetails> spurDetailsList2) {
        this.spurDetailsList = spurDetailsList2;
    }

    public void addSpurDetails(SpurDetails spurDetails) {
        if (this.spurDetailsList == null) {
            this.spurDetailsList = new ArrayList();
        }
        this.spurDetailsList.add(spurDetails);
    }
}
