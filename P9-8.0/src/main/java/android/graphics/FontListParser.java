package android.graphics;

import android.graphics.fonts.FontVariationAxis;
import android.media.midi.MidiDeviceInfo;
import android.media.tv.TvContract.PreviewPrograms;
import android.net.ProxyInfo;
import android.os.DropBoxManager;
import android.text.FontConfig;
import android.text.FontConfig.Alias;
import android.text.FontConfig.Family;
import android.text.FontConfig.Font;
import android.util.Xml;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class FontListParser {
    private static final Pattern FILENAME_WHITESPACE_PATTERN = Pattern.compile("^[ \\n\\r\\t]+|[ \\n\\r\\t]+$");

    public static FontConfig parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(in, null);
            parser.nextTag();
            FontConfig readFamilies = readFamilies(parser);
            return readFamilies;
        } finally {
            in.close();
        }
    }

    private static FontConfig readFamilies(XmlPullParser parser) throws XmlPullParserException, IOException {
        List<Family> families = new ArrayList();
        List<Alias> aliases = new ArrayList();
        parser.require(2, null, "familyset");
        while (parser.next() != 3) {
            if (parser.getEventType() == 2) {
                String tag = parser.getName();
                if (tag.equals("family")) {
                    families.add(readFamily(parser));
                } else if (tag.equals("alias")) {
                    aliases.add(readAlias(parser));
                } else {
                    skip(parser);
                }
            }
        }
        return new FontConfig((Family[]) families.toArray(new Family[families.size()]), (Alias[]) aliases.toArray(new Alias[aliases.size()]));
    }

    private static Family readFamily(XmlPullParser parser) throws XmlPullParserException, IOException {
        String name = parser.getAttributeValue(null, MidiDeviceInfo.PROPERTY_NAME);
        String lang = parser.getAttributeValue(null, "lang");
        String variant = parser.getAttributeValue(null, "variant");
        List<Font> fonts = new ArrayList();
        while (parser.next() != 3) {
            if (parser.getEventType() == 2) {
                if (parser.getName().equals("font")) {
                    fonts.add(readFont(parser));
                } else {
                    skip(parser);
                }
            }
        }
        int intVariant = 0;
        if (variant != null) {
            if (variant.equals("compact")) {
                intVariant = 1;
            } else if (variant.equals("elegant")) {
                intVariant = 2;
            }
        }
        return new Family(name, (Font[]) fonts.toArray(new Font[fonts.size()]), lang, intVariant);
    }

    private static Font readFont(XmlPullParser parser) throws XmlPullParserException, IOException {
        String indexStr = parser.getAttributeValue(null, "index");
        int index = indexStr == null ? 0 : Integer.parseInt(indexStr);
        List<FontVariationAxis> axes = new ArrayList();
        String weightStr = parser.getAttributeValue(null, PreviewPrograms.COLUMN_WEIGHT);
        int weight = weightStr == null ? 400 : Integer.parseInt(weightStr);
        boolean isItalic = "italic".equals(parser.getAttributeValue(null, "style"));
        StringBuilder filename = new StringBuilder();
        while (parser.next() != 3) {
            if (parser.getEventType() == 4) {
                filename.append(parser.getText());
            }
            if (parser.getEventType() == 2) {
                if (parser.getName().equals("axis")) {
                    axes.add(readAxis(parser));
                } else {
                    skip(parser);
                }
            }
        }
        return new Font(FILENAME_WHITESPACE_PATTERN.matcher(filename).replaceAll(ProxyInfo.LOCAL_EXCL_LIST), index, (FontVariationAxis[]) axes.toArray(new FontVariationAxis[axes.size()]), weight, isItalic);
    }

    private static FontVariationAxis readAxis(XmlPullParser parser) throws XmlPullParserException, IOException {
        String tagStr = parser.getAttributeValue(null, DropBoxManager.EXTRA_TAG);
        String styleValueStr = parser.getAttributeValue(null, "stylevalue");
        skip(parser);
        return new FontVariationAxis(tagStr, Float.parseFloat(styleValueStr));
    }

    private static Alias readAlias(XmlPullParser parser) throws XmlPullParserException, IOException {
        int weight;
        String name = parser.getAttributeValue(null, MidiDeviceInfo.PROPERTY_NAME);
        String toName = parser.getAttributeValue(null, "to");
        String weightStr = parser.getAttributeValue(null, PreviewPrograms.COLUMN_WEIGHT);
        if (weightStr == null) {
            weight = 400;
        } else {
            weight = Integer.parseInt(weightStr);
        }
        skip(parser);
        return new Alias(name, toName, weight);
    }

    private static void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        int depth = 1;
        while (depth > 0) {
            switch (parser.next()) {
                case 2:
                    depth++;
                    break;
                case 3:
                    depth--;
                    break;
                default:
                    break;
            }
        }
    }
}
