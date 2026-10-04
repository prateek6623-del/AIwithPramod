package com.qa.salesforce.tests;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import com.qa.salesforce.base.BaseTest;
import com.qa.salesforce.exceptions.FrameworkException;
import com.qa.salesforce.pages.LoginPage;

/**
 * Verifies that the core controls of the Salesforce login page are rendered and
 * visible. No credentials are used — this test only navigates and inspects the UI.
 */
public class LoginPageElementsTest extends BaseTest {

    @Test(groups = "loginPage", description = "Core login page elements are displayed")
    public void coreElementsAreDisplayed() {
        try {
            LoginPage loginPage = new LoginPage(getDriver()).open(CONFIG.getSalesforceUrl());

            SoftAssert soft = new SoftAssert();
            soft.assertTrue(loginPage.isUsernameDisplayed(), "Username field should be displayed");
            soft.assertTrue(loginPage.isLoginButtonDisplayed(), "Login button should be displayed");
            soft.assertTrue(loginPage.isRememberMeDisplayed(), "Remember-me checkbox should be displayed");
            soft.assertTrue(loginPage.isForgotPasswordLinkDisplayed(), "Forgot-password link should be displayed");
            soft.assertTrue(loginPage.isSignupLinkDisplayed(), "'Try for Free' sign-up link should be displayed");
            soft.assertAll();
        } catch (FrameworkException e) {
            Assert.fail("Failed while verifying login page elements: " + e.getMessage(), e);
        }
    }
}
