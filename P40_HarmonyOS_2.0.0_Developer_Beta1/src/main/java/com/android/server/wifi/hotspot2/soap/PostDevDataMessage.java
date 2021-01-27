package com.android.server.wifi.hotspot2.soap;

import android.content.Context;
import com.android.server.wifi.hotspot2.SystemInfo;
import com.android.server.wifi.hotspot2.omadm.DevDetailMo;
import com.android.server.wifi.hotspot2.omadm.DevInfoMo;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;

public class PostDevDataMessage {
    public static SoapSerializationEnvelope serializeToSoapEnvelope(Context context, SystemInfo info, String redirectUri, String requestReason, String sessionId) {
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER12);
        envelope.implicitTypes = true;
        envelope.setAddAdornments(false);
        SoapObject requestObject = new SoapObject(SoapEnvelope.NS20, SppConstants.METHOD_POST_DEV_DATA);
        requestObject.addAttribute(SoapEnvelope.NS20, SppConstants.ATTRIBUTE_SPP_VERSION, SppConstants.SUPPORTED_SPP_VERSION);
        requestObject.addAttribute(SppConstants.ATTRIBUTE_REQUEST_REASON, requestReason);
        requestObject.addAttribute(SppConstants.ATTRIBUTE_REDIRECT_URI, redirectUri);
        if (sessionId != null) {
            requestObject.addAttribute(SoapEnvelope.NS20, SppConstants.ATTRIBUTE_SESSION_ID, sessionId);
        }
        requestObject.addProperty(SoapEnvelope.NS20, SppConstants.PROPERTY_SUPPORTED_SPP_VERSIONS, SppConstants.SUPPORTED_SPP_VERSION);
        requestObject.addProperty(SoapEnvelope.NS20, SppConstants.PROPERTY_SUPPORTED_MO_LIST, String.join(" ", SppConstants.SUPPORTED_MO_LIST));
        addMoContainer(requestObject, DevInfoMo.URN, DevInfoMo.serializeToXml(info));
        addMoContainer(requestObject, DevDetailMo.URN, DevDetailMo.serializeToXml(context, info, redirectUri));
        envelope.setOutputSoapObject(requestObject);
        return envelope;
    }

    private static void addMoContainer(SoapObject soapObject, String moUrn, String moText) {
        SoapPrimitive moContainer = new SoapPrimitive(SoapEnvelope.NS20, SppConstants.PROPERTY_MO_CONTAINER, moText);
        moContainer.addAttribute(SoapEnvelope.NS20, "moURN", moUrn);
        soapObject.addProperty(SoapEnvelope.NS20, SppConstants.PROPERTY_MO_CONTAINER, moContainer);
    }
}
