package ru.job4j.dreamjob.service;

import ru.job4j.dreamjob.model.Candidate;

import java.util.Collection;
import java.util.Optional;

public interface CandidateService {

    Candidate saveCandidate(Candidate candidate);

    boolean deleteByIdCandidate(int id);

    boolean updateCandidate(Candidate candidate);

    Optional<Candidate> findByIdCandidate(int id);

    Collection<Candidate> findAllCandidate();
}
