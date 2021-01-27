package com.huawei.internal.telephony.smartnet;

public class StudyIndexTable {
    String iccidHash;
    int routeId;
    int studyMaxCount;

    public StudyIndexTable(int routeId2, String iccidHash2, int studyMaxCount2) {
        this.routeId = routeId2;
        this.iccidHash = iccidHash2;
        this.studyMaxCount = studyMaxCount2;
    }

    public int getStudyMaxCount() {
        return this.studyMaxCount;
    }

    public int getRouteId() {
        return this.routeId;
    }

    public String getIccidHash() {
        return this.iccidHash;
    }
}
