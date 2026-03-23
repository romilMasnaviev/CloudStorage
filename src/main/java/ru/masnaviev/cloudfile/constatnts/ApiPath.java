package ru.masnaviev.cloudfile.constatnts;

public class ApiPath {
    public static final String AUTH_SIGN_UP_URL = "/api/auth/sign-up";
    public static final String AUTH_SIGN_IN_URL = "/api/auth/sign-in";
    public static final String AUTH_SIGN_OUT_URL = "/api/auth/sign-out";

    public static final String USER_ME_URL = "/api/user/me";

    public static final String GET_RESOURCE_INFO = "/api/resource";
    public static final String DELETE_RESOURCE = "/api/resource";
    public static final String DOWNLOAD_RESOURCE = "/api/resource/download";
    public static final String UPLOAD_RESOURCE = "/api/resource";
    public static final String MOVE_RESOURCE = "/api/resource/move";
    public static final String FIND_RESOURCE = "/api/resource/search";

    public static final String UPLOAD_DIRECTORY = "/api/directory";
    public static final String GET_DIRECTORY_CONTENTS_INFO = "/api/directory";

    public static final String[] SWAGGER_AUTH_WHITELIST = {
            "/v2/api-docs",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-resources",
            "/swagger-resources/**",
            "/configuration/ui",
            "/configuration/security",
            "/swagger-ui/**",
            "/webjars/**",
            "/swagger-ui.html"
    };
}
