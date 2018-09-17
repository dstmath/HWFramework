package android.mtp;

import android.app.DownloadManager;
import android.content.ContentProviderClient;
import android.database.Cursor;
import android.media.midi.MidiDeviceInfo;
import android.net.ProxyInfo;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.MediaStore.Audio.Genres;
import android.provider.MediaStore.Audio.Media;
import android.provider.MediaStore.Files;
import android.util.Log;
import java.util.ArrayList;

class MtpPropertyGroup {
    private static final String FORMAT_WHERE = "format=?";
    private static final String ID_FORMAT_WHERE = "_id=? AND format=?";
    private static final String ID_WHERE = "_id=?";
    private static final String PARENT_FORMAT_WHERE = "parent=? AND format=?";
    private static final String PARENT_WHERE = "parent=?";
    private static final String TAG = "MtpPropertyGroup";
    private String[] mColumns;
    private final MtpDatabase mDatabase;
    private final Property[] mProperties;
    private final ContentProviderClient mProvider;
    private final Uri mUri;
    private final String mVolumeName;

    private class Property {
        int code;
        int column;
        int type;

        Property(int code, int type, int column) {
            this.code = code;
            this.type = type;
            this.column = column;
        }
    }

    public static native String format_date_time(long j);

    public MtpPropertyGroup(MtpDatabase database, ContentProviderClient provider, String volumeName, int[] properties) {
        int i;
        this.mDatabase = database;
        this.mProvider = provider;
        this.mVolumeName = volumeName;
        this.mUri = Files.getMtpObjectsUri(volumeName).buildUpon().appendQueryParameter("nonotify", "1").build();
        int count = properties.length;
        ArrayList<String> columns = new ArrayList(count);
        columns.add(DownloadManager.COLUMN_ID);
        columns.add(DownloadManager.COLUMN_MEDIA_TYPE);
        this.mProperties = new Property[count];
        for (i = 0; i < count; i++) {
            this.mProperties[i] = createProperty(properties[i], columns);
        }
        count = columns.size();
        this.mColumns = new String[count];
        for (i = 0; i < count; i++) {
            this.mColumns[i] = (String) columns.get(i);
        }
    }

    private Property createProperty(int code, ArrayList<String> columns) {
        int type;
        Object column = null;
        switch (code) {
            case MtpConstants.PROPERTY_STORAGE_ID /*56321*/:
                column = "storage_id";
                type = 6;
                break;
            case MtpConstants.PROPERTY_OBJECT_FORMAT /*56322*/:
                column = "format";
                type = 4;
                break;
            case MtpConstants.PROPERTY_PROTECTION_STATUS /*56323*/:
                type = 4;
                break;
            case MtpConstants.PROPERTY_OBJECT_SIZE /*56324*/:
                column = "_size";
                type = 8;
                break;
            case MtpConstants.PROPERTY_OBJECT_FILE_NAME /*56327*/:
                column = "_data";
                type = 65535;
                break;
            case MtpConstants.PROPERTY_DATE_MODIFIED /*56329*/:
                column = "date_modified";
                type = 65535;
                break;
            case MtpConstants.PROPERTY_PARENT_OBJECT /*56331*/:
                column = "parent";
                type = 6;
                break;
            case MtpConstants.PROPERTY_PERSISTENT_UID /*56385*/:
                column = "storage_id";
                type = 10;
                break;
            case MtpConstants.PROPERTY_NAME /*56388*/:
                column = "title";
                type = 65535;
                break;
            case MtpConstants.PROPERTY_ARTIST /*56390*/:
                type = 65535;
                break;
            case MtpConstants.PROPERTY_DESCRIPTION /*56392*/:
                column = "description";
                type = 65535;
                break;
            case MtpConstants.PROPERTY_DATE_ADDED /*56398*/:
                column = "date_added";
                type = 65535;
                break;
            case MtpConstants.PROPERTY_DURATION /*56457*/:
                column = "duration";
                type = 6;
                break;
            case MtpConstants.PROPERTY_TRACK /*56459*/:
                column = "track";
                type = 4;
                break;
            case MtpConstants.PROPERTY_GENRE /*56460*/:
                type = 65535;
                break;
            case MtpConstants.PROPERTY_COMPOSER /*56470*/:
                column = "composer";
                type = 65535;
                break;
            case MtpConstants.PROPERTY_ORIGINAL_RELEASE_DATE /*56473*/:
                column = "year";
                type = 65535;
                break;
            case MtpConstants.PROPERTY_ALBUM_NAME /*56474*/:
                type = 65535;
                break;
            case MtpConstants.PROPERTY_ALBUM_ARTIST /*56475*/:
                column = "album_artist";
                type = 65535;
                break;
            case MtpConstants.PROPERTY_DISPLAY_NAME /*56544*/:
                column = "_display_name";
                type = 65535;
                break;
            case MtpConstants.PROPERTY_BITRATE_TYPE /*56978*/:
            case MtpConstants.PROPERTY_NUMBER_OF_CHANNELS /*56980*/:
                type = 4;
                break;
            case MtpConstants.PROPERTY_SAMPLE_RATE /*56979*/:
            case MtpConstants.PROPERTY_AUDIO_WAVE_CODEC /*56985*/:
            case MtpConstants.PROPERTY_AUDIO_BITRATE /*56986*/:
                type = 6;
                break;
            default:
                type = 0;
                Log.e(TAG, "unsupported property " + code);
                break;
        }
        if (column == null) {
            return new Property(code, type, -1);
        }
        columns.add(column);
        return new Property(code, type, columns.size() - 1);
    }

