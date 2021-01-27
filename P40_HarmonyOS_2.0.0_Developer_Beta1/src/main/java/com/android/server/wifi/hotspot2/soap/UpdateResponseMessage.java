package com.android.server.wifi.hotspot2.soap;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;

public class UpdateResponseMessage {
    public static SoapSerializationEnvelope serializeToSoapEnvelope(String sessionId, boolean isError) {
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER12);
        envelope.implicitTypes = true;
        envelope.setAddAdornments(false);
        SoapObject requestObject = new SoapObject(SoapEnvelope.NS20, SppConstants.METHOD_UPDATE_RESPONSE);
        requestObject.addAttribute(SoapEnvelope.NS20, SppConstants.ATTRIBUTE_SPP_VERSION, SppConstants.SUPPORTED_SPP_VERSION);
        requestObject.addAttribute(SoapEnvelope.NS20, SppConstants.ATTRIBUTE_SESSION_ID, sessionId);
        if (isError) {
            requestObject.addAttribute(SoapEnvelope.NS20, SppConstants.ATTRIBUTE_SPP_STATUS, SppConstants.mapStatusIntToString(6));
            SoapObject sppError = new SoapObject(SoapEnvelope.NS20, SppConstants.PROPERTY_SPP_ERROR);
            sppError.addAttribute(SppConstants.ATTRIBUTE_ERROR_CODE, SppConstants.mapErrorIntToString(10));
            requestObject.addProperty(SoapEnvelope.NS20, SppConstants.PROPERTY_SPP_ERROR, sppError);
        } else {
            requestObject.addAttribute(SoapEnvelope.NS20, SppConstants.ATTRIBUTE_SPP_STATUS, SppConstants.mapStatusIntToString(0));
        }
        envelope.setOutputSoapObject(requestObject);
        return envelope;
    }
}
