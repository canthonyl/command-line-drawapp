package com.app.drawapp.ui.commands;

import com.app.drawapp.DrawContext;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class CommandBuilder {

    private String command;
    private List<String> argumentNames;
    private List<ArgumentEvaluator> argumentList;
    private BiConsumer<DrawContext, List<ArgumentValue>> processor;
    private Predicate<DrawContext> preArgPredicates;
    private BiPredicate<DrawContext, List<ArgumentValue>> postArgPredicates;
    private String helpTextTemplate;

    public CommandBuilder() {
        argumentNames = new LinkedList<>();
        argumentList = new LinkedList<>();
        preArgPredicates = dc -> Boolean.TRUE;
        postArgPredicates = (dc, l) -> Boolean.TRUE;
        command = "";
        helpTextTemplate = "";
    }

    public CommandBuilder command(String command) {
        this.command = command;
        return this;
    }

    public CommandBuilder argument(String name, ArgumentEvaluator argumentHandler) {
        argumentNames.add(name);
        argumentList.add(argumentHandler);
        return this;
    }

    public CommandBuilder action(BiConsumer<DrawContext, List<ArgumentValue>> processor) {
        this.processor = processor;
        return this;
    }

    public CommandBuilder action(Consumer<DrawContext> processor) {
        this.processor = (dc, a) -> processor.accept(dc);
        return this;
    }

    public CommandBuilder validate(Predicate<DrawContext> condition){
        preArgPredicates = preArgPredicates.and(condition);
        return this;
    }

    public CommandBuilder validate(BiPredicate<DrawContext, List<ArgumentValue>> condition){
        postArgPredicates = postArgPredicates.and(condition);
        return this;
    }

    public Command build() {
        return new Command(command, argumentNames, argumentList, processor,
                preArgPredicates, postArgPredicates);
    }

}
