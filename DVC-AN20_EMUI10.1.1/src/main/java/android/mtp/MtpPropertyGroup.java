package android.mtp;

import android.content.ContentProviderClient;
import android.database.Cursor;
import android.mtp.MtpStorageManager;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.util.Log;
import java.util.ArrayList;

class MtpPropertyGroup {
    private static final String PATH_WHERE = "_data=?";
    private static final String TAG = MtpPropertyGroup.class.getSimpleName();
    private String[] mColumns;
    private final Property[] mProperties;

    public static native String format_date_time(long j);

    /* access modifiers changed from: private */
    public class Property {
        int code;
        int column;
        int type;

        Property(int code2, int type2, int column2) {
            this.code = code2;
            this.type = type2;
            this.column = column2;
        }
    }

    public MtpPropertyGroup(int[] properties) {
        int count = properties.length;
        ArrayList<String> columns = new ArrayList<>(count);
        columns.add("_id");
        this.mProperties = new Property[count];
        for (int i = 0; i < count; i++) {
            this.mProperties[i] = createProperty(properties[i], columns);
        }
        int count2 = columns.size();
        this.mColumns = new String[count2];
        for (int i2 = 0; i2 < count2; i2++) {
            this.mColumns[i2] = columns.get(i2);
        }
    }

    private Property createProperty(int code, ArrayList<String> columns) {
        int type;
        String column = null;
        switch (code) {
            case MtpConstants.PROPERTY_STORAGE_ID:
                type = 6;
                break;
            case MtpConstants.PROPERTY_OBJECT_FORMAT:
                type = 4;
                break;
            case MtpConstants.PROPERTY_PROTECTION_STATUS:
                type = 4;
                break;
            case MtpConstants.PROPERTY_OBJECT_SIZE:
                type = 8;
                break;
            case MtpConstants.PROPERTY_OBJECT_FILE_NAME:
                type = 65535;
                break;
            case MtpConstants.PROPERTY_DATE_MODIFIED:
                type = 65535;
                break;
            case MtpConstants.PROPERTY_PARENT_OBJECT:
                type = 6;
                break;
            case MtpConstants.PROPERTY_PERSISTENT_UID:
                type = 10;
                break;
            case MtpConstants.PROPERTY_NAME:
                type = 65535;
                break;
            case MtpConstants.PROPERTY_ARTIST:
                type = 65535;
                break;
            case MtpConstants.PROPERTY_DESCRIPTION:
                column = "description";
                type = 65535;
                break;
            case MtpConstants.PROPERTY_DATE_ADDED:
                type = 65535;
                break;
            case MtpConstants.PROPERTY_DURATION:
                column = "duration";
                type = 6;
                break;
            case MtpConstants.PROPERTY_TRACK:
                column = MediaStore.Audio.AudioColumns.TRACK;
                type = 4;
                break;
            case MtpConstants.PROPERTY_COMPOSER:
                column = MediaStore.Audio.AudioColumns.COMPOSER;
                type = 65535;
                break;
            case MtpConstants.PROPERTY_ORIGINAL_RELEASE_DATE:
                column = MediaStore.Audio.AudioColumns.YEAR;
                type = 65535;
                break;
            case MtpConstants.PROPERTY_ALBUM_NAME:
                type = 65535;
                break;
            case MtpConstants.PROPERTY_ALBUM_ARTIST:
                column = MediaStore.Audio.AudioColumns.ALBUM_ARTIST;
                type = 65535;
                break;
            case MtpConstants.PROPERTY_DISPLAY_NAME:
                type = 65535;
                break;
            case MtpConstants.PROPERTY_BITRATE_TYPE:
            case MtpConstants.PROPERTY_NUMBER_OF_CHANNELS:
                type = 4;
                break;
            case MtpConstants.PROPERTY_SAMPLE_RATE:
            case MtpConstants.PROPERTY_AUDIO_WAVE_CODEC:
            case MtpConstants.PROPERTY_AUDIO_BITRATE:
                type = 6;
                break;
            default:
                type = 0;
                String str = TAG;
                Log.e(str, "unsupported property " + code);
                break;
        }
        if (column == null) {
            return new Property(code, type, -1);
        }
        columns.add(column);
        return new Property(code, type, columns.size() - 1);
    }

