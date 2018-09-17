package android.provider;

import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriPermission;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.hwgallerycache.HwGalleryCacheManager;
import android.media.MiniThumbFile;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.provider.Contacts.GroupMembership;
import android.service.voice.VoiceInteractionSession;
import android.util.Log;
import android.util.LogException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import libcore.io.IoUtils;

public final class MediaStore {
    public static final String ACTION_IMAGE_CAPTURE = "android.media.action.IMAGE_CAPTURE";
    public static final String ACTION_IMAGE_CAPTURE_SECURE = "android.media.action.IMAGE_CAPTURE_SECURE";
    public static final String ACTION_MTP_SESSION_END = "android.provider.action.MTP_SESSION_END";
    public static final String ACTION_VIDEO_CAPTURE = "android.media.action.VIDEO_CAPTURE";
    public static final String AUTHORITY = "media";
    private static final String CONTENT_AUTHORITY_SLASH = "content://media/";
    public static final String EXTRA_DURATION_LIMIT = "android.intent.extra.durationLimit";
    public static final String EXTRA_FINISH_ON_COMPLETION = "android.intent.extra.finishOnCompletion";
    public static final String EXTRA_FULL_SCREEN = "android.intent.extra.fullScreen";
    public static final String EXTRA_MEDIA_ALBUM = "android.intent.extra.album";
    public static final String EXTRA_MEDIA_ARTIST = "android.intent.extra.artist";
    public static final String EXTRA_MEDIA_FOCUS = "android.intent.extra.focus";
    public static final String EXTRA_MEDIA_GENRE = "android.intent.extra.genre";
    public static final String EXTRA_MEDIA_PLAYLIST = "android.intent.extra.playlist";
    public static final String EXTRA_MEDIA_RADIO_CHANNEL = "android.intent.extra.radio_channel";
    public static final String EXTRA_MEDIA_TITLE = "android.intent.extra.title";
    public static final String EXTRA_OUTPUT = "output";
    public static final String EXTRA_SCREEN_ORIENTATION = "android.intent.extra.screenOrientation";
    public static final String EXTRA_SHOW_ACTION_ICONS = "android.intent.extra.showActionIcons";
    public static final String EXTRA_SIZE_LIMIT = "android.intent.extra.sizeLimit";
    public static final String EXTRA_VIDEO_QUALITY = "android.intent.extra.videoQuality";
    public static final String INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH = "android.media.action.MEDIA_PLAY_FROM_SEARCH";
    public static final String INTENT_ACTION_MEDIA_SEARCH = "android.intent.action.MEDIA_SEARCH";
    @Deprecated
    public static final String INTENT_ACTION_MUSIC_PLAYER = "android.intent.action.MUSIC_PLAYER";
    public static final String INTENT_ACTION_STILL_IMAGE_CAMERA = "android.media.action.STILL_IMAGE_CAMERA";
    public static final String INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE = "android.media.action.STILL_IMAGE_CAMERA_SECURE";
    public static final String INTENT_ACTION_TEXT_OPEN_FROM_SEARCH = "android.media.action.TEXT_OPEN_FROM_SEARCH";
    public static final String INTENT_ACTION_VIDEO_CAMERA = "android.media.action.VIDEO_CAMERA";
    public static final String INTENT_ACTION_VIDEO_PLAY_FROM_SEARCH = "android.media.action.VIDEO_PLAY_FROM_SEARCH";
    public static final String MEDIA_IGNORE_FILENAME = ".nomedia";
    public static final String MEDIA_SCANNER_VOLUME = "volume";
    public static final String META_DATA_STILL_IMAGE_CAMERA_PREWARM_SERVICE = "android.media.still_image_camera_preview_service";
    public static final String PARAM_DELETE_DATA = "deletedata";
    private static final String TAG = "MediaStore";
    public static final String UNHIDE_CALL = "unhide";
    public static final String UNKNOWN_STRING = "<unknown>";

    public interface MediaColumns extends BaseColumns {
        public static final String DATA = "_data";
        public static final String DATE_ADDED = "date_added";
        public static final String DATE_MODIFIED = "date_modified";
        public static final String DISPLAY_NAME = "_display_name";
        public static final String HEIGHT = "height";
        public static final String IS_DRM = "is_drm";
        public static final String MEDIA_SCANNER_NEW_OBJECT_ID = "media_scanner_new_object_id";
        public static final String MIME_TYPE = "mime_type";
        public static final String SIZE = "_size";
        public static final String TITLE = "title";
        public static final String WIDTH = "width";
    }

    public static final class Audio {

        public interface AlbumColumns {
            public static final String ALBUM = "album";
            public static final String ALBUM_ART = "album_art";
            public static final String ALBUM_ID = "album_id";
            public static final String ALBUM_KEY = "album_key";
            public static final String ARTIST = "artist";
            public static final String FIRST_YEAR = "minyear";
            public static final String LAST_YEAR = "maxyear";
            public static final String NUMBER_OF_SONGS = "numsongs";
            public static final String NUMBER_OF_SONGS_FOR_ARTIST = "numsongs_by_artist";
        }

