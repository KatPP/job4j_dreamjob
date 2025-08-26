package ru.job4j.dreamjob.service;

import org.springframework.stereotype.Service;
import ru.job4j.dreamjob.model.Candidate;
import ru.job4j.dreamjob.repository.CandidateRepository;

import java.util.Collection;
import java.util.Optional;
import net.jcip.annotations.ThreadSafe;

@ThreadSafe
@Service
public class SimpleCandidateService implements CandidateService {

    private final CandidateRepository candidateRepository;

    private SimpleCandidateService(CandidateRepository candidateRepository) {
        this.candidateRepository = candidateRepository;
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