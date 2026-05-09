package org.example.projects;

public class ProjectDeletedException extends RuntimeException {
    public ProjectDeletedException() {
        super("Project was deleted");
    }
}
