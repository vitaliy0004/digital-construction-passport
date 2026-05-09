package org.example.projects;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/projects")
@Validated
public class ProjectController {
    private final ProjectRepository projectRepository;

    public ProjectController(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public record ProjectRequest(
            @NotBlank String name,
            @NotNull ProjectStatus status,
            List<String> documents
    ) {
    }

    public record ProjectResponse(Long id, String name, ProjectStatus status, List<String> documents, boolean deleted) {
        public static ProjectResponse from(Project p) {
            return new ProjectResponse(p.getId(), p.getName(), p.getStatus(), p.getDocuments(), p.isDeleted());
        }
    }

    @GetMapping
    public List<ProjectResponse> list() {
        return projectRepository.findAll().stream().map(ProjectResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ProjectResponse get(@PathVariable Long id) {
        Project p = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        if (p.isDeleted()) {
            throw new ProjectDeletedException();
        }
        return ProjectResponse.from(p);
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> create(@Valid @RequestBody ProjectRequest request) {
        Project p = new Project(request.name(), request.status(), request.documents());
        Project saved = projectRepository.save(p);
        return ResponseEntity.ok(ProjectResponse.from(saved));
    }

    @PutMapping("/{id}")
    public ProjectResponse update(@PathVariable Long id, @Valid @RequestBody ProjectRequest request) {
        Project p = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        if (p.isDeleted()) {
            throw new ProjectDeletedException();
        }

        p.setName(request.name());
        p.setStatus(request.status());
        p.setDocuments(request.documents());

        Project saved = projectRepository.save(p);
        return ProjectResponse.from(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Project p = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        if (p.isDeleted()) {
            throw new ProjectDeletedException();
        }

        p.setDeleted(true);
        projectRepository.save(p);
        return ResponseEntity.noContent().build();
    }
}
