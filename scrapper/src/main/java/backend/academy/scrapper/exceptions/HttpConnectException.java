package backend.academy.scrapper.exceptions;

public class HttpConnectException extends RuntimeException {
    public HttpConnectException(String message) {
        super(message);
    }
}
