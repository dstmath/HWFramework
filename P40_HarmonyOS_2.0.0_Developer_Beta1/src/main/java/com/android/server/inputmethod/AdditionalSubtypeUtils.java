package com.android.server.inputmethod;

import android.os.Environment;
import android.os.FileUtils;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.AtomicFile;
import android.util.Slog;
import android.util.Xml;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodSubtype;
import com.android.internal.util.FastXmlSerializer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

/* access modifiers changed from: package-private */
public final class AdditionalSubtypeUtils {
    private static final String ADDITIONAL_SUBTYPES_FILE_NAME = "subtypes.xml";
    private static final String ATTR_ICON = "icon";
    private static final String ATTR_ID = "id";
    private static final String ATTR_IME_SUBTYPE_EXTRA_VALUE = "imeSubtypeExtraValue";
    private static final String ATTR_IME_SUBTYPE_ID = "subtypeId";
    private static final String ATTR_IME_SUBTYPE_LANGUAGE_TAG = "languageTag";
    private static final String ATTR_IME_SUBTYPE_LOCALE = "imeSubtypeLocale";
    private static final String ATTR_IME_SUBTYPE_MODE = "imeSubtypeMode";
    private static final String ATTR_IS_ASCII_CAPABLE = "isAsciiCapable";
    private static final String ATTR_IS_AUXILIARY = "isAuxiliary";
    private static final String ATTR_LABEL = "label";
    private static final String INPUT_METHOD_PATH = "inputmethod";
    private static final String NODE_IMI = "imi";
    private static final String NODE_SUBTYPE = "subtype";
    private static final String NODE_SUBTYPES = "subtypes";
    private static final String SYSTEM_PATH = "system";
    private static final String TAG = "AdditionalSubtypeUtils";

    private AdditionalSubtypeUtils() {
    }

    private static File getInputMethodDir(int userId) {
        File systemDir;
        if (userId == 0) {
            systemDir = new File(Environment.getDataDirectory(), SYSTEM_PATH);
        } else {
            systemDir = Environment.getUserSystemDirectory(userId);
        }
        return new File(systemDir, INPUT_METHOD_PATH);
    }

    private static AtomicFile getAdditionalSubtypeFile(File inputMethodDir) {
        return new AtomicFile(new File(inputMethodDir, ADDITIONAL_SUBTYPES_FILE_NAME), "input-subtypes");
    }

