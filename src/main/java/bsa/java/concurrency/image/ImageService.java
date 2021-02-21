package bsa.java.concurrency.image;

import bsa.java.concurrency.exception.FileProcessingException;
import bsa.java.concurrency.fs.FileSystemService;
import bsa.java.concurrency.hash.Hasher;
import bsa.java.concurrency.image.dto.SearchResultDTO;
import bsa.java.concurrency.image.model.Image;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

@Service
public class ImageService {

    private final ImageAsyncService imageAsyncService;

    private final ImageRepository imageRepository;

    private final FileSystemService fileSystem;

    @Autowired
    ImageService(ImageAsyncService imageAsyncService,
                 ImageRepository imageRepository,
                 FileSystemService fileSystem) {
        this.imageAsyncService = imageAsyncService;
        this.imageRepository = imageRepository;
        this.fileSystem = fileSystem;
    }

    void batchUploadImages(MultipartFile[] multipartFiles) {
        var futures = new ArrayList<CompletableFuture<Void>>();

        for (var multipartFile : multipartFiles) {
            try {
                var file = multipartFile.getBytes();
                String name = multipartFile.getOriginalFilename();
                futures.add(imageAsyncService.processImage(file, name));
            } catch (IOException e) {
                throw new FileProcessingException(e.getMessage());
            }
        }
        futures.forEach(future -> future.exceptionally(e -> {
            throw new FileProcessingException(e.getMessage());
        }).join());
    }

    List<SearchResultDTO> searchMatches(MultipartFile multipartFile, double threshold) {
        try {
            var file = multipartFile.getBytes();
            String name = multipartFile.getOriginalFilename();
            long hash = Hasher.calculateHash(file);
            var result = imageRepository.searchMatches(hash, threshold);
            if (result.isEmpty()) {
                writeImage(file, name, hash);
            }
            return result;
        } catch (IOException e) {
            throw new FileProcessingException(e.getMessage());
        }
    }

    public void deleteImage(UUID id) {
        fileSystem.deleteImage(id.toString());
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        CompletableFuture.runAsync(()-> imageRepository.deleteById(id), executorService);
        executorService.shutdown();
    }

    public void deleteAllImages() {
        fileSystem.deleteAllImages();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        CompletableFuture.runAsync(imageRepository::deleteAll, executorService);
        executorService.shutdown();
    }

    private void writeImage(byte[] file, String name, long hash) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        CompletableFuture.runAsync(() -> {
            UUID id = UUID.randomUUID();
            try {
                String url = fileSystem.saveFile(
                        file,
                        id.toString() + fileSystem.getExtension(name)
                ).get();
                imageRepository.save(new Image(id, hash, url));
            } catch (InterruptedException | ExecutionException e) {
                Thread.currentThread().interrupt();
            }
        }, executorService);
        executorService.shutdown();
    }

}
