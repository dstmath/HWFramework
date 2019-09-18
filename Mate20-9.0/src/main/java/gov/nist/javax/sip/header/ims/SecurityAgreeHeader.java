package gov.nist.javax.sip.header.ims;

import java.text.ParseException;
import javax.sip.InvalidArgumentException;
import javax.sip.header.Header;
import javax.sip.header.Parameters;

public interface SecurityAgreeHeader extends Parameters, Header {
    String getAlgorithm();

    String getEncryptionAlgorithm();

    String getMode();

    int getPortClient();

    int getPortServer();

    float getPreference();

    String getProtocol();

    int getSPIClient();

    int getSPIServer();

    String getSecurityMechanism();

    void setAlgorithm(String str) throws ParseException;

    void setEncryptionAlgorithm(String str) throws ParseException;

    void setMode(String str) throws ParseException;

    void setPortClient(int i) throws InvalidArgumentException;

    void setPortServer(int i) throws InvalidArgumentException;

    void setPreference(float f) throws InvalidArgumentException;

    void setProtocol(String str) throws ParseException;

    void setSPIClient(int i) throws InvalidArgumentException;

    void setSPIServer(int i) throws InvalidArgumentException;

    void setSecurityMechanism(String str) throws ParseException;
}
