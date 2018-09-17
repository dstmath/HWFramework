package android.media.tv;

import android.content.ComponentName;
import android.content.ContentUris;
import android.net.ConnectivityManager;
import android.net.ProxyInfo;
import android.net.Uri;
import android.net.Uri.Builder;
import android.provider.BaseColumns;
import android.rms.HwSysResource;
import android.service.voice.VoiceInteractionSession;
import android.text.TextUtils;
import android.util.ArraySet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class TvContract {
    public static final String AUTHORITY = "android.media.tv";
    public static final String PARAM_BROWSABLE_ONLY = "browsable_only";
    public static final String PARAM_CANONICAL_GENRE = "canonical_genre";
    public static final String PARAM_CHANNEL = "channel";
    public static final String PARAM_END_TIME = "end_time";
    public static final String PARAM_INPUT = "input";
    public static final String PARAM_START_TIME = "start_time";
    private static final String PATH_CHANNEL = "channel";
    private static final String PATH_PASSTHROUGH = "passthrough";
    private static final String PATH_PROGRAM = "program";
    private static final String PATH_RECORDED_PROGRAM = "recorded_program";
    public static final String PERMISSION_READ_TV_LISTINGS = "android.permission.READ_TV_LISTINGS";

    public interface BaseTvColumns extends BaseColumns {
        public static final String COLUMN_PACKAGE_NAME = "package_name";
    }

    public static final class Channels implements BaseTvColumns {
        public static final String COLUMN_APP_LINK_COLOR = "app_link_color";
        public static final String COLUMN_APP_LINK_ICON_URI = "app_link_icon_uri";
        public static final String COLUMN_APP_LINK_INTENT_URI = "app_link_intent_uri";
        public static final String COLUMN_APP_LINK_POSTER_ART_URI = "app_link_poster_art_uri";
        public static final String COLUMN_APP_LINK_TEXT = "app_link_text";
        public static final String COLUMN_BROWSABLE = "browsable";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_DISPLAY_NAME = "display_name";
        public static final String COLUMN_DISPLAY_NUMBER = "display_number";
        public static final String COLUMN_INPUT_ID = "input_id";
        public static final String COLUMN_INTERNAL_PROVIDER_DATA = "internal_provider_data";
        public static final String COLUMN_INTERNAL_PROVIDER_FLAG1 = "internal_provider_flag1";
        public static final String COLUMN_INTERNAL_PROVIDER_FLAG2 = "internal_provider_flag2";
        public static final String COLUMN_INTERNAL_PROVIDER_FLAG3 = "internal_provider_flag3";
        public static final String COLUMN_INTERNAL_PROVIDER_FLAG4 = "internal_provider_flag4";
        public static final String COLUMN_LOCKED = "locked";
        public static final String COLUMN_NETWORK_AFFILIATION = "network_affiliation";
        public static final String COLUMN_ORIGINAL_NETWORK_ID = "original_network_id";
        public static final String COLUMN_SEARCHABLE = "searchable";
        public static final String COLUMN_SERVICE_ID = "service_id";
        public static final String COLUMN_SERVICE_TYPE = "service_type";
        public static final String COLUMN_TRANSPORT_STREAM_ID = "transport_stream_id";
        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_VERSION_NUMBER = "version_number";
        public static final String COLUMN_VIDEO_FORMAT = "video_format";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/channel";
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/channel";
        public static final Uri CONTENT_URI = null;
        public static final String SERVICE_TYPE_AUDIO = "SERVICE_TYPE_AUDIO";
        public static final String SERVICE_TYPE_AUDIO_VIDEO = "SERVICE_TYPE_AUDIO_VIDEO";
        public static final String SERVICE_TYPE_OTHER = "SERVICE_TYPE_OTHER";
        public static final String TYPE_1SEG = "TYPE_1SEG";
        public static final String TYPE_ATSC_C = "TYPE_ATSC_C";
        public static final String TYPE_ATSC_M_H = "TYPE_ATSC_M_H";
        public static final String TYPE_ATSC_T = "TYPE_ATSC_T";
        public static final String TYPE_CMMB = "TYPE_CMMB";
        public static final String TYPE_DTMB = "TYPE_DTMB";
        public static final String TYPE_DVB_C = "TYPE_DVB_C";
        public static final String TYPE_DVB_C2 = "TYPE_DVB_C2";
        public static final String TYPE_DVB_H = "TYPE_DVB_H";
        public static final String TYPE_DVB_S = "TYPE_DVB_S";
        public static final String TYPE_DVB_S2 = "TYPE_DVB_S2";
        public static final String TYPE_DVB_SH = "TYPE_DVB_SH";
        public static final String TYPE_DVB_T = "TYPE_DVB_T";
        public static final String TYPE_DVB_T2 = "TYPE_DVB_T2";
        public static final String TYPE_ISDB_C = "TYPE_ISDB_C";
        public static final String TYPE_ISDB_S = "TYPE_ISDB_S";
        public static final String TYPE_ISDB_T = "TYPE_ISDB_T";
        public static final String TYPE_ISDB_TB = "TYPE_ISDB_TB";
        public static final String TYPE_NTSC = "TYPE_NTSC";
        public static final String TYPE_OTHER = "TYPE_OTHER";
        public static final String TYPE_PAL = "TYPE_PAL";
        public static final String TYPE_SECAM = "TYPE_SECAM";
        public static final String TYPE_S_DMB = "TYPE_S_DMB";
        public static final String TYPE_T_DMB = "TYPE_T_DMB";
        public static final String VIDEO_FORMAT_1080I = "VIDEO_FORMAT_1080I";
        public static final String VIDEO_FORMAT_1080P = "VIDEO_FORMAT_1080P";
        public static final String VIDEO_FORMAT_2160P = "VIDEO_FORMAT_2160P";
        public static final String VIDEO_FORMAT_240P = "VIDEO_FORMAT_240P";
        public static final String VIDEO_FORMAT_360P = "VIDEO_FORMAT_360P";
        public static final String VIDEO_FORMAT_4320P = "VIDEO_FORMAT_4320P";
        public static final String VIDEO_FORMAT_480I = "VIDEO_FORMAT_480I";
        public static final String VIDEO_FORMAT_480P = "VIDEO_FORMAT_480P";
        public static final String VIDEO_FORMAT_576I = "VIDEO_FORMAT_576I";
        public static final String VIDEO_FORMAT_576P = "VIDEO_FORMAT_576P";
        public static final String VIDEO_FORMAT_720P = "VIDEO_FORMAT_720P";
        private static final Map<String, String> VIDEO_FORMAT_TO_RESOLUTION_MAP = null;
        public static final String VIDEO_RESOLUTION_ED = "VIDEO_RESOLUTION_ED";
        public static final String VIDEO_RESOLUTION_FHD = "VIDEO_RESOLUTION_FHD";
        public static final String VIDEO_RESOLUTION_HD = "VIDEO_RESOLUTION_HD";
        public static final String VIDEO_RESOLUTION_SD = "VIDEO_RESOLUTION_SD";
        public static final String VIDEO_RESOLUTION_UHD = "VIDEO_RESOLUTION_UHD";

        public static final class Logo {
            public static final String CONTENT_DIRECTORY = "logo";

            private Logo() {
            }
        }

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.tv.TvContract.Channels.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.tv.TvContract.Channels.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.tv.TvContract.Channels.<clinit>():void");
        }

        public static final String getVideoResolution(String videoFormat) {
            return (String) VIDEO_FORMAT_TO_RESOLUTION_MAP.get(videoFormat);
        }

        private Channels() {
        }
    }

    public static final class Programs implements BaseTvColumns {
        public static final String COLUMN_AUDIO_LANGUAGE = "audio_language";
        public static final String COLUMN_BROADCAST_GENRE = "broadcast_genre";
        public static final String COLUMN_CANONICAL_GENRE = "canonical_genre";
        public static final String COLUMN_CHANNEL_ID = "channel_id";
        public static final String COLUMN_CONTENT_RATING = "content_rating";
        public static final String COLUMN_END_TIME_UTC_MILLIS = "end_time_utc_millis";
        public static final String COLUMN_EPISODE_DISPLAY_NUMBER = "episode_display_number";
        @Deprecated
        public static final String COLUMN_EPISODE_NUMBER = "episode_number";
        public static final String COLUMN_EPISODE_TITLE = "episode_title";
        public static final String COLUMN_INTERNAL_PROVIDER_DATA = "internal_provider_data";
        public static final String COLUMN_INTERNAL_PROVIDER_FLAG1 = "internal_provider_flag1";
        public static final String COLUMN_INTERNAL_PROVIDER_FLAG2 = "internal_provider_flag2";
        public static final String COLUMN_INTERNAL_PROVIDER_FLAG3 = "internal_provider_flag3";
        public static final String COLUMN_INTERNAL_PROVIDER_FLAG4 = "internal_provider_flag4";
        public static final String COLUMN_LONG_DESCRIPTION = "long_description";
        public static final String COLUMN_POSTER_ART_URI = "poster_art_uri";
        public static final String COLUMN_RECORDING_PROHIBITED = "recording_prohibited";
        public static final String COLUMN_SEARCHABLE = "searchable";
        public static final String COLUMN_SEASON_DISPLAY_NUMBER = "season_display_number";
        @Deprecated
        public static final String COLUMN_SEASON_NUMBER = "season_number";
        public static final String COLUMN_SEASON_TITLE = "season_title";
        public static final String COLUMN_SHORT_DESCRIPTION = "short_description";
        public static final String COLUMN_START_TIME_UTC_MILLIS = "start_time_utc_millis";
        public static final String COLUMN_THUMBNAIL_URI = "thumbnail_uri";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_VERSION_NUMBER = "version_number";
        public static final String COLUMN_VIDEO_HEIGHT = "video_height";
        public static final String COLUMN_VIDEO_WIDTH = "video_width";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/program";
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/program";
        public static final Uri CONTENT_URI = null;

        public static final class Genres {
            public static final String ANIMAL_WILDLIFE = "ANIMAL_WILDLIFE";
            public static final String ARTS = "ARTS";
            private static final ArraySet<String> CANONICAL_GENRES = null;
            public static final String COMEDY = "COMEDY";
            private static final char COMMA = ',';
            private static final String DELIMITER = ",";
            private static final char DOUBLE_QUOTE = '\"';
            public static final String DRAMA = "DRAMA";
            public static final String EDUCATION = "EDUCATION";
            private static final String[] EMPTY_STRING_ARRAY = null;
            public static final String ENTERTAINMENT = "ENTERTAINMENT";
            public static final String FAMILY_KIDS = "FAMILY_KIDS";
            public static final String GAMING = "GAMING";
            public static final String LIFE_STYLE = "LIFE_STYLE";
            public static final String MOVIES = "MOVIES";
            public static final String MUSIC = "MUSIC";
            public static final String NEWS = "NEWS";
            public static final String PREMIER = "PREMIER";
            public static final String SHOPPING = "SHOPPING";
            public static final String SPORTS = "SPORTS";
            public static final String TECH_SCIENCE = "TECH_SCIENCE";
            public static final String TRAVEL = "TRAVEL";

            static {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.tv.TvContract.Programs.Genres.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.tv.TvContract.Programs.Genres.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 9 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 10 more
*/
                /*
                // Can't load method instructions.
                */
                throw new UnsupportedOperationException("Method not decompiled: android.media.tv.TvContract.Programs.Genres.<clinit>():void");
            }

            private Genres() {
            }

            public static String encode(String... genres) {
                if (genres == null) {
                    return null;
                }
                StringBuilder sb = new StringBuilder();
                String separator = ProxyInfo.LOCAL_EXCL_LIST;
                for (String genre : genres) {
                    sb.append(separator).append(encodeToCsv(genre));
                    separator = DELIMITER;
                }
                return sb.toString();
            }

            private static String encodeToCsv(String genre) {
                StringBuilder sb = new StringBuilder();
                int length = genre.length();
                for (int i = 0; i < length; i++) {
                    char c = genre.charAt(i);
                    switch (c) {
                        case HwSysResource.APPMNGWHITELIST /*34*/:
                            sb.append(DOUBLE_QUOTE);
                            break;
                        case ConnectivityManager.TYPE_MOBILE_BIP6 /*44*/:
                            sb.append(DOUBLE_QUOTE);
                            break;
                        default:
                            break;
                    }
                    sb.append(c);
                }
                return sb.toString();
            }

            /* JADX WARNING: inconsistent code. */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public static String[] decode(String genres) {
                if (TextUtils.isEmpty(genres)) {
                    return EMPTY_STRING_ARRAY;
                }
                if (genres.indexOf(44) == -1 && genres.indexOf(34) == -1) {
                    return new String[]{genres.trim()};
                }
                String string;
                StringBuilder sb = new StringBuilder();
                List<String> results = new ArrayList();
                int length = genres.length();
                boolean escape = false;
                for (int i = 0; i < length; i++) {
                    char c = genres.charAt(i);
                    switch (c) {
                        case HwSysResource.APPMNGWHITELIST /*34*/:
                            if (!escape) {
                                escape = true;
                                break;
                            }
                        case ConnectivityManager.TYPE_MOBILE_BIP6 /*44*/:
                            if (!escape) {
                                string = sb.toString().trim();
                                if (string.length() > 0) {
                                    results.add(string);
                                }
                                sb = new StringBuilder();
                                break;
                            }
                        default:
                            sb.append(c);
                            escape = false;
                            break;
                    }
                }
                string = sb.toString().trim();
                if (string.length() > 0) {
                    results.add(string);
                }
                return (String[]) results.toArray(new String[results.size()]);
            }

            public static boolean isCanonical(String genre) {
                return CANONICAL_GENRES.contains(genre);
            }
        }

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.tv.TvContract.Programs.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.tv.TvContract.Programs.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.tv.TvContract.Programs.<clinit>():void");
        }

        private Programs() {
        }
    }

    public static final class RecordedPrograms implements BaseTvColumns {
        public static final String COLUMN_AUDIO_LANGUAGE = "audio_language";
        public static final String COLUMN_BROADCAST_GENRE = "broadcast_genre";
        public static final String COLUMN_CANONICAL_GENRE = "canonical_genre";
        public static final String COLUMN_CHANNEL_ID = "channel_id";
        public static final String COLUMN_CONTENT_RATING = "content_rating";
        public static final String COLUMN_END_TIME_UTC_MILLIS = "end_time_utc_millis";
        public static final String COLUMN_EPISODE_DISPLAY_NUMBER = "episode_display_number";
        public static final String COLUMN_EPISODE_TITLE = "episode_title";
        public static final String COLUMN_INPUT_ID = "input_id";
        public static final String COLUMN_INTERNAL_PROVIDER_DATA = "internal_provider_data";
        public static final String COLUMN_INTERNAL_PROVIDER_FLAG1 = "internal_provider_flag1";
        public static final String COLUMN_INTERNAL_PROVIDER_FLAG2 = "internal_provider_flag2";
        public static final String COLUMN_INTERNAL_PROVIDER_FLAG3 = "internal_provider_flag3";
        public static final String COLUMN_INTERNAL_PROVIDER_FLAG4 = "internal_provider_flag4";
        public static final String COLUMN_LONG_DESCRIPTION = "long_description";
        public static final String COLUMN_POSTER_ART_URI = "poster_art_uri";
        public static final String COLUMN_RECORDING_DATA_BYTES = "recording_data_bytes";
        public static final String COLUMN_RECORDING_DATA_URI = "recording_data_uri";
        public static final String COLUMN_RECORDING_DURATION_MILLIS = "recording_duration_millis";
        public static final String COLUMN_RECORDING_EXPIRE_TIME_UTC_MILLIS = "recording_expire_time_utc_millis";
        public static final String COLUMN_SEARCHABLE = "searchable";
        public static final String COLUMN_SEASON_DISPLAY_NUMBER = "season_display_number";
        public static final String COLUMN_SEASON_TITLE = "season_title";
        public static final String COLUMN_SHORT_DESCRIPTION = "short_description";
        public static final String COLUMN_START_TIME_UTC_MILLIS = "start_time_utc_millis";
        public static final String COLUMN_THUMBNAIL_URI = "thumbnail_uri";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_VERSION_NUMBER = "version_number";
        public static final String COLUMN_VIDEO_HEIGHT = "video_height";
        public static final String COLUMN_VIDEO_WIDTH = "video_width";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/recorded_program";
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/recorded_program";
        public static final Uri CONTENT_URI = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.tv.TvContract.RecordedPrograms.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.tv.TvContract.RecordedPrograms.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.tv.TvContract.RecordedPrograms.<clinit>():void");
        }

        private RecordedPrograms() {
        }
    }

    public static final class WatchedPrograms implements BaseTvColumns {
        public static final String COLUMN_CHANNEL_ID = "channel_id";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_END_TIME_UTC_MILLIS = "end_time_utc_millis";
        public static final String COLUMN_INTERNAL_SESSION_TOKEN = "session_token";
        public static final String COLUMN_INTERNAL_TUNE_PARAMS = "tune_params";
        public static final String COLUMN_START_TIME_UTC_MILLIS = "start_time_utc_millis";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_WATCH_END_TIME_UTC_MILLIS = "watch_end_time_utc_millis";
        public static final String COLUMN_WATCH_START_TIME_UTC_MILLIS = "watch_start_time_utc_millis";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/watched_program";
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/watched_program";
        public static final Uri CONTENT_URI = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.tv.TvContract.WatchedPrograms.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.tv.TvContract.WatchedPrograms.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.tv.TvContract.WatchedPrograms.<clinit>():void");
        }

        private WatchedPrograms() {
        }
    }

    public static final String buildInputId(ComponentName name) {
        return name.flattenToShortString();
    }

    public static final Uri buildChannelUri(long channelId) {
        return ContentUris.withAppendedId(Channels.CONTENT_URI, channelId);
    }

    public static final Uri buildChannelUriForPassthroughInput(String inputId) {
        return new Builder().scheme(VoiceInteractionSession.KEY_CONTENT).authority(AUTHORITY).appendPath(PATH_PASSTHROUGH).appendPath(inputId).build();
    }

    public static final Uri buildChannelLogoUri(long channelId) {
        return buildChannelLogoUri(buildChannelUri(channelId));
    }

    public static final Uri buildChannelLogoUri(Uri channelUri) {
        if (isChannelUriForTunerInput(channelUri)) {
            return Uri.withAppendedPath(channelUri, Logo.CONTENT_DIRECTORY);
        }
        throw new IllegalArgumentException("Not a channel: " + channelUri);
    }

    public static final Uri buildChannelsUriForInput(String inputId) {
        return buildChannelsUriForInput(inputId, false);
    }

    public static final Uri buildChannelsUriForInput(String inputId, boolean browsableOnly) {
        Builder builder = Channels.CONTENT_URI.buildUpon();
        if (inputId != null) {
            builder.appendQueryParameter(PARAM_INPUT, inputId);
        }
        return builder.appendQueryParameter(PARAM_BROWSABLE_ONLY, String.valueOf(browsableOnly)).build();
    }

    public static final Uri buildChannelsUriForInput(String inputId, String genre, boolean browsableOnly) {
        if (genre == null) {
            return buildChannelsUriForInput(inputId, browsableOnly);
        }
        if (Genres.isCanonical(genre)) {
            return buildChannelsUriForInput(inputId, browsableOnly).buildUpon().appendQueryParameter(PARAM_CANONICAL_GENRE, genre).build();
        }
        throw new IllegalArgumentException("Not a canonical genre: '" + genre + "'");
    }

    public static final Uri buildProgramUri(long programId) {
        return ContentUris.withAppendedId(Programs.CONTENT_URI, programId);
    }

    public static final Uri buildProgramsUriForChannel(long channelId) {
        return Programs.CONTENT_URI.buildUpon().appendQueryParameter(PATH_CHANNEL, String.valueOf(channelId)).build();
    }

    public static final Uri buildProgramsUriForChannel(Uri channelUri) {
        if (isChannelUriForTunerInput(channelUri)) {
            return buildProgramsUriForChannel(ContentUris.parseId(channelUri));
        }
        throw new IllegalArgumentException("Not a channel: " + channelUri);
    }

    public static final Uri buildProgramsUriForChannel(long channelId, long startTime, long endTime) {
        return buildProgramsUriForChannel(channelId).buildUpon().appendQueryParameter(PARAM_START_TIME, String.valueOf(startTime)).appendQueryParameter(PARAM_END_TIME, String.valueOf(endTime)).build();
    }

    public static final Uri buildProgramsUriForChannel(Uri channelUri, long startTime, long endTime) {
        if (isChannelUriForTunerInput(channelUri)) {
            return buildProgramsUriForChannel(ContentUris.parseId(channelUri), startTime, endTime);
        }
        throw new IllegalArgumentException("Not a channel: " + channelUri);
    }

    public static final Uri buildRecordedProgramUri(long recordedProgramId) {
        return ContentUris.withAppendedId(RecordedPrograms.CONTENT_URI, recordedProgramId);
    }

    public static final Uri buildWatchedProgramUri(long watchedProgramId) {
        return ContentUris.withAppendedId(WatchedPrograms.CONTENT_URI, watchedProgramId);
    }

    private static boolean isTvUri(Uri uri) {
        if (uri == null || !VoiceInteractionSession.KEY_CONTENT.equals(uri.getScheme())) {
            return false;
        }
        return AUTHORITY.equals(uri.getAuthority());
    }

    private static boolean isTwoSegmentUriStartingWith(Uri uri, String pathSegment) {
        List<String> pathSegments = uri.getPathSegments();
        if (pathSegments.size() == 2) {
            return pathSegment.equals(pathSegments.get(0));
        }
        return false;
    }

    public static final boolean isChannelUri(Uri uri) {
        return !isChannelUriForTunerInput(uri) ? isChannelUriForPassthroughInput(uri) : true;
    }

    public static final boolean isChannelUriForTunerInput(Uri uri) {
        return isTvUri(uri) ? isTwoSegmentUriStartingWith(uri, PATH_CHANNEL) : false;
    }

    public static final boolean isChannelUriForPassthroughInput(Uri uri) {
        return isTvUri(uri) ? isTwoSegmentUriStartingWith(uri, PATH_PASSTHROUGH) : false;
    }

    public static final boolean isProgramUri(Uri uri) {
        return isTvUri(uri) ? isTwoSegmentUriStartingWith(uri, PATH_PROGRAM) : false;
    }

    private TvContract() {
    }
}
