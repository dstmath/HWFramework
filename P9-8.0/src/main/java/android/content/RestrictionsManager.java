package android.content;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import com.android.internal.R;
import com.android.internal.util.XmlUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.xmlpull.v1.XmlPullParserException;

public class RestrictionsManager {
    public static final String ACTION_PERMISSION_RESPONSE_RECEIVED = "android.content.action.PERMISSION_RESPONSE_RECEIVED";
    public static final String ACTION_REQUEST_LOCAL_APPROVAL = "android.content.action.REQUEST_LOCAL_APPROVAL";
    public static final String ACTION_REQUEST_PERMISSION = "android.content.action.REQUEST_PERMISSION";
    public static final String EXTRA_PACKAGE_NAME = "android.content.extra.PACKAGE_NAME";
    public static final String EXTRA_REQUEST_BUNDLE = "android.content.extra.REQUEST_BUNDLE";
    public static final String EXTRA_REQUEST_ID = "android.content.extra.REQUEST_ID";
    public static final String EXTRA_REQUEST_TYPE = "android.content.extra.REQUEST_TYPE";
    public static final String EXTRA_RESPONSE_BUNDLE = "android.content.extra.RESPONSE_BUNDLE";
    public static final String META_DATA_APP_RESTRICTIONS = "android.content.APP_RESTRICTIONS";
    public static final String REQUEST_KEY_APPROVE_LABEL = "android.request.approve_label";
    public static final String REQUEST_KEY_DATA = "android.request.data";
    public static final String REQUEST_KEY_DENY_LABEL = "android.request.deny_label";
    public static final String REQUEST_KEY_ICON = "android.request.icon";
    public static final String REQUEST_KEY_ID = "android.request.id";
    public static final String REQUEST_KEY_MESSAGE = "android.request.mesg";
    public static final String REQUEST_KEY_NEW_REQUEST = "android.request.new_request";
    public static final String REQUEST_KEY_TITLE = "android.request.title";
    public static final String REQUEST_TYPE_APPROVAL = "android.request.type.approval";
    public static final String RESPONSE_KEY_ERROR_CODE = "android.response.errorcode";
    public static final String RESPONSE_KEY_MESSAGE = "android.response.msg";
    public static final String RESPONSE_KEY_RESPONSE_TIMESTAMP = "android.response.timestamp";
    public static final String RESPONSE_KEY_RESULT = "android.response.result";
    public static final int RESULT_APPROVED = 1;
    public static final int RESULT_DENIED = 2;
    public static final int RESULT_ERROR = 5;
    public static final int RESULT_ERROR_BAD_REQUEST = 1;
    public static final int RESULT_ERROR_INTERNAL = 3;
    public static final int RESULT_ERROR_NETWORK = 2;
    public static final int RESULT_NO_RESPONSE = 3;
    public static final int RESULT_UNKNOWN_REQUEST = 4;
    private static final String TAG = "RestrictionsManager";
    private static final String TAG_RESTRICTION = "restriction";
    private final Context mContext;
    private final IRestrictionsManager mService;

    public RestrictionsManager(Context context, IRestrictionsManager service) {
        this.mContext = context;
        this.mService = service;
    }

