package backend.academy.bot.exceptions;

public class HttpConnectException extends RuntimeException {
    public HttpConnectException(String message) {
        super(message);
    }
}
