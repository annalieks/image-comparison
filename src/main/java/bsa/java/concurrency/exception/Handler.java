package bsa.java.concurrency.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

@ControllerAdvice
public final class Handler extends RuntimeException {

    @ExceptionHandler(FileProcessingException.class)
    public ResponseEntity<Object> handleApplicationIOException(FileProcessingException e) {
        return  ResponseEntity
                .status(500)
                .body(
                        Map.of("Error appeared",
                                e.getMessage() == null
                                        ? "Could not process file"
                                        : e.getMessage())
                );
    }

}
