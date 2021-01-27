package com.android.internal.util;

import android.content.ClipDescription;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.graphics.drawable.Icon;
import android.provider.ContactsContract;
import android.provider.DocumentsContract;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.ArrayMap;
import com.android.internal.R;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.widget.MessagingMessage;
import java.util.Locale;
import java.util.Objects;
import libcore.net.MimeUtils;

public class MimeIconUtils {
    @GuardedBy({"sCache"})
    private static final ArrayMap<String, ContentResolver.MimeTypeInfo> sCache = new ArrayMap<>();

    private static ContentResolver.MimeTypeInfo buildTypeInfo(String mimeType, int iconId, int labelId, int extLabelId) {
        CharSequence label;
        Resources res = Resources.getSystem();
        String ext = MimeUtils.guessExtensionFromMimeType(mimeType);
        if (TextUtils.isEmpty(ext) || extLabelId == -1) {
            label = res.getString(labelId);
        } else {
            label = res.getString(extLabelId, ext.toUpperCase(Locale.US));
        }
        return new ContentResolver.MimeTypeInfo(Icon.createWithResource(res, iconId), label, label);
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private static ContentResolver.MimeTypeInfo buildTypeInfo(String mimeType) {
        char c;
        switch (mimeType.hashCode()) {
            case -2135180893:
                if (mimeType.equals("application/vnd.stardivision.calc")) {
                    c = 'S';
                    break;
                }
                c = 65535;
                break;
            case -2135135086:
                if (mimeType.equals("application/vnd.stardivision.draw")) {
                    c = 'F';
                    break;
                }
                c = 65535;
                break;
            case -2035614749:
                if (mimeType.equals("application/vnd.google-apps.spreadsheet")) {
                    c = 'W';
                    break;
                }
                c = 65535;
                break;
            case -1988437312:
                if (mimeType.equals("application/x-x509-ca-cert")) {
                    c = '\b';
                    break;
                }
                c = 65535;
                break;
            case -1917350260:
                if (mimeType.equals("text/x-literate-haskell")) {
                    c = 28;
                    break;
                }
                c = 65535;
                break;
            case -1883861089:
                if (mimeType.equals("application/rss+xml")) {
                    c = 14;
                    break;
                }
                c = 65535;
                break;
            case -1808693885:
                if (mimeType.equals("text/x-pascal")) {
                    c = 29;
                    break;
                }
                c = 65535;
                break;
            case -1777056778:
                if (mimeType.equals("application/vnd.oasis.opendocument.image")) {
                    c = DateFormat.DAY;
                    break;
                }
                c = 65535;
                break;
            case -1747277413:
                if (mimeType.equals("application/vnd.sun.xml.writer.template")) {
                    c = '`';
                    break;
                }
                c = 65535;
                break;
            case -1719571662:
                if (mimeType.equals("application/vnd.oasis.opendocument.text")) {
                    c = 'X';
                    break;
                }
                c = 65535;
                break;
            case -1628346451:
                if (mimeType.equals("application/vnd.sun.xml.writer")) {
                    c = '^';
                    break;
                }
                c = 65535;
                break;
            case -1590813831:
                if (mimeType.equals("application/vnd.sun.xml.calc.template")) {
                    c = 'U';
                    break;
                }
                c = 65535;
                break;
            case -1506009513:
                if (mimeType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.template")) {
                    c = DateFormat.HOUR_OF_DAY;
                    break;
                }
                c = 65535;
                break;
            case -1386165903:
                if (mimeType.equals("application/x-kpresenter")) {
                    c = PhoneNumberUtils.WILD;
                    break;
                }
                c = 65535;
                break;
            case -1348236371:
                if (mimeType.equals("application/x-deb")) {
                    c = '9';
                    break;
                }
                c = 65535;
                break;
            case -1348228591:
                if (mimeType.equals("application/x-lha")) {
                    c = '0';
                    break;
                }
                c = 65535;
                break;
            case -1348228026:
                if (mimeType.equals("application/x-lzh")) {
                    c = '1';
                    break;
                }
                c = 65535;
                break;
            case -1348228010:
                if (mimeType.equals("application/x-lzx")) {
                    c = '2';
                    break;
                }
                c = 65535;
                break;
            case -1348221103:
                if (mimeType.equals("application/x-tar")) {
                    c = '4';
                    break;
                }
                c = 65535;
                break;
            case -1326989846:
                if (mimeType.equals("application/x-shockwave-flash")) {
                    c = 'e';
                    break;
                }
                c = 65535;
                break;
            case -1316922187:
                if (mimeType.equals("application/vnd.oasis.opendocument.text-template")) {
                    c = 'Z';
                    break;
                }
                c = 65535;
                break;
            case -1296467268:
                if (mimeType.equals("application/atom+xml")) {
                    c = '\"';
                    break;
                }
                c = 65535;
                break;
            case -1294595255:
                if (mimeType.equals("inode/directory")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case -1248334925:
                if (mimeType.equals("application/pdf")) {
                    c = 'J';
                    break;
                }
                c = 65535;
                break;
            case -1248333084:
                if (mimeType.equals("application/rar")) {
                    c = '*';
                    break;
                }
                c = 65535;
                break;
            case -1248326952:
                if (mimeType.equals("application/xml")) {
                    c = '&';
                    break;
                }
                c = 65535;
                break;
            case -1248325150:
                if (mimeType.equals("application/zip")) {
                    c = '+';
                    break;
                }
                c = 65535;
                break;
            case -1190438973:
                if (mimeType.equals("application/x-pkcs7-signature")) {
                    c = '\f';
                    break;
                }
                c = 65535;
                break;
            case -1143717099:
                if (mimeType.equals("application/x-pkcs7-certreqresp")) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case -1082243251:
                if (mimeType.equals(ClipDescription.MIMETYPE_TEXT_HTML)) {
                    c = 18;
                    break;
                }
                c = 65535;
                break;
            case -1073633483:
                if (mimeType.equals("application/vnd.openxmlformats-officedocument.presentationml.presentation")) {
                    c = DateFormat.MINUTE;
                    break;
                }
                c = 65535;
                break;
            case -1071817359:
                if (mimeType.equals("application/vnd.ms-powerpoint")) {
                    c = 'l';
                    break;
                }
                c = 65535;
                break;
            case -1050893613:
                if (mimeType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
                    c = 'g';
                    break;
                }
                c = 65535;
                break;
            case -1033484950:
                if (mimeType.equals("application/vnd.sun.xml.draw.template")) {
                    c = 'H';
                    break;
                }
                c = 65535;
                break;
            case -1004747231:
                if (mimeType.equals("text/css")) {
                    c = 17;
                    break;
                }
                c = 65535;
                break;
            case -1004727243:
                if (mimeType.equals("text/xml")) {
                    c = 19;
                    break;
                }
                c = 65535;
                break;
            case -958424608:
                if (mimeType.equals("text/calendar")) {
                    c = '=';
                    break;
                }
                c = 65535;
                break;
            case -951557661:
                if (mimeType.equals("application/vnd.google-apps.presentation")) {
                    c = 'P';
                    break;
                }
                c = 65535;
                break;
            case -779959281:
                if (mimeType.equals("application/vnd.sun.xml.calc")) {
                    c = 'T';
                    break;
                }
                c = 65535;
                break;
            case -779913474:
                if (mimeType.equals("application/vnd.sun.xml.draw")) {
                    c = 'G';
                    break;
                }
                c = 65535;
                break;
            case -723118015:
                if (mimeType.equals("application/x-javascript")) {
                    c = '(';
                    break;
                }
                c = 65535;
                break;
            case -676675015:
                if (mimeType.equals("application/vnd.oasis.opendocument.text-web")) {
                    c = '[';
                    break;
                }
                c = 65535;
                break;
            case -479218428:
                if (mimeType.equals("application/vnd.sun.xml.writer.global")) {
                    c = '_';
                    break;
                }
                c = 65535;
                break;
            case -427343476:
                if (mimeType.equals("application/x-webarchive-xml")) {
                    c = '6';
                    break;
                }
                c = 65535;
                break;
            case -396757341:
                if (mimeType.equals("application/vnd.sun.xml.impress.template")) {
                    c = DateFormat.MONTH;
                    break;
                }
                c = 65535;
                break;
            case -366307023:
                if (mimeType.equals("application/vnd.ms-excel")) {
                    c = 'i';
                    break;
                }
                c = 65535;
                break;
            case -261480694:
                if (mimeType.equals("text/x-chdr")) {
                    c = 22;
                    break;
                }
                c = 65535;
                break;
            case -261469704:
                if (mimeType.equals("text/x-csrc")) {
                    c = 23;
                    break;
                }
                c = 65535;
                break;
            case -261439913:
                if (mimeType.equals("text/x-dsrc")) {
                    c = 24;
                    break;
                }
                c = 65535;
                break;
            case -261278343:
                if (mimeType.equals("text/x-java")) {
                    c = 27;
                    break;
                }
                c = 65535;
                break;
            case -228136375:
                if (mimeType.equals("application/x-pkcs7-mime")) {
                    c = 11;
                    break;
                }
                c = 65535;
                break;
            case -221944004:
                if (mimeType.equals("application/x-font-ttf")) {
                    c = 'B';
                    break;
                }
                c = 65535;
                break;
            case -109382304:
                if (mimeType.equals("application/vnd.oasis.opendocument.spreadsheet-template")) {
                    c = 'R';
                    break;
                }
                c = 65535;
                break;
            case -43923783:
                if (mimeType.equals("application/gzip")) {
                    c = '7';
                    break;
                }
                c = 65535;
                break;
            case -43840953:
                if (mimeType.equals("application/json")) {
                    c = '$';
                    break;
                }
                c = 65535;
                break;
            case 26919318:
                if (mimeType.equals("application/x-iso9660-image")) {
                    c = '/';
                    break;
                }
                c = 65535;
                break;
            case 81142075:
                if (mimeType.equals("application/vnd.android.package-archive")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 163679941:
                if (mimeType.equals("application/pgp-signature")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case 180207563:
                if (mimeType.equals("application/x-stuffit")) {
                    c = '3';
                    break;
                }
                c = 65535;
                break;
            case 245790645:
                if (mimeType.equals("application/vnd.google-apps.drawing")) {
                    c = 'I';
                    break;
                }
                c = 65535;
                break;
            case 262346941:
                if (mimeType.equals("text/x-vcalendar")) {
                    c = '>';
                    break;
                }
                c = 65535;
                break;
            case 302189274:
                if (mimeType.equals(DocumentsContract.Document.MIME_TYPE_DIR)) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 302663708:
                if (mimeType.equals("application/ecmascript")) {
                    c = '#';
                    break;
                }
                c = 65535;
                break;
            case 363965503:
                if (mimeType.equals("application/x-rar-compressed")) {
                    c = ':';
                    break;
                }
                c = 65535;
                break;
            case 394650567:
                if (mimeType.equals("application/pgp-keys")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 428819984:
                if (mimeType.equals("application/vnd.oasis.opendocument.graphics")) {
                    c = 'C';
                    break;
                }
                c = 65535;
                break;
            case 501428239:
                if (mimeType.equals(ContactsContract.Contacts.CONTENT_VCARD_TYPE)) {
                    c = ';';
                    break;
                }
                c = 65535;
                break;
            case 571050671:
                if (mimeType.equals("application/vnd.stardivision.writer-global")) {
                    c = ']';
                    break;
                }
                c = 65535;
                break;
            case 603849904:
                if (mimeType.equals("application/xhtml+xml")) {
                    c = 16;
                    break;
                }
                c = 65535;
                break;
            case 641141505:
                if (mimeType.equals("application/x-texinfo")) {
                    c = '!';
                    break;
                }
                c = 65535;
                break;
            case 669516689:
                if (mimeType.equals("application/vnd.stardivision.impress")) {
                    c = 'K';
                    break;
                }
                c = 65535;
                break;
            case 694663701:
                if (mimeType.equals("application/vnd.openxmlformats-officedocument.presentationml.template")) {
                    c = 'n';
                    break;
                }
                c = 65535;
                break;
            case 717553764:
                if (mimeType.equals("application/vnd.google-apps.document")) {
                    c = 'c';
                    break;
                }
                c = 65535;
                break;
            case 822609188:
                if (mimeType.equals("text/vcard")) {
                    c = '<';
                    break;
                }
                c = 65535;
                break;
            case 822849473:
                if (mimeType.equals("text/x-csh")) {
                    c = 25;
                    break;
                }
                c = 65535;
                break;
            case 822865318:
                if (mimeType.equals("text/x-tcl")) {
                    c = 30;
                    break;
                }
                c = 65535;
                break;
            case 822865392:
                if (mimeType.equals("text/x-tex")) {
                    c = 31;
                    break;
                }
                c = 65535;
                break;
            case 859118878:
                if (mimeType.equals("application/x-abiword")) {
                    c = DateFormat.AM_PM;
                    break;
                }
                c = 65535;
                break;
            case 904647503:
                if (mimeType.equals("application/msword")) {
                    c = 'f';
                    break;
                }
                c = 65535;
                break;
            case 1043583697:
                if (mimeType.equals("application/x-pkcs7-certificates")) {
                    c = '\n';
                    break;
                }
                c = 65535;
                break;
            case 1060806194:
                if (mimeType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.template")) {
                    c = DateFormat.HOUR;
                    break;
                }
                c = 65535;
                break;
            case 1154415139:
                if (mimeType.equals("application/x-font")) {
                    c = '?';
                    break;
                }
                c = 65535;
                break;
            case 1154449330:
                if (mimeType.equals("application/x-gtar")) {
                    c = '.';
                    break;
                }
                c = 65535;
                break;
            case 1239557416:
                if (mimeType.equals("application/x-pkcs7-crl")) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case 1255211837:
                if (mimeType.equals("text/x-haskell")) {
                    c = 26;
                    break;
                }
                c = 65535;
                break;
            case 1283455191:
                if (mimeType.equals("application/x-apple-diskimage")) {
                    c = ',';
                    break;
                }
                c = 65535;
                break;
            case 1305955842:
                if (mimeType.equals("application/x-debian-package")) {
                    c = '-';
                    break;
                }
                c = 65535;
                break;
            case 1377360791:
                if (mimeType.equals("application/vnd.oasis.opendocument.graphics-template")) {
                    c = 'D';
                    break;
                }
                c = 65535;
                break;
            case 1383977381:
                if (mimeType.equals("application/vnd.sun.xml.impress")) {
                    c = DateFormat.STANDALONE_MONTH;
                    break;
                }
                c = 65535;
                break;
            case 1431987873:
                if (mimeType.equals("application/x-kword")) {
                    c = 'b';
                    break;
                }
                c = 65535;
                break;
            case 1432260414:
                if (mimeType.equals("application/x-latex")) {
                    c = ' ';
                    break;
                }
                c = 65535;
                break;
            case 1436962847:
                if (mimeType.equals("application/vnd.oasis.opendocument.presentation")) {
                    c = 'O';
                    break;
                }
                c = 65535;
                break;
            case 1440428940:
                if (mimeType.equals("application/javascript")) {
                    c = '%';
                    break;
                }
                c = 65535;
                break;
            case 1454024983:
                if (mimeType.equals("application/x-7z-compressed")) {
                    c = '8';
                    break;
                }
                c = 65535;
                break;
            case 1461751133:
                if (mimeType.equals("application/vnd.oasis.opendocument.text-master")) {
                    c = 'Y';
                    break;
                }
                c = 65535;
                break;
            case 1502452311:
                if (mimeType.equals("application/font-woff")) {
                    c = '@';
                    break;
                }
                c = 65535;
                break;
            case 1536912403:
                if (mimeType.equals("application/x-object")) {
                    c = 15;
                    break;
                }
                c = 65535;
                break;
            case 1573656544:
                if (mimeType.equals("application/x-pkcs12")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case 1577426419:
                if (mimeType.equals("application/vnd.openxmlformats-officedocument.presentationml.slideshow")) {
                    c = 'o';
                    break;
                }
                c = 65535;
                break;
            case 1637222218:
                if (mimeType.equals("application/x-kspread")) {
                    c = 'V';
                    break;
                }
                c = 65535;
                break;
            case 1643664935:
                if (mimeType.equals("application/vnd.oasis.opendocument.spreadsheet")) {
                    c = 'Q';
                    break;
                }
                c = 65535;
                break;
            case 1673742401:
                if (mimeType.equals("application/vnd.stardivision.writer")) {
                    c = '\\';
                    break;
                }
                c = 65535;
                break;
            case 1709755138:
                if (mimeType.equals("application/x-font-woff")) {
                    c = DateFormat.CAPITAL_AM_PM;
                    break;
                }
                c = 65535;
                break;
            case 1851895234:
                if (mimeType.equals("application/x-webarchive")) {
                    c = '5';
                    break;
                }
                c = 65535;
                break;
            case 1868769095:
                if (mimeType.equals("application/x-quicktimeplayer")) {
                    c = DateFormat.DATE;
                    break;
                }
                c = 65535;
                break;
            case 1948418893:
                if (mimeType.equals("application/mac-binhex40")) {
                    c = ')';
                    break;
                }
                c = 65535;
                break;
            case 1969663169:
                if (mimeType.equals("application/rdf+xml")) {
                    c = '\r';
                    break;
                }
                c = 65535;
                break;
            case 1993842850:
                if (mimeType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
                    c = 'j';
                    break;
                }
                c = 65535;
                break;
            case 2041423923:
                if (mimeType.equals("application/x-x509-user-cert")) {
                    c = '\t';
                    break;
                }
                c = 65535;
                break;
            case 2062084266:
                if (mimeType.equals("text/x-c++hdr")) {
                    c = 20;
                    break;
                }
                c = 65535;
                break;
            case 2062095256:
                if (mimeType.equals("text/x-c++src")) {
                    c = 21;
                    break;
                }
                c = 65535;
                break;
            case 2132236175:
                if (mimeType.equals("text/javascript")) {
                    c = DateFormat.QUOTE;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
            case 1:
                return buildTypeInfo(mimeType, R.drawable.ic_doc_folder, R.string.mime_type_folder, -1);
            case 2:
                return buildTypeInfo(mimeType, R.drawable.ic_doc_apk, R.string.mime_type_apk, -1);
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case '\b':
            case '\t':
            case '\n':
            case 11:
            case '\f':
                return buildTypeInfo(mimeType, R.drawable.ic_doc_certificate, R.string.mime_type_generic, R.string.mime_type_generic_ext);
            case '\r':
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
            case 20:
            case 21:
            case 22:
            case 23:
            case 24:
            case 25:
            case 26:
            case 27:
            case 28:
            case 29:
            case 30:
            case 31:
            case ' ':
            case '!':
            case '\"':
            case '#':
            case '$':
            case '%':
            case '&':
            case '\'':
            case '(':
                return buildTypeInfo(mimeType, R.drawable.ic_doc_codes, R.string.mime_type_document, R.string.mime_type_document_ext);
            case ')':
            case '*':
            case '+':
            case ',':
            case '-':
            case '.':
            case '/':
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
            case ':':
                return buildTypeInfo(mimeType, R.drawable.ic_doc_compressed, R.string.mime_type_compressed, R.string.mime_type_compressed_ext);
            case ';':
            case '<':
                return buildTypeInfo(mimeType, R.drawable.ic_doc_contact, R.string.mime_type_generic, R.string.mime_type_generic_ext);
            case '=':
            case '>':
                return buildTypeInfo(mimeType, R.drawable.ic_doc_event, R.string.mime_type_generic, R.string.mime_type_generic_ext);
            case '?':
            case '@':
            case 'A':
            case 'B':
                return buildTypeInfo(mimeType, R.drawable.ic_doc_font, R.string.mime_type_generic, R.string.mime_type_generic_ext);
            case 'C':
            case 'D':
            case 'E':
            case 'F':
            case 'G':
            case 'H':
            case 'I':
                return buildTypeInfo(mimeType, R.drawable.ic_doc_image, R.string.mime_type_image, R.string.mime_type_image_ext);
            case 'J':
                return buildTypeInfo(mimeType, R.drawable.ic_doc_pdf, R.string.mime_type_document, R.string.mime_type_document_ext);
            case 'K':
            case 'L':
            case 'M':
            case 'N':
            case 'O':
            case 'P':
                return buildTypeInfo(mimeType, R.drawable.ic_doc_presentation, R.string.mime_type_presentation, R.string.mime_type_presentation_ext);
            case 'Q':
            case 'R':
            case 'S':
            case 'T':
            case 'U':
            case 'V':
            case 'W':
                return buildTypeInfo(mimeType, R.drawable.ic_doc_spreadsheet, R.string.mime_type_spreadsheet, R.string.mime_type_spreadsheet_ext);
            case 'X':
            case 'Y':
            case 'Z':
            case '[':
            case '\\':
            case ']':
            case '^':
            case '_':
            case '`':
            case 'a':
            case 'b':
            case 'c':
                return buildTypeInfo(mimeType, R.drawable.ic_doc_document, R.string.mime_type_document, R.string.mime_type_document_ext);
            case 'd':
            case 'e':
                return buildTypeInfo(mimeType, R.drawable.ic_doc_video, R.string.mime_type_video, R.string.mime_type_video_ext);
            case 'f':
            case 'g':
            case 'h':
                return buildTypeInfo(mimeType, R.drawable.ic_doc_word, R.string.mime_type_document, R.string.mime_type_document_ext);
            case 'i':
            case 'j':
            case 'k':
                return buildTypeInfo(mimeType, R.drawable.ic_doc_excel, R.string.mime_type_spreadsheet, R.string.mime_type_spreadsheet_ext);
            case 'l':
            case 'm':
            case 'n':
            case 'o':
                return buildTypeInfo(mimeType, R.drawable.ic_doc_powerpoint, R.string.mime_type_presentation, R.string.mime_type_presentation_ext);
            default:
                return buildGenericTypeInfo(mimeType);
        }
    }

    private static ContentResolver.MimeTypeInfo buildGenericTypeInfo(String mimeType) {
        if (mimeType.startsWith("audio/")) {
            return buildTypeInfo(mimeType, R.drawable.ic_doc_audio, R.string.mime_type_audio, R.string.mime_type_audio_ext);
        }
        if (mimeType.startsWith("video/")) {
            return buildTypeInfo(mimeType, R.drawable.ic_doc_video, R.string.mime_type_video, R.string.mime_type_video_ext);
        }
        if (mimeType.startsWith(MessagingMessage.IMAGE_MIME_TYPE_PREFIX)) {
            return buildTypeInfo(mimeType, R.drawable.ic_doc_image, R.string.mime_type_image, R.string.mime_type_image_ext);
        }
        if (mimeType.startsWith("text/")) {
            return buildTypeInfo(mimeType, R.drawable.ic_doc_text, R.string.mime_type_document, R.string.mime_type_document_ext);
        }
        String bouncedMimeType = MimeUtils.guessMimeTypeFromExtension(MimeUtils.guessExtensionFromMimeType(mimeType));
        if (bouncedMimeType == null || Objects.equals(mimeType, bouncedMimeType)) {
            return buildTypeInfo(mimeType, R.drawable.ic_doc_generic, R.string.mime_type_generic, R.string.mime_type_generic_ext);
        }
        return buildTypeInfo(bouncedMimeType);
    }

    public static ContentResolver.MimeTypeInfo getTypeInfo(String mimeType) {
        ContentResolver.MimeTypeInfo res;
        String mimeType2 = mimeType.toLowerCase(Locale.US);
        synchronized (sCache) {
            res = sCache.get(mimeType2);
            if (res == null) {
                res = buildTypeInfo(mimeType2);
                sCache.put(mimeType2, res);
            }
        }
        return res;
    }

    public static void clearCache() {
        synchronized (sCache) {
            sCache.clear();
        }
    }
}
