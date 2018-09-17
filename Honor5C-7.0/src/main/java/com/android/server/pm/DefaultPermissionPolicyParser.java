package com.android.server.pm;

import android.content.Context;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.server.pm.DefaultAppPermission.DefaultPermissionGroup;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.huawei.android.util.NoExtAPIException;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class DefaultPermissionPolicyParser {
    private static final String CONFIG_ATTR_FIXED = "systemFixed";
    private static final String CONFIG_ATTR_GRANT = "grant";
    private static final String CONFIG_ATTR_NAME = "name";
    private static final String CONFIG_ATTR_TRUST = "trust";
    private static final String CONFIG_TAG_PERM_GROUP = "PermissionGroup";
    private static final String CONFIG_TAG_PKG = "Package";
    private static final String CONFIG_TAG_POLICY = "DefaultPermissionPolicy";
    private static final String FILE_NAME = "permission_grant_policy.xml";
    private static final String FILE_NAME_OVERSEA = "permission_grant_policy_oversea.xml";
    private static final String FILE_NAME_THIRDPARTY = "permission_grant_policy_thirdparty.xml";

    public static Map<String, DefaultAppPermission> parseConfig(Context context) {
        HashMap<String, DefaultAppPermission> map = new HashMap();
        getValueFromXml(map, context, null);
        getValueFromXml(map, context, FILE_NAME_THIRDPARTY);
        return map;
    }

    public static Map<String, DefaultAppPermission> parseCustConfig(Context context) {
        HashMap<String, DefaultAppPermission> map = new HashMap();
        getValueFromXml(map, context, FILE_NAME_THIRDPARTY);
        return map;
    }

    private static String getFileName() {
        if ("factory".equals(SystemProperties.get("ro.runmode", "normal"))) {
            return FILE_NAME;
        }
        return isGlobalVersion() ? FILE_NAME_OVERSEA : FILE_NAME;
    }

    private static boolean isGlobalVersion() {
        if ("zh".equals(SystemProperties.get("ro.product.locale.language")) && "CN".equals(SystemProperties.get("ro.product.locale.region"))) {
            return false;
        }
        return true;
    }

    private static void getValueFromXml(HashMap<String, DefaultAppPermission> configMap, Context context, String thirdpartyFile) {
        InputStream inputStream = null;
        if (thirdpartyFile == null) {
            try {
                String fileName = getFileName();
                Slog.i(AppHibernateCst.INVALID_PKG, "Default permission policy file:" + fileName);
                inputStream = context.getAssets().open(fileName);
            } catch (NullPointerException e) {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e2) {
                    }
                }
            } catch (NumberFormatException e3) {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e4) {
                    }
                }
            } catch (IOException e5) {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e6) {
                    }
                }
            } catch (IndexOutOfBoundsException e7) {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e8) {
                    }
                }
            } catch (NoExtAPIException e9) {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e10) {
                    }
                }
            } catch (NoClassDefFoundError e11) {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e12) {
                    }
                }
            } catch (Exception e13) {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e14) {
                    }
                }
            } catch (Throwable th) {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e15) {
                    }
                }
            }
        } else {
            File file = HwCfgFilePolicy.getCfgFile(String.format("/xml/%s", new Object[]{thirdpartyFile}), 0);
            if (file == null) {
                Slog.i(AppHibernateCst.INVALID_PKG, "permission_grant_policy_thirdparty not exist");
                return;
            }
            inputStream = new FileInputStream(file);
        }
        parseRootElement(configMap, inputStream);
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e16) {
            }
        }
    }

    private static void parseRootElement(HashMap<String, DefaultAppPermission> configMap, InputStream is) throws IOException, ParserConfigurationException, SAXException {
        try {
            NodeList policies = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is).getChildNodes();
            for (int i = 0; i < policies.getLength(); i++) {
                Node policy = policies.item(i);
                if (CONFIG_TAG_POLICY.equals(policy.getNodeName())) {
                    parsePolicyElement(policy, configMap);
                }
            }
        } catch (ParserConfigurationException e) {
        } catch (SAXException e2) {
        } catch (IOException e3) {
        }
    }

    private static void parsePolicyElement(Node policy, HashMap<String, DefaultAppPermission> configMap) {
        NodeList packages = policy.getChildNodes();
        for (int i = 0; i < packages.getLength(); i++) {
            Node pkg = packages.item(i);
            if (CONFIG_TAG_PKG.endsWith(pkg.getNodeName())) {
                NamedNodeMap attrs = pkg.getAttributes();
                String pkgName = getAttrs(attrs, CONFIG_ATTR_NAME);
                if (pkgName != null) {
                    DefaultAppPermission appPermission = new DefaultAppPermission();
                    appPermission.mPackageName = pkgName;
                    String trust = getAttrs(attrs, CONFIG_ATTR_TRUST);
                    if (trust == null || !"true".equals(trust)) {
                        appPermission.mTrust = false;
                        appPermission.mGrantedGroups = parsePackageElement(pkg, configMap);
                    } else {
                        appPermission.mTrust = true;
                    }
                    configMap.put(pkgName, appPermission);
                }
            }
        }
    }

    private static String getAttrs(NamedNodeMap attrMap, String attrName) {
        Node res = attrMap.getNamedItem(attrName);
        if (res != null) {
            return res.getNodeValue();
        }
        return null;
    }

    private static ArrayList<DefaultPermissionGroup> parsePackageElement(Node pkg, HashMap<String, DefaultAppPermission> hashMap) {
        ArrayList<DefaultPermissionGroup> groupsList = new ArrayList();
        NodeList permissGroups = pkg.getChildNodes();
        for (int i = 0; i < permissGroups.getLength(); i++) {
            Node group = permissGroups.item(i);
            if (CONFIG_TAG_PERM_GROUP.equals(group.getNodeName())) {
                NamedNodeMap attrs = group.getAttributes();
                String permissionName = getAttrs(attrs, CONFIG_ATTR_NAME);
                if (permissionName != null) {
                    boolean z;
                    String grant = getAttrs(attrs, CONFIG_ATTR_GRANT);
                    String fixed = getAttrs(attrs, CONFIG_ATTR_FIXED);
                    if ("false".equals(grant)) {
                        z = false;
                    } else {
                        z = true;
                    }
                    groupsList.add(new DefaultPermissionGroup(permissionName, z, !"false".equals(fixed)));
                }
            }
        }
        return groupsList;
    }
}
