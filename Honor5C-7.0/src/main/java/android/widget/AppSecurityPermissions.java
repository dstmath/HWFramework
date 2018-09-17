package android.widget;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.graphics.drawable.Drawable;
import android.hwcontrol.HwWidgetFactory.PermissionInformation;
import android.os.Parcel;
import android.os.UserHandle;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout.LayoutParams;
import com.android.internal.R;
import com.huawei.hwperformance.HwPerformance;
import com.huawei.pgmng.log.LogPower;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AppSecurityPermissions {
    private static final String TAG = "AppSecurityPermissions";
    public static final int WHICH_ALL = 65535;
    public static final int WHICH_NEW = 4;
    private static final boolean localLOGV = false;
    private final Context mContext;
    private final LayoutInflater mInflater;
    private final CharSequence mNewPermPrefix;
    private String mPackageName;
    private final PermissionInfoComparator mPermComparator;
    private final PermissionGroupInfoComparator mPermGroupComparator;
    private final Map<String, MyPermissionGroupInfo> mPermGroups;
    private final List<MyPermissionGroupInfo> mPermGroupsList;
    private final List<MyPermissionInfo> mPermsList;
    private final PackageManager mPm;

    static class MyPermissionGroupInfo extends PermissionGroupInfo {
        final ArrayList<MyPermissionInfo> mAllPermissions;
        CharSequence mLabel;
        final ArrayList<MyPermissionInfo> mNewPermissions;

        MyPermissionGroupInfo(PermissionInfo perm) {
            this.mNewPermissions = new ArrayList();
            this.mAllPermissions = new ArrayList();
            this.name = perm.packageName;
            this.packageName = perm.packageName;
        }

        MyPermissionGroupInfo(PermissionGroupInfo info) {
            super(info);
            this.mNewPermissions = new ArrayList();
            this.mAllPermissions = new ArrayList();
        }

        public Drawable loadGroupIcon(Context context, PackageManager pm) {
            if (this.icon != 0) {
                return loadUnbadgedIcon(pm);
            }
            return context.getDrawable(R.drawable.ic_perm_device_info);
        }
    }

    private static class MyPermissionInfo extends PermissionInfo {
        int mExistingReqFlags;
        CharSequence mLabel;
        boolean mNew;
        int mNewReqFlags;

        MyPermissionInfo(PermissionInfo info) {
            super(info);
        }
    }

    private static class PermissionGroupInfoComparator implements Comparator<MyPermissionGroupInfo> {
        private final Collator sCollator;

        private PermissionGroupInfoComparator() {
            this.sCollator = Collator.getInstance();
        }

        public final int compare(MyPermissionGroupInfo a, MyPermissionGroupInfo b) {
            return this.sCollator.compare(a.mLabel, b.mLabel);
        }
    }

    private static class PermissionInfoComparator implements Comparator<MyPermissionInfo> {
        private final Collator sCollator;

        PermissionInfoComparator() {
            this.sCollator = Collator.getInstance();
        }

        public final int compare(MyPermissionInfo a, MyPermissionInfo b) {
            return this.sCollator.compare(a.mLabel, b.mLabel);
        }
    }

    public static class PermissionItemView extends LinearLayout implements OnClickListener {
        AlertDialog mDialog;
        MyPermissionGroupInfo mGroup;
        private String mPackageName;
        MyPermissionInfo mPerm;
        private boolean mShowRevokeUI;

        public PermissionItemView(Context context, AttributeSet attrs) {
            super(context, attrs);
            this.mShowRevokeUI = false;
            setClickable(true);
        }

        public void setPermission(MyPermissionGroupInfo grp, MyPermissionInfo perm, boolean first, CharSequence newPermPrefix, String packageName, boolean showRevokeUI) {
            this.mGroup = grp;
            this.mPerm = perm;
            this.mShowRevokeUI = showRevokeUI;
            this.mPackageName = packageName;
            ImageView permGrpIcon = (ImageView) findViewById(R.id.perm_icon);
            TextView permNameView = (TextView) findViewById(R.id.perm_name);
            permGrpIcon = (ImageView) PermissionInformation.getPermissionImageView(this);
            permNameView = (TextView) PermissionInformation.getPermissionTextView(this);
            Drawable icon = null;
            if (first) {
                icon = this.mContext.getResources().getDrawable(R.drawable.ic_text_dot);
            }
            CharSequence label = perm.mLabel;
            if (perm.mNew && newPermPrefix != null) {
                SpannableStringBuilder builder = new SpannableStringBuilder();
                Parcel parcel = Parcel.obtain();
                TextUtils.writeToParcel(newPermPrefix, parcel, 0);
                parcel.setDataPosition(0);
                CharSequence newStr = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel);
                parcel.recycle();
                builder.append(newStr);
                builder.append(label);
                label = builder;
            }
            permGrpIcon.setImageDrawable(icon);
            permNameView.setText(label);
            setOnClickListener(this);
        }

        public void onClick(View v) {
            if (this.mGroup != null && this.mPerm != null) {
                if (this.mDialog != null) {
                    this.mDialog.dismiss();
                }
                PackageManager pm = getContext().getPackageManager();
                Builder builder = new Builder(getContext());
                builder.setTitle(this.mGroup.mLabel);
                if (this.mPerm.descriptionRes != 0) {
                    builder.setMessage(this.mPerm.loadDescription(pm));
                } else {
                    CharSequence appName;
                    try {
                        appName = pm.getApplicationInfo(this.mPerm.packageName, 0).loadLabel(pm);
                    } catch (NameNotFoundException e) {
                        appName = this.mPerm.packageName;
                    }
                    StringBuilder sbuilder = new StringBuilder(LogPower.START_CHG_ROTATION);
                    sbuilder.append(getContext().getString(R.string.perms_description_app, new Object[]{appName}));
                    sbuilder.append("\n\n");
                    sbuilder.append(this.mPerm.name);
                    builder.setMessage(sbuilder.toString());
                }
                PermissionInformation.setPositiveButton(builder, this.mDialog);
                builder.setCancelable(true);
                builder.setIcon(this.mGroup.loadGroupIcon(getContext(), pm));
                addRevokeUIIfNecessary(builder);
                this.mDialog = builder.show();
                this.mDialog.setCanceledOnTouchOutside(true);
            }
        }

        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            if (this.mDialog != null) {
                this.mDialog.dismiss();
            }
        }

        private void addRevokeUIIfNecessary(Builder builder) {
            boolean isRequired = false;
            if (this.mShowRevokeUI) {
                if ((this.mPerm.mExistingReqFlags & 1) != 0) {
                    isRequired = true;
                }
                if (!isRequired) {
                    builder.setNegativeButton(R.string.revoke, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            PermissionItemView.this.getContext().getPackageManager().revokeRuntimePermission(PermissionItemView.this.mPackageName, PermissionItemView.this.mPerm.name, new UserHandle(PermissionItemView.this.mContext.getUserId()));
                            PermissionItemView.this.setVisibility(8);
                        }
                    });
                    builder.setPositiveButton(R.string.ok, null);
                }
            }
        }
    }

    private AppSecurityPermissions(Context context) {
        this.mPermGroups = new HashMap();
        this.mPermGroupsList = new ArrayList();
        this.mPermGroupComparator = new PermissionGroupInfoComparator();
        this.mPermComparator = new PermissionInfoComparator();
        this.mPermsList = new ArrayList();
        this.mContext = context;
        this.mInflater = (LayoutInflater) this.mContext.getSystemService("layout_inflater");
        this.mPm = this.mContext.getPackageManager();
        this.mNewPermPrefix = this.mContext.getText(R.string.perms_new_perm_prefix);
    }

    public AppSecurityPermissions(Context context, String packageName) {
        this(context);
        this.mPackageName = packageName;
        Set<MyPermissionInfo> permSet = new HashSet();
        try {
            PackageInfo pkgInfo = this.mPm.getPackageInfo(packageName, HwPerformance.PERF_EVENT_RAW_REQ);
            if (!(pkgInfo.applicationInfo == null || pkgInfo.applicationInfo.uid == -1)) {
                getAllUsedPermissions(pkgInfo.applicationInfo.uid, permSet);
            }
            this.mPermsList.addAll(permSet);
            setPermissions(this.mPermsList);
        } catch (NameNotFoundException e) {
            Log.w(TAG, "Couldn't retrieve permissions for package:" + packageName);
        }
    }

    public AppSecurityPermissions(Context context, PackageInfo info) {
        this(context);
        Set<MyPermissionInfo> permSet = new HashSet();
        if (info != null) {
            this.mPackageName = info.packageName;
            PackageInfo installedPkgInfo = null;
            if (info.requestedPermissions != null) {
                try {
                    installedPkgInfo = this.mPm.getPackageInfo(info.packageName, HwPerformance.PERF_EVENT_RAW_REQ);
                } catch (NameNotFoundException e) {
                }
                extractPerms(info, permSet, installedPkgInfo);
            }
            if (info.sharedUserId != null) {
                try {
                    getAllUsedPermissions(this.mPm.getUidForSharedUser(info.sharedUserId), permSet);
                } catch (NameNotFoundException e2) {
                    Log.w(TAG, "Couldn't retrieve shared user id for: " + info.packageName);
                }
            }
            this.mPermsList.addAll(permSet);
            setPermissions(this.mPermsList);
        }
    }

    public static View getPermissionItemView(Context context, CharSequence grpName, CharSequence description, boolean dangerous) {
        return getPermissionItemViewOld(context, (LayoutInflater) context.getSystemService("layout_inflater"), grpName, description, dangerous, context.getDrawable(dangerous ? R.drawable.ic_bullet_key_permission : R.drawable.ic_text_dot));
    }

    private void getAllUsedPermissions(int sharedUid, Set<MyPermissionInfo> permSet) {
        String[] sharedPkgList = this.mPm.getPackagesForUid(sharedUid);
        if (sharedPkgList != null && sharedPkgList.length != 0) {
            for (String sharedPkg : sharedPkgList) {
                getPermissionsForPackage(sharedPkg, permSet);
            }
        }
    }

    private void getPermissionsForPackage(String packageName, Set<MyPermissionInfo> permSet) {
        try {
            PackageInfo pkgInfo = this.mPm.getPackageInfo(packageName, HwPerformance.PERF_EVENT_RAW_REQ);
            extractPerms(pkgInfo, permSet, pkgInfo);
        } catch (NameNotFoundException e) {
            Log.w(TAG, "Couldn't retrieve permissions for package: " + packageName);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void extractPerms(PackageInfo info, Set<MyPermissionInfo> permSet, PackageInfo installedPkgInfo) {
        String[] strList = info.requestedPermissions;
        int[] flagsList = info.requestedPermissionsFlags;
        if (strList != null && strList.length != 0) {
            int i = 0;
            while (true) {
                int length = strList.length;
                if (i < r0) {
                    String permName = strList[i];
                    try {
                        PermissionInfo tmpPermInfo = this.mPm.getPermissionInfo(permName, 0);
                        if (tmpPermInfo != null) {
                            int existingIndex = -1;
                            if (!(installedPkgInfo == null || installedPkgInfo.requestedPermissions == null)) {
                                int j = 0;
                                while (true) {
                                    length = installedPkgInfo.requestedPermissions.length;
                                    if (j >= r0) {
                                        break;
                                    }
                                    if (permName.equals(installedPkgInfo.requestedPermissions[j])) {
                                        break;
                                    }
                                    j++;
                                }
                            }
                            int existingFlags = existingIndex >= 0 ? installedPkgInfo.requestedPermissionsFlags[existingIndex] : 0;
                            if (isDisplayablePermission(tmpPermInfo, flagsList[i], existingFlags)) {
                                String origGroupName = tmpPermInfo.group;
                                String groupName = origGroupName;
                                if (origGroupName == null) {
                                    groupName = tmpPermInfo.packageName;
                                    tmpPermInfo.group = groupName;
                                }
                                if (((MyPermissionGroupInfo) this.mPermGroups.get(groupName)) == null) {
                                    MyPermissionGroupInfo group;
                                    PermissionGroupInfo grp = null;
                                    if (origGroupName != null) {
                                        grp = this.mPm.getPermissionGroupInfo(origGroupName, 0);
                                    }
                                    if (grp != null) {
                                        group = new MyPermissionGroupInfo(grp);
                                    } else {
                                        tmpPermInfo.group = tmpPermInfo.packageName;
                                        if (((MyPermissionGroupInfo) this.mPermGroups.get(tmpPermInfo.group)) == null) {
                                            group = new MyPermissionGroupInfo(tmpPermInfo);
                                        }
                                        group = new MyPermissionGroupInfo(tmpPermInfo);
                                    }
                                    this.mPermGroups.put(tmpPermInfo.group, group);
                                }
                                boolean newPerm = installedPkgInfo != null ? (existingFlags & 2) == 0 : false;
                                MyPermissionInfo myPerm = new MyPermissionInfo(tmpPermInfo);
                                myPerm.mNewReqFlags = flagsList[i];
                                myPerm.mExistingReqFlags = existingFlags;
                                myPerm.mNew = newPerm;
                                permSet.add(myPerm);
                            }
                        }
                    } catch (NameNotFoundException e) {
                        Log.i(TAG, "Ignoring unknown permission:" + permName);
                    }
                    i++;
                } else {
                    return;
                }
            }
        }
    }

    public int getPermissionCount() {
        return getPermissionCount(WHICH_ALL);
    }

    private List<MyPermissionInfo> getPermissionList(MyPermissionGroupInfo grp, int which) {
        if (which == WHICH_NEW) {
            return grp.mNewPermissions;
        }
        return grp.mAllPermissions;
    }

    public int getPermissionCount(int which) {
        int N = 0;
        for (int i = 0; i < this.mPermGroupsList.size(); i++) {
            N += getPermissionList((MyPermissionGroupInfo) this.mPermGroupsList.get(i), which).size();
        }
        return N;
    }

    public View getPermissionsView() {
        return getPermissionsView(WHICH_ALL, false);
    }

    public View getPermissionsViewWithRevokeButtons() {
        return getPermissionsView(WHICH_ALL, true);
    }

    public View getPermissionsView(int which) {
        return getPermissionsView(which, false);
    }

    private View getPermissionsView(int which, boolean showRevokeUI) {
        LinearLayout permsView = (LinearLayout) this.mInflater.inflate((int) R.layout.app_perms_summary, null);
        LinearLayout displayList = (LinearLayout) permsView.findViewById(R.id.perms_list);
        View noPermsView = permsView.findViewById(R.id.no_permissions);
        displayPermissions(this.mPermGroupsList, displayList, which, showRevokeUI);
        if (displayList.getChildCount() <= 0) {
            noPermsView.setVisibility(0);
        }
        return permsView;
    }

    private void displayPermissions(List<MyPermissionGroupInfo> groups, LinearLayout permListView, int which, boolean showRevokeUI) {
        permListView.removeAllViews();
        int spacing = (int) (this.mContext.getResources().getDisplayMetrics().density * 8.0f);
        for (int i = 0; i < groups.size(); i++) {
            MyPermissionGroupInfo grp = (MyPermissionGroupInfo) groups.get(i);
            List<MyPermissionInfo> perms = getPermissionList(grp, which);
            int j = 0;
            while (j < perms.size()) {
                View view = getPermissionItemView(grp, (MyPermissionInfo) perms.get(j), j == 0, which != WHICH_NEW ? this.mNewPermPrefix : null, showRevokeUI);
                LayoutParams lp = new LayoutParams(-1, -2);
                if (j == 0) {
                    lp.topMargin = spacing;
                }
                if (j == grp.mAllPermissions.size() - 1) {
                    lp.bottomMargin = spacing;
                }
                if (permListView.getChildCount() == 0) {
                    lp.topMargin *= 2;
                }
                permListView.addView(view, (ViewGroup.LayoutParams) lp);
                j++;
            }
        }
    }

    private PermissionItemView getPermissionItemView(MyPermissionGroupInfo grp, MyPermissionInfo perm, boolean first, CharSequence newPermPrefix, boolean showRevokeUI) {
        return getPermissionItemView(this.mContext, this.mInflater, grp, perm, first, newPermPrefix, this.mPackageName, showRevokeUI);
    }

    private static PermissionItemView getPermissionItemView(Context context, LayoutInflater inflater, MyPermissionGroupInfo grp, MyPermissionInfo perm, boolean first, CharSequence newPermPrefix, String packageName, boolean showRevokeUI) {
        boolean z;
        PermissionItemView permView = (PermissionItemView) inflater.inflate((perm.flags & 1) != 0 ? R.layout.app_permission_item_money : R.layout.app_permission_item, null);
        if ((perm.flags & 1) != 0) {
            z = true;
        } else {
            z = false;
        }
        permView = PermissionInformation.getHwPermItemView(z, inflater);
        permView.setPermission(grp, perm, first, newPermPrefix, packageName, showRevokeUI);
        return permView;
    }

    private static View getPermissionItemViewOld(Context context, LayoutInflater inflater, CharSequence grpName, CharSequence permList, boolean dangerous, Drawable icon) {
        View inflate = inflater.inflate((int) R.layout.app_permission_item_old, null);
        inflate = PermissionInformation.getPermissionItemViewOld(inflater);
        TextView permGrpView = (TextView) inflate.findViewById(R.id.permission_group);
        TextView permDescView = (TextView) inflate.findViewById(R.id.permission_list);
        ImageView imgView = (ImageView) inflate.findViewById(R.id.perm_icon);
        permGrpView = PermissionInformation.getPermissionItemViewOldPermGrpView(inflate);
        permDescView = PermissionInformation.getPermissionItemViewOldPermDescView(inflate);
        PermissionInformation.getPermissionItemViewOldImgView(inflate).setImageDrawable(icon);
        if (grpName != null) {
            permGrpView.setText(grpName);
            permDescView.setText(permList);
        } else {
            permGrpView.setText(permList);
            permDescView.setVisibility(8);
        }
        return inflate;
    }

    private boolean isDisplayablePermission(PermissionInfo pInfo, int newReqFlags, int existingReqFlags) {
        boolean isNormal;
        int base = pInfo.protectionLevel & 15;
        if (base == 0) {
            isNormal = true;
        } else {
            isNormal = false;
        }
        if (isNormal) {
            return false;
        }
        boolean isDangerous = base != 1 ? (pInfo.protectionLevel & LogPower.START_CHG_ROTATION) != 0 : true;
        boolean isRequired = (newReqFlags & 1) != 0;
        boolean isDevelopment = (pInfo.protectionLevel & 32) != 0;
        boolean wasGranted = (existingReqFlags & 2) != 0;
        boolean isGranted = (newReqFlags & 2) != 0;
        if (isDangerous && (isRequired || wasGranted || isGranted)) {
            return true;
        }
        return isDevelopment && wasGranted;
    }

    private void addPermToList(List<MyPermissionInfo> permList, MyPermissionInfo pInfo) {
        if (pInfo.mLabel == null) {
            pInfo.mLabel = pInfo.loadLabel(this.mPm);
        }
        int idx = Collections.binarySearch(permList, pInfo, this.mPermComparator);
        if (idx < 0) {
            permList.add((-idx) - 1, pInfo);
        }
    }

    private void setPermissions(List<MyPermissionInfo> permList) {
        if (permList != null) {
            for (MyPermissionInfo pInfo : permList) {
                if (isDisplayablePermission(pInfo, pInfo.mNewReqFlags, pInfo.mExistingReqFlags)) {
                    MyPermissionGroupInfo group = (MyPermissionGroupInfo) this.mPermGroups.get(pInfo.group);
                    if (group != null) {
                        pInfo.mLabel = pInfo.loadLabel(this.mPm);
                        addPermToList(group.mAllPermissions, pInfo);
                        if (pInfo.mNew) {
                            addPermToList(group.mNewPermissions, pInfo);
                        }
                    }
                }
            }
        }
        for (MyPermissionGroupInfo pgrp : this.mPermGroups.values()) {
            if (pgrp.labelRes == 0 && pgrp.nonLocalizedLabel == null) {
                try {
                    pgrp.mLabel = this.mPm.getApplicationInfo(pgrp.packageName, 0).loadLabel(this.mPm);
                } catch (NameNotFoundException e) {
                    pgrp.mLabel = pgrp.loadLabel(this.mPm);
                }
            } else {
                pgrp.mLabel = pgrp.loadLabel(this.mPm);
            }
            this.mPermGroupsList.add(pgrp);
        }
        Collections.sort(this.mPermGroupsList, this.mPermGroupComparator);
    }
}
