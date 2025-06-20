package ru.masnaviev.cloudfile.user;

import ru.masnaviev.cloudfile.user.model.User;

public class TestData {

    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";

    public static User createUser(String username, String password) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        return user;
    }

    public static User createDefaultuser() {
        User user = new User();
        user.setUsername(USERNAME);
        user.setPassword(PASSWORD);
        return user;
    }

}
