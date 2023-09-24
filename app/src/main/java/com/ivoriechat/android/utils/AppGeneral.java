package com.ivoriechat.android.utils;

public class AppGeneral {
    public static final String SERVER_SECURE_PROTOCOL = "https://";
    public static final String SERVER_DOMAIN_NAME = "service.ivoriechat.com";
    public static final String COLON = ":";
    public static final String SERVER_PORT_NUMBER = "8443";
    public static final String WEB_MODULE_PATH = "/webWeb";

    public static final String PREF_NAME = "IvorieChatPreferences";
    public static final String USER_ID = "user_id";
    public static final String USER_MOBILE_PHONE_NUMBER = "user_mobile_phone_number";
    public static final String COUNTRY_CALLING_CODE = "country_calling_code";

    // Constants for Login Activity
    public static final String USER_EXIST = "user_exist";
    public static final String LOGIN_SUCCESS = "login_success";
    public static final String TOKEN = "token";
    public static final String TOKEN_VALID = "token_valid";
    public static final String TOKEN_NOT_VALID = "token_not_valid";

    // SMS validation
    public static final String CODE = "code";
    public static final String VALIDATION_CODE = "validation_code";
    public static final String VALIDATION_CODE_SENT = "validation_code_sent";
    public static final String VALIDATION_TOKEN = "validation_token";
    public static final String SMS_CODE_NOT_CORRECT = "sms_code_not_correct";
    public static final String SMS_CODE_CORRECT = "sms_code_correct";
    public static final String INVALIDE_VALIDATION_CODE = "invalide_validation_code";
    public static final String USER_NOT_EXIST = "user_not_exist";

    public static final String ACCOUNT_TYPE = "com.ivoriechat.android";
    public static final String AUTHTOKEN_TYPE_FULL_ACCESS = "Full access";
    public static final String AUTHTOKENTYPE = "access_all";
    public static final String OWNER_ID_IN_DB = "owner_id_in_database";
    public static final String PLAIN_PASSWD = "plain_passwd";
    public static final String ENCRYPTED_PASSWD = "encrypted_passwd";
    public static final String ISLOGGEDIN = "isLoggedIn";
    public static Boolean TokenValid = true;

    public static final String GET_WALLET_CONNECT_KEY_API = "/GetWalletConnectKeyServlet";
    public static final String WEBSOCKET_SERVER_URL = "wss://service.ivoriechat.com:8443";
    public static final String VIDEO_CALL_WEBSOCKET_ENDPOINT = "/videocall_wsendpoint";

    public static final String LOGIN_WITH_SMS_API = "/LoginWithSMSServlet";
    public static final String VALIDATION_CODE_API = "/GenerateValidationCodeServlet";
    public static final String APPLY_FOR_VERIFICATION_API = "/ApplyForMentorshipServlet";
    public static final String GET_MENTOR_COLLECTION_API = "/GetMentorCollectionServlet";
    public static final String GET_LIKED_MENTOR_COLLECTION_API  = "/GetLikedMentorCollectionServlet";
    public static final String UPDATE_WALLET_ADDRESS_API = "/UpdateWalletAddressServlet";
    public static final String GET_MY_PROFILE_API = "/GetMyProfileServlet";
    public static final String UPLOAD_ICON_IMAGE_API = "/SaveUserIconServlet";
    public static final String CHECK_BALANCE_AND_ALLOWANCE_API = "/CheckBalanceAndAllowanceServlet";
    public static final String CHECK_DEPOSIT_API = "/CheckDepositServlet";
    public static final String UPDATE_PREFERRED_PAYMENT_CRYPTO_API = "/UpdatePreferredPaymentCryptoServlet";

