package ru.job4j.dreamjob.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.ConcurrentModel;
import org.springframework.web.multipart.MultipartFile;
import ru.job4j.dreamjob.dto.FileDto;
import ru.job4j.dreamjob.model.Candidate;
import ru.job4j.dreamjob.model.City;
import ru.job4j.dreamjob.service.CandidateService;
import ru.job4j.dreamjob.service.CityService;

import java.util.List;
import java.util.Optional;

import static java.time.LocalDateTime.now;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("Тесты для CandidateController")
public class CandidateControllerTest {

    private CandidateService candidateService;
    private CityService cityService;
    private CandidateController candidateController;
    private MultipartFile testFile;

    @BeforeEach
    public void initServices() {
        candidateService = mock(CandidateService.class);
        cityService = mock(CityService.class);
        candidateController = new CandidateController(candidateService, cityService);
        testFile = new MockMultipartFile("testFile.img", new byte[] {1, 2, 3});
    }

    @Test
    @DisplayName("При запросе страницы со списком кандидатов должен вернуться список кандидатов")
    public void whenRequestCandidateListPageThenGetPageWithCandidates() {
        var candidate1 = new Candidate(1, "test1", "desc1", now(), 1, 1);
        var candidate2 = new Candidate(2, "test2", "desc2", now(), 2, 2);
        var expectedCandidates = List.of(candidate1, candidate2);
        when(candidateService.findAllCandidate()).thenReturn(expectedCandidates);

        var model = new ConcurrentModel();
        var view = candidateController.getAll(model);
        var actualCandidates = model.getAttribute("candidates");

        assertThat(view).isEqualTo("candidates/list");
        assertThat(actualCandidates).isEqualTo(expectedCandidates);
    }

    @Test
    @DisplayName("При запросе страницы создания кандидата должны отобразиться города")
    public void whenRequestCandidateCreationPageThenGetPageWithCities() {
        var city1 = new City(1, "Москва");
        var city2 = new City(2, "Санкт-Петербург");
        var expectedCities = List.of(city1, city2);
        when(cityService.findAll()).thenReturn(expectedCities);

        var model = new ConcurrentModel();
        var view = candidateController.getCreationPage(model);
        var actualCities = model.getAttribute("cities");

        assertThat(view).isEqualTo("candidates/create");
        assertThat(actualCities).isEqualTo(expectedCities);
    }

    @Test
    @DisplayName("При создании кандидата с файлом должны сохраниться те же данные и перенаправление на страницу кандидатов")
    public void whenPostCandidateWithFileThenSameDataAndRedirectToCandidatesPage() throws Exception {
        var candidate = new Candidate();
        candidate.setId(1);
        candidate.setName("test1");
        candidate.setDescription("desc1");
        candidate.setCreationDate(now());
        candidate.setCityId(1);
        candidate.setFileId(1);

        var fileDto = new FileDto(testFile.getOriginalFilename(), testFile.getBytes());
        var candidateArgumentCaptor = ArgumentCaptor.forClass(Candidate.class);
        var fileDtoArgumentCaptor = ArgumentCaptor.forClass(FileDto.class);
        when(candidateService.saveCandidate(candidateArgumentCaptor.capture(), fileDtoArgumentCaptor.capture())).thenReturn(candidate);

        var model = new ConcurrentModel();
        var view = candidateController.create(candidate, testFile, model);
        var actualCandidate = candidateArgumentCaptor.getValue();
        var actualFileDto = fileDtoArgumentCaptor.getValue();

        assertThat(view).isEqualTo("redirect:/candidates");
        assertThat(actualCandidate.getName()).isEqualTo(candidate.getName());
        assertThat(actualCandidate.getDescription()).isEqualTo(candidate.getDescription());
        assertThat(fileDto).usingRecursiveComparison().isEqualTo(actualFileDto);
    }

    @Test
    @DisplayName("Когда выбрасывается исключение при создании кандидата, то должна отобразиться страница ошибки с сообщением")
    public void whenCreateCandidateThrowsExceptionThenGetErrorPageWithMessage() throws Exception {
        var expectedException = new RuntimeException("Failed to write file");
        when(candidateService.saveCandidate(any(), any())).thenThrow(expectedException);

        var model = new ConcurrentModel();
        var view = candidateController.create(new Candidate(), testFile, model);
        var actualExceptionMessage = model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(actualExceptionMessage).isEqualTo(expectedException.getMessage());
    }

