package ru.job4j.dreamjob.repository;

import net.jcip.annotations.ThreadSafe;
import org.springframework.stereotype.Repository;
import ru.job4j.dreamjob.model.Candidate;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@ThreadSafe
@Repository
public class MemoryCandidateRepository implements CandidateRepository {

    private static final MemoryCandidateRepository INSTANCE_NEW = new MemoryCandidateRepository();

    private final AtomicInteger nextId = new AtomicInteger(1);

    private final Map<Integer, Candidate> candidates = new ConcurrentHashMap<>();

    private MemoryCandidateRepository() {
        saveCandidate(new Candidate(0, "Bob", "просто описание", LocalDateTime.now(), 1));
        saveCandidate(new Candidate(0, "Cat", "просто описание", LocalDateTime.now(), 1));
        saveCandidate(new Candidate(0, "Robert", "просто описание", LocalDateTime.now(), 2));
        saveCandidate(new Candidate(0, "Pavel", "просто описание", LocalDateTime.now(), 2));
        saveCandidate(new Candidate(0, "Max", "просто описание", LocalDateTime.now(), 2));
        saveCandidate(new Candidate(0, "Fred", "просто описание", LocalDateTime.now(), 3));
    }

    public static MemoryCandidateRepository getInstance() {
        return INSTANCE_NEW;
    }

    @Override
    public Candidate saveCandidate(Candidate candidate) {
        candidate.setId(nextId.getAndIncrement());
        candidate.setCreationDate(LocalDateTime.now());
        candidates.put(candidate.getId(), candidate);
        return candidate;
    }

    @Override
    public boolean deleteByIdCandidate(int id) {
        Candidate removed = candidates.remove(id);
        return removed != null;
    }

    @Override
    public boolean updateCandidate(Candidate candidate) {
        return candidates.computeIfPresent(candidate.getId(),
                (id, oldCandidate) -> new Candidate(
                        oldCandidate.getId(),
                        candidate.getName(),
                        candidate.getDescription(),
                        oldCandidate.getCreationDate(),
                        candidate.getCityId()
                )) != null;
    }

    @Override
    public Optional<Candidate> findByIdCandidate(int id) {
        return Optional.ofNullable(candidates.get(id));
    }

    @Override
    public Collection<Candidate> findAllCandidate() {
        return candidates.values();
    }
}