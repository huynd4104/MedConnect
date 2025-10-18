package com.medconnect.repository;

import com.medconnect.entity.Token;
import com.medconnect.entity.Token.TokenType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Integer> {
    @Query("SELECT t FROM Token t WHERE t.token = :token AND t.tokenType = :type AND t.used = false")
    Optional<Token> findValidByTokenAndType(String token, TokenType type);
}