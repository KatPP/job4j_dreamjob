package ru.job4j.dreamjob.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ConcurrentModel;
import ru.job4j.dreamjob.model.User;
import ru.job4j.dreamjob.service.UserService;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("Тесты для UserController")
public class UserControllerTest {

    private UserService userService;
    private UserController userController;

    @BeforeEach
    public void initServices() {
        userService = mock(UserService.class);
        userController = new UserController(userService);
    }

    @Test
    @DisplayName("При запросе страницы регистрации должен вернуться шаблон регистрации")
    public void whenRequestRegistrationPageThenGetRegistrationTemplate() {
        var view = userController.getRegistrationPage();
        assertThat(view).isEqualTo("users/register");
    }

    @Test
    @DisplayName("При успешной регистрации пользователя должно произойти перенаправление на страницу вакансий")
    public void whenRegisterUserSuccessfullyThenRedirectToVacancies() {
        var user = new User(1, "test@example.com", "Test User", "password");
        when(userService.save(user)).thenReturn(Optional.of(user));

        var model = new ConcurrentModel();
        var view = userController.register(model, user);

        assertThat(view).isEqualTo("redirect:/vacancies");
    }

    @Test
    @DisplayName("При регистрации пользователя с существующей почтой должна отобразиться страница ошибки")
    public void whenRegisterUserWithExistingEmailThenGetErrorPage() {
        var user = new User(1, "test@example.com", "Test User", "password");
        when(userService.save(user)).thenReturn(Optional.empty());

        var model = new ConcurrentModel();
        var view = userController.register(model, user);
        var actualMessage = model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(actualMessage).isEqualTo("Пользователь с такой почтой уже существует");
    }

    @Test
    @DisplayName("При запросе страницы входа должен вернуться шаблон входа")
    public void whenRequestLoginPageThenGetLoginTemplate() {
        var view = userController.getLoginPage();
        assertThat(view).isEqualTo("users/login");
    }

    @Test
    @DisplayName("При успешном входе пользователя должно произойти перенаправление на страницу вакансий")
    public void whenLoginUserSuccessfullyThenRedirectToVacancies() {
        var user = new User(1, "test@example.com", "Test User", "password");
        var request = new MockHttpServletRequest();
        when(userService.findByEmailAndPassword("test@example.com", "password")).thenReturn(Optional.of(user));

        var model = new ConcurrentModel();
        var view = userController.loginUser(user, model, request);
        var sessionUser = request.getSession().getAttribute("user");

        assertThat(view).isEqualTo("redirect:/vacancies");
        assertThat(sessionUser).isEqualTo(user);
    }

    @Test
    @DisplayName("При неверных данных входа должна отобразиться страница входа с ошибкой")
    public void whenLoginUserWithInvalidCredentialsThenGetLoginPageWithError() {
        var user = new User(1, "test@example.com", "Test User", "wrongpassword");
        var request = new MockHttpServletRequest();
        when(userService.findByEmailAndPassword("test@example.com", "wrongpassword")).thenReturn(Optional.empty());

        var model = new ConcurrentModel();
        var view = userController.loginUser(user, model, request);
        var errorMessage = model.getAttribute("error");

        assertThat(view).isEqualTo("users/login");
        assertThat(errorMessage).isEqualTo("Почта или пароль введены неверно");
    }

    @Test
    @DisplayName("При выходе пользователя должна произойти инвалидация сессии и перенаправление на страницу входа")
    public void whenLogoutUserThenInvalidateSessionAndRedirectToLoginPage() {
        var session = new MockHttpSession();
        var view = userController.logout(session);

        assertThat(view).isEqualTo("redirect:/users/login");
        assertThat(session.isInvalid()).isTrue();
    }
}