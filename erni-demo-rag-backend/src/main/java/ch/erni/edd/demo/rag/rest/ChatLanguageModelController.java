package ch.erni.edd.demo.rag.rest;

import dev.ai4j.openai4j.chat.AssistantMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/llm")
@RequiredArgsConstructor
public class ChatLanguageModelController {

    private final ChatLanguageModel chatLanguageModel;

    @Data
    public static class AskInput {
        String question;
    }

    @Data
    @Builder
    public static class Message {
        String text;
        String type;
    }

    @PostMapping("/ask/simple")
    public Message ask(@RequestBody AskInput input) {
        var response = this.chatLanguageModel.chat(input.question);
        return Message.builder()
                .text(response)
                .type("assistant")
                .build();
    }

    @PostMapping("/ask/messages")
    public Message askWithMessages(@RequestBody Message[] input) {
        List<ChatMessage> messages = Stream.of(input).map(m -> {
            if (Objects.equals(m.getType(), "user")) {
                return dev.langchain4j.data.message.UserMessage.from(m.getText());
            } else if (Objects.equals(m.getType(), "system")) {
                return dev.langchain4j.data.message.SystemMessage.from(m.getText());
            }else if (Objects.equals(m.getType(), "assistant")) {
                return dev.langchain4j.data.message.SystemMessage.from(m.getText());
            }
            throw new IllegalArgumentException("Unknown message type: " + m.getType());
        }).toList();

        ChatResponse response = chatLanguageModel.chat(messages);

        return
                Message.builder()
                        .text(response.aiMessage().text())
                        .type("assistant").build();
    }
}
