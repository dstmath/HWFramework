package com.android.server.wifi.hotspot2.omadm;

import android.util.Log;
import com.android.server.wifi.hotspot2.SystemInfo;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DevInfoMo {
    private static final String MO_NAME = "DevInfo";
    public static final String TAG = "DevInfoMo";
    private static final String TAG_DEVID = "DevID";
    private static final String TAG_DM_VERSION = "DmV";
    private static final String TAG_LANGUAGE = "Lang";
    private static final String TAG_MANUFACTURE = "Man";
    private static final String TAG_MODEL = "Mod";
    public static final String URN = "urn:oma:mo:oma-dm-devinfo:1.0";

    public static String serializeToXml(SystemInfo systemInfo) {
        try {
            MoSerializer moSerializer = new MoSerializer();
            Document doc = moSerializer.createNewDocument();
            Element rootElement = moSerializer.createMgmtTree(doc);
            rootElement.appendChild(moSerializer.writeVersion(doc));
            Element moNode = moSerializer.createNode(doc, MO_NAME);
            moNode.appendChild(moSerializer.createNodeForUrn(doc, URN));
            rootElement.appendChild(moNode);
            rootElement.appendChild(moSerializer.createNodeForValue(doc, TAG_DEVID, systemInfo.getDeviceId()));
            rootElement.appendChild(moSerializer.createNodeForValue(doc, TAG_MANUFACTURE, systemInfo.getDeviceManufacturer()));
            rootElement.appendChild(moSerializer.createNodeForValue(doc, TAG_MODEL, systemInfo.getDeviceModel()));
            rootElement.appendChild(moSerializer.createNodeForValue(doc, TAG_DM_VERSION, "1.2"));
            rootElement.appendChild(moSerializer.createNodeForValue(doc, TAG_LANGUAGE, systemInfo.getLanguage()));
            return moSerializer.serialize(doc);
        } catch (ParserConfigurationException e) {
            Log.e(TAG, "failed to create the MoSerializer: " + e);
            return null;
        }
    }
}
