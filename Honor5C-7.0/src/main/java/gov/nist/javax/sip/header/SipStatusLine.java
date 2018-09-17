package gov.nist.javax.sip.header;

public interface SipStatusLine {
    String getReasonPhrase();

    String getSipVersion();

    int getStatusCode();

    String getVersionMajor();

    String getVersionMinor();

    void setReasonPhrase(String str);

    void setSipVersion(String str);

    void setStatusCode(int i);
}
