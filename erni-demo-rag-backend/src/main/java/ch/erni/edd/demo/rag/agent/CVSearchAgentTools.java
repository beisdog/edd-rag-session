package ch.erni.edd.demo.rag.agent;

import ch.erni.edd.demo.rag.model.Profile;
import ch.erni.edd.demo.rag.rest.CVIngestorController;
import ch.erni.edd.demo.rag.service.CVService;
import ch.erni.edd.demo.rag.service.SearchInput;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CVSearchAgentTools {
    private final CVService cvService;
    @Tool("Get a complete CV by id in markdown format")
    public String getProfile(String id) {
        return cvService.getProfileAsMarkdown(id);
    }

    @Tool("Get a complete CV by id in json format")
    public Profile getProfileAsJson(String id) {
        return cvService.getProfile(id);
    }

    @Tool("Search CV summaries from a vectorstore")
    public String searchCVSummariesFromVectorStore(String query) {
        return CVService.convertTextSegmentsToString(cvService.vectorSearch(CVIngestorController.Namespace.PROFILE_SUMMARY, new SearchInput(query, 10)));
    }

    @Tool("Search CV skills from a vectorstore")
    public String searchCVsSkills(String query) {
        return CVService.convertTextSegmentsToString(cvService.vectorSearch(CVIngestorController.Namespace.PROFILE_SKILLS, new SearchInput(query, 20)));
    }
}
