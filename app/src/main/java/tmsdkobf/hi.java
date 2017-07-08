package tmsdkobf;

import android.text.TextUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* compiled from: Unknown */
public class hi {
    public static int a(String str, Pattern pattern) {
        int i = 0;
        Matcher matcher = pattern.matcher(str);
        if (matcher.find() && matcher.groupCount() >= 1) {
            try {
                i = Integer.parseInt(matcher.group(1));
            } catch (Exception e) {
            }
        }
        return i;
    }

    public static List<String> aZ(String str) {
        List<String> arrayList = new ArrayList();
        if (TextUtils.isEmpty(str)) {
            return arrayList;
        }
        for (String str2 : gq.aT()) {
            String str3 = str2 + "/" + str;
            String[] list = new File(str3).list();
            if (list != null) {
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

    public static String b(String str, Pattern pattern) {
        Matcher matcher = pattern.matcher(str);
        return (matcher.find() && matcher.groupCount() >= 1) ? matcher.group(1) : null;
    }

    public static long ba(String str) {
        long j = 0;
        if (TextUtils.isEmpty(str)) {
            return 0;
        }
        long j2;
        Stack stack = new Stack();
        Stack stack2 = new Stack();
        stack.push(str);
        while (true) {
            j2 = j;
            while (!stack.isEmpty()) {
                File file = new File((String) stack.pop());
                if (file.isDirectory()) {
                    stack2.push(file.getAbsolutePath());
                    String[] list = file.list();
                    if (list == null) {
                        j = j2;
                    } else {
                        j = j2;
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
                    j2 = file.length() + j2;
                }
            }
            break;
        }
        while (!stack2.isEmpty()) {
            j2 += new File((String) stack2.pop()).length();
        }
        return j2;
    }

    public static List<String> bb(String str) {
        InputStreamReader inputStreamReader;
        Exception e;
        InputStreamReader inputStreamReader2;
        FileInputStream fileInputStream;
        Throwable th;
        BufferedReader bufferedReader = null;
        List<String> arrayList = new ArrayList();
        FileInputStream fileInputStream2;
        BufferedReader bufferedReader2;
        try {
            fileInputStream2 = new FileInputStream(str);
            try {
                inputStreamReader = new InputStreamReader(fileInputStream2, "utf-8");
                try {
                    bufferedReader2 = new BufferedReader(inputStreamReader);
                    while (true) {
                        try {
                            String readLine = bufferedReader2.readLine();
                            if (readLine == null) {
                                break;
                            }
                            arrayList.add(readLine);
                        } catch (Exception e2) {
                            e = e2;
                            inputStreamReader2 = inputStreamReader;
                            fileInputStream = fileInputStream2;
                        } catch (Throwable th2) {
                            th = th2;
                            bufferedReader = bufferedReader2;
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
                } catch (Exception e4) {
                    e = e4;
                    bufferedReader2 = null;
                    inputStreamReader2 = inputStreamReader;
                    fileInputStream = fileInputStream2;
                    try {
                        e.printStackTrace();
                        if (bufferedReader2 != null) {
                            try {
                                bufferedReader2.close();
                            } catch (IOException e3222) {
                                e3222.printStackTrace();
                            }
                        }
                        if (inputStreamReader2 != null) {
                            try {
                                inputStreamReader2.close();
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
                    } catch (Throwable th3) {
                        th = th3;
                        fileInputStream2 = fileInputStream;
                        inputStreamReader = inputStreamReader2;
                        bufferedReader = bufferedReader2;
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            } catch (IOException e5) {
                                e5.printStackTrace();
                            }
                        }
                        if (inputStreamReader != null) {
                            try {
                                inputStreamReader.close();
                            } catch (IOException e52) {
                                e52.printStackTrace();
                            }
                        }
                        if (fileInputStream2 != null) {
                            try {
                                fileInputStream2.close();
                            } catch (IOException e522) {
                                e522.printStackTrace();
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                    if (inputStreamReader != null) {
                        inputStreamReader.close();
                    }
                    if (fileInputStream2 != null) {
                        fileInputStream2.close();
                    }
                    throw th;
                }
            } catch (Exception e6) {
                e = e6;
                bufferedReader2 = null;
                fileInputStream = fileInputStream2;
                e.printStackTrace();
                if (bufferedReader2 != null) {
                    bufferedReader2.close();
                }
                if (inputStreamReader2 != null) {
                    inputStreamReader2.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                return arrayList;
            } catch (Throwable th5) {
                th = th5;
                inputStreamReader = null;
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                if (fileInputStream2 != null) {
                    fileInputStream2.close();
                }
                throw th;
            }
        } catch (Exception e7) {
            e = e7;
            bufferedReader2 = null;
            fileInputStream = null;
            e.printStackTrace();
            if (bufferedReader2 != null) {
                bufferedReader2.close();
            }
            if (inputStreamReader2 != null) {
                inputStreamReader2.close();
            }
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            return arrayList;
        } catch (Throwable th6) {
            th = th6;
            inputStreamReader = null;
            fileInputStream2 = null;
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }
            if (fileInputStream2 != null) {
                fileInputStream2.close();
            }
            throw th;
        }
        return arrayList;
    }

    public static String bc(String str) {
        StringBuffer stringBuffer = new StringBuffer();
        Matcher matcher = Pattern.compile("\\\\u([\\S]{4})([^\\\\]*)").matcher(str);
        while (matcher.find()) {
            stringBuffer.append((char) Integer.parseInt(matcher.group(1), 16));
            stringBuffer.append(matcher.group(2));
        }
        return stringBuffer.toString();
    }

    public static String bd(String str) {
        byte[] cF = nb.cF(str);
        StringBuilder stringBuilder = new StringBuilder(cF.length * 2);
        for (byte b : cF) {
            String toHexString = Integer.toHexString(b & 255);
            if (toHexString.length() == 1) {
                toHexString = "0" + toHexString;
            }
            stringBuilder.append(toHexString);
        }
        return stringBuilder.toString();
    }
}
