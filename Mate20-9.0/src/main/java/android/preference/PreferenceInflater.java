package android.preference;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import com.android.internal.util.XmlUtils;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

class PreferenceInflater extends GenericInflater<Preference, PreferenceGroup> {
    private static final String EXTRA_TAG_NAME = "extra";
    private static final String INTENT_TAG_NAME = "intent";
    private static final String TAG = "PreferenceInflater";
    private PreferenceManager mPreferenceManager;

    public PreferenceInflater(Context context, PreferenceManager preferenceManager) {
        super(context);
        init(preferenceManager);
    }

    PreferenceInflater(GenericInflater<Preference, PreferenceGroup> original, PreferenceManager preferenceManager, Context newContext) {
        super(original, newContext);
        init(preferenceManager);
    }

    public GenericInflater<Preference, PreferenceGroup> cloneInContext(Context newContext) {
        return new PreferenceInflater(this, this.mPreferenceManager, newContext);
    }

    private void init(PreferenceManager preferenceManager) {
        this.mPreferenceManager = preferenceManager;
        setDefaultPackage("android.preference.");
    }

    /* access modifiers changed from: protected */
    public boolean onCreateCustomFromTag(XmlPullParser parser, Preference parentPreference, AttributeSet attrs) throws XmlPullParserException {
        String tag = parser.getName();
        if (tag.equals("intent")) {
            try {
                Intent intent = Intent.parseIntent(getContext().getResources(), parser, attrs);
                if (intent != null) {
                    parentPreference.setIntent(intent);
                }
                return true;
            } catch (IOException e) {
                XmlPullParserException ex = new XmlPullParserException("Error parsing preference");
                ex.initCause(e);
                throw ex;
            }
        } else if (!tag.equals(EXTRA_TAG_NAME)) {
            return false;
        } else {
            getContext().getResources().parseBundleExtra(EXTRA_TAG_NAME, attrs, parentPreference.getExtras());
            try {
                XmlUtils.skipCurrentTag(parser);
                return true;
            } catch (IOException e2) {
                XmlPullParserException ex2 = new XmlPullParserException("Error parsing preference");
                ex2.initCause(e2);
                throw ex2;
            }
        }
    }

    /* access modifiers changed from: protected */
    public PreferenceGroup onMergeRoots(PreferenceGroup givenRoot, boolean attachToGivenRoot, PreferenceGroup xmlRoot) {
        if (givenRoot != null) {
            return givenRoot;
        }
        xmlRoot.onAttachedToHierarchy(this.mPreferenceManager);
        return xmlRoot;
    }
}
