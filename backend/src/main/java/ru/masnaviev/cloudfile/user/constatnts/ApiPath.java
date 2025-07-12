package ru.masnaviev.cloudfile.user.constatnts;

public class ApiPath {
    public static final String AUTH_SIGN_UP_URL = "/api/auth/sign-up";
    public static final String AUTH_SIGN_IN_URL = "/api/auth/sign-in";
    public static final String AUTH_SIGN_OUT_URL = "/api/auth/sign-out";

    public static final String USER_ME_URL = "/api/user/me";

    public static final String UPLOAD = "/api/resource";

    public static final String[] SWAGGER_AUTH_WHITELIST = new String[]{"/v3/api-docs*/**", "/swagger-ui/**"};

}