        public static final class Albums implements BaseColumns, AlbumColumns {
            public static final String CONTENT_TYPE = "vnd.android.cursor.dir/albums";
            public static final String DEFAULT_SORT_ORDER = "album_key";
            public static final String ENTRY_CONTENT_TYPE = "vnd.android.cursor.item/album";
            public static final Uri EXTERNAL_CONTENT_URI = getContentUri("external");
            public static final Uri INTERNAL_CONTENT_URI = getContentUri("internal");

            public static Uri getContentUri(String volumeName) {
                return Uri.parse(MediaStore.CONTENT_AUTHORITY_SLASH + volumeName + "/audio/albums");
            }
        }

        public interface ArtistColumns {
            public static final String ARTIST = "artist";
            public static final String ARTIST_KEY = "artist_key";
            public static final String NUMBER_OF_ALBUMS = "number_of_albums";
            public static final String NUMBER_OF_TRACKS = "number_of_tracks";
        }

        public static final class Artists implements BaseColumns, ArtistColumns {
            public static final String CONTENT_TYPE = "vnd.android.cursor.dir/artists";
            public static final String DEFAULT_SORT_ORDER = "artist_key";
            public static final String ENTRY_CONTENT_TYPE = "vnd.android.cursor.item/artist";
            public static final Uri EXTERNAL_CONTENT_URI = getContentUri("external");
            public static final Uri INTERNAL_CONTENT_URI = getContentUri("internal");

            public static final class Albums implements AlbumColumns {
                public static final Uri getContentUri(String volumeName, long artistId) {
                    return Uri.parse(MediaStore.CONTENT_AUTHORITY_SLASH + volumeName + "/audio/artists/" + artistId + "/albums");
                }
            }

            public static Uri getContentUri(String volumeName) {
                return Uri.parse(MediaStore.CONTENT_AUTHORITY_SLASH + volumeName + "/audio/artists");
            }
        }

        public interface AudioColumns extends MediaColumns {
            public static final String ALBUM = "album";
            public static final String ALBUM_ARTIST = "album_artist";
            public static final String ALBUM_ID = "album_id";
            public static final String ALBUM_KEY = "album_key";
            public static final String ARTIST = "artist";
            public static final String ARTIST_ID = "artist_id";
            public static final String ARTIST_KEY = "artist_key";
            public static final String BOOKMARK = "bookmark";
            public static final String COMPILATION = "compilation";
            public static final String COMPOSER = "composer";
            public static final String DURATION = "duration";
            public static final String GENRE = "genre";
            public static final String IS_ALARM = "is_alarm";
            public static final String IS_MUSIC = "is_music";
            public static final String IS_NOTIFICATION = "is_notification";
            public static final String IS_PODCAST = "is_podcast";
            public static final String IS_RINGTONE = "is_ringtone";
            public static final String TITLE_KEY = "title_key";
            public static final String TRACK = "track";
            public static final String YEAR = "year";
        }

        public interface GenresColumns {
            public static final String NAME = "name";
        }

        public static final class Genres implements BaseColumns, GenresColumns {
            public static final String CONTENT_TYPE = "vnd.android.cursor.dir/genre";
            public static final String DEFAULT_SORT_ORDER = "name";
            public static final String ENTRY_CONTENT_TYPE = "vnd.android.cursor.item/genre";
            public static final Uri EXTERNAL_CONTENT_URI = getContentUri("external");
            public static final Uri INTERNAL_CONTENT_URI = getContentUri("internal");

            public static final class Members implements AudioColumns {
                public static final String AUDIO_ID = "audio_id";
                public static final String CONTENT_DIRECTORY = "members";
                public static final String DEFAULT_SORT_ORDER = "title_key";
                public static final String GENRE_ID = "genre_id";

                public static final Uri getContentUri(String volumeName, long genreId) {
                    return Uri.parse(MediaStore.CONTENT_AUTHORITY_SLASH + volumeName + "/audio/genres/" + genreId + "/members");
                }
            }

            public static Uri getContentUri(String volumeName) {
                return Uri.parse(MediaStore.CONTENT_AUTHORITY_SLASH + volumeName + "/audio/genres");
            }

            public static Uri getContentUriForAudioId(String volumeName, int audioId) {
                return Uri.parse(MediaStore.CONTENT_AUTHORITY_SLASH + volumeName + "/audio/media/" + audioId + "/genres");
            }
        }

        public static final class Media implements AudioColumns {
            public static final String CONTENT_TYPE = "vnd.android.cursor.dir/audio";
            public static final String DEFAULT_SORT_ORDER = "title_key";
            public static final String ENTRY_CONTENT_TYPE = "vnd.android.cursor.item/audio";
            public static final Uri EXTERNAL_CONTENT_URI = getContentUri("external");
            private static final String[] EXTERNAL_PATHS;
            public static final String EXTRA_MAX_BYTES = "android.provider.MediaStore.extra.MAX_BYTES";
            public static final Uri INTERNAL_CONTENT_URI = getContentUri("internal");
            public static final String RECORD_SOUND_ACTION = "android.provider.MediaStore.RECORD_SOUND";

