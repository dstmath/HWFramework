package android.preference;

import android.animation.LayoutTransition;
import android.annotation.UnsupportedAppUsage;
import android.app.Fragment;
import android.app.FragmentBreadCrumbs;
import android.app.FragmentTransaction;
import android.app.ListActivity;
import android.common.HwFrameworkMonitor;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.provider.Downloads;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.android.internal.R;
import com.android.internal.util.XmlUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParserException;

@Deprecated
public abstract class PreferenceActivity extends ListActivity implements PreferenceManager.OnPreferenceTreeClickListener, PreferenceFragment.OnPreferenceStartFragmentCallback {
    private static final String BACK_STACK_PREFS = ":android:prefs";
    private static final String CUR_HEADER_TAG = ":android:cur_header";
    public static final String EXTRA_NO_HEADERS = ":android:no_headers";
    private static final String EXTRA_PREFS_SET_BACK_TEXT = "extra_prefs_set_back_text";
    private static final String EXTRA_PREFS_SET_NEXT_TEXT = "extra_prefs_set_next_text";
    private static final String EXTRA_PREFS_SHOW_BUTTON_BAR = "extra_prefs_show_button_bar";
    private static final String EXTRA_PREFS_SHOW_SKIP = "extra_prefs_show_skip";
    public static final String EXTRA_SHOW_FRAGMENT = ":android:show_fragment";
    public static final String EXTRA_SHOW_FRAGMENT_ARGUMENTS = ":android:show_fragment_args";
    public static final String EXTRA_SHOW_FRAGMENT_SHORT_TITLE = ":android:show_fragment_short_title";
    public static final String EXTRA_SHOW_FRAGMENT_TITLE = ":android:show_fragment_title";
    private static final int FIRST_REQUEST_CODE = 100;
    private static final String HEADERS_TAG = ":android:headers";
    public static final long HEADER_ID_UNDEFINED = -1;
    private static final int MSG_BIND_PREFERENCES = 1;
    private static final int MSG_BUILD_HEADERS = 2;
    private static final String PREFERENCES_TAG = ":android:preferences";
    private static final String TAG = "PreferenceActivity";
    private CharSequence mActivityTitle;
    private Header mCurHeader;
    private FragmentBreadCrumbs mFragmentBreadCrumbs;
    private Handler mHandler = new Handler() {
        /* class android.preference.PreferenceActivity.AnonymousClass1 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            PreferenceActivity preferenceActivity;
            Header mappedHeader;
            int i = msg.what;
            if (i == 1) {
                PreferenceActivity.this.bindPreferences();
            } else if (i == 2) {
                ArrayList<Header> oldHeaders = new ArrayList<>(PreferenceActivity.this.mHeaders);
                PreferenceActivity.this.mHeaders.clear();
                PreferenceActivity preferenceActivity2 = PreferenceActivity.this;
                preferenceActivity2.onBuildHeaders(preferenceActivity2.mHeaders);
                if (PreferenceActivity.this.mAdapter instanceof BaseAdapter) {
                    ((BaseAdapter) PreferenceActivity.this.mAdapter).notifyDataSetChanged();
                }
                Header header = PreferenceActivity.this.onGetNewHeader();
                if (header != null && header.fragment != null) {
                    Header mappedHeader2 = PreferenceActivity.this.findBestMatchingHeader(header, oldHeaders);
                    if (mappedHeader2 == null || PreferenceActivity.this.mCurHeader != mappedHeader2) {
                        PreferenceActivity.this.switchToHeader(header);
                    }
                } else if (PreferenceActivity.this.mCurHeader != null && (mappedHeader = (preferenceActivity = PreferenceActivity.this).findBestMatchingHeader(preferenceActivity.mCurHeader, PreferenceActivity.this.mHeaders)) != null) {
                    PreferenceActivity.this.setSelectedHeader(mappedHeader);
                }
            }
        }
    };
    private final ArrayList<Header> mHeaders = new ArrayList<>();
    private ViewGroup mHeadersContainer;
    private FrameLayout mListFooter;
    private Button mNextButton;
    private int mPreferenceHeaderItemResId = 0;
    private boolean mPreferenceHeaderRemoveEmptyIcon = false;
    @UnsupportedAppUsage
    private PreferenceManager mPreferenceManager;
    @UnsupportedAppUsage
    private ViewGroup mPrefsContainer;
    private Bundle mSavedInstanceState;
    private boolean mSinglePane;

    private static class HeaderAdapter extends ArrayAdapter<Header> {
        private LayoutInflater mInflater;
        private int mLayoutResId;
        private boolean mRemoveIconIfEmpty;

        private static class HeaderViewHolder {
            ImageView icon;
            TextView summary;
            TextView title;

            private HeaderViewHolder() {
            }
        }

        public HeaderAdapter(Context context, List<Header> objects, int layoutResId, boolean removeIconBehavior) {
            super(context, 0, objects);
            this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.mLayoutResId = layoutResId;
            this.mRemoveIconIfEmpty = removeIconBehavior;
        }

        @Override // android.widget.ArrayAdapter, android.widget.Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            HeaderViewHolder holder;
            View view;
            if (convertView == null) {
                view = this.mInflater.inflate(this.mLayoutResId, parent, false);
                holder = new HeaderViewHolder();
                holder.icon = (ImageView) view.findViewById(16908294);
                holder.title = (TextView) view.findViewById(16908310);
                holder.summary = (TextView) view.findViewById(16908304);
                view.setTag(holder);
            } else {
                view = convertView;
                holder = (HeaderViewHolder) view.getTag();
            }
            Header header = (Header) getItem(position);
            if (!this.mRemoveIconIfEmpty) {
                holder.icon.setImageResource(header.iconRes);
            } else if (header.iconRes == 0) {
                holder.icon.setVisibility(8);
            } else {
                holder.icon.setVisibility(0);
                holder.icon.setImageResource(header.iconRes);
            }
            holder.title.setText(header.getTitle(getContext().getResources()));
            CharSequence summary = header.getSummary(getContext().getResources());
            if (!TextUtils.isEmpty(summary)) {
                holder.summary.setVisibility(0);
                holder.summary.setText(summary);
            } else {
                holder.summary.setVisibility(8);
            }
            return view;
        }
    }

    @Deprecated
    public static final class Header implements Parcelable {
        public static final Parcelable.Creator<Header> CREATOR = new Parcelable.Creator<Header>() {
            /* class android.preference.PreferenceActivity.Header.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public Header createFromParcel(Parcel source) {
                return new Header(source);
            }

            @Override // android.os.Parcelable.Creator
            public Header[] newArray(int size) {
                return new Header[size];
            }
        };
        public CharSequence breadCrumbShortTitle;
        public int breadCrumbShortTitleRes;
        public CharSequence breadCrumbTitle;
        public int breadCrumbTitleRes;
        public Bundle extras;
        public String fragment;
        public Bundle fragmentArguments;
        public int iconRes;
        public long id = -1;
        public Intent intent;
        public CharSequence summary;
        public int summaryRes;
        public CharSequence title;
        public int titleRes;

        public Header() {
        }

        public CharSequence getTitle(Resources res) {
            int i = this.titleRes;
            if (i != 0) {
                return res.getText(i);
            }
            return this.title;
        }

        public CharSequence getSummary(Resources res) {
            int i = this.summaryRes;
            if (i != 0) {
                return res.getText(i);
            }
            return this.summary;
        }

        public CharSequence getBreadCrumbTitle(Resources res) {
            int i = this.breadCrumbTitleRes;
            if (i != 0) {
                return res.getText(i);
            }
            return this.breadCrumbTitle;
        }

        public CharSequence getBreadCrumbShortTitle(Resources res) {
            int i = this.breadCrumbShortTitleRes;
            if (i != 0) {
                return res.getText(i);
            }
            return this.breadCrumbShortTitle;
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(this.id);
            dest.writeInt(this.titleRes);
            TextUtils.writeToParcel(this.title, dest, flags);
            dest.writeInt(this.summaryRes);
            TextUtils.writeToParcel(this.summary, dest, flags);
            dest.writeInt(this.breadCrumbTitleRes);
            TextUtils.writeToParcel(this.breadCrumbTitle, dest, flags);
            dest.writeInt(this.breadCrumbShortTitleRes);
            TextUtils.writeToParcel(this.breadCrumbShortTitle, dest, flags);
            dest.writeInt(this.iconRes);
            dest.writeString(this.fragment);
            dest.writeBundle(this.fragmentArguments);
            if (this.intent != null) {
                dest.writeInt(1);
                this.intent.writeToParcel(dest, flags);
            } else {
                dest.writeInt(0);
            }
            dest.writeBundle(this.extras);
        }

        public void readFromParcel(Parcel in) {
            this.id = in.readLong();
            this.titleRes = in.readInt();
            this.title = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
            this.summaryRes = in.readInt();
            this.summary = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
            this.breadCrumbTitleRes = in.readInt();
            this.breadCrumbTitle = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
            this.breadCrumbShortTitleRes = in.readInt();
            this.breadCrumbShortTitle = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
            this.iconRes = in.readInt();
            this.fragment = in.readString();
            this.fragmentArguments = in.readBundle();
            if (in.readInt() != 0) {
                this.intent = Intent.CREATOR.createFromParcel(in);
            }
            this.extras = in.readBundle();
        }

        Header(Parcel in) {
            readFromParcel(in);
        }
    }

    @Override // android.app.Activity
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != 16908332) {
            return super.onOptionsItemSelected(item);
        }
        onBackPressed();
        return true;
    }

    /* access modifiers changed from: protected */
    @Override // android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        Header header;
        super.onCreate(savedInstanceState);
        TypedArray sa = obtainStyledAttributes(null, R.styleable.PreferenceActivity, R.attr.preferenceActivityStyle, 0);
        int layoutResId = sa.getResourceId(0, R.layout.preference_list_content);
        this.mPreferenceHeaderItemResId = sa.getResourceId(1, R.layout.preference_header_item);
        this.mPreferenceHeaderRemoveEmptyIcon = sa.getBoolean(2, false);
        sa.recycle();
        setContentView(layoutResId);
        this.mListFooter = (FrameLayout) findViewById(R.id.list_footer);
        this.mPrefsContainer = (ViewGroup) findViewById(R.id.prefs_frame);
        this.mHeadersContainer = (ViewGroup) findViewById(R.id.headers);
        this.mSinglePane = onIsHidingHeaders() || !onIsMultiPane();
        String initialFragment = getIntent().getStringExtra(EXTRA_SHOW_FRAGMENT);
        Bundle initialArguments = getIntent().getBundleExtra(EXTRA_SHOW_FRAGMENT_ARGUMENTS);
        int initialTitle = getIntent().getIntExtra(EXTRA_SHOW_FRAGMENT_TITLE, 0);
        int initialShortTitle = getIntent().getIntExtra(EXTRA_SHOW_FRAGMENT_SHORT_TITLE, 0);
        this.mActivityTitle = getTitle();
        if (savedInstanceState != null) {
            ArrayList<Header> headers = savedInstanceState.getParcelableArrayList(HEADERS_TAG);
            if (headers != null) {
                this.mHeaders.addAll(headers);
                int curHeader = savedInstanceState.getInt(CUR_HEADER_TAG, -1);
                if (curHeader >= 0 && curHeader < this.mHeaders.size()) {
                    setSelectedHeader(this.mHeaders.get(curHeader));
                } else if (!this.mSinglePane && initialFragment == null) {
                    switchToHeader(onGetInitialHeader());
                }
            } else {
                showBreadCrumbs(getTitle(), null);
            }
        } else {
            if (!onIsHidingHeaders()) {
                onBuildHeaders(this.mHeaders);
            }
            if (initialFragment != null) {
                switchToHeader(initialFragment, initialArguments);
            } else if (!this.mSinglePane && this.mHeaders.size() > 0) {
                switchToHeader(onGetInitialHeader());
            }
        }
        if (this.mHeaders.size() > 0) {
            setListAdapter(new HeaderAdapter(this, this.mHeaders, this.mPreferenceHeaderItemResId, this.mPreferenceHeaderRemoveEmptyIcon));
            if (!this.mSinglePane) {
                getListView().setChoiceMode(1);
            }
        }
        if (!(!this.mSinglePane || initialFragment == null || initialTitle == 0)) {
            showBreadCrumbs(getText(initialTitle), initialShortTitle != 0 ? getText(initialShortTitle) : null);
        }
        if (this.mHeaders.size() == 0 && initialFragment == null) {
            setContentView(R.layout.preference_list_content_single);
            this.mListFooter = (FrameLayout) findViewById(R.id.list_footer);
            this.mPrefsContainer = (ViewGroup) findViewById(R.id.prefs);
            this.mPreferenceManager = new PreferenceManager(this, 100);
            this.mPreferenceManager.setOnPreferenceTreeClickListener(this);
            this.mHeadersContainer = null;
        } else if (this.mSinglePane) {
            if (initialFragment == null && this.mCurHeader == null) {
                this.mPrefsContainer.setVisibility(8);
            } else {
                this.mHeadersContainer.setVisibility(8);
            }
            ((ViewGroup) findViewById(R.id.prefs_container)).setLayoutTransition(new LayoutTransition());
        } else if (this.mHeaders.size() > 0 && (header = this.mCurHeader) != null) {
            setSelectedHeader(header);
        }
        Intent intent = getIntent();
        if (intent.getBooleanExtra(EXTRA_PREFS_SHOW_BUTTON_BAR, false)) {
            findViewById(16945153).setVisibility(0);
            Button backButton = (Button) findViewById(R.id.back_button);
            backButton.setOnClickListener(new View.OnClickListener() {
                /* class android.preference.PreferenceActivity.AnonymousClass2 */

                @Override // android.view.View.OnClickListener
                public void onClick(View v) {
                    PreferenceActivity.this.setResult(0);
                    PreferenceActivity.this.finish();
                }
            });
            Button skipButton = (Button) findViewById(R.id.skip_button);
            skipButton.setOnClickListener(new View.OnClickListener() {
                /* class android.preference.PreferenceActivity.AnonymousClass3 */

                @Override // android.view.View.OnClickListener
                public void onClick(View v) {
                    PreferenceActivity.this.setResult(-1);
                    PreferenceActivity.this.finish();
                }
            });
            this.mNextButton = (Button) findViewById(R.id.next_button);
            this.mNextButton.setOnClickListener(new View.OnClickListener() {
                /* class android.preference.PreferenceActivity.AnonymousClass4 */

                @Override // android.view.View.OnClickListener
                public void onClick(View v) {
                    PreferenceActivity.this.setResult(-1);
                    PreferenceActivity.this.finish();
                }
            });
            if (intent.hasExtra(EXTRA_PREFS_SET_NEXT_TEXT)) {
                String buttonText = intent.getStringExtra(EXTRA_PREFS_SET_NEXT_TEXT);
                if (TextUtils.isEmpty(buttonText)) {
                    this.mNextButton.setVisibility(8);
                } else {
                    this.mNextButton.setText(buttonText);
                }
            }
            if (intent.hasExtra(EXTRA_PREFS_SET_BACK_TEXT)) {
                String buttonText2 = intent.getStringExtra(EXTRA_PREFS_SET_BACK_TEXT);
                if (TextUtils.isEmpty(buttonText2)) {
                    backButton.setVisibility(8);
                } else {
                    backButton.setText(buttonText2);
                }
            }
            if (intent.getBooleanExtra(EXTRA_PREFS_SHOW_SKIP, false)) {
                skipButton.setVisibility(0);
            }
        }
    }

