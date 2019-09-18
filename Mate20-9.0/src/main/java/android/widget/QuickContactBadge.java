package android.widget;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telecom.PhoneAccount;
import android.util.AttributeSet;
import android.view.View;
import com.android.internal.R;

public class QuickContactBadge extends ImageView implements View.OnClickListener {
    static final int EMAIL_ID_COLUMN_INDEX = 0;
    static final String[] EMAIL_LOOKUP_PROJECTION = {"contact_id", ContactsContract.ContactsColumns.LOOKUP_KEY};
    static final int EMAIL_LOOKUP_STRING_COLUMN_INDEX = 1;
    private static final String EXTRA_URI_CONTENT = "uri_content";
    static final int PHONE_ID_COLUMN_INDEX = 0;
    static final String[] PHONE_LOOKUP_PROJECTION = {"_id", ContactsContract.ContactsColumns.LOOKUP_KEY, "number"};
    static final int PHONE_LOOKUP_STRING_COLUMN_INDEX = 1;
    private static final String TAG = "QuickContactBadge";
    private static final int TOKEN_EMAIL_LOOKUP = 0;
    private static final int TOKEN_EMAIL_LOOKUP_AND_TRIGGER = 2;
    private static final int TOKEN_PHONE_LOOKUP = 1;
    private static final int TOKEN_PHONE_LOOKUP_AND_TRIGGER = 3;
    private String mContactEmail;
    private String mContactPhone;
    /* access modifiers changed from: private */
    public Uri mContactUri;
    private Drawable mDefaultAvatar;
    protected String[] mExcludeMimes;
    private Bundle mExtras;
    private Drawable mOverlay;
    /* access modifiers changed from: private */
    public String mPrioritizedMimeType;
    private QueryHandler mQueryHandler;

    private class QueryHandler extends AsyncQueryHandler {
        public QueryHandler(ContentResolver cr) {
            super(cr);
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Code restructure failed: missing block: B:14:0x0043, code lost:
            if (r12 == null) goto L_0x0087;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:16:0x0049, code lost:
            if (r12.moveToFirst() == false) goto L_0x0087;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:18:0x005a, code lost:
            if (android.common.HwFrameworkFactory.getHwInnerTelephonyManager().isCallerInfofixedIndexValid(r3.getString(android.widget.QuickContactBadge.EXTRA_URI_CONTENT), r12) != false) goto L_0x005d;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:20:0x005d, code lost:
            r0 = android.provider.ContactsContract.Contacts.getLookupUri(r12.getLong(0), r12.getString(1));
         */
        /* JADX WARNING: Code restructure failed: missing block: B:21:0x006b, code lost:
            if (r12 == null) goto L_0x0087;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:23:0x0071, code lost:
            if (r12.moveToFirst() == false) goto L_0x0087;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:25:0x007f, code lost:
            r0 = android.provider.ContactsContract.Contacts.getLookupUri(r12.getLong(0), r12.getString(1));
         */
        /* JADX WARNING: Code restructure failed: missing block: B:29:0x0087, code lost:
            if (r12 == null) goto L_0x008c;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:30:0x0089, code lost:
            r12.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:31:0x008c, code lost:
            android.widget.QuickContactBadge.access$002(r9.this$0, r0);
            android.widget.QuickContactBadge.access$100(r9.this$0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:32:0x0096, code lost:
            if (r2 == false) goto L_0x00bc;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:34:0x009e, code lost:
            if (android.widget.QuickContactBadge.access$000(r9.this$0) == null) goto L_0x00bc;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:35:0x00a0, code lost:
            android.provider.ContactsContract.QuickContact.showQuickContact(r9.this$0.getContext(), (android.view.View) r9.this$0, android.widget.QuickContactBadge.access$000(r9.this$0), r9.this$0.mExcludeMimes, android.widget.QuickContactBadge.access$200(r9.this$0));
         */
        /* JADX WARNING: Code restructure failed: missing block: B:36:0x00bc, code lost:
            if (r1 == null) goto L_?;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:37:0x00be, code lost:
            r4 = new android.content.Intent("com.android.contacts.action.SHOW_OR_CREATE_CONTACT", r1);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:38:0x00c5, code lost:
            if (r3 == null) goto L_0x00d0;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:39:0x00c7, code lost:
            r3.remove(android.widget.QuickContactBadge.EXTRA_URI_CONTENT);
            r4.putExtras(r3);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:41:?, code lost:
            r9.this$0.getContext().startActivity(r4);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:43:0x00db, code lost:
            android.util.Slog.w(android.widget.QuickContactBadge.TAG, "Activity not found for intent");
         */
        /* JADX WARNING: Code restructure failed: missing block: B:44:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:45:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:46:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:47:?, code lost:
            return;
         */
        public void onQueryComplete(int token, Object cookie, Cursor cursor) {
            Uri lookupUri = null;
            Uri createUri = null;
            boolean trigger = false;
            Bundle extras = cookie != null ? (Bundle) cookie : new Bundle();
            switch (token) {
                case 0:
                    break;
                case 1:
                    break;
                case 2:
                    trigger = true;
                    createUri = Uri.fromParts("mailto", extras.getString(QuickContactBadge.EXTRA_URI_CONTENT), null);
                    break;
                case 3:
                    trigger = true;
                    try {
                        if (extras.getString(QuickContactBadge.EXTRA_URI_CONTENT) != null) {
                            createUri = Uri.fromParts(PhoneAccount.SCHEME_TEL, extras.getString(QuickContactBadge.EXTRA_URI_CONTENT), null);
                            break;
                        }
                    } catch (Throwable th) {
                        if (cursor != null) {
                            cursor.close();
                        }
                        throw th;
                    }
                    break;
            }
        }
    }

