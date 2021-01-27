package ohos.data.jsfile;

import com.huawei.ace.plugin.Function;
import com.huawei.ace.plugin.ModuleGroup;
import com.huawei.ace.plugin.Result;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import ohos.ace.ability.AceAbility;
import ohos.ai.asr.util.AsrConstants;
import ohos.app.Context;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaSymbols;
import ohos.com.sun.org.apache.xpath.internal.compiler.Keywords;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.fastjson.JSONObject;

public class FilePlugin implements ModuleGroup.ModuleGroupHandler {
    private static final String APP_CACHE_DIRECTORY = "internal://cache/";
    private static final String APP_FILE_DIRECTORY = "internal://app/";
    private static final String APP_SHARE_DIRECTORY = "internal://share/";
    private static final HiLogLabel LABEL_LOG = new HiLogLabel(3, 218109441, "JsFileKit");
    private static final String MODULE_GROUP_NAME = "AceModuleGroup/File";
    private static final String TAG = "FilePlugin";
    private static Map<Integer, FilePlugin> instanceMap = new ConcurrentHashMap();
    private Context applicationContext;
    private ModuleGroup moduleGroup;

    public static String getJsCode() {
        return "var file ={ \nfileModuleGroup : null,\nonInit : function onInit(){\n    if (file.fileModuleGroup == null) {\n        file.fileModuleGroup = ModuleGroup.getGroup(\"AceModuleGroup/File\");\n    }\n},\nmkdir : async function mkdir(param) {\n    file.onInit();\n    return await catching(file.fileModuleGroup.callNative(\"mkdir\",param),param);\n},\nrmdir : async function rmdir(param) {\n    file.onInit();\n    return await catching(file.fileModuleGroup.callNative(\"rmdir\",param),param);\n},\nget : async function get(param) {\n    file.onInit();\n    return await catching(file.fileModuleGroup.callNative(\"get\",param),param);\n},\nlist : async function list(param) {\n    file.onInit();\n    return await catching(file.fileModuleGroup.callNative(\"list\",param),param);\n},\ncopy : async function copy(param) {\n    file.onInit();\n    return await catching(file.fileModuleGroup.callNative(\"copy\",param),param);\n},\nmove : async function move(param) {\n    file.onInit();\n    return await catching(file.fileModuleGroup.callNative(\"move\",param),param);\n},\ndeleteFile : async function deleteFile(param) {\n    file.onInit();\n    return await catching(file.fileModuleGroup.callNative(\"delete\",param),param);\n},\naccess : async function access(param) {\n    file.onInit();\n    return await catching(file.fileModuleGroup.callNative(\"access\",param),param);\n},\nwriteText : async function writeText(param) {\n    file.onInit();\n    return await catching(file.fileModuleGroup.callNative(\"writeText\",param),param);\n},\nwriteArrayBuffer : async function writeArrayBuffer(param) {\n    file.onInit();\n    return await catching(file.fileModuleGroup.callNative(\"writeArrayBuffer\",param),param);\n},\nreadText : async function readText(param) {\n    file.onInit();\n    return await catching(file.fileModuleGroup.callNative(\"readText\",param),param);\n},\nreadArrayBuffer : async function readArrayBuffer(param) {\n    file.onInit();\n    return await catching(file.fileModuleGroup.callNative(\"readArrayBuffer\",param),param);\n}\n};\nglobal.systemplugin.file = {\n    get: file.get,\n    mkdir: file.mkdir,\n    rmdir: file.rmdir,\n    list: file.list,\n    copy: file.copy,\n    move: file.move,\n    delete: file.deleteFile,\n    access: file.access,\n    writeText: file.writeText,\n    writeArrayBuffer: file.writeArrayBuffer,\n    readText: file.readText,\n    readArrayBuffer: file.readArrayBuffer,\n};\n";
    }

