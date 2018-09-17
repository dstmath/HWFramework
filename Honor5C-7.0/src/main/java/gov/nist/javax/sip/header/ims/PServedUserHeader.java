package gov.nist.javax.sip.header.ims;

public interface PServedUserHeader {
    public static final String NAME = "P-Served-User";

    String getRegistrationState();

    String getSessionCase();

    void setRegistrationState(String str);

    void setSessionCase(String str);
}
