package org.apache.xml.serializer.utils;

import java.io.File;
import javax.xml.transform.TransformerException;
import org.apache.xml.serializer.utils.URI.MalformedURIException;
import org.apache.xpath.compiler.PsuedoNames;

public final class SystemIDResolver {
    public static String getAbsoluteURIFromRelative(String localPath) {
        if (localPath == null || localPath.length() == 0) {
            return "";
        }
        String urlString;
        String absolutePath = localPath;
        if (!isAbsolutePath(localPath)) {
            try {
                absolutePath = getAbsolutePathFromRelativePath(localPath);
            } catch (SecurityException e) {
                return "file:" + localPath;
            }
        }
        if (absolutePath == null) {
            urlString = "file:" + localPath;
        } else if (absolutePath.startsWith(File.separator)) {
            urlString = "file://" + absolutePath;
        } else {
            urlString = "file:///" + absolutePath;
        }
        return replaceChars(urlString);
    }

    private static String getAbsolutePathFromRelativePath(String relativePath) {
        return new File(relativePath).getAbsolutePath();
    }

    public static boolean isAbsoluteURI(String systemId) {
        boolean z = false;
        if (isWindowsAbsolutePath(systemId)) {
            return false;
        }
        int fragmentIndex = systemId.indexOf(35);
        int queryIndex = systemId.indexOf(63);
        int slashIndex = systemId.indexOf(47);
        int colonIndex = systemId.indexOf(58);
        int index = systemId.length() - 1;
        if (fragmentIndex > 0) {
            index = fragmentIndex;
        }
        if (queryIndex > 0 && queryIndex < index) {
            index = queryIndex;
        }
        if (slashIndex > 0 && slashIndex < index) {
            index = slashIndex;
        }
        if (colonIndex > 0 && colonIndex < index) {
            z = true;
        }
        return z;
    }

    public static boolean isAbsolutePath(String systemId) {
        if (systemId == null) {
            return false;
        }
        return new File(systemId).isAbsolute();
    }

    private static boolean isWindowsAbsolutePath(String systemId) {
        return isAbsolutePath(systemId) && systemId.length() > 2 && systemId.charAt(1) == ':' && Character.isLetter(systemId.charAt(0)) && (systemId.charAt(2) == '\\' || systemId.charAt(2) == '/');
    }

    private static String replaceChars(String str) {
        StringBuffer buf = new StringBuffer(str);
        int length = buf.length();
        int i = 0;
        while (i < length) {
            char currentChar = buf.charAt(i);
            if (currentChar == ' ') {
                buf.setCharAt(i, '%');
                buf.insert(i + 1, "20");
                length += 2;
                i += 2;
            } else if (currentChar == '\\') {
                buf.setCharAt(i, '/');
            }
            i++;
        }
        return buf.toString();
    }

    public static String getAbsoluteURI(String systemId) {
        String absoluteURI = systemId;
        if (!isAbsoluteURI(systemId)) {
            return getAbsoluteURIFromRelative(systemId);
        }
        if (!systemId.startsWith("file:")) {
            return systemId;
        }
        String str = systemId.substring(5);
        if (str == null || !str.startsWith(PsuedoNames.PSEUDONAME_ROOT)) {
            return getAbsoluteURIFromRelative(systemId.substring(5));
        }
        if (str.startsWith("///") || (str.startsWith("//") ^ 1) != 0) {
            int secondColonIndex = systemId.indexOf(58, 5);
            if (secondColonIndex > 0) {
                String localPath = systemId.substring(secondColonIndex - 1);
                try {
                    if (!isAbsolutePath(localPath)) {
                        absoluteURI = systemId.substring(0, secondColonIndex - 1) + getAbsolutePathFromRelativePath(localPath);
                    }
                } catch (SecurityException e) {
                    return systemId;
                }
            }
        }
        return replaceChars(absoluteURI);
    }

    public static String getAbsoluteURI(String urlString, String base) throws TransformerException {
        if (base == null) {
            return getAbsoluteURI(urlString);
        }
        try {
            return replaceChars(new URI(new URI(getAbsoluteURI(base)), urlString).toString());
        } catch (MalformedURIException mue) {
            throw new TransformerException(mue);
        }
    }
}
