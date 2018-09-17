package com.nxp.nfc.gsma.internal;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.nfc.cardemulation.AidGroup;
import java.util.ArrayList;
import java.util.List;

public class NxpOffHostService {
    public List<AidGroup> mAidGroupList = new ArrayList();
    public Drawable mBanner;
    public int mBannerId;
    public Context mContext = null;
    public String mDescription = null;
    public boolean mModifiable = true;
    public NxpNfcController mNxpNfcController;
    public String mPackageName = null;
    public String mSEName = null;
    public String mServiceName = null;
    public int mUserId;

    public NxpOffHostService(int userId, String description, String SELocation, String packageName, String serviceName, boolean modifiable) {
        this.mUserId = userId;
        this.mDescription = description;
        this.mSEName = SELocation;
        this.mPackageName = packageName;
        this.mServiceName = serviceName;
        this.mModifiable = modifiable;
    }

    public String getLocation() {
        return this.mSEName;
    }

    public String getDescription() {
        return this.mDescription;
    }

    protected String getServiceName() {
        return this.mServiceName;
    }

    public void setBanner(Drawable banner) {
        this.mBanner = banner;
    }

    public void setBannerId(int bannerid) {
        this.mBannerId = bannerid;
    }

    protected boolean getModifiable() {
        return this.mModifiable;
    }

    public Drawable getBanner() {
        return this.mBanner;
    }

    public int getBannerId() {
        return this.mBannerId;
    }

    public void setContext(Context context) {
        this.mContext = context;
    }

    public Context getContext() {
        return this.mContext;
    }

    public void setNxpNfcController(NxpNfcController nxpNfcController) {
        this.mNxpNfcController = nxpNfcController;
    }

    public String toString() {
        StringBuilder out = new StringBuilder("NxpOffHostService: ");
        out.append("mUserId:").append(this.mUserId);
        out.append(", description: ").append(this.mDescription);
        out.append(", mSEName: ").append(this.mSEName);
        out.append(", mPackageName: ").append(this.mPackageName);
        out.append(", mServiceName: ").append(this.mServiceName);
        out.append(", mModifiable: ").append(this.mModifiable);
        if (this.mAidGroupList != null && this.mAidGroupList.size() > 0) {
            out.append("AidGroupList:");
            for (AidGroup group : this.mAidGroupList) {
                out.append(group.toString());
            }
        }
        return out.toString();
    }
}
