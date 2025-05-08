package backend.academy.scrapper.liquibase;

import backend.academy.scrapper.config.LiquibaseConfig;
import jakarta.annotation.PostConstruct;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.DirectoryResourceAccessor;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.ResourceAccessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Slf4j
@RequiredArgsConstructor
public class LiquibaseRunner {

    private final LiquibaseConfig config;

    @PostConstruct()
    public void run() {
        try (Connection connection = DriverManager.getConnection(config.url(), config.username(), config.password())) {
            log.info("Старт миграции.");
            Database database = DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(new JdbcConnection(connection));

            File changelogFile = new File(config.file());
            ResourceAccessor accessor = new DirectoryResourceAccessor(changelogFile.getParentFile());

            Liquibase liquibase = new Liquibase(changelogFile.getName(), accessor, database);
            liquibase.update((String) null);
            log.info("Миграция завершена.");
        } catch (SQLException | LiquibaseException e) {
            throw new RuntimeException("Liquibase migration failed: " + e.getMessage(), e);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
