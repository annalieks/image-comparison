package bsa.java.concurrency.fs;

import java.util.concurrent.CompletableFuture;

public interface FileSystem {

    CompletableFuture<String> saveFile(byte[] file, String name);

    CompletableFuture<Void> deleteImage(String name);

    CompletableFuture<Void> deleteAllImages();

}
