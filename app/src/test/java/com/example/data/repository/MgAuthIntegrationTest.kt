package com.example.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.base.FirestoreEmulatorTestBase
import com.example.data.local.AppDatabase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.net.HttpURLConnection
import java.net.URL

class MgAuthIntegrationTest : FirestoreEmulatorTestBase() {

    private lateinit var repository: MgRepository
    private lateinit var db: AppDatabase

    @Before
    override fun setUpFirebase() {
        super.setUpFirebase()
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = MgRepository(dao = db.mgDao(), firestore = firestore, auth = auth)
    }

    @Test
    fun register_withInvalidEmail_failsValidation() = runBlocking {
        val result = repository.registerWithEmail(
            email = "invalid-email-string",
            password = "StrongPassword123!",
            username = "test_user_inv",
            displayName = "Test User"
        )
        assertFalse(result.isSuccess)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun register_withWeakPassword_failsValidation() = runBlocking {
        val result = repository.registerWithEmail(
            email = "valid.user@gmail.com",
            password = "weak",
            username = "test_user_weak",
            displayName = "Test User"
        )
        assertFalse(result.isSuccess)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun register_duplicateAccount_failsWithAlreadyExists() = runBlocking {
        val email = "duplicate.test.${System.currentTimeMillis()}@gmail.com"
        val password = "StrongPassword123!"

        // First registration succeeds in Firebase
        val firstResult = repository.registerWithEmail(
            email = email,
            password = password,
            username = "dup_user_${System.currentTimeMillis() % 10000}",
            displayName = "First User"
        )
        assertTrue("First registration must succeed", firstResult.isSuccess)

        // Second registration with the same email must be rejected
        val duplicateResult = repository.registerWithEmail(
            email = email,
            password = password,
            username = "other_user_${System.currentTimeMillis() % 10000}",
            displayName = "Duplicate User"
        )
        assertFalse("Duplicate registration must fail", duplicateResult.isSuccess)
        assertTrue(
            duplicateResult.exceptionOrNull()?.message?.contains("already exists", ignoreCase = true) == true
        )
    }

    @Test
    fun register_accessibleEmail_remainsUnverifiedUntilConfirmed() = runBlocking {
        val email = "unverified.user.${System.currentTimeMillis()}@gmail.com"
        val password = "StrongPassword123!"
        val username = "unver_${System.currentTimeMillis() % 10000}"

        val regResult = repository.registerWithEmail(
            email = email,
            password = password,
            username = username,
            displayName = "Unverified User"
        )
        assertTrue("Registration must succeed in Firebase Auth", regResult.isSuccess)

        // Session must NOT be active while unverified
        assertNull("Current user session must remain null before verification", repository.currentUserValue)

        // Checking status while unverified must fail
        val fbUser = regResult.getOrThrow()
        val checkStatusResult = repository.checkEmailVerificationStatus(fbUser, username, "Unverified User")
        assertFalse("Verification status check must fail before email link is clicked", checkStatusResult.isSuccess)

        // Attempting to login while unverified must be rejected
        val loginResult = repository.loginWithEmail(email, password)
        assertFalse("Login must be blocked for unverified accounts", loginResult.isSuccess)
        assertTrue(loginResult.exceptionOrNull() is UnverifiedEmailException)
    }

    @Test
    fun register_followedByEmailVerification_allowsLoginAndGrantsAccess() = runBlocking {
        val email = "verified.user.${System.currentTimeMillis()}@gmail.com"
        val password = "StrongPassword123!"
        val username = "verified_${System.currentTimeMillis() % 10000}"

        val regResult = repository.registerWithEmail(
            email = email,
            password = password,
            username = username,
            displayName = "Verified User"
        )
        assertTrue(regResult.isSuccess)
        val fbUser = regResult.getOrThrow()

        // Simulate user clicking email verification link via emulator API
        markEmailVerifiedInAuthEmulator(email)

        // Check verification status now
        val statusResult = repository.checkEmailVerificationStatus(fbUser, username, "Verified User")
        assertTrue("Verification status check must succeed after verification", statusResult.isSuccess)

        val user = statusResult.getOrThrow()
        assertTrue(user.isVerified)
        assertEquals(email, user.email)

        // Now login must succeed!
        val loginResult = repository.loginWithEmail(email, password)
        assertTrue("Login must succeed after email is verified", loginResult.isSuccess)
        assertEquals(user.id, loginResult.getOrThrow().id)
    }

    private fun markEmailVerifiedInAuthEmulator(email: String) {
        val oobUrl = URL("http://$EMULATOR_HOST:$AUTH_PORT/emulator/v1/projects/$PROJECT_ID/oobCodes")
        val oobConn = oobUrl.openConnection() as HttpURLConnection
        val oobResponse = oobConn.inputStream.bufferedReader().readText()
        oobConn.disconnect()

        val regex = Regex(""""email":"${Regex.escape(email)}"[^}]*"oobCode":"([^"]+)"""")
        val match = regex.find(oobResponse) ?: throw IllegalStateException("No oobCode found for $email in $oobResponse")
        val oobCode = match.groupValues[1]

        val updateUrl = URL("http://$EMULATOR_HOST:$AUTH_PORT/identitytoolkit.googleapis.com/v1/accounts:update?key=fake-api-key-for-emulator")
        val updateConn = updateUrl.openConnection() as HttpURLConnection
        updateConn.requestMethod = "POST"
        updateConn.setRequestProperty("Content-Type", "application/json")
        updateConn.doOutput = true
        val payload = """{"oobCode":"$oobCode"}"""
        updateConn.outputStream.use { it.write(payload.toByteArray()) }
        assertEquals(200, updateConn.responseCode)
        updateConn.disconnect()
    }
}
