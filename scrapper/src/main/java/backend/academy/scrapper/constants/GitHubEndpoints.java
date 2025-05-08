package backend.academy.scrapper.constants;

import lombok.Getter;

@Getter
public enum GitHubEndpoints {
    COMMIT("/commits"),
    PULL_REQUEST("/pulls"),
    ISSUES("/issues");

    private final String description;

    GitHubEndpoints(String description) {
        this.description = description;
    }
}
