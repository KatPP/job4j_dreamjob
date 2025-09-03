package ru.job4j.dreamjob.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.job4j.dreamjob.dto.FileDto;
import ru.job4j.dreamjob.model.File;
import ru.job4j.dreamjob.repository.FileRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

/**
 * Сервис для работы с файлами: сохранение, получение, удаление.
 * Управляет как хранением файлов в файловой системе, так и метаинформацией в БД.
 */
@Service
public class SimpleFileService implements FileService {

    private final FileRepository fileRepository;

    private final String storageDirectory;

    /**
     * Конструктор сервиса файлов.
     * @param sql2oFileRepository репозиторий для работы с файлами в БД
     * @param storageDirectory путь к директории для хранения файлов (берется из application.properties)
     */
    public SimpleFileService(FileRepository sql2oFileRepository,
                             @Value("${file.directory}") String storageDirectory) {
        this.fileRepository = sql2oFileRepository;
        this.storageDirectory = storageDirectory;
        // Создаем директорию для хранения файлов при запуске приложения
        createStorageDirectory(storageDirectory);
    }

    /**
     * Создает директорию для хранения файлов, если она не существует.
     * @param path путь к директории
     */
    private void createStorageDirectory(String path) {
        try {
            Files.createDirectories(Path.of(path));
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать директорию для хранения файлов: " + path, e);
        }
    }

    /**
     * Сохраняет файл: записывает содержимое в файловую систему и метаинформацию в БД.
     * @param fileDto объект с данными файла (имя и содержимое)
     * @return сохраненный файл с присвоенным ID
     */
    @Override
    public File save(FileDto fileDto) {
        // Генерируем уникальный путь для файла
        var path = getNewFilePath(fileDto.getName());
        // Записываем содержимое файла на диск
        writeFileBytes(path, fileDto.getContent());
        // Сохраняем метаинформацию в БД и возвращаем результат
        return fileRepository.save(new File(fileDto.getName(), path));
    }

    /**
     * Генерирует уникальный путь для нового файла.
     * @param sourceName оригинальное имя файла
     * @return уникальный путь к файлу
     */
    private String getNewFilePath(String sourceName) {
        // Используем UUID для обеспечения уникальности имен файлов
        return storageDirectory + java.io.File.separator + UUID.randomUUID() + sourceName;
    }

    /**
     * Записывает массив байтов в файл по указанному пути.
     * @param path путь к файлу
     * @param content содержимое файла в виде массива байтов
     */
    private void writeFileBytes(String path, byte[] content) {
        try {
            Files.write(Path.of(path), content);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось записать файл: " + path, e);
        }
    }

    /**
     * Получает файл по ID: читает содержимое из файловой системы и возвращает как DTO.
     * @param id идентификатор файла
     * @return Optional с данными файла или пустой Optional, если файл не найден
     */
    @Override
    public Optional<FileDto> getFileById(int id) {
        // Ищем файл в БД по ID
        var fileOptional = fileRepository.findById(id);
        if (fileOptional.isEmpty()) {
            return Optional.empty();
        }
        // Читаем содержимое файла из файловой системы
        var content = readFileAsBytes(fileOptional.get().getPath());
        // Возвращаем DTO с именем и содержимым файла
        return Optional.of(new FileDto(fileOptional.get().getName(), content));
    }

    /**
     * Читает файл из файловой системы и возвращает его содержимое как массив байтов.
     * @param path путь к файлу
     * @return содержимое файла в виде массива байтов
     */
    private byte[] readFileAsBytes(String path) {
        try {
            return Files.readAllBytes(Path.of(path));
        } catch (IOException e) {
            throw new RuntimeException("Не удалось прочитать файл: " + path, e);
        }
    }

    /**
     * Удаляет файл: сначала из файловой системы, затем запись из БД.
     * @param id идентификатор файла для удаления
     */
    @Override
    public void deleteById(int id) {
        // Ищем файл в БД для получения пути к файлу в файловой системе
        var fileOptional = fileRepository.findById(id);
        if (fileOptional.isPresent()) {
            // Удаляем файл из файловой системы
            deleteFile(fileOptional.get().getPath());
            // Удаляем запись о файле из БД
            fileRepository.deleteById(id);
        }
    }

    /**
     * Удаляет файл из файловой системы.
     * @param path путь к файлу
     */
    private void deleteFile(String path) {
        try {
            Files.deleteIfExists(Path.of(path));
        } catch (IOException e) {
            throw new RuntimeException("Не удалось удалить файл: " + path, e);
        }
    }
}