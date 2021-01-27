package com.google.android.mms.pdu;

import android.os.SystemProperties;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

public class PduBody {
    private static final boolean GCF_MMS_TEST = SystemProperties.getBoolean("ro.config.mms_file_upper_cmp", false);
    private Map<String, PduPart> mPartMapByContentId;
    private Map<String, PduPart> mPartMapByContentLocation;
    private Map<String, PduPart> mPartMapByFileName;
    private Map<String, PduPart> mPartMapByName;
    private Vector<PduPart> mParts;

    public PduBody() {
        this.mParts = null;
        this.mPartMapByContentId = null;
        this.mPartMapByContentLocation = null;
        this.mPartMapByName = null;
        this.mPartMapByFileName = null;
        this.mParts = new Vector<>();
        this.mPartMapByContentId = new HashMap();
        this.mPartMapByContentLocation = new HashMap();
        this.mPartMapByName = new HashMap();
        this.mPartMapByFileName = new HashMap();
    }

    private void putPartToMaps(PduPart part) {
        byte[] contentId = part.getContentId();
        if (contentId != null) {
            String clc = new String(contentId);
            if (GCF_MMS_TEST) {
                this.mPartMapByContentId.put(clc.toUpperCase(Locale.getDefault()), part);
            } else {
                this.mPartMapByContentId.put(clc, part);
            }
        }
        byte[] contentLocation = part.getContentLocation();
        if (contentLocation != null) {
            String clc2 = new String(contentLocation);
            if (GCF_MMS_TEST) {
                this.mPartMapByContentLocation.put(clc2.toUpperCase(Locale.getDefault()), part);
            } else {
                this.mPartMapByContentLocation.put(clc2, part);
            }
        }
        byte[] name = part.getName();
        if (name != null) {
            String clc3 = new String(name);
            if (GCF_MMS_TEST) {
                this.mPartMapByName.put(clc3.toUpperCase(Locale.getDefault()), part);
            } else {
                this.mPartMapByName.put(clc3, part);
            }
        }
        byte[] fileName = part.getFilename();
        if (fileName != null) {
            String clc4 = new String(fileName);
            if (GCF_MMS_TEST) {
                this.mPartMapByFileName.put(clc4.toUpperCase(Locale.getDefault()), part);
            } else {
                this.mPartMapByFileName.put(clc4, part);
            }
        }
    }

    public boolean addPart(PduPart part) {
        if (part != null) {
            putPartToMaps(part);
            return this.mParts.add(part);
        }
        throw new NullPointerException();
    }

    public void addPart(int index, PduPart part) {
        if (part != null) {
            putPartToMaps(part);
            this.mParts.add(index, part);
            return;
        }
        throw new NullPointerException();
    }

    public PduPart removePart(int index) {
        return this.mParts.remove(index);
    }

    public void removeAll() {
        this.mParts.clear();
    }

    public PduPart getPart(int index) {
        return this.mParts.get(index);
    }

    public int getPartIndex(PduPart part) {
        return this.mParts.indexOf(part);
    }

    public int getPartsNum() {
        return this.mParts.size();
    }

    public PduPart getPartByContentId(String cid) {
        return this.mPartMapByContentId.get(cid);
    }

    public PduPart getPartByContentLocation(String contentLocation) {
        return this.mPartMapByContentLocation.get(contentLocation);
    }

    public PduPart getPartByName(String name) {
        return this.mPartMapByName.get(name);
    }

    public PduPart getPartByFileName(String filename) {
        return this.mPartMapByFileName.get(filename);
    }
}