            static {
                String secondary_storage = System.getenv("SECONDARY_STORAGE");
                if (secondary_storage != null) {
                    EXTERNAL_PATHS = secondary_storage.split(SettingsStringUtil.DELIMITER);
                } else {
                    EXTERNAL_PATHS = new String[0];
                }
            }

            public static Uri getContentUri(String volumeName) {
                return Uri.parse(MediaStore.CONTENT_AUTHORITY_SLASH + volumeName + "/audio/media");
            }

            public static Uri getContentUriForPath(String path) {
                for (String ep : EXTERNAL_PATHS) {
                    if (path.startsWith(ep)) {
                        return EXTERNAL_CONTENT_URI;
                    }
                }
                return path.startsWith(Environment.getExternalStorageDirectory().getPath()) ? EXTERNAL_CONTENT_URI : INTERNAL_CONTENT_URI;
            }
        }

        public interface PlaylistsColumns {
            public static final String DATA = "_data";
            public static final String DATE_ADDED = "date_added";
            public static final String DATE_MODIFIED = "date_modified";
            public static final String NAME = "name";
        }

        public static final class Playlists implements BaseColumns, PlaylistsColumns {
            public static final String CONTENT_TYPE = "vnd.android.cursor.dir/playlist";
            public static final String DEFAULT_SORT_ORDER = "name";
            public static final String ENTRY_CONTENT_TYPE = "vnd.android.cursor.item/playlist";
            public static final Uri EXTERNAL_CONTENT_URI = getContentUri("external");
            public static final Uri INTERNAL_CONTENT_URI = getContentUri("internal");

            public static final class Members implements AudioColumns {
                public static final String AUDIO_ID = "audio_id";
                public static final String CONTENT_DIRECTORY = "members";
                public static final String DEFAULT_SORT_ORDER = "play_order";
                public static final String PLAYLIST_ID = "playlist_id";
                public static final String PLAY_ORDER = "play_order";
                public static final String _ID = "_id";

                public static final Uri getContentUri(String volumeName, long playlistId) {
                    return Uri.parse(MediaStore.CONTENT_AUTHORITY_SLASH + volumeName + "/audio/playlists/" + playlistId + "/members");
                }

                public static final boolean moveItem(ContentResolver res, long playlistId, int from, int to) {
                    Uri uri = getContentUri("external", playlistId).buildUpon().appendEncodedPath(String.valueOf(from)).appendQueryParameter("move", "true").build();
                    ContentValues values = new ContentValues();
                    values.put("play_order", Integer.valueOf(to));
                    if (res.update(uri, values, null, null) != 0) {
                        return true;
                    }
                    return false;
                }
            }

            public static Uri getContentUri(String volumeName) {
                return Uri.parse(MediaStore.CONTENT_AUTHORITY_SLASH + volumeName + "/audio/playlists");
            }
        }

        public static final class Radio {
            public static final String ENTRY_CONTENT_TYPE = "vnd.android.cursor.item/radio";

            private Radio() {
            }
        }

        public static String keyFor(String name) {
            if (name == null) {
                return null;
            }
            boolean sortfirst = false;
            if (name.equals(MediaStore.UNKNOWN_STRING)) {
                return "\u0001";
            }
            if (name.startsWith("\u0001")) {
                sortfirst = true;
            }
            name = name.trim().toLowerCase();
            if (name.startsWith("the ")) {
                name = name.substring(4);
            }
            if (name.startsWith("an ")) {
                name = name.substring(3);
            }
            if (name.startsWith("a ")) {
                name = name.substring(2);
            }
            if (name.endsWith(", the") || name.endsWith(",the") || name.endsWith(", an") || name.endsWith(",an") || name.endsWith(", a") || name.endsWith(",a")) {
                name = name.substring(0, name.lastIndexOf(44));
            }
            name = name.replaceAll("[\\[\\]\\(\\)\"'.,?!]", LogException.NO_VALUE).trim();
            if (name.length() <= 0) {
                return LogException.NO_VALUE;
            }
            StringBuilder b = new StringBuilder();
            b.append('.');
            int nl = name.length();
            for (int i = 0; i < nl; i++) {
                b.append(name.charAt(i));
                b.append('.');
            }
            String key = DatabaseUtils.getCollationKey(b.toString());
            if (sortfirst) {
                key = "\u0001" + key;
            }
            return key;
        }
    }

    public static final class Files {

        public interface FileColumns extends MediaColumns {
            public static final String FORMAT = "format";
            public static final String MEDIA_TYPE = "media_type";
            public static final int MEDIA_TYPE_AUDIO = 2;
            public static final int MEDIA_TYPE_IMAGE = 1;
            public static final int MEDIA_TYPE_NONE = 0;
            public static final int MEDIA_TYPE_PLAYLIST = 4;
            public static final int MEDIA_TYPE_VIDEO = 3;
            public static final String MIME_TYPE = "mime_type";
            public static final String PARENT = "parent";
            public static final String STORAGE_ID = "storage_id";
            public static final String TITLE = "title";
        }

