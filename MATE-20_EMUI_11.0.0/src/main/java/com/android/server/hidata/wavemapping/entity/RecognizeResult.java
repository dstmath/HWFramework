package com.android.server.hidata.wavemapping.entity;

public class RecognizeResult {
    private int allApModelName = 0;
    private int mainApModelName = 0;
    private int mainApRgResult = 0;
    private int rgResult = 0;

    public int getMainApRgResult() {
        return this.mainApRgResult;
    }

    public void setMainApRgResult(int mainApRgResult2) {
        this.mainApRgResult = mainApRgResult2;
    }

    public int getRgResult() {
        return this.rgResult;
    }

    public void setRgResult(int rgResult2) {
        this.rgResult = rgResult2;
    }

    public int getMainApModelName() {
        return this.mainApModelName;
    }

    public void setMainApModelName(int mainApModelName2) {
        this.mainApModelName = mainApModelName2;
    }

    public int getAllApModelName() {
        return this.allApModelName;
    }

    public void setAllApModelName(int allApModelName2) {
        this.allApModelName = allApModelName2;
    }

    public boolean cmpResults(RecognizeResult results) {
        if (this.rgResult == results.getRgResult() && this.mainApRgResult == results.getMainApRgResult() && this.rgResult != 0) {
            return false;
        }
        return true;
    }

    public void copyResults(RecognizeResult results) {
        this.rgResult = results.getRgResult();
        this.mainApRgResult = results.getMainApRgResult();
        this.allApModelName = results.getAllApModelName();
        this.mainApModelName = results.getMainApModelName();
    }

    public String printResults() {
        return "Space ID:{ mainAp=" + this.mainApRgResult + ", allAp=" + this.rgResult + "}";
    }

    public String printResultsDemo() {
        return "Space ID: " + this.rgResult + " ";
    }

    public String toString() {
        return "RecognizeResult{mainApRgResult='" + this.mainApRgResult + "', rgResult='" + this.rgResult + "', mainApModelName='" + this.mainApModelName + "', allApModelName='" + this.allApModelName + "'}";
    }
}
