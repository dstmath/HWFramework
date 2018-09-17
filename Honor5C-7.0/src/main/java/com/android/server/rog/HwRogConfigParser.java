package com.android.server.rog;

import android.content.Context;
import android.provider.Settings.Global;
import android.rog.AppRogInfo;
import android.util.Slog;
import android.util.Xml;
import com.android.server.PPPOEStateMachine;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.shrinker.ProcessStopShrinker;
import huawei.com.android.server.policy.HwGlobalActionsData;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class HwRogConfigParser {
    private static final String CONFIG_FILE_NAME = null;
    private static final String ROG_SWITCH_NAME = "rog_switch";
    private static final String TAG = "HwRogConfigParser";
    private static boolean sRogSwitch;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.rog.HwRogConfigParser.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.rog.HwRogConfigParser.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.rog.HwRogConfigParser.<clinit>():void");
    }

    public HwRogConfigParser(Context context) {
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean parseConfig(Context context, HashMap<String, AppRogInfo> container, float rogAppSclae) {
        FileInputStream fileInputStream = null;
        try {
            FileInputStream fileInputStream2 = new FileInputStream(getConfigFile());
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(fileInputStream2, "UTF-8"));
                int rogEnable = Global.getInt(context.getContentResolver(), ROG_SWITCH_NAME, 0);
                Slog.d(TAG, "parseConfig->rogEnable:" + rogEnable);
                updateRogSwitchState(rogEnable > 0);
                return sRogSwitch;
            } catch (FileNotFoundException e) {
                Slog.w(TAG, "can not find config file");
                return false;
            } catch (UnsupportedEncodingException e2) {
                fileInputStream = fileInputStream2;
                Slog.w(TAG, "create parse UnsupportedEncodingException happended");
                try {
                    fileInputStream.close();
                } catch (Exception e3) {
                    Slog.w(TAG, "close buffered exception happended");
                }
                return false;
            }
        } catch (FileNotFoundException e4) {
            Slog.w(TAG, "can not find config file");
            return false;
        } catch (UnsupportedEncodingException e5) {
            Slog.w(TAG, "create parse UnsupportedEncodingException happended");
            fileInputStream.close();
            return false;
        }
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(br);
            for (int event = parser.getEventType(); event != 1; event = parser.next()) {
                switch (event) {
                    case HwGlobalActionsData.FLAG_AIRPLANEMODE_OFF /*2*/:
                        if ("info".equals(parser.getName())) {
                            boolean z;
                            AppRogInfo newInfo = new AppRogInfo();
                            newInfo.mPackageName = parser.getAttributeValue(null, MemoryConstant.MEM_POLICY_ACTIONNAME);
                            newInfo.mRogMode = Integer.parseInt(parser.getAttributeValue(null, ProcessStopShrinker.MODE_KEY));
                            String value = parser.getAttributeValue(null, "hot_support");
                            if (value != null) {
                                if (Integer.parseInt(value) <= 0) {
                                    z = false;
                                    newInfo.mSupportHotSwitch = z;
                                    newInfo.mRogScale = rogAppSclae;
                                    container.put(newInfo.mPackageName, newInfo);
                                    break;
                                }
                            }
                            z = true;
                            newInfo.mSupportHotSwitch = z;
                            newInfo.mRogScale = rogAppSclae;
                            container.put(newInfo.mPackageName, newInfo);
                        }
                        break;
                }
            }
            try {
                br.close();
            } catch (Exception e6) {
                Slog.w(TAG, "Got execption parsing rog config.", e6);
            }
        } catch (XmlPullParserException e7) {
            Slog.w(TAG, "Got execption parsing rog config.", e7);
        } catch (IOException e8) {
            Slog.w(TAG, "Got execption parsing rog config.", e8);
            try {
                br.close();
            } catch (Exception e62) {
                Slog.w(TAG, "Got execption parsing rog config.", e62);
            }
        } catch (Throwable th) {
            try {
                br.close();
            } catch (Exception e622) {
                Slog.w(TAG, "Got execption parsing rog config.", e622);
            }
        }
    }

    private static void updateRogSwitchState(boolean state) {
        sRogSwitch = state;
    }

    public void updateConfig(Context context, Collection<AppRogInfo> newRogInfos) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            factory.setValidating(true);
            Document doc = factory.newDocumentBuilder().parse(getConfigFile());
            Node infoListRoot = doc.getDocumentElement().getElementsByTagName("info_list").item(0);
            NodeList infoll = infoListRoot.getChildNodes();
            ArrayList<AppRogInfo> pendingInsertInfos = new ArrayList();
            for (AppRogInfo info : newRogInfos) {
                Element target = findInfoNode(infoll, info);
                if (target != null) {
                    target.setAttribute(ProcessStopShrinker.MODE_KEY, AppHibernateCst.INVALID_PKG + info.mRogMode);
                } else {
                    pendingInsertInfos.add(info);
                }
            }
            for (AppRogInfo info2 : pendingInsertInfos) {
                Element newNode = doc.createElement("info");
                newNode.setAttribute(MemoryConstant.MEM_POLICY_ACTIONNAME, info2.mPackageName);
                newNode.setAttribute(ProcessStopShrinker.MODE_KEY, AppHibernateCst.INVALID_PKG + info2.mRogMode);
                infoListRoot.appendChild(newNode);
            }
            saveToConfig(doc);
        } catch (RuntimeException e) {
            Slog.w(TAG, "updateConfig->got RuntimeException:", e);
        } catch (Exception e2) {
            Slog.w(TAG, "updateConfig->got Exception:", e2);
        }
    }

    private Element findInfoNode(NodeList infoll, AppRogInfo info) {
        int length = infoll.getLength();
        for (int i = 0; i < length; i++) {
            Element item = (Element) infoll.item(i);
            if (item.getAttribute(MemoryConstant.MEM_POLICY_ACTIONNAME).equalsIgnoreCase(info.mPackageName)) {
                return item;
            }
        }
        return null;
    }

    public boolean setRogSwitchState(Context context, boolean open) {
        if (sRogSwitch == open) {
            Slog.i(TAG, "setRogSwitchState->already is :" + open);
            return false;
        }
        updateRogSwitchState(open);
        return true;
    }

    public boolean getRogSwitchState(Context context) {
        return sRogSwitch;
    }

    void saveSwitchStateToFile(Context context) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            factory.setValidating(true);
            Document doc = factory.newDocumentBuilder().parse(getConfigFile());
            doc.getDocumentElement().getElementsByTagName("switch").item(0).setTextContent(sRogSwitch ? PPPOEStateMachine.PHASE_INITIALIZE : PPPOEStateMachine.PHASE_DEAD);
            saveToConfig(doc);
        } catch (RuntimeException e) {
            Slog.w(TAG, "saveSwitchStateToFile->got RuntimeException:", e);
        } catch (Exception e2) {
            Slog.w(TAG, "saveSwitchStateToFile->got Exception:", e2);
        }
    }

    private File getConfigFile() {
        Slog.i(TAG, "getConfigFile->CONFIG_FILE_NAME:" + CONFIG_FILE_NAME);
        return new File(CONFIG_FILE_NAME);
    }

    private void saveToConfig(Document doc) {
        doc2XmlFile(doc, CONFIG_FILE_NAME);
    }

    private boolean doc2XmlFile(Document document, String filename) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty("indent", "yes");
            transformer.setOutputProperty("standalone", "no");
            transformer.transform(new DOMSource(document), new StreamResult(new File(filename)));
            return true;
        } catch (RuntimeException ex) {
            Slog.w(TAG, "doc2XmlFile->got exception:", ex);
            return false;
        } catch (Exception e) {
            Slog.w(TAG, "doc2XmlFile->got Exception:", e);
            return true;
        }
    }
}
