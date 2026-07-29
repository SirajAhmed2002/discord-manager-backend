package ch.zhaw.it.pm4.discordmanagerbe.api;

import ch.zhaw.it.pm4.discordmanagerbe.dto.ServerConfigDTO;
import ch.zhaw.it.pm4.discordmanagerbe.dto.TemplateDTO;
import ch.zhaw.it.pm4.discordmanagerbe.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * This class serves as the API gateway for template management-related endpoints.
 */
@RestController
@RequestMapping("/templates")
public class TemplateManagementApiGateway {

    /**
     * Service for managing templates.
     */
    private final TemplateService templateService;

    /**
     * Constructor for TemplateManagementApiGateway.
     */
    @Autowired
    public TemplateManagementApiGateway(TemplateService templateService) {
        this.templateService = templateService;
    }

    /**
     * This endpoint returns a list of all templates.
     * @return A string containing the list of templates.
     */
    @GetMapping("")
    public ResponseEntity<List<TemplateDTO>> getTemplates() {
        return ResponseEntity.ok(templateService.getAllTemplates());
    }

    /**
     * This endpoint gets the details of a specific template.
     * @param templateId The ID of the template to retrieve.
     * @return A string containing the template details.
     */
    @GetMapping("/{templateId}")
    public ResponseEntity<ServerConfigDTO> getTemplateById(@PathVariable String templateId) {
        System.out.println(templateService.getTemplateByName(templateId));
        return ResponseEntity.ok(templateService.getTemplateByName(templateId));
    }
}
