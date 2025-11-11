package com.example.ejb.exceptions;

import jakarta.ejb.ApplicationException;


@ApplicationException(rollback = true)
public class OperacaoInvalidaException extends RuntimeException {
    public OperacaoInvalidaException(String message) {
        super(message);
    }
}