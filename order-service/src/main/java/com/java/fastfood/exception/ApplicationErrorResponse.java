package com.java.fastfood.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Setter
@Getter
@NoArgsConstructor
public class ApplicationErrorResponse {

    private String message;

    public ApplicationErrorResponse(String message) {
        this.message = message;
    }

    public ApplicationErrorResponse(Exception ex) {
        this.message = ex.getMessage();
    }
}
