package ie.com.rag.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


@Service
public class RagUploaderService {

    private final VectorStore vectorStore;
    private final JdbcClient jdbcClient;

    private static final Logger logger = LoggerFactory.getLogger(RagUploaderService.class.getName());

    public RagUploaderService(VectorStore vectorStore, JdbcClient jdbcClient) {
        this.vectorStore = vectorStore;
        this.jdbcClient = jdbcClient;
    }

    /**
     * Processes the uploaded PDF file and stores its content in the vector store.
     * @param file
     * @throws IOException
     */
    public void processAndStoreFile(MultipartFile file) throws IOException {

        try {

            Integer count = jdbcClient.sql("select COUNT(*) from vector_store")
                    .query(Integer.class)
                    .single();

            logger.debug("No of Records in the PG Vector Store = {}", count);

            // Create temporary file and process the PDF
            Path tempFile = Files.createTempFile("pdf-upload-", ".pdf");
            file.transferTo(tempFile.toFile());
            Resource fileResource = new FileSystemResource(tempFile.toFile());

            // Configure and process with PDF reader
            PdfDocumentReaderConfig config = PdfDocumentReaderConfig.builder()
                    .withPagesPerDocument(1)
                    .build();

            PagePdfDocumentReader reader = new PagePdfDocumentReader(fileResource, config);
            var textSplitter = new TokenTextSplitter();
            vectorStore.accept(textSplitter.apply(reader.get()));

            // Cleanup temporary file
            Files.deleteIfExists(tempFile);
        } catch (Exception e) {
            logger.error("Error processing PDF file: {}", e.getMessage());
            throw new IOException("Failed to process PDF file", e);
        }

        logger.debug("Document processed and added to vector store successfully");
    }
}
