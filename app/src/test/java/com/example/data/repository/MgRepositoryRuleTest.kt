package com.example.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.base.FirestoreEmulatorTestBase
import com.example.data.local.AppDatabase
import com.example.data.model.Comment
import com.example.data.model.User
import com.example.data.model.Video
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MgRepositoryRuleTest : FirestoreEmulatorTestBase() {

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
  fun createUser_authenticatedUser_createsDocumentSuccessfully() = runBlocking {
    val uid = signInTestUser(ALICE_EMAIL)
    val user = User(
      id = uid,
      username = "alice_lg",
      displayName = "Alice Rivera",
      email = ALICE_EMAIL
    )
    val result: Result<Unit> = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.createFirestoreUser(user)
    }
    assertTrue(result.isSuccess)
  }

  @Test
  fun createVideo_authenticatedCreator_createsVideoDocument() = runBlocking {
    val uid = signInTestUser(ALICE_EMAIL)
    val video = Video(
      videoId = "vid_alice_1",
      creatorId = uid,
      creatorUsername = "alice_lg",
      creatorDisplayName = "Alice Rivera",
      title = "Liquid Glass Demo",
      description = "Next gen UI demo",
      category = "Technology"
    )
    val result: Result<String> = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.createFirestoreVideo(video)
    }
    assertTrue(result.isSuccess)
    assertEquals("vid_alice_1", result.getOrNull())
  }

  @Test
  fun createVideo_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
    auth.signOut()
    val video = Video(
      videoId = "vid_unauth_1",
      creatorId = "unauth_user",
      creatorUsername = "unauth",
      creatorDisplayName = "Unauth",
      title = "Unauth Video",
      description = "Should fail",
      category = "Technology"
    )
    val result: Result<String> = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.createFirestoreVideo(video)
    }
    assertTrue(result.isFailure)
    val exception = result.exceptionOrNull()
    assertTrue(exception is FirebaseFirestoreException)
    val fsException = exception as FirebaseFirestoreException
    assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsException.code)
  }

  @Test
  fun updateUserPresence_authenticatedUser_succeeds() = runBlocking {
    val uid = signInTestUser(ALICE_EMAIL)
    val result: Result<Unit> = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.setFirestorePresence(uid, isOnline = true)
    }
    assertTrue(result.isSuccess)
  }

  @Test
  fun createComment_authenticatedUser_succeeds() = runBlocking {
    val uid = signInTestUser(ALICE_EMAIL)
    val comment = Comment(
      commentId = "c_1",
      videoId = "vid_alice_1",
      userId = uid,
      username = "alice_lg",
      text = "Incredible liquid glass aesthetics!"
    )
    val result: Result<Unit> = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.createFirestoreComment(comment)
    }
    assertTrue(result.isSuccess)
  }

  @Test
  fun likeVideo_authenticatedUser_succeeds() = runBlocking {
    val uid = signInTestUser(ALICE_EMAIL)
    val result: Result<Unit> = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.createFirestoreLike("vid_alice_1", uid)
    }
    assertTrue(result.isSuccess)
  }

  private companion object {
    const val ALICE_EMAIL = "alice@lg.app"
    const val DEFAULT_TIMEOUT_MS = 5000L
  }
}
