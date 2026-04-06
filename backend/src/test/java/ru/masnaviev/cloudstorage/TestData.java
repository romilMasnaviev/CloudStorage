package ru.masnaviev.cloudstorage;

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import ru.masnaviev.cloudstorage.dto.response.resource.ResourceInfoResponse;
import ru.masnaviev.cloudstorage.model.Resource;
import ru.masnaviev.cloudstorage.model.ResourceFactory;
import ru.masnaviev.cloudstorage.model.User;

import static ru.masnaviev.cloudstorage.dto.response.resource.ResourceInfoResponseAssembler.resourceToResourceInfoResponse;

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

    public static User createUser(Long id, String username, String password) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setPassword(password);
        return user;
    }

    public static final MockMultipartFile file1 = new MockMultipartFile("file", "hello.txt", MediaType.TEXT_PLAIN_VALUE, "Hello,world1".getBytes());
    public static final String file1Path = "user-1-files/hello.txt";
    public static final Resource resource1 = ResourceFactory.createFromFullMinioPath(1L, file1Path);
    public static final ResourceInfoResponse file1ExpectedResponse = resourceToResourceInfoResponse(resource1, file1.getSize());

    public static final MockMultipartFile file2 = new MockMultipartFile("file", "folder1/hello.txt", MediaType.TEXT_PLAIN_VALUE, "Hello,world12".getBytes());
    public static final String file2Path = "user-1-files/folder1/hello.txt";
    public static final Resource resource2 = ResourceFactory.createFromFullMinioPath(1L, file2Path);
    public static final ResourceInfoResponse file2ExpectedResponse = resourceToResourceInfoResponse(resource2, file2.getSize());

    public static final MockMultipartFile file3 = new MockMultipartFile("file", "folder1/folder2/hello.txt", MediaType.TEXT_PLAIN_VALUE, "Hello,world123".getBytes());
    public static final String file3Path = "user-1-files/folder1/folder2/hello.txt";
    public static final Resource resource3 = ResourceFactory.createFromFullMinioPath(1L, file3Path);
    public static final ResourceInfoResponse file3ExpectedResponse = resourceToResourceInfoResponse(resource3, file3.getSize());

    public static final MockMultipartFile file4 = new MockMultipartFile("file", "folder1/hello4.txt", MediaType.TEXT_PLAIN_VALUE, "Hello,world1234".getBytes());
    public static final String file4Path = "user-1-files/folder1/hello4.txt";
    public static final Resource resource4 = ResourceFactory.createFromFullMinioPath(1L, file4Path);
    public static final ResourceInfoResponse file4ExpectedResponse = resourceToResourceInfoResponse(resource4, file4.getSize());

    public static final MockMultipartFile file5 = new MockMultipartFile("file", "folder1/hello5.txt", MediaType.TEXT_PLAIN_VALUE, "Hello,world12345".getBytes());
    public static final String file5Path = "user-1-files/folder1/hello5.txt";
    public static final Resource resource5 = ResourceFactory.createFromFullMinioPath(1L, file5Path);
    public static final ResourceInfoResponse file5ExpectedResponse = resourceToResourceInfoResponse(resource5, file5.getSize());

    public static final Resource resourceFolder1 = ResourceFactory.createFromFullMinioPath(1L, "user-1-files/folder1/");
    public static final ResourceInfoResponse folder1 = resourceToResourceInfoResponse(resourceFolder1, null);
    public static final ResourceInfoResponse folder1ExpectedResponse = resourceToResourceInfoResponse(resourceFolder1, null);

    public static final Resource resourceFolder1Folder2 = ResourceFactory.createFromFullMinioPath(1L, "user-1-files/folder1/folder2/");
    public static final ResourceInfoResponse folder1folder2 = resourceToResourceInfoResponse(resourceFolder1Folder2, null);

    public static final MockMultipartFile file6 = new MockMultipartFile("file", "hello6.txt", MediaType.TEXT_PLAIN_VALUE, "Hello,world123456".getBytes());
    public static final String file6Path = "user-1-files/hello6.txt";
    public static final Resource resource6 = ResourceFactory.createFromFullMinioPath(1L, file6Path);
    public static final ResourceInfoResponse file6ExpectedResponse = resourceToResourceInfoResponse(resource6, file6.getSize());

    public static final MockMultipartFile file7 = new MockMultipartFile("file", "hello7.txt", MediaType.TEXT_PLAIN_VALUE, "Hello,world1234567".getBytes());
    public static final String file7Path = "user-1-files/hello7.txt";
    public static final Resource resource7 = ResourceFactory.createFromFullMinioPath(1L, file7Path);
    public static final ResourceInfoResponse file7ExpectedResponse = resourceToResourceInfoResponse(resource7, file7.getSize());
}