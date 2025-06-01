package ie.com.rag.controller;

import ie.com.rag.service.RagUploaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;


@RestController
@RequestMapping("/api/rag")
public class RagUploaderController {


    private final RagUploaderService ragUploaderService;


    private static final Logger logger = LoggerFactory.getLogger(RagUploaderController.class.getName());

    public RagUploaderController(RagUploaderService ragUploaderService) {
        this.ragUploaderService = ragUploaderService;
    }

    /**
     * Endpoint to upload a PDF document for processing.
     * The PDF file is expected to be sent as a multipart/form-data request.
     *
     * @param file the PDF file to be uploaded
     * @return ResponseEntity with status and message
     */

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadDocument(@RequestParam("file") MultipartFile file) {
        try {
            // Check if file is present
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body("PDF file is required");
            }

            // Check if file is PDF
            if (file.getContentType() == null || !file.getContentType().contains("pdf")) {
                return ResponseEntity.badRequest().body("Only PDF files are supported");
            }

            // Get file content as byte array
            byte[] pdfContent = file.getBytes();

            // Get filename
            String filename = file.getOriginalFilename();

            // Log file details
            logger.debug("Received PDF file: {}, size: {} bytes", filename, pdfContent.length);

            // Process the file
            ragUploaderService.processAndStoreFile(file);

            return ResponseEntity.ok("Document uploaded and processed successfully");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload document: " + e.getMessage());
        }
    }
}
