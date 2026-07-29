package ch.zhaw.it.pm4.discordmanagerbe.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * This class serves as the API gateway for the application.
 */
@RestController
@RequestMapping("/api")
public class ApiGateway {

    /**
     * Constructor for ApiGateway.
     */
    public ApiGateway() {
    }

    /**
     * This endpoint returns the status of the API.
     * @return A string indicating the status of the API.
     */
    @GetMapping("/status")
    public ResponseEntity<String> getStatus() {
        return ResponseEntity.ok("Running");
    }

    /**
     * This endpoint returns the version of the API.
     * @return A string containing the version of the API.
     */
    @GetMapping("/version")
    public ResponseEntity<String> getVersion() {
        return ResponseEntity.ok("1.0.0");
    }
}
