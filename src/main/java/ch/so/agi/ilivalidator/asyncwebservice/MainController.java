package ch.so.agi.ilivalidator.asyncwebservice;


import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.commons.io.FilenameUtils;

@Controller
public class MainController {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private static String FOLDER_PREFIX = "ilivalidator_async_";

    @Autowired
    private ServletContext servletContext;
    
    @Autowired
    CamelContext camelContext;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index() {
        return "index";
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<?> uploadFile(
            @RequestParam(name = "file", required = true) MultipartFile uploadFile, @RequestHeader String host) {
        try {
            String filename = uploadFile.getOriginalFilename();

            // If the upload button was pushed w/o choosing a file,
            // we just redirect to the starting page.
            if (uploadFile.getSize() == 0 || filename.trim().equalsIgnoreCase("") || filename == null) {
                log.warn("No file was uploaded. Redirecting to starting page.");

                HttpHeaders headers = new HttpHeaders();
                headers.add("Location", servletContext.getContextPath());
                return new ResponseEntity<String>(headers, HttpStatus.FOUND);
            }
            
            // Fix file name when using IE or Edge or...
            String fixedFilename = uploadFile.getOriginalFilename();
            int startIndex = fixedFilename.replaceAll("\\\\", "/").lastIndexOf("/");
            fixedFilename = fixedFilename.substring(startIndex + 1);
            
            // Build the local file path.
            String directory = System.getProperty("java.io.tmpdir");
            Path tmpDirectory = Files.createTempDirectory(Paths.get(directory), FOLDER_PREFIX);
            Path uploadFilePath = Paths.get(tmpDirectory.toString(), filename);

            // Save the file locally.
            byte[] bytes = uploadFile.getBytes();
            Files.write(uploadFilePath, bytes);
            log.info("Uploaded file saved: " + uploadFilePath.toFile().getAbsolutePath());

            /*
            // Validate transfer file with ilivalidator library.
            String inputFileName = uploadFilePath.toString();
            String baseFileName = FilenameUtils.getFullPath(inputFileName) + FilenameUtils.getBaseName(inputFileName);
            String logFileName = baseFileName + ".log";

            // The checkbox is not exposed in the gui at the moment.
            // But we want to use the configuration file if one is present.
            String configFile = "on";
            */
            
            // Send message to route.
            ProducerTemplate template = camelContext.createProducerTemplate();

            
            
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return ResponseEntity.badRequest().contentType(MediaType.parseMediaType("text/plain")).body(e.getMessage());
        }
        
        
        return ResponseEntity.badRequest().contentType(MediaType.parseMediaType("text/plain")).body("fubar");

    }
}
