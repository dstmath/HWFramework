package com.huawei.i18n.taboo;

import android.util.Log;
import com.huawei.android.os.storage.StorageManagerExt;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Scanner;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ParseXml {
    private static final String CONFIG_NAME = "taboo-config.xml";
    private static final String DATA_NAME = "/taboo-data.xml";
    private static final String EN_NAME = "/xml/taboo-data.xml";
    private static final String FILE_SEP = "/";
    private static final String ITEM = "item";
    private static final int LIMITED_LENGTH = 4;
    private static final int MAP_SIZE = 10;
    private static final String NAME = "name";
    private static final String PARSE_XML_TAG = "parseXml";
    private static final int REGION_LENGTH = 2;
    private static final int TXTSECTION = 2;
    private static final String VALUE = "value";
    private static final String VERSION_NAME = "version.txt";
    private static final String VERSION_TAG = "version";
    private static final String XML_SEP = "/xml-";

    public static HashMap<String, String> parse(String path, String locale) {
        HashMap<String, String> dataHashMap = new HashMap<>(10);
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            setXmlEntityPolicy(factory);
            DocumentBuilder builder = factory.newDocumentBuilder();
            File file = getFile(path, locale);
            if (file == null || !file.exists()) {
                return dataHashMap;
            }
            getDocument(dataHashMap, builder, file);
            return dataHashMap;
        } catch (IOException | ParserConfigurationException | SAXException e) {
            Log.e(PARSE_XML_TAG, "parse xml exception");
        }
    }

    private static void setXmlEntityPolicy(DocumentBuilderFactory builderFactory) {
        if (builderFactory == null) {
            Log.e(PARSE_XML_TAG, "setXmlEntityPolicy failed,builderFactory is null.");
            return;
        }
        setXmlSecurityFeature(builderFactory, "http://apache.org/xml/features/disallow-doctype-decl", true);
        setXmlSecurityFeature(builderFactory, "http://xml.org/sax/features/external-general-entities", false);
        setXmlSecurityFeature(builderFactory, "http://xml.org/sax/features/external-parameter-entities", false);
        setXmlSecurityFeature(builderFactory, "http://javax.xml.XMLConstants/feature/secure-processing", true);
        builderFactory.setExpandEntityReferences(false);
    }

    private static void setXmlSecurityFeature(DocumentBuilderFactory builderFactory, String featureName, boolean value) {
        if (builderFactory == null || featureName == null) {
            Log.e(PARSE_XML_TAG, "setXmlSecurityFeature failed, builderFactory = " + builderFactory + ", featureName = " + featureName);
            return;
        }
        try {
            builderFactory.setFeature(featureName, value);
        } catch (ParserConfigurationException e) {
            Log.e(PARSE_XML_TAG, "setXmlSecurityFeature occur ParserConfigurationException: set " + featureName + " to " + value + " failed.");
        }
    }

    public static ArrayList<String> getXmlLanguageList(String path) {
        ArrayList<String> languageList = new ArrayList<>();
        File[] fileArray = new File(path).listFiles();
        if (fileArray != null) {
            for (File fi : fileArray) {
                if (fi.isDirectory()) {
                    languageList.add(getLanguageTag(fi.getName()));
                }
            }
        }
        return languageList;
    }

    private static String getLanguageTag(String fileName) {
        int indexOf = fileName.indexOf("-");
        if (indexOf == -1) {
            return "en";
        }
        String substring = fileName.substring(indexOf + 1);
        if (substring.contains("+")) {
            return substring.replace("b+", StorageManagerExt.INVALID_KEY_DESC).replace("+", "-");
        }
        if (substring.contains("-")) {
            return substring.replace("-r", "-");
        }
        return substring;
    }

    private static File getFile(String path, String targetLocaleID) {
        String filePath;
        Locale targetLocale = Locale.forLanguageTag(targetLocaleID);
        String language = targetLocale.getLanguage();
        String script = targetLocale.getScript();
        String country = targetLocale.getCountry();
        String replaceLocaleID = targetLocaleID.replaceAll("-", "+");
        if (language.isEmpty()) {
            return null;
        }
        if (!script.isEmpty() || country.length() > 2) {
            filePath = path + XML_SEP + "b+" + replaceLocaleID + DATA_NAME;
        } else if (!script.isEmpty() || !country.isEmpty() || !"en".equals(language)) {
            String filePath2 = path + XML_SEP + language;
            if (!country.isEmpty()) {
                filePath2 = filePath2 + "-r" + country;
            }
            filePath = filePath2 + DATA_NAME;
        } else {
            filePath = path + EN_NAME;
        }
        return new File(filePath);
    }

    public static HashMap<String, String> parseConfigXml(String path) {
        HashMap<String, String> configHashMap = new HashMap<>(10);
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            setXmlEntityPolicy(factory);
            DocumentBuilder builder = factory.newDocumentBuilder();
            File file = new File(path + FILE_SEP + CONFIG_NAME);
            if (!file.exists()) {
                return configHashMap;
            }
            getDocument(configHashMap, builder, file);
            return configHashMap;
        } catch (IOException | ParserConfigurationException | SAXException e) {
            Log.e(PARSE_XML_TAG, "parse config xml exception");
        }
    }

    private static void getDocument(HashMap<String, String> hashMap, DocumentBuilder builder, File file) throws SAXException, IOException {
        NodeList item;
        Document doc = builder.parse(file);
        if (!(doc == null || (item = doc.getElementsByTagName(ITEM)) == null)) {
            int len = item.getLength();
            for (int i = 0; i < len; i++) {
                NamedNodeMap map = item.item(i).getAttributes();
                hashMap.put(map.getNamedItem(NAME).getNodeValue(), map.getNamedItem(VALUE).getNodeValue());
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0042, code lost:
        if (r3 == null) goto L_0x004c;
     */
    public static long getVersion(String path) {
        File file = new File(path + FILE_SEP + VERSION_NAME);
        long version = 0;
        if (file.exists()) {
            Scanner scanner = null;
            try {
                scanner = new Scanner(file, "UTF-8");
                version = computeVersion(scanner);
            } catch (FileNotFoundException e) {
                Log.e(PARSE_XML_TAG, "version file not found");
            } catch (Throwable th) {
                if (scanner != null) {
                    scanner.close();
                }
                throw th;
            }
            scanner.close();
        }
        return version;
    }

    private static long computeVersion(Scanner scanner) {
        long version = 0;
        while (true) {
            if (!scanner.hasNextLine()) {
                break;
            }
            String[] content = scanner.nextLine().split("=");
            String[] versionInfo = content[1].split("\\.");
            if (VERSION_TAG.equals(content[0]) && isVersionFourSegments(versionInfo)) {
                for (String str : versionInfo) {
                    version = Long.parseLong(str) + (1000 * version);
                }
            }
        }
        return version;
    }

    private static boolean isVersionFourSegments(String[] versionInfo) {
        if (versionInfo.length != 4 || !isNumFormat(versionInfo)) {
            return false;
        }
        return true;
    }

    private static boolean isNumFormat(String[] version) {
        Pattern pattern = Pattern.compile("[0-9]{1,3}$");
        for (String ver : version) {
            if (!pattern.matcher(ver).matches()) {
                return false;
            }
        }
        return true;
    }

    public static long getFileLastModify(String path) {
        File file = new File(path + FILE_SEP + VERSION_NAME);
        if (file.exists()) {
            return file.lastModified();
        }
        return 0;
    }
}
