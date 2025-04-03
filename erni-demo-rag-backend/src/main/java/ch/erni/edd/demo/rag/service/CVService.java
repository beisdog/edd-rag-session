package ch.erni.edd.demo.rag.service;

import ch.erni.edd.demo.rag.config.PineconeConfig;
import ch.erni.edd.demo.rag.model.Profile;
import ch.erni.edd.demo.rag.rest.CVIngestorController;
import ch.erni.edd.demo.rag.util.FileReaderHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.Query;
import jakarta.annotation.PostConstruct;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CVService {

    @NotNull
    public static String convertTextSegmentsToString(List<TextSegmentResult> textSegments) {
        return textSegments
                .stream()
                .map(textSegment ->
                        new StringBuilder("Profile ID:")
                                .append(textSegment.metadata.get("id")).append("\n")
                                .append("Name:").append(textSegment.metadata.get("name")).append("\n")
                                .append("CV:\n")
                                .append(textSegment.text).append("\n")
                                .append("---\n")
                                .toString()
                )
                .collect(Collectors.joining("\n"));
    }

    public static class ProfileShort {
        public String id;
        public String name;
        public CareerInfoShort careerinfo;
    }

    public static class CareerInfoShort {
        public String AdvertisingText;
        public String LongAdvertisingText;
    }

    private final ObjectMapper objectMapper;
    private final EmbeddingModel embeddingModel;
    private final PineconeConfig pineconeConfig;
    private final ChatLanguageModel chatLanguageModel;
    @Getter
    private final List<ProfileShort> profiles = new ArrayList<>();

    @SneakyThrows
    @PostConstruct
    public void init() {

        List<String> files = FileReaderHelper.listFilesInClasspathDir("/cv_jsons");
        for (String file : files) {
            ProfileShort profile = objectMapper.readValue(FileReaderHelper.readFileFromClasspath("/cv_jsons/" + file), ProfileShort.class);
            profile.id = file.substring(0, file.lastIndexOf("."));
            profiles.add(profile);
        }
    }

    @SneakyThrows
    public ProfileShort getProfileShort(String id) {
        return objectMapper.readValue(FileReaderHelper.readFileFromClasspath("/cv_jsons/" + id + ".json"), ProfileShort.class);
    }

    @SneakyThrows
    public Profile getProfile(String id) {
        return objectMapper.readValue(FileReaderHelper.readFileFromClasspath("/cv_jsons/" + id + ".json"), Profile.class);
    }

    @SneakyThrows
    public String getProfileAsMarkdown(String id) {
        return FileReaderHelper.readFileFromClasspath("/cv_files/" + id + ".md");
    }

    public List<TextSegmentResult> vectorSearch(@PathVariable("namespace") CVIngestorController.Namespace namespace, @RequestBody SearchInput searchInput) {
        log.info("*** vectorSearch: {}: for query '{}' ...", namespace, searchInput.question);
        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(this.pineconeConfig.createEmbeddingStore(namespace.getType()))
                .embeddingModel(embeddingModel)
                .maxResults(searchInput.maxResults)
                //.minScore(0.75)
                .build();

        var result = contentRetriever
                .retrieve(Query.from(searchInput.question))
                .stream().map(content -> {
                    TextSegment textSegment = content.textSegment();
                    return TextSegmentResult.builder()
                            .text(textSegment.text())
                            .metadata(textSegment.metadata().toMap())
                            .namespace(namespace.getType())
                            .build();
                }).toList();
        log.info("Results:\n{}", result.stream().map(t -> "\n-------- BEGIN OF TEXTSEGMENT------\n"
                        + t.text
                        + "\n-------- END OF TEXTSEGMENT------\n")
                .collect(Collectors.joining()));
        return result;
    }

}
