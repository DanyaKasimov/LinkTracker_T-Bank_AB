package backend.academy.scrapper.dto;

public record GitHubUpdate(String sha, String title, String username, String createdAt, String preview)
        implements UserMessage {

    @Override
    public String toString() {
        return String.format("[GitHub] %s%nAuthor: %s%nCreated: %s%n%s", title(), username(), createdAt(), preview());
    }
}
