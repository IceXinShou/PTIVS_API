package tw.xserver.util;

import io.netty.handler.codec.http.HttpResponseStatus;

public class ErrorException extends Exception {
    public HttpResponseStatus status = HttpResponseStatus.BAD_REQUEST;

    @SuppressWarnings("unused")
    public ErrorException() {
        super();
    }

    public ErrorException(String reason) {
        super(reason);
    }

    public ErrorException(String reason, HttpResponseStatus status) {
        super(reason);

        this.status = status;
    }
}
