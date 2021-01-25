package com.nxp.nfc.gsma.internal;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.nfc.cardemulation.NxpAidGroup;
import java.util.ArrayList;
import java.util.List;

public class NxpOffHostService {
    public Drawable mBanner;
    public int mBannerId;
    public Context mContext = null;
    public String mDescription = null;
    public boolean mModifiable = true;
    public List<NxpAidGroup> mNxpAidGroupList = new ArrayList();
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

    /* access modifiers changed from: protected */
    public String getServiceName() {
        return this.mServiceName;
    }

    public void setBanner(Drawable banner) {
        this.mBanner = banner;
    }

    public void setBannerId(int bannerid) {
        this.mBannerId = bannerid;
    }

    /* access modifiers changed from: protected */
    public boolean getModifiable() {
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
        out.append("mUserId:" + this.mUserId);
        out.append(", description: " + this.mDescription);
        out.append(", mSEName: " + this.mSEName);
        out.append(", mPackageName: " + this.mPackageName);
        out.append(", mServiceName: " + this.mServiceName);
        out.append(", mModifiable: " + this.mModifiable);
        List<NxpAidGroup> list = this.mNxpAidGroupList;
        if (list != null && list.size() > 0) {
            out.append("AidGroupList:");
            for (NxpAidGroup group : this.mNxpAidGroupList) {
                out.append(group.toString());
            }
        }
        return out.toString();
    }
}
