package ru.masnaviev.cloudstorage.util;

import io.minio.GetObjectResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipBuilder {

    public static ByteArrayOutputStream createZipFromResources(String prefixToRemove, Map<String, GetObjectResponse> downloadedResources) {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();

        try (ZipOutputStream zos = new ZipOutputStream(byteOut)) {
            for (var downloadedResource : downloadedResources.entrySet()) {

                String key = downloadedResource.getKey();
                String pathInsideZip = key.startsWith(prefixToRemove)
                        ? key.substring(prefixToRemove.length())
                        : key;

                zos.putNextEntry(new ZipEntry(pathInsideZip));
                downloadedResource.getValue().transferTo(zos);
                zos.closeEntry();
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex.getMessage());
        }

        return byteOut;
    }
}
