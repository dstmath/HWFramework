package tmsdkobf;

import android.text.TextUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class rq {
    public static int a(String str, Pattern pattern) {
        int i = 0;
        Matcher matcher = pattern.matcher(str);
        if (!matcher.find() || matcher.groupCount() < 1) {
            return i;
        }
        try {
            return Integer.parseInt(matcher.group(1));
        } catch (Exception e) {
            return i;
        }
    }

    public static String b(String str, Pattern pattern) {
        Matcher matcher = pattern.matcher(str);
        return (matcher.find() && matcher.groupCount() >= 1) ? matcher.group(1) : null;
    }

    public static List<String> dp(String str) {
        List<String> arrayList = new ArrayList();
        if (TextUtils.isEmpty(str)) {
            return arrayList;
        }
        for (String str2 : rh.jZ()) {
            String str3 = str2 + "/" + str;
            String[] list = new File(str3).list();
            if (list != null) {
                String[] strArr = list;
                for (String str4 : list) {
                    File file = new File(str3 + "/" + str4);
                    if (file.isDirectory()) {
                        arrayList.add(file.getAbsolutePath());
                    }
                }
            }
        }
        return arrayList;
    }

    public static long dq(String str) {
        if (TextUtils.isEmpty(str)) {
            return 0;
        }
        long j = 0;
        Stack stack = new Stack();
        Stack stack2 = new Stack();
        stack.push(str);
        while (!stack.isEmpty()) {
            File file = new File((String) stack.pop());
            if (file.isDirectory()) {
                stack2.push(file.getAbsolutePath());
                String[] list = file.list();
                if (list != null) {
                    String[] strArr = list;
                    for (String file2 : list) {
                        File file3 = new File(file, file2);
                        if (file3.isDirectory()) {
                            stack.push(file3.getAbsolutePath());
                        } else {
                            j += file3.length();
                        }
                    }
                }
            } else {
                j += file.length();
            }
        }
        while (!stack2.isEmpty()) {
            j += new File((String) stack2.pop()).length();
        }
        return j;
    }

    /* JADX WARNING: Removed duplicated region for block: B:45:0x0060 A:{SYNTHETIC, Splitter: B:45:0x0060} */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x0069 A:{SYNTHETIC, Splitter: B:49:0x0069} */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x003c A:{SYNTHETIC, Splitter: B:29:0x003c} */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x007a A:{SYNTHETIC, Splitter: B:58:0x007a} */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x0083 A:{SYNTHETIC, Splitter: B:62:0x0083} */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x008c A:{SYNTHETIC, Splitter: B:66:0x008c} */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x0060 A:{SYNTHETIC, Splitter: B:45:0x0060} */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x0069 A:{SYNTHETIC, Splitter: B:49:0x0069} */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x003c A:{SYNTHETIC, Splitter: B:29:0x003c} */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x007a A:{SYNTHETIC, Splitter: B:58:0x007a} */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x0083 A:{SYNTHETIC, Splitter: B:62:0x0083} */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x008c A:{SYNTHETIC, Splitter: B:66:0x008c} */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x0060 A:{SYNTHETIC, Splitter: B:45:0x0060} */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x0069 A:{SYNTHETIC, Splitter: B:49:0x0069} */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x003c A:{SYNTHETIC, Splitter: B:29:0x003c} */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x007a A:{SYNTHETIC, Splitter: B:58:0x007a} */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x0083 A:{SYNTHETIC, Splitter: B:62:0x0083} */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x008c A:{SYNTHETIC, Splitter: B:66:0x008c} */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x0060 A:{SYNTHETIC, Splitter: B:45:0x0060} */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x0069 A:{SYNTHETIC, Splitter: B:49:0x0069} */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x003c A:{SYNTHETIC, Splitter: B:29:0x003c} */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x007a A:{SYNTHETIC, Splitter: B:58:0x007a} */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x0083 A:{SYNTHETIC, Splitter: B:62:0x0083} */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x008c A:{SYNTHETIC, Splitter: B:66:0x008c} */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x007a A:{SYNTHETIC, Splitter: B:58:0x007a} */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x0083 A:{SYNTHETIC, Splitter: B:62:0x0083} */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x008c A:{SYNTHETIC, Splitter: B:66:0x008c} */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x0060 A:{SYNTHETIC, Splitter: B:45:0x0060} */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x0069 A:{SYNTHETIC, Splitter: B:49:0x0069} */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x003c A:{SYNTHETIC, Splitter: B:29:0x003c} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static List<String> dr(String str) {
        Exception e;
        Throwable th;
        Reader reader;
        InputStream inputStream;
        List<String> arrayList = new ArrayList();
        FileInputStream fileInputStream = null;
        InputStreamReader reader2 = null;
        BufferedReader bufferedReader = null;
        try {
            InputStream fileInputStream2 = new FileInputStream(str);
            try {
                try {
                    Reader inputStreamReader = new InputStreamReader(fileInputStream2, "utf-8");
                    try {
                        try {
                            BufferedReader bufferedReader2 = new BufferedReader(inputStreamReader);
                            while (true) {
                                try {
                                    String readLine = bufferedReader2.readLine();
                                    if (readLine == null) {
                                        break;
                                    }
                                    arrayList.add(readLine);
                                } catch (Exception e2) {
                                    e = e2;
                                    bufferedReader = bufferedReader2;
                                    reader2 = inputStreamReader;
                                    fileInputStream = fileInputStream2;
                                    try {
                                        e.printStackTrace();
                                        if (bufferedReader != null) {
                                        }
                                        if (reader2 != null) {
                                        }
                                        if (fileInputStream != null) {
                                        }
                                        return arrayList;
                                    } catch (Throwable th2) {
                                        th = th2;
                                        if (bufferedReader != null) {
                                        }
                                        if (reader2 != null) {
                                        }
                                        if (fileInputStream != null) {
                                        }
                                        throw th;
                                    }
                                } catch (Throwable th3) {
                                    th = th3;
                                    bufferedReader = bufferedReader2;
                                    reader2 = inputStreamReader;
                                    inputStream = fileInputStream2;
                                    if (bufferedReader != null) {
                                    }
                                    if (reader2 != null) {
                                    }
                                    if (fileInputStream != null) {
                                    }
                                    throw th;
                                }
                            }
                            if (bufferedReader2 != null) {
                                try {
                                    bufferedReader2.close();
                                } catch (IOException e3) {
                                    e3.printStackTrace();
                                }
                            }
                            if (inputStreamReader != null) {
                                try {
                                    inputStreamReader.close();
                                } catch (IOException e32) {
                                    e32.printStackTrace();
                                }
                            }
                            if (fileInputStream2 != null) {
                                try {
                                    fileInputStream2.close();
                                } catch (IOException e322) {
                                    e322.printStackTrace();
                                }
                            }
                            bufferedReader = bufferedReader2;
                            reader2 = inputStreamReader;
                            inputStream = fileInputStream2;
                        } catch (Exception e4) {
                            e = e4;
                            reader2 = inputStreamReader;
                            inputStream = fileInputStream2;
                            e.printStackTrace();
                            if (bufferedReader != null) {
                            }
                            if (reader2 != null) {
                            }
                            if (fileInputStream != null) {
                            }
                            return arrayList;
                        } catch (Throwable th4) {
                            th = th4;
                            reader2 = inputStreamReader;
                            inputStream = fileInputStream2;
                            if (bufferedReader != null) {
                            }
                            if (reader2 != null) {
                            }
                            if (fileInputStream != null) {
                            }
                            throw th;
                        }
                    } catch (Exception e5) {
                        e = e5;
                        reader2 = inputStreamReader;
                        inputStream = fileInputStream2;
                        e.printStackTrace();
                        if (bufferedReader != null) {
                        }
                        if (reader2 != null) {
                        }
                        if (fileInputStream != null) {
                        }
                        return arrayList;
                    } catch (Throwable th5) {
                        th = th5;
                        reader2 = inputStreamReader;
                        inputStream = fileInputStream2;
                        if (bufferedReader != null) {
                        }
                        if (reader2 != null) {
                        }
                        if (fileInputStream != null) {
                        }
                        throw th;
                    }
                } catch (Exception e6) {
                    e = e6;
                    inputStream = fileInputStream2;
                    e.printStackTrace();
                    if (bufferedReader != null) {
                    }
                    if (reader2 != null) {
                    }
                    if (fileInputStream != null) {
                    }
                    return arrayList;
                } catch (Throwable th6) {
                    th = th6;
                    inputStream = fileInputStream2;
                    if (bufferedReader != null) {
                    }
                    if (reader2 != null) {
                    }
                    if (fileInputStream != null) {
                    }
                    throw th;
                }
            } catch (Exception e7) {
                e = e7;
                inputStream = fileInputStream2;
                e.printStackTrace();
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e3222) {
                        e3222.printStackTrace();
                    }
                }
                if (reader2 != null) {
                    try {
                        reader2.close();
                    } catch (IOException e32222) {
                        e32222.printStackTrace();
                    }
                }
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e322222) {
                        e322222.printStackTrace();
                    }
                }
                return arrayList;
            } catch (Throwable th7) {
                th = th7;
                inputStream = fileInputStream2;
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e8) {
                        e8.printStackTrace();
                    }
                }
                if (reader2 != null) {
                    try {
                        reader2.close();
                    } catch (IOException e82) {
                        e82.printStackTrace();
                    }
                }
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e822) {
                        e822.printStackTrace();
                    }
                }
                throw th;
            }
        } catch (Exception e9) {
            e = e9;
            e.printStackTrace();
            if (bufferedReader != null) {
            }
            if (reader2 != null) {
            }
            if (fileInputStream != null) {
            }
            return arrayList;
        }
        return arrayList;
    }

    public static String ds(String str) {
        StringBuffer stringBuffer = new StringBuffer();
        Matcher matcher = Pattern.compile("\\\\u([\\S]{4})([^\\\\]*)").matcher(str);
        while (matcher.find()) {
            stringBuffer.append((char) Integer.parseInt(matcher.group(1), 16));
            stringBuffer.append(matcher.group(2));
        }
        return stringBuffer.toString();
    }

    public static String dt(String str) {
        byte[] bT = mc.bT(str);
        StringBuilder stringBuilder = new StringBuilder(bT.length * 2);
        byte[] bArr = bT;
        for (byte b : bT) {
            String toHexString = Integer.toHexString(b & 255);
            if (toHexString.length() == 1) {
                toHexString = "0" + toHexString;
            }
            stringBuilder.append(toHexString);
        }
        return stringBuilder.toString();
    }
}
