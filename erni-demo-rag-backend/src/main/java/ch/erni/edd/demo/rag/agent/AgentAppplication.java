package ch.erni.edd.demo.rag.agent;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = {
        "ch.erni.edd.demo.rag",
        "dev.langchain4j"
})
@SpringBootApplication
@RequiredArgsConstructor
public class AgentAppplication implements CommandLineRunner {

    private final CVSearchAgent cvSearchAgent;
    private final ConfigurableApplicationContext ctx;

    public static void main(String[] args) {
        SpringApplication.run(AgentAppplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Starting Agent ...");
        String query = "Java";
        cvSearchAgent.searchCVs(query);
        System.out.println("Finished Agent!");
        ctx.close();
    }
}