    private FilePlugin(Context context) {
        this.applicationContext = context;
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0058, code lost:
        if (r1.equals("move") != false) goto L_0x00b1;
     */
    @Override // com.huawei.ace.plugin.ModuleGroup.ModuleGroupHandler
    public void onFunctionCall(Function function, Result result) {
        char c = 0;
        if (result == null) {
            HiLog.error(LABEL_LOG, "parameter result is null when onFunctionCall", new Object[0]);
        } else if (function == null) {
            HiLog.error(LABEL_LOG, "parameter call is null when onFunctionCall", new Object[0]);
            result.error(202, "call is null");
        } else {
            String str = function.name;
            switch (str.hashCode()) {
                case -1423461020:
                    if (str.equals("access")) {
                        c = '\t';
                        break;
                    }
                    c = 65535;
                    break;
                case -1406334548:
                    if (str.equals("writeText")) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
                case -1335458389:
                    if (str.equals("delete")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case -867543069:
                    if (str.equals("readText")) {
                        c = 7;
                        break;
                    }
                    c = 65535;
                    break;
                case -219963261:
                    if (str.equals("readArrayBuffer")) {
                        c = '\b';
                        break;
                    }
                    c = 65535;
                    break;
                case 102230:
                    if (str.equals("get")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case 3059573:
                    if (str.equals(Constants.ELEMNAME_COPY_STRING)) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case 3322014:
                    if (str.equals(SchemaSymbols.ATTVAL_LIST)) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case 3357649:
                    break;
                case 103950895:
                    if (str.equals("mkdir")) {
                        c = '\n';
                        break;
                    }
                    c = 65535;
                    break;
                case 108628082:
                    if (str.equals("rmdir")) {
                        c = 11;
                        break;
                    }
                    c = 65535;
                    break;
                case 278137498:
                    if (str.equals("writeArrayBuffer")) {
                        c = 6;
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
                    move(function, result);
                    return;
                case 1:
                    copy(function, result);
                    return;
                case 2:
                    list(function, result);
                    return;
                case 3:
                    get(function, result);
                    return;
                case 4:
                    delete(function, result);
                    return;
                case 5:
                    writeText(function, result);
                    return;
                case 6:
                    writeArrayBuffer(function, result);
                    return;
                case 7:
                    readText(function, result);
                    return;
                case '\b':
                    readArrayBuffer(function, result);
                    return;
                case '\t':
                    access(function, result);
                    return;
                case '\n':
                    mkdir(function, result);
                    return;
                case 11:
                    rmdir(function, result);
                    return;
                default:
                    result.notExistFunction();
                    return;
            }
        }
    }

    private void move(Function function, Result result) {
        JSONObject parseObject = JSONObject.parseObject((String) function.arguments.get(0));
        if (parseObject == null || !parseObject.containsKey("srcUri") || !parseObject.containsKey("dstUri")) {
            result.error(202, "json arguments illegal");
            return;
        }
        String path = getPath(parseObject.getString("srcUri"), result);
        String path2 = getPath(parseObject.getString("dstUri"), result);
        if (path != null && path2 != null) {
            try {
                if (Files.move(new File(path).toPath(), new File(path2).toPath(), new CopyOption[0]) != null) {
                    result.success(parseObject.getString("dstUri"));
                } else {
                    result.error(300, "move file fail, IO Exception");
                }
            } catch (IOException unused) {
                result.error(300, "move file fail, IO Exception");
            }
        }
    }

    private void copy(Function function, Result result) {
        JSONObject parseObject = JSONObject.parseObject((String) function.arguments.get(0));
        if (parseObject == null || !parseObject.containsKey("srcUri") || !parseObject.containsKey("dstUri")) {
            result.error(202, "json arguments illegal");
            return;
        }
        String path = getPath(parseObject.getString("srcUri"), result);
        String path2 = getPath(parseObject.getString("dstUri"), result);
        if (path != null && path2 != null) {
            try {
                if (Files.copy(new File(path).toPath(), new File(path2).toPath(), new CopyOption[0]) != null) {
                    result.success(parseObject.getString("dstUri"));
                } else {
                    result.error(300, "copy file fail, IO Exception");
                }
            } catch (IOException unused) {
                result.error(300, "copy file fail, IO Exception");
            }
        }
    }

    private void list(Function function, Result result) {
        JSONObject parseObject = JSONObject.parseObject((String) function.arguments.get(0));
        if (parseObject == null || !parseObject.containsKey(Constants.ELEMNAME_URL_STRING)) {
            result.error(202, "json arguments illegal");
            return;
        }
        String path = getPath(parseObject.getString(Constants.ELEMNAME_URL_STRING), result);
        if (path != null) {
            File file = new File(path);
            if (file.exists()) {
                ArrayList arrayList = new ArrayList();
                File[] listFiles = file.listFiles();
                if (listFiles != null) {
                    for (File file2 : listFiles) {
                        arrayList.add(getFileInfo(parseObject.getString(Constants.ELEMNAME_URL_STRING) + File.separator + file2.getName(), file2, false));
                    }
                }
                HashMap hashMap = new HashMap();
                hashMap.put("fileList", arrayList);
                result.success(hashMap);
                return;
            }
            result.error(300, "list file failed");
        }
    }

    private void get(Function function, Result result) {
        HashMap<String, Object> hashMap;
        JSONObject parseObject = JSONObject.parseObject((String) function.arguments.get(0));
        if (parseObject == null || !parseObject.containsKey(Constants.ELEMNAME_URL_STRING)) {
            result.error(202, "json arguments illegal");
            return;
        }
        String path = getPath(parseObject.getString(Constants.ELEMNAME_URL_STRING), result);
        if (path != null) {
            File file = new File(path);
            if (file.exists()) {
                if (parseObject.containsKey("recursive")) {
                    hashMap = getFileInfo(parseObject.getString(Constants.ELEMNAME_URL_STRING), file, parseObject.getBoolean("recursive").booleanValue());
                } else {
                    hashMap = getFileInfo(parseObject.getString(Constants.ELEMNAME_URL_STRING), file, false);
                }
                result.success(hashMap);
                return;
            }
            result.error(300, "get file failed");
        }
    }

    private HashMap<String, Object> getFileInfo(String str, File file, boolean z) {
        File[] listFiles;
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put(Constants.ELEMNAME_URL_STRING, str);
        hashMap.put("lastModifiedTime", Long.valueOf(file.lastModified()));
        hashMap.put("length", Long.valueOf(file.length()));
        if (file.isDirectory()) {
            hashMap.put("type", "dir");
            if (z && (listFiles = file.listFiles()) != null) {
                for (File file2 : listFiles) {
                    hashMap.put("subFiles", getFileInfo(str + file2.getName(), file2, true));
                }
            }
        } else {
            hashMap.put("type", AsrConstants.ASR_SRC_FILE);
        }
        return hashMap;
    }

    private void delete(Function function, Result result) {
        JSONObject parseObject = JSONObject.parseObject((String) function.arguments.get(0));
        if (parseObject == null || !parseObject.containsKey(Constants.ELEMNAME_URL_STRING)) {
            result.error(202, "json arguments illegal");
            return;
        }
        String path = getPath(parseObject.getString(Constants.ELEMNAME_URL_STRING), result);
        if (path != null) {
            if (new File(path).delete()) {
                result.success(null);
            } else {
                result.error(300, "delete file failed");
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0069, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x006a, code lost:
        $closeResource(r4, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x006d, code lost:
        throw r5;
     */
    private void writeText(Function function, Result result) {
        boolean z = false;
        JSONObject parseObject = JSONObject.parseObject((String) function.arguments.get(0));
        if (parseObject == null || !parseObject.containsKey(Constants.ELEMNAME_URL_STRING)) {
            result.error(202, "json arguments illegal");
            return;
        }
        String path = getPath(parseObject.getString(Constants.ELEMNAME_URL_STRING), result);
        if (path != null) {
            File file = new File(path);
            String string = parseObject.containsKey(Constants.ATTRNAME_OUTPUT_ENCODING) ? parseObject.getString(Constants.ATTRNAME_OUTPUT_ENCODING) : "UTF-8";
            if (parseObject.containsKey("append")) {
                z = parseObject.getBoolean("append").booleanValue();
            }
            try {
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(file, z), string);
                outputStreamWriter.write(parseObject.getString("text"));
                outputStreamWriter.flush();
                result.success(null);
                $closeResource(null, outputStreamWriter);
            } catch (IOException unused) {
                result.error(300, "write file failed");
            }
        }
    }

    private static /* synthetic */ void $closeResource(Throwable th, AutoCloseable autoCloseable) {
        if (th != null) {
            try {
                autoCloseable.close();
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
        } else {
            autoCloseable.close();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:32:0x008b, code lost:
        r7 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x008c, code lost:
        $closeResource(r6, r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x008f, code lost:
        throw r7;
     */
    private void writeArrayBuffer(Function function, Result result) {
        JSONObject parseObject = JSONObject.parseObject((String) function.arguments.get(0));
        if (parseObject == null || !parseObject.containsKey(Constants.ELEMNAME_URL_STRING) || !parseObject.containsKey("buffer")) {
            result.error(202, "json arguments illegal");
            return;
        }
        String path = getPath(parseObject.getString(Constants.ELEMNAME_URL_STRING), result);
        if (path != null) {
            File file = new File(path);
            boolean booleanValue = parseObject.containsKey("append") ? parseObject.getBoolean("append").booleanValue() : false;
            String string = parseObject.getString("buffer");
            try {
                RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
                if (!booleanValue) {
                    randomAccessFile.seek((long) (parseObject.containsKey(Keywords.FUNC_POSITION_STRING) ? parseObject.getInteger(Keywords.FUNC_POSITION_STRING).intValue() : 0));
                } else {
                    randomAccessFile.seek(file.length());
                }
                for (int i = 0; i < string.length(); i++) {
                    randomAccessFile.write(string.charAt(i));
                }
                result.success(null);
                $closeResource(null, randomAccessFile);
            } catch (IOException unused) {
                result.error(300, "write file failed");
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0085, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0086, code lost:
        $closeResource(r3, r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0089, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x008c, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x008d, code lost:
        $closeResource(r3, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0090, code lost:
        throw r4;
     */
    private void readText(Function function, Result result) {
        JSONObject parseObject = JSONObject.parseObject((String) function.arguments.get(0));
        if (parseObject == null || !parseObject.containsKey(Constants.ELEMNAME_URL_STRING)) {
            result.error(202, "json arguments illegal");
            return;
        }
        String path = getPath(parseObject.getString(Constants.ELEMNAME_URL_STRING), result);
        if (path != null) {
            File file = new File(path);
            if (!file.exists()) {
                result.error(301, "file not exist");
                return;
            }
            StringBuilder sb = new StringBuilder();
            try {
                InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(file), parseObject.containsKey(Constants.ATTRNAME_OUTPUT_ENCODING) ? parseObject.getString(Constants.ATTRNAME_OUTPUT_ENCODING) : "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                for (String readLine = bufferedReader.readLine(); readLine != null; readLine = bufferedReader.readLine()) {
                    sb.append(readLine);
                }
                HashMap hashMap = new HashMap();
                hashMap.put("text", sb.toString());
                result.success(hashMap);
                $closeResource(null, bufferedReader);
                $closeResource(null, inputStreamReader);
            } catch (IOException unused) {
                result.error(300, "read file failed");
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0098, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0099, code lost:
        $closeResource(r8, r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x009c, code lost:
        throw r0;
     */
    private void readArrayBuffer(Function function, Result result) {
        int i;
        JSONObject parseObject = JSONObject.parseObject((String) function.arguments.get(0));
        if (parseObject == null || !parseObject.containsKey(Constants.ELEMNAME_URL_STRING)) {
            result.error(202, "json arguments illegal");
            return;
        }
        String path = getPath(parseObject.getString(Constants.ELEMNAME_URL_STRING), result);
        if (path != null) {
            File file = new File(path);
            if (!file.exists()) {
                result.error(301, "file not exist");
                return;
            }
            try {
                RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
                if (parseObject.containsKey("length")) {
                    i = parseObject.getInteger("length").intValue();
                } else {
                    i = (int) file.length();
                }
                StringBuilder sb = new StringBuilder();
                randomAccessFile.seek((long) (parseObject.containsKey(Keywords.FUNC_POSITION_STRING) ? parseObject.getInteger(Keywords.FUNC_POSITION_STRING).intValue() : 0));
                for (int i2 = 0; i2 < i; i2++) {
                    sb.append((char) randomAccessFile.read());
                }
                HashMap hashMap = new HashMap();
                hashMap.put("text", sb.toString());
                result.success(hashMap);
                $closeResource(null, randomAccessFile);
            } catch (IOException unused) {
                result.error(300, "read file failed");
            }
        }
    }

    private void access(Function function, Result result) {
        JSONObject parseObject = JSONObject.parseObject((String) function.arguments.get(0));
        if (parseObject == null || !parseObject.containsKey(Constants.ELEMNAME_URL_STRING)) {
            result.error(202, "json arguments illegal");
            return;
        }
        String path = getPath(parseObject.getString(Constants.ELEMNAME_URL_STRING), result);
        if (path != null) {
            if (new File(path).exists()) {
                result.success(null);
            } else {
                result.error(301, "file not exist");
            }
        }
    }

    private void mkdir(Function function, Result result) {
        JSONObject parseObject = JSONObject.parseObject((String) function.arguments.get(0));
        if (parseObject == null || !parseObject.containsKey(Constants.ELEMNAME_URL_STRING)) {
            result.error(202, "json arguments illegal");
            return;
        }
        String path = getPath(parseObject.getString(Constants.ELEMNAME_URL_STRING), result);
        if (path != null) {
            File file = new File(path);
            if (!parseObject.containsKey("recursive") || !parseObject.getBoolean("recursive").booleanValue()) {
                if (file.mkdir()) {
                    result.success(null);
                } else {
                    result.error(300, "make directory failed");
                }
            } else if (file.mkdirs()) {
                result.success(null);
            } else {
                result.error(300, "make directory failed");
            }
        }
    }

    private void rmdir(Function function, Result result) {
        JSONObject parseObject = JSONObject.parseObject((String) function.arguments.get(0));
        if (parseObject == null || !parseObject.containsKey(Constants.ELEMNAME_URL_STRING)) {
            result.error(202, "json arguments illegal");
            return;
        }
        String path = getPath(parseObject.getString(Constants.ELEMNAME_URL_STRING), result);
        if (path != null) {
            File file = new File(path);
            if (!parseObject.containsKey("recursive") || !parseObject.getBoolean("recursive").booleanValue()) {
                if (file.delete()) {
                    result.success(null);
                } else {
                    result.error(300, "remove directory failed");
                }
            } else if (deleteFolder(file)) {
                result.success(null);
            } else {
                result.error(300, "remove directory failed");
            }
        }
    }

    private boolean deleteFolder(File file) {
        File[] listFiles;
        if (file.isDirectory() && (listFiles = file.listFiles()) != null) {
            for (File file2 : listFiles) {
                deleteFolder(file2);
            }
        }
        return file.delete();
    }

    private String getPath(String str, Result result) {
        try {
            String realPath = getRealPath(str);
            if (realPath != null) {
                return realPath;
            }
            result.error(202, "illegal uri:" + str);
            HiLog.warn(LABEL_LOG, "illegal uri", new Object[0]);
            return null;
        } catch (IOException | NullPointerException unused) {
            result.error(202, "illegal path");
            return null;
        }
    }

    private String getRealPath(String str) throws IOException, NullPointerException {
        if (str == null) {
            return null;
        }
        if (str.indexOf(APP_CACHE_DIRECTORY) == 0) {
            String path = Paths.get(str.substring(16), new String[0]).normalize().toString();
            return this.applicationContext.getCacheDir().getCanonicalPath() + path;
        } else if (str.indexOf(APP_FILE_DIRECTORY) == 0) {
            String path2 = Paths.get(str.substring(14), new String[0]).normalize().toString();
            return this.applicationContext.getFilesDir().getCanonicalPath() + path2;
        } else if (str.indexOf(APP_SHARE_DIRECTORY) == 0) {
            String path3 = Paths.get(str.substring(16), new String[0]).normalize().toString();
            return this.applicationContext.getExternalCacheDir().getCanonicalPath() + path3;
        } else {
            HiLog.warn(LABEL_LOG, "getRealPath: illegal uri", new Object[0]);
            return null;
        }
    }

    public static void register(Context context) {
        if (context == null) {
            HiLog.error(LABEL_LOG, "Context may not be null when register FilePlugin", new Object[0]);
        } else if (!(context instanceof AceAbility)) {
            HiLog.error(LABEL_LOG, "Failed to get abilityId when register FilePlugin", new Object[0]);
        } else {
            int abilityId = ((AceAbility) context).getAbilityId();
            FilePlugin filePlugin = new FilePlugin(context);
            instanceMap.put(Integer.valueOf(abilityId), filePlugin);
            ModuleGroup.registerModuleGroup(MODULE_GROUP_NAME, filePlugin, Integer.valueOf(abilityId));
            filePlugin.onRegister(context);
        }
    }

    private void onRegister(Context context) {
        this.applicationContext = context;
    }

    public static void deregister(Context context) {
        if (!(context instanceof AceAbility)) {
            HiLog.error(LABEL_LOG, "Failed to get abilityId when deregister FilePlugin", new Object[0]);
            return;
        }
        int abilityId = ((AceAbility) context).getAbilityId();
        instanceMap.remove(Integer.valueOf(abilityId));
        ModuleGroup.registerModuleGroup(MODULE_GROUP_NAME, null, Integer.valueOf(abilityId));
    }

    public static Set<String> getPluginGroup() {
        HashSet hashSet = new HashSet();
        hashSet.add(MODULE_GROUP_NAME);
        return hashSet;
    }
}
