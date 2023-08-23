package com.app.drawapp.ui.commands;

import com.app.drawapp.DrawContext;

import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;

public class ArgumentEvaluator<T> {

    private String typeName;
    private Function<String, Optional<T>> parser;
    private BiPredicate<DrawContext, T> isValid;
    private String invalidReason;

    public ArgumentEvaluator(String typeName, Function<String, Optional<T>> parser, BiPredicate<DrawContext, T> isValid, String invalidReason) {
        this.typeName = typeName;
        this.parser = parser;
        this.isValid = isValid;
        this.invalidReason = invalidReason;
    }

    public ArgumentValue<T> evaluate(String argumentName, String text, DrawContext context) {
        Optional<T> value = parser.apply(text);
        Boolean parseSuccess = value.isPresent();
        Boolean validValue = parseSuccess ? isValid.test(context, value.get()) : Boolean.FALSE;
        if (parseSuccess && validValue) {
            return new ArgumentValue<>(argumentName, value);
        } else {
            String reason = !parseSuccess ? "Unable to parse "+text+" as "+typeName : invalidReason;
            return new ArgumentValue<>(argumentName, value, reason);
        }
    }

    public String reason() {
        return invalidReason;
    }

}
