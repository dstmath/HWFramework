package android.content;

import android.annotation.UnsupportedAppUsage;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Build;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.os.StrictMode;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.URLSpan;
import android.util.Log;
import android.util.proto.ProtoOutputStream;
import com.android.internal.transition.EpicenterTranslateClipReveal;
import com.android.internal.util.ArrayUtils;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import libcore.io.IoUtils;

public class ClipData implements Parcelable {
    public static final Parcelable.Creator<ClipData> CREATOR = new Parcelable.Creator<ClipData>() {
        /* class android.content.ClipData.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ClipData createFromParcel(Parcel source) {
            return new ClipData(source);
        }

        @Override // android.os.Parcelable.Creator
        public ClipData[] newArray(int size) {
            return new ClipData[size];
        }
    };
    static final String[] MIMETYPES_TEXT_HTML = {ClipDescription.MIMETYPE_TEXT_HTML};
    static final String[] MIMETYPES_TEXT_INTENT = {ClipDescription.MIMETYPE_TEXT_INTENT};
    static final String[] MIMETYPES_TEXT_PLAIN = {ClipDescription.MIMETYPE_TEXT_PLAIN};
    static final String[] MIMETYPES_TEXT_URILIST = {ClipDescription.MIMETYPE_TEXT_URILIST};
    static final int PARCEL_MAX_SIZE_BYTES = 819200;
    static final int PARCEL_TYPE_PFD = 1;
    static final int PARCEL_TYPE_STRING = 0;
    final ClipDescription mClipDescription;
    final Bitmap mIcon;
    final ArrayList<Item> mItems;

    public static class Item {
        final String mHtmlText;
        final Intent mIntent;
        final CharSequence mText;
        @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
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
            CharSequence text = getText();
            if (text != null) {
                return text;
            }
            Uri uri = getUri();
            if (uri != null) {
                AssetFileDescriptor descr = null;
                try {
                    descr = context.getContentResolver().openTypedAssetFileDescriptor(uri, "text/*", null);
                } catch (SecurityException e) {
                    Log.w("ClipData", "Failure opening stream", e);
                } catch (FileNotFoundException | RuntimeException e2) {
                } catch (Throwable th) {
                    IoUtils.closeQuietly((AutoCloseable) null);
                    IoUtils.closeQuietly((AutoCloseable) null);
                    IoUtils.closeQuietly((AutoCloseable) null);
                    throw th;
                }
                if (descr != null) {
                    try {
                        FileInputStream stream = descr.createInputStream();
                        InputStreamReader reader = new InputStreamReader(stream, "UTF-8");
                        StringBuilder builder = new StringBuilder(128);
                        char[] buffer = new char[8192];
                        while (true) {
                            int len = reader.read(buffer);
                            if (len > 0) {
                                builder.append(buffer, 0, len);
                            } else {
                                String sb = builder.toString();
                                IoUtils.closeQuietly(descr);
                                IoUtils.closeQuietly(stream);
                                IoUtils.closeQuietly(reader);
                                return sb;
                            }
                        }
                    } catch (IOException e3) {
                        Log.w("ClipData", "Failure loading text", e3);
                        String iOException = e3.toString();
                        IoUtils.closeQuietly(descr);
                        IoUtils.closeQuietly((AutoCloseable) null);
                        IoUtils.closeQuietly((AutoCloseable) null);
                        return iOException;
                    }
                } else {
                    IoUtils.closeQuietly(descr);
                    IoUtils.closeQuietly((AutoCloseable) null);
                    IoUtils.closeQuietly((AutoCloseable) null);
                    String scheme = uri.getScheme();
                    if ("content".equals(scheme) || ContentResolver.SCHEME_ANDROID_RESOURCE.equals(scheme) || ContentResolver.SCHEME_FILE.equals(scheme)) {
                        return "";
                    }
                    return uri.toString();
                }
            } else {
                Intent intent = getIntent();
                if (intent != null) {
                    return intent.toUri(1);
                }
                return "";
            }
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
            String htmlText = getHtmlText();
            if (htmlText != null) {
                return htmlText;
            }
            CharSequence text = getText();
            if (text == null) {
                CharSequence text2 = coerceToHtmlOrStyledText(context, false);
                if (text2 != null) {
                    return text2.toString();
                }
                return null;
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
                boolean hasHtml2 = false;
                String str = ClipDescription.MIMETYPE_TEXT_HTML;
                if (types != null) {
                    boolean hasText = false;
                    boolean hasHtml3 = false;
                    for (String type : types) {
                        if (str.equals(type)) {
                            hasHtml3 = true;
                        } else if (type.startsWith("text/")) {
                            hasText = true;
                        }
                    }
                    hasHtml = hasHtml3;
                    hasHtml2 = hasText;
                }
                if (hasHtml || hasHtml2) {
                    FileInputStream stream = null;
                    try {
                        ContentResolver contentResolver = context.getContentResolver();
                        Uri uri = this.mUri;
                        if (!hasHtml) {
                            str = ClipDescription.MIMETYPE_TEXT_PLAIN;
                        }
                        AssetFileDescriptor descr = contentResolver.openTypedAssetFileDescriptor(uri, str, null);
                        if (descr != null) {
                            FileInputStream stream2 = descr.createInputStream();
                            InputStreamReader reader = new InputStreamReader(stream2, "UTF-8");
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
                                        CharSequence charSequence = newText != null ? newText : text;
                                        if (stream2 != null) {
                                            try {
                                                stream2.close();
                                            } catch (IOException e2) {
                                            }
                                        }
                                        return charSequence;
                                    } catch (RuntimeException e3) {
                                        if (stream2 != null) {
                                            try {
                                                stream2.close();
                                            } catch (IOException e4) {
                                            }
                                        }
                                        return text;
                                    }
                                } else {
                                    String str2 = text.toString();
                                    if (stream2 != null) {
                                        try {
                                            stream2.close();
                                        } catch (IOException e5) {
                                        }
                                    }
                                    return str2;
                                }
                            } else if (styled) {
                                if (stream2 != null) {
                                    try {
                                        stream2.close();
                                    } catch (IOException e6) {
                                    }
                                }
                                return text;
                            } else {
                                String escapeHtml = Html.escapeHtml(text);
                                if (stream2 != null) {
                                    try {
                                        stream2.close();
                                    } catch (IOException e7) {
                                    }
                                }
                                return escapeHtml;
                            }
                        } else {
                            Log.w("ClipData", "descr is null");
                            if (0 != 0) {
                                try {
                                    stream.close();
                                } catch (IOException e8) {
                                }
                            }
                        }
                    } catch (SecurityException e9) {
                        Log.w("ClipData", "Failure opening stream", e9);
                        if (0 != 0) {
                            stream.close();
                        }
                    } catch (FileNotFoundException e10) {
                        if (0 != 0) {
                            stream.close();
                        }
                    } catch (IOException e11) {
                        Log.w("ClipData", "Failure loading text", e11);
                        String escapeHtml2 = Html.escapeHtml(e11.toString());
                        if (0 != 0) {
                            try {
                                stream.close();
                            } catch (IOException e12) {
                            }
                        }
                        return escapeHtml2;
                    } catch (Throwable th) {
                        if (0 != 0) {
                            try {
                                stream.close();
                            } catch (IOException e13) {
                            }
                        }
                        throw th;
                    }
                }
                String scheme = this.mUri.getScheme();
                if ("content".equals(scheme) || ContentResolver.SCHEME_ANDROID_RESOURCE.equals(scheme) || ContentResolver.SCHEME_FILE.equals(scheme)) {
                    return "";
                }
                if (styled) {
                    return uriToStyledText(this.mUri.toString());
                }
                return uriToHtml(this.mUri.toString());
            }
            Intent intent = this.mIntent;
            if (intent == null) {
                return "";
            }
            if (styled) {
                return uriToStyledText(intent.toUri(1));
            }
            return uriToHtml(intent.toUri(1));
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
            builder.append((CharSequence) uri);
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

        public void writeToProto(ProtoOutputStream proto, long fieldId) {
            long token = proto.start(fieldId);
            String str = this.mHtmlText;
            if (str != null) {
                proto.write(1138166333441L, str);
            } else {
                CharSequence charSequence = this.mText;
                if (charSequence != null) {
                    proto.write(1138166333442L, charSequence.toString());
                } else {
                    Uri uri = this.mUri;
                    if (uri != null) {
                        proto.write(1138166333443L, uri.toString());
                    } else {
                        Intent intent = this.mIntent;
                        if (intent != null) {
                            intent.writeToProto(proto, 1146756268036L, true, true, true, true);
                        } else {
                            proto.write(1133871366149L, true);
                        }
                    }
                }
            }
            proto.end(token);
        }
    }

    public ClipData(CharSequence label, String[] mimeTypes, Item item) {
        this.mClipDescription = new ClipDescription(label, mimeTypes);
        if (item != null) {
            this.mIcon = null;
            this.mItems = new ArrayList<>();
            this.mItems.add(item);
            return;
        }
        throw new NullPointerException("item is null");
    }

    public ClipData(ClipDescription description, Item item) {
        this.mClipDescription = description;
        if (item != null) {
            this.mIcon = null;
            this.mItems = new ArrayList<>();
            this.mItems.add(item);
            return;
        }
        throw new NullPointerException("item is null");
    }

    public ClipData(ClipDescription description, ArrayList<Item> items) {
        this.mClipDescription = description;
        if (items != null) {
            this.mIcon = null;
            this.mItems = items;
            return;
        }
        throw new NullPointerException("item is null");
    }

    public ClipData(ClipData other) {
        this.mClipDescription = other.mClipDescription;
        this.mIcon = other.mIcon;
        this.mItems = new ArrayList<>(other.mItems);
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
        if (item != null) {
            this.mItems.add(item);
            return;
        }
        throw new NullPointerException("item is null");
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

    @UnsupportedAppUsage
    public Bitmap getIcon() {
        return this.mIcon;
    }

    public int getItemCount() {
        return this.mItems.size();
    }

    public Item getItemAt(int index) {
        return this.mItems.get(index);
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
            Item item = this.mItems.get(i);
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
            Item item = this.mItems.get(i);
            if (item.mIntent != null) {
                item.mIntent.prepareToEnterProcess();
            }
        }
    }

    public void fixUris(int contentUserHint) {
        int size = this.mItems.size();
        for (int i = 0; i < size; i++) {
            Item item = this.mItems.get(i);
            if (item.mIntent != null) {
                item.mIntent.fixUris(contentUserHint);
            }
            if (item.mUri != null) {
                item.mUri = ContentProvider.maybeAddUserId(item.mUri, contentUserHint);
            }
        }
    }

    public void fixUrisLight(int contentUserHint) {
        Uri data;
        int size = this.mItems.size();
        for (int i = 0; i < size; i++) {
            Item item = this.mItems.get(i);
            if (!(item.mIntent == null || (data = item.mIntent.getData()) == null)) {
                item.mIntent.setData(ContentProvider.maybeAddUserId(data, contentUserHint));
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
        ClipDescription clipDescription = this.mClipDescription;
        boolean first = clipDescription != null ? !clipDescription.toShortString(b) : true;
        if (this.mIcon != null) {
            if (!first) {
                b.append(' ');
            }
            first = false;
            b.append("I:");
            b.append(this.mIcon.getWidth());
            b.append(EpicenterTranslateClipReveal.StateProperty.TARGET_X);
            b.append(this.mIcon.getHeight());
        }
        for (int i = 0; i < this.mItems.size(); i++) {
            if (!first) {
                b.append(' ');
            }
            first = false;
            b.append('{');
            this.mItems.get(i).toShortString(b);
            b.append('}');
        }
    }

    public void toShortStringShortItems(StringBuilder b, boolean first) {
        if (this.mItems.size() > 0) {
            if (!first) {
                b.append(' ');
            }
            this.mItems.get(0).toShortString(b);
            if (this.mItems.size() > 1) {
                b.append(" ...");
            }
        }
    }

    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        long token = proto.start(fieldId);
        ClipDescription clipDescription = this.mClipDescription;
        if (clipDescription != null) {
            clipDescription.writeToProto(proto, 1146756268033L);
        }
        if (this.mIcon != null) {
            long iToken = proto.start(1146756268034L);
            proto.write(1120986464257L, this.mIcon.getWidth());
            proto.write(1120986464258L, this.mIcon.getHeight());
            proto.end(iToken);
        }
        for (int i = 0; i < this.mItems.size(); i++) {
            this.mItems.get(i).writeToProto(proto, 2246267895811L);
        }
        proto.end(token);
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

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
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
            Item item = this.mItems.get(i);
            TextUtils.writeToParcel(item.mText, dest, flags);
            writeHtmlTextToParcel(item.mHtmlText, dest, flags);
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
            this.mIcon = Bitmap.CREATOR.createFromParcel(in);
        } else {
            this.mIcon = null;
        }
        this.mItems = new ArrayList<>();
        int N = in.readInt();
        for (int i = 0; i < N; i++) {
            this.mItems.add(new Item(TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in), readHtmlTextFromParcel(in), in.readInt() != 0 ? Intent.CREATOR.createFromParcel(in) : null, in.readInt() != 0 ? Uri.CREATOR.createFromParcel(in) : null));
        }
    }

    private static void writeHtmlTextToParcel(String text, Parcel dest, int flags) {
        byte[] textData = text != null ? text.getBytes() : new byte[0];
        if (textData.length <= 409600 || Build.VERSION.SDK_INT > 29) {
            dest.writeInt(0);
            dest.writeString(text);
            return;
        }
        try {
            ParcelFileDescriptor pfd = ParcelFileDescriptor.fromData(textData, null);
            dest.writeInt(1);
            dest.writeParcelable(pfd, flags);
        } catch (IOException e) {
            throw new IllegalStateException("Error creating the shared memory area: " + e.toString());
        }
    }

    private static String readHtmlTextFromParcel(Parcel in) {
        if (in.readInt() == 0) {
            return in.readString();
        }
        ParcelFileDescriptor pfd = (ParcelFileDescriptor) in.readParcelable(ParcelFileDescriptor.class.getClassLoader());
        if (pfd != null) {
            FileInputStream fis = new ParcelFileDescriptor.AutoCloseInputStream(pfd);
            InputStreamReader reader = new InputStreamReader(fis);
            StringBuilder builder = new StringBuilder();
            char[] buffer = new char[4096];
            while (true) {
                try {
                    int numRead = reader.read(buffer);
                    if (numRead != -1) {
                        builder.append(buffer, 0, numRead);
                    } else {
                        String sb = builder.toString();
                        IoUtils.closeQuietly(fis);
                        return sb;
                    }
                } catch (IOException e) {
                    throw new IllegalStateException("Error reading data from ParcelFileDescriptor: " + e.toString());
                } catch (Throwable th) {
                    IoUtils.closeQuietly(fis);
                    throw th;
                }
            }
        } else {
            throw new IllegalStateException("Error reading ParcelFileDescriptor from Parcel");
        }
    }
}
