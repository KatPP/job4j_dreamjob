package ru.job4j.dreamjob.repository;

import org.springframework.stereotype.Repository;
import ru.job4j.dreamjob.model.Candidate;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class MemoryCandidateRepository implements CandidateRepository {

    private static final MemoryCandidateRepository INSTANCE_NEW = new MemoryCandidateRepository();

    private int nextId = 1;

    private final Map<Integer, Candidate> candidaties = new HashMap<>();

    private MemoryCandidateRepository() {
        saveCandidate(new Candidate(0, "Bob", "просто описание", LocalDateTime.now()));
        saveCandidate(new Candidate(0, "Cat", "просто описание", LocalDateTime.now()));
        saveCandidate(new Candidate(0, "Robert", "просто описание", LocalDateTime.now()));
        saveCandidate(new Candidate(0, "Pavel", "просто описание", LocalDateTime.now()));
        saveCandidate(new Candidate(0, "Max", "просто описание", LocalDateTime.now()));
        saveCandidate(new Candidate(0, "Fred", "просто описание", LocalDateTime.now()));
    }

    public static MemoryCandidateRepository getInstance() {
        return INSTANCE_NEW;
    }

    @Override
    public void saveCandidate(Candidate candidate) {
        candidate.setId(nextId++);
        candidate.setCreationDate(LocalDateTime.now());
        candidaties.put(candidate.getId(), candidate);
    }

    @Override
    public boolean deleteByIdCandidate(int id) {
        Candidate removed = candidaties.remove(id);
        return removed != null;
    }

    @Override
    public boolean updateCandidate(Candidate candidate) {
        return candidaties.computeIfPresent(candidate.getId(),
                (id, oldCandidate) -> new Candidate(
                        oldCandidate.getId(),
                        candidate.getName(),
                        candidate.getDescription(),
                        oldCandidate.getCreationDate()
                )) != null;
    }

    @Override
    public Optional<Candidate> findByIdCandidate(int id) {
        return Optional.ofNullable(candidaties.get(id));
    }

    @Override
    public Collection<Candidate> findAllCandidate() {
        return candidaties.values();
    }
}