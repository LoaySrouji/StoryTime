package com.continuesvoicerecognition;

import java.util.HashMap;
import java.util.Map;

class Manager {
    final static String ENCODED_FIELD = "EncodedImg";
    final static String ENCODED_AUDIO_FIELD = "music";
    final static String MAIN_STORIES_COLLECTION = "Stories";
    final static String SUB_STORY_COLLECTION = "Lines";
    final static String STORY_TEXT_DOCUMENT_ID = "StoryText";
    final static String STORY_TEXT_DOCUMENT_FIELD = "Text";

    final static String MAIN_CONNECTION_COLLECTION = "ConnectionManager";
    final static String ALL_CONNECTIONS_DOCUMENT_IDS = "Connections";
    final static String ALL_CONNECTIONS_COLLECTION = "Connection's_IDs";
    final static String BASE_CONNECTION_ID = "Base";
    final static String CONNECTION_ID = "id";

    final static String AUDIO_COLLECTION_NAME = "Audios";
    final static String AUDIO_FIELD = "audio";

    final static String CONNECTION_SUB_NAME = "Connection";
    final static String SUB_CONNECTION_COLLECTION = "StoryTime";
    final static String IMG_DOCUMENT_ID = "img";
    final static String IMG_DOCUMENT_FIELD = "img";
    final static String AUDIO_DOCUMENT_FIELD = "music";

    final static String PLAY_VIEW_KEY = "PV_KEY";
    final static String PLAY_AUDIO = "play";
    final static String READ_STORY = "read";

    final static String COVER_ID = "Cover Picture";
    final static String FIRST_ID = "First Picture";

    final static String STORY_KEY_NAME = "story_key";
    final static String STORY_NAME_SNOW_WHITE = "Snow White and the Seven Dwarfs";
    final static String STORY_NAME_LITTLE_RED_RIDING_HOOD = "Little Red Riding Hood";
    final static String STORY_NAME_THE_GINGERBREAD_MAN = "The Gingerbread Man";
    final static String STORY_NAME_NIGHT_BEFORE_CHRISTMAS = "THE NIGHT BEFORE CHRISTMAS";


    final static Map<String, String> STORY_NAME_TO_COLLECTION = new HashMap<String, String>(){{
        put(STORY_NAME_SNOW_WHITE, "snow_white");
        put(STORY_NAME_LITTLE_RED_RIDING_HOOD, "little_red_ridding_hood");
        put(STORY_NAME_THE_GINGERBREAD_MAN, "the_gingerbread_man");
        put(STORY_NAME_NIGHT_BEFORE_CHRISTMAS, "night_before_christmas");
    }};
}
