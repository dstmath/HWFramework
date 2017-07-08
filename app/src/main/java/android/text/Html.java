package android.text;

import android.app.ActivityThread;
import android.graphics.drawable.Drawable;
import android.text.Layout.Alignment;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.AlignmentSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.BulletSpan;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.ParagraphStyle;
import android.text.style.QuoteSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.SubscriptSpan;
import android.text.style.SuperscriptSpan;
import android.text.style.TypefaceSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.AsyncService;
import com.android.internal.util.Protocol;
import com.huawei.pgmng.plug.PGSdk;
import org.ccil.cowan.tagsoup.HTMLSchema;
import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

public class Html {
    public static final int FROM_HTML_MODE_COMPACT = 63;
    public static final int FROM_HTML_MODE_LEGACY = 0;
    public static final int FROM_HTML_OPTION_USE_CSS_COLORS = 256;
    public static final int FROM_HTML_SEPARATOR_LINE_BREAK_BLOCKQUOTE = 32;
    public static final int FROM_HTML_SEPARATOR_LINE_BREAK_DIV = 16;
    public static final int FROM_HTML_SEPARATOR_LINE_BREAK_HEADING = 2;
    public static final int FROM_HTML_SEPARATOR_LINE_BREAK_LIST = 8;
    public static final int FROM_HTML_SEPARATOR_LINE_BREAK_LIST_ITEM = 4;
    public static final int FROM_HTML_SEPARATOR_LINE_BREAK_PARAGRAPH = 1;
    private static final int TO_HTML_PARAGRAPH_FLAG = 1;
    public static final int TO_HTML_PARAGRAPH_LINES_CONSECUTIVE = 0;
    public static final int TO_HTML_PARAGRAPH_LINES_INDIVIDUAL = 1;

    private static class HtmlParser {
        private static final HTMLSchema schema = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.text.Html.HtmlParser.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.text.Html.HtmlParser.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.text.Html.HtmlParser.<clinit>():void");
        }

        private HtmlParser() {
        }
    }

    public interface ImageGetter {
        Drawable getDrawable(String str);
    }

    public interface TagHandler {
        void handleTag(boolean z, String str, Editable editable, XMLReader xMLReader);
    }

    private Html() {
    }

    @Deprecated
    public static Spanned fromHtml(String source) {
        return fromHtml(source, TO_HTML_PARAGRAPH_LINES_CONSECUTIVE, null, null);
    }

    public static Spanned fromHtml(String source, int flags) {
        return fromHtml(source, flags, null, null);
    }

    @Deprecated
    public static Spanned fromHtml(String source, ImageGetter imageGetter, TagHandler tagHandler) {
        return fromHtml(source, TO_HTML_PARAGRAPH_LINES_CONSECUTIVE, imageGetter, tagHandler);
    }

    public static Spanned fromHtml(String source, int flags, ImageGetter imageGetter, TagHandler tagHandler) {
        Parser parser = new Parser();
        try {
            parser.setProperty("http://www.ccil.org/~cowan/tagsoup/properties/schema", HtmlParser.schema);
            return new HtmlToSpannedConverter(source, imageGetter, tagHandler, parser, flags).convert();
        } catch (SAXNotRecognizedException e) {
            throw new RuntimeException(e);
        } catch (SAXNotSupportedException e2) {
            throw new RuntimeException(e2);
        }
    }

    @Deprecated
    public static String toHtml(Spanned text) {
        return toHtml(text, TO_HTML_PARAGRAPH_LINES_CONSECUTIVE);
    }

    public static String toHtml(Spanned text, int option) {
        StringBuilder out = new StringBuilder();
        withinHtml(out, text, option);
        return out.toString();
    }

    public static String escapeHtml(CharSequence text) {
        StringBuilder out = new StringBuilder();
        withinStyle(out, text, TO_HTML_PARAGRAPH_LINES_CONSECUTIVE, text.length());
        return out.toString();
    }

    private static void withinHtml(StringBuilder out, Spanned text, int option) {
        if ((option & TO_HTML_PARAGRAPH_LINES_INDIVIDUAL) == 0) {
            encodeTextAlignmentByDiv(out, text, option);
        } else {
            withinDiv(out, text, TO_HTML_PARAGRAPH_LINES_CONSECUTIVE, text.length(), option);
        }
    }

    private static void encodeTextAlignmentByDiv(StringBuilder out, Spanned text, int option) {
        int len = text.length();
        int i = TO_HTML_PARAGRAPH_LINES_CONSECUTIVE;
        while (i < len) {
            int next = text.nextSpanTransition(i, len, ParagraphStyle.class);
            ParagraphStyle[] style = (ParagraphStyle[]) text.getSpans(i, next, ParagraphStyle.class);
            String elements = " ";
            boolean needDiv = false;
            for (int j = TO_HTML_PARAGRAPH_LINES_CONSECUTIVE; j < style.length; j += TO_HTML_PARAGRAPH_LINES_INDIVIDUAL) {
                if (style[j] instanceof AlignmentSpan) {
                    Alignment align = ((AlignmentSpan) style[j]).getAlignment();
                    needDiv = true;
                    if (align == Alignment.ALIGN_CENTER) {
                        elements = "align=\"center\" " + elements;
                    } else if (align == Alignment.ALIGN_OPPOSITE) {
                        elements = "align=\"right\" " + elements;
                    } else {
                        elements = "align=\"left\" " + elements;
                    }
                }
            }
            if (needDiv) {
                out.append("<div ").append(elements).append(">");
            }
            withinDiv(out, text, i, next, option);
            if (needDiv) {
                out.append("</div>");
            }
            i = next;
        }
    }

    private static void withinDiv(StringBuilder out, Spanned text, int start, int end, int option) {
        int i = start;
        while (i < end) {
            int i2;
            int next = text.nextSpanTransition(i, end, QuoteSpan.class);
            QuoteSpan[] quotes = (QuoteSpan[]) text.getSpans(i, next, QuoteSpan.class);
            int length = quotes.length;
            for (i2 = TO_HTML_PARAGRAPH_LINES_CONSECUTIVE; i2 < length; i2 += TO_HTML_PARAGRAPH_LINES_INDIVIDUAL) {
                QuoteSpan quote = quotes[i2];
                out.append("<blockquote>");
            }
            withinBlockquote(out, text, i, next, option);
            length = quotes.length;
            for (i2 = TO_HTML_PARAGRAPH_LINES_CONSECUTIVE; i2 < length; i2 += TO_HTML_PARAGRAPH_LINES_INDIVIDUAL) {
                quote = quotes[i2];
                out.append("</blockquote>\n");
            }
            i = next;
        }
    }

    private static String getTextDirection(Spanned text, int start, int end) {
        int len = end - start;
        byte[] levels = ArrayUtils.newUnpaddedByteArray(len);
        char[] buffer = TextUtils.obtain(len);
        TextUtils.getChars(text, start, end, buffer, TO_HTML_PARAGRAPH_LINES_CONSECUTIVE);
        switch (AndroidBidi.bidi(FROM_HTML_SEPARATOR_LINE_BREAK_HEADING, buffer, levels, len, false)) {
            case PGSdk.TYPE_UNKNOW /*-1*/:
                return " dir=\"rtl\"";
            default:
                return " dir=\"ltr\"";
        }
    }

    private static String getTextStyles(Spanned text, int start, int end, boolean forceNoVerticalMargin, boolean includeTextAlign) {
        String margin = null;
        String textAlign = null;
        if (forceNoVerticalMargin) {
            margin = "margin-top:0; margin-bottom:0;";
        }
        if (includeTextAlign) {
            AlignmentSpan[] alignmentSpans = (AlignmentSpan[]) text.getSpans(start, end, AlignmentSpan.class);
            int i = alignmentSpans.length - 1;
            while (i >= 0) {
                AlignmentSpan s = alignmentSpans[i];
                if ((text.getSpanFlags(s) & 51) == 51) {
                    Alignment alignment = s.getAlignment();
                    if (alignment == Alignment.ALIGN_NORMAL) {
                        textAlign = "text-align:start;";
                    } else if (alignment == Alignment.ALIGN_CENTER) {
                        textAlign = "text-align:center;";
                    } else if (alignment == Alignment.ALIGN_OPPOSITE) {
                        textAlign = "text-align:end;";
                    }
                } else {
                    i--;
                }
            }
        }
        if (margin == null && textAlign == null) {
            return "";
        }
        StringBuilder style = new StringBuilder(" style=\"");
        if (margin != null && textAlign != null) {
            style.append(margin).append(" ").append(textAlign);
        } else if (margin != null) {
            style.append(margin);
        } else if (textAlign != null) {
            style.append(textAlign);
        }
        return style.append("\"").toString();
    }

    private static void withinBlockquote(StringBuilder out, Spanned text, int start, int end, int option) {
        if ((option & TO_HTML_PARAGRAPH_LINES_INDIVIDUAL) == 0) {
            withinBlockquoteConsecutive(out, text, start, end);
        } else {
            withinBlockquoteIndividual(out, text, start, end);
        }
    }

    private static void withinBlockquoteIndividual(StringBuilder out, Spanned text, int start, int end) {
        boolean isInList = false;
        int i = start;
        while (i <= end) {
            int next = TextUtils.indexOf((CharSequence) text, '\n', i, end);
            if (next < 0) {
                next = end;
            }
            if (next == i) {
                if (isInList) {
                    isInList = false;
                    out.append("</ul>\n");
                }
                out.append("<br>\n");
            } else {
                boolean isListItem = false;
                ParagraphStyle[] paragraphStyles = (ParagraphStyle[]) text.getSpans(i, next, ParagraphStyle.class);
                int length = paragraphStyles.length;
                for (int i2 = TO_HTML_PARAGRAPH_LINES_CONSECUTIVE; i2 < length; i2 += TO_HTML_PARAGRAPH_LINES_INDIVIDUAL) {
                    ParagraphStyle paragraphStyle = paragraphStyles[i2];
                    if ((text.getSpanFlags(paragraphStyle) & 51) == 51 && (paragraphStyle instanceof BulletSpan)) {
                        isListItem = true;
                        break;
                    }
                }
                if (isListItem && !isInList) {
                    isInList = true;
                    out.append("<ul").append(getTextStyles(text, i, next, true, false)).append(">\n");
                }
                if (isInList && !isListItem) {
                    isInList = false;
                    out.append("</ul>\n");
                }
                String tagType = isListItem ? "li" : "p";
                out.append("<").append(tagType).append(getTextDirection(text, i, next)).append(getTextStyles(text, i, next, !isListItem, true)).append(">");
                withinParagraph(out, text, i, next);
                out.append("</");
                out.append(tagType);
                out.append(">\n");
                if (next == end && isInList) {
                    isInList = false;
                    out.append("</ul>\n");
                }
            }
            i = next + TO_HTML_PARAGRAPH_LINES_INDIVIDUAL;
        }
    }

    private static void withinBlockquoteConsecutive(StringBuilder out, Spanned text, int start, int end) {
        out.append("<p").append(getTextDirection(text, start, end)).append(">");
        int i = start;
        while (i < end) {
            int next = TextUtils.indexOf((CharSequence) text, '\n', i, end);
            if (next < 0) {
                next = end;
            }
            int nl = TO_HTML_PARAGRAPH_LINES_CONSECUTIVE;
            while (next < end && text.charAt(next) == '\n') {
                nl += TO_HTML_PARAGRAPH_LINES_INDIVIDUAL;
                next += TO_HTML_PARAGRAPH_LINES_INDIVIDUAL;
            }
            withinParagraph(out, text, i, next - nl);
            if (nl == TO_HTML_PARAGRAPH_LINES_INDIVIDUAL) {
                out.append("<br>\n");
            } else {
                for (int j = FROM_HTML_SEPARATOR_LINE_BREAK_HEADING; j < nl; j += TO_HTML_PARAGRAPH_LINES_INDIVIDUAL) {
                    out.append("<br>");
                }
                if (next != end) {
                    out.append("</p>\n");
                    out.append("<p").append(getTextDirection(text, i, next - nl)).append(">");
                }
            }
            i = next;
        }
        out.append("</p>\n");
    }

    private static void withinParagraph(StringBuilder out, Spanned text, int start, int end) {
        int i = start;
        while (i < end) {
            int j;
            int next = text.nextSpanTransition(i, end, CharacterStyle.class);
            CharacterStyle[] style = (CharacterStyle[]) text.getSpans(i, next, CharacterStyle.class);
            for (j = TO_HTML_PARAGRAPH_LINES_CONSECUTIVE; j < style.length; j += TO_HTML_PARAGRAPH_LINES_INDIVIDUAL) {
                int s;
                if (style[j] instanceof StyleSpan) {
                    s = ((StyleSpan) style[j]).getStyle();
                    if ((s & TO_HTML_PARAGRAPH_LINES_INDIVIDUAL) != 0) {
                        out.append("<b>");
                    }
                    if ((s & FROM_HTML_SEPARATOR_LINE_BREAK_HEADING) != 0) {
                        out.append("<i>");
                    }
                }
                if (style[j] instanceof TypefaceSpan) {
                    if ("monospace".equals(((TypefaceSpan) style[j]).getFamily())) {
                        out.append("<tt>");
                    }
                }
                if (style[j] instanceof SuperscriptSpan) {
                    out.append("<sup>");
                }
                if (style[j] instanceof SubscriptSpan) {
                    out.append("<sub>");
                }
                if (style[j] instanceof UnderlineSpan) {
                    out.append("<u>");
                }
                if (style[j] instanceof StrikethroughSpan) {
                    out.append("<span style=\"text-decoration:line-through;\">");
                }
                if (style[j] instanceof URLSpan) {
                    out.append("<a href=\"");
                    out.append(((URLSpan) style[j]).getURL());
                    out.append("\">");
                }
                if (style[j] instanceof ImageSpan) {
                    out.append("<img src=\"");
                    out.append(((ImageSpan) style[j]).getSource());
                    out.append("\">");
                    i = next;
                }
                if (style[j] instanceof AbsoluteSizeSpan) {
                    AbsoluteSizeSpan s2 = style[j];
                    float sizeDip = (float) s2.getSize();
                    if (!s2.getDip()) {
                        sizeDip /= ActivityThread.currentApplication().getResources().getDisplayMetrics().density;
                    }
                    Object[] objArr = new Object[TO_HTML_PARAGRAPH_LINES_INDIVIDUAL];
                    objArr[TO_HTML_PARAGRAPH_LINES_CONSECUTIVE] = Float.valueOf(sizeDip);
                    out.append(String.format("<span style=\"font-size:%.0fpx\";>", objArr));
                }
                if (style[j] instanceof RelativeSizeSpan) {
                    objArr = new Object[TO_HTML_PARAGRAPH_LINES_INDIVIDUAL];
                    objArr[TO_HTML_PARAGRAPH_LINES_CONSECUTIVE] = Float.valueOf(((RelativeSizeSpan) style[j]).getSizeChange());
                    out.append(String.format("<span style=\"font-size:%.2fem;\">", objArr));
                }
                if (style[j] instanceof ForegroundColorSpan) {
                    objArr = new Object[TO_HTML_PARAGRAPH_LINES_INDIVIDUAL];
                    objArr[TO_HTML_PARAGRAPH_LINES_CONSECUTIVE] = Integer.valueOf(AsyncService.CMD_ASYNC_SERVICE_ON_START_INTENT & ((ForegroundColorSpan) style[j]).getForegroundColor());
                    out.append(String.format("<span style=\"color:#%06X;\">", objArr));
                }
                if (style[j] instanceof BackgroundColorSpan) {
                    objArr = new Object[TO_HTML_PARAGRAPH_LINES_INDIVIDUAL];
                    objArr[TO_HTML_PARAGRAPH_LINES_CONSECUTIVE] = Integer.valueOf(AsyncService.CMD_ASYNC_SERVICE_ON_START_INTENT & ((BackgroundColorSpan) style[j]).getBackgroundColor());
                    out.append(String.format("<span style=\"background-color:#%06X;\">", objArr));
                }
            }
            withinStyle(out, text, i, next);
            j = style.length - 1;
            while (j >= 0) {
                if (style[j] instanceof BackgroundColorSpan) {
                    out.append("</span>");
                }
                if (style[j] instanceof ForegroundColorSpan) {
                    out.append("</span>");
                }
                if (style[j] instanceof RelativeSizeSpan) {
                    out.append("</span>");
                }
                if (style[j] instanceof AbsoluteSizeSpan) {
                    out.append("</span>");
                }
                if (style[j] instanceof URLSpan) {
                    out.append("</a>");
                }
                if (style[j] instanceof StrikethroughSpan) {
                    out.append("</span>");
                }
                if (style[j] instanceof UnderlineSpan) {
                    out.append("</u>");
                }
                if (style[j] instanceof SubscriptSpan) {
                    out.append("</sub>");
                }
                if (style[j] instanceof SuperscriptSpan) {
                    out.append("</sup>");
                }
                if ((style[j] instanceof TypefaceSpan) && ((TypefaceSpan) style[j]).getFamily().equals("monospace")) {
                    out.append("</tt>");
                }
                if (style[j] instanceof StyleSpan) {
                    s = ((StyleSpan) style[j]).getStyle();
                    if ((s & TO_HTML_PARAGRAPH_LINES_INDIVIDUAL) != 0) {
                        out.append("</b>");
                    }
                    if ((s & FROM_HTML_SEPARATOR_LINE_BREAK_HEADING) != 0) {
                        out.append("</i>");
                    }
                }
                j--;
            }
            i = next;
        }
    }

    private static void withinStyle(StringBuilder out, CharSequence text, int start, int end) {
        int i = start;
        while (i < end) {
            char c = text.charAt(i);
            if (c == '<') {
                out.append("&lt;");
            } else if (c == '>') {
                out.append("&gt;");
            } else if (c == '&') {
                out.append("&amp;");
            } else if (c < '\ud800' || c > '\udfff') {
                if (c > '~' || c < ' ') {
                    out.append("&#").append(c).append(";");
                } else if (c == ' ') {
                    while (i + TO_HTML_PARAGRAPH_LINES_INDIVIDUAL < end && text.charAt(i + TO_HTML_PARAGRAPH_LINES_INDIVIDUAL) == ' ') {
                        out.append("&nbsp;");
                        i += TO_HTML_PARAGRAPH_LINES_INDIVIDUAL;
                    }
                    out.append(' ');
                } else {
                    out.append(c);
                }
            } else if (c < '\udc00' && i + TO_HTML_PARAGRAPH_LINES_INDIVIDUAL < end) {
                char d = text.charAt(i + TO_HTML_PARAGRAPH_LINES_INDIVIDUAL);
                if (d >= '\udc00' && d <= '\udfff') {
                    i += TO_HTML_PARAGRAPH_LINES_INDIVIDUAL;
                    out.append("&#").append((((c - 55296) << 10) | Protocol.BASE_SYSTEM_RESERVED) | (d - 56320)).append(";");
                }
            }
            i += TO_HTML_PARAGRAPH_LINES_INDIVIDUAL;
        }
    }
}
