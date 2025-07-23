package com.forkmyfolio.repository;

import com.forkmyfolio.model.PortfolioProfile;
import com.forkmyfolio.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PortfolioProfileRepository extends JpaRepository<PortfolioProfile, Long> {

    @EntityGraph(attributePaths = "user")
    Optional<PortfolioProfile> findByUser(User user);

    void deleteByUser(User user);
}