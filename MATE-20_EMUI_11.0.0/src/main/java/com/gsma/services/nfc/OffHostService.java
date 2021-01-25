package com.gsma.services.nfc;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.nfc.cardemulation.NxpAidGroup;
import android.util.Log;
import com.gsma.services.utils.InsufficientResourcesException;
import com.nxp.nfc.gsma.internal.NxpNfcController;
import com.nxp.nfc.gsma.internal.NxpOffHostService;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
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
        this.mAidGroupList = convertToOffHostAidGroupList(service.mNxpAidGroupList);
        this.mBanner = service.mBanner;
        this.mBannerResId = service.getBannerId();
        this.mContext = service.getContext();
        this.mNxpNfcController = service.mNxpNfcController;
        PackageManager pManager = this.mContext.getPackageManager();
        if (this.mBannerResId > 0) {
            try {
                Log.d(TAG, "setBannerResId(): getDrawable() with mBannerResId=" + String.valueOf(this.mBannerResId));
                this.mBanner = pManager.getResourcesForApplication(this.mPackageName).getDrawable(this.mBannerResId, null);
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

    /* access modifiers changed from: protected */
    public String getServiceName() {
        return this.mServiceName;
    }

    public void setBanner(Drawable banner) {
        String packName = this.mPackageName;
        Log.d(TAG, "setBanner() Resources packName: " + packName);
        boolean z = false;
        int i = 0;
        while (true) {
            try {
                if (i >= Class.forName(packName + ".R").getClasses().length) {
                    break;
                }
                if (Class.forName(packName + ".R").getClasses()[i].getName().split("\\$")[1].equals("drawable")) {
                    if (Class.forName(packName + ".R").getClasses()[i] != null) {
                        Field[] f = Class.forName(packName + ".R").getClasses()[i].getDeclaredFields();
                        int counter = 0;
                        int max = f.length;
                        while (true) {
                            if (counter >= max) {
                                break;
                            }
                            int resId = f[counter].getInt(null);
                            Drawable d = this.mContext.getDrawable(resId);
                            if (areDrawablesEqual(banner, d)) {
                                this.mBannerResId = resId;
                                this.mBanner = d;
                                Log.d(TAG, "setBanner() Resources GOT THE DRAWABLE On loop " + String.valueOf(counter) + "got resId : " + String.valueOf(resId));
                                StringBuilder sb = new StringBuilder();
                                sb.append("setBanner() is banner null?");
                                if (this.mBanner == null) {
                                    z = true;
                                }
                                sb.append(z);
                                Log.d(TAG, sb.toString());
                            } else {
                                counter++;
                            }
                        }
                    }
                }
                i++;
            } catch (Exception e) {
                Log.d(TAG, "setBanner() Resources exception ...", e);
            }
        }
        if (this.mBannerResId == 0) {
            Log.d(TAG, "bannerId  set to 0");
            this.mBannerResId = -1;
            this.mBanner = banner;
        }
    }

    public void setBanner(int bannerResId) throws Resources.NotFoundException {
        Log.d(TAG, "setBannerResId() with " + String.valueOf(bannerResId));
        this.mBannerResId = bannerResId;
        PackageManager pManager = this.mContext.getPackageManager();
        String packName = this.mContext.getPackageName();
        if (this.mBannerResId > 0) {
            try {
                Log.d(TAG, "setBannerResId(): getDrawable() with mBannerResId=" + String.valueOf(this.mBannerResId));
                this.mBanner = pManager.getResourcesForApplication(packName).getDrawable(this.mBannerResId, null);
            } catch (Exception e) {
                Log.e(TAG, "Exception : " + e.getMessage());
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean getModifiable() {
        return this.mModifiable;
    }

    public Drawable getBanner() {
        return this.mBanner;
    }

    public AidGroup defineAidGroup(String description, String category) {
        Log.d(TAG, "defineAidGroup description=" + description + ",  category=" + category);
        if (description == null) {
            throw new IllegalArgumentException("Invalid description provided");
        } else if ("payment".equals(category) || "other".equals(category)) {
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

    private ArrayList<NxpAidGroup> convertToCeAidGroupList(List<AidGroup> mAidGroups) {
        NxpAidGroup mCeAidGroup;
        ArrayList<NxpAidGroup> mApduAidGroupList = new ArrayList<>();
        List<String> aidList = new ArrayList<>();
        for (AidGroup mGroup : mAidGroups) {
            for (String aid : mGroup.getAidList()) {
                aidList.add(aid);
            }
            if (aidList.size() == 0) {
                mCeAidGroup = new NxpAidGroup(mGroup.getCategory(), mGroup.getDescription());
            } else {
                mCeAidGroup = new NxpAidGroup(aidList, mGroup.getCategory(), mGroup.getDescription());
            }
            mApduAidGroupList.add(mCeAidGroup);
        }
        return mApduAidGroupList;
    }

    private NxpOffHostService convertToNxpOffhostService(OffHostService service) {
        ArrayList<NxpAidGroup> mAidGroupList2 = convertToCeAidGroupList(service.mAidGroupList);
        NxpOffHostService mNxpOffHostService = new NxpOffHostService(service.mUserId, service.mDescription, service.mSEName, service.mPackageName, service.mServiceName, service.mModifiable);
        if (service.mBannerResId <= 0) {
            mNxpOffHostService.setBanner(service.mBanner);
        }
        mNxpOffHostService.setBannerId(service.mBannerResId);
        mNxpOffHostService.mNxpAidGroupList.addAll(mAidGroupList2);
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

    private ArrayList<AidGroup> convertToOffHostAidGroupList(List<NxpAidGroup> mAidGroups) {
        String aidGroupDescription;
        ArrayList<AidGroup> mOffHostAidGroups = new ArrayList<>();
        for (NxpAidGroup mCeAidGroup : mAidGroups) {
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

    private boolean areDrawablesEqual(Drawable drawableA, Drawable drawableB) {
        Drawable.ConstantState stateA = drawableA.getConstantState();
        Drawable.ConstantState stateB = drawableB.getConstantState();
        if ((stateA == null || stateB == null || !stateA.equals(stateB)) && !areDrawableBitmapsEqual(drawableA, drawableB)) {
            return false;
        }
        return true;
    }

    private boolean areDrawableBitmapsEqual(Drawable drawableA, Drawable drawableB) {
        if (!(drawableA instanceof BitmapDrawable) || !(drawableB instanceof BitmapDrawable)) {
            return false;
        }
        Bitmap bitmapA = ((BitmapDrawable) drawableA).getBitmap();
        Bitmap bitmapB = ((BitmapDrawable) drawableB).getBitmap();
        if (bitmapA.getWidth() == bitmapB.getWidth() && bitmapA.getHeight() == bitmapB.getHeight() && bitmapA.sameAs(bitmapB)) {
            return true;
        }
        return false;
    }

    public String toString() {
        StringBuffer out = new StringBuffer("OffHostService: ");
        out.append("mPackageName: " + this.mPackageName);
        out.append(", mServiceName: " + this.mServiceName);
        out.append(", description: " + this.mDescription);
        Iterator<AidGroup> it = this.mAidGroupList.iterator();
        while (it.hasNext()) {
            out.append(", aidGroup: " + it.next());
        }
        return out.toString();
    }
}
