package com.zone01.lets_play.token;


import com.zone01.lets_play.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "tokens")
public class Token {

    @Id
    private String id;

    @Indexed(unique = true)
    private String token;

    @DBRef
    private User user;

    private boolean revoked;
    private boolean expired;
}
