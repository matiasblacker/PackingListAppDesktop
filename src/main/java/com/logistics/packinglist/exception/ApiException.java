package com.logistics.packinglist.exception;

public class ApiException extends Exception {
    private final String code;
    private final String serverMessage;

    public ApiException(String code, String serverMessage) {
        super(serverMessage);
        this.code = code;
        this.serverMessage = serverMessage;
    }

    public String getCode() {
        return code;
    }

    public String getServerMessage() {
        return serverMessage;
    }

    @Override
    public String getMessage() {
        if ("ERR-500".equals(code)) {
            return "No fue posible completar la operación.\n\nIntente nuevamente más tarde.\n\nCódigo de referencia: " + code;
        }
        return serverMessage;
    }
}
