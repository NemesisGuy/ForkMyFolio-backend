package com.forkmyfolio.repository;

import com.forkmyfolio.model.PortfolioProfile;
import com.forkmyfolio.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PortfolioProfileRepository extends JpaRepository<PortfolioProfile, Long> {

    // Method to find a profile by the owner's User entity.
    Optional<PortfolioProfile> findByUser(User user);

    void deleteByUser(User user);
    @Query("SELECT p FROM PortfolioProfile p JOIN FETCH p.user WHERE p.user = :user")
    Optional<PortfolioProfile> findByUserWithUserEagerly(@Param("user") User user);
}