package bsa.java.concurrency.image;

import bsa.java.concurrency.fs.FileSystemService;
import bsa.java.concurrency.hash.Hasher;
import bsa.java.concurrency.image.model.Image;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.*;

@Service
public class ImageAsyncService {

    private final FileSystemService fileSystem;

    private final ImageRepository imageRepository;

    @Autowired
    public ImageAsyncService(FileSystemService fileSystem,
                             ImageRepository imageRepository) {
        this.fileSystem = fileSystem;
        this.imageRepository = imageRepository;
    }

    @Async
    CompletableFuture<Void> processImage(byte[] file, String name) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        UUID id = UUID.randomUUID();

        var hashFuture = CompletableFuture.supplyAsync(() -> Hasher.calculateHash(file), executorService);
        executorService.shutdown();
        CompletableFuture<String> fsFuture = fileSystem.saveFile(
                file,
                id.toString() + fileSystem.getExtension(name)
        );

        try {
            imageRepository.save(new Image(id, hashFuture.get(), fsFuture.get()));
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
        }

        return null;
    }

}
