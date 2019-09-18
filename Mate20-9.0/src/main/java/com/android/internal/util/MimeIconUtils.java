package com.android.internal.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import com.android.internal.os.PowerProfile;
import java.util.HashMap;

public class MimeIconUtils {
    private static HashMap<String, Integer> sMimeIcons = new HashMap<>();

    static {
        add("application/vnd.android.package-archive", 17302357);
        add("application/ogg", 17302358);
        add("application/x-flac", 17302358);
        add("application/pgp-keys", 17302359);
        add("application/pgp-signature", 17302359);
        add("application/x-pkcs12", 17302359);
        add("application/x-pkcs7-certreqresp", 17302359);
        add("application/x-pkcs7-crl", 17302359);
        add("application/x-x509-ca-cert", 17302359);
        add("application/x-x509-user-cert", 17302359);
        add("application/x-pkcs7-certificates", 17302359);
        add("application/x-pkcs7-mime", 17302359);
        add("application/x-pkcs7-signature", 17302359);
        add("application/rdf+xml", 17302360);
        add("application/rss+xml", 17302360);
        add("application/x-object", 17302360);
        add("application/xhtml+xml", 17302360);
        add("text/css", 17302360);
        add("text/html", 17302360);
        add("text/xml", 17302360);
        add("text/x-c++hdr", 17302360);
        add("text/x-c++src", 17302360);
        add("text/x-chdr", 17302360);
        add("text/x-csrc", 17302360);
        add("text/x-dsrc", 17302360);
        add("text/x-csh", 17302360);
        add("text/x-haskell", 17302360);
        add("text/x-java", 17302360);
        add("text/x-literate-haskell", 17302360);
        add("text/x-pascal", 17302360);
        add("text/x-tcl", 17302360);
        add("text/x-tex", 17302360);
        add("application/x-latex", 17302360);
        add("application/x-texinfo", 17302360);
        add("application/atom+xml", 17302360);
        add("application/ecmascript", 17302360);
        add("application/json", 17302360);
        add("application/javascript", 17302360);
        add("application/xml", 17302360);
        add("text/javascript", 17302360);
        add("application/x-javascript", 17302360);
        add("application/mac-binhex40", 17302361);
        add("application/rar", 17302361);
        add("application/zip", 17302361);
        add("application/x-apple-diskimage", 17302361);
        add("application/x-debian-package", 17302361);
        add("application/x-gtar", 17302361);
        add("application/x-iso9660-image", 17302361);
        add("application/x-lha", 17302361);
        add("application/x-lzh", 17302361);
        add("application/x-lzx", 17302361);
        add("application/x-stuffit", 17302361);
        add("application/x-tar", 17302361);
        add("application/x-webarchive", 17302361);
        add("application/x-webarchive-xml", 17302361);
        add("application/gzip", 17302361);
        add("application/x-7z-compressed", 17302361);
        add("application/x-deb", 17302361);
        add("application/x-rar-compressed", 17302361);
        add("text/x-vcard", 17302362);
        add("text/vcard", 17302362);
        add("text/calendar", 17302364);
        add("text/x-vcalendar", 17302364);
        add("application/x-font", 17302367);
        add("application/font-woff", 17302367);
        add("application/x-font-woff", 17302367);
        add("application/x-font-ttf", 17302367);
        add("application/vnd.oasis.opendocument.graphics", 17302369);
        add("application/vnd.oasis.opendocument.graphics-template", 17302369);
        add("application/vnd.oasis.opendocument.image", 17302369);
        add("application/vnd.stardivision.draw", 17302369);
        add("application/vnd.sun.xml.draw", 17302369);
        add("application/vnd.sun.xml.draw.template", 17302369);
        add("application/pdf", 17302370);
        add("application/vnd.stardivision.impress", 17302372);
        add("application/vnd.sun.xml.impress", 17302372);
        add("application/vnd.sun.xml.impress.template", 17302372);
        add("application/x-kpresenter", 17302372);
        add("application/vnd.oasis.opendocument.presentation", 17302372);
        add("application/vnd.oasis.opendocument.spreadsheet", 17302373);
        add("application/vnd.oasis.opendocument.spreadsheet-template", 17302373);
        add("application/vnd.stardivision.calc", 17302373);
        add("application/vnd.sun.xml.calc", 17302373);
        add("application/vnd.sun.xml.calc.template", 17302373);
        add("application/x-kspread", 17302373);
        add("application/vnd.oasis.opendocument.text", 17302363);
        add("application/vnd.oasis.opendocument.text-master", 17302363);
        add("application/vnd.oasis.opendocument.text-template", 17302363);
        add("application/vnd.oasis.opendocument.text-web", 17302363);
        add("application/vnd.stardivision.writer", 17302363);
        add("application/vnd.stardivision.writer-global", 17302363);
        add("application/vnd.sun.xml.writer", 17302363);
        add("application/vnd.sun.xml.writer.global", 17302363);
        add("application/vnd.sun.xml.writer.template", 17302363);
        add("application/x-abiword", 17302363);
        add("application/x-kword", 17302363);
        add("application/x-quicktimeplayer", 17302375);
        add("application/x-shockwave-flash", 17302375);
        add("application/msword", 17302376);
        add("application/vnd.openxmlformats-officedocument.wordprocessingml.document", 17302376);
        add("application/vnd.openxmlformats-officedocument.wordprocessingml.template", 17302376);
        add("application/vnd.ms-excel", 17302365);
        add("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", 17302365);
        add("application/vnd.openxmlformats-officedocument.spreadsheetml.template", 17302365);
        add("application/vnd.ms-powerpoint", 17302371);
        add("application/vnd.openxmlformats-officedocument.presentationml.presentation", 17302371);
        add("application/vnd.openxmlformats-officedocument.presentationml.template", 17302371);
        add("application/vnd.openxmlformats-officedocument.presentationml.slideshow", 17302371);
    }

    private static void add(String mimeType, int resId) {
        if (sMimeIcons.put(mimeType, Integer.valueOf(resId)) != null) {
            throw new RuntimeException(mimeType + " already registered!");
        }
    }

    public static Drawable loadMimeIcon(Context context, String mimeType) {
        if ("vnd.android.document/directory".equals(mimeType)) {
            return context.getDrawable(17302366);
        }
        Integer resId = sMimeIcons.get(mimeType);
        if (resId != null) {
            return context.getDrawable(resId.intValue());
        }
        if (mimeType == null) {
            return null;
        }
        String typeOnly = mimeType.split("/")[0];
        if (PowerProfile.POWER_AUDIO.equals(typeOnly)) {
            return context.getDrawable(17302358);
        }
        if ("image".equals(typeOnly)) {
            return context.getDrawable(17302369);
        }
        if ("text".equals(typeOnly)) {
            return context.getDrawable(17302374);
        }
        if (PowerProfile.POWER_VIDEO.equals(typeOnly)) {
            return context.getDrawable(17302375);
        }
        return context.getDrawable(17302368);
    }
}