    /* JADX WARNING: Removed duplicated region for block: B:63:0x01c2  */
    /* JADX WARNING: Removed duplicated region for block: B:75:? A[RETURN, SYNTHETIC] */
    static void save(ArrayMap<String, List<InputMethodSubtype>> allSubtypes, ArrayMap<String, InputMethodInfo> methodMap, int userId) {
        IOException e;
        File inputMethodDir;
        ArrayMap<String, InputMethodInfo> arrayMap = methodMap;
        File inputMethodDir2 = getInputMethodDir(userId);
        if (allSubtypes.isEmpty()) {
            if (inputMethodDir2.exists()) {
                AtomicFile subtypesFile = getAdditionalSubtypeFile(inputMethodDir2);
                if (subtypesFile.exists()) {
                    subtypesFile.delete();
                }
                if (FileUtils.listFilesOrEmpty(inputMethodDir2).length == 0 && !inputMethodDir2.delete()) {
                    Slog.e(TAG, "Failed to delete the empty parent directory " + inputMethodDir2);
                }
            }
        } else if (inputMethodDir2.exists() || inputMethodDir2.mkdirs()) {
            boolean isSetMethodMap = arrayMap != null && methodMap.size() > 0;
            FileOutputStream fos = null;
            AtomicFile subtypesFile2 = getAdditionalSubtypeFile(inputMethodDir2);
            try {
                fos = subtypesFile2.startWrite();
                XmlSerializer out = new FastXmlSerializer();
                out.setOutput(fos, StandardCharsets.UTF_8.name());
                String str = null;
                out.startDocument(null, true);
                out.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
                out.startTag(null, NODE_SUBTYPES);
                for (String imiId : allSubtypes.keySet()) {
                    if (isSetMethodMap) {
                        try {
                            if (!arrayMap.containsKey(imiId)) {
                                Slog.w(TAG, "IME uninstalled or not valid.: " + imiId);
                            }
                        } catch (IOException e2) {
                            e = e2;
                            Slog.w(TAG, "Error writing subtypes", e);
                            if (fos != null) {
                            }
                        }
                    }
                    out.startTag(str, NODE_IMI);
                    out.attribute(str, ATTR_ID, imiId);
                    List<InputMethodSubtype> subtypesList = allSubtypes.get(imiId);
                    int numSubtypes = subtypesList.size();
                    int i = 0;
                    while (i < numSubtypes) {
                        InputMethodSubtype subtype = subtypesList.get(i);
                        out.startTag(null, NODE_SUBTYPE);
                        if (subtype.hasSubtypeId()) {
                            inputMethodDir = inputMethodDir2;
                            try {
                                out.attribute(null, ATTR_IME_SUBTYPE_ID, String.valueOf(subtype.getSubtypeId()));
                            } catch (IOException e3) {
                                e = e3;
                                Slog.w(TAG, "Error writing subtypes", e);
                                if (fos != null) {
                                }
                            }
                        } else {
                            inputMethodDir = inputMethodDir2;
                        }
                        out.attribute(null, ATTR_ICON, String.valueOf(subtype.getIconResId()));
                        out.attribute(null, ATTR_LABEL, String.valueOf(subtype.getNameResId()));
                        out.attribute(null, ATTR_IME_SUBTYPE_LOCALE, subtype.getLocale());
                        out.attribute(null, ATTR_IME_SUBTYPE_LANGUAGE_TAG, subtype.getLanguageTag());
                        out.attribute(null, ATTR_IME_SUBTYPE_MODE, subtype.getMode());
                        out.attribute(null, ATTR_IME_SUBTYPE_EXTRA_VALUE, subtype.getExtraValue());
                        out.attribute(null, ATTR_IS_AUXILIARY, String.valueOf(subtype.isAuxiliary() ? 1 : 0));
                        out.attribute(null, ATTR_IS_ASCII_CAPABLE, String.valueOf(subtype.isAsciiCapable() ? 1 : 0));
                        out.endTag(null, NODE_SUBTYPE);
                        i++;
                        numSubtypes = numSubtypes;
                        inputMethodDir2 = inputMethodDir;
                    }
                    out.endTag(null, NODE_IMI);
                    arrayMap = methodMap;
                    inputMethodDir2 = inputMethodDir2;
                    str = null;
                }
                out.endTag(null, NODE_SUBTYPES);
                out.endDocument();
                subtypesFile2.finishWrite(fos);
            } catch (IOException e4) {
                e = e4;
                Slog.w(TAG, "Error writing subtypes", e);
                if (fos != null) {
                    subtypesFile2.failWrite(fos);
                }
            }
        } else {
            Slog.e(TAG, "Failed to create a parent directory " + inputMethodDir2);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:79:0x01fc, code lost:
        r0 = e;
     */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x01fc A[ExcHandler: IOException | NumberFormatException | XmlPullParserException (e java.lang.Throwable), Splitter:B:76:0x01f8] */
    static void load(ArrayMap<String, List<InputMethodSubtype>> allSubtypes, int userId) {
        Throwable th;
        Throwable th2;
        int type;
        int i;
        int i2;
        String str;
        int depth;
        String firstNodeName;
        int type2;
        AtomicFile subtypesFile;
        String str2;
        int depth2;
        String firstNodeName2;
        int type3;
        AtomicFile subtypesFile2;
        String subtypeIdString = "1";
        allSubtypes.clear();
        AtomicFile subtypesFile3 = getAdditionalSubtypeFile(getInputMethodDir(userId));
        if (subtypesFile3.exists()) {
            try {
                FileInputStream fis = subtypesFile3.openRead();
                try {
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(fis, StandardCharsets.UTF_8.name());
                    parser.getEventType();
                    do {
                        type = parser.next();
                        i = 1;
                        i2 = 2;
                        if (type == 2) {
                            break;
                        }
                    } while (type != 1);
                    String firstNodeName3 = parser.getName();
                    if (NODE_SUBTYPES.equals(firstNodeName3)) {
                        int depth3 = parser.getDepth();
                        String currentImiId = null;
                        String str3 = null;
                        ArrayList<InputMethodSubtype> tempSubtypesArray = null;
                        while (true) {
                            int type4 = parser.next();
                            if (type4 == 3) {
                                try {
                                    if (parser.getDepth() <= depth3) {
                                        break;
                                    }
                                } catch (Throwable th3) {
                                    th = th3;
                                    try {
                                        throw th;
                                    } catch (IOException | NumberFormatException | XmlPullParserException e) {
                                    } catch (Throwable th4) {
                                        th.addSuppressed(th4);
                                    }
                                }
                            }
                            if (type4 == i) {
                                break;
                            }
                            if (type4 != i2) {
                                str = subtypeIdString;
                                subtypesFile = subtypesFile3;
                                type2 = type4;
                                firstNodeName = firstNodeName3;
                                depth = depth3;
                            } else {
                                String nodeName = parser.getName();
                                if (NODE_IMI.equals(nodeName)) {
                                    currentImiId = parser.getAttributeValue(str3, ATTR_ID);
                                    if (TextUtils.isEmpty(currentImiId)) {
                                        Slog.w(TAG, "Invalid imi id found in subtypes.xml");
                                    } else {
                                        tempSubtypesArray = new ArrayList<>();
                                        try {
                                            allSubtypes.put(currentImiId, tempSubtypesArray);
                                            str2 = subtypeIdString;
                                            subtypesFile2 = subtypesFile3;
                                            type3 = type4;
                                            firstNodeName2 = firstNodeName3;
                                            depth2 = depth3;
                                        } catch (Throwable th5) {
                                            th = th5;
                                            throw th;
                                        }
                                    }
                                } else {
                                    try {
                                        if (NODE_SUBTYPE.equals(nodeName)) {
                                            if (TextUtils.isEmpty(currentImiId)) {
                                                str = subtypeIdString;
                                                subtypesFile = subtypesFile3;
                                                type2 = type4;
                                                firstNodeName = firstNodeName3;
                                                depth = depth3;
                                            } else if (tempSubtypesArray == null) {
                                                str = subtypeIdString;
                                                subtypesFile = subtypesFile3;
                                                type2 = type4;
                                                firstNodeName = firstNodeName3;
                                                depth = depth3;
                                            } else {
                                                int icon = Integer.parseInt(parser.getAttributeValue(str3, ATTR_ICON));
                                                int label = Integer.parseInt(parser.getAttributeValue(str3, ATTR_LABEL));
                                                String imeSubtypeLocale = parser.getAttributeValue(str3, ATTR_IME_SUBTYPE_LOCALE);
                                                subtypesFile2 = subtypesFile3;
                                                try {
                                                    String languageTag = parser.getAttributeValue(null, ATTR_IME_SUBTYPE_LANGUAGE_TAG);
                                                    type3 = type4;
                                                    String imeSubtypeMode = parser.getAttributeValue(null, ATTR_IME_SUBTYPE_MODE);
                                                    firstNodeName2 = firstNodeName3;
                                                    String imeSubtypeExtraValue = parser.getAttributeValue(null, ATTR_IME_SUBTYPE_EXTRA_VALUE);
                                                    depth2 = depth3;
                                                    InputMethodSubtype.InputMethodSubtypeBuilder builder = new InputMethodSubtype.InputMethodSubtypeBuilder().setSubtypeNameResId(label).setSubtypeIconResId(icon).setSubtypeLocale(imeSubtypeLocale).setLanguageTag(languageTag).setSubtypeMode(imeSubtypeMode).setSubtypeExtraValue(imeSubtypeExtraValue).setIsAuxiliary(subtypeIdString.equals(String.valueOf(parser.getAttributeValue(null, ATTR_IS_AUXILIARY)))).setIsAsciiCapable(subtypeIdString.equals(String.valueOf(parser.getAttributeValue(null, ATTR_IS_ASCII_CAPABLE))));
                                                    str2 = subtypeIdString;
                                                    String subtypeIdString2 = parser.getAttributeValue(null, ATTR_IME_SUBTYPE_ID);
                                                    if (subtypeIdString2 != null) {
                                                        builder.setSubtypeId(Integer.parseInt(subtypeIdString2));
                                                    }
                                                    tempSubtypesArray.add(builder.build());
                                                } catch (Throwable th6) {
                                                    th = th6;
                                                    throw th;
                                                }
                                            }
                                            Slog.w(TAG, "IME uninstalled or not valid.: " + currentImiId);
                                        } else {
                                            str2 = subtypeIdString;
                                            subtypesFile2 = subtypesFile3;
                                            type3 = type4;
                                            firstNodeName2 = firstNodeName3;
                                            depth2 = depth3;
                                        }
                                    } catch (Throwable th7) {
                                        th2 = th7;
                                        th = th2;
                                        throw th;
                                    }
                                }
                                subtypesFile3 = subtypesFile2;
                                firstNodeName3 = firstNodeName2;
                                depth3 = depth2;
                                subtypeIdString = str2;
                                i = 1;
                                i2 = 2;
                                str3 = null;
                            }
                            subtypesFile3 = subtypesFile;
                            firstNodeName3 = firstNodeName;
                            depth3 = depth;
                            subtypeIdString = str;
                            i = 1;
                            i2 = 2;
                            str3 = null;
                        }
                        if (fis != null) {
                            fis.close();
                            return;
                        }
                        return;
                    }
                    throw new XmlPullParserException("Xml doesn't start with subtypes");
                } catch (Throwable th8) {
                    th2 = th8;
                    th = th2;
                    throw th;
                }
            } catch (IOException | NumberFormatException | XmlPullParserException e2) {
                Exception e3 = e2;
                Slog.w(TAG, "Error reading subtypes", e3);
                return;
            }
        } else {
            return;
        }
        throw th;
    }
}
