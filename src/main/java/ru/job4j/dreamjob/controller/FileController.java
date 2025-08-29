package ru.job4j.dreamjob.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.job4j.dreamjob.service.FileService;

/**
 * REST контроллер для работы с файлами.
 * Обеспечивает получение файлов по их идентификаторам.
 */
@RestController
@RequestMapping("/files")
public class FileController {

    private final FileService fileService;

    /**
     * Конструктор контроллера файлов.
     * @param fileService сервис для работы с файлами
     */
    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    /**
     * Обрабатывает GET-запрос на получение файла по ID.
     * Возвращает содержимое файла в теле ответа.
     * @param id идентификатор файла
     * @return ResponseEntity с содержимым файла или 404 если файл не найден
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable int id) {
        // Получаем файл из сервиса по ID
        var contentOptional = fileService.getFileById(id);
        // Если файл не найден - возвращаем 404 Not Found
        if (contentOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        // Если файл найден - возвращаем его содержимое в теле ответа
        return ResponseEntity.ok(contentOptional.get().getContent());
    }
}