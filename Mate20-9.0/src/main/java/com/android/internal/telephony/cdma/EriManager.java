package com.android.internal.telephony.cdma;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.PersistableBundle;
import android.telephony.CarrierConfigManager;
import android.telephony.Rlog;
import android.util.Xml;
import com.android.internal.telephony.Phone;
import com.android.internal.util.XmlUtils;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class EriManager {
    private static final boolean DBG = true;
    static final int ERI_FROM_FILE_SYSTEM = 1;
    static final int ERI_FROM_MODEM = 2;
    public static final int ERI_FROM_XML = 0;
    private static final boolean VDBG = false;
    private String LOG_TAG = "EriManager";
    private Context mContext;
    private EriFile mEriFile;
    private int mEriFileSource = 0;
    private boolean mIsEriFileLoaded;
    private final Phone mPhone;

    class EriDisplayInformation {
        int mEriIconIndex;
        int mEriIconMode;
        String mEriIconText;

        EriDisplayInformation(int eriIconIndex, int eriIconMode, String eriIconText) {
            this.mEriIconIndex = eriIconIndex;
            this.mEriIconMode = eriIconMode;
            this.mEriIconText = eriIconText;
        }

        public String toString() {
            return "EriDisplayInformation: { IconIndex: " + this.mEriIconIndex + " EriIconMode: " + this.mEriIconMode + " EriIconText: " + this.mEriIconText + " }";
        }
    }

    class EriFile {
        String[] mCallPromptId = {"", "", ""};
        int mEriFileType = -1;
        int mNumberOfEriEntries = 0;
        HashMap<Integer, EriInfo> mRoamIndTable = new HashMap<>();
        int mVersionNumber = -1;

        EriFile() {
        }
    }

    public EriManager(Phone phone, Context context, int eriFileSource) {
        this.mPhone = phone;
        this.mContext = context;
        this.mEriFileSource = eriFileSource;
        this.mEriFile = new EriFile();
        if (phone != null) {
            this.LOG_TAG += "[SUB" + phone.getPhoneId() + "]";
        }
    }

    public void dispose() {
        this.mEriFile = new EriFile();
        this.mIsEriFileLoaded = false;
    }

    public void loadEriFile() {
        switch (this.mEriFileSource) {
            case 1:
                loadEriFileFromFileSystem();
                return;
            case 2:
                loadEriFileFromModem();
                return;
            default:
                loadEriFileFromXml();
                return;
        }
    }

    private void loadEriFileFromModem() {
    }

    private void loadEriFileFromFileSystem() {
    }

    private void loadEriFileFromXml() {
        XmlPullParser parser;
        FileInputStream stream = null;
        Resources r = this.mContext.getResources();
        String str = null;
        try {
            Rlog.d(this.LOG_TAG, "loadEriFileFromXml: check for alternate file");
            stream = new FileInputStream(r.getString(17039576));
            parser = Xml.newPullParser();
            parser.setInput(stream, null);
            Rlog.d(this.LOG_TAG, "loadEriFileFromXml: opened alternate file");
        } catch (FileNotFoundException e) {
            Rlog.d(this.LOG_TAG, "loadEriFileFromXml: no alternate file");
            parser = null;
        } catch (XmlPullParserException e2) {
            Rlog.d(this.LOG_TAG, "loadEriFileFromXml: no parser for alternate file");
            parser = null;
        }
        if (parser == null) {
            String eriFile = null;
            CarrierConfigManager configManager = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
            if (configManager != null) {
                PersistableBundle b = configManager.getConfigForSubId(this.mPhone.getSubId());
                if (b != null) {
                    eriFile = b.getString("carrier_eri_file_name_string");
                }
            }
            String eriFile2 = eriFile;
            String str2 = this.LOG_TAG;
            Rlog.d(str2, "eriFile = " + eriFile2);
            if (eriFile2 == null) {
                Rlog.e(this.LOG_TAG, "loadEriFileFromXml: Can't find ERI file to load");
                return;
            }
            try {
                parser = Xml.newPullParser();
                parser.setInput(this.mContext.getAssets().open(eriFile2), null);
            } catch (IOException | XmlPullParserException e3) {
                String str3 = this.LOG_TAG;
                Rlog.e(str3, "loadEriFileFromXml: no parser for " + eriFile2 + ". Exception = " + e3.toString());
            }
        }
        try {
            XmlUtils.beginDocument(parser, "EriFile");
            this.mEriFile.mVersionNumber = Integer.parseInt(parser.getAttributeValue(null, "VersionNumber"));
            this.mEriFile.mNumberOfEriEntries = Integer.parseInt(parser.getAttributeValue(null, "NumberOfEriEntries"));
            this.mEriFile.mEriFileType = Integer.parseInt(parser.getAttributeValue(null, "EriFileType"));
            int parsedEriEntries = 0;
            while (true) {
                XmlUtils.nextElement(parser);
                String name = parser.getName();
                if (name == null) {
                    break;
                }
                if (name.equals("CallPromptId")) {
                    int id = Integer.parseInt(parser.getAttributeValue(str, "Id"));
                    String text = parser.getAttributeValue(str, "CallPromptText");
                    if (id < 0 || id > 2) {
                        String str4 = this.LOG_TAG;
                        Rlog.e(str4, "Error Parsing ERI file: found" + id + " CallPromptId");
                    } else {
                        this.mEriFile.mCallPromptId[id] = text;
                    }
                } else if (name.equals("EriInfo")) {
                    int roamingIndicator = Integer.parseInt(parser.getAttributeValue(str, "RoamingIndicator"));
                    int iconIndex = Integer.parseInt(parser.getAttributeValue(str, "IconIndex"));
                    int iconMode = Integer.parseInt(parser.getAttributeValue(str, "IconMode"));
                    String eriText = parser.getAttributeValue(str, "EriText");
                    int callPromptId = Integer.parseInt(parser.getAttributeValue(str, "CallPromptId"));
                    int alertId = Integer.parseInt(parser.getAttributeValue(str, "AlertId"));
                    parsedEriEntries++;
                    HashMap<Integer, EriInfo> hashMap = this.mEriFile.mRoamIndTable;
                    EriInfo eriInfo = r8;
                    Integer valueOf = Integer.valueOf(roamingIndicator);
                    EriInfo eriInfo2 = new EriInfo(roamingIndicator, iconIndex, iconMode, eriText, callPromptId, alertId);
                    hashMap.put(valueOf, eriInfo);
                }
                str = null;
            }
            if (parsedEriEntries != this.mEriFile.mNumberOfEriEntries) {
                String str5 = this.LOG_TAG;
                Rlog.e(str5, "Error Parsing ERI file: " + this.mEriFile.mNumberOfEriEntries + " defined, " + parsedEriEntries + " parsed!");
            }
            String str6 = this.LOG_TAG;
            Rlog.d(str6, "loadEriFileFromXml: eri parsing successful, file loaded. ver = " + this.mEriFile.mVersionNumber + ", # of entries = " + this.mEriFile.mNumberOfEriEntries);
            this.mIsEriFileLoaded = true;
            if ((parser instanceof XmlResourceParser) != 0) {
                ((XmlResourceParser) parser).close();
            }
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e4) {
                }
            }
        } catch (Exception e5) {
            Rlog.e(this.LOG_TAG, "Got exception while loading ERI file.", e5);
            if (parser instanceof XmlResourceParser) {
                ((XmlResourceParser) parser).close();
            }
            if (stream != null) {
                stream.close();
            }
        } catch (Throwable th) {
            Throwable th2 = th;
            if (parser instanceof XmlResourceParser) {
                ((XmlResourceParser) parser).close();
            }
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e6) {
                }
            }
            throw th2;
        }
    }

    public int getEriFileVersion() {
        return this.mEriFile.mVersionNumber;
    }

    public int getEriNumberOfEntries() {
        return this.mEriFile.mNumberOfEriEntries;
    }

    public int getEriFileType() {
        return this.mEriFile.mEriFileType;
    }

    public boolean isEriFileLoaded() {
        return this.mIsEriFileLoaded;
    }

    private EriInfo getEriInfo(int roamingIndicator) {
        if (this.mEriFile.mRoamIndTable.containsKey(Integer.valueOf(roamingIndicator))) {
            return this.mEriFile.mRoamIndTable.get(Integer.valueOf(roamingIndicator));
        }
        return null;
    }

    private EriDisplayInformation getEriDisplayInformation(int roamInd, int defRoamInd) {
        EriDisplayInformation ret;
        EriDisplayInformation eriDisplayInformation;
        if (this.mIsEriFileLoaded) {
            EriInfo eriInfo = getEriInfo(roamInd);
            if (eriInfo != null) {
                return new EriDisplayInformation(eriInfo.iconIndex, eriInfo.iconMode, eriInfo.eriText);
            }
        }
        switch (roamInd) {
            case 0:
                ret = new EriDisplayInformation(0, 0, this.mContext.getText(17041022).toString());
                break;
            case 1:
                ret = new EriDisplayInformation(1, 0, this.mContext.getText(17041023).toString());
                break;
            case 2:
                ret = new EriDisplayInformation(2, 1, this.mContext.getText(17041027).toString());
                break;
            case 3:
                ret = new EriDisplayInformation(roamInd, 0, this.mContext.getText(17041028).toString());
                break;
            case 4:
                ret = new EriDisplayInformation(roamInd, 0, this.mContext.getText(17041029).toString());
                break;
            case 5:
                ret = new EriDisplayInformation(roamInd, 0, this.mContext.getText(17041030).toString());
                break;
            case 6:
                ret = new EriDisplayInformation(roamInd, 0, this.mContext.getText(17041031).toString());
                break;
            case 7:
                ret = new EriDisplayInformation(roamInd, 0, this.mContext.getText(17041032).toString());
                break;
            case 8:
                ret = new EriDisplayInformation(roamInd, 0, this.mContext.getText(17041033).toString());
                break;
            case 9:
                ret = new EriDisplayInformation(roamInd, 0, this.mContext.getText(17041034).toString());
                break;
            case 10:
                ret = new EriDisplayInformation(roamInd, 0, this.mContext.getText(17041024).toString());
                break;
            case 11:
                ret = new EriDisplayInformation(roamInd, 0, this.mContext.getText(17041025).toString());
                break;
            case 12:
                ret = new EriDisplayInformation(roamInd, 0, this.mContext.getText(17041026).toString());
                break;
            default:
                if (this.mIsEriFileLoaded) {
                    EriInfo eriInfo2 = getEriInfo(roamInd);
                    EriInfo defEriInfo = getEriInfo(defRoamInd);
                    if (eriInfo2 != null) {
                        eriDisplayInformation = new EriDisplayInformation(eriInfo2.iconIndex, eriInfo2.iconMode, eriInfo2.eriText);
                    } else if (defEriInfo == null) {
                        String str = this.LOG_TAG;
                        Rlog.e(str, "ERI defRoamInd " + defRoamInd + " not found in ERI file ...on");
                        eriDisplayInformation = new EriDisplayInformation(0, 0, this.mContext.getText(17041022).toString());
                    } else {
                        eriDisplayInformation = new EriDisplayInformation(defEriInfo.iconIndex, defEriInfo.iconMode, defEriInfo.eriText);
                    }
                    ret = eriDisplayInformation;
                    break;
                } else {
                    Rlog.d(this.LOG_TAG, "ERI File not loaded");
                    if (defRoamInd <= 2) {
                        switch (defRoamInd) {
                            case 0:
                                ret = new EriDisplayInformation(0, 0, this.mContext.getText(17041022).toString());
                                break;
                            case 1:
                                ret = new EriDisplayInformation(1, 0, this.mContext.getText(17041023).toString());
                                break;
                            case 2:
                                ret = new EriDisplayInformation(2, 1, this.mContext.getText(17041027).toString());
                                break;
                            default:
                                ret = new EriDisplayInformation(-1, -1, "ERI text");
                                break;
                        }
                    } else {
                        ret = new EriDisplayInformation(2, 1, this.mContext.getText(17041027).toString());
                        break;
                    }
                }
        }
        return ret;
    }

    public int getCdmaEriIconIndex(int roamInd, int defRoamInd) {
        return getEriDisplayInformation(roamInd, defRoamInd).mEriIconIndex;
    }

    public int getCdmaEriIconMode(int roamInd, int defRoamInd) {
        return getEriDisplayInformation(roamInd, defRoamInd).mEriIconMode;
    }

    public String getCdmaEriText(int roamInd, int defRoamInd) {
        return getEriDisplayInformation(roamInd, defRoamInd).mEriIconText;
    }
}
