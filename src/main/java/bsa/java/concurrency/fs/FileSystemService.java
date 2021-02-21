package bsa.java.concurrency.fs;

import bsa.java.concurrency.exception.FileProcessingException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class FileSystemService implements FileSystem {

    @Value("${server.port}")
    private String port;

    @Value("${fs.images}")
    private String imagesDir;

    public CompletableFuture<String> saveFile(byte[] file, String name) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        var result = CompletableFuture.supplyAsync(() -> {
            try {
                createDirIfNotExist(Path.of(imagesDir));
                var filePath = Path.of(imagesDir + File.separator + name);
                var os = Files.newOutputStream(filePath);
                os.write(file);
                return composeFileName(filePath);
            } catch (IOException e) {
                throw new FileProcessingException(e.getMessage());
            }
        }, executorService);
        executorService.shutdown();
        return result;
    }

    public CompletableFuture<Void> deleteImage(String name) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        var result = CompletableFuture.runAsync(() -> {
            var it = FileUtils.iterateFiles(new File(imagesDir), null, false);

            while (it.hasNext()) {
                var file = it.next();
                if (FilenameUtils.getBaseName(file.getName()).equals(name)) {
                    file.delete();
                    return;
                }
            }
        }, executorService);
        executorService.shutdown();
        return result;
    }

    public CompletableFuture<Void> deleteAllImages() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        var result = CompletableFuture.runAsync(() -> {
            try {
                FileUtils.deleteDirectory(new File(imagesDir));
                createDirIfNotExist(Path.of(imagesDir));
            } catch (IOException e) {
                throw new FileProcessingException("Cannot delete files");
            }
        }, executorService);
        executorService.shutdown();
        return result;
    }

    private String composeFileName(Path filePath) {
        var host = InetAddress.getLoopbackAddress().getHostAddress();
        var path = '/' + filePath.toString().replace("\\", "/");

        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .scheme("http").host(host).port(port).path(path).build();

        return uriComponents.toUriString();
    }

    private void createDirIfNotExist(Path dir) throws IOException {
        if(!Files.exists(dir)) {
            Files.createDirectory(dir);
        }
    }

    public String getExtension(String fileName) {
        if(fileName == null) {
            throw new FileProcessingException("Cannot resolve file name");
        }
        return fileName.substring(fileName.lastIndexOf('.'));
    }

}
