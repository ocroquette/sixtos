package com.github.ocroquette.sixtos.core

import com.github.ocroquette.sixtos.core.CredentialsFile
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class CredentialFileTest extends Specification {

    static final DIR = "src/test/resources/extools"

    @Rule
    final TemporaryFolder temporaryFolder = new TemporaryFolder()

    def "compute hash"() {
        expect:
        CredentialsFile.generateSaltedHash("abc123","12345678901234567890123456789012") == "12345678901234567890123456789012e382e257d39b03c13ae8af497cabffc4602f349a"
    }

    def "create credential files and validate credential"() {
        given:
        File file = temporaryFolder.newFile()
        file.delete()

        def username = "username"
        def password = "password"
        CredentialsFile credentialsFileWrite = CredentialsFile.with(file)

        when:
        credentialsFileWrite.addUser(username, password, ["ADMIN", "READER"])

        CredentialsFile credentialsFileRead = CredentialsFile.with(file)

        then:
        ! credentialsFileRead.validateCredentials(username, password + "x")

        credentialsFileRead.validateCredentials(username, password)
        credentialsFileRead.getRoles(username) == ["ADMIN", "READER"]
    }
}
