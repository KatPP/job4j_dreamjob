package ru.job4j.dreamjob.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.job4j.dreamjob.dto.FileDto;
import ru.job4j.dreamjob.service.FileService;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("Тесты для FileController")
public class FileControllerTest {

    private FileService fileService;
    private FileController fileController;

    @BeforeEach
    public void initServices() {
        fileService = mock(FileService.class);
        fileController = new FileController(fileService);
    }

    @Test
    @DisplayName("При запросе существующего файла по ID должен вернуться ResponseEntity с содержимым файла")
    public void whenRequestExistingFileByIdThenReturnResponseWithFileContent() {
        var fileContent = new byte[] {1, 2, 3, 4, 5};
        var fileDto = new FileDto("test.txt", fileContent);
        when(fileService.getFileById(1)).thenReturn(Optional.of(fileDto));

        var response = fileController.getById(1);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(fileContent);
    }

    @Test
    @DisplayName("При запросе несуществующего файла по ID должен вернуться ResponseEntity с 404")
    public void whenRequestNonExistentFileByIdThenReturnNotFoundResponse() {
        when(fileService.getFileById(999)).thenReturn(Optional.empty());

        var response = fileController.getById(999);

        assertThat(response.getStatusCodeValue()).isEqualTo(404);
        assertThat(response.getBody()).isNull();
    }
}