package gov.nist.javax.sip.header.ims;

import gov.nist.core.NameValue;
import gov.nist.javax.sip.header.ParametersHeader;
import java.text.ParseException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import javax.sip.header.ExtensionHeader;

public class PChargingFunctionAddresses extends ParametersHeader implements PChargingFunctionAddressesHeader, SIPHeaderNamesIms, ExtensionHeader {
    public PChargingFunctionAddresses() {
        super("P-Charging-Function-Addresses");
    }

    /* access modifiers changed from: protected */
    @Override // gov.nist.javax.sip.header.ParametersHeader, gov.nist.javax.sip.header.SIPHeader
    public String encodeBody() {
        StringBuffer encoding = new StringBuffer();
        if (!this.duplicates.isEmpty()) {
            encoding.append(this.duplicates.encode());
        }
        return encoding.toString();
    }

    @Override // gov.nist.javax.sip.header.ims.PChargingFunctionAddressesHeader
    public void setChargingCollectionFunctionAddress(String ccfAddress) throws ParseException {
        if (ccfAddress != null) {
            setMultiParameter(ParameterNamesIms.CCF, ccfAddress);
            return;
        }
        throw new NullPointerException("JAIN-SIP Exception, P-Charging-Function-Addresses, setChargingCollectionFunctionAddress(), the ccfAddress parameter is null.");
    }

    @Override // gov.nist.javax.sip.header.ims.PChargingFunctionAddressesHeader
    public void addChargingCollectionFunctionAddress(String ccfAddress) throws ParseException {
        if (ccfAddress != null) {
            this.parameters.set(ParameterNamesIms.CCF, ccfAddress);
            return;
        }
        throw new NullPointerException("JAIN-SIP Exception, P-Charging-Function-Addresses, setChargingCollectionFunctionAddress(), the ccfAddress parameter is null.");
    }

    @Override // gov.nist.javax.sip.header.ims.PChargingFunctionAddressesHeader
    public void removeChargingCollectionFunctionAddress(String ccfAddress) throws ParseException {
        if (ccfAddress == null) {
            throw new NullPointerException("JAIN-SIP Exception, P-Charging-Function-Addresses, setChargingCollectionFunctionAddress(), the ccfAddress parameter is null.");
        } else if (!delete(ccfAddress, ParameterNamesIms.CCF)) {
            throw new ParseException("CCF Address Not Removed", 0);
        }
    }

    @Override // gov.nist.javax.sip.header.ims.PChargingFunctionAddressesHeader
    public ListIterator getChargingCollectionFunctionAddresses() {
        Iterator li = this.parameters.iterator();
        LinkedList ccfLIST = new LinkedList();
        while (li.hasNext()) {
            NameValue nv = li.next();
            if (nv.getName().equalsIgnoreCase(ParameterNamesIms.CCF)) {
                NameValue ccfNV = new NameValue();
                ccfNV.setName(nv.getName());
                ccfNV.setValueAsObject(nv.getValueAsObject());
                ccfLIST.add(ccfNV);
            }
        }
        return ccfLIST.listIterator();
    }

    @Override // gov.nist.javax.sip.header.ims.PChargingFunctionAddressesHeader
    public void setEventChargingFunctionAddress(String ecfAddress) throws ParseException {
        if (ecfAddress != null) {
            setMultiParameter(ParameterNamesIms.ECF, ecfAddress);
            return;
        }
        throw new NullPointerException("JAIN-SIP Exception, P-Charging-Function-Addresses, setEventChargingFunctionAddress(), the ecfAddress parameter is null.");
    }

    @Override // gov.nist.javax.sip.header.ims.PChargingFunctionAddressesHeader
    public void addEventChargingFunctionAddress(String ecfAddress) throws ParseException {
        if (ecfAddress != null) {
            this.parameters.set(ParameterNamesIms.ECF, ecfAddress);
            return;
        }
        throw new NullPointerException("JAIN-SIP Exception, P-Charging-Function-Addresses, setEventChargingFunctionAddress(), the ecfAddress parameter is null.");
    }

    @Override // gov.nist.javax.sip.header.ims.PChargingFunctionAddressesHeader
    public void removeEventChargingFunctionAddress(String ecfAddress) throws ParseException {
        if (ecfAddress == null) {
            throw new NullPointerException("JAIN-SIP Exception, P-Charging-Function-Addresses, setEventChargingFunctionAddress(), the ecfAddress parameter is null.");
        } else if (!delete(ecfAddress, ParameterNamesIms.ECF)) {
            throw new ParseException("ECF Address Not Removed", 0);
        }
    }

    @Override // gov.nist.javax.sip.header.ims.PChargingFunctionAddressesHeader
    public ListIterator<NameValue> getEventChargingFunctionAddresses() {
        LinkedList<NameValue> listw = new LinkedList<>();
        Iterator li = this.parameters.iterator();
        ListIterator<NameValue> ecfLIST = listw.listIterator();
        while (li.hasNext()) {
            NameValue nv = li.next();
            if (nv.getName().equalsIgnoreCase(ParameterNamesIms.ECF)) {
                NameValue ecfNV = new NameValue();
                ecfNV.setName(nv.getName());
                ecfNV.setValueAsObject(nv.getValueAsObject());
                ecfLIST.add(ecfNV);
            }
        }
        return ecfLIST;
    }

    public boolean delete(String value, String name) {
        Iterator li = this.parameters.iterator();
        boolean removed = false;
        while (li.hasNext()) {
            NameValue nv = li.next();
            if (((String) nv.getValueAsObject()).equalsIgnoreCase(value) && nv.getName().equalsIgnoreCase(name)) {
                li.remove();
                removed = true;
            }
        }
        return removed;
    }

    @Override // javax.sip.header.ExtensionHeader
    public void setValue(String value) throws ParseException {
        throw new ParseException(value, 0);
    }
}