    @Override // android.app.Activity
    public void onBackPressed() {
        if (this.mCurHeader == null || !this.mSinglePane || getFragmentManager().getBackStackEntryCount() != 0 || getIntent().getStringExtra(EXTRA_SHOW_FRAGMENT) != null) {
            super.onBackPressed();
            return;
        }
        this.mCurHeader = null;
        this.mPrefsContainer.setVisibility(8);
        this.mHeadersContainer.setVisibility(0);
        CharSequence charSequence = this.mActivityTitle;
        if (charSequence != null) {
            showBreadCrumbs(charSequence, null);
        }
        getListView().clearChoices();
    }

    public boolean hasHeaders() {
        ViewGroup viewGroup = this.mHeadersContainer;
        return viewGroup != null && viewGroup.getVisibility() == 0;
    }

    @UnsupportedAppUsage
    public List<Header> getHeaders() {
        return this.mHeaders;
    }

    public boolean isMultiPane() {
        return !this.mSinglePane;
    }

    public boolean onIsMultiPane() {
        return getResources().getBoolean(R.bool.preferences_prefer_dual_pane);
    }

    public boolean onIsHidingHeaders() {
        return getIntent().getBooleanExtra(EXTRA_NO_HEADERS, false);
    }

    public Header onGetInitialHeader() {
        for (int i = 0; i < this.mHeaders.size(); i++) {
            Header h = this.mHeaders.get(i);
            if (h.fragment != null) {
                return h;
            }
        }
        throw new IllegalStateException("Must have at least one header with a fragment");
    }

