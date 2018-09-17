package com.huawei.theme.a.a;

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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
            Throwable th2 = th;
            FileOutputStream fileOutputStream2 = fileOutputStream;
            Throwable th3 = th2;
            if (fileOutputStream2 != null) {
                try {
                    fileOutputStream2.close();
                } catch (IOException e5) {
                    e5.printStackTrace();
                }
            }
            throw th3;
        }
    }

    public static JSONObject b(Context context, String str) {
        FileInputStream openFileInput;
        BufferedReader bufferedReader;
        BufferedReader bufferedReader2;
        FileInputStream fileInputStream;
        JSONException e;
        Exception e2;
        Throwable th;
        try {
            openFileInput = context.openFileInput(d(context, str));
            try {
                bufferedReader = new BufferedReader(new InputStreamReader(openFileInput, "UTF-8"));
                try {
                    StringBuffer stringBuffer = new StringBuffer("");
                    while (true) {
                        String readLine = bufferedReader.readLine();
                        if (readLine == null) {
                            break;
                        }
                        stringBuffer.append(readLine);
                    }
                    if (stringBuffer.length() == 0) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e3) {
                            e3.printStackTrace();
                        }
                        if (openFileInput == null) {
                            return null;
                        }
                        try {
                            openFileInput.close();
                            return null;
                        } catch (IOException e32) {
                            e32.printStackTrace();
                            return null;
                        }
                    }
                    JSONObject jSONObject = new JSONObject(stringBuffer.toString());
                    try {
                        bufferedReader.close();
                    } catch (IOException e4) {
                        e4.printStackTrace();
                    }
                    if (openFileInput != null) {
                        try {
                            openFileInput.close();
                        } catch (IOException e42) {
                            e42.printStackTrace();
                        }
                    }
                    return jSONObject;
                } catch (FileNotFoundException e5) {
                    bufferedReader2 = bufferedReader;
                    fileInputStream = openFileInput;
                } catch (IOException e6) {
                } catch (JSONException e7) {
                    e = e7;
                } catch (Exception e8) {
                    e2 = e8;
                }
            } catch (FileNotFoundException e9) {
                bufferedReader2 = null;
                fileInputStream = openFileInput;
                if (bufferedReader2 != null) {
                    try {
                        bufferedReader2.close();
                    } catch (IOException e322) {
                        e322.printStackTrace();
                    }
                }
                if (fileInputStream != null) {
                    return null;
                }
                try {
                    fileInputStream.close();
                    return null;
                } catch (IOException e3222) {
                    e3222.printStackTrace();
                    return null;
                }
            } catch (IOException e10) {
                bufferedReader = null;
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e32222) {
                        e32222.printStackTrace();
                    }
                }
                if (openFileInput != null) {
                    return null;
                }
                try {
                    openFileInput.close();
                    return null;
                } catch (IOException e322222) {
                    e322222.printStackTrace();
                    return null;
                }
            } catch (JSONException e11) {
                e = e11;
                bufferedReader = null;
                try {
                    e.printStackTrace();
                    c(context, str);
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e3222222) {
                            e3222222.printStackTrace();
                        }
                    }
                    if (openFileInput != null) {
                        return null;
                    }
                    try {
                        openFileInput.close();
                        return null;
                    } catch (IOException e32222222) {
                        e32222222.printStackTrace();
                        return null;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e322222222) {
                            e322222222.printStackTrace();
                        }
                    }
                    if (openFileInput != null) {
                        try {
                            openFileInput.close();
                        } catch (IOException e3222222222) {
                            e3222222222.printStackTrace();
                        }
                    }
                    throw th;
                }
            } catch (Exception e12) {
                e2 = e12;
                bufferedReader = null;
                e2.printStackTrace();
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e32222222222) {
                        e32222222222.printStackTrace();
                    }
                }
                if (openFileInput != null) {
                    return null;
                }
                try {
                    openFileInput.close();
                    return null;
                } catch (IOException e322222222222) {
                    e322222222222.printStackTrace();
                    return null;
                }
            } catch (Throwable th3) {
                bufferedReader = null;
                th = th3;
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (openFileInput != null) {
                    openFileInput.close();
                }
                throw th;
            }
        } catch (FileNotFoundException e13) {
            bufferedReader2 = null;
            fileInputStream = null;
            if (bufferedReader2 != null) {
                bufferedReader2.close();
            }
            if (fileInputStream != null) {
                return null;
            }
            fileInputStream.close();
            return null;
        } catch (IOException e14) {
            bufferedReader = null;
            openFileInput = null;
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (openFileInput != null) {
                return null;
            }
            openFileInput.close();
            return null;
        } catch (JSONException e15) {
            e = e15;
            bufferedReader = null;
            openFileInput = null;
            e.printStackTrace();
            c(context, str);
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (openFileInput != null) {
                return null;
            }
            openFileInput.close();
            return null;
        } catch (Exception e16) {
            e2 = e16;
            bufferedReader = null;
            openFileInput = null;
            e2.printStackTrace();
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (openFileInput != null) {
                return null;
            }
            openFileInput.close();
            return null;
        } catch (Throwable th32) {
            bufferedReader = null;
            openFileInput = null;
            th = th32;
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (openFileInput != null) {
                openFileInput.close();
            }
            throw th;
        }
    }

    public static void c(Context context, String str) {
        context.deleteFile(d(context, str));
    }

    private static String d(Context context, String str) {
        return "hianalytics_" + str + "_" + context.getPackageName();
    }
}
