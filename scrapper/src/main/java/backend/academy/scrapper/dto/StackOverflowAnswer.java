package backend.academy.scrapper.dto;

public record StackOverflowAnswer(
    Integer answerId,
    String questionTitle,
    String username,
    String createdAt,
    String preview
) implements UserMessage {}
