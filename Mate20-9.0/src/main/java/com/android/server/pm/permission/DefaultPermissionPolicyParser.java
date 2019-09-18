package com.android.server.pm.permission;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.server.pm.permission.DefaultAppPermission;
import com.android.server.wifipro.WifiProCommonUtils;
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
    private static final String CONFIG_TAG_PERM_SINGLE = "Permission";
    private static final String CONFIG_TAG_PKG = "Package";
    private static final String CONFIG_TAG_POLICY = "DefaultPermissionPolicy";
    private static final String FILE_NAME = "permission_grant_policy.xml";
    private static final String FILE_NAME_OVERSEA = "permission_grant_policy_oversea.xml";
    private static final String FILE_NAME_THIRDPARTY = "permission_grant_policy_thirdparty.xml";
    private static final String FILE_NAME_THIRDPARTY_COTA_NOREBOOT = "/data/cota/live_update/work/xml/permission_grant_policy_thirdparty.xml";
    public static final boolean IS_ATT = (SystemProperties.get("ro.config.hw_opta", "0").equals("07") && SystemProperties.get("ro.config.hw_optb", "0").equals("840"));
    public static final boolean IS_SINGLE_PERMISSION = SystemProperties.getBoolean("ro.config.single_permission", false);
    private static final String TAG = "DefaultPermissionPolicyParser";

    public static Map<String, DefaultAppPermission> parseConfig(Context context) {
        HashMap<String, DefaultAppPermission> map = new HashMap<>();
        getValueFromXml(map, context, null);
        getValueFromXml(map, context, FILE_NAME_THIRDPARTY);
        return map;
    }

    public static Map<String, DefaultAppPermission> parseCustConfig(Context context) {
        HashMap<String, DefaultAppPermission> map = new HashMap<>();
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
        return !"zh".equals(SystemProperties.get("ro.product.locale.language")) || !"CN".equals(SystemProperties.get(WifiProCommonUtils.KEY_PROP_LOCALE));
    }

    private static void getValueFromXml(HashMap<String, DefaultAppPermission> configMap, Context context, String thirdpartyFile) {
        InputStream fileInputStream;
        File file;
        InputStream fileInputStream2 = null;
        if (thirdpartyFile == null) {
            try {
                String fileName = getFileName();
                Slog.i("", "Default permission policy file:" + fileName);
                fileInputStream = context.getAssets().open(fileName);
            } catch (NullPointerException e) {
                Slog.e(TAG, "parseRootElement NullPointerException");
                if (fileInputStream2 != null) {
                    fileInputStream2.close();
                }
            } catch (NumberFormatException e2) {
                Slog.e(TAG, "parseRootElement NumberFormatException");
                if (fileInputStream2 != null) {
                    fileInputStream2.close();
                }
            } catch (IOException e3) {
                Slog.e(TAG, "parseRootElement IOException");
                if (fileInputStream2 != null) {
                    fileInputStream2.close();
                }
            } catch (IndexOutOfBoundsException e4) {
                Slog.e(TAG, "parseRootElement IndexOutOfBoundsException");
                if (fileInputStream2 != null) {
                    fileInputStream2.close();
                }
            } catch (NoExtAPIException e5) {
                Slog.e(TAG, "parseRootElement NoExtAPIException");
                if (fileInputStream2 != null) {
                    fileInputStream2.close();
                }
            } catch (NoClassDefFoundError e6) {
                Slog.e(TAG, "parseRootElement NoClassDefFoundError");
                if (fileInputStream2 != null) {
                    fileInputStream2.close();
                }
            } catch (Exception e7) {
                Slog.e(TAG, "parseRootElement other Exception ");
                if (fileInputStream2 != null) {
                    fileInputStream2.close();
                }
            } catch (Throwable th) {
                if (fileInputStream2 != null) {
                    try {
                        fileInputStream2.close();
                    } catch (IOException e8) {
                        Slog.e(TAG, "parseRootElement IOException in finally");
                    }
                }
                throw th;
            }
        } else {
            String xmlPath = String.format("/xml/%s", new Object[]{thirdpartyFile});
            File cotaFileTemp = new File(FILE_NAME_THIRDPARTY_COTA_NOREBOOT);
            if (cotaFileTemp.exists()) {
                file = cotaFileTemp;
            } else {
                file = HwCfgFilePolicy.getCfgFile(xmlPath, 0);
            }
            Slog.i(TAG, "permission_grant_policy_thirdparty.xml is = " + file);
            if (file == null) {
                Slog.i("", "permission_grant_policy_thirdparty not exist");
                if (fileInputStream2 != null) {
                    try {
                        fileInputStream2.close();
                    } catch (IOException e9) {
                        Slog.e(TAG, "parseRootElement IOException in finally");
                    }
                }
                return;
            }
            fileInputStream = new FileInputStream(file);
        }
        parseRootElement(configMap, fileInputStream);
        if (fileInputStream != null) {
            try {
                fileInputStream.close();
            } catch (IOException e10) {
                Slog.e(TAG, "parseRootElement IOException in finally");
            }
        }
    }

    @SuppressLint({"AvoidMethodInForLoop"})
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
            Slog.e(TAG, "parseRootElement ParserConfigurationException");
        } catch (SAXException e2) {
            Slog.e(TAG, "parseRootElement.SAXException");
        } catch (IOException e3) {
            Slog.e(TAG, "parseRootElement.IOException");
        }
    }

    @SuppressLint({"AvoidMethodInForLoop"})
    private static void parsePolicyElement(Node policy, HashMap<String, DefaultAppPermission> configMap) {
        NodeList packages = policy.getChildNodes();
        for (int i = 0; i < packages.getLength(); i++) {
            Node pkg = packages.item(i);
            if ("Package".endsWith(pkg.getNodeName())) {
                NamedNodeMap attrs = pkg.getAttributes();
                String pkgName = getAttrs(attrs, "name");
                if (pkgName != null) {
                    DefaultAppPermission appPermission = new DefaultAppPermission();
                    appPermission.mPackageName = pkgName;
                    String trust = getAttrs(attrs, CONFIG_ATTR_TRUST);
                    if (!IS_ATT && !IS_SINGLE_PERMISSION) {
                        if (trust == null || !"true".equals(trust)) {
                            appPermission.mTrust = false;
                            appPermission.mGrantedGroups = parsePackageElement(pkg, configMap);
                        } else {
                            appPermission.mTrust = true;
                        }
                        configMap.put(pkgName, appPermission);
                    } else if (trust == null || !"true".equals(trust)) {
                        appPermission.mTrust = false;
                        parsePackageElement(pkg, configMap, appPermission);
                    } else {
                        appPermission.mTrust = true;
                        configMap.put(pkgName, appPermission);
                    }
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

    @SuppressLint({"AvoidMethodInForLoop"})
    private static ArrayList<DefaultAppPermission.DefaultPermissionGroup> parsePackageElement(Node pkg, HashMap<String, DefaultAppPermission> hashMap) {
        ArrayList<DefaultAppPermission.DefaultPermissionGroup> groupsList = new ArrayList<>();
        NodeList permissGroups = pkg.getChildNodes();
        for (int i = 0; i < permissGroups.getLength(); i++) {
            Node group = permissGroups.item(i);
            if (CONFIG_TAG_PERM_GROUP.equals(group.getNodeName())) {
                NamedNodeMap attrs = group.getAttributes();
                String permissionName = getAttrs(attrs, "name");
                if (permissionName != null) {
                    groupsList.add(new DefaultAppPermission.DefaultPermissionGroup(permissionName, !"false".equals(getAttrs(attrs, CONFIG_ATTR_GRANT)), !"false".equals(getAttrs(attrs, CONFIG_ATTR_FIXED))));
                }
            }
        }
        return groupsList;
    }

    @SuppressLint({"AvoidMethodInForLoop"})
    private static void parsePackageElement(Node pkg, HashMap<String, DefaultAppPermission> configMap, DefaultAppPermission appPermission) {
        ArrayList<DefaultAppPermission.DefaultPermissionGroup> groupsList = new ArrayList<>();
        ArrayList<DefaultAppPermission.DefaultPermissionSingle> singlesList = new ArrayList<>();
        NodeList permissGroups = pkg.getChildNodes();
        for (int i = 0; i < permissGroups.getLength(); i++) {
            Node group = permissGroups.item(i);
            if (CONFIG_TAG_PERM_GROUP.equals(group.getNodeName()) || CONFIG_TAG_PERM_SINGLE.equals(group.getNodeName())) {
                NamedNodeMap attrs = group.getAttributes();
                String permissionName = getAttrs(attrs, "name");
                if (permissionName != null) {
                    String grant = getAttrs(attrs, CONFIG_ATTR_GRANT);
                    String fixed = getAttrs(attrs, CONFIG_ATTR_FIXED);
                    if (CONFIG_TAG_PERM_GROUP.equals(group.getNodeName())) {
                        groupsList.add(new DefaultAppPermission.DefaultPermissionGroup(permissionName, !"false".equals(grant), !"false".equals(fixed)));
                    } else if (CONFIG_TAG_PERM_SINGLE.equals(group.getNodeName())) {
                        singlesList.add(new DefaultAppPermission.DefaultPermissionSingle(permissionName, !"false".equals(grant), !"false".equals(fixed)));
                    } else {
                        Slog.e(TAG, "group.getNodeName() is not equal PermissionGroup and Permission");
                    }
                }
            }
        }
        appPermission.mGrantedGroups = groupsList;
        appPermission.mGrantedSingles = singlesList;
        configMap.put(appPermission.mPackageName, appPermission);
    }
}
