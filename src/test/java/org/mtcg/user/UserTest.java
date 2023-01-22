package org.mtcg.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User user;
    @BeforeEach
    void init() {
        user = new User("lucasHausi","secret5");
    }
    @Test
    void testEncryptSHA512() {
        //Arrange
        String password = "secret5";
        //Act
        String hashed = user.encryptSHA512(password);
        //Assert
        assertEquals("984d12ce7e9780c226a98d764ff36e4f84382b87444325c8bc2e7ec3b9c79a5f45007dc2132924737aa60a9a52774b0a68adb7da68e9d50955c93a9cbea37ee2", hashed);
    }
    @Test
    void testNewUserPW() {
        //Arrange
        String password = "secret5";
        //Act
        String hashed = user.encryptSHA512(password);
        //Assert
        assertEquals(hashed, user.getPassword());
    }
}