        public static Uri getContentUri(String volumeName) {
            return Uri.parse(MediaStore.CONTENT_AUTHORITY_SLASH + volumeName + "/file");
        }

        public static final Uri getContentUri(String volumeName, long rowId) {
            return Uri.parse(MediaStore.CONTENT_AUTHORITY_SLASH + volumeName + "/file/" + rowId);
        }

        public static Uri getMtpObjectsUri(String volumeName) {
            return Uri.parse(MediaStore.CONTENT_AUTHORITY_SLASH + volumeName + "/object");
        }

        public static final Uri getMtpObjectsUri(String volumeName, long fileId) {
            return Uri.parse(MediaStore.CONTENT_AUTHORITY_SLASH + volumeName + "/object/" + fileId);
        }

        public static final Uri getMtpReferencesUri(String volumeName, long fileId) {
            return Uri.parse(MediaStore.CONTENT_AUTHORITY_SLASH + volumeName + "/object/" + fileId + "/references");
        }

        public static final Uri getDirectoryUri(String volumeName) {
            return Uri.parse(MediaStore.CONTENT_AUTHORITY_SLASH + volumeName + "/dir");
        }
    }

    public static final class Images {

        public interface ImageColumns extends MediaColumns {
            public static final String BUCKET_DISPLAY_NAME = "bucket_display_name";
            public static final String BUCKET_ID = "bucket_id";
            public static final String DATE_TAKEN = "datetaken";
            public static final String DESCRIPTION = "description";
            public static final String IS_HDR = "is_hdr";
            public static final String IS_PRIVATE = "isprivate";
            public static final String LATITUDE = "latitude";
            public static final String LONGITUDE = "longitude";
            public static final String MINI_THUMB_MAGIC = "mini_thumb_magic";
            public static final String ORIENTATION = "orientation";
            public static final String PICASA_ID = "picasa_id";
        }

        public static final class Media implements ImageColumns {
            public static final String CONTENT_TYPE = "vnd.android.cursor.dir/image";
            public static final String DEFAULT_SORT_ORDER = "bucket_display_name";
            public static final Uri EXTERNAL_CONTENT_URI = getContentUri("external");
            public static final Uri INTERNAL_CONTENT_URI = getContentUri("internal");

            public static final Cursor query(ContentResolver cr, Uri uri, String[] projection) {
                return cr.query(uri, projection, null, null, "bucket_display_name");
            }

            public static final Cursor query(ContentResolver cr, Uri uri, String[] projection, String where, String orderBy) {
                String str;
                if (orderBy == null) {
                    str = "bucket_display_name";
                } else {
                    str = orderBy;
                }
                return cr.query(uri, projection, where, null, str);
            }

            public static final Cursor query(ContentResolver cr, Uri uri, String[] projection, String selection, String[] selectionArgs, String orderBy) {
                String str;
                if (orderBy == null) {
                    str = "bucket_display_name";
                } else {
                    str = orderBy;
                }
                return cr.query(uri, projection, selection, selectionArgs, str);
            }

            public static final Bitmap getBitmap(ContentResolver cr, Uri url) throws FileNotFoundException, IOException {
                InputStream input = cr.openInputStream(url);
                Bitmap bitmap = BitmapFactory.decodeStream(input);
                input.close();
                return bitmap;
            }

            public static final String insertImage(ContentResolver cr, String imagePath, String name, String description) throws FileNotFoundException {
                FileInputStream stream = new FileInputStream(imagePath);
                try {
                    Bitmap bm = BitmapFactory.decodeFile(imagePath);
                    String ret = insertImage(cr, bm, name, description);
                    if (bm != null) {
                        bm.recycle();
                    }
                    return ret;
                } finally {
                    try {
                        stream.close();
                    } catch (IOException e) {
                    }
                }
            }

            private static final Bitmap StoreThumbnail(ContentResolver cr, Bitmap source, long id, float width, float height, int kind) {
                Matrix matrix = new Matrix();
                matrix.setScale(width / ((float) source.getWidth()), height / ((float) source.getHeight()));
                Bitmap thumb = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
                ContentValues contentValues = new ContentValues(4);
                contentValues.put("kind", Integer.valueOf(kind));
                contentValues.put("image_id", Integer.valueOf((int) id));
                contentValues.put("height", Integer.valueOf(thumb.getHeight()));
                contentValues.put("width", Integer.valueOf(thumb.getWidth()));
                try {
                    OutputStream thumbOut = cr.openOutputStream(cr.insert(Thumbnails.EXTERNAL_CONTENT_URI, contentValues));
                    thumb.compress(CompressFormat.JPEG, 100, thumbOut);
                    thumbOut.close();
                    return thumb;
                } catch (FileNotFoundException e) {
                    return null;
                } catch (IOException e2) {
                    return null;
                }
            }

