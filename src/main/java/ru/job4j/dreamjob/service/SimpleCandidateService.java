package ru.job4j.dreamjob.service;

import net.jcip.annotations.ThreadSafe;
import org.springframework.stereotype.Service;
import ru.job4j.dreamjob.dto.FileDto;
import ru.job4j.dreamjob.model.Candidate;
import ru.job4j.dreamjob.repository.CandidateRepository;

import java.util.Collection;
import java.util.Optional;

@ThreadSafe
@Service
public class SimpleCandidateService implements CandidateService {

    private final CandidateRepository candidateRepository;
    private final FileService fileService;

    public SimpleCandidateService(CandidateRepository candidateRepository, FileService fileService) {
        this.candidateRepository = candidateRepository;
        this.fileService = fileService;
    }

    @Override
    public Candidate saveCandidate(Candidate candidate, FileDto image) {
        saveNewFile(candidate, image);
        return candidateRepository.saveCandidate(candidate);
    }

    private void saveNewFile(Candidate candidate, FileDto image) {
        var file = fileService.save(image);
        candidate.setFileId(file.getId());
    }

    @Override
    public boolean deleteByIdCandidate(int id) {
        var candidateOptional = findByIdCandidate(id);
        if (candidateOptional.isPresent()) {
            candidateRepository.deleteByIdCandidate(id);
            fileService.deleteById(candidateOptional.get().getFileId());
            return true;
        }
        return false;
    }

    @Override
    public boolean updateCandidate(Candidate candidate, FileDto image) {
        var isNewFileEmpty = image.getContent().length == 0;
        if (isNewFileEmpty) {
            return candidateRepository.updateCandidate(candidate);
        }

        var oldFileId = candidate.getFileId();
        saveNewFile(candidate, image);
        var isUpdated = candidateRepository.updateCandidate(candidate);
        fileService.deleteById(oldFileId);
        return isUpdated;
    }

    @Override
    public Optional<Candidate> findByIdCandidate(int id) {
        return candidateRepository.findByIdCandidate(id);
    }

    @Override
    public Collection<Candidate> findAllCandidate() {
        return candidateRepository.findAllCandidate();
    }
}