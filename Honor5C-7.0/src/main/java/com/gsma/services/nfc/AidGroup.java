package com.gsma.services.nfc;

import java.util.ArrayList;
import java.util.List;

public class AidGroup {
    private List<String> mAidList;
    private String mCategory;
    private String mDescription;

    AidGroup() {
        this.mDescription = null;
        this.mCategory = null;
        this.mAidList = new ArrayList();
    }

    AidGroup(String description, String category) {
        this.mDescription = null;
        this.mCategory = null;
        this.mAidList = new ArrayList();
        this.mDescription = description;
        this.mCategory = category;
    }

    public String getCategory() {
        return this.mCategory;
    }

    public String getDescription() {
        return this.mDescription;
    }

    public void addNewAid(String aid) {
        this.mAidList.add(aid);
    }

    public void removeAid(String aid) {
        this.mAidList.remove(aid);
    }

    public List<String> getAidList() {
        return this.mAidList;
    }

    public String[] getAids() {
        return (String[]) this.mAidList.toArray(new String[this.mAidList.size()]);
    }

    public String toString() {
        StringBuffer out = new StringBuffer("AidGroup: ");
        out.append("mDescription: " + this.mDescription);
        out.append(", mCategory: " + this.mCategory);
        for (String aid : this.mAidList) {
            out.append(", aid: " + aid);
        }
        return out.toString();
    }
}
