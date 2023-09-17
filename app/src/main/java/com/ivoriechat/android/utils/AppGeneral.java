package com.ivoriechat.android.utils;

public class AppGeneral {
    public static final String SERVER_SECURE_PROTOCOL = "https://";
    public static final String SERVER_DOMAIN_NAME = "service.ivoriechat.com";
    public static final String SERVER_PORT_NUMBER = "8443";
    public static final String WEB_MODULE_PATH = "/webWeb";

    public static final String PREF_NAME = "IvorieChatPreferences";
    public static final String USER_ID = "user_id";
    public static final String USER_MOBILE_PHONE_NUMBER = "user_mobile_phone_number";
    public static final String COUNTRY_CALLING_CODE = "country_calling_code";

    // Constants for Login Activity
    public static final String USER_EXIST = "user_exist";
    public static final String CORRECT_PASSWORD = "correct_password";
    public static final String LOGIN_SUCCESS = "login_success";
    public static final String TOKEN = "token";
    public static final String TOKEN_VALID = "token_valid";
    public static final String TOKEN_NOT_VALID = "token_not_valid";

    // SMS validation
    public static final String CODE = "code";
    public static final String VALIDATION_CODE = "validation_code";
    public static final String VALIDATION_CODE_SENT = "validation_code_sent";
    public static final String VALIDATION_TOKEN = "validation_token";
    public static final String SMS_CODE_CORRECT = "sms_code_correct";
    public static final String INVALIDE_VALIDATION_CODE = "invalide_validation_code";
    public static final String USER_NOT_EXIST = "user_not_exist";

    public static final String ACCOUNT_TYPE = "com.jieyoubaoapp.android";
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
    public static final String APPLY_FOR_MENTORSHIP_API = "/ApplyForMentorshipServlet";
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
}
