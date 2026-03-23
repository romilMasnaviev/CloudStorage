package ru.masnaviev.cloudfile;

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import ru.masnaviev.cloudfile.dto.response.resource.ResourceInfoResponse;
import ru.masnaviev.cloudfile.model.User;

import static ru.masnaviev.cloudfile.dto.response.resource.ResourceInfoResponseBuilder.createFrom;
import static ru.masnaviev.cloudfile.util.ResourceType.DIRECTORY;
import static ru.masnaviev.cloudfile.util.ResourceType.FILE;

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

    public static final MockMultipartFile file1 = new MockMultipartFile("file", "hello.txt", MediaType.TEXT_PLAIN_VALUE, "Hello,world1".getBytes());
    public static final String file1Path = "user-1-files/hello.txt";
    public static final ResourceInfoResponse file1ExpectedResponse = createFrom("", "hello.txt", file1.getSize(), FILE);

    public static final MockMultipartFile file2 = new MockMultipartFile("file", "folder1/hello.txt", MediaType.TEXT_PLAIN_VALUE, "Hello,world12".getBytes());
    public static final String file2Path = "user-1-files/folder1/hello.txt";
    public static final ResourceInfoResponse file2ExpectedResponse = createFrom("folder1/", "hello.txt", file2.getSize(), FILE);

    public static final MockMultipartFile file3 = new MockMultipartFile("file", "folder1/folder2/hello.txt", MediaType.TEXT_PLAIN_VALUE, "Hello,world123".getBytes());
    public static final String file3Path = "user-1-files/folder1/folder2/hello.txt";
    public static final ResourceInfoResponse file3ExpectedResponse = createFrom("folder1/folder2/", "hello.txt", file3.getSize(), FILE);

    public static final MockMultipartFile file4 = new MockMultipartFile("file", "folder1/hello4.txt", MediaType.TEXT_PLAIN_VALUE, "Hello,world1234".getBytes());
    public static final String file4Path = "user-1-files/folder1/hello4.txt";
    public static final ResourceInfoResponse file4ExpectedResponse = createFrom("folder1/", "hello4.txt", file4.getSize(), FILE);

    public static final MockMultipartFile file5 = new MockMultipartFile("file", "folder1/hello5.txt", MediaType.TEXT_PLAIN_VALUE, "Hello,world12345".getBytes());
    public static final String file5Path = "user-1-files/folder1/hello5.txt";
    public static final ResourceInfoResponse file5ExpectedResponse = createFrom("folder1/", "hello5.txt", file5.getSize(), FILE);

    public static final ResourceInfoResponse folder1 = createFrom("", "folder1", null, DIRECTORY);
    public static final ResourceInfoResponse folder1folder2 = createFrom("folder1/", "folder2", null, DIRECTORY);

}