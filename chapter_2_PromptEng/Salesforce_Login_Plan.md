# Salesforce Login — Enterprise Selenium + TestNG POM Framework

## What I understood (mapped to your RICEPOT template)

- **R — Role:** QA automation engineer building an enterprise-grade automation framework.
- **I — Instructions:** Automate and verify `https://login.salesforce.com/?locale=in` (valid + invalid cases) using Page Object Model with `PageFactory`/`@FindBy`, TestNG annotations, and robust exception handling in both pages and tests.
- **C — Context:** Greenfield repo (`D:\AI_Testing_Pramod_Dutta`) currently holds only docs. No build files exist. Login page is standard Salesforce: `#username`, `#password`, `#Login`, error banner `#error`.
- **E — Expected:** Runnable Maven project that passes a valid-login test and an invalid-login test on Chromium.
- **P — Parameters:** Production-level, near-zero bad practice; external/staging URLs and credentials supplied out-of-band.
- **O — Output:** 1 Page Object file + 2 TestNG test scripts inside a Maven project.
- **T — Tone:** Technical, precise, enterprise-grade.

### One correction I made
Your prompt mixed **Playwright + TypeScript** with **TestNG / `PageFactory` / `@FindBy` / Maven**. Those cannot coexist. Per your selection, this plan builds **Selenium + Java + TestNG** and drops Playwright entirely.

## Decisions (from your answers)
- Stack: **Selenium 4 + Java 17 + TestNG + Maven**
- Coverage: **Chromium only**, one valid-login test + one invalid-login test
- Location: **repo-root project** `salesforce-login/`
- Scope: full POM framework (base test, config, explicit waits, README)
- Credentials: **environment variables / `.env`**, nothing secret committed

## Target structure
```
salesforce-login/
├── pom.xml
├── testng.xml
├── .env.example
├── .gitignore
├── README.md
└── src/
    ├── main/java/com/qa/salesforce/
    │   ├── pages/LoginPage.java            # the single Page Object
    │   ├── utils/ConfigReader.java         # system prop → env → .env
    │   └── exceptions/FrameworkException.java
    └── test/java/com/qa/salesforce/
        ├── base/BaseTest.java              # driver setup/teardown
        └── tests/
            ├── ValidLoginTest.java         # test script 1
            └── InvalidLoginTest.java       # test script 2
```

## Files to create

### `pom.xml`
- `maven.compiler.release=17`, UTF-8.
- Dependencies: `org.seleniumhq.selenium:selenium-java:4.25.0`, `org.testng:testng:7.10.2`, `io.github.bonigarcia:webdrivermanager:5.9.2`, `io.github.cdimascio:dotenv-java:3.0.0`.
- `maven-surefire-plugin` 3.5.2 wired to `testng.xml`.

### `src/main/java/com/qa/salesforce/utils/ConfigReader.java`
- Singleton reading, in order: JVM system property (`-DSF_URL=...`) → OS env var → `.env` file (dotenv-java).
- Keys: `SF_URL` (default `https://login.salesforce.com/?locale=in`), `SF_USERNAME`, `SF_PASSWORD`, `BROWSER` (default `chrome`), `HEADLESS` (default `false`), `EXPLICIT_WAIT` (default `20`).
- `getRequired(key)` throws `FrameworkException` when a mandatory value (username/password) is missing.

### `src/main/java/com/qa/salesforce/exceptions/FrameworkException.java`
- Unchecked `RuntimeException` with `(String message)` and `(String message, Throwable cause)` constructors.

### `src/main/java/com/qa/salesforce/pages/LoginPage.java`  *(the required Page Object)*
- Fields with `@FindBy`:
  - `//input[@id='username']` → `username`
  - `//input[@id='password']` → `password`
  - `//input[@id='Login']` → `loginButton`
  - `//div[@id='error']` → `errorMessage`
- Constructor initializes with lazy locators + timeout:
  `PageFactory.initElements(new AjaxElementLocatorFactory(driver, EXPLICIT_WAIT), this);`
