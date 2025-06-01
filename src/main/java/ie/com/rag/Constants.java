package ie.com.rag;

import org.springframework.stereotype.Component;

@Component
public class Constants {

    /**
     * The prompt template for the chat model.
     * It instructs the model to answer questions about the Irish Constitution using the provided documents.
     */
    public static final String PROMPT = """
            Your task is to answer the questions about any content available. Use the information from the DOCUMENTS
            section to provide accurate answers. If unsure or if the answer isn't found in the DOCUMENTS section, 
            simply state that you don't know the answer.
                        
            QUESTION:
            {input}
                        
            DOCUMENTS:
            {documents}
                        
            """;
}
