package com.interviewprep.platform.repository;

import com.interviewprep.platform.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    @Query("select rt from RefreshToken rt join fetch rt.user u left join fetch u.roles where rt.token = :token")
    Optional<RefreshToken> findByTokenWithUserAndRoles(String token);
}
