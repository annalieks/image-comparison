package bsa.java.concurrency.image;

import bsa.java.concurrency.image.dto.SearchResultDTO;
import bsa.java.concurrency.image.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ImageRepository extends JpaRepository<Image, UUID> {

    @Query(value = "select * from " +
            "(select cast(i.id as varchar) as imageId, " +
            "1 - diff_percent(:hash # i.hash) as matchPercent, " +
            "i.url as imageUrl " +
            "from images as i) as matches " +
            "where matchPercent >= :threshold", nativeQuery = true)
    List<SearchResultDTO> searchMatches(@Param("hash") long hash, @Param("threshold") double threshold);
}
