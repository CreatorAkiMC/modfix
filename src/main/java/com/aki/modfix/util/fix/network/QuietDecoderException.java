package com.aki.modfix.util.fix.network;

import io.netty.handler.codec.DecoderException;

public class QuietDecoderException extends DecoderException {

    public QuietDecoderException(String message) {
        super(message);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}