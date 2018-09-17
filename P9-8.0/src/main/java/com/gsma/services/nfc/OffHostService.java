package com.gsma.services.nfc;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources.NotFoundException;
import android.graphics.drawable.Drawable;
import android.nfc.cardemulation.AidGroup;
import android.util.Log;
import com.gsma.services.utils.InsufficientResourcesException;
import com.nxp.nfc.gsma.internal.NxpNfcController;
import com.nxp.nfc.gsma.internal.NxpOffHostService;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class OffHostService {
    private static final String TAG = "OffHostService";
    List<AidGroup> mAidGroupList;
    Drawable mBanner;
    int mBannerResId;
    Context mContext;
    String mDescription;
    boolean mModifiable;
    NxpNfcController mNxpNfcController;
    String mPackageName;
    String mSEName;
    String mServiceName;
    int mUserId;

    protected OffHostService(int userId, String description, String SELocation, String packageName, String serviceName, boolean modifiable) {
        this.mDescription = null;
        this.mSEName = null;
        this.mPackageName = null;
        this.mServiceName = null;
        this.mModifiable = true;
        this.mAidGroupList = new ArrayList();
        this.mNxpNfcController = null;
        this.mContext = null;
        this.mBannerResId = 0;
        this.mUserId = userId;
        this.mDescription = description;
        this.mSEName = SELocation;
        this.mPackageName = packageName;
        this.mServiceName = serviceName;
        this.mModifiable = modifiable;
    }

    protected OffHostService(NxpOffHostService service) {
        this.mDescription = null;
        this.mSEName = null;
        this.mPackageName = null;
        this.mServiceName = null;
        this.mModifiable = true;
        this.mAidGroupList = new ArrayList();
        this.mNxpNfcController = null;
        this.mContext = null;
        this.mUserId = service.mUserId;
        this.mDescription = service.mDescription;
        this.mSEName = service.mSEName;
        this.mPackageName = service.mPackageName;
        this.mServiceName = service.mServiceName;
        this.mModifiable = service.mModifiable;
        this.mAidGroupList = convertToOffHostAidGroupList(service.mAidGroupList);
        this.mBannerResId = service.getBannerId();
        this.mContext = service.getContext();
        this.mNxpNfcController = service.mNxpNfcController;
        PackageManager pManager = this.mContext.getPackageManager();
        if (this.mBannerResId > 0) {
            try {
                Log.d(TAG, "setBannerResId(): getDrawable() with mBannerResId=" + String.valueOf(this.mBannerResId));
                this.mBanner = pManager.getResourcesForApplication(this.mPackageName).getDrawable(this.mBannerResId);
            } catch (Exception e) {
                Log.e(TAG, "Exception : " + e.getMessage());
            }
        }
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

    /* JADX WARNING: Removed duplicated region for block: B:30:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0127  */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0127  */
    /* JADX WARNING: Removed duplicated region for block: B:30:? A:{SYNTHETIC, RETURN} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setBanner(Drawable banner) {
        boolean z = true;
        String packName = this.mPackageName;
        Log.d(TAG, "setBanner() Resources packName: " + packName);
        int i = 0;
        while (i < Class.forName(packName + ".R").getClasses().length) {
            try {
                if (!Class.forName(packName + ".R").getClasses()[i].getName().split("\\$")[1].equals("drawable") || Class.forName(packName + ".R").getClasses()[i] == null) {
                    i++;
                } else {
                    Field[] f = Class.forName(packName + ".R").getClasses()[i].getDeclaredFields();
                    int max = f.length;
                    for (int counter = 0; counter < max; counter++) {
                        int resId = f[counter].getInt(null);
                        Drawable d = this.mContext.getDrawable(resId);
                        if (d.getConstantState().equals(banner.getConstantState())) {
                            this.mBannerResId = resId;
                            this.mBanner = d;
                            Log.d(TAG, "setBanner() Resources GOT THE DRAWABLE On loop " + String.valueOf(counter) + "got resId : " + String.valueOf(resId));
                            String str = TAG;
                            StringBuilder append = new StringBuilder().append("setBanner() is banner null?");
                            if (this.mBanner != null) {
                                z = false;
                            }
                            Log.d(str, append.append(z).toString());
                            if (this.mBannerResId == 0) {
                                Log.d(TAG, "bannerId  set to 0");
                                this.mBannerResId = -1;
                                this.mBanner = banner;
                                return;
                            }
                            return;
                        }
                    }
                    if (this.mBannerResId == 0) {
                    }
                }
            } catch (Exception e) {
                Log.d(TAG, "setBanner() Resources exception ..." + e.getMessage());
            }
        }
        if (this.mBannerResId == 0) {
        }
    }

    public void setBanner(int bannerResId) throws NotFoundException {
        Log.d(TAG, "setBannerResId() with " + String.valueOf(bannerResId));
        this.mBannerResId = bannerResId;
        PackageManager pManager = this.mContext.getPackageManager();
        String packName = this.mContext.getPackageName();
        if (this.mBannerResId > 0) {
            try {
                Log.d(TAG, "setBannerResId(): getDrawable() with mBannerResId=" + String.valueOf(this.mBannerResId));
                this.mBanner = pManager.getResourcesForApplication(packName).getDrawable(this.mBannerResId);
            } catch (Exception e) {
                Log.e(TAG, "Exception : " + e.getMessage());
            }
        }
    }

    protected boolean getModifiable() {
        return this.mModifiable;
    }

    public Drawable getBanner() {
        return this.mBanner;
    }

    public AidGroup defineAidGroup(String description, String category) {
        Log.d(TAG, "defineAidGroup description=" + description + ",  category=" + category);
        if (description == null) {
            throw new IllegalArgumentException("Invalid description provided");
        } else if ("payment".equals(category) || ("other".equals(category) ^ 1) == 0) {
            AidGroup aidGroup = new AidGroup(description, category);
            this.mAidGroupList.add(aidGroup);
            return aidGroup;
        } else {
            throw new IllegalArgumentException("Invalid category provided");
        }
    }

    public void deleteAidGroup(AidGroup group) {
        this.mAidGroupList.remove(group);
    }

    public AidGroup[] getAidGroups() {
        if (this.mAidGroupList.size() == 0) {
            return null;
        }
        return (AidGroup[]) this.mAidGroupList.toArray(new AidGroup[this.mAidGroupList.size()]);
    }

    private ArrayList<AidGroup> convertToCeAidGroupList(List<AidGroup> mAidGroups) {
        ArrayList<AidGroup> mApduAidGroupList = new ArrayList();
        List<String> aidList = new ArrayList();
        for (AidGroup mGroup : mAidGroups) {
            AidGroup mCeAidGroup = new AidGroup(mGroup.getCategory(), mGroup.getDescription());
            aidList = mCeAidGroup.getAids();
            for (String aid : mGroup.getAidList()) {
                aidList.add(aid);
            }
            mApduAidGroupList.add(mCeAidGroup);
        }
        return mApduAidGroupList;
    }

    private NxpOffHostService convertToNxpOffhostService(OffHostService service) {
        ArrayList<AidGroup> mAidGroupList = convertToCeAidGroupList(service.mAidGroupList);
        NxpOffHostService mNxpOffHostService = new NxpOffHostService(service.mUserId, service.mDescription, service.mSEName, service.mPackageName, service.mServiceName, service.mModifiable);
        if (service.mBannerResId <= 0) {
            mNxpOffHostService.setBanner(service.mBanner);
        }
        mNxpOffHostService.setBannerId(service.mBannerResId);
        mNxpOffHostService.mAidGroupList.addAll(mAidGroupList);
        return mNxpOffHostService;
    }

    public void commit() throws InsufficientResourcesException {
        Log.d(TAG, "commit() begin");
        boolean status = this.mNxpNfcController.commitOffHostService(this.mUserId, this.mPackageName, convertToNxpOffhostService(this));
        Log.d(TAG, " commit status value" + status);
        if (!status) {
            throw new InsufficientResourcesException("Routing Table is Full, Cannot Commit");
        }
    }

    private void setNxpNfcController(NxpNfcController nxpNfcController) {
        this.mNxpNfcController = nxpNfcController;
    }

    private ArrayList<AidGroup> convertToOffHostAidGroupList(List<AidGroup> mAidGroups) {
        ArrayList<AidGroup> mOffHostAidGroups = new ArrayList();
        String aidGroupDescription = "";
        for (AidGroup mCeAidGroup : mAidGroups) {
            Log.d(TAG, mCeAidGroup.getDescription() + "," + mCeAidGroup.getCategory());
            if (mCeAidGroup.getDescription() == null) {
                aidGroupDescription = "";
            } else {
                aidGroupDescription = mCeAidGroup.getDescription();
            }
            AidGroup mAidGroup = defineAidGroup(aidGroupDescription, mCeAidGroup.getCategory());
            for (String aid : mCeAidGroup.getAids()) {
                mAidGroup.addNewAid(aid);
            }
            mOffHostAidGroups.add(mAidGroup);
        }
        return mOffHostAidGroups;
    }

    public String toString() {
        StringBuffer out = new StringBuffer("OffHostService: ");
        out.append("mPackageName: " + this.mPackageName);
        out.append(", mServiceName: " + this.mServiceName);
        out.append(", description: " + this.mDescription);
        for (AidGroup aidGroup : this.mAidGroupList) {
            out.append(", aidGroup: " + aidGroup);
        }
        return out.toString();
    }
}