            /* JADX WARNING: Removed duplicated region for block: B:23:? A:{SYNTHETIC, RETURN} */
            /* JADX WARNING: Removed duplicated region for block: B:10:0x004e  */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public static final String insertImage(ContentResolver cr, Bitmap source, String title, String description) {
                ContentValues values = new ContentValues();
                values.put("title", title);
                values.put("description", description);
                values.put("mime_type", "image/jpeg");
                Uri url = null;
                OutputStream imageOut;
                try {
                    url = cr.insert(EXTERNAL_CONTENT_URI, values);
                    if (source != null) {
                        imageOut = cr.openOutputStream(url);
                        source.compress(CompressFormat.JPEG, 50, imageOut);
                        imageOut.close();
                        long id = ContentUris.parseId(url);
                        Bitmap StoreThumbnail = StoreThumbnail(cr, Thumbnails.getThumbnail(cr, id, 1, null), id, 50.0f, 50.0f, 3);
                        if (url == null) {
                            return url.toString();
                        }
                        return null;
                    }
                    Log.e(MediaStore.TAG, "Failed to create thumbnail, removing original");
                    cr.delete(url, null, null);
                    url = null;
                    if (url == null) {
                    }
                } catch (Exception e) {
                    Log.e(MediaStore.TAG, "Failed to insert image", e);
                    if (url != null) {
                        cr.delete(url, null, null);
                        url = null;
                    }
                } catch (Throwable th) {
                    imageOut.close();
                }
            }

            public static Uri getContentUri(String volumeName) {
                return Uri.parse(MediaStore.CONTENT_AUTHORITY_SLASH + volumeName + "/images/media");
            }
        }

        public static class Thumbnails implements BaseColumns {
            public static final String DATA = "_data";
            public static final String DEFAULT_SORT_ORDER = "image_id ASC";
            public static final Uri EXTERNAL_CONTENT_URI = getContentUri("external");
            public static final int FULL_SCREEN_KIND = 2;
            public static final String HEIGHT = "height";
            public static final String IMAGE_ID = "image_id";
            public static final Uri INTERNAL_CONTENT_URI = getContentUri("internal");
            public static final String KIND = "kind";
            public static final int MICRO_KIND = 3;
            public static final int MINI_KIND = 1;
            public static final String THUMB_DATA = "thumb_data";
            public static final String WIDTH = "width";

            public static final Cursor query(ContentResolver cr, Uri uri, String[] projection) {
                return cr.query(uri, projection, null, null, DEFAULT_SORT_ORDER);
            }

            public static final Cursor queryMiniThumbnails(ContentResolver cr, Uri uri, int kind, String[] projection) {
                return cr.query(uri, projection, "kind = " + kind, null, DEFAULT_SORT_ORDER);
            }

            public static final Cursor queryMiniThumbnail(ContentResolver cr, long origId, int kind, String[] projection) {
                return cr.query(EXTERNAL_CONTENT_URI, projection, "image_id = " + origId + " AND " + "kind" + " = " + kind, null, null);
            }

            public static void cancelThumbnailRequest(ContentResolver cr, long origId) {
                InternalThumbnails.cancelThumbnailRequest(cr, origId, EXTERNAL_CONTENT_URI, 0);
            }

            public static Bitmap getThumbnail(ContentResolver cr, long origId, int kind, Options options) {
                return InternalThumbnails.getThumbnail(cr, origId, 0, kind, options, EXTERNAL_CONTENT_URI, false);
            }

            public static void cancelThumbnailRequest(ContentResolver cr, long origId, long groupId) {
                InternalThumbnails.cancelThumbnailRequest(cr, origId, EXTERNAL_CONTENT_URI, groupId);
            }

            public static Bitmap getThumbnail(ContentResolver cr, long origId, long groupId, int kind, Options options) {
                return InternalThumbnails.getThumbnail(cr, origId, groupId, kind, options, EXTERNAL_CONTENT_URI, false);
            }

            public static Uri getContentUri(String volumeName) {
                return Uri.parse(MediaStore.CONTENT_AUTHORITY_SLASH + volumeName + "/images/thumbnails");
            }
        }
    }

    private static class InternalThumbnails implements BaseColumns {
        static final int DEFAULT_GROUP_ID = 0;
        private static final int FULL_SCREEN_KIND = 2;
        private static final int MICRO_KIND = 3;
        private static final int MINI_KIND = 1;
        private static final String[] PROJECTION = new String[]{"_id", "_data"};
        private static byte[] sThumbBuf;
        private static final Object sThumbBufLock = new Object();

        private InternalThumbnails() {
        }

        private static Bitmap getMiniThumbFromFile(Cursor c, Uri baseUri, ContentResolver cr, Options options) {
            Bitmap bitmap = null;
            Object obj = null;
            try {
                long thumbId = c.getLong(0);
                String filePath = c.getString(1);
                obj = ContentUris.withAppendedId(baseUri, thumbId);
                ParcelFileDescriptor pfdInput = cr.openFileDescriptor(obj, "r");
                bitmap = BitmapFactory.decodeFileDescriptor(pfdInput.getFileDescriptor(), null, options);
                pfdInput.close();
                return bitmap;
            } catch (FileNotFoundException ex) {
                Log.e(MediaStore.TAG, "couldn't open thumbnail " + obj + "; " + ex);
                return bitmap;
            } catch (IOException ex2) {
                Log.e(MediaStore.TAG, "couldn't open thumbnail " + obj + "; " + ex2);
                return bitmap;
            } catch (OutOfMemoryError ex3) {
                Log.e(MediaStore.TAG, "failed to allocate memory for thumbnail " + obj + "; " + ex3);
                return bitmap;
            }
        }

