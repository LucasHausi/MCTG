package org.mtcg.repository;

import org.mtcg.user.User;

public interface UserRepository {
    User getUserByUsername(String username);

}
