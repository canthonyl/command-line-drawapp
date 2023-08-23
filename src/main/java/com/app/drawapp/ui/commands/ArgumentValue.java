package com.app.drawapp.ui.commands;

import java.util.Optional;

public class ArgumentValue<T> {
    private String argumentName;
    private Optional<T> value;
    private Optional<String> invalidReason;

    public ArgumentValue(String argumentName, Optional<T> value) {
        this.argumentName = argumentName;
        this.value = value;
        this.invalidReason = Optional.empty();
    }

    public ArgumentValue(String argumentName, Optional<T> value, String invalidReason) {
        this.argumentName = argumentName;
        this.value = value;
        this.invalidReason = Optional.of(invalidReason);
    }

    public Boolean isValid(){
        return value.isPresent();
    }

    public Integer asInt() {
        return (Integer) value.get();
    }

    public String asString() { return String.valueOf(value.get()); }

    public String reason() {
        return invalidReason.orElse("");
    }

    public String getName() {
        return argumentName;
    }

    public T get() {
        return value.get();
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
