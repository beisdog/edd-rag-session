package ch.erni.edd.demo.rag.service;


import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DiagnosticsChatModelListener implements ChatModelListener {

    @Getter
    private List<Object> diagnostics = new ArrayList<>();

    @Override
    public void onRequest(ChatModelRequestContext requestContext) {
        log.info(toSummary(requestContext.chatRequest()));
        diagnostics.add(requestContext.chatRequest());
    }

    @Override
    public void onResponse(ChatModelResponseContext responseContext) {
        log.info("Model[{}]: {}", responseContext.chatResponse().metadata().modelName(), toSummary(responseContext.chatResponse()));
        diagnostics.add(responseContext.chatResponse());
    }

    @Override
    public void onError(ChatModelErrorContext errorContext) {
        log.info("ERROR:" + errorContext.error().toString());
        diagnostics.add(errorContext.error());
    }

    public String getDiagnosticsSummary() {
        return diagnostics.stream()
                .map(o -> {
                    if (o instanceof ChatRequest r) {
                        return toSummary(r);
                    }
                    if (o instanceof ChatResponse r) {
                        return toSummary(r);
                    }
                    else {
                        return o.toString();
                    }
                })
                .collect(Collectors.joining("\n"));
    }

    @NotNull
    private static String toSummary(ChatResponse r) {
        if (r.aiMessage().hasToolExecutionRequests()) {
            return "Chat Response: tool_execution_requests:" + r.aiMessage().toolExecutionRequests().stream().map(ToolExecutionRequest::name).collect(Collectors.joining(","));
        } else {
            return "Chat Response: text: " + r.aiMessage().text();
        }
    }

    @NotNull
    private static String toSummary(ChatRequest r) {
        return "Chat Request: messages: " + r.messages().stream().map(m -> m.type().name()).collect(Collectors.joining(","));
    }
}
