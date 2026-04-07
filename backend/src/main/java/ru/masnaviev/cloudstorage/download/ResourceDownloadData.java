package ru.masnaviev.cloudstorage.download;

import ru.masnaviev.cloudstorage.model.ResourceType;

public record ResourceDownloadData(String resourceName,
                                   ResourceWriter resourceWriter,
                                   ResourceType type) {
}