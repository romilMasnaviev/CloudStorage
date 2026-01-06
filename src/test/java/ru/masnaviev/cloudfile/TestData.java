package ru.masnaviev.cloudfile;

import ru.masnaviev.cloudfile.model.User;

public class TestData {

    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String TOO_SHORT_USERNAME = "user";
    public static final String TOO_LONG_USERNAME = "usernameusernameusernameusernameusernameusernameusernameusername";
    public static final String TOO_SHORT_PASSWORD = "pass";
    public static final String TOO_LONG_PASSWORD = "passwordpasswordpasswordpasswordpasswordpasswordpasswordpassword" +
            "passwordpasswordpasswordpasswordpassword";

    public static User createUser(String username, String password) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        return user;
    }
}
