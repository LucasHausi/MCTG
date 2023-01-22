package org.mtcg.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenControllerTest {

    @Test
    void generateToken(){
        //Arrange
        String username = "lucasHausi";
        //Act
        //Assert
        assertEquals("Basic lucasHausi-mtcgToken", TokenController.generateNewAuthToken(username));
    }

}