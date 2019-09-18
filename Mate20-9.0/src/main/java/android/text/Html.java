package android.text;

import android.app.ActivityThread;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiEnterpriseConfig;
import android.text.Layout;
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
        /* access modifiers changed from: private */
        public static final HTMLSchema schema = new HTMLSchema();

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
        return fromHtml(source, 0, null, null);
    }

    public static Spanned fromHtml(String source, int flags) {
        return fromHtml(source, flags, null, null);
    }

    @Deprecated
    public static Spanned fromHtml(String source, ImageGetter imageGetter, TagHandler tagHandler) {
        return fromHtml(source, 0, imageGetter, tagHandler);
    }

    public static Spanned fromHtml(String source, int flags, ImageGetter imageGetter, TagHandler tagHandler) {
        Parser parser = new Parser();
        try {
            parser.setProperty("http://www.ccil.org/~cowan/tagsoup/properties/schema", HtmlParser.schema);
            HtmlToSpannedConverter converter = new HtmlToSpannedConverter(source, imageGetter, tagHandler, parser, flags);
            return converter.convert();
        } catch (SAXNotRecognizedException e) {
            throw new RuntimeException(e);
        } catch (SAXNotSupportedException e2) {
            throw new RuntimeException(e2);
        }
    }

    @Deprecated
    public static String toHtml(Spanned text) {
        return toHtml(text, 0);
    }

    public static String toHtml(Spanned text, int option) {
        StringBuilder out = new StringBuilder();
        withinHtml(out, text, option);
        return out.toString();
    }

    public static String escapeHtml(CharSequence text) {
        StringBuilder out = new StringBuilder();
        withinStyle(out, text, 0, text.length());
        return out.toString();
    }

    private static void withinHtml(StringBuilder out, Spanned text, int option) {
        if ((option & 1) == 0) {
            encodeTextAlignmentByDiv(out, text, option);
        } else {
            withinDiv(out, text, 0, text.length(), option);
        }
    }

    private static void encodeTextAlignmentByDiv(StringBuilder out, Spanned text, int option) {
        int len = text.length();
        int i = 0;
        while (i < len) {
            int next = text.nextSpanTransition(i, len, ParagraphStyle.class);
            ParagraphStyle[] style = (ParagraphStyle[]) text.getSpans(i, next, ParagraphStyle.class);
            boolean needDiv = false;
            String elements = WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER;
            for (int j = 0; j < style.length; j++) {
                if (style[j] instanceof AlignmentSpan) {
                    Layout.Alignment align = ((AlignmentSpan) style[j]).getAlignment();
                    needDiv = true;
                    if (align == Layout.Alignment.ALIGN_CENTER) {
                        elements = "align=\"center\" " + elements;
                    } else if (align == Layout.Alignment.ALIGN_OPPOSITE) {
                        elements = "align=\"right\" " + elements;
                    } else {
                        elements = "align=\"left\" " + elements;
                    }
                }
            }
            if (needDiv) {
                out.append("<div ");
                out.append(elements);
                out.append(">");
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
            int next = text.nextSpanTransition(i, end, QuoteSpan.class);
            QuoteSpan[] quotes = (QuoteSpan[]) text.getSpans(i, next, QuoteSpan.class);
            for (QuoteSpan quoteSpan : quotes) {
                out.append("<blockquote>");
            }
            withinBlockquote(out, text, i, next, option);
            for (QuoteSpan quoteSpan2 : quotes) {
                out.append("</blockquote>\n");
            }
            i = next;
        }
    }

    private static String getTextDirection(Spanned text, int start, int end) {
        if (TextDirectionHeuristics.FIRSTSTRONG_LTR.isRtl((CharSequence) text, start, end - start)) {
            return " dir=\"rtl\"";
        }
        return " dir=\"ltr\"";
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
            while (true) {
                if (i < 0) {
                    break;
                }
                AlignmentSpan s = alignmentSpans[i];
                if ((text.getSpanFlags(s) & 51) == 51) {
                    Layout.Alignment alignment = s.getAlignment();
                    if (alignment == Layout.Alignment.ALIGN_NORMAL) {
                        textAlign = "text-align:start;";
                    } else if (alignment == Layout.Alignment.ALIGN_CENTER) {
                        textAlign = "text-align:center;";
                    } else if (alignment == Layout.Alignment.ALIGN_OPPOSITE) {
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
            style.append(margin);
            style.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
            style.append(textAlign);
        } else if (margin != null) {
            style.append(margin);
        } else if (textAlign != null) {
            style.append(textAlign);
        }
        style.append("\"");
        return style.toString();
    }

    private static void withinBlockquote(StringBuilder out, Spanned text, int start, int end, int option) {
        if ((option & 1) == 0) {
            withinBlockquoteConsecutive(out, text, start, end);
        } else {
            withinBlockquoteIndividual(out, text, start, end);
        }
    }

    private static void withinBlockquoteIndividual(StringBuilder out, Spanned text, int start, int end) {
        boolean isInList = false;
        int i = start;
        while (i <= end) {
            int next = TextUtils.indexOf((CharSequence) text, 10, i, end);
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
                boolean z = false;
                int i2 = 0;
                while (true) {
                    if (i2 >= length) {
                        break;
                    }
                    ParagraphStyle paragraphStyle = paragraphStyles[i2];
                    if ((text.getSpanFlags(paragraphStyle) & 51) == 51 && (paragraphStyle instanceof BulletSpan)) {
                        isListItem = true;
                        break;
                    }
                    i2++;
                }
                if (isListItem && !isInList) {
                    isInList = true;
                    out.append("<ul");
                    out.append(getTextStyles(text, i, next, true, false));
                    out.append(">\n");
                }
                if (isInList && !isListItem) {
                    isInList = false;
                    out.append("</ul>\n");
                }
                String tagType = isListItem ? "li" : "p";
                out.append("<");
                out.append(tagType);
                out.append(getTextDirection(text, i, next));
                if (!isListItem) {
                    z = true;
                }
                out.append(getTextStyles(text, i, next, z, true));
                out.append(">");
                withinParagraph(out, text, i, next);
                out.append("</");
                out.append(tagType);
                out.append(">\n");
                if (next == end && isInList) {
                    isInList = false;
                    out.append("</ul>\n");
                }
            }
            i = next + 1;
        }
    }

    private static void withinBlockquoteConsecutive(StringBuilder out, Spanned text, int start, int end) {
        out.append("<p");
        out.append(getTextDirection(text, start, end));
        out.append(">");
        int i = start;
        while (i < end) {
            int next = TextUtils.indexOf((CharSequence) text, 10, i, end);
            if (next < 0) {
                next = end;
            }
            int nl = 0;
            while (next < end && text.charAt(next) == 10) {
                nl++;
                next++;
            }
            withinParagraph(out, text, i, next - nl);
            if (nl == 1) {
                out.append("<br>\n");
            } else {
                for (int j = 2; j < nl; j++) {
                    out.append("<br>");
                }
                if (next != end) {
                    out.append("</p>\n");
                    out.append("<p");
                    out.append(getTextDirection(text, i, next - nl));
                    out.append(">");
                }
            }
            i = next;
        }
        out.append("</p>\n");
    }

    private static void withinParagraph(StringBuilder out, Spanned text, int start, int end) {
        int j = start;
        while (j < end) {
            int next = text.nextSpanTransition(j, end, CharacterStyle.class);
            CharacterStyle[] style = (CharacterStyle[]) text.getSpans(j, next, CharacterStyle.class);
            int i = j;
            for (int j2 = 0; j2 < style.length; j2++) {
                if (style[j2] instanceof StyleSpan) {
                    int s = ((StyleSpan) style[j2]).getStyle();
                    if ((s & 1) != 0) {
                        out.append("<b>");
                    }
                    if ((s & 2) != 0) {
                        out.append("<i>");
                    }
                }
                if ((style[j2] instanceof TypefaceSpan) && "monospace".equals(((TypefaceSpan) style[j2]).getFamily())) {
                    out.append("<tt>");
                }
                if (style[j2] instanceof SuperscriptSpan) {
                    out.append("<sup>");
                }
                if (style[j2] instanceof SubscriptSpan) {
                    out.append("<sub>");
                }
                if (style[j2] instanceof UnderlineSpan) {
                    out.append("<u>");
                }
                if (style[j2] instanceof StrikethroughSpan) {
                    out.append("<span style=\"text-decoration:line-through;\">");
                }
                if (style[j2] instanceof URLSpan) {
                    out.append("<a href=\"");
                    out.append(((URLSpan) style[j2]).getURL());
                    out.append("\">");
                }
                if (style[j2] instanceof ImageSpan) {
                    out.append("<img src=\"");
                    out.append(((ImageSpan) style[j2]).getSource());
                    out.append("\">");
                    i = next;
                }
                if (style[j2] instanceof AbsoluteSizeSpan) {
                    AbsoluteSizeSpan s2 = (AbsoluteSizeSpan) style[j2];
                    float sizeDip = (float) s2.getSize();
                    if (!s2.getDip()) {
                        sizeDip /= ActivityThread.currentApplication().getResources().getDisplayMetrics().density;
                    }
                    out.append(String.format("<span style=\"font-size:%.0fpx\";>", new Object[]{Float.valueOf(sizeDip)}));
                }
                if (style[j2] instanceof RelativeSizeSpan) {
                    out.append(String.format("<span style=\"font-size:%.2fem;\">", new Object[]{Float.valueOf(((RelativeSizeSpan) style[j2]).getSizeChange())}));
                }
                if (style[j2] instanceof ForegroundColorSpan) {
                    out.append(String.format("<span style=\"color:#%06X;\">", new Object[]{Integer.valueOf(16777215 & ((ForegroundColorSpan) style[j2]).getForegroundColor())}));
                }
                if (style[j2] instanceof BackgroundColorSpan) {
                    out.append(String.format("<span style=\"background-color:#%06X;\">", new Object[]{Integer.valueOf(16777215 & ((BackgroundColorSpan) style[j2]).getBackgroundColor())}));
                }
            }
            withinStyle(out, text, i, next);
            for (int j3 = style.length - 1; j3 >= 0; j3--) {
                if (style[j3] instanceof BackgroundColorSpan) {
                    out.append("</span>");
                }
                if (style[j3] instanceof ForegroundColorSpan) {
                    out.append("</span>");
                }
                if (style[j3] instanceof RelativeSizeSpan) {
                    out.append("</span>");
                }
                if (style[j3] instanceof AbsoluteSizeSpan) {
                    out.append("</span>");
                }
                if (style[j3] instanceof URLSpan) {
                    out.append("</a>");
                }
                if (style[j3] instanceof StrikethroughSpan) {
                    out.append("</span>");
                }
                if (style[j3] instanceof UnderlineSpan) {
                    out.append("</u>");
                }
                if (style[j3] instanceof SubscriptSpan) {
                    out.append("</sub>");
                }
                if (style[j3] instanceof SuperscriptSpan) {
                    out.append("</sup>");
                }
                if ((style[j3] instanceof TypefaceSpan) && ((TypefaceSpan) style[j3]).getFamily().equals("monospace")) {
                    out.append("</tt>");
                }
                if (style[j3] instanceof StyleSpan) {
                    int s3 = ((StyleSpan) style[j3]).getStyle();
                    if ((s3 & 1) != 0) {
                        out.append("</b>");
                    }
                    if ((s3 & 2) != 0) {
                        out.append("</i>");
                    }
                }
            }
            j = next;
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
            } else if (c < 55296 || c > 57343) {
                if (c > '~' || c < ' ') {
                    out.append("&#");
                    out.append(c);
                    out.append(";");
                } else if (c == ' ') {
                    while (i + 1 < end && text.charAt(i + 1) == ' ') {
                        out.append("&nbsp;");
                        i++;
                    }
                    out.append(' ');
                } else {
                    out.append(c);
                }
            } else if (c < 56320 && i + 1 < end) {
                char d = text.charAt(i + 1);
                if (d >= 56320 && d <= 57343) {
                    i++;
                    out.append("&#");
                    out.append(((c - 55296) << 10) | 65536 | (d - 56320));
                    out.append(";");
                }
            }
            i++;
        }
    }
}