    private String queryString(int id, String column) {
        Cursor cursor = null;
        try {
            cursor = this.mProvider.query(this.mUri, new String[]{DownloadManager.COLUMN_ID, column}, ID_WHERE, new String[]{Integer.toString(id)}, null, null);
            String str;
            if (cursor == null || !cursor.moveToNext()) {
                str = ProxyInfo.LOCAL_EXCL_LIST;
                if (cursor != null) {
                    cursor.close();
                }
                return str;
            }
            str = cursor.getString(1);
            if (cursor != null) {
                cursor.close();
            }
            return str;
        } catch (Exception e) {
            if (cursor != null) {
                cursor.close();
            }
            return null;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    private String queryAudio(int id, String column) {
        Cursor cursor = null;
        Uri audioUri = Media.getContentUri(this.mVolumeName).buildUpon().appendQueryParameter("nonotify", "1").build();
        try {
            cursor = this.mProvider.query(audioUri, new String[]{DownloadManager.COLUMN_ID, column}, ID_WHERE, new String[]{Integer.toString(id)}, null, null);
            String str;
            if (cursor == null || !cursor.moveToNext()) {
                str = ProxyInfo.LOCAL_EXCL_LIST;
                if (cursor != null) {
                    cursor.close();
                }
                return str;
            }
            str = cursor.getString(1);
            if (cursor != null) {
                cursor.close();
            }
            return str;
        } catch (Exception e) {
            if (cursor != null) {
                cursor.close();
            }
            return null;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    private String queryGenre(int id) {
        Cursor c = null;
        try {
            Uri uri = Genres.getContentUriForAudioId(this.mVolumeName, id).buildUpon().appendQueryParameter("nonotify", "1").build();
            c = this.mProvider.query(uri, new String[]{DownloadManager.COLUMN_ID, MidiDeviceInfo.PROPERTY_NAME}, null, null, null, null);
            String str;
            if (c == null || !c.moveToNext()) {
                str = ProxyInfo.LOCAL_EXCL_LIST;
                if (c != null) {
                    c.close();
                }
                return str;
            }
            str = c.getString(1);
            if (c != null) {
                c.close();
            }
            return str;
        } catch (Exception e) {
            Log.e(TAG, "queryGenre exception", e);
            if (c != null) {
                c.close();
            }
            return null;
        } catch (Throwable th) {
            if (c != null) {
                c.close();
            }
            throw th;
        }
    }

    private Long queryLong(int id, String column) {
        Cursor cursor = null;
        try {
            cursor = this.mProvider.query(this.mUri, new String[]{DownloadManager.COLUMN_ID, column}, ID_WHERE, new String[]{Integer.toString(id)}, null, null);
            if (cursor == null || !cursor.moveToNext()) {
                if (cursor != null) {
                    cursor.close();
                }
                return null;
            }
            Long l = new Long(cursor.getLong(1));
            if (cursor != null) {
                cursor.close();
            }
            return l;
        } catch (Exception e) {
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static String nameFromPath(String path) {
        int start = 0;
        int lastSlash = path.lastIndexOf(47);
        if (lastSlash >= 0) {
            start = lastSlash + 1;
        }
        int end = path.length();
        if (end - start > 255) {
            end = start + 255;
        }
        return path.substring(start, end);
    }

    /* JADX WARNING: Missing block: B:32:0x0091, code:
            if (r34.mColumns.length <= 1) goto L_0x0093;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    MtpPropertyList getPropertyList(int handle, int format, int depth) {
        if (depth > 1) {
            return new MtpPropertyList(0, MtpConstants.RESPONSE_SPECIFICATION_BY_DEPTH_UNSUPPORTED);
        }
        String where;
        String[] whereArgs;
        if (format == 0) {
            if (handle == -1) {
                where = null;
                whereArgs = null;
            } else {
                whereArgs = new String[]{Integer.toString(handle)};
                if (depth == 1) {
                    where = PARENT_WHERE;
                } else {
                    where = ID_WHERE;
                }
            }
        } else if (handle == -1) {
            where = FORMAT_WHERE;
            whereArgs = new String[]{Integer.toString(format)};
        } else {
            whereArgs = new String[]{Integer.toString(handle), Integer.toString(format)};
            if (depth == 1) {
                where = PARENT_FORMAT_WHERE;
            } else {
                where = ID_FORMAT_WHERE;
            }
        }
        Cursor c = null;
        if (depth <= 0 && handle != -1) {
        }
        MtpPropertyList mtpPropertyList;
        try {
            c = this.mProvider.query(this.mUri, this.mColumns, where, whereArgs, null, null);
            if (c == null) {
                mtpPropertyList = new MtpPropertyList(0, MtpConstants.RESPONSE_INVALID_OBJECT_HANDLE);
                if (c != null) {
                    c.close();
                }
                return mtpPropertyList;
            }
            int count = c == null ? 1 : c.getCount();
            MtpPropertyList result = new MtpPropertyList(this.mProperties.length * count, MtpConstants.RESPONSE_OK);
            for (int objectIndex = 0; objectIndex < count; objectIndex++) {
                boolean isAudio = false;
                if (c != null) {
                    c.moveToNext();
                    handle = (int) c.getLong(0);
                    isAudio = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_MEDIA_TYPE)) == 2;
                }
                for (Property property : this.mProperties) {
                    int propertyCode = property.code;
                    int column = property.column;
                    switch (propertyCode) {
                        case MtpConstants.PROPERTY_PROTECTION_STATUS /*56323*/:
                            result.append(handle, propertyCode, 4, 0);
                            break;
                        case MtpConstants.PROPERTY_OBJECT_FILE_NAME /*56327*/:
                            String value = c.getString(column);
                            if (value == null) {
                                result.setResult(MtpConstants.RESPONSE_INVALID_OBJECT_HANDLE);
                                break;
                            }
                            result.append(handle, propertyCode, nameFromPath(value));
                            break;
                        case MtpConstants.PROPERTY_DATE_MODIFIED /*56329*/:
                        case MtpConstants.PROPERTY_DATE_ADDED /*56398*/:
                            result.append(handle, propertyCode, format_date_time((long) c.getInt(column)));
                            break;
                        case MtpConstants.PROPERTY_PERSISTENT_UID /*56385*/:
                            result.append(handle, propertyCode, 10, (c.getLong(column) << 32) + ((long) handle));
                            break;
                        case MtpConstants.PROPERTY_NAME /*56388*/:
                            String name = c.getString(column);
                            if (name == null) {
                                name = queryString(handle, MidiDeviceInfo.PROPERTY_NAME);
                            }
                            if (name == null) {
                                name = queryString(handle, "_data");
                                if (name != null) {
                                    name = nameFromPath(name);
                                }
                            }
                            if (name == null) {
                                result.setResult(MtpConstants.RESPONSE_INVALID_OBJECT_HANDLE);
                                break;
                            }
                            result.append(handle, propertyCode, name);
                            break;
                        case MtpConstants.PROPERTY_ARTIST /*56390*/:
                            if (!isAudio) {
                                result.append(handle, propertyCode, ProxyInfo.LOCAL_EXCL_LIST);
                                break;
                            }
                            result.append(handle, propertyCode, queryAudio(handle, "artist"));
                            break;
                        case MtpConstants.PROPERTY_TRACK /*56459*/:
                            result.append(handle, propertyCode, 4, (long) (c.getInt(column) % 1000));
                            break;
                        case MtpConstants.PROPERTY_GENRE /*56460*/:
                            String genre = ProxyInfo.LOCAL_EXCL_LIST;
                            if (isAudio) {
                                genre = queryGenre(handle);
                            }
                            if (genre == null) {
                                result.setResult(MtpConstants.RESPONSE_INVALID_OBJECT_HANDLE);
                                break;
                            }
                            result.append(handle, propertyCode, genre);
                            break;
                        case MtpConstants.PROPERTY_ORIGINAL_RELEASE_DATE /*56473*/:
                            result.append(handle, propertyCode, Integer.toString(c.getInt(column)) + "0101T000000");
                            break;
                        case MtpConstants.PROPERTY_ALBUM_NAME /*56474*/:
                            if (!isAudio) {
                                result.append(handle, propertyCode, ProxyInfo.LOCAL_EXCL_LIST);
                                break;
                            }
                            result.append(handle, propertyCode, queryAudio(handle, "album"));
                            break;
                        case MtpConstants.PROPERTY_BITRATE_TYPE /*56978*/:
                        case MtpConstants.PROPERTY_NUMBER_OF_CHANNELS /*56980*/:
                            result.append(handle, propertyCode, 4, 0);
                            break;
                        case MtpConstants.PROPERTY_SAMPLE_RATE /*56979*/:
                        case MtpConstants.PROPERTY_AUDIO_WAVE_CODEC /*56985*/:
                        case MtpConstants.PROPERTY_AUDIO_BITRATE /*56986*/:
                            result.append(handle, propertyCode, 6, 0);
                            break;
                        default:
                            if (property.type != 65535) {
                                if (property.type != 0) {
                                    result.append(handle, propertyCode, property.type, c.getLong(column));
                                    break;
                                }
                                result.append(handle, propertyCode, property.type, 0);
                                break;
                            }
                            result.append(handle, propertyCode, c.getString(column));
                            break;
                    }
                }
            }
            if (c != null) {
                c.close();
            }
            return result;
        } catch (RemoteException e) {
            mtpPropertyList = new MtpPropertyList(0, 8194);
            if (c != null) {
                c.close();
            }
            return mtpPropertyList;
        } catch (Throwable th) {
            if (c != null) {
                c.close();
            }
            throw th;
        }
    }
}