        static void cancelThumbnailRequest(ContentResolver cr, long origId, Uri baseUri, long groupId) {
            Cursor c = cr.query(baseUri.buildUpon().appendQueryParameter("cancel", "1").appendQueryParameter("orig_id", String.valueOf(origId)).appendQueryParameter(GroupMembership.GROUP_ID, String.valueOf(groupId)).build(), PROJECTION, null, null, null);
            if (c != null) {
                c.close();
            }
        }

        static Bitmap getThumbnail(ContentResolver cr, long origId, long groupId, int kind, Options options, Uri baseUri, boolean isVideo) {
            Cursor cursor;
            Bitmap bitmap = null;
            if (HwGalleryCacheManager.isGalleryCacheEffect() && 1 == kind && isVideo && origId <= 2147483647L) {
                cursor = null;
                try {
                    cursor = cr.query(Uri.parse(baseUri.buildUpon().appendPath(String.valueOf(origId)).toString().replaceFirst("thumbnails", MediaStore.AUTHORITY)), new String[]{"_id", "_data", "date_modified"}, null, null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        bitmap = HwGalleryCacheManager.getGalleryCachedVideo((int) origId, cursor.getLong(cursor.getColumnIndexOrThrow("date_modified")), options);
                        if (bitmap != null) {
                            if (cursor != null) {
                                cursor.close();
                            }
                            return bitmap;
                        }
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                } catch (SQLiteException e) {
                    Log.w(MediaStore.TAG, "sqlite exception");
                    if (cursor != null) {
                        cursor.close();
                    }
                } catch (Throwable th) {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
                Log.w(MediaStore.TAG, "getThumbnail for video from kvdb faild!");
            }
            MiniThumbFile thumbFile = MiniThumbFile.instance(isVideo ? Media.EXTERNAL_CONTENT_URI : Media.EXTERNAL_CONTENT_URI);
            cursor = null;
            try {
                if (thumbFile.getMagic(origId) != 0) {
                    if (kind == 3) {
                        synchronized (sThumbBufLock) {
                            if (sThumbBuf == null) {
                                sThumbBuf = new byte[10000];
                            }
                            if (thumbFile.getMiniThumbFromFile(origId, sThumbBuf) != null) {
                                bitmap = BitmapFactory.decodeByteArray(sThumbBuf, 0, sThumbBuf.length);
                                if (bitmap == null) {
                                    Log.w(MediaStore.TAG, "couldn't decode byte array.");
                                }
                            }
                        }
                        thumbFile.deactivate();
                        return bitmap;
                    } else if (kind == 1) {
                        cursor = cr.query(baseUri, PROJECTION, (isVideo ? "video_id=" : "image_id=") + origId, null, null);
                        if (cursor != null && cursor.moveToFirst()) {
                            bitmap = getMiniThumbFromFile(cursor, baseUri, cr, options);
                            if (bitmap != null) {
                                if (cursor != null) {
                                    cursor.close();
                                }
                                thumbFile.deactivate();
                                return bitmap;
                            }
                        }
                    }
                }
                Uri blockingUri = baseUri.buildUpon().appendQueryParameter("blocking", "1").appendQueryParameter("orig_id", String.valueOf(origId)).appendQueryParameter(GroupMembership.GROUP_ID, String.valueOf(groupId)).build();
                if (cursor != null) {
                    cursor.close();
                }
                cursor = cr.query(blockingUri, PROJECTION, null, null, null);
                if (cursor == null) {
                    if (cursor != null) {
                        cursor.close();
                    }
                    thumbFile.deactivate();
                    return null;
                }
                if (kind == 3) {
                    synchronized (sThumbBufLock) {
                        if (sThumbBuf == null) {
                            sThumbBuf = new byte[10000];
                        }
                        Arrays.fill(sThumbBuf, (byte) 0);
                        if (thumbFile.getMiniThumbFromFile(origId, sThumbBuf) != null) {
                            bitmap = BitmapFactory.decodeByteArray(sThumbBuf, 0, sThumbBuf.length);
                            if (bitmap == null) {
                                Log.w(MediaStore.TAG, "couldn't decode byte array.");
                            }
                        }
                    }
                } else if (kind != 1) {
                    throw new IllegalArgumentException("Unsupported kind: " + kind);
                } else if (cursor.moveToFirst()) {
                    bitmap = getMiniThumbFromFile(cursor, baseUri, cr, options);
                }
                if (bitmap == null) {
                    Log.v(MediaStore.TAG, "Create the thumbnail in memory: origId=" + origId + ", kind=" + kind + ", isVideo=" + isVideo);
                    Uri uri = Uri.parse(baseUri.buildUpon().appendPath(String.valueOf(origId)).toString().replaceFirst("thumbnails", MediaStore.AUTHORITY));
                    if (cursor != null) {
                        cursor.close();
                    }
                    cursor = cr.query(uri, PROJECTION, null, null, null);
                    if (cursor == null || (cursor.moveToFirst() ^ 1) != 0) {
                        if (cursor != null) {
                            cursor.close();
                        }
                        thumbFile.deactivate();
                        return null;
                    }
                    String filePath = cursor.getString(1);
                    if (filePath != null) {
                        if (isVideo) {
                            bitmap = ThumbnailUtils.createVideoThumbnail(filePath, kind);
                        } else {
                            bitmap = ThumbnailUtils.createImageThumbnail(filePath, kind);
                        }
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
                thumbFile.deactivate();
                return bitmap;
            } catch (Throwable ex) {
                try {
                    Log.w(MediaStore.TAG, ex);
                    if (cursor != null) {
                        cursor.close();
                    }
                    thumbFile.deactivate();
                } catch (Throwable th2) {
                    if (cursor != null) {
                        cursor.close();
                    }
                    thumbFile.deactivate();
                }
            }
        }
    }

    public static final class Video {
        public static final String DEFAULT_SORT_ORDER = "_display_name";

        public interface VideoColumns extends MediaColumns {
            public static final String ALBUM = "album";
            public static final String ARTIST = "artist";
            public static final String BOOKMARK = "bookmark";
            public static final String BUCKET_DISPLAY_NAME = "bucket_display_name";
            public static final String BUCKET_ID = "bucket_id";
            public static final String CATEGORY = "category";
            public static final String DATE_TAKEN = "datetaken";
            public static final String DESCRIPTION = "description";
            public static final String DURATION = "duration";
            public static final String IS_PRIVATE = "isprivate";
            public static final String LANGUAGE = "language";
            public static final String LATITUDE = "latitude";
            public static final String LONGITUDE = "longitude";
            public static final String MINI_THUMB_MAGIC = "mini_thumb_magic";
            public static final String RESOLUTION = "resolution";
            public static final String TAGS = "tags";
        }

        public static final class Media implements VideoColumns {
            public static final String CONTENT_TYPE = "vnd.android.cursor.dir/video";
            public static final String DEFAULT_SORT_ORDER = "title";
            public static final Uri EXTERNAL_CONTENT_URI = getContentUri("external");
            public static final Uri INTERNAL_CONTENT_URI = getContentUri("internal");

            public static Uri getContentUri(String volumeName) {
                return Uri.parse(MediaStore.CONTENT_AUTHORITY_SLASH + volumeName + "/video/media");
            }
        }

        public static class Thumbnails implements BaseColumns {
            public static final String DATA = "_data";
            public static final String DEFAULT_SORT_ORDER = "video_id ASC";
            public static final Uri EXTERNAL_CONTENT_URI = getContentUri("external");
            public static final int FULL_SCREEN_KIND = 2;
            public static final String HEIGHT = "height";
            public static final Uri INTERNAL_CONTENT_URI = getContentUri("internal");
            public static final String KIND = "kind";
            public static final int MICRO_KIND = 3;
            public static final int MINI_KIND = 1;
            public static final String VIDEO_ID = "video_id";
            public static final String WIDTH = "width";

            public static void cancelThumbnailRequest(ContentResolver cr, long origId) {
                InternalThumbnails.cancelThumbnailRequest(cr, origId, EXTERNAL_CONTENT_URI, 0);
            }

            public static Bitmap getThumbnail(ContentResolver cr, long origId, int kind, Options options) {
                return InternalThumbnails.getThumbnail(cr, origId, 0, kind, options, EXTERNAL_CONTENT_URI, true);
            }

            public static Bitmap getThumbnail(ContentResolver cr, long origId, long groupId, int kind, Options options) {
                return InternalThumbnails.getThumbnail(cr, origId, groupId, kind, options, EXTERNAL_CONTENT_URI, true);
            }

            public static void cancelThumbnailRequest(ContentResolver cr, long origId, long groupId) {
                InternalThumbnails.cancelThumbnailRequest(cr, origId, EXTERNAL_CONTENT_URI, groupId);
            }

            public static Uri getContentUri(String volumeName) {
                return Uri.parse(MediaStore.CONTENT_AUTHORITY_SLASH + volumeName + "/video/thumbnails");
            }
        }

        public static final Cursor query(ContentResolver cr, Uri uri, String[] projection) {
            return cr.query(uri, projection, null, null, "_display_name");
        }
    }

    public static Uri getMediaScannerUri() {
        return Uri.parse("content://media/none/media_scanner");
    }

    public static String getVersion(Context context) {
        Cursor c = context.getContentResolver().query(Uri.parse("content://media/none/version"), null, null, null, null);
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    String string = c.getString(0);
                    return string;
                }
                c.close();
            } finally {
                c.close();
            }
        }
        return null;
    }

