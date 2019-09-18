package com.huawei.android.app.admin;

public class HwMailProvider {
    public String domain;
    public String id;
    public String incomingfield;
    public String incominguri;
    public String incomingusername;
    public String label;
    public String outgoinguri;
    public String outgoingusername;

    public HwMailProvider() {
    }

    public HwMailProvider(String id2, String label2, String domain2, String incominguri2, String incomingusername2, String incomingfield2, String outgoinguri2, String outgoingusername2) {
        setId(id2);
        setLabel(label2);
        setDomain(domain2);
        setIncominguri(incominguri2);
        setIncomingusername(incomingusername2);
        setIncomingfield(incomingfield2);
        setOutgoinguri(outgoinguri2);
        setOutgoingusername(outgoingusername2);
    }

    public void setId(String id2) {
        this.id = id2;
    }

    public String getId() {
        return this.id;
    }

    public void setLabel(String label2) {
        this.label = label2;
    }

    public String getLabel() {
        return this.label;
    }

    public void setDomain(String domain2) {
        this.domain = domain2;
    }

    public String getDomain() {
        return this.domain;
    }

    public void setIncominguri(String incominguri2) {
        this.incominguri = incominguri2;
    }

    public String getIncominguri() {
        return this.incominguri;
    }

    public void setIncomingusername(String incomingusername2) {
        this.incomingusername = incomingusername2;
    }

    public String getIncomingusername() {
        return this.incomingusername;
    }

    public void setIncomingfield(String incomingfield2) {
        this.incomingfield = incomingfield2;
    }

    public String getIncomingfield() {
        return this.incomingfield;
    }

    public void setOutgoinguri(String outgoinguri2) {
        this.outgoinguri = outgoinguri2;
    }

    public String getOutgoinguri() {
        return this.outgoinguri;
    }

    public void setOutgoingusername(String outgoingusername2) {
        this.outgoingusername = outgoingusername2;
    }

    public String getOutgoingusername() {
        return this.outgoingusername;
    }
}