    public static final String GET_CRYPTO_TOKEN_PRICE_API = "/GetCryptoTokenPriceServlet";
    public static final String CHECK_FOLLOWING_STATUS_API = "/CheckFollowingStatusServlet";
    public static final String FOLLOW_USER_API = "/FollowUserServlet";
    public static final String CEASE_FOLLOWING_USER_API = "/CeaseFollowingUserServlet";
    public static final String FETCH_MENTOR_VIDEOS_BY_MENTOR_ID_API = "/FetchMentorVideosServlet";
    public static final String FETCH_MENTOR_BY_ID_API = "/FetchMentorDetailsServlet";
    public static final String FETCH_MENTOR_EXPERTISES_API = "/FetchMentorExpertisesServlet";
    public static final String CREATE_MEDIA_API = "/CreateMediaServlet";
    public static final String INCREMENT_VIDEO_LIKED_STATS_API = "/IncrementVideoLikedStatsServlet";
    public static final String FETCH_VIDEO_API = "/FetchVideoServlet";
    public static final String FETCH_CHAT_SUMMARY_API = "/FetchChatSummaryServlet";
    public static final String UPDATE_NAME_API = "/UpdateNameServlet";
    public static final String FETCH_CHAT_LIST_API = "/FetchChatCollectionServlet";
    public static final String FETCH_USER_PROFILE_API = "/FetchUserProfileServlet";

    // Fragment name
    public static final String VIDEO_CHAT_FRAGMENT = "video_chat_fragment";
    public static final String IN_PROGRESS_FRAGMENT = "in_progress_fragment";

    public static final String AUTHORIZATION = "Authorization";

    public static final String CHAT_TYPE = "chat_type";
    public static final String MENTOR_ID = "mentor_id";

    //WebRTC
    public static final String TYPE = "type";
    public static final String TARGET = "target";
    public static final String SDP = "sdp";
    public static final String CANDIDATE = "candidate";
    public static final String SDP_M_ID = "sdpMid";
    public static final String SDP_M_LINE_INDEX = "sdpMLineIndex";
    public static final String LIVECAST_ID = "livecastId";
    public static final String INSPECTOR_ID = "inspectorId";
    public static final String LIVECAST_INITIATION = "livecast_initiation";
    public static final String WATCH_LIVECAST_REQUEST = "watch_livecast_request";
    public static final String NOTIFICATION_FOR_WATCH_VIDEO_REQUEST = "notification_for_watch_video_request";
    public static final String INSPECTOR_VIDEO_ANSWER = "inspector_video_answer";
    public static final String LIVECASTER_VIDEO_OFFER = "livecaster_video_offer";
    public static final String REPLY_TO_LIVECASTER = "reply_to_livecaster";
    public static final String NEW_ICE_CANDIDATE_FROM_INSPECTOR = "new_ice_candidate_from_inspector";
    public static final String NEW_ICE_CANDIDATE_FROM_LIVECASTER = "new_ice_candidate_from_livecaster";
    public static final String RTC_PEER_CONNECTION_CONNECTED = "rtc_peer_connection_connected";
    public static final String RTC_PEER_CONNECTION_CLOSED = "rtc_peer_connection_closed";
    public static final String TERMINATE_LIVECAST = "terminate_livecast";
    public static final String TERMINATE_LIVECAST_RESPONSE = "terminate_livecast_response";

    public static final String GENERAL_PUBLIC = "general_public";
    public static final String LIVECAST_PROVIDER = "livecast_provider";
    public static final String RESULT = "result";
    public static final String WEBSOCKET_CONNECTION_CALLBACK = "websocket_connection_callback";
    public static final String CONNECTION_STATUS = "connection_status";
    public static final String OPEN = "open";
    public static final String CLOSE = "close";
    public static final String ERROR = "error";
    public static final String BEING_MENTOR = "being_mentor";
    public static final String READY_FOR_MENTORING = "ready_for_mentoring";
    public static final String WATCH_VIDEO_REQUEST = "watch_video_request";
    public static final String VIDEO_ANSWER = "video_answer";
    public static final String VIDEO_OFFER = "video_offer";
    public static final String NEW_ICE_CANDIDATE = "new_ice_candidate";
    public static final String READY_FOR_MENTORING_RESPONSE = "ready_for_mentoring_response";
    public static final String WATCH_VIDEO_REQUEST_RESPONSE = "watch_video_request_response";
    public static final String FIND_VIDEO_SUCCESS = "find_video_success";

    public static final String USER_REAL_NAME = "userRealName";
    public static final String HOURLY_RATE = "hourlyRate";
    public static final String EXPECTED_USAGE = "expectedUsage";
    public static final String EXPERTISE_LIST = "expertiseList";
    public static final String SELF_INTRODUCTION = "selfIntroduction";

    public static final String PORTRAIT_ATTACHED = "portrait_attached";
    public static final String PORTRAIT_FILE = "portrait_file";
    public static final String FILE_SIZE = "file_size";
}
