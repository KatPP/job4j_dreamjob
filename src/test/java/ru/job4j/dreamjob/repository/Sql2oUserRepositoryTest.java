package ru.job4j.dreamjob.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sql2o.Sql2o;
import ru.job4j.dreamjob.configuration.DatasourceConfiguration;
import ru.job4j.dreamjob.model.User;

import java.lang.reflect.Field;
import java.util.Properties;

import static java.util.Optional.empty;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class Sql2oUserRepositoryTest {

    private static Sql2oUserRepository sql2oUserRepository;

    @BeforeAll
    public static void initRepositories() throws Exception {
        var properties = new Properties();
        try (var inputStream = Sql2oUserRepositoryTest.class.getClassLoader().getResourceAsStream("connection.properties")) {
            properties.load(inputStream);
        }
        var url = properties.getProperty("datasource.url");
        var username = properties.getProperty("datasource.username");
        var password = properties.getProperty("datasource.password");

        var configuration = new DatasourceConfiguration();
        var datasource = configuration.connectionPool(url, username, password);
        var sql2o = configuration.databaseClient(datasource);

        sql2oUserRepository = new Sql2oUserRepository(sql2o);
    }

    @AfterEach
    public void clearUsers() {
        try {
            Field field = Sql2oUserRepository.class.getDeclaredField("sql2o");
            field.setAccessible(true);
            Sql2o sql2o = (Sql2o) field.get(sql2oUserRepository);
            try (var connection = sql2o.open()) {
                connection.createQuery("DELETE FROM users").executeUpdate();
            }
        } catch (Exception ignored) {
            assert true;
        }
    }

    @Test
    @DisplayName("Сохранение возвращает того же пользователя")
    public void whenSaveUserThenGetSame() {
        var user = new User("test@example.com", "Тестовый Пользователь", "password123");
        var savedUser = sql2oUserRepository.save(user);
        assertThat(savedUser).isPresent();
        assertThat(savedUser.get()).usingRecursiveComparison().isEqualTo(user);
    }

    @Test
    @DisplayName("Поиск по почте и паролю находит пользователя")
    public void whenFindByEmailAndPasswordThenGetUser() {
        var user = new User("test@example.com", "Тестовый Пользователь", "password123");
        var savedUser = sql2oUserRepository.save(user).get();

        var foundUser = sql2oUserRepository.findByEmailAndPassword("test@example.com", "password123");
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get()).usingRecursiveComparison().isEqualTo(savedUser);
    }

    @Test
    @DisplayName("Поиск по неверным данным возвращает пустой результат")
    public void whenFindByWrongEmailAndPasswordThenGetEmpty() {
        var user = sql2oUserRepository.findByEmailAndPassword("wrong@example.com", "wrongpass");
        assertThat(user).isEqualTo(empty());
    }

    @Test
    @DisplayName("Попытка сохранения пользователя с существующей почтой возвращает пустой результат")
    public void whenSaveUserWithSameEmailThenGetEmpty() {
        var user1 = new User("duplicate@example.com", "Первый Пользователь", "password123");
        var savedUser1 = sql2oUserRepository.save(user1);

        var user2 = new User("duplicate@example.com", "Второй Пользователь", "password456");
        var savedUser2 = sql2oUserRepository.save(user2);

        assertThat(savedUser1).isPresent();
        assertThat(savedUser2).isEmpty();
    }

    @Test
    @DisplayName("Поиск пользователя с правильной почтой и неправильным паролем возвращает пустой результат")
    public void whenFindByEmailAndWrongPasswordThenGetEmpty() {
        var user = new User("test2@example.com", "Тестовый Пользователь", "password123");
        sql2oUserRepository.save(user);

        var foundUser = sql2oUserRepository.findByEmailAndPassword("test2@example.com", "wrongpassword");
        assertThat(foundUser).isEqualTo(empty());
    }

    @Test
    @DisplayName("Поиск пользователя с неправильной почтой и правильным паролем возвращает пустой результат")
    public void whenFindByWrongEmailAndCorrectPasswordThenGetEmpty() {
        var user = new User("test3@example.com", "Тестовый Пользователь", "password123");
        sql2oUserRepository.save(user);

        var foundUser = sql2oUserRepository.findByEmailAndPassword("wrong@example.com", "password123");
        assertThat(foundUser).isEqualTo(empty());
    }
}