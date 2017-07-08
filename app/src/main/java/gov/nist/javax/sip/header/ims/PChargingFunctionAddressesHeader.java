package gov.nist.javax.sip.header.ims;

import java.text.ParseException;
import java.util.ListIterator;
import javax.sip.header.Header;
import javax.sip.header.Parameters;

public interface PChargingFunctionAddressesHeader extends Parameters, Header {
    public static final String NAME = "P-Charging-Function-Addresses";

    void addChargingCollectionFunctionAddress(String str) throws ParseException;

    void addEventChargingFunctionAddress(String str) throws ParseException;

    ListIterator getChargingCollectionFunctionAddresses();

    ListIterator getEventChargingFunctionAddresses();

    void removeChargingCollectionFunctionAddress(String str) throws ParseException;

    void removeEventChargingFunctionAddress(String str) throws ParseException;

    void setChargingCollectionFunctionAddress(String str) throws ParseException;

    void setEventChargingFunctionAddress(String str) throws ParseException;
}