    public Bundle getApplicationRestrictions() {
        try {
            if (this.mService != null) {
                return this.mService.getApplicationRestrictions(this.mContext.getPackageName());
            }
            return null;
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean hasRestrictionsProvider() {
        try {
            if (this.mService != null) {
                return this.mService.hasRestrictionsProvider();
            }
            return false;
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public void requestPermission(String requestType, String requestId, PersistableBundle request) {
        if (requestType == null) {
            throw new NullPointerException("requestType cannot be null");
        } else if (requestId == null) {
            throw new NullPointerException("requestId cannot be null");
        } else if (request == null) {
            throw new NullPointerException("request cannot be null");
        } else {
            try {
                if (this.mService != null) {
                    this.mService.requestPermission(this.mContext.getPackageName(), requestType, requestId, request);
                }
            } catch (RemoteException re) {
                throw re.rethrowFromSystemServer();
            }
        }
    }

    public Intent createLocalApprovalIntent() {
        try {
            if (this.mService != null) {
                return this.mService.createLocalApprovalIntent();
            }
            return null;
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public void notifyPermissionResponse(String packageName, PersistableBundle response) {
        if (packageName == null) {
            throw new NullPointerException("packageName cannot be null");
        } else if (response == null) {
            throw new NullPointerException("request cannot be null");
        } else if (!response.containsKey(REQUEST_KEY_ID)) {
            throw new IllegalArgumentException("REQUEST_KEY_ID must be specified");
        } else if (response.containsKey(RESPONSE_KEY_RESULT)) {
            try {
                if (this.mService != null) {
                    this.mService.notifyPermissionResponse(packageName, response);
                }
            } catch (RemoteException re) {
                throw re.rethrowFromSystemServer();
            }
        } else {
            throw new IllegalArgumentException("RESPONSE_KEY_RESULT must be specified");
        }
    }

    public List<RestrictionEntry> getManifestRestrictions(String packageName) {
        try {
            ApplicationInfo appInfo = this.mContext.getPackageManager().getApplicationInfo(packageName, 128);
            if (appInfo == null || (appInfo.metaData.containsKey(META_DATA_APP_RESTRICTIONS) ^ 1) != 0) {
                return null;
            }
            return loadManifestRestrictions(packageName, appInfo.loadXmlMetaData(this.mContext.getPackageManager(), META_DATA_APP_RESTRICTIONS));
        } catch (NameNotFoundException e) {
            throw new IllegalArgumentException("No such package " + packageName);
        }
    }

    private List<RestrictionEntry> loadManifestRestrictions(String packageName, XmlResourceParser xml) {
        try {
            Context appContext = this.mContext.createPackageContext(packageName, 0);
            ArrayList<RestrictionEntry> restrictions = new ArrayList();
            try {
                int tagType = xml.next();
                while (tagType != 1) {
                    if (tagType == 2) {
                        RestrictionEntry restriction = loadRestrictionElement(appContext, xml);
                        if (restriction != null) {
                            restrictions.add(restriction);
                        }
                    }
                    tagType = xml.next();
                }
                return restrictions;
            } catch (XmlPullParserException e) {
                Log.w(TAG, "Reading restriction metadata for " + packageName, e);
                return null;
            } catch (IOException e2) {
                Log.w(TAG, "Reading restriction metadata for " + packageName, e2);
                return null;
            }
        } catch (NameNotFoundException e3) {
            return null;
        }
    }

    private RestrictionEntry loadRestrictionElement(Context appContext, XmlResourceParser xml) throws IOException, XmlPullParserException {
        if (xml.getName().equals(TAG_RESTRICTION)) {
            AttributeSet attrSet = Xml.asAttributeSet(xml);
            if (attrSet != null) {
                return loadRestriction(appContext, appContext.obtainStyledAttributes(attrSet, R.styleable.RestrictionEntry), xml);
            }
        }
        return null;
    }

    private RestrictionEntry loadRestriction(Context appContext, TypedArray a, XmlResourceParser xml) throws IOException, XmlPullParserException {
        String key = a.getString(3);
        int restrictionType = a.getInt(6, -1);
        String title = a.getString(2);
        String description = a.getString(0);
        int entries = a.getResourceId(1, 0);
        int entryValues = a.getResourceId(5, 0);
        if (restrictionType == -1) {
            Log.w(TAG, "restrictionType cannot be omitted");
            return null;
        } else if (key == null) {
            Log.w(TAG, "key cannot be omitted");
            return null;
        } else {
            RestrictionEntry restriction = new RestrictionEntry(restrictionType, key);
            restriction.setTitle(title);
            restriction.setDescription(description);
            if (entries != 0) {
                restriction.setChoiceEntries(appContext, entries);
            }
            if (entryValues != 0) {
                restriction.setChoiceValues(appContext, entryValues);
            }
            switch (restrictionType) {
                case 0:
                case 2:
                case 6:
                    restriction.setSelectedString(a.getString(4));
                    break;
                case 1:
                    restriction.setSelectedState(a.getBoolean(4, false));
                    break;
                case 4:
                    int resId = a.getResourceId(4, 0);
                    if (resId != 0) {
                        restriction.setAllSelectedStrings(appContext.getResources().getStringArray(resId));
                        break;
                    }
                    break;
                case 5:
                    restriction.setIntValue(a.getInt(4, 0));
                    break;
                case 7:
                case 8:
                    int outerDepth = xml.getDepth();
                    List<RestrictionEntry> restrictionEntries = new ArrayList();
                    while (XmlUtils.nextElementWithin(xml, outerDepth)) {
                        RestrictionEntry childEntry = loadRestrictionElement(appContext, xml);
                        if (childEntry == null) {
                            Log.w(TAG, "Child entry cannot be loaded for bundle restriction " + key);
                        } else {
                            restrictionEntries.add(childEntry);
                            if (restrictionType == 8 && childEntry.getType() != 7) {
                                Log.w(TAG, "bundle_array " + key + " can only contain entries of type bundle");
                            }
                        }
                    }
                    restriction.setRestrictions((RestrictionEntry[]) restrictionEntries.toArray(new RestrictionEntry[restrictionEntries.size()]));
                    break;
                default:
                    Log.w(TAG, "Unknown restriction type " + restrictionType);
                    break;
            }
            return restriction;
        }
    }

    public static Bundle convertRestrictionsToBundle(List<RestrictionEntry> entries) {
        Bundle bundle = new Bundle();
        for (RestrictionEntry entry : entries) {
            addRestrictionToBundle(bundle, entry);
        }
        return bundle;
    }

    private static Bundle addRestrictionToBundle(Bundle bundle, RestrictionEntry entry) {
        switch (entry.getType()) {
            case 0:
            case 6:
                bundle.putString(entry.getKey(), entry.getSelectedString());
                break;
            case 1:
                bundle.putBoolean(entry.getKey(), entry.getSelectedState());
                break;
            case 2:
            case 3:
            case 4:
                bundle.putStringArray(entry.getKey(), entry.getAllSelectedStrings());
                break;
            case 5:
                bundle.putInt(entry.getKey(), entry.getIntValue());
                break;
            case 7:
                bundle.putBundle(entry.getKey(), convertRestrictionsToBundle(Arrays.asList(entry.getRestrictions())));
                break;
            case 8:
                RestrictionEntry[] bundleRestrictionArray = entry.getRestrictions();
                Bundle[] bundleArray = new Bundle[bundleRestrictionArray.length];
                for (int i = 0; i < bundleRestrictionArray.length; i++) {
                    RestrictionEntry[] bundleRestrictions = bundleRestrictionArray[i].getRestrictions();
                    if (bundleRestrictions == null) {
                        Log.w(TAG, "addRestrictionToBundle: Non-bundle entry found in bundle array");
                        bundleArray[i] = new Bundle();
                    } else {
                        bundleArray[i] = convertRestrictionsToBundle(Arrays.asList(bundleRestrictions));
                    }
                }
                bundle.putParcelableArray(entry.getKey(), bundleArray);
                break;
            default:
                throw new IllegalArgumentException("Unsupported restrictionEntry type: " + entry.getType());
        }
        return bundle;
    }
}
