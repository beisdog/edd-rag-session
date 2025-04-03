package ch.erni.edd.demo.rag.rest;

import ch.erni.edd.demo.rag.model.Profile;
import ch.erni.edd.demo.rag.rest.CVIngestorController.Namespace;
import ch.erni.edd.demo.rag.service.CVAssistantInterface;
import ch.erni.edd.demo.rag.service.CVService;
import ch.erni.edd.demo.rag.service.SearchInput;
import ch.erni.edd.demo.rag.service.TextSegmentResult;
import ch.erni.edd.demo.rag.util.FileReaderHelper;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/cv")

@RequiredArgsConstructor
public class CVController {

    @Value("${erni.resources.dir}")
    private String resourcesDir;
    private final ChatLanguageModel chatLanguageModel;
    private final CVService cvService;


    @GetMapping("/profiles")
    public List<CVService.ProfileShort> getProfiles() {
        return cvService.getProfiles();
    }

    @SneakyThrows
    @GetMapping("/profiles/{id}")
    public Profile getProfile(@PathVariable("id") String id) {
        return cvService.getProfile(id);
    }

    @SneakyThrows
    @GetMapping("/profiles/{id}/md")
    public String getProfileAsMarkdown(@PathVariable("id") String id) {
        return cvService.getProfileAsMarkdown(id);
    }

    @PostMapping("/profiles/vs/search/{namespace}")
    public List<TextSegmentResult> vectorSearch(@PathVariable("namespace") Namespace namespace, @RequestBody SearchInput searchInput) {
        return cvService.vectorSearch(namespace, searchInput);
    }


    @PostMapping("/ask/cv/{id}")
    public ChatLanguageModelController.Message askAboutCV(@PathVariable("id") String id, @RequestBody ChatLanguageModelController.AskInput input) throws URISyntaxException, IOException {
        log.info("***************************** askAboutCV({}) *********************************", id);
        // cv laden
        String cv = FileReaderHelper.readFileFromClasspath("/cv_files/" + id + ".md");
        // prompts laden
        String systemPrompt = FileReaderHelper.readFileFromClasspath("/prompts/cv_rag_system_prompt.txt");
        String userPrompt = FileReaderHelper.readFileFromClasspath("/prompts/cv_rag_user_prompt.txt");
        // cv in den prompt einbetten
        String userMessageText = userPrompt.replace("{{cv_content}}", cv);
        userMessageText = userMessageText.replace("{{question}}", input.question);
        logPrompt(userMessageText);
        // chatmessage liste zusammen bauen
        List<ChatMessage> chatMessages = new ArrayList<>();
        chatMessages.add(SystemMessage.from(systemPrompt));
        chatMessages.add(UserMessage.from(userMessageText));

        //llm fragen
        var response = this.chatLanguageModel.chat(chatMessages);
        return ChatLanguageModelController.Message
                .builder()
                .text(response.aiMessage().text())
                .type("assistant")
                .build();
    }

    private static void logPrompt(String userMessageText) {
        log.info("User Prompt: \n{}", userMessageText);
    }

    @PostMapping("/ask/cv-list/{namespace}")
    public ChatLanguageModelController.Message askAboutCVSearchResult(@PathVariable("namespace") Namespace namespace,
                                                                      @RequestBody SearchInput input) throws URISyntaxException, IOException {
        log.info("***************************** askAboutCVSearchResult({}) *********************************", namespace);
        List<TextSegmentResult> textSegments = vectorSearch(namespace, input);
        String textSegmentsAsString = CVService.convertTextSegmentsToString(textSegments);
        String systemPrompt = FileReaderHelper.readFileFromFileSystemOrClassPath(resourcesDir, "/prompts/cv_rag_vs_system_prompt.txt");
        String userPrompt = FileReaderHelper.readFileFromFileSystemOrClassPath(resourcesDir, "/prompts/cv_rag_vs_user_prompt.txt");
        SystemMessage systemMessage = SystemMessage.from(systemPrompt);
        String userMessageText = userPrompt
                .replace("{{cv_list}}", textSegmentsAsString)
                .replace("{{question}}", input.question);
        UserMessage userMessage = UserMessage.from(userMessageText);

        logPrompt(userMessageText);

        var response = chatLanguageModel.chat(systemMessage, userMessage);

        return
                ChatLanguageModelController.Message.builder()
                        .text(response.aiMessage().text())
                        .type("assistant").build();
    }


    @PostMapping("/agent")
    public ChatLanguageModelController.Message agentAssistForCVs(@RequestBody ChatLanguageModelController.AskInput input) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