- Methods (each wrapped in try-catch, rethrowing `FrameworkException` with context):
  - `open(String url)` — navigate + wait for `username` to be clickable.
  - `enterUsername(String)`, `enterPassword(String)`, `clickLogin()`.
  - `doLogin(String user, String pass)` — calls the three above (signature required by your prompt).
  - `getErrorMessage()` / `isLoginErrorDisplayed()` — explicit-wait then return text/visibility.

### `src/test/java/com/qa/salesforce/base/BaseTest.java`
- `ThreadLocal<WebDriver>` for parallel readiness; `getDriver()` accessor.
- `@BeforeMethod`: WebDriverManager `chromedriver().setup()`, build `ChromeOptions` (headless only when `HEADLESS=true`), maximize, set implicit wait to 0 and page-load timeout, `driver.manage()` timeouts; navigate to `SF_URL`.
- `@AfterMethod(alwaysRun = true)`: `quit()` + remove from ThreadLocal, guarded against null.
- `@BeforeSuite`/`@AfterSuite` optional logging hooks.

### `src/test/java/com/qa/salesforce/tests/ValidLoginTest.java`
- `extends BaseTest`.
- `@Test(groups="login", description="Valid credentials reach Salesforce Home")`.
- Read `SF_USERNAME`/`SF_PASSWORD` via `ConfigReader`; `new LoginPage(getDriver()).doLogin(...)`.
- Assert success with an explicit wait for the App Launcher / Home marker, plus `Assert.assertTrue(url.contains("lightning") || url.contains("home"), ...)`.
- Catch `TimeoutException` → `Assert.fail("Login did not reach Home — check credentials or identity verification.", e)`.

### `src/test/java/com/qa/salesforce/tests/InvalidLoginTest.java`
- `extends BaseTest`.
- `@Test(groups="login", description="Invalid credentials show error banner")`.
- `doLogin("invalid@user.com", "WrongPassword123")`.
- Assert `isLoginErrorDisplayed()` and error text contains "check your username and password" (case-insensitive).

### `testng.xml`
- Single suite `Salesforce Login`, one test containing both classes, `verbose="2"`.

### `.env.example`, `.gitignore`, `README.md`
- `.env.example`: `SF_URL=`, `SF_USERNAME=`, `SF_PASSWORD=`, `HEADLESS=false`, `EXPLICIT_WAIT=20`.
- `.gitignore`: `.env`, `target/`, IDE files.
- `README.md`: prerequisites (JDK 17, Maven 3.9+), how to fill `.env`, `mvn clean test`, how to switch to a sandbox/My Domain URL.

## Constraints honored
- Exactly **1 Page Object** (`LoginPage`); success/failure assertions live in the tests via URL + explicit wait so no second page object is introduced.
- TestNG `@Test` + `@BeforeMethod`/`@AfterMethod` setup/teardown used.
- `try-catch` in both the page object and the test scripts.
- No committed secrets — all via env/`.env`.

## Risks & mitigations
- **Identity verification on new IP:** first real login from a fresh machine may show a "verify your identity" screen, failing the valid test. Mitigation: pre-verify the org once, or use a staging/My Domain URL via `SF_URL`.
- **Bot detection / CAPTCHA:** repeated failed logins can trigger CAPTCHA. Mitigation: keep the invalid case to a clearly-bad username and low retries.
- **Flakiness:** all interactions use explicit waits (`AjaxElementLocatorFactory` + `WebDriverWait`), no `Thread.sleep`.

## Verification
1. `cd salesforce-login`
2. `mvn -q clean test` → Surefire runs `testng.xml`.
3. Expected: `InvalidLoginTest` passes without credentials; `ValidLoginTest` passes once `.env` has valid `SF_USERNAME`/`SF_PASSWORD` (otherwise it fails fast with a clear `ConfigReader` message).
4. `mvn -q -DHEADLESS=true test` to confirm headless Chromium works.
5. Confirm no `.env` appears in git status (repo is not yet a git repo — `git init` first if version control is wanted).
