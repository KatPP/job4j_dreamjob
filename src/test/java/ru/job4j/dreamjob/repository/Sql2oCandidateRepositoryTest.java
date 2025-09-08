package ru.job4j.dreamjob.repository;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.job4j.dreamjob.configuration.DatasourceConfiguration;
import ru.job4j.dreamjob.model.Candidate;
import ru.job4j.dreamjob.model.File;

import java.util.List;
import java.util.Properties;

import static java.time.LocalDateTime.now;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class Sql2oCandidateRepositoryTest {

    private static Sql2oCandidateRepository sql2oCandidateRepository;

    private static Sql2oFileRepository sql2oFileRepository;

    private static File file;

    @BeforeAll
    public static void initRepositories() throws Exception {
        var properties = new Properties();
        try (var inputStream = Sql2oCandidateRepositoryTest.class.getClassLoader().getResourceAsStream("connection.properties")) {
            properties.load(inputStream);
        }
        var url = properties.getProperty("datasource.url");
        var username = properties.getProperty("datasource.username");
        var password = properties.getProperty("datasource.password");

        var configuration = new DatasourceConfiguration();
        var datasource = configuration.connectionPool(url, username, password);
        var sql2o = configuration.databaseClient(datasource);

        sql2oCandidateRepository = new Sql2oCandidateRepository(sql2o);
        sql2oFileRepository = new Sql2oFileRepository(sql2o);

        // нужно сохранить хотя бы один файл, т.к. Candidate от него зависит
        file = new File("test", "test");
        sql2oFileRepository.save(file);
    }

    @AfterAll
    public static void deleteFile() {
        sql2oFileRepository.deleteById(file.getId());
    }

    @AfterEach
    public void clearCandidates() {
        var candidates = sql2oCandidateRepository.findAllCandidate();
        for (var candidate : candidates) {
            sql2oCandidateRepository.deleteByIdCandidate(candidate.getId());
        }
    }

    @Test
    public void whenSaveThenGetSame() {
        var creationDate = now().withSecond(0).withNano(0);
        var candidate = sql2oCandidateRepository.saveCandidate(new Candidate(0, "name", "description", creationDate, 1, file.getId()));
        var savedCandidate = sql2oCandidateRepository.findByIdCandidate(candidate.getId()).get();
        assertThat(savedCandidate).usingRecursiveComparison().isEqualTo(candidate);
    }

    @Test
    public void whenSaveSeveralThenGetAll() {
        var creationDate = now().withSecond(0).withNano(0);
        var candidate1 = sql2oCandidateRepository.saveCandidate(new Candidate(0, "name1", "description1", creationDate, 1, file.getId()));
        var candidate2 = sql2oCandidateRepository.saveCandidate(new Candidate(0, "name2", "description2", creationDate, 1, file.getId()));
        var candidate3 = sql2oCandidateRepository.saveCandidate(new Candidate(0, "name3", "description3", creationDate, 1, file.getId()));
        var result = sql2oCandidateRepository.findAllCandidate();
        assertThat(result).isEqualTo(List.of(candidate1, candidate2, candidate3));
    }

    @Test
    public void whenDontSaveThenNothingFound() {
        assertThat(sql2oCandidateRepository.findAllCandidate()).isEqualTo(emptyList());
        assertThat(sql2oCandidateRepository.findByIdCandidate(0)).isEqualTo(empty());
    }

    @Test
    public void whenDeleteThenGetEmptyOptional() {
        var creationDate = now().withSecond(0).withNano(0);
        var candidate = sql2oCandidateRepository.saveCandidate(new Candidate(0, "name", "description", creationDate, 1, file.getId()));
        sql2oCandidateRepository.deleteByIdCandidate(candidate.getId());
        var savedCandidate = sql2oCandidateRepository.findByIdCandidate(candidate.getId());
        assertThat(savedCandidate).isEqualTo(empty());
    }

    @Test
    public void whenDeleteByInvalidIdThenNothingHappens() {
        sql2oCandidateRepository.deleteByIdCandidate(0);
        assertThat(true).isTrue();
    }

    @Test
    public void whenUpdateThenGetUpdated() {
        var creationDate = now().withSecond(0).withNano(0);
        var candidate = sql2oCandidateRepository.saveCandidate(new Candidate(0, "name", "description", creationDate, 1, file.getId()));
        var updatedCandidate = new Candidate(
                candidate.getId(), "new name", "new description", creationDate,
                candidate.getCityId(), file.getId()
        );
        var isUpdated = sql2oCandidateRepository.updateCandidate(updatedCandidate);
        var savedCandidate = sql2oCandidateRepository.findByIdCandidate(updatedCandidate.getId()).get();
        assertThat(isUpdated).isTrue();
        assertThat(savedCandidate).usingRecursiveComparison().isEqualTo(updatedCandidate);
    }

    @Test
    public void whenUpdateUnExistingCandidateThenGetFalse() {
        var creationDate = now().withSecond(0).withNano(0);
        var candidate = new Candidate(0, "name", "description", creationDate, 1, file.getId());
        var isUpdated = sql2oCandidateRepository.updateCandidate(candidate);
        assertThat(isUpdated).isFalse();
    }
}