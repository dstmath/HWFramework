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
import com.google.android.mms.pdu.CharacterSets;
import com.google.android.mms.pdu.PduPersister;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class EriManager {
    private static final boolean DBG = true;
    static final int ERI_FROM_FILE_SYSTEM = 1;
    static final int ERI_FROM_MODEM = 2;
    public static final int ERI_FROM_XML = 0;
    private static final boolean VDBG = false;
    private String LOG_TAG;
    private Context mContext;
    private EriFile mEriFile;
    private int mEriFileSource;
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
        String[] mCallPromptId;
        int mEriFileType;
        int mNumberOfEriEntries;
        HashMap<Integer, EriInfo> mRoamIndTable;
        int mVersionNumber;

        EriFile() {
            this.mVersionNumber = -1;
            this.mNumberOfEriEntries = 0;
            this.mEriFileType = -1;
            this.mCallPromptId = new String[]{"", "", ""};
            this.mRoamIndTable = new HashMap();
        }
    }

    public EriManager(Phone phone, Context context, int eriFileSource) {
        this.LOG_TAG = "EriManager";
        this.mEriFileSource = 0;
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
            case ERI_FROM_FILE_SYSTEM /*1*/:
                loadEriFileFromFileSystem();
            case ERI_FROM_MODEM /*2*/:
                loadEriFileFromModem();
            default:
                loadEriFileFromXml();
        }
    }

    private void loadEriFileFromModem() {
    }

    private void loadEriFileFromFileSystem() {
    }

    private void loadEriFileFromXml() {
        XmlPullParser parser;
        InputStream stream;
        String eriFile;
        CarrierConfigManager configManager;
        PersistableBundle b;
        EriFile eriFile2;
        int parsedEriEntries;
        String name;
        int roamingIndicator;
        int iconIndex;
        int iconMode;
        String eriText;
        int callPromptId;
        int alertId;
        HashMap hashMap;
        Integer valueOf;
        int id;
        String text;
        int i;
        FileInputStream fileInputStream = null;
        Resources r = this.mContext.getResources();
        try {
            Rlog.d(this.LOG_TAG, "loadEriFileFromXml: check for alternate file");
            InputStream fileInputStream2 = new FileInputStream(r.getString(17040480));
            try {
                parser = Xml.newPullParser();
                parser.setInput(fileInputStream2, null);
                Rlog.d(this.LOG_TAG, "loadEriFileFromXml: opened alternate file");
                fileInputStream = fileInputStream2;
            } catch (FileNotFoundException e) {
                stream = fileInputStream2;
                Rlog.d(this.LOG_TAG, "loadEriFileFromXml: no alternate file");
                parser = null;
                if (parser == null) {
                    eriFile = null;
                    configManager = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
                    if (configManager != null) {
                        b = configManager.getConfigForSubId(this.mPhone.getSubId());
                        if (b != null) {
                            eriFile = b.getString("carrier_eri_file_name_string");
                        }
                    }
                    Rlog.d(this.LOG_TAG, "eriFile = " + eriFile);
                    if (eriFile == null) {
                        try {
                            parser = Xml.newPullParser();
                            parser.setInput(this.mContext.getAssets().open(eriFile), null);
                        } catch (Exception e2) {
                            Rlog.e(this.LOG_TAG, "loadEriFileFromXml: no parser for " + eriFile + ". Exception = " + e2.toString());
                        }
                    } else {
                        Rlog.e(this.LOG_TAG, "loadEriFileFromXml: Can't find ERI file to load");
                        return;
                    }
                }
                XmlUtils.beginDocument(parser, "EriFile");
                eriFile2 = this.mEriFile;
                eriFile2.mVersionNumber = Integer.parseInt(parser.getAttributeValue(null, "VersionNumber"));
                eriFile2 = this.mEriFile;
                eriFile2.mNumberOfEriEntries = Integer.parseInt(parser.getAttributeValue(null, "NumberOfEriEntries"));
                eriFile2 = this.mEriFile;
                eriFile2.mEriFileType = Integer.parseInt(parser.getAttributeValue(null, "EriFileType"));
                parsedEriEntries = 0;
                while (true) {
                    XmlUtils.nextElement(parser);
                    name = parser.getName();
                    if (name == null) {
                        if (name.equals("CallPromptId")) {
                            if (name.equals("EriInfo")) {
                                roamingIndicator = Integer.parseInt(parser.getAttributeValue(null, "RoamingIndicator"));
                                iconIndex = Integer.parseInt(parser.getAttributeValue(null, "IconIndex"));
                                iconMode = Integer.parseInt(parser.getAttributeValue(null, "IconMode"));
                                eriText = parser.getAttributeValue(null, "EriText");
                                callPromptId = Integer.parseInt(parser.getAttributeValue(null, "CallPromptId"));
                                alertId = Integer.parseInt(parser.getAttributeValue(null, "AlertId"));
                                parsedEriEntries += ERI_FROM_FILE_SYSTEM;
                                hashMap = this.mEriFile.mRoamIndTable;
                                valueOf = Integer.valueOf(roamingIndicator);
                                hashMap.put(r26, new EriInfo(roamingIndicator, iconIndex, iconMode, eriText, callPromptId, alertId));
                            }
                        } else {
                            id = Integer.parseInt(parser.getAttributeValue(null, "Id"));
                            text = parser.getAttributeValue(null, "CallPromptText");
                            if (id >= 0) {
                            }
                            Rlog.e(this.LOG_TAG, "Error Parsing ERI file: found" + id + " CallPromptId");
                        }
                    } else {
                        break;
                        if (parsedEriEntries != this.mEriFile.mNumberOfEriEntries) {
                            Rlog.e(this.LOG_TAG, "Error Parsing ERI file: " + this.mEriFile.mNumberOfEriEntries + " defined, " + parsedEriEntries + " parsed!");
                        }
                        i = this.mEriFile.mVersionNumber;
                        Rlog.d(this.LOG_TAG, "loadEriFileFromXml: eri parsing successful, file loaded. ver = " + r0 + ", # of entries = " + this.mEriFile.mNumberOfEriEntries);
                        this.mIsEriFileLoaded = DBG;
                        if (parser instanceof XmlResourceParser) {
                            ((XmlResourceParser) parser).close();
                        }
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (IOException e3) {
                            }
                        }
                    }
                }
            } catch (XmlPullParserException e4) {
                stream = fileInputStream2;
                Rlog.d(this.LOG_TAG, "loadEriFileFromXml: no parser for alternate file");
                parser = null;
                if (parser == null) {
                    eriFile = null;
                    configManager = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
                    if (configManager != null) {
                        b = configManager.getConfigForSubId(this.mPhone.getSubId());
                        if (b != null) {
                            eriFile = b.getString("carrier_eri_file_name_string");
                        }
                    }
                    Rlog.d(this.LOG_TAG, "eriFile = " + eriFile);
                    if (eriFile == null) {
                        Rlog.e(this.LOG_TAG, "loadEriFileFromXml: Can't find ERI file to load");
                        return;
                    }
                    parser = Xml.newPullParser();
                    parser.setInput(this.mContext.getAssets().open(eriFile), null);
                }
                XmlUtils.beginDocument(parser, "EriFile");
                eriFile2 = this.mEriFile;
                eriFile2.mVersionNumber = Integer.parseInt(parser.getAttributeValue(null, "VersionNumber"));
                eriFile2 = this.mEriFile;
                eriFile2.mNumberOfEriEntries = Integer.parseInt(parser.getAttributeValue(null, "NumberOfEriEntries"));
                eriFile2 = this.mEriFile;
                eriFile2.mEriFileType = Integer.parseInt(parser.getAttributeValue(null, "EriFileType"));
                parsedEriEntries = 0;
                while (true) {
                    XmlUtils.nextElement(parser);
                    name = parser.getName();
                    if (name == null) {
                        break;
                        if (parsedEriEntries != this.mEriFile.mNumberOfEriEntries) {
                            Rlog.e(this.LOG_TAG, "Error Parsing ERI file: " + this.mEriFile.mNumberOfEriEntries + " defined, " + parsedEriEntries + " parsed!");
                        }
                        i = this.mEriFile.mVersionNumber;
                        Rlog.d(this.LOG_TAG, "loadEriFileFromXml: eri parsing successful, file loaded. ver = " + r0 + ", # of entries = " + this.mEriFile.mNumberOfEriEntries);
                        this.mIsEriFileLoaded = DBG;
                        if (parser instanceof XmlResourceParser) {
                            ((XmlResourceParser) parser).close();
                        }
                        if (fileInputStream != null) {
                            fileInputStream.close();
                        }
                    }
                    if (name.equals("CallPromptId")) {
                        id = Integer.parseInt(parser.getAttributeValue(null, "Id"));
                        text = parser.getAttributeValue(null, "CallPromptText");
                        if (id >= 0) {
                        }
                        Rlog.e(this.LOG_TAG, "Error Parsing ERI file: found" + id + " CallPromptId");
                    } else {
                        if (name.equals("EriInfo")) {
                            roamingIndicator = Integer.parseInt(parser.getAttributeValue(null, "RoamingIndicator"));
                            iconIndex = Integer.parseInt(parser.getAttributeValue(null, "IconIndex"));
                            iconMode = Integer.parseInt(parser.getAttributeValue(null, "IconMode"));
                            eriText = parser.getAttributeValue(null, "EriText");
                            callPromptId = Integer.parseInt(parser.getAttributeValue(null, "CallPromptId"));
                            alertId = Integer.parseInt(parser.getAttributeValue(null, "AlertId"));
                            parsedEriEntries += ERI_FROM_FILE_SYSTEM;
                            hashMap = this.mEriFile.mRoamIndTable;
                            valueOf = Integer.valueOf(roamingIndicator);
                            hashMap.put(r26, new EriInfo(roamingIndicator, iconIndex, iconMode, eriText, callPromptId, alertId));
                        }
                    }
                }
            }
        } catch (FileNotFoundException e5) {
            Rlog.d(this.LOG_TAG, "loadEriFileFromXml: no alternate file");
            parser = null;
            if (parser == null) {
                eriFile = null;
                configManager = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
                if (configManager != null) {
                    b = configManager.getConfigForSubId(this.mPhone.getSubId());
                    if (b != null) {
                        eriFile = b.getString("carrier_eri_file_name_string");
                    }
                }
                Rlog.d(this.LOG_TAG, "eriFile = " + eriFile);
                if (eriFile == null) {
                    parser = Xml.newPullParser();
                    parser.setInput(this.mContext.getAssets().open(eriFile), null);
                } else {
                    Rlog.e(this.LOG_TAG, "loadEriFileFromXml: Can't find ERI file to load");
                    return;
                }
            }
            XmlUtils.beginDocument(parser, "EriFile");
            eriFile2 = this.mEriFile;
            eriFile2.mVersionNumber = Integer.parseInt(parser.getAttributeValue(null, "VersionNumber"));
            eriFile2 = this.mEriFile;
            eriFile2.mNumberOfEriEntries = Integer.parseInt(parser.getAttributeValue(null, "NumberOfEriEntries"));
            eriFile2 = this.mEriFile;
            eriFile2.mEriFileType = Integer.parseInt(parser.getAttributeValue(null, "EriFileType"));
            parsedEriEntries = 0;
            while (true) {
                XmlUtils.nextElement(parser);
                name = parser.getName();
                if (name == null) {
                    if (name.equals("CallPromptId")) {
                        if (name.equals("EriInfo")) {
                            roamingIndicator = Integer.parseInt(parser.getAttributeValue(null, "RoamingIndicator"));
                            iconIndex = Integer.parseInt(parser.getAttributeValue(null, "IconIndex"));
                            iconMode = Integer.parseInt(parser.getAttributeValue(null, "IconMode"));
                            eriText = parser.getAttributeValue(null, "EriText");
                            callPromptId = Integer.parseInt(parser.getAttributeValue(null, "CallPromptId"));
                            alertId = Integer.parseInt(parser.getAttributeValue(null, "AlertId"));
                            parsedEriEntries += ERI_FROM_FILE_SYSTEM;
                            hashMap = this.mEriFile.mRoamIndTable;
                            valueOf = Integer.valueOf(roamingIndicator);
                            hashMap.put(r26, new EriInfo(roamingIndicator, iconIndex, iconMode, eriText, callPromptId, alertId));
                        }
                    } else {
                        id = Integer.parseInt(parser.getAttributeValue(null, "Id"));
                        text = parser.getAttributeValue(null, "CallPromptText");
                        if (id >= 0) {
                        }
                        Rlog.e(this.LOG_TAG, "Error Parsing ERI file: found" + id + " CallPromptId");
                    }
                } else {
                    break;
                    if (parsedEriEntries != this.mEriFile.mNumberOfEriEntries) {
                        Rlog.e(this.LOG_TAG, "Error Parsing ERI file: " + this.mEriFile.mNumberOfEriEntries + " defined, " + parsedEriEntries + " parsed!");
                    }
                    i = this.mEriFile.mVersionNumber;
                    Rlog.d(this.LOG_TAG, "loadEriFileFromXml: eri parsing successful, file loaded. ver = " + r0 + ", # of entries = " + this.mEriFile.mNumberOfEriEntries);
                    this.mIsEriFileLoaded = DBG;
                    if (parser instanceof XmlResourceParser) {
                        ((XmlResourceParser) parser).close();
                    }
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                }
            }
        } catch (XmlPullParserException e6) {
            Rlog.d(this.LOG_TAG, "loadEriFileFromXml: no parser for alternate file");
            parser = null;
            if (parser == null) {
                eriFile = null;
                configManager = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
                if (configManager != null) {
                    b = configManager.getConfigForSubId(this.mPhone.getSubId());
                    if (b != null) {
                        eriFile = b.getString("carrier_eri_file_name_string");
                    }
                }
                Rlog.d(this.LOG_TAG, "eriFile = " + eriFile);
                if (eriFile == null) {
                    Rlog.e(this.LOG_TAG, "loadEriFileFromXml: Can't find ERI file to load");
                    return;
                }
                parser = Xml.newPullParser();
                parser.setInput(this.mContext.getAssets().open(eriFile), null);
            }
            XmlUtils.beginDocument(parser, "EriFile");
            eriFile2 = this.mEriFile;
            eriFile2.mVersionNumber = Integer.parseInt(parser.getAttributeValue(null, "VersionNumber"));
            eriFile2 = this.mEriFile;
            eriFile2.mNumberOfEriEntries = Integer.parseInt(parser.getAttributeValue(null, "NumberOfEriEntries"));
            eriFile2 = this.mEriFile;
            eriFile2.mEriFileType = Integer.parseInt(parser.getAttributeValue(null, "EriFileType"));
            parsedEriEntries = 0;
            while (true) {
                XmlUtils.nextElement(parser);
                name = parser.getName();
                if (name == null) {
                    break;
                    if (parsedEriEntries != this.mEriFile.mNumberOfEriEntries) {
                        Rlog.e(this.LOG_TAG, "Error Parsing ERI file: " + this.mEriFile.mNumberOfEriEntries + " defined, " + parsedEriEntries + " parsed!");
                    }
                    i = this.mEriFile.mVersionNumber;
                    Rlog.d(this.LOG_TAG, "loadEriFileFromXml: eri parsing successful, file loaded. ver = " + r0 + ", # of entries = " + this.mEriFile.mNumberOfEriEntries);
                    this.mIsEriFileLoaded = DBG;
                    if (parser instanceof XmlResourceParser) {
                        ((XmlResourceParser) parser).close();
                    }
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                }
                if (name.equals("CallPromptId")) {
                    id = Integer.parseInt(parser.getAttributeValue(null, "Id"));
                    text = parser.getAttributeValue(null, "CallPromptText");
                    if (id >= 0) {
                    }
                    Rlog.e(this.LOG_TAG, "Error Parsing ERI file: found" + id + " CallPromptId");
                } else {
                    if (name.equals("EriInfo")) {
                        roamingIndicator = Integer.parseInt(parser.getAttributeValue(null, "RoamingIndicator"));
                        iconIndex = Integer.parseInt(parser.getAttributeValue(null, "IconIndex"));
                        iconMode = Integer.parseInt(parser.getAttributeValue(null, "IconMode"));
                        eriText = parser.getAttributeValue(null, "EriText");
                        callPromptId = Integer.parseInt(parser.getAttributeValue(null, "CallPromptId"));
                        alertId = Integer.parseInt(parser.getAttributeValue(null, "AlertId"));
                        parsedEriEntries += ERI_FROM_FILE_SYSTEM;
                        hashMap = this.mEriFile.mRoamIndTable;
                        valueOf = Integer.valueOf(roamingIndicator);
                        hashMap.put(r26, new EriInfo(roamingIndicator, iconIndex, iconMode, eriText, callPromptId, alertId));
                    }
                }
            }
        }
        if (parser == null) {
            eriFile = null;
            configManager = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
            if (configManager != null) {
                b = configManager.getConfigForSubId(this.mPhone.getSubId());
                if (b != null) {
                    eriFile = b.getString("carrier_eri_file_name_string");
                }
            }
            Rlog.d(this.LOG_TAG, "eriFile = " + eriFile);
            if (eriFile == null) {
                Rlog.e(this.LOG_TAG, "loadEriFileFromXml: Can't find ERI file to load");
                return;
            }
            parser = Xml.newPullParser();
            parser.setInput(this.mContext.getAssets().open(eriFile), null);
        }
        try {
            XmlUtils.beginDocument(parser, "EriFile");
            eriFile2 = this.mEriFile;
            eriFile2.mVersionNumber = Integer.parseInt(parser.getAttributeValue(null, "VersionNumber"));
            eriFile2 = this.mEriFile;
            eriFile2.mNumberOfEriEntries = Integer.parseInt(parser.getAttributeValue(null, "NumberOfEriEntries"));
            eriFile2 = this.mEriFile;
            eriFile2.mEriFileType = Integer.parseInt(parser.getAttributeValue(null, "EriFileType"));
            parsedEriEntries = 0;
            while (true) {
                XmlUtils.nextElement(parser);
                name = parser.getName();
                if (name == null) {
                    break;
                }
                if (name.equals("CallPromptId")) {
                    id = Integer.parseInt(parser.getAttributeValue(null, "Id"));
                    text = parser.getAttributeValue(null, "CallPromptText");
                    if (id >= 0 || id > ERI_FROM_MODEM) {
                        Rlog.e(this.LOG_TAG, "Error Parsing ERI file: found" + id + " CallPromptId");
                    } else {
                        this.mEriFile.mCallPromptId[id] = text;
                    }
                } else {
                    if (name.equals("EriInfo")) {
                        roamingIndicator = Integer.parseInt(parser.getAttributeValue(null, "RoamingIndicator"));
                        iconIndex = Integer.parseInt(parser.getAttributeValue(null, "IconIndex"));
                        iconMode = Integer.parseInt(parser.getAttributeValue(null, "IconMode"));
                        eriText = parser.getAttributeValue(null, "EriText");
                        callPromptId = Integer.parseInt(parser.getAttributeValue(null, "CallPromptId"));
                        alertId = Integer.parseInt(parser.getAttributeValue(null, "AlertId"));
                        parsedEriEntries += ERI_FROM_FILE_SYSTEM;
                        hashMap = this.mEriFile.mRoamIndTable;
                        valueOf = Integer.valueOf(roamingIndicator);
                        hashMap.put(r26, new EriInfo(roamingIndicator, iconIndex, iconMode, eriText, callPromptId, alertId));
                    }
                }
            }
            if (parsedEriEntries != this.mEriFile.mNumberOfEriEntries) {
                Rlog.e(this.LOG_TAG, "Error Parsing ERI file: " + this.mEriFile.mNumberOfEriEntries + " defined, " + parsedEriEntries + " parsed!");
            }
            i = this.mEriFile.mVersionNumber;
            Rlog.d(this.LOG_TAG, "loadEriFileFromXml: eri parsing successful, file loaded. ver = " + r0 + ", # of entries = " + this.mEriFile.mNumberOfEriEntries);
            this.mIsEriFileLoaded = DBG;
            if (parser instanceof XmlResourceParser) {
                ((XmlResourceParser) parser).close();
            }
            if (fileInputStream != null) {
                fileInputStream.close();
            }
        } catch (Exception e22) {
            Rlog.e(this.LOG_TAG, "Got exception while loading ERI file.", e22);
            if (parser instanceof XmlResourceParser) {
                ((XmlResourceParser) parser).close();
            }
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e7) {
                }
            }
        } catch (Throwable th) {
            if (parser instanceof XmlResourceParser) {
                ((XmlResourceParser) parser).close();
            }
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e8) {
                }
            }
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
            return (EriInfo) this.mEriFile.mRoamIndTable.get(Integer.valueOf(roamingIndicator));
        }
        return null;
    }

    private EriDisplayInformation getEriDisplayInformation(int roamInd, int defRoamInd) {
        EriInfo eriInfo;
        EriDisplayInformation ret;
        if (this.mIsEriFileLoaded) {
            eriInfo = getEriInfo(roamInd);
            if (eriInfo != null) {
                return new EriDisplayInformation(eriInfo.iconIndex, eriInfo.iconMode, eriInfo.eriText);
            }
        }
        switch (roamInd) {
            case PduPersister.LOAD_MODE_MMS_COMMON /*0*/:
                ret = new EriDisplayInformation(0, 0, this.mContext.getText(17039576).toString());
                break;
            case ERI_FROM_FILE_SYSTEM /*1*/:
                ret = new EriDisplayInformation(ERI_FROM_FILE_SYSTEM, 0, this.mContext.getText(17039577).toString());
                break;
            case ERI_FROM_MODEM /*2*/:
                ret = new EriDisplayInformation(ERI_FROM_MODEM, ERI_FROM_FILE_SYSTEM, this.mContext.getText(17039578).toString());
                break;
            case PduPersister.PROC_STATUS_COMPLETED /*3*/:
                ret = new EriDisplayInformation(roamInd, 0, this.mContext.getText(17039579).toString());
                break;
            case CharacterSets.ISO_8859_1 /*4*/:
                ret = new EriDisplayInformation(roamInd, 0, this.mContext.getText(17039580).toString());
                break;
            case CharacterSets.ISO_8859_2 /*5*/:
                ret = new EriDisplayInformation(roamInd, 0, this.mContext.getText(17039581).toString());
                break;
            case CharacterSets.ISO_8859_3 /*6*/:
                ret = new EriDisplayInformation(roamInd, 0, this.mContext.getText(17039582).toString());
                break;
            case CharacterSets.ISO_8859_4 /*7*/:
                ret = new EriDisplayInformation(roamInd, 0, this.mContext.getText(17039583).toString());
                break;
            case CharacterSets.ISO_8859_5 /*8*/:
                ret = new EriDisplayInformation(roamInd, 0, this.mContext.getText(17039584).toString());
                break;
            case CharacterSets.ISO_8859_6 /*9*/:
                ret = new EriDisplayInformation(roamInd, 0, this.mContext.getText(17039585).toString());
                break;
            case CharacterSets.ISO_8859_7 /*10*/:
                ret = new EriDisplayInformation(roamInd, 0, this.mContext.getText(17039586).toString());
                break;
            case CharacterSets.ISO_8859_8 /*11*/:
                ret = new EriDisplayInformation(roamInd, 0, this.mContext.getText(17039587).toString());
                break;
            case CharacterSets.ISO_8859_9 /*12*/:
                ret = new EriDisplayInformation(roamInd, 0, this.mContext.getText(17039588).toString());
                break;
            default:
                if (!this.mIsEriFileLoaded) {
                    Rlog.d(this.LOG_TAG, "ERI File not loaded");
                    if (defRoamInd <= ERI_FROM_MODEM) {
                        switch (defRoamInd) {
                            case PduPersister.LOAD_MODE_MMS_COMMON /*0*/:
                                ret = new EriDisplayInformation(0, 0, this.mContext.getText(17039576).toString());
                                break;
                            case ERI_FROM_FILE_SYSTEM /*1*/:
                                ret = new EriDisplayInformation(ERI_FROM_FILE_SYSTEM, 0, this.mContext.getText(17039577).toString());
                                break;
                            case ERI_FROM_MODEM /*2*/:
                                ret = new EriDisplayInformation(ERI_FROM_MODEM, ERI_FROM_FILE_SYSTEM, this.mContext.getText(17039578).toString());
                                break;
                            default:
                                ret = new EriDisplayInformation(-1, -1, "ERI text");
                                break;
                        }
                    }
                    ret = new EriDisplayInformation(ERI_FROM_MODEM, ERI_FROM_FILE_SYSTEM, this.mContext.getText(17039578).toString());
                    break;
                }
                eriInfo = getEriInfo(roamInd);
                EriInfo defEriInfo = getEriInfo(defRoamInd);
                if (eriInfo == null) {
                    if (defEriInfo != null) {
                        ret = new EriDisplayInformation(defEriInfo.iconIndex, defEriInfo.iconMode, defEriInfo.eriText);
                        break;
                    }
                    Rlog.e(this.LOG_TAG, "ERI defRoamInd " + defRoamInd + " not found in ERI file ...on");
                    ret = new EriDisplayInformation(0, 0, this.mContext.getText(17039576).toString());
                    break;
                }
                ret = new EriDisplayInformation(eriInfo.iconIndex, eriInfo.iconMode, eriInfo.eriText);
                break;
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
