const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

// --- Security Bounds Tests ---

test("Unauthenticated user: cannot read users or videos", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").get());
  await assertFails(unauthDb.collection("videos").get());
});

test("Authenticated user: can create their own profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      username: "alice_lg",
      displayName: "Alice",
      email: "alice@lg.app",
    })
  );
});

test("Authenticated user: cannot update someone else's profile", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(BOB_UID).set({
      username: "bob_lg",
      displayName: "Bob",
      email: "bob@lg.app",
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).update({
      displayName: "Hacked",
    })
  );
});

test("Authenticated user: can publish video with valid fields", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("videos").doc("vid_1").set({
      creatorId: ALICE_UID,
      title: "Liquid Glass Demo",
      category: "Technology",
      views: 0,
      likes: 0,
    })
  );
});

test("Authenticated user: cannot publish video for another creatorId", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("videos").doc("vid_2").set({
      creatorId: BOB_UID,
      title: "Impersonated Video",
      category: "Technology",
    })
  );
});

test("Presence tracking: user can set their own presence", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("presence").doc(ALICE_UID).set({
      userId: ALICE_UID,
      isOnline: true,
    })
  );
});

test("Presence tracking: user cannot update another user's presence", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("presence").doc(BOB_UID).set({
      userId: BOB_UID,
      isOnline: false,
    })
  );
});
