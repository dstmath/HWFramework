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
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.ContactsContract.QuickContact;
import android.telecom.PhoneAccount;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import com.android.internal.R;
import com.android.internal.logging.MetricsProto.MetricsEvent;

public class QuickContactBadge extends ImageView implements OnClickListener {
    static final int EMAIL_ID_COLUMN_INDEX = 0;
    static final String[] EMAIL_LOOKUP_PROJECTION = null;
    static final int EMAIL_LOOKUP_STRING_COLUMN_INDEX = 1;
    private static final String EXTRA_URI_CONTENT = "uri_content";
    static final int PHONE_ID_COLUMN_INDEX = 0;
    static final String[] PHONE_LOOKUP_PROJECTION = null;
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
    private Drawable mOverlay;
    private String mPrioritizedMimeType;
    private QueryHandler mQueryHandler;

    private class QueryHandler extends AsyncQueryHandler {
        public QueryHandler(ContentResolver cr) {
            super(cr);
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            Uri lookupUri = null;
            Uri uri = null;
            boolean trigger = false;
            Bundle extras = cookie != null ? (Bundle) cookie : new Bundle();
            switch (token) {
                case QuickContactBadge.TOKEN_EMAIL_LOOKUP /*0*/:
                    break;
                case QuickContactBadge.TOKEN_PHONE_LOOKUP /*1*/:
                    break;
                case QuickContactBadge.TOKEN_EMAIL_LOOKUP_AND_TRIGGER /*2*/:
                    trigger = true;
                    uri = Uri.fromParts("mailto", extras.getString(QuickContactBadge.EXTRA_URI_CONTENT), null);
                    break;
                case QuickContactBadge.TOKEN_PHONE_LOOKUP_AND_TRIGGER /*3*/:
                    trigger = true;
                    try {
                        if (extras.getString(QuickContactBadge.EXTRA_URI_CONTENT) != null) {
                            uri = Uri.fromParts(PhoneAccount.SCHEME_TEL, extras.getString(QuickContactBadge.EXTRA_URI_CONTENT), null);
                            break;
                        }
                    } catch (Throwable th) {
                        if (cursor != null) {
                            cursor.close();
                        }
                    }
                    break;
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.widget.QuickContactBadge.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.widget.QuickContactBadge.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.QuickContactBadge.<clinit>():void");
    }

    public QuickContactBadge(Context context) {
        this(context, null);
    }

    public QuickContactBadge(Context context, AttributeSet attrs) {
        this(context, attrs, TOKEN_EMAIL_LOOKUP);
    }

    public QuickContactBadge(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, TOKEN_EMAIL_LOOKUP);
    }

    public QuickContactBadge(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mExtras = null;
        this.mExcludeMimes = null;
        TypedArray styledAttributes = this.mContext.obtainStyledAttributes(R.styleable.Theme);
        this.mOverlay = styledAttributes.getDrawable(MetricsEvent.ACTION_BUGREPORT_NOTIFICATION_ACTION_SCREENSHOT);
        styledAttributes.recycle();
        setOnClickListener(this);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
            this.mQueryHandler = new QueryHandler(this.mContext.getContentResolver());
        }
    }

    protected void drawableStateChanged() {
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

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isEnabled() && this.mOverlay != null && this.mOverlay.getIntrinsicWidth() != 0 && this.mOverlay.getIntrinsicHeight() != 0) {
            this.mOverlay.setBounds(TOKEN_EMAIL_LOOKUP, TOKEN_EMAIL_LOOKUP, getWidth(), getHeight());
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
        this.mContactEmail = emailAddress;
        this.mExtras = extras;
        if (lazyLookup || this.mQueryHandler == null) {
            this.mContactUri = null;
            onContactUriChanged();
            return;
        }
        this.mQueryHandler.startQuery(TOKEN_EMAIL_LOOKUP, null, Uri.withAppendedPath(Email.CONTENT_LOOKUP_URI, Uri.encode(this.mContactEmail)), EMAIL_LOOKUP_PROJECTION, null, null, null);
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
        this.mQueryHandler.startQuery(TOKEN_PHONE_LOOKUP, null, Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, this.mContactPhone), PHONE_LOOKUP_PROJECTION, null, null, null);
    }

    public void setOverlay(Drawable overlay) {
        this.mOverlay = overlay;
    }

    private void onContactUriChanged() {
        setEnabled(isAssigned());
    }

    public void onClick(View v) {
        Bundle extras = this.mExtras == null ? new Bundle() : this.mExtras;
        if (this.mContactUri != null) {
            QuickContact.showQuickContact(getContext(), this, this.mContactUri, this.mExcludeMimes, this.mPrioritizedMimeType);
        } else if (this.mContactEmail != null && this.mQueryHandler != null) {
            extras.putString(EXTRA_URI_CONTENT, this.mContactEmail);
            this.mQueryHandler.startQuery(TOKEN_EMAIL_LOOKUP_AND_TRIGGER, extras, Uri.withAppendedPath(Email.CONTENT_LOOKUP_URI, Uri.encode(this.mContactEmail)), EMAIL_LOOKUP_PROJECTION, null, null, null);
        } else if (this.mContactPhone != null && this.mQueryHandler != null) {
            extras.putString(EXTRA_URI_CONTENT, this.mContactPhone);
            this.mQueryHandler.startQuery(TOKEN_PHONE_LOOKUP_AND_TRIGGER, extras, Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, this.mContactPhone), PHONE_LOOKUP_PROJECTION, null, null, null);
        }
    }

    public CharSequence getAccessibilityClassName() {
        return QuickContactBadge.class.getName();
    }

    public void setExcludeMimes(String[] excludeMimes) {
        this.mExcludeMimes = excludeMimes;
    }
}
