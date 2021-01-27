package com.huawei.coauth.pool;

import com.huawei.hwpartsecurity.BuildConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ExecutorPropertyHelper {
    private static final String DEFALUT_VALUE = "default";
    private static final int PASSWORD_INFO_COUNT = 0;
    private static final int PASSWORD_INFO_ISROOT = 2;
    private static final int PASSWORD_INFO_PASSWORDTYPE = 1;
    private static final int PASSWORD_INFO_SIZE = 3;
    private static final String TAG = "ExecutorPropertyHelper";
    private static final int TEMPLATE_INFO_DESC = 1;
    private static final int TEMPLATE_INFO_ID = 0;
    private static final int TEMPLATE_INFO_SIZE = 2;

    public static class PasswordInfo {
        public int count;
        public int isRoot;
        public int type;
    }

    public static class TemplateInfo {
        public String desc;
        public String templateId;
    }

    public static byte[] fromTemplateInfoList(List<TemplateInfo> list) {
        return new byte[0];
    }

    public static List<TemplateInfo> toTemplateInfoList(byte[] value) {
        List<TemplateInfo> out = new ArrayList<>();
        TemplateInfo info = new TemplateInfo();
        info.templateId = DEFALUT_VALUE;
        info.desc = DEFALUT_VALUE;
        out.add(info);
        return out;
    }

    public static byte[] fromPasswordInfo(PasswordInfo info) {
        return new byte[0];
    }

    public static PasswordInfo toPasswordInfo(byte[] value) {
        PasswordInfo info = new PasswordInfo();
        info.count = 0;
        info.type = 0;
        info.isRoot = 0;
        return info;
    }

    private static String fromTemplateInfo(TemplateInfo info) {
        if (Objects.isNull(info)) {
            return BuildConfig.FLAVOR;
        }
        return info.templateId + "||" + info.desc;
    }

    private static String fromPasswordInfoToString(PasswordInfo info) {
        if (Objects.isNull(info)) {
            return BuildConfig.FLAVOR;
        }
        return (info.count + "||" + info.type + "||" + info.isRoot).toString();
    }
}
