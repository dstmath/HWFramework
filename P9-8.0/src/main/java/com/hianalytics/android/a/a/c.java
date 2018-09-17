package com.hianalytics.android.a.a;

import android.content.Context;
import android.content.SharedPreferences;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import org.json.JSONException;
import org.json.JSONObject;

public final class c {
    public static SharedPreferences a(Context context, String str) {
        return context.getSharedPreferences("hianalytics_" + str + "_" + context.getPackageName(), 0);
    }

    public static void a(Context context, JSONObject jSONObject, String str) {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = context.openFileOutput(d(context, str), 0);
            fileOutputStream.write(jSONObject.toString().getBytes("UTF-8"));
            fileOutputStream.flush();
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException e2) {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e3) {
                    e3.printStackTrace();
                }
            }
        } catch (IOException e4) {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e32) {
                    e32.printStackTrace();
                }
            }
        } catch (Throwable th) {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e5) {
                    e5.printStackTrace();
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:36:0x0068 A:{SYNTHETIC, Splitter: B:36:0x0068} */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x0071 A:{SYNTHETIC, Splitter: B:40:0x0071} */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x0080 A:{SYNTHETIC, Splitter: B:48:0x0080} */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x0089 A:{SYNTHETIC, Splitter: B:52:0x0089} */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x009e A:{SYNTHETIC, Splitter: B:62:0x009e} */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x00a7 A:{SYNTHETIC, Splitter: B:66:0x00a7} */
    /* JADX WARNING: Removed duplicated region for block: B:76:0x00b9 A:{SYNTHETIC, Splitter: B:76:0x00b9} */
    /* JADX WARNING: Removed duplicated region for block: B:80:0x00c2 A:{SYNTHETIC, Splitter: B:80:0x00c2} */
    /* JADX WARNING: Removed duplicated region for block: B:88:0x00d1 A:{SYNTHETIC, Splitter: B:88:0x00d1} */
    /* JADX WARNING: Removed duplicated region for block: B:92:0x00da A:{SYNTHETIC, Splitter: B:92:0x00da} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static JSONObject b(Context context, String str) {
        JSONException e;
        Throwable th;
        Exception e2;
        FileInputStream fileInputStream = null;
        BufferedReader bufferedReader = null;
        try {
            fileInputStream = context.openFileInput(d(context, str));
            BufferedReader bufferedReader2 = new BufferedReader(new InputStreamReader(fileInputStream, "UTF-8"));
            try {
                StringBuffer stringBuffer = new StringBuffer("");
                while (true) {
                    String readLine = bufferedReader2.readLine();
                    if (readLine == null) {
                        break;
                    }
                    stringBuffer.append(readLine);
                }
                if (stringBuffer.length() != 0) {
                    JSONObject jSONObject = new JSONObject(stringBuffer.toString());
                    try {
                        bufferedReader2.close();
                    } catch (IOException e3) {
                        e3.printStackTrace();
                    }
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e32) {
                            e32.printStackTrace();
                        }
                    }
                    return jSONObject;
                }
                try {
                    bufferedReader2.close();
                } catch (IOException e322) {
                    e322.printStackTrace();
                }
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e3222) {
                        e3222.printStackTrace();
                    }
                }
                return null;
            } catch (FileNotFoundException e4) {
                bufferedReader = bufferedReader2;
                if (bufferedReader != null) {
                }
                if (fileInputStream != null) {
                }
                return null;
            } catch (IOException e5) {
                bufferedReader = bufferedReader2;
                if (bufferedReader != null) {
                }
                if (fileInputStream != null) {
                }
                return null;
            } catch (JSONException e6) {
                e = e6;
                bufferedReader = bufferedReader2;
                try {
                    e.printStackTrace();
                    c(context, str);
                    if (bufferedReader != null) {
                    }
                    if (fileInputStream != null) {
                    }
                    return null;
                } catch (Throwable th2) {
                    th = th2;
                    if (bufferedReader != null) {
                    }
                    if (fileInputStream != null) {
                    }
                    throw th;
                }
            } catch (Exception e7) {
                e2 = e7;
                bufferedReader = bufferedReader2;
                e2.printStackTrace();
                if (bufferedReader != null) {
                }
                if (fileInputStream != null) {
                }
                return null;
            } catch (Throwable th3) {
                th = th3;
                bufferedReader = bufferedReader2;
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e8) {
                        e8.printStackTrace();
                    }
                }
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e82) {
                        e82.printStackTrace();
                    }
                }
                throw th;
            }
        } catch (FileNotFoundException e9) {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
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
            return null;
        } catch (IOException e10) {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e3222222) {
                    e3222222.printStackTrace();
                }
            }
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e32222222) {
                    e32222222.printStackTrace();
                }
            }
            return null;
        } catch (JSONException e11) {
            e = e11;
            e.printStackTrace();
            c(context, str);
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e322222222) {
                    e322222222.printStackTrace();
                }
            }
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e3222222222) {
                    e3222222222.printStackTrace();
                }
            }
            return null;
        } catch (Exception e12) {
            e2 = e12;
            e2.printStackTrace();
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e32222222222) {
                    e32222222222.printStackTrace();
                }
            }
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e322222222222) {
                    e322222222222.printStackTrace();
                }
            }
            return null;
        }
    }

    public static void c(Context context, String str) {
        context.deleteFile(d(context, str));
    }

    private static String d(Context context, String str) {
        return "hianalytics_" + str + "_" + context.getPackageName();
    }
}
