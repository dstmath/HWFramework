package android.content;

import android.graphics.Bitmap;
import android.net.ProxyInfo;
import android.net.Uri;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.StrictMode;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.URLSpan;
import android.util.Log;
import com.android.internal.util.ArrayUtils;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ClipData implements Parcelable {
    public static final Creator<ClipData> CREATOR = new Creator<ClipData>() {
        public ClipData createFromParcel(Parcel source) {
            return new ClipData(source);
        }

        public ClipData[] newArray(int size) {
            return new ClipData[size];
        }
    };
    static final String[] MIMETYPES_TEXT_HTML = new String[]{ClipDescription.MIMETYPE_TEXT_HTML};
    static final String[] MIMETYPES_TEXT_INTENT = new String[]{ClipDescription.MIMETYPE_TEXT_INTENT};
    static final String[] MIMETYPES_TEXT_PLAIN = new String[]{ClipDescription.MIMETYPE_TEXT_PLAIN};
    static final String[] MIMETYPES_TEXT_URILIST = new String[]{ClipDescription.MIMETYPE_TEXT_URILIST};
    final ClipDescription mClipDescription;
    final Bitmap mIcon;
    final ArrayList<Item> mItems;

    public static class Item {
        final String mHtmlText;
        final Intent mIntent;
        final CharSequence mText;
        Uri mUri;

        public Item(Item other) {
            this.mText = other.mText;
            this.mHtmlText = other.mHtmlText;
            this.mIntent = other.mIntent;
            this.mUri = other.mUri;
        }

        public Item(CharSequence text) {
            this.mText = text;
            this.mHtmlText = null;
            this.mIntent = null;
            this.mUri = null;
        }

        public Item(CharSequence text, String htmlText) {
            this.mText = text;
            this.mHtmlText = htmlText;
            this.mIntent = null;
            this.mUri = null;
        }

        public Item(Intent intent) {
            this.mText = null;
            this.mHtmlText = null;
            this.mIntent = intent;
            this.mUri = null;
        }

        public Item(Uri uri) {
            this.mText = null;
            this.mHtmlText = null;
            this.mIntent = null;
            this.mUri = uri;
        }

        public Item(CharSequence text, Intent intent, Uri uri) {
            this.mText = text;
            this.mHtmlText = null;
            this.mIntent = intent;
            this.mUri = uri;
        }

        public Item(CharSequence text, String htmlText, Intent intent, Uri uri) {
            if (htmlText == null || text != null) {
                this.mText = text;
                this.mHtmlText = htmlText;
                this.mIntent = intent;
                this.mUri = uri;
                return;
            }
            throw new IllegalArgumentException("Plain text must be supplied if HTML text is supplied");
        }

        public CharSequence getText() {
            return this.mText;
        }

        public String getHtmlText() {
            return this.mHtmlText;
        }

        public Intent getIntent() {
            return this.mIntent;
        }

        public Uri getUri() {
            return this.mUri;
        }

        public CharSequence coerceToText(Context context) {
            Uri uri;
            CharSequence text = getText();
            if (text != null) {
                return text;
            }
            uri = getUri();
            if (uri != null) {
                FileInputStream fileInputStream = null;
                CharSequence stringBuilder;
                try {
                    fileInputStream = context.getContentResolver().openTypedAssetFileDescriptor(uri, "text/*", null).createInputStream();
                    InputStreamReader reader = new InputStreamReader(fileInputStream, "UTF-8");
                    StringBuilder builder = new StringBuilder(128);
                    char[] buffer = new char[8192];
                    while (true) {
                        int len = reader.read(buffer);
                        if (len <= 0) {
                            break;
                        }
                        builder.append(buffer, 0, len);
                    }
                    stringBuilder = builder.toString();
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e) {
                        }
                    }
                    return stringBuilder;
                } catch (SecurityException e2) {
                    Log.w("ClipData", "Failure opening stream", e2);
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e3) {
                        }
                    }
                } catch (FileNotFoundException e4) {
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e5) {
                        }
                    }
                } catch (IOException e6) {
                    Log.w("ClipData", "Failure loading text", e6);
                    stringBuilder = e6.toString();
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e7) {
                        }
                    }
                    return stringBuilder;
                } catch (Throwable th) {
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e8) {
                        }
                    }
                }
            } else {
                Intent intent = getIntent();
                if (intent != null) {
                    return intent.toUri(1);
                }
                return ProxyInfo.LOCAL_EXCL_LIST;
            }
            String scheme = uri.getScheme();
            if ("content".equals(scheme) || ContentResolver.SCHEME_ANDROID_RESOURCE.equals(scheme) || ContentResolver.SCHEME_FILE.equals(scheme)) {
                return ProxyInfo.LOCAL_EXCL_LIST;
            }
            return uri.toString();
        }

        public CharSequence coerceToStyledText(Context context) {
            CharSequence text = getText();
            if (text instanceof Spanned) {
                return text;
            }
            String htmlText = getHtmlText();
            if (htmlText != null) {
                try {
                    CharSequence newText = Html.fromHtml(htmlText);
                    if (newText != null) {
                        return newText;
                    }
                } catch (RuntimeException e) {
                }
            }
            if (text != null) {
                return text;
            }
            return coerceToHtmlOrStyledText(context, true);
        }

        public String coerceToHtmlText(Context context) {
            String str = null;
            String htmlText = getHtmlText();
            if (htmlText != null) {
                return htmlText;
            }
            CharSequence text = getText();
            if (text == null) {
                text = coerceToHtmlOrStyledText(context, false);
                if (text != null) {
                    str = text.toString();
                }
                return str;
            } else if (text instanceof Spanned) {
                return Html.toHtml((Spanned) text);
            } else {
                return Html.escapeHtml(text);
            }
        }

        private CharSequence coerceToHtmlOrStyledText(Context context, boolean styled) {
            if (this.mUri != null) {
                String[] types = null;
                try {
                    types = context.getContentResolver().getStreamTypes(this.mUri, "text/*");
                } catch (SecurityException e) {
                }
                boolean hasHtml = false;
                boolean hasText = false;
                if (types != null) {
                    for (String type : types) {
                        if (ClipDescription.MIMETYPE_TEXT_HTML.equals(type)) {
                            hasHtml = true;
                        } else if (type.startsWith("text/")) {
                            hasText = true;
                        }
                    }
                }
                if (hasHtml || hasText) {
                    FileInputStream fileInputStream = null;
                    CharSequence str;
                    try {
                        fileInputStream = context.getContentResolver().openTypedAssetFileDescriptor(this.mUri, hasHtml ? ClipDescription.MIMETYPE_TEXT_HTML : ClipDescription.MIMETYPE_TEXT_PLAIN, null).createInputStream();
                        InputStreamReader reader = new InputStreamReader(fileInputStream, "UTF-8");
                        StringBuilder builder = new StringBuilder(128);
                        char[] buffer = new char[8192];
                        while (true) {
                            int len = reader.read(buffer);
                            if (len <= 0) {
                                break;
                            }
                            builder.append(buffer, 0, len);
                        }
                        String text = builder.toString();
                        if (hasHtml) {
                            if (styled) {
                                try {
                                    CharSequence newText = Html.fromHtml(text);
                                    if (newText == null) {
                                        Object newText2 = text;
                                    }
                                    if (fileInputStream != null) {
                                        try {
                                            fileInputStream.close();
                                        } catch (IOException e2) {
                                        }
                                    }
                                    return newText2;
                                } catch (RuntimeException e3) {
                                    if (fileInputStream != null) {
                                        try {
                                            fileInputStream.close();
                                        } catch (IOException e4) {
                                        }
                                    }
                                    return text;
                                }
                            }
                            str = text.toString();
                            if (fileInputStream != null) {
                                try {
                                    fileInputStream.close();
                                } catch (IOException e5) {
                                }
                            }
                            return str;
                        } else if (styled) {
                            if (fileInputStream != null) {
                                try {
                                    fileInputStream.close();
                                } catch (IOException e6) {
                                }
                            }
                            return text;
                        } else {
                            str = Html.escapeHtml(text);
                            if (fileInputStream != null) {
                                try {
                                    fileInputStream.close();
                                } catch (IOException e7) {
                                }
                            }
                            return str;
                        }
                    } catch (SecurityException e8) {
                        Log.w("ClipData", "Failure opening stream", e8);
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (IOException e9) {
                            }
                        }
                    } catch (FileNotFoundException e10) {
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (IOException e11) {
                            }
                        }
                    } catch (IOException e12) {
                        Log.w("ClipData", "Failure loading text", e12);
                        str = Html.escapeHtml(e12.toString());
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (IOException e13) {
                            }
                        }
                        return str;
                    } catch (Throwable th) {
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (IOException e14) {
                            }
                        }
                    }
                }
                String scheme = this.mUri.getScheme();
                if ("content".equals(scheme) || ContentResolver.SCHEME_ANDROID_RESOURCE.equals(scheme) || ContentResolver.SCHEME_FILE.equals(scheme)) {
                    return ProxyInfo.LOCAL_EXCL_LIST;
                }
                if (styled) {
                    return uriToStyledText(this.mUri.toString());
                }
                return uriToHtml(this.mUri.toString());
            } else if (this.mIntent == null) {
                return ProxyInfo.LOCAL_EXCL_LIST;
            } else {
                if (styled) {
                    return uriToStyledText(this.mIntent.toUri(1));
                }
                return uriToHtml(this.mIntent.toUri(1));
            }
        }

        private String uriToHtml(String uri) {
            StringBuilder builder = new StringBuilder(256);
            builder.append("<a href=\"");
            builder.append(Html.escapeHtml(uri));
            builder.append("\">");
            builder.append(Html.escapeHtml(uri));
            builder.append("</a>");
            return builder.toString();
        }

        private CharSequence uriToStyledText(String uri) {
            SpannableStringBuilder builder = new SpannableStringBuilder();
            builder.append(uri);
            builder.setSpan(new URLSpan(uri), 0, builder.length(), 33);
            return builder;
        }

        public String toString() {
            StringBuilder b = new StringBuilder(128);
            b.append("ClipData.Item { ");
            toShortString(b);
            b.append(" }");
            return b.toString();
        }

        public void toShortString(StringBuilder b) {
            if (this.mHtmlText != null) {
                b.append("H:");
                b.append(this.mHtmlText);
            } else if (this.mText != null) {
                b.append("T:");
                b.append(this.mText);
            } else if (this.mUri != null) {
                b.append("U:");
                b.append(this.mUri);
            } else if (this.mIntent != null) {
                b.append("I:");
                this.mIntent.toShortString(b, true, true, true, true);
            } else {
                b.append(WifiEnterpriseConfig.EMPTY_VALUE);
            }
        }

        public void toShortSummaryString(StringBuilder b) {
            if (this.mHtmlText != null) {
                b.append("HTML");
            } else if (this.mText != null) {
                b.append("TEXT");
            } else if (this.mUri != null) {
                b.append("U:");
                b.append(this.mUri);
            } else if (this.mIntent != null) {
                b.append("I:");
                this.mIntent.toShortString(b, true, true, true, true);
            } else {
                b.append(WifiEnterpriseConfig.EMPTY_VALUE);
            }
        }
    }

    public ClipData(CharSequence label, String[] mimeTypes, Item item) {
        this.mClipDescription = new ClipDescription(label, mimeTypes);
        if (item == null) {
            throw new NullPointerException("item is null");
        }
        this.mIcon = null;
        this.mItems = new ArrayList();
        this.mItems.add(item);
    }

    public ClipData(ClipDescription description, Item item) {
        this.mClipDescription = description;
        if (item == null) {
            throw new NullPointerException("item is null");
        }
        this.mIcon = null;
        this.mItems = new ArrayList();
        this.mItems.add(item);
    }

    public ClipData(ClipDescription description, ArrayList<Item> items) {
        this.mClipDescription = description;
        if (items == null) {
            throw new NullPointerException("item is null");
        }
        this.mIcon = null;
        this.mItems = items;
    }

    public ClipData(ClipData other) {
        this.mClipDescription = other.mClipDescription;
        this.mIcon = other.mIcon;
        this.mItems = new ArrayList(other.mItems);
    }

    public static ClipData newPlainText(CharSequence label, CharSequence text) {
        return new ClipData(label, MIMETYPES_TEXT_PLAIN, new Item(text));
    }

    public static ClipData newHtmlText(CharSequence label, CharSequence text, String htmlText) {
        return new ClipData(label, MIMETYPES_TEXT_HTML, new Item(text, htmlText));
    }

    public static ClipData newIntent(CharSequence label, Intent intent) {
        return new ClipData(label, MIMETYPES_TEXT_INTENT, new Item(intent));
    }

    public static ClipData newUri(ContentResolver resolver, CharSequence label, Uri uri) {
        return new ClipData(label, getMimeTypes(resolver, uri), new Item(uri));
    }

    private static String[] getMimeTypes(ContentResolver resolver, Uri uri) {
        String[] mimeTypes = null;
        if ("content".equals(uri.getScheme())) {
            String realType = resolver.getType(uri);
            mimeTypes = resolver.getStreamTypes(uri, "*/*");
            if (realType != null) {
                if (mimeTypes == null) {
                    mimeTypes = new String[]{realType};
                } else if (!ArrayUtils.contains(mimeTypes, realType)) {
                    String[] tmp = new String[(mimeTypes.length + 1)];
                    tmp[0] = realType;
                    System.arraycopy(mimeTypes, 0, tmp, 1, mimeTypes.length);
                    mimeTypes = tmp;
                }
            }
        }
        if (mimeTypes == null) {
            return MIMETYPES_TEXT_URILIST;
        }
        return mimeTypes;
    }

    public static ClipData newRawUri(CharSequence label, Uri uri) {
        return new ClipData(label, MIMETYPES_TEXT_URILIST, new Item(uri));
    }

    public ClipDescription getDescription() {
        return this.mClipDescription;
    }

    public void addItem(Item item) {
        if (item == null) {
            throw new NullPointerException("item is null");
        }
        this.mItems.add(item);
    }

    @Deprecated
    public void addItem(Item item, ContentResolver resolver) {
        addItem(resolver, item);
    }

    public void addItem(ContentResolver resolver, Item item) {
        addItem(item);
        if (item.getHtmlText() != null) {
            this.mClipDescription.addMimeTypes(MIMETYPES_TEXT_HTML);
        } else if (item.getText() != null) {
            this.mClipDescription.addMimeTypes(MIMETYPES_TEXT_PLAIN);
        }
        if (item.getIntent() != null) {
            this.mClipDescription.addMimeTypes(MIMETYPES_TEXT_INTENT);
        }
        if (item.getUri() != null) {
            this.mClipDescription.addMimeTypes(getMimeTypes(resolver, item.getUri()));
        }
    }

    public Bitmap getIcon() {
        return this.mIcon;
    }

    public int getItemCount() {
        return this.mItems.size();
    }

    public Item getItemAt(int index) {
        return (Item) this.mItems.get(index);
    }

    public void setItemAt(int index, Item item) {
        this.mItems.set(index, item);
    }

    public void prepareToLeaveProcess(boolean leavingPackage) {
        prepareToLeaveProcess(leavingPackage, 1);
    }

    public void prepareToLeaveProcess(boolean leavingPackage, int intentFlags) {
        int size = this.mItems.size();
        for (int i = 0; i < size; i++) {
            Item item = (Item) this.mItems.get(i);
            if (item.mIntent != null) {
                item.mIntent.prepareToLeaveProcess(leavingPackage);
            }
            if (item.mUri != null && leavingPackage) {
                if (StrictMode.vmFileUriExposureEnabled()) {
                    item.mUri.checkFileUriExposed("ClipData.Item.getUri()");
                }
                if (StrictMode.vmContentUriWithoutPermissionEnabled()) {
                    item.mUri.checkContentUriWithoutPermission("ClipData.Item.getUri()", intentFlags);
                }
            }
        }
    }

    public void prepareToEnterProcess() {
        int size = this.mItems.size();
        for (int i = 0; i < size; i++) {
            Item item = (Item) this.mItems.get(i);
            if (item.mIntent != null) {
                item.mIntent.prepareToEnterProcess();
            }
        }
    }

    public void fixUris(int contentUserHint) {
        int size = this.mItems.size();
        for (int i = 0; i < size; i++) {
            Item item = (Item) this.mItems.get(i);
            if (item.mIntent != null) {
                item.mIntent.fixUris(contentUserHint);
            }
            if (item.mUri != null) {
                item.mUri = ContentProvider.maybeAddUserId(item.mUri, contentUserHint);
            }
        }
    }

    public void fixUrisLight(int contentUserHint) {
        int size = this.mItems.size();
        for (int i = 0; i < size; i++) {
            Item item = (Item) this.mItems.get(i);
            if (item.mIntent != null) {
                Uri data = item.mIntent.getData();
                if (data != null) {
                    item.mIntent.setData(ContentProvider.maybeAddUserId(data, contentUserHint));
                }
            }
            if (item.mUri != null) {
                item.mUri = ContentProvider.maybeAddUserId(item.mUri, contentUserHint);
            }
        }
    }

    public String toString() {
        StringBuilder b = new StringBuilder(128);
        b.append("ClipData { ");
        toShortString(b);
        b.append(" }");
        return b.toString();
    }

    public void toShortString(StringBuilder b) {
        int first;
        if (this.mClipDescription != null) {
            first = this.mClipDescription.toShortString(b) ^ 1;
        } else {
            first = 1;
        }
        if (this.mIcon != null) {
            if (first == 0) {
                b.append(' ');
            }
            first = 0;
            b.append("I:");
            b.append(this.mIcon.getWidth());
            b.append('x');
            b.append(this.mIcon.getHeight());
        }
        for (int i = 0; i < this.mItems.size(); i++) {
            if (first == 0) {
                b.append(' ');
            }
            first = 0;
            b.append('{');
            ((Item) this.mItems.get(i)).toShortString(b);
            b.append('}');
        }
    }

    public void toShortStringShortItems(StringBuilder b, boolean first) {
        if (this.mItems.size() > 0) {
            if (!first) {
                b.append(' ');
            }
            ((Item) this.mItems.get(0)).toShortString(b);
            if (this.mItems.size() > 1) {
                b.append(" ...");
            }
        }
    }

    public void collectUris(List<Uri> out) {
        for (int i = 0; i < this.mItems.size(); i++) {
            Item item = getItemAt(i);
            if (item.getUri() != null) {
                out.add(item.getUri());
            }
            Intent intent = item.getIntent();
            if (intent != null) {
                if (intent.getData() != null) {
                    out.add(intent.getData());
                }
                if (intent.getClipData() != null) {
                    intent.getClipData().collectUris(out);
                }
            }
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        this.mClipDescription.writeToParcel(dest, flags);
        if (this.mIcon != null) {
            dest.writeInt(1);
            this.mIcon.writeToParcel(dest, flags);
        } else {
            dest.writeInt(0);
        }
        int N = this.mItems.size();
        dest.writeInt(N);
        for (int i = 0; i < N; i++) {
            Item item = (Item) this.mItems.get(i);
            TextUtils.writeToParcel(item.mText, dest, flags);
            dest.writeString(item.mHtmlText);
            if (item.mIntent != null) {
                dest.writeInt(1);
                item.mIntent.writeToParcel(dest, flags);
            } else {
                dest.writeInt(0);
            }
            if (item.mUri != null) {
                dest.writeInt(1);
                item.mUri.writeToParcel(dest, flags);
            } else {
                dest.writeInt(0);
            }
        }
    }

    ClipData(Parcel in) {
        this.mClipDescription = new ClipDescription(in);
        if (in.readInt() != 0) {
            this.mIcon = (Bitmap) Bitmap.CREATOR.createFromParcel(in);
        } else {
            this.mIcon = null;
        }
        this.mItems = new ArrayList();
        int N = in.readInt();
        for (int i = 0; i < N; i++) {
            this.mItems.add(new Item((CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in), in.readString(), in.readInt() != 0 ? (Intent) Intent.CREATOR.createFromParcel(in) : null, in.readInt() != 0 ? (Uri) Uri.CREATOR.createFromParcel(in) : null));
        }
    }
}
