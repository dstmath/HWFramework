package gov.nist.javax.sip.header;

import javax.sip.header.ViaHeader;

public interface ViaHeaderExt extends ViaHeader {
    @Override // javax.sip.header.ViaHeader
    String getSentByField();

    @Override // javax.sip.header.ViaHeader
    String getSentProtocolField();
}
