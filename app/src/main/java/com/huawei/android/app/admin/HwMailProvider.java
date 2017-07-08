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

    public HwMailProvider(String id, String label, String domain, String incominguri, String incomingusername, String incomingfield, String outgoinguri, String outgoingusername) {
        setId(id);
        setLabel(label);
        setDomain(domain);
        setIncominguri(incominguri);
        setIncomingusername(incomingusername);
        setIncomingfield(incomingfield);
        setOutgoinguri(outgoinguri);
        setOutgoingusername(outgoingusername);
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return this.label;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getDomain() {
        return this.domain;
    }

    public void setIncominguri(String incominguri) {
        this.incominguri = incominguri;
    }

    public String getIncominguri() {
        return this.incominguri;
    }

    public void setIncomingusername(String incomingusername) {
        this.incomingusername = incomingusername;
    }

    public String getIncomingusername() {
        return this.incomingusername;
    }

    public void setIncomingfield(String incomingfield) {
        this.incomingfield = incomingfield;
    }

    public String getIncomingfield() {
        return this.incomingfield;
    }

    public void setOutgoinguri(String outgoinguri) {
        this.outgoinguri = outgoinguri;
    }

    public String getOutgoinguri() {
        return this.outgoinguri;
    }

    public void setOutgoingusername(String outgoingusername) {
        this.outgoingusername = outgoingusername;
    }

    public String getOutgoingusername() {
        return this.outgoingusername;
    }
}
