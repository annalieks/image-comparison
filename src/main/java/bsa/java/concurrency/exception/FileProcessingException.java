package bsa.java.concurrency.exception;

public class FileProcessingException extends RuntimeException {

    public FileProcessingException() { }

    public FileProcessingException(String message) {
        super(message);
    }

}
