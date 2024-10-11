package com.zone01.lets_play.token;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends MongoRepository<Token, String> {
    // Custom MongoDB query to find valid tokens by userId
    @Query("{ 'user.id': ?0, $or: [{'expired': false}, {'revoked': false}] }")
    List<Token> findAllValidTokenByUser(String userId);

    // Find a token by its string value
    Optional<Token> findByToken(String token);
}
