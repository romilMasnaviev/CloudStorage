package ru.masnaviev.cloudstorage.download;

import java.io.IOException;
import java.io.OutputStream;

@FunctionalInterface
public interface ResourceWriter {

    void writeResourceToOutputStream(OutputStream outputStream) throws IOException;
}
