package backend.academy.scrapper.dto;

public record GitHubUpdate(
    String sha,
    String title,
    String username,
    String createdAt,
    String preview
) {}
