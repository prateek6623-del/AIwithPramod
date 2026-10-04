# Salesforce Login Page — Selenium + TestNG POM Framework

Enterprise-grade Page Object framework that navigates to the Salesforce login page
(`https://login.salesforce.com/?locale=in`) and verifies the UI elements it renders.

> The original request mixed **Playwright + TypeScript** with **TestNG / `PageFactory` / `@FindBy` / Maven**,
> which cannot coexist. This project implements the **Selenium + Java + TestNG** interpretation.
> It also verifies the rendered elements instead of performing a login: the page is
> **identity-first** — it shows only `#username` + `#Login` on load, and the password field
> appears only after the username step. No credentials are required.

## Stack

| Concern | Choice |
| --- | --- |
| Language / JDK | Java 17 |
| Browser driver | Selenium 4.25 (`selenium-java`) |
| Driver binaries | WebDriverManager 5.9 (auto-downloads chromedriver) |
| Test runner | TestNG 7.10 |
| Config | dotenv-java 3.0 (`system property → env var → .env`) |
| Build | Maven, Surefire wired to `testng.xml` |
| Browser | Chromium (Chrome) only |

## Prerequisites
- JDK 17 or newer (`java -version`)
- Maven 3.9 or newer (`mvn -version`)
- Google Chrome installed

## Setup
1. Optionally copy the sample config to override defaults:
   ```
   cp .env.example .env
   ```
   ```
   SF_URL=https://login.salesforce.com/?locale=in
   HEADLESS=false
   EXPLICIT_WAIT=20
   ```
   Values can also be supplied as environment variables or JVM properties, e.g.
   `mvn test -DHEADLESS=true`.

2. Run the suite:
   ```
   mvn clean test
   ```

3. Run headless:
   ```
   mvn clean test -DHEADLESS=true
   ```

## What the tests cover
| Test class | Case | Assertion |
| --- | --- | --- |
| `LoginPageElementsTest` | Presence | Username, Login button, remember-me, forgot-password and sign-up controls are displayed |
| `LoginPageAttributesTest` | Attributes | Username `type=email` / `autocomplete=username`, Login label `Log In`, forgot-password target, remember-me unchecked |

Both use `SoftAssert` so every element is checked and all failures are reported at once.

## Project structure
```
salesforce-login/
├── pom.xml                       Maven build + Surefire → testng.xml
├── testng.xml                    Suite definition
├── .env.example                  Optional config overrides
├── .gitignore                    Excludes .env, target/, IDE files
└── src/
    ├── main/java/com/qa/salesforce/
    │   ├── pages/LoginPage.java          Page Object (@FindBy + PageFactory)
    │   ├── utils/ConfigReader.java       Layered configuration reader
    │   └── exceptions/FrameworkException.java
    └── test/java/com/qa/salesforce/
        ├── base/BaseTest.java            @BeforeMethod/@AfterMethod driver lifecycle
        └── tests/
            ├── LoginPageElementsTest.java
            └── LoginPageAttributesTest.java
```

## Elements verified
| Element | Locator | Notes |
| --- | --- | --- |
| Username | `//input[@id='username']` | `type="email"`, `autocomplete="username"` |
| Login button | `//input[@id='Login']` | label `Log In` |
| Remember me | `//input[@id='rememberUn']` | unchecked on load |
| Forgot password | `//a[@id='forgot_password_link']` | targets the recovery page |
| Sign up | `//a[@id='signup_link']` | "Try for Free" |

## Design notes
- **Page Object Model:** a single `LoginPage` owns locators and interactions.
- **PageFactory:** elements are proxied via `AjaxElementLocatorFactory` with the configured
  timeout, so every interaction waits for presence before failing.
- **Explicit waits only:** `WebDriverWait` throughout; no `Thread.sleep`.
- **Exception handling:** page actions wrap failures in `FrameworkException` with context;
  tests catch it and fail with actionable messages.
- **Thread-safe:** the driver is held in a `ThreadLocal`, ready for parallel execution.

## Troubleshooting
- **Different login variants:** the identity-first layout is what `login.salesforce.com` serves.
  A sandbox or My Domain URL may expose a different set of elements — update the locators in
  `LoginPage` and set `SF_URL` accordingly.
- **Region/locale:** `?locale=in` selects the India locale; change `SF_URL` for another region.
