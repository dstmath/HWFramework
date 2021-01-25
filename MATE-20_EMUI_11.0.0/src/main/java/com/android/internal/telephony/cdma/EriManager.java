package com.android.internal.telephony.cdma;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.PersistableBundle;
import android.telephony.CarrierConfigManager;
import android.telephony.Rlog;
import android.util.Xml;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConfigurationManager;
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

    /* access modifiers changed from: package-private */
    public class EriFile {
        String[] mCallPromptId = {PhoneConfigurationManager.SSSS, PhoneConfigurationManager.SSSS, PhoneConfigurationManager.SSSS};
        int mEriFileType = -1;
        int mNumberOfEriEntries = 0;
        HashMap<Integer, EriInfo> mRoamIndTable = new HashMap<>();
        int mVersionNumber = -1;

        EriFile() {
        }
    }

    /* access modifiers changed from: package-private */
    public class EriDisplayInformation {
        int mEriIconIndex;
        int mEriIconMode;
        @UnsupportedAppUsage
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

    public EriManager(Phone phone, int eriFileSource) {
        this.mPhone = phone;
        this.mContext = this.mPhone.getContext();
        this.mEriFileSource = eriFileSource;
        this.mEriFile = new EriFile();
        this.LOG_TAG += "[SUB" + phone.getPhoneId() + "]";
    }

    public void dispose() {
        this.mEriFile = new EriFile();
        this.mIsEriFileLoaded = false;
    }

    public void loadEriFile() {
        int i = this.mEriFileSource;
        if (i == 1) {
            loadEriFileFromFileSystem();
        } else if (i != 2) {
            loadEriFileFromXml();
        } else {
            loadEriFileFromModem();
        }
    }

    private void loadEriFileFromModem() {
    }

    private void loadEriFileFromFileSystem() {
    }

    private void loadEriFileFromXml() {
        XmlPullParser parser;
        String eriFile;
        PersistableBundle b;
        FileInputStream stream = null;
        Resources r = this.mContext.getResources();
        String str = null;
        try {
            Rlog.d(this.LOG_TAG, "loadEriFileFromXml: check for alternate file");
            stream = new FileInputStream(r.getString(17039589));
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
            CarrierConfigManager configManager = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
            if (configManager == null || (b = configManager.getConfigForSubId(this.mPhone.getSubId())) == null) {
                eriFile = null;
            } else {
                eriFile = b.getString("carrier_eri_file_name_string");
            }
            Rlog.d(this.LOG_TAG, "eriFile = " + eriFile);
            if (eriFile == null) {
                Rlog.e(this.LOG_TAG, "loadEriFileFromXml: Can't find ERI file to load");
                return;
            }
            try {
                parser = Xml.newPullParser();
                parser.setInput(this.mContext.getAssets().open(eriFile), null);
            } catch (IOException | XmlPullParserException e3) {
                Rlog.e(this.LOG_TAG, "loadEriFileFromXml: no parser for " + eriFile + ". Exception = " + e3.toString());
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
                        Rlog.e(this.LOG_TAG, "Error Parsing ERI file: found" + id + " CallPromptId");
                    } else {
                        this.mEriFile.mCallPromptId[id] = text;
                    }
                } else if (name.equals("EriInfo")) {
                    int roamingIndicator = Integer.parseInt(parser.getAttributeValue(str, "RoamingIndicator"));
                    this.mEriFile.mRoamIndTable.put(Integer.valueOf(roamingIndicator), new EriInfo(roamingIndicator, Integer.parseInt(parser.getAttributeValue(str, "IconIndex")), Integer.parseInt(parser.getAttributeValue(str, "IconMode")), parser.getAttributeValue(str, "EriText"), Integer.parseInt(parser.getAttributeValue(str, "CallPromptId")), Integer.parseInt(parser.getAttributeValue(str, "AlertId"))));
                    parsedEriEntries++;
                }
                str = null;
            }
            if (parsedEriEntries != this.mEriFile.mNumberOfEriEntries) {
                Rlog.e(this.LOG_TAG, "Error Parsing ERI file: " + this.mEriFile.mNumberOfEriEntries + " defined, " + parsedEriEntries + " parsed!");
            }
            Rlog.d(this.LOG_TAG, "loadEriFileFromXml: eri parsing successful, file loaded. ver = " + this.mEriFile.mVersionNumber + ", # of entries = " + this.mEriFile.mNumberOfEriEntries);
            this.mIsEriFileLoaded = true;
            if (parser instanceof XmlResourceParser) {
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
            if (parser instanceof XmlResourceParser) {
                ((XmlResourceParser) parser).close();
            }
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e6) {
                }
            }
            throw th;
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

    @UnsupportedAppUsage
    private EriDisplayInformation getEriDisplayInformation(int roamInd, int defRoamInd) {
        EriInfo eriInfo;
        if (this.mIsEriFileLoaded && (eriInfo = getEriInfo(roamInd)) != null) {
            return new EriDisplayInformation(eriInfo.iconIndex, eriInfo.iconMode, eriInfo.eriText);
        }
        switch (roamInd) {
            case 0:
                return new EriDisplayInformation(0, 0, this.mContext.getText(17041145).toString());
            case 1:
                return new EriDisplayInformation(1, 0, this.mContext.getText(17041146).toString());
            case 2:
                return new EriDisplayInformation(2, 1, this.mContext.getText(17041150).toString());
            case 3:
                return new EriDisplayInformation(roamInd, 0, this.mContext.getText(17041151).toString());
            case 4:
                return new EriDisplayInformation(roamInd, 0, this.mContext.getText(17041152).toString());
            case 5:
                return new EriDisplayInformation(roamInd, 0, this.mContext.getText(17041153).toString());
            case 6:
                return new EriDisplayInformation(roamInd, 0, this.mContext.getText(17041154).toString());
            case 7:
                return new EriDisplayInformation(roamInd, 0, this.mContext.getText(17041155).toString());
            case 8:
                return new EriDisplayInformation(roamInd, 0, this.mContext.getText(17041156).toString());
            case 9:
                return new EriDisplayInformation(roamInd, 0, this.mContext.getText(17041157).toString());
            case 10:
                return new EriDisplayInformation(roamInd, 0, this.mContext.getText(17041147).toString());
            case 11:
                return new EriDisplayInformation(roamInd, 0, this.mContext.getText(17041148).toString());
            case 12:
                return new EriDisplayInformation(roamInd, 0, this.mContext.getText(17041149).toString());
            default:
                if (!this.mIsEriFileLoaded) {
                    Rlog.d(this.LOG_TAG, "ERI File not loaded");
                    if (defRoamInd > 2) {
                        return new EriDisplayInformation(2, 1, this.mContext.getText(17041150).toString());
                    }
                    if (defRoamInd == 0) {
                        return new EriDisplayInformation(0, 0, this.mContext.getText(17041145).toString());
                    }
                    if (defRoamInd == 1) {
                        return new EriDisplayInformation(1, 0, this.mContext.getText(17041146).toString());
                    }
                    if (defRoamInd != 2) {
                        return new EriDisplayInformation(-1, -1, "ERI text");
                    }
                    return new EriDisplayInformation(2, 1, this.mContext.getText(17041150).toString());
                }
                EriInfo eriInfo2 = getEriInfo(roamInd);
                EriInfo defEriInfo = getEriInfo(defRoamInd);
                if (eriInfo2 != null) {
                    return new EriDisplayInformation(eriInfo2.iconIndex, eriInfo2.iconMode, eriInfo2.eriText);
                }
                if (defEriInfo != null) {
                    return new EriDisplayInformation(defEriInfo.iconIndex, defEriInfo.iconMode, defEriInfo.eriText);
                }
                String str = this.LOG_TAG;
                Rlog.e(str, "ERI defRoamInd " + defRoamInd + " not found in ERI file ...on");
                return new EriDisplayInformation(0, 0, this.mContext.getText(17041145).toString());
        }
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
