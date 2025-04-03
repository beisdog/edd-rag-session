package ch.erni.edd.demo.rag.agent;

import lombok.Data;

@Data
public class CVSearchResult {

    @Data
    public static class CV {
        private String id;
        private String name;
        private String whyThePersonFitsTheQuery;
        private RelevantProject[] relevantProjects;
    }

    @Data
    public static class RelevantProject{
        private String title;
        private String from;
        private String to;
        private String skills;
    }

    private CV[] cvs;
}
