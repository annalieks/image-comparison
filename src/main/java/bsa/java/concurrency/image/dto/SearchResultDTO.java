package bsa.java.concurrency.image.dto;

import java.util.UUID;

public interface SearchResultDTO {
    UUID getImageId();
    Double getMatchPercent();
    String getImageUrl();
}
