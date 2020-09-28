package javax.sip.address;

import java.text.ParseException;

public interface AddressFactory {
    Address createAddress();

    Address createAddress(String str) throws ParseException;

    Address createAddress(String str, URI uri) throws ParseException;

    Address createAddress(URI uri);

    SipURI createSipURI(String str) throws ParseException;

    SipURI createSipURI(String str, String str2) throws ParseException;

    TelURL createTelURL(String str) throws ParseException;

    URI createURI(String str) throws ParseException;
}