    /* JADX WARNING: Removed duplicated region for block: B:26:0x0071  */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x0075  */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0084  */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x0091  */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x00b6  */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x00d6  */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x00f9  */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x011c  */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x012b  */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x0136  */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x0146  */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x0154  */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x0164  */
    public int getPropertyList(ContentProviderClient provider, String volumeName, MtpStorageManager.MtpObject object, MtpPropertyList list) {
        Cursor c;
        int track;
        long longValue;
        int id = object.getId();
        String path = object.getPath().toString();
        Property[] propertyArr = this.mProperties;
        int length = propertyArr.length;
        Cursor c2 = null;
        int i = 0;
        while (i < length) {
            Property property = propertyArr[i];
            if (property.column == -1 || c2 != null) {
                c = c2;
            } else {
                try {
                    try {
                        Cursor c3 = provider.query(MtpDatabase.getObjectPropertiesUri(object.getFormat(), volumeName), this.mColumns, PATH_WHERE, new String[]{path}, null, null);
                        if (c3 != null && !c3.moveToNext()) {
                            c3.close();
                            c3 = null;
                        }
                        c = c3;
                    } catch (IllegalArgumentException e) {
                        return MtpConstants.RESPONSE_INVALID_OBJECT_PROP_CODE;
                    } catch (RemoteException e2) {
                        Log.e(TAG, "Mediaprovider lookup failed");
                        c = c2;
                        switch (property.code) {
                            case MtpConstants.PROPERTY_STORAGE_ID:
                                break;
                            case MtpConstants.PROPERTY_OBJECT_FORMAT:
                                break;
                            case MtpConstants.PROPERTY_PROTECTION_STATUS:
                                break;
                            case MtpConstants.PROPERTY_OBJECT_SIZE:
                                break;
                            case MtpConstants.PROPERTY_OBJECT_FILE_NAME:
                            case MtpConstants.PROPERTY_NAME:
                            case MtpConstants.PROPERTY_DISPLAY_NAME:
                                break;
                            case MtpConstants.PROPERTY_DATE_MODIFIED:
                            case MtpConstants.PROPERTY_DATE_ADDED:
                                break;
                            case MtpConstants.PROPERTY_PARENT_OBJECT:
                                break;
                            case MtpConstants.PROPERTY_PERSISTENT_UID:
                                break;
                            case MtpConstants.PROPERTY_TRACK:
                                break;
                            case MtpConstants.PROPERTY_ORIGINAL_RELEASE_DATE:
                                break;
                            case MtpConstants.PROPERTY_BITRATE_TYPE:
                            case MtpConstants.PROPERTY_NUMBER_OF_CHANNELS:
                                break;
                            case MtpConstants.PROPERTY_SAMPLE_RATE:
                            case MtpConstants.PROPERTY_AUDIO_WAVE_CODEC:
                            case MtpConstants.PROPERTY_AUDIO_BITRATE:
                                break;
                        }
                        i++;
                        c2 = c;
                    }
                } catch (IllegalArgumentException e3) {
                    return MtpConstants.RESPONSE_INVALID_OBJECT_PROP_CODE;
                } catch (RemoteException e4) {
                    Log.e(TAG, "Mediaprovider lookup failed");
                    c = c2;
                    switch (property.code) {
                        case MtpConstants.PROPERTY_STORAGE_ID:
                            break;
                        case MtpConstants.PROPERTY_OBJECT_FORMAT:
                            break;
                        case MtpConstants.PROPERTY_PROTECTION_STATUS:
                            break;
                        case MtpConstants.PROPERTY_OBJECT_SIZE:
                            break;
                        case MtpConstants.PROPERTY_OBJECT_FILE_NAME:
                        case MtpConstants.PROPERTY_NAME:
                        case MtpConstants.PROPERTY_DISPLAY_NAME:
                            break;
                        case MtpConstants.PROPERTY_DATE_MODIFIED:
                        case MtpConstants.PROPERTY_DATE_ADDED:
                            break;
                        case MtpConstants.PROPERTY_PARENT_OBJECT:
                            break;
                        case MtpConstants.PROPERTY_PERSISTENT_UID:
                            break;
                        case MtpConstants.PROPERTY_TRACK:
                            break;
                        case MtpConstants.PROPERTY_ORIGINAL_RELEASE_DATE:
                            break;
                        case MtpConstants.PROPERTY_BITRATE_TYPE:
                        case MtpConstants.PROPERTY_NUMBER_OF_CHANNELS:
                            break;
                        case MtpConstants.PROPERTY_SAMPLE_RATE:
                        case MtpConstants.PROPERTY_AUDIO_WAVE_CODEC:
                        case MtpConstants.PROPERTY_AUDIO_BITRATE:
                            break;
                    }
                    i++;
                    c2 = c;
                }
            }
            switch (property.code) {
                case MtpConstants.PROPERTY_STORAGE_ID:
                    list.append(id, property.code, property.type, (long) object.getStorageId());
                    break;
                case MtpConstants.PROPERTY_OBJECT_FORMAT:
                    list.append(id, property.code, property.type, (long) object.getFormat());
                    break;
                case MtpConstants.PROPERTY_PROTECTION_STATUS:
                    list.append(id, property.code, property.type, 0);
                    break;
                case MtpConstants.PROPERTY_OBJECT_SIZE:
                    list.append(id, property.code, property.type, object.getSize());
                    break;
                case MtpConstants.PROPERTY_OBJECT_FILE_NAME:
                case MtpConstants.PROPERTY_NAME:
                case MtpConstants.PROPERTY_DISPLAY_NAME:
                    list.append(id, property.code, object.getName());
                    break;
                case MtpConstants.PROPERTY_DATE_MODIFIED:
                case MtpConstants.PROPERTY_DATE_ADDED:
                    list.append(id, property.code, format_date_time(object.getModifiedTime()));
                    break;
                case MtpConstants.PROPERTY_PARENT_OBJECT:
                    list.append(id, property.code, property.type, object.getParent().isRoot() ? 0 : (long) object.getParent().getId());
                    break;
                case MtpConstants.PROPERTY_PERSISTENT_UID:
                    list.append(id, property.code, property.type, ((long) (object.getPath().toString().hashCode() << 32)) + object.getModifiedTime());
                    break;
                case MtpConstants.PROPERTY_TRACK:
                    if (c != null) {
                        track = c.getInt(property.column);
                    } else {
                        track = 0;
                    }
                    list.append(id, property.code, 4, (long) (track % 1000));
                    break;
                case MtpConstants.PROPERTY_ORIGINAL_RELEASE_DATE:
                    int year = 0;
                    if (c != null) {
                        year = c.getInt(property.column);
                    }
                    list.append(id, property.code, Integer.toString(year) + "0101T000000");
                    break;
                case MtpConstants.PROPERTY_BITRATE_TYPE:
                case MtpConstants.PROPERTY_NUMBER_OF_CHANNELS:
                    list.append(id, property.code, 4, 0);
                    break;
                case MtpConstants.PROPERTY_SAMPLE_RATE:
                case MtpConstants.PROPERTY_AUDIO_WAVE_CODEC:
                case MtpConstants.PROPERTY_AUDIO_BITRATE:
                    list.append(id, property.code, 6, 0);
                    break;
                default:
                    int i2 = property.type;
                    if (i2 != 0) {
                        if (i2 == 65535) {
                            String value = "";
                            if (c != null) {
                                value = c.getString(property.column);
                            }
                            list.append(id, property.code, value);
                            break;
                        } else {
                            if (c != null) {
                                longValue = c.getLong(property.column);
                            } else {
                                longValue = 0;
                            }
                            list.append(id, property.code, property.type, longValue);
                            break;
                        }
                    } else {
                        list.append(id, property.code, property.type, 0);
                        break;
                    }
            }
            i++;
            c2 = c;
        }
        if (c2 == null) {
            return 8193;
        }
        c2.close();
        return 8193;
    }
}
