package backend.academy.scrapper.dto;

public record StackOverflowAnswer(
    Integer answerId,
    String questionTitle,
    String username,
    String createdAt,
    String preview
) implements UserMessage {
    @Override
    public String toString() {
        return String.format(
            "**%s** ответил на вопрос \"%s\":\n%s",
            username(),
            questionTitle(),
            preview()
        );
    }
}
