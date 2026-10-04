package com.qa.salesforce.pages;

import java.time.Duration;

import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.qa.salesforce.exceptions.FrameworkException;
import com.qa.salesforce.utils.ConfigReader;

/**
 * Page Object for the Salesforce login page
 * ({@code https://login.salesforce.com/?locale=in}).
 *
 * <p>The page is identity-first: the initial form exposes a username field and a
 * Login button (plus remember-me, forgot-password, sign-up and My Domain
 * controls). The password field only appears after the username step, so this
 * object verifies the elements rendered on load rather than driving a login.</p>
 *
 * <p>Elements are proxied by {@link PageFactory} with an
 * {@link AjaxElementLocatorFactory} so every interaction waits for the element to
 * appear before failing. Public actions translate low-level Selenium exceptions
 * into a {@link FrameworkException} that carries context.</p>
 */
public class LoginPage {

    private final WebDriver driver;
    private final int timeoutSeconds;

    @FindBy(xpath = "//input[@id='username']")
    private WebElement username;

    @FindBy(xpath = "//input[@id='Login']")
    private WebElement loginButton;

    @FindBy(xpath = "//input[@id='rememberUn']")
    private WebElement rememberMeCheckbox;

    @FindBy(xpath = "//a[@id='forgot_password_link']")
    private WebElement forgotPasswordLink;

    @FindBy(xpath = "//a[@id='signup_link']")
    private WebElement signupLink;

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.timeoutSeconds = ConfigReader.getInstance().getExplicitWait();
        PageFactory.initElements(new AjaxElementLocatorFactory(driver, timeoutSeconds), this);
    }

    /** Navigates to {@code url} and waits until the login form is rendered. */
    public LoginPage open(String url) {
        try {
            driver.get(url);
            waitUntilVisible(username);
            return this;
        } catch (Exception e) {
            throw new FrameworkException("Failed to open the Salesforce login page at '" + url + "'", e);
        }
    }

    public boolean isUsernameDisplayed() {
        return isDisplayed(username, "username");
    }

    public boolean isLoginButtonDisplayed() {
        return isDisplayed(loginButton, "login button");
    }

    public boolean isRememberMeDisplayed() {
        return isDisplayed(rememberMeCheckbox, "remember-me checkbox");
    }

    public boolean isForgotPasswordLinkDisplayed() {
        return isDisplayed(forgotPasswordLink, "forgot-password link");
    }

    public boolean isSignupLinkDisplayed() {
        return isDisplayed(signupLink, "sign-up link");
    }

    /** Returns an attribute (e.g. {@code type}, {@code autocomplete}) of the username field. */
    public String getUsernameAttribute(String attribute) {
        try {
            return waitUntilVisible(username).getAttribute(attribute);
        } catch (Exception e) {
            throw new FrameworkException("Failed to read the username field attribute '" + attribute + "'", e);
        }
    }

    /** Returns the visible label of the Login button. */
    public String getLoginButtonValue() {
        try {
            return waitUntilVisible(loginButton).getAttribute("value");
        } catch (Exception e) {
            throw new FrameworkException("Failed to read the login button label", e);
        }
    }

    /** Returns the target of the "Forgot Your Password?" link. */
    public String getForgotPasswordHref() {
        try {
            return waitUntilVisible(forgotPasswordLink).getAttribute("href");
        } catch (Exception e) {
            throw new FrameworkException("Failed to read the forgot-password link target", e);
        }
    }

    /** Returns whether the remember-me checkbox is ticked; expected to be unchecked on load. */
    public boolean isRememberMeSelected() {
        try {
            return waitUntilVisible(rememberMeCheckbox).isSelected();
        } catch (Exception e) {
            throw new FrameworkException("Failed to read the remember-me checkbox state", e);
        }
    }

    private boolean isDisplayed(WebElement element, String name) {
        try {
            return waitUntilVisible(element).isDisplayed();
        } catch (TimeoutException e) {
            return false;
        } catch (Exception e) {
            throw new FrameworkException("Unexpected failure while checking the " + name, e);
        }
    }

    private WebElement waitUntilVisible(WebElement element) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
                .until(ExpectedConditions.visibilityOf(element));
    }
}