    public QuickContactBadge(Context context) {
        this(context, null);
    }

    public QuickContactBadge(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public QuickContactBadge(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public QuickContactBadge(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mExtras = null;
        this.mExcludeMimes = null;
        TypedArray styledAttributes = this.mContext.obtainStyledAttributes(R.styleable.Theme);
        this.mOverlay = styledAttributes.getDrawable(320);
        styledAttributes.recycle();
        setOnClickListener(this);
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
            this.mQueryHandler = new QueryHandler(this.mContext.getContentResolver());
        }
    }

    /* access modifiers changed from: protected */
    public void drawableStateChanged() {
        super.drawableStateChanged();
        Drawable overlay = this.mOverlay;
        if (overlay != null && overlay.isStateful() && overlay.setState(getDrawableState())) {
            invalidateDrawable(overlay);
        }
    }

    public void drawableHotspotChanged(float x, float y) {
        super.drawableHotspotChanged(x, y);
        if (this.mOverlay != null) {
            this.mOverlay.setHotspot(x, y);
        }
    }

    public void setMode(int size) {
    }

    public void setPrioritizedMimeType(String prioritizedMimeType) {
        this.mPrioritizedMimeType = prioritizedMimeType;
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isEnabled() && this.mOverlay != null && this.mOverlay.getIntrinsicWidth() != 0 && this.mOverlay.getIntrinsicHeight() != 0) {
            this.mOverlay.setBounds(0, 0, getWidth(), getHeight());
            if (this.mPaddingTop == 0 && this.mPaddingLeft == 0) {
                this.mOverlay.draw(canvas);
            } else {
                int saveCount = canvas.getSaveCount();
                canvas.save();
                canvas.translate((float) this.mPaddingLeft, (float) this.mPaddingTop);
                this.mOverlay.draw(canvas);
                canvas.restoreToCount(saveCount);
            }
        }
    }

    private boolean isAssigned() {
        return (this.mContactUri == null && this.mContactEmail == null && this.mContactPhone == null) ? false : true;
    }

    public void setImageToDefault() {
        if (this.mDefaultAvatar == null) {
            this.mDefaultAvatar = this.mContext.getDrawable(17302330);
        }
        setImageDrawable(this.mDefaultAvatar);
    }

    public void assignContactUri(Uri contactUri) {
        this.mContactUri = contactUri;
        this.mContactEmail = null;
        this.mContactPhone = null;
        onContactUriChanged();
    }

    public void assignContactFromEmail(String emailAddress, boolean lazyLookup) {
        assignContactFromEmail(emailAddress, lazyLookup, null);
    }

    public void assignContactFromEmail(String emailAddress, boolean lazyLookup, Bundle extras) {
        this.mContactEmail = emailAddress;
        this.mExtras = extras;
        if (lazyLookup || this.mQueryHandler == null) {
            this.mContactUri = null;
            onContactUriChanged();
            return;
        }
        this.mQueryHandler.startQuery(0, null, Uri.withAppendedPath(ContactsContract.CommonDataKinds.Email.CONTENT_LOOKUP_URI, Uri.encode(this.mContactEmail)), EMAIL_LOOKUP_PROJECTION, null, null, null);
    }

    public void assignContactFromPhone(String phoneNumber, boolean lazyLookup) {
        assignContactFromPhone(phoneNumber, lazyLookup, new Bundle());
    }

    public void assignContactFromPhone(String phoneNumber, boolean lazyLookup, Bundle extras) {
        this.mContactPhone = phoneNumber;
        this.mExtras = extras;
        if (lazyLookup || this.mQueryHandler == null) {
            this.mContactUri = null;
            onContactUriChanged();
            return;
        }
        this.mQueryHandler.startQuery(1, null, Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, this.mContactPhone), PHONE_LOOKUP_PROJECTION, null, null, null);
    }

    public void setOverlay(Drawable overlay) {
        this.mOverlay = overlay;
    }

    /* access modifiers changed from: private */
    public void onContactUriChanged() {
        setEnabled(isAssigned());
    }

    public void onClick(View v) {
        Bundle extras = this.mExtras == null ? new Bundle() : this.mExtras;
        if (this.mContactUri != null) {
            ContactsContract.QuickContact.showQuickContact(getContext(), (View) this, this.mContactUri, this.mExcludeMimes, this.mPrioritizedMimeType);
        } else if (this.mContactEmail != null && this.mQueryHandler != null) {
            extras.putString(EXTRA_URI_CONTENT, this.mContactEmail);
            this.mQueryHandler.startQuery(2, extras, Uri.withAppendedPath(ContactsContract.CommonDataKinds.Email.CONTENT_LOOKUP_URI, Uri.encode(this.mContactEmail)), EMAIL_LOOKUP_PROJECTION, null, null, null);
        } else if (this.mContactPhone != null && this.mQueryHandler != null) {
            extras.putString(EXTRA_URI_CONTENT, this.mContactPhone);
            this.mQueryHandler.startQuery(3, extras, Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, this.mContactPhone), PHONE_LOOKUP_PROJECTION, null, null, null);
        }
    }

    public CharSequence getAccessibilityClassName() {
        return QuickContactBadge.class.getName();
    }

    public void setExcludeMimes(String[] excludeMimes) {
        this.mExcludeMimes = excludeMimes;
    }
}
