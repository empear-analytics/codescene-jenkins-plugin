package org.jenkinsci.plugins.codescene.Domain;

import org.junit.Test;

import static org.junit.Assert.*;

public class CodeSceneUserTest {

    @Test
    public void encodesUserNameAndPassword() {
        final String name = "Bot";
        final String password = "BotPassword";

        final CodeSceneUser user = new CodeSceneUser(name, password);

        // Dobule checked with https://www.base64encode.org/
        assertEquals("Qm90OkJvdFBhc3N3b3Jk", user.asBase64Encoded());
    }
}