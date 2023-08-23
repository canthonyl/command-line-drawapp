package com.app.drawapp.ui.commands;

import com.app.drawapp.DrawContext;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Command {

    private final String prefix;
    private final List<String> argumentNames;
    private final List<ArgumentEvaluator> argumentList;
    private final Predicate<DrawContext> preArgValidation;
    private final BiPredicate<DrawContext, List<ArgumentValue>> postArgValidation;
    private final BiConsumer<DrawContext, List<ArgumentValue>> processor;

    Command(String prefix, List<String> argumentNames, List<ArgumentEvaluator> argumentList,
            BiConsumer<DrawContext, List<ArgumentValue>> processor, Predicate<DrawContext> preArgValidation,
            BiPredicate<DrawContext, List<ArgumentValue>> postArgValidation) {
        this.prefix = prefix;
        this.argumentNames = argumentNames;
        this.argumentList = argumentList;
        this.processor = processor;
        this.preArgValidation = preArgValidation;
        this.postArgValidation = postArgValidation;
    }

    private String buildSyntax(){
        StringBuilder sb = new StringBuilder();
        sb.append(prefix);
        for (String name : argumentNames) {
            sb.append(" [").append(name).append("] ");
        }
        return sb.toString();
    }

    private String format(String reason, List<ArgumentValue> invalidList){
        StringBuilder sb = new StringBuilder();
        sb.append(reason).append(" : ");
        sb.append(invalidList.stream().map(ArgumentValue::getName).collect(Collectors.joining(", ")));
        return sb.toString();
    }

    public void process(List<String> input, DrawContext drawContext) {
        if (preArgValidation.test(drawContext)) {
            Map<Boolean, List<ArgumentValue>> allValues = IntStream.range(0, argumentList.size())
                    .mapToObj(i -> {
                        String name = argumentNames.get(i);
                        if (i < input.size())
                            return argumentList.get(i).evaluate(name, input.get(i), drawContext);
                        else
                            return new ArgumentValue<>(name, Optional.empty(), "Missing Argument");
                    })
                    .collect(Collectors.partitioningBy(ArgumentValue::isValid));

            List<ArgumentValue> valid = allValues.get(Boolean.TRUE);
            List<ArgumentValue> invalid = allValues.get(Boolean.FALSE);

            if (invalid.size() > 0) {
                invalid.stream()
                        .collect(Collectors.groupingBy(ArgumentValue::reason))
                        .forEach((reason, list) -> drawContext.getPrintStream().println(format(reason, list)));
                drawContext.resetColor();
            } else {
                if (postArgValidation.test(drawContext, valid)) {
                    processor.accept(drawContext, valid);
                }
            }
        }
    }

}
