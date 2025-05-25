package backend.academy.scrapper.exceptions;

public class KafkaException extends RuntimeException {
    public KafkaException(String message) {
        super(message);
    }
}
