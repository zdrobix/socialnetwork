package com.example.demo.domain.validators;

public interface Validator<T> {
    void validate(T entity) throws ValidationException;
}