package ie.com.rag.service;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import static ie.com.rag.Constants.PROMPT;

@Service
public class RagMcpService {

    private final ChatModel chatModel;
    private final VectorStore vectorStore;

    public RagMcpService(ChatModel chatModel, VectorStore vectorStore) {
        this.chatModel = chatModel;
        this.vectorStore = vectorStore;
    }

    /**
     * This method is used to answer questions based on the documents processed in the RAG system.
     * It uses a prompt template to format the question and the relevant documents.
     * @param question The question to be answered.
     * @return The answer to the question based on the documents.
     */

    @Tool(name = "rag_listed_documents",
          description = "RAG to get knowledge from the documents that have been processed")
    public String knowledgeRAG(String question) {

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

    /**
     * Finds similar data in the vector store based on the question.
     * @param question
     * @return
     */
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

