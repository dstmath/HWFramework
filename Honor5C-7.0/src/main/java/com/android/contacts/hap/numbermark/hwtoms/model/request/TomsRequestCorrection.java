package com.android.contacts.hap.numbermark.hwtoms.model.request;

public class TomsRequestCorrection extends TomsRequestBase {
    private String mytel;
    private String newAddr;
    private String newName;
    private String newTel;
    private String oldAddr;
    private String oldName;
    private String oldTel;
    private String problem;

    public String getOldName() {
        return this.oldName;
    }

    public void setOldName(String oldName) {
        this.oldName = oldName;
    }

    public String getNewName() {
        return this.newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }

    public String getOldAddr() {
        return this.oldAddr;
    }

    public void setOldAddr(String oldAddr) {
        this.oldAddr = oldAddr;
    }

    public String getNewAddr() {
        return this.newAddr;
    }

    public void setNewAddr(String newAddr) {
        this.newAddr = newAddr;
    }

    public String getOldTel() {
        return this.oldTel;
    }

    public void setOldTel(String oldTel) {
        this.oldTel = oldTel;
    }

    public String getNewTel() {
        return this.newTel;
    }

    public void setNewTel(String newTel) {
        this.newTel = newTel;
    }

    public String getProblem() {
        return this.problem;
    }

    public void setProblem(String problem) {
        this.problem = problem;
    }

    public String getMytel() {
        return this.mytel;
    }

    public void setMytel(String mytel) {
        this.mytel = mytel;
    }
}
