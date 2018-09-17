package com.android.internal.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.provider.ContactsContract.Contacts;
import android.provider.DocumentsContract.Document;
import com.android.internal.R;
import java.util.HashMap;

public class MimeIconUtils {
    private static HashMap<String, Integer> sMimeIcons = new HashMap();

    static {
        add("application/vnd.android.package-archive", R.drawable.ic_doc_apk);
        add("application/ogg", R.drawable.ic_doc_audio);
        add("application/x-flac", R.drawable.ic_doc_audio);
        add("application/pgp-keys", R.drawable.ic_doc_certificate);
        add("application/pgp-signature", R.drawable.ic_doc_certificate);
        add("application/x-pkcs12", R.drawable.ic_doc_certificate);
        add("application/x-pkcs7-certreqresp", R.drawable.ic_doc_certificate);
        add("application/x-pkcs7-crl", R.drawable.ic_doc_certificate);
        add("application/x-x509-ca-cert", R.drawable.ic_doc_certificate);
        add("application/x-x509-user-cert", R.drawable.ic_doc_certificate);
        add("application/x-pkcs7-certificates", R.drawable.ic_doc_certificate);
        add("application/x-pkcs7-mime", R.drawable.ic_doc_certificate);
        add("application/x-pkcs7-signature", R.drawable.ic_doc_certificate);
        add("application/rdf+xml", R.drawable.ic_doc_codes);
        add("application/rss+xml", R.drawable.ic_doc_codes);
        add("application/x-object", R.drawable.ic_doc_codes);
        add("application/xhtml+xml", R.drawable.ic_doc_codes);
        add("text/css", R.drawable.ic_doc_codes);
        add("text/html", R.drawable.ic_doc_codes);
        add("text/xml", R.drawable.ic_doc_codes);
        add("text/x-c++hdr", R.drawable.ic_doc_codes);
        add("text/x-c++src", R.drawable.ic_doc_codes);
        add("text/x-chdr", R.drawable.ic_doc_codes);
        add("text/x-csrc", R.drawable.ic_doc_codes);
        add("text/x-dsrc", R.drawable.ic_doc_codes);
        add("text/x-csh", R.drawable.ic_doc_codes);
        add("text/x-haskell", R.drawable.ic_doc_codes);
        add("text/x-java", R.drawable.ic_doc_codes);
        add("text/x-literate-haskell", R.drawable.ic_doc_codes);
        add("text/x-pascal", R.drawable.ic_doc_codes);
        add("text/x-tcl", R.drawable.ic_doc_codes);
        add("text/x-tex", R.drawable.ic_doc_codes);
        add("application/x-latex", R.drawable.ic_doc_codes);
        add("application/x-texinfo", R.drawable.ic_doc_codes);
        add("application/atom+xml", R.drawable.ic_doc_codes);
        add("application/ecmascript", R.drawable.ic_doc_codes);
        add("application/json", R.drawable.ic_doc_codes);
        add("application/javascript", R.drawable.ic_doc_codes);
        add("application/xml", R.drawable.ic_doc_codes);
        add("text/javascript", R.drawable.ic_doc_codes);
        add("application/x-javascript", R.drawable.ic_doc_codes);
        add("application/mac-binhex40", R.drawable.ic_doc_compressed);
        add("application/rar", R.drawable.ic_doc_compressed);
        add("application/zip", R.drawable.ic_doc_compressed);
        add("application/x-apple-diskimage", R.drawable.ic_doc_compressed);
        add("application/x-debian-package", R.drawable.ic_doc_compressed);
        add("application/x-gtar", R.drawable.ic_doc_compressed);
        add("application/x-iso9660-image", R.drawable.ic_doc_compressed);
        add("application/x-lha", R.drawable.ic_doc_compressed);
        add("application/x-lzh", R.drawable.ic_doc_compressed);
        add("application/x-lzx", R.drawable.ic_doc_compressed);
        add("application/x-stuffit", R.drawable.ic_doc_compressed);
        add("application/x-tar", R.drawable.ic_doc_compressed);
        add("application/x-webarchive", R.drawable.ic_doc_compressed);
        add("application/x-webarchive-xml", R.drawable.ic_doc_compressed);
        add("application/gzip", R.drawable.ic_doc_compressed);
        add("application/x-7z-compressed", R.drawable.ic_doc_compressed);
        add("application/x-deb", R.drawable.ic_doc_compressed);
        add("application/x-rar-compressed", R.drawable.ic_doc_compressed);
        add(Contacts.CONTENT_VCARD_TYPE, R.drawable.ic_doc_contact);
        add("text/vcard", R.drawable.ic_doc_contact);
        add("text/calendar", R.drawable.ic_doc_event);
        add("text/x-vcalendar", R.drawable.ic_doc_event);
        add("application/x-font", R.drawable.ic_doc_font);
        add("application/font-woff", R.drawable.ic_doc_font);
        add("application/x-font-woff", R.drawable.ic_doc_font);
        add("application/x-font-ttf", R.drawable.ic_doc_font);
        add("application/vnd.oasis.opendocument.graphics", R.drawable.ic_doc_image);
        add("application/vnd.oasis.opendocument.graphics-template", R.drawable.ic_doc_image);
        add("application/vnd.oasis.opendocument.image", R.drawable.ic_doc_image);
        add("application/vnd.stardivision.draw", R.drawable.ic_doc_image);
        add("application/vnd.sun.xml.draw", R.drawable.ic_doc_image);
        add("application/vnd.sun.xml.draw.template", R.drawable.ic_doc_image);
        add("application/pdf", R.drawable.ic_doc_pdf);
        add("application/vnd.stardivision.impress", R.drawable.ic_doc_presentation);
        add("application/vnd.sun.xml.impress", R.drawable.ic_doc_presentation);
        add("application/vnd.sun.xml.impress.template", R.drawable.ic_doc_presentation);
        add("application/x-kpresenter", R.drawable.ic_doc_presentation);
        add("application/vnd.oasis.opendocument.presentation", R.drawable.ic_doc_presentation);
        add("application/vnd.oasis.opendocument.spreadsheet", R.drawable.ic_doc_spreadsheet);
        add("application/vnd.oasis.opendocument.spreadsheet-template", R.drawable.ic_doc_spreadsheet);
        add("application/vnd.stardivision.calc", R.drawable.ic_doc_spreadsheet);
        add("application/vnd.sun.xml.calc", R.drawable.ic_doc_spreadsheet);
        add("application/vnd.sun.xml.calc.template", R.drawable.ic_doc_spreadsheet);
        add("application/x-kspread", R.drawable.ic_doc_spreadsheet);
        add("application/vnd.oasis.opendocument.text", R.drawable.ic_doc_document);
        add("application/vnd.oasis.opendocument.text-master", R.drawable.ic_doc_document);
        add("application/vnd.oasis.opendocument.text-template", R.drawable.ic_doc_document);
        add("application/vnd.oasis.opendocument.text-web", R.drawable.ic_doc_document);
        add("application/vnd.stardivision.writer", R.drawable.ic_doc_document);
        add("application/vnd.stardivision.writer-global", R.drawable.ic_doc_document);
        add("application/vnd.sun.xml.writer", R.drawable.ic_doc_document);
        add("application/vnd.sun.xml.writer.global", R.drawable.ic_doc_document);
        add("application/vnd.sun.xml.writer.template", R.drawable.ic_doc_document);
        add("application/x-abiword", R.drawable.ic_doc_document);
        add("application/x-kword", R.drawable.ic_doc_document);
        add("application/x-quicktimeplayer", R.drawable.ic_doc_video);
        add("application/x-shockwave-flash", R.drawable.ic_doc_video);
        add("application/msword", R.drawable.ic_doc_word);
        add("application/vnd.openxmlformats-officedocument.wordprocessingml.document", R.drawable.ic_doc_word);
        add("application/vnd.openxmlformats-officedocument.wordprocessingml.template", R.drawable.ic_doc_word);
        add("application/vnd.ms-excel", R.drawable.ic_doc_excel);
        add("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", R.drawable.ic_doc_excel);
        add("application/vnd.openxmlformats-officedocument.spreadsheetml.template", R.drawable.ic_doc_excel);
        add("application/vnd.ms-powerpoint", R.drawable.ic_doc_powerpoint);
        add("application/vnd.openxmlformats-officedocument.presentationml.presentation", R.drawable.ic_doc_powerpoint);
        add("application/vnd.openxmlformats-officedocument.presentationml.template", R.drawable.ic_doc_powerpoint);
        add("application/vnd.openxmlformats-officedocument.presentationml.slideshow", R.drawable.ic_doc_powerpoint);
    }

    private static void add(String mimeType, int resId) {
        if (sMimeIcons.put(mimeType, Integer.valueOf(resId)) != null) {
            throw new RuntimeException(mimeType + " already registered!");
        }
    }

    public static Drawable loadMimeIcon(Context context, String mimeType) {
        if (Document.MIME_TYPE_DIR.equals(mimeType)) {
            return context.getDrawable(R.drawable.ic_doc_folder);
        }
        Integer resId = (Integer) sMimeIcons.get(mimeType);
        if (resId != null) {
            return context.getDrawable(resId.intValue());
        }
        if (mimeType == null) {
            return null;
        }
        String typeOnly = mimeType.split("/")[0];
        if ("audio".equals(typeOnly)) {
            return context.getDrawable(R.drawable.ic_doc_audio);
        }
        if ("image".equals(typeOnly)) {
            return context.getDrawable(R.drawable.ic_doc_image);
        }
        if ("text".equals(typeOnly)) {
            return context.getDrawable(R.drawable.ic_doc_text);
        }
        if ("video".equals(typeOnly)) {
            return context.getDrawable(R.drawable.ic_doc_video);
        }
        return context.getDrawable(R.drawable.ic_doc_generic);
    }
}
