package com.forkmyfolio.repository;

import com.forkmyfolio.model.PortfolioProfile;
import com.forkmyfolio.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PortfolioProfileRepository extends JpaRepository<PortfolioProfile, Long> {

    // Method to find a profile by the owner's User entity.
    Optional<PortfolioProfile> findByUser(User user);
}