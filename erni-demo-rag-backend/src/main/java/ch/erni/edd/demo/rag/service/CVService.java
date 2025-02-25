package ch.erni.edd.demo.rag.service;

import ch.erni.edd.demo.rag.config.PineconeConfig;
import ch.erni.edd.demo.rag.model.Profile;
import ch.erni.edd.demo.rag.util.FileReaderHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModelName;
import dev.langchain4j.model.openai.OpenAiTokenizer;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import jakarta.annotation.PostConstruct;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class CVService {

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
}