    @Test
    @DisplayName("При запросе кандидата по ID должен вернуться кандидат и список городов")
    public void whenRequestCandidateByIdThenGetCandidateAndCities() {
        var candidate = new Candidate(1, "test1", "desc1", now(), 1, 1);
        var city1 = new City(1, "Москва");
        var city2 = new City(2, "Санкт-Петербург");
        var expectedCities = List.of(city1, city2);

        when(candidateService.findByIdCandidate(1)).thenReturn(Optional.of(candidate));
        when(cityService.findAll()).thenReturn(expectedCities);

        var model = new ConcurrentModel();
        var view = candidateController.getById(model, 1);
        var actualCandidate = model.getAttribute("candidate");
        var actualCities = model.getAttribute("cities");

        assertThat(view).isEqualTo("candidates/one");
        assertThat(actualCandidate).isEqualTo(candidate);
        assertThat(actualCities).isEqualTo(expectedCities);
    }

    @Test
    @DisplayName("При запросе несуществующего кандидата должна отобразиться страница ошибки")
    public void whenRequestNonExistentCandidateThenGetErrorPage() {
        when(candidateService.findByIdCandidate(999)).thenReturn(Optional.empty());

        var model = new ConcurrentModel();
        var view = candidateController.getById(model, 999);
        var actualMessage = model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(actualMessage).isEqualTo("Кандидат с указанным идентификатором не найден");
    }

    @Test
    @DisplayName("При обновлении кандидата с файлом должно произойти перенаправление на страницу кандидатов")
    public void whenUpdateCandidateWithFileThenRedirectToCandidatesPage() throws Exception {
        var candidate = new Candidate();
        candidate.setId(1);
        candidate.setName("updated test");
        candidate.setDescription("updated desc");
        candidate.setCreationDate(now());
        candidate.setCityId(2);
        candidate.setFileId(1);

        var fileDto = new FileDto(testFile.getOriginalFilename(), testFile.getBytes());

        var candidateArgumentCaptor = ArgumentCaptor.forClass(Candidate.class);
        var fileDtoArgumentCaptor = ArgumentCaptor.forClass(FileDto.class);

        when(candidateService.updateCandidate(candidateArgumentCaptor.capture(), fileDtoArgumentCaptor.capture())).thenReturn(true);

        var model = new ConcurrentModel();
        var view = candidateController.update(candidate, testFile, model);
        var actualCandidate = candidateArgumentCaptor.getValue();
        var actualFileDto = fileDtoArgumentCaptor.getValue();

        assertThat(view).isEqualTo("redirect:/candidates");
        assertThat(actualCandidate.getName()).isEqualTo(candidate.getName());
        assertThat(actualCandidate.getDescription()).isEqualTo(candidate.getDescription());
        assertThat(fileDto).usingRecursiveComparison().isEqualTo(actualFileDto);
    }

    @Test
    @DisplayName("При обновлении несуществующего кандидата должна отобразиться страница ошибки")
    public void whenUpdateNonExistentCandidateThenGetErrorPage() throws Exception {
        var candidate = new Candidate();
        candidate.setId(999);
        candidate.setName("test");
        candidate.setDescription("desc");
        candidate.setCreationDate(now());
        candidate.setCityId(1);
        candidate.setFileId(1);

        when(candidateService.updateCandidate(any(), any())).thenReturn(false);

        var model = new ConcurrentModel();
        var view = candidateController.update(candidate, testFile, model);
        var actualMessage = model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(actualMessage).isEqualTo("Кандидат с указанным идентификатором не найден");
    }

    @Test
    @DisplayName("При возникновении исключения при обновлении кандидата должна отобразиться страница ошибки")
    public void whenUpdateCandidateThrowsExceptionThenGetErrorPage() throws Exception {
        var expectedException = new RuntimeException("Update failed");
        when(candidateService.updateCandidate(any(), any())).thenThrow(expectedException);

        var model = new ConcurrentModel();
        var view = candidateController.update(new Candidate(), testFile, model);
        var actualMessage = model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(actualMessage).isEqualTo(expectedException.getMessage());
    }

    @Test
    @DisplayName("При удалении кандидата должно произойти перенаправление на страницу кандидатов")
    public void whenDeleteCandidateThenRedirectToCandidatesPage() {
        var model = new ConcurrentModel();
        var view = candidateController.delete(1);

        verify(candidateService).deleteByIdCandidate(1);
        assertThat(view).isEqualTo("redirect:/candidates");
    }
}