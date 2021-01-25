package com.gsma.services.nfc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AidGroup {
    private List<String> mAidList = new ArrayList();
    private String mCategory = null;
    private String mDescription = null;

    AidGroup() {
    }

    AidGroup(String description, String category) {
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
        if (aid == null || aid.isEmpty()) {
            throw new IllegalArgumentException("Invalid AID");
        }
        this.mAidList.add(aid.toUpperCase());
    }

    public void removeAid(String aid) {
        if (aid == null || aid.isEmpty()) {
            throw new IllegalArgumentException("Invalid AID");
        }
        this.mAidList.remove(aid);
    }

    public List<String> getAidList() {
        return this.mAidList;
    }

    public String[] getAids() {
        List<String> list = this.mAidList;
        return (String[]) list.toArray(new String[list.size()]);
    }

    public String toString() {
        StringBuffer out = new StringBuffer("AidGroup: ");
        out.append("mDescription: " + this.mDescription);
        out.append(", mCategory: " + this.mCategory);
        Iterator<String> it = this.mAidList.iterator();
        while (it.hasNext()) {
            out.append(", aid: " + it.next());
        }
        return out.toString();
    }
}
