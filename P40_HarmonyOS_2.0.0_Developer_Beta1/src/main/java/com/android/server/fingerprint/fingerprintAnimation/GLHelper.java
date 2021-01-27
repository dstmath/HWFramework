package com.android.server.fingerprint.fingerprintAnimation;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;
import com.huawei.hwpartfingerprintopt.BuildConfig;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.Buffer;
import java.nio.FloatBuffer;

public class GLHelper {
    private static final int BUFFER_SIZE = 32768;
    private static final boolean IS_LOGGER_ON = false;
    private static final String TAG = "GLHelper";

    private static int compileShader(int type, String shaderCode) {
        int shaderObjectId = GLES20.glCreateShader(type);
        if (shaderObjectId == 0) {
            return 0;
        }
        GLES20.glShaderSource(shaderObjectId, shaderCode);
        GLES20.glCompileShader(shaderObjectId);
        int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shaderObjectId, 35713, compileStatus, 0);
        if (compileStatus[0] != 0) {
            return shaderObjectId;
        }
        GLES20.glDeleteShader(shaderObjectId);
        return 0;
    }

    private static int linkProgram(int vertexShaderId, int fragmentShaderId) {
        int programObjectId = GLES20.glCreateProgram();
        if (programObjectId == 0) {
            return 0;
        }
        GLES20.glAttachShader(programObjectId, vertexShaderId);
        GLES20.glAttachShader(programObjectId, fragmentShaderId);
        GLES20.glLinkProgram(programObjectId);
        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(programObjectId, 35714, linkStatus, 0);
        if (linkStatus[0] != 0) {
            return programObjectId;
        }
        GLES20.glDeleteProgram(programObjectId);
        return 0;
    }

    private static boolean validateProgram(int programObjectId) {
        GLES20.glValidateProgram(programObjectId);
        int[] validateStatus = new int[1];
        GLES20.glGetProgramiv(programObjectId, 35715, validateStatus, 0);
        if (validateStatus[0] != 0) {
            return true;
        }
        return false;
    }

    public static int buildProgram(String vertexShaderSource, String fragmentShaderSource) {
        int program = linkProgram(compileShader(35633, vertexShaderSource), compileShader(35632, fragmentShaderSource));
        validateProgram(program);
        return program;
    }

    public static int loadTexture(Context context, int resourceId) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        return loadTexture(BitmapFactory.decodeResource(context.getResources(), resourceId, options));
    }

    public static int loadTexture(Bitmap bitmap) {
        int[] textureObjectIds = new int[1];
        GLES20.glGenTextures(1, textureObjectIds, 0);
        if (textureObjectIds[0] == 0) {
            return 0;
        }
        GLES20.glBindTexture(3553, textureObjectIds[0]);
        GLES20.glTexParameteri(3553, 10241, 9987);
        GLES20.glTexParameteri(3553, 10240, 9729);
        GLES20.glTexParameteri(3553, 10242, 33071);
        GLES20.glTexParameteri(3553, 10243, 33071);
        GLUtils.texImage2D(3553, 0, 6408, bitmap, 5121, 0);
        GLES20.glGenerateMipmap(3553);
        GLES20.glBindTexture(3553, 0);
        return textureObjectIds[0];
    }

    public static String loadShaderSource(Context context, String fileName) {
        if (context == null) {
            Log.w(TAG, "readShaderSource context is null");
            return BuildConfig.FLAVOR;
        }
        AssetManager assetManager = context.getAssets();
        if (assetManager == null) {
            Log.w(TAG, "readShaderSource asset is null");
            return BuildConfig.FLAVOR;
        }
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        Reader reader = null;
        String result = null;
        Writer writer = new StringWriter();
        char[] buffer = new char[BUFFER_SIZE];
        try {
            InputStream inputStream2 = assetManager.open(fileName);
            if (inputStream2 != null) {
                InputStreamReader inputStreamReader2 = new InputStreamReader(inputStream2, "UTF-8");
                Reader reader2 = new BufferedReader(inputStreamReader2);
                while (true) {
                    int readLength = reader2.read(buffer);
                    if (readLength == -1) {
                        break;
                    }
                    writer.write(buffer, 0, readLength);
                }
                result = writer.toString();
                try {
                    inputStream2.close();
                } catch (IOException e) {
                    Log.e(TAG, "loadShaderSource, IOException when close inputStream");
                }
                try {
                    inputStreamReader2.close();
                } catch (IOException e2) {
                    Log.e(TAG, "loadShaderSource, IOException when close inputStreamReader");
                }
                try {
                    reader2.close();
                } catch (IOException e3) {
                    Log.e(TAG, "loadShaderSource, IOException when close reader");
                }
                return result;
            }
            throw new RuntimeException("Error: Null InputStream from app's asset manager:" + fileName);
        } catch (IOException e4) {
            Log.e(TAG, "loadShaderSource, IOException when open file");
            if (0 != 0) {
                try {
                    inputStream.close();
                } catch (IOException e5) {
                    Log.e(TAG, "loadShaderSource, IOException when close inputStream");
                }
            }
            if (0 != 0) {
                try {
                    inputStreamReader.close();
                } catch (IOException e6) {
                    Log.e(TAG, "loadShaderSource, IOException when close inputStreamReader");
                }
            }
            if (0 != 0) {
                try {
                    reader.close();
                } catch (IOException e7) {
                    Log.e(TAG, "loadShaderSource, IOException when close reader");
                }
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    inputStream.close();
                } catch (IOException e8) {
                    Log.e(TAG, "loadShaderSource, IOException when close inputStream");
                }
            }
            if (0 != 0) {
                try {
                    inputStreamReader.close();
                } catch (IOException e9) {
                    Log.e(TAG, "loadShaderSource, IOException when close inputStreamReader");
                }
            }
            if (0 != 0) {
                try {
                    reader.close();
                } catch (IOException e10) {
                    Log.e(TAG, "loadShaderSource, IOException when close reader");
                }
            }
            throw th;
        }
    }

    public static void setVertexAttributePointer(FloatBuffer floatBuffer, int dataOffset, int attributeLocation, int componentCount, int stride) {
        floatBuffer.position(dataOffset);
        GLES20.glVertexAttribPointer(attributeLocation, componentCount, 5126, false, stride, (Buffer) floatBuffer);
        GLES20.glEnableVertexAttribArray(attributeLocation);
        floatBuffer.position(0);
    }
}