    public Header onGetNewHeader() {
        return null;
    }

    public void onBuildHeaders(List<Header> list) {
    }

    public void invalidateHeaders() {
        if (!this.mHandler.hasMessages(2)) {
            this.mHandler.sendEmptyMessage(2);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:132:0x01e4  */
    public void loadHeadersFromResource(int resid, List<Header> target) {
        XmlPullParserException e;
        XmlPullParserException e2;
        IOException e3;
        AttributeSet attrs;
        int i;
        int i2;
        String nodeName;
        Bundle curBundle;
        int innerDepth;
        XmlResourceParser parser = null;
        try {
            try {
                parser = getResources().getXml(resid);
                attrs = Xml.asAttributeSet(parser);
                nodeName = parser.getName();
            } catch (XmlPullParserException e4) {
                e2 = e4;
                throw new RuntimeException("Error parsing headers", e2);
            } catch (IOException e5) {
                e3 = e5;
                throw new RuntimeException("Error parsing headers", e3);
            } catch (Throwable th) {
                e = th;
                if (parser != null) {
                    parser.close();
                }
                throw e;
            }
        } catch (XmlPullParserException e6) {
            e2 = e6;
            throw new RuntimeException("Error parsing headers", e2);
        } catch (IOException e7) {
            e3 = e7;
            throw new RuntimeException("Error parsing headers", e3);
        } catch (Throwable th2) {
            e = th2;
            if (parser != null) {
            }
            throw e;
        }
        while (true) {
            int type = parser.next();
            i = 2;
            i2 = 1;
            if (type == 1 || type == 2) {
                break;
            }
        }
        if ("preference-headers".equals(nodeName)) {
            Bundle curBundle2 = null;
            int outerDepth = parser.getDepth();
            while (true) {
                int type2 = parser.next();
                if (type2 == i2) {
                    break;
                }
                if (type2 == 3) {
                    if (parser.getDepth() <= outerDepth) {
                        break;
                    }
                }
                if (type2 == 3) {
                    curBundle = curBundle2;
                } else if (type2 == 4) {
                    curBundle = curBundle2;
                } else if (Downloads.Impl.RequestHeaders.COLUMN_HEADER.equals(parser.getName())) {
                    Header header = new Header();
                    try {
                        TypedArray sa = obtainStyledAttributes(attrs, R.styleable.PreferenceHeader);
                        header.id = (long) sa.getResourceId(i2, -1);
                        TypedValue tv = sa.peekValue(i);
                        if (tv != null && tv.type == 3) {
                            if (tv.resourceId != 0) {
                                header.titleRes = tv.resourceId;
                            } else {
                                header.title = tv.string;
                            }
                        }
                        TypedValue tv2 = sa.peekValue(3);
                        if (tv2 != null && tv2.type == 3) {
                            if (tv2.resourceId != 0) {
                                header.summaryRes = tv2.resourceId;
                            } else {
                                header.summary = tv2.string;
                            }
                        }
                        TypedValue tv3 = sa.peekValue(5);
                        if (tv3 != null && tv3.type == 3) {
                            if (tv3.resourceId != 0) {
                                header.breadCrumbTitleRes = tv3.resourceId;
                            } else {
                                header.breadCrumbTitle = tv3.string;
                            }
                        }
                        TypedValue tv4 = sa.peekValue(6);
                        if (tv4 != null && tv4.type == 3) {
                            if (tv4.resourceId != 0) {
                                header.breadCrumbShortTitleRes = tv4.resourceId;
                            } else {
                                header.breadCrumbShortTitle = tv4.string;
                            }
                        }
                        header.iconRes = sa.getResourceId(0, 0);
                        header.fragment = sa.getString(4);
                        sa.recycle();
                        if (curBundle2 == null) {
                            curBundle2 = new Bundle();
                        } else {
                            curBundle2 = curBundle2;
                        }
                        innerDepth = parser.getDepth();
                        if (curBundle2.size() > 0) {
                            header.fragmentArguments = curBundle2;
                            curBundle2 = null;
                        }
                    } catch (XmlPullParserException e8) {
                        e2 = e8;
                        throw new RuntimeException("Error parsing headers", e2);
                    } catch (IOException e9) {
                        e3 = e9;
                        throw new RuntimeException("Error parsing headers", e3);
                    } catch (Throwable th3) {
                        e = th3;
                        if (parser != null) {
                        }
                        throw e;
                    }
                    while (true) {
                        int type3 = parser.next();
                        if (type3 == 1 || (type3 == 3 && parser.getDepth() <= innerDepth)) {
                            break;
                        } else if (type3 != 3) {
                            if (type3 != 4) {
                                String innerNodeName = parser.getName();
                                if (innerNodeName.equals(HwFrameworkMonitor.KEY_EXTRA)) {
                                    getResources().parseBundleExtra(HwFrameworkMonitor.KEY_EXTRA, attrs, curBundle2);
                                    XmlUtils.skipCurrentTag(parser);
                                } else if (innerNodeName.equals("intent")) {
                                    header.intent = Intent.parseIntent(getResources(), parser, attrs);
                                } else {
                                    XmlUtils.skipCurrentTag(parser);
                                }
                            }
                        }
                    }
                    try {
                        target.add(header);
                        i = 2;
                        i2 = 1;
                    } catch (XmlPullParserException e10) {
                        e2 = e10;
                        throw new RuntimeException("Error parsing headers", e2);
                    } catch (IOException e11) {
                        e3 = e11;
                        throw new RuntimeException("Error parsing headers", e3);
                    }
                } else {
                    XmlUtils.skipCurrentTag(parser);
                    curBundle2 = curBundle2;
                    i = 2;
                    i2 = 1;
                }
                curBundle2 = curBundle;
                i = 2;
                i2 = 1;
            }
            parser.close();
            return;
        }
        throw new RuntimeException("XML document must start with <preference-headers> tag; found" + nodeName + " at " + parser.getPositionDescription());
    }

    /* access modifiers changed from: protected */
    public boolean isValidFragment(String fragmentName) {
        if (getApplicationInfo().targetSdkVersion < 19) {
            return true;
        }
        throw new RuntimeException("Subclasses of PreferenceActivity must override isValidFragment(String) to verify that the Fragment class is valid! " + getClass().getName() + " has not checked if fragment " + fragmentName + " is valid.");
    }

    public void setListFooter(View view) {
        this.mListFooter.removeAllViews();
        this.mListFooter.addView(view, new FrameLayout.LayoutParams(-1, -2));
    }

    /* access modifiers changed from: protected */
    @Override // android.app.Activity
    public void onStop() {
        super.onStop();
        PreferenceManager preferenceManager = this.mPreferenceManager;
        if (preferenceManager != null) {
            preferenceManager.dispatchActivityStop();
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.app.ListActivity, android.app.Activity
    public void onDestroy() {
        this.mHandler.removeMessages(1);
        this.mHandler.removeMessages(2);
        super.onDestroy();
        PreferenceManager preferenceManager = this.mPreferenceManager;
        if (preferenceManager != null) {
            preferenceManager.dispatchActivityDestroy();
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.app.Activity
    public void onSaveInstanceState(Bundle outState) {
        PreferenceScreen preferenceScreen;
        int index;
        super.onSaveInstanceState(outState);
        if (this.mHeaders.size() > 0) {
            outState.putParcelableArrayList(HEADERS_TAG, this.mHeaders);
            Header header = this.mCurHeader;
            if (header != null && (index = this.mHeaders.indexOf(header)) >= 0) {
                outState.putInt(CUR_HEADER_TAG, index);
            }
        }
        if (this.mPreferenceManager != null && (preferenceScreen = getPreferenceScreen()) != null) {
            Bundle container = new Bundle();
            preferenceScreen.saveHierarchyState(container);
            outState.putBundle(PREFERENCES_TAG, container);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.app.ListActivity, android.app.Activity
    public void onRestoreInstanceState(Bundle state) {
        Header header;
        Bundle container;
        PreferenceScreen preferenceScreen;
        if (this.mPreferenceManager == null || (container = state.getBundle(PREFERENCES_TAG)) == null || (preferenceScreen = getPreferenceScreen()) == null) {
            super.onRestoreInstanceState(state);
            if (!this.mSinglePane && (header = this.mCurHeader) != null) {
                setSelectedHeader(header);
                return;
            }
            return;
        }
        preferenceScreen.restoreHierarchyState(container);
        this.mSavedInstanceState = state;
    }

    /* access modifiers changed from: protected */
    @Override // android.app.Activity
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        PreferenceManager preferenceManager = this.mPreferenceManager;
        if (preferenceManager != null) {
            preferenceManager.dispatchActivityResult(requestCode, resultCode, data);
        }
    }

    @Override // android.app.ListActivity, android.app.Activity, android.view.Window.Callback
    public void onContentChanged() {
        super.onContentChanged();
        if (this.mPreferenceManager != null) {
            postBindPreferences();
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.app.ListActivity
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (isResumed()) {
            super.onListItemClick(l, v, position, id);
            if (this.mAdapter != null) {
                Object item = this.mAdapter.getItem(position);
                if (item instanceof Header) {
                    onHeaderClick((Header) item, position);
                }
            }
        }
    }

    public void onHeaderClick(Header header, int position) {
        if (header.fragment != null) {
            switchToHeader(header);
        } else if (header.intent != null) {
            startActivity(header.intent);
        }
    }

    public Intent onBuildStartFragmentIntent(String fragmentName, Bundle args, int titleRes, int shortTitleRes) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClass(this, getClass());
        intent.putExtra(EXTRA_SHOW_FRAGMENT, fragmentName);
        intent.putExtra(EXTRA_SHOW_FRAGMENT_ARGUMENTS, args);
        intent.putExtra(EXTRA_SHOW_FRAGMENT_TITLE, titleRes);
        intent.putExtra(EXTRA_SHOW_FRAGMENT_SHORT_TITLE, shortTitleRes);
        intent.putExtra(EXTRA_NO_HEADERS, true);
        return intent;
    }

    public void startWithFragment(String fragmentName, Bundle args, Fragment resultTo, int resultRequestCode) {
        startWithFragment(fragmentName, args, resultTo, resultRequestCode, 0, 0);
    }

    public void startWithFragment(String fragmentName, Bundle args, Fragment resultTo, int resultRequestCode, int titleRes, int shortTitleRes) {
        Intent intent = onBuildStartFragmentIntent(fragmentName, args, titleRes, shortTitleRes);
        if (resultTo == null) {
            startActivity(intent);
        } else {
            resultTo.startActivityForResult(intent, resultRequestCode);
        }
    }

    public void showBreadCrumbs(CharSequence title, CharSequence shortTitle) {
        if (this.mFragmentBreadCrumbs == null) {
            try {
                this.mFragmentBreadCrumbs = (FragmentBreadCrumbs) findViewById(16908310);
                FragmentBreadCrumbs fragmentBreadCrumbs = this.mFragmentBreadCrumbs;
                if (fragmentBreadCrumbs != null) {
                    if (this.mSinglePane) {
                        fragmentBreadCrumbs.setVisibility(8);
                        View bcSection = findViewById(R.id.breadcrumb_section);
                        if (bcSection != null) {
                            bcSection.setVisibility(8);
                        }
                        setTitle(title);
                    }
                    this.mFragmentBreadCrumbs.setMaxVisible(2);
                    this.mFragmentBreadCrumbs.setActivity(this);
                } else if (title != null) {
                    setTitle(title);
                    return;
                } else {
                    return;
                }
            } catch (ClassCastException e) {
                setTitle(title);
                return;
            }
        }
        if (this.mFragmentBreadCrumbs.getVisibility() != 0) {
            setTitle(title);
            return;
        }
        this.mFragmentBreadCrumbs.setTitle(title, shortTitle);
        this.mFragmentBreadCrumbs.setParentTitle(null, null, null);
    }

    public void setParentTitle(CharSequence title, CharSequence shortTitle, View.OnClickListener listener) {
        FragmentBreadCrumbs fragmentBreadCrumbs = this.mFragmentBreadCrumbs;
        if (fragmentBreadCrumbs != null) {
            fragmentBreadCrumbs.setParentTitle(title, shortTitle, listener);
        }
    }

    /* access modifiers changed from: package-private */
    public void setSelectedHeader(Header header) {
        this.mCurHeader = header;
        int index = this.mHeaders.indexOf(header);
        if (index >= 0) {
            getListView().setItemChecked(index, true);
        } else {
            getListView().clearChoices();
        }
        showBreadCrumbs(header);
    }

    /* access modifiers changed from: package-private */
    public void showBreadCrumbs(Header header) {
        if (header != null) {
            CharSequence title = header.getBreadCrumbTitle(getResources());
            if (title == null) {
                title = header.getTitle(getResources());
            }
            if (title == null) {
                title = getTitle();
            }
            showBreadCrumbs(title, header.getBreadCrumbShortTitle(getResources()));
            return;
        }
        showBreadCrumbs(getTitle(), null);
    }

    private void switchToHeaderInner(String fragmentName, Bundle args) {
        int i;
        getFragmentManager().popBackStack(BACK_STACK_PREFS, 1);
        if (isValidFragment(fragmentName)) {
            Fragment f = Fragment.instantiate(this, fragmentName, args);
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            if (this.mSinglePane) {
                i = 0;
            } else {
                i = 4099;
            }
            transaction.setTransition(i);
            transaction.replace(R.id.prefs, f);
            transaction.commitAllowingStateLoss();
            if (this.mSinglePane && this.mPrefsContainer.getVisibility() == 8) {
                this.mPrefsContainer.setVisibility(0);
                this.mHeadersContainer.setVisibility(8);
                return;
            }
            return;
        }
        throw new IllegalArgumentException("Invalid fragment for this activity: " + fragmentName);
    }

    public void switchToHeader(String fragmentName, Bundle args) {
        Header selectedHeader = null;
        int i = 0;
        while (true) {
            if (i >= this.mHeaders.size()) {
                break;
            } else if (fragmentName.equals(this.mHeaders.get(i).fragment)) {
                selectedHeader = this.mHeaders.get(i);
                break;
            } else {
                i++;
            }
        }
        setSelectedHeader(selectedHeader);
        switchToHeaderInner(fragmentName, args);
    }

    public void switchToHeader(Header header) {
        if (this.mCurHeader == header) {
            getFragmentManager().popBackStack(BACK_STACK_PREFS, 1);
        } else if (header.fragment != null) {
            switchToHeaderInner(header.fragment, header.fragmentArguments);
            setSelectedHeader(header);
        } else {
            throw new IllegalStateException("can't switch to header that has no fragment");
        }
    }

    /* access modifiers changed from: package-private */
    public Header findBestMatchingHeader(Header cur, ArrayList<Header> from) {
        Header oh;
        ArrayList<Header> matches = new ArrayList<>();
        int j = 0;
        while (true) {
            if (j >= from.size()) {
                break;
            }
            oh = from.get(j);
            if (cur == oh || (cur.id != -1 && cur.id == oh.id)) {
                break;
            }
            if (cur.fragment != null) {
                if (cur.fragment.equals(oh.fragment)) {
                    matches.add(oh);
                }
            } else if (cur.intent != null) {
                if (cur.intent.equals(oh.intent)) {
                    matches.add(oh);
                }
            } else if (cur.title != null && cur.title.equals(oh.title)) {
                matches.add(oh);
            }
            j++;
        }
        matches.clear();
        matches.add(oh);
        int NM = matches.size();
        if (NM == 1) {
            return matches.get(0);
        }
        if (NM <= 1) {
            return null;
        }
        for (int j2 = 0; j2 < NM; j2++) {
            Header oh2 = matches.get(j2);
            if (cur.fragmentArguments != null && cur.fragmentArguments.equals(oh2.fragmentArguments)) {
                return oh2;
            }
            if (cur.extras != null && cur.extras.equals(oh2.extras)) {
                return oh2;
            }
            if (cur.title != null && cur.title.equals(oh2.title)) {
                return oh2;
            }
        }
        return null;
    }

    public void startPreferenceFragment(Fragment fragment, boolean push) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.prefs, fragment);
        if (push) {
            transaction.setTransition(4097);
            transaction.addToBackStack(BACK_STACK_PREFS);
        } else {
            transaction.setTransition(4099);
        }
        transaction.commitAllowingStateLoss();
    }

    public void startPreferencePanel(String fragmentClass, Bundle args, int titleRes, CharSequence titleText, Fragment resultTo, int resultRequestCode) {
        Fragment f = Fragment.instantiate(this, fragmentClass, args);
        if (resultTo != null) {
            f.setTargetFragment(resultTo, resultRequestCode);
        }
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.prefs, f);
        if (titleRes != 0) {
            transaction.setBreadCrumbTitle(titleRes);
        } else if (titleText != null) {
            transaction.setBreadCrumbTitle(titleText);
        }
        transaction.setTransition(4097);
        transaction.addToBackStack(BACK_STACK_PREFS);
        transaction.commitAllowingStateLoss();
    }

    public void finishPreferencePanel(Fragment caller, int resultCode, Intent resultData) {
        onBackPressed();
        if (caller != null && caller.getTargetFragment() != null) {
            caller.getTargetFragment().onActivityResult(caller.getTargetRequestCode(), resultCode, resultData);
        }
    }

    @Override // android.preference.PreferenceFragment.OnPreferenceStartFragmentCallback
    public boolean onPreferenceStartFragment(PreferenceFragment caller, Preference pref) {
        startPreferencePanel(pref.getFragment(), pref.getExtras(), pref.getTitleRes(), pref.getTitle(), null, 0);
        return true;
    }

    @UnsupportedAppUsage
    private void postBindPreferences() {
        if (!this.mHandler.hasMessages(1)) {
            this.mHandler.obtainMessage(1).sendToTarget();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void bindPreferences() {
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        if (preferenceScreen != null) {
            preferenceScreen.bind(getListView());
            Bundle bundle = this.mSavedInstanceState;
            if (bundle != null) {
                super.onRestoreInstanceState(bundle);
                this.mSavedInstanceState = null;
            }
        }
    }

    @Deprecated
    public PreferenceManager getPreferenceManager() {
        return this.mPreferenceManager;
    }

    @UnsupportedAppUsage
    private void requirePreferenceManager() {
        if (this.mPreferenceManager != null) {
            return;
        }
        if (this.mAdapter == null) {
            throw new RuntimeException("This should be called after super.onCreate.");
        }
        throw new RuntimeException("Modern two-pane PreferenceActivity requires use of a PreferenceFragment");
    }

    @Deprecated
    public void setPreferenceScreen(PreferenceScreen preferenceScreen) {
        requirePreferenceManager();
        if (this.mPreferenceManager.setPreferences(preferenceScreen) && preferenceScreen != null) {
            postBindPreferences();
            CharSequence title = getPreferenceScreen().getTitle();
            if (title != null) {
                setTitle(title);
            }
        }
    }

    @Deprecated
    public PreferenceScreen getPreferenceScreen() {
        PreferenceManager preferenceManager = this.mPreferenceManager;
        if (preferenceManager != null) {
            return preferenceManager.getPreferenceScreen();
        }
        return null;
    }

    @Deprecated
    public void addPreferencesFromIntent(Intent intent) {
        requirePreferenceManager();
        setPreferenceScreen(this.mPreferenceManager.inflateFromIntent(intent, getPreferenceScreen()));
    }

    @Deprecated
    public void addPreferencesFromResource(int preferencesResId) {
        requirePreferenceManager();
        setPreferenceScreen(this.mPreferenceManager.inflateFromResource(this, preferencesResId, getPreferenceScreen()));
    }

    @Override // android.preference.PreferenceManager.OnPreferenceTreeClickListener
    @Deprecated
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        return false;
    }

    @Deprecated
    public Preference findPreference(CharSequence key) {
        PreferenceManager preferenceManager = this.mPreferenceManager;
        if (preferenceManager == null) {
            return null;
        }
        return preferenceManager.findPreference(key);
    }

    /* access modifiers changed from: protected */
    @Override // android.app.Activity
    public void onNewIntent(Intent intent) {
        PreferenceManager preferenceManager = this.mPreferenceManager;
        if (preferenceManager != null) {
            preferenceManager.dispatchNewIntent(intent);
        }
    }

    /* access modifiers changed from: protected */
    public boolean hasNextButton() {
        return this.mNextButton != null;
    }

    /* access modifiers changed from: protected */
    public Button getNextButton() {
        return this.mNextButton;
    }
}