    public static Uri getDocumentUri(Context context, Uri mediaUri) {
        try {
            ContentResolver resolver = context.getContentResolver();
            return getDocumentUri(resolver, getFilePath(resolver, mediaUri), resolver.getPersistedUriPermissions());
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    /* JADX WARNING: Missing block: B:15:0x0041, code:
            r2 = move-exception;
     */
    /* JADX WARNING: Missing block: B:16:0x0042, code:
            r9 = r2;
            r2 = r1;
            r1 = r9;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static String getFilePath(ContentResolver resolver, Uri mediaUri) throws RemoteException {
        Throwable th = null;
        ContentProviderClient contentProviderClient = null;
        Cursor c;
        try {
            contentProviderClient = resolver.acquireUnstableContentProviderClient(AUTHORITY);
            c = contentProviderClient.query(mediaUri, new String[]{"_data"}, null, null, null);
            if (c.getCount() == 0) {
                throw new IllegalStateException("Not found media file under URI: " + mediaUri);
            } else if (c.moveToFirst()) {
                String path = c.getString(0);
                IoUtils.closeQuietly(c);
                if (contentProviderClient != null) {
                    try {
                        contentProviderClient.close();
                    } catch (Throwable th2) {
                        th = th2;
                    }
                }
                if (th == null) {
                    return path;
                }
                throw th;
            } else {
                throw new IllegalStateException("Failed to move cursor to the first item.");
            }
        } catch (Throwable th3) {
            Throwable th4 = th3;
            Throwable th5 = null;
            if (contentProviderClient != null) {
                try {
                    contentProviderClient.close();
                } catch (Throwable th6) {
                    if (th5 == null) {
                        th5 = th6;
                    } else if (th5 != th6) {
                        th5.addSuppressed(th6);
                    }
                }
            }
            if (th5 != null) {
                throw th5;
            }
            throw th4;
        }
    }

    private static Uri getDocumentUri(ContentResolver resolver, String path, List<UriPermission> uriPermissions) throws RemoteException {
        Throwable th;
        Throwable th2 = null;
        ContentProviderClient contentProviderClient = null;
        try {
            contentProviderClient = resolver.acquireUnstableContentProviderClient(DocumentsContract.EXTERNAL_STORAGE_PROVIDER_AUTHORITY);
            Bundle in = new Bundle();
            in.putParcelableList("com.android.externalstorage.documents.extra.uriPermissions", uriPermissions);
            Uri uri = (Uri) contentProviderClient.call("getDocumentId", path, in).getParcelable("uri");
            if (contentProviderClient != null) {
                try {
                    contentProviderClient.close();
                } catch (Throwable th3) {
                    th2 = th3;
                }
            }
            if (th2 == null) {
                return uri;
            }
            throw th2;
        } catch (Throwable th22) {
            Throwable th4 = th22;
            th22 = th;
            th = th4;
        }
        if (contentProviderClient != null) {
            try {
                contentProviderClient.close();
            } catch (Throwable th5) {
                if (th22 == null) {
                    th22 = th5;
                } else if (th22 != th5) {
                    th22.addSuppressed(th5);
                }
            }
        }
        if (th22 != null) {
            throw th22;
        }
        throw th;
    }

    public static String getPath(Context context, Uri uri) {
        if (DocumentsContract.isDocumentUri(context, uri)) {
            String[] split;
            if (isExternalStorageDocument(uri)) {
                split = DocumentsContract.getDocumentId(uri).split(SettingsStringUtil.DELIMITER);
                if ("primary".equalsIgnoreCase(split[0])) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else if (isDownloadsDocument(uri)) {
                try {
                    return getDataColumn(context, ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.parseLong(DocumentsContract.getDocumentId(uri))), null, null);
                } catch (NumberFormatException e) {
                    Log.w(TAG, "exception get contenturi.");
                    return null;
                }
            } else if (isMediaDocument(uri)) {
                String type = DocumentsContract.getDocumentId(uri).split(SettingsStringUtil.DELIMITER)[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = Media.EXTERNAL_CONTENT_URI;
                }
                String selection = "_id=?";
                return getDataColumn(context, contentUri, "_id=?", new String[]{split[1]});
            }
        } else if (VoiceInteractionSession.KEY_CONTENT.equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        } else {
            if ("file".equalsIgnoreCase(uri.getScheme())) {
                return uri.getPath();
            }
        }
        return null;
    }

    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        String column = "_data";
        try {
            cursor = context.getContentResolver().query(uri, new String[]{"_data"}, selection, selectionArgs, null);
            if (cursor == null || !cursor.moveToFirst()) {
                if (cursor != null) {
                    cursor.close();
                }
                return null;
            }
            String string = cursor.getString(cursor.getColumnIndexOrThrow("_data"));
            if (cursor != null) {
                cursor.close();
            }
            return string;
        } catch (SQLiteException e) {
            Log.w(TAG, "SQLiteException when getDataColumn");
            if (cursor != null) {
                cursor.close();
            }
        } catch (IllegalArgumentException e2) {
            Log.w(TAG, "IllegalArgumentException when getDataColumn");
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return DocumentsContract.EXTERNAL_STORAGE_PROVIDER_AUTHORITY.equals(uri.getAuthority());
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}
