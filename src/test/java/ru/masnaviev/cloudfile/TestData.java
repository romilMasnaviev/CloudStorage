package ru.masnaviev.cloudfile;

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
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

    public static final MockMultipartFile file1 = new MockMultipartFile("file", "hello.txt", MediaType.TEXT_PLAIN_VALUE, "Hello,world".getBytes());
    public static final MockMultipartFile file2 = new MockMultipartFile("file", "folder1/hello.txt", MediaType.TEXT_PLAIN_VALUE, "Hello,world".getBytes());
    public static final MockMultipartFile file3 = new MockMultipartFile("file", "folder1/folder2/hello.txt", MediaType.TEXT_PLAIN_VALUE, "Hello,world".getBytes());

    public static final MockMultipartFile file4 = new MockMultipartFile("file", "folder1/hello4.txt", MediaType.TEXT_PLAIN_VALUE, "Hello,world".getBytes());
    public static final MockMultipartFile file5 = new MockMultipartFile("file", "folder1/hello5.txt", MediaType.TEXT_PLAIN_VALUE, "Hello,world".getBytes());
    public static final MockMultipartFile file6 = new MockMultipartFile("file", "folder1/folder2/hello6.txt", MediaType.TEXT_PLAIN_VALUE, "Hello,world".getBytes());
}
