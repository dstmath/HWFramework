package android.widget;

import android.annotation.UnsupportedAppUsage;
import android.common.HwFrameworkFactory;
import android.content.ActivityNotFoundException;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telecom.PhoneAccount;
import android.util.AttributeSet;
import android.util.Slog;
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
    private Uri mContactUri;
    private Drawable mDefaultAvatar;
    protected String[] mExcludeMimes;
    private Bundle mExtras;
    @UnsupportedAppUsage
    private Drawable mOverlay;
    private String mPrioritizedMimeType;
    private QueryHandler mQueryHandler;

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
        this.mOverlay = styledAttributes.getDrawable(325);
        styledAttributes.recycle();
        setOnClickListener(this);
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.ImageView, android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
            this.mQueryHandler = new QueryHandler(this.mContext.getContentResolver());
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.ImageView, android.view.View
    public void drawableStateChanged() {
        super.drawableStateChanged();
        Drawable overlay = this.mOverlay;
        if (overlay != null && overlay.isStateful() && overlay.setState(getDrawableState())) {
            invalidateDrawable(overlay);
        }
    }

    @Override // android.widget.ImageView, android.view.View
    public void drawableHotspotChanged(float x, float y) {
        super.drawableHotspotChanged(x, y);
        Drawable drawable = this.mOverlay;
        if (drawable != null) {
            drawable.setHotspot(x, y);
        }
    }

    public void setMode(int size) {
    }

    public void setPrioritizedMimeType(String prioritizedMimeType) {
        this.mPrioritizedMimeType = prioritizedMimeType;
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.ImageView, android.view.View
    public void onDraw(Canvas canvas) {
        Drawable drawable;
        super.onDraw(canvas);
        if (isEnabled() && (drawable = this.mOverlay) != null && drawable.getIntrinsicWidth() != 0 && this.mOverlay.getIntrinsicHeight() != 0) {
            this.mOverlay.setBounds(0, 0, getWidth(), getHeight());
            if (this.mPaddingTop == 0 && this.mPaddingLeft == 0) {
                this.mOverlay.draw(canvas);
                return;
            }
            int saveCount = canvas.getSaveCount();
            canvas.save();
            canvas.translate((float) this.mPaddingLeft, (float) this.mPaddingTop);
            this.mOverlay.draw(canvas);
            canvas.restoreToCount(saveCount);
        }
    }

    private boolean isAssigned() {
        return (this.mContactUri == null && this.mContactEmail == null && this.mContactPhone == null) ? false : true;
    }

    public void setImageToDefault() {
        if (this.mDefaultAvatar == null) {
            this.mDefaultAvatar = this.mContext.getDrawable(R.drawable.ic_contact_picture);
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
        QueryHandler queryHandler;
        this.mContactEmail = emailAddress;
        this.mExtras = extras;
        if (lazyLookup || (queryHandler = this.mQueryHandler) == null) {
            this.mContactUri = null;
            onContactUriChanged();
            return;
        }
        queryHandler.startQuery(0, null, Uri.withAppendedPath(ContactsContract.CommonDataKinds.Email.CONTENT_LOOKUP_URI, Uri.encode(this.mContactEmail)), EMAIL_LOOKUP_PROJECTION, null, null, null);
    }

    public void assignContactFromPhone(String phoneNumber, boolean lazyLookup) {
        assignContactFromPhone(phoneNumber, lazyLookup, new Bundle());
    }

    public void assignContactFromPhone(String phoneNumber, boolean lazyLookup, Bundle extras) {
        QueryHandler queryHandler;
        this.mContactPhone = phoneNumber;
        this.mExtras = extras;
        if (lazyLookup || (queryHandler = this.mQueryHandler) == null) {
            this.mContactUri = null;
            onContactUriChanged();
            return;
        }
        queryHandler.startQuery(1, null, Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, this.mContactPhone), PHONE_LOOKUP_PROJECTION, null, null, null);
    }

    public void setOverlay(Drawable overlay) {
        this.mOverlay = overlay;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onContactUriChanged() {
        setEnabled(isAssigned());
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View v) {
        Bundle extras = this.mExtras;
        if (extras == null) {
            extras = new Bundle();
        }
        if (this.mContactUri != null) {
            ContactsContract.QuickContact.showQuickContact(getContext(), this, this.mContactUri, this.mExcludeMimes, this.mPrioritizedMimeType);
            return;
        }
        String str = this.mContactEmail;
        if (str == null || this.mQueryHandler == null) {
            String str2 = this.mContactPhone;
            if (str2 != null && this.mQueryHandler != null) {
                extras.putString(EXTRA_URI_CONTENT, str2);
                this.mQueryHandler.startQuery(3, extras, Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, this.mContactPhone), PHONE_LOOKUP_PROJECTION, null, null, null);
                return;
            }
            return;
        }
        extras.putString(EXTRA_URI_CONTENT, str);
        this.mQueryHandler.startQuery(2, extras, Uri.withAppendedPath(ContactsContract.CommonDataKinds.Email.CONTENT_LOOKUP_URI, Uri.encode(this.mContactEmail)), EMAIL_LOOKUP_PROJECTION, null, null, null);
    }

    @Override // android.widget.ImageView, android.view.View
    public CharSequence getAccessibilityClassName() {
        return QuickContactBadge.class.getName();
    }

    public void setExcludeMimes(String[] excludeMimes) {
        this.mExcludeMimes = excludeMimes;
    }

    /* access modifiers changed from: private */
    public class QueryHandler extends AsyncQueryHandler {
        public QueryHandler(ContentResolver cr) {
            super(cr);
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Removed duplicated region for block: B:35:0x0087  */
        /* JADX WARNING: Removed duplicated region for block: B:38:0x0096  */
        /* JADX WARNING: Removed duplicated region for block: B:42:0x00ba  */
        /* JADX WARNING: Removed duplicated region for block: B:49:? A[RETURN, SYNTHETIC] */
        @Override // android.content.AsyncQueryHandler
        public void onQueryComplete(int token, Object cookie, Cursor cursor) {
            Uri lookupUri = null;
            Uri createUri = null;
            boolean trigger = false;
            Bundle extras = cookie != null ? (Bundle) cookie : new Bundle();
            if (token != 0) {
                if (token != 1) {
                    if (token != 2) {
                        if (token == 3) {
                            trigger = true;
                            try {
                                if (extras.getString(QuickContactBadge.EXTRA_URI_CONTENT) != null) {
                                    createUri = Uri.fromParts(PhoneAccount.SCHEME_TEL, extras.getString(QuickContactBadge.EXTRA_URI_CONTENT), null);
                                }
                            } catch (Throwable th) {
                                if (cursor != null) {
                                    cursor.close();
                                }
                                throw th;
                            }
                        }
                        if (cursor != null) {
                            cursor.close();
                        }
                        QuickContactBadge.this.mContactUri = lookupUri;
                        QuickContactBadge.this.onContactUriChanged();
                        if (!trigger && QuickContactBadge.this.mContactUri != null) {
                            Context context = QuickContactBadge.this.getContext();
                            QuickContactBadge quickContactBadge = QuickContactBadge.this;
                            ContactsContract.QuickContact.showQuickContact(context, quickContactBadge, quickContactBadge.mContactUri, QuickContactBadge.this.mExcludeMimes, QuickContactBadge.this.mPrioritizedMimeType);
                            return;
                        } else if (createUri != null) {
                            Intent intent = new Intent("com.android.contacts.action.SHOW_OR_CREATE_CONTACT", createUri);
                            extras.remove(QuickContactBadge.EXTRA_URI_CONTENT);
                            intent.putExtras(extras);
                            try {
                                QuickContactBadge.this.getContext().startActivity(intent);
                                return;
                            } catch (ActivityNotFoundException e) {
                                Slog.w(QuickContactBadge.TAG, "Activity not found for intent ");
                                return;
                            }
                        } else {
                            return;
                        }
                    } else {
                        trigger = true;
                        createUri = Uri.fromParts("mailto", extras.getString(QuickContactBadge.EXTRA_URI_CONTENT), null);
                    }
                }
                if (cursor != null && cursor.moveToFirst()) {
                    if (HwFrameworkFactory.getHwInnerTelephonyManager().isCallerInfofixedIndexValid(extras.getString(QuickContactBadge.EXTRA_URI_CONTENT), cursor)) {
                        lookupUri = ContactsContract.Contacts.getLookupUri(cursor.getLong(0), cursor.getString(1));
                    }
                }
                if (cursor != null) {
                }
                QuickContactBadge.this.mContactUri = lookupUri;
                QuickContactBadge.this.onContactUriChanged();
                if (!trigger) {
                }
                if (createUri != null) {
                }
            }
            if (cursor != null && cursor.moveToFirst()) {
                lookupUri = ContactsContract.Contacts.getLookupUri(cursor.getLong(0), cursor.getString(1));
            }
            if (cursor != null) {
            }
            QuickContactBadge.this.mContactUri = lookupUri;
            QuickContactBadge.this.onContactUriChanged();
            if (!trigger) {
            }
            if (createUri != null) {
            }
        }
    }
}
