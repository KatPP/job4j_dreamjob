package ru.job4j.dreamjob.service;

import ru.job4j.dreamjob.model.Candidate;
import ru.job4j.dreamjob.repository.CandidateRepository;
import ru.job4j.dreamjob.repository.MemoryCandidateRepository;

import java.util.Collection;
import java.util.Optional;

public class SimpleCandidateService implements CandidateService {

    private static final SimpleCandidateService INSTANCE_CANDIDATE = new SimpleCandidateService();

    private final CandidateRepository candidateRepository = MemoryCandidateRepository.getInstance();

    private SimpleCandidateService() { }

    public static SimpleCandidateService getInstance() {
        return INSTANCE_CANDIDATE;
    }

    @Override
    public Candidate saveCandidate(Candidate candidate) {
        return candidateRepository.saveCandidate(candidate);
    }

    @Override
    public boolean deleteByIdCandidate(int id) {
        return candidateRepository.deleteByIdCandidate(id);
    }

    @Override
    public boolean updateCandidate(Candidate candidate) {
        return candidateRepository.updateCandidate(candidate);
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
