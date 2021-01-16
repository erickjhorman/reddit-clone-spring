package com.co.reddit.exception;

public class SpringRedditException extends RuntimeException {
    public SpringRedditException(String exMessage) {
      super(exMessage);
    }
}
