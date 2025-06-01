package ie.com.rag.controller;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ie.com.rag.Constants.PROMPT;

@RestController
@RequestMapping("/api/rag")
public class RagWiserController {

    private final ChatModel chatModel;
    private final VectorStore vectorStore;

    public RagWiserController(ChatModel chatModel, VectorStore vectorStore) {
        this.chatModel = chatModel;
        this.vectorStore = vectorStore;
    }

    /**
     * This endpoint is used to answer questions based on the documents processed in the RAG system.
     * It uses a prompt template to format the question and the relevant documents.
     *
     * @param question The question to be answered.
     * @return The answer to the question based on the documents.
     */

    @GetMapping
    public String simplify(@RequestParam(value = "question",
    defaultValue = "List all the Articles in the Irish Constitution")
                           String question) {
        // Create a prompt template with the provided prompt string
        PromptTemplate template
                = new PromptTemplate(PROMPT);
        Map<String, Object> promptsParameters = new HashMap<>();
        promptsParameters.put("input", question);
        promptsParameters.put("documents", findSimilarData(question));

        return chatModel
                .call(template.create(promptsParameters))
                .getResult()
                .getOutput()
                .getContent();
    }

    private String findSimilarData(String question) {
        List<Document> documents =
                vectorStore.similaritySearch(SearchRequest
                .query(question)
                        .withTopK(5));

        return documents
                .stream()
                .map(document -> document.getContent().toString())
                .collect(Collectors.joining());

    }
}
