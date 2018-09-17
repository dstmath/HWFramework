package gov.nist.javax.sip.header;

import javax.sip.header.ViaHeader;

public interface ViaHeaderExt extends ViaHeader {
    String getSentByField();

    String getSentProtocolField();
}
