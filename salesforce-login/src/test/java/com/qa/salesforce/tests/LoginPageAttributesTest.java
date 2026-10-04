package com.qa.salesforce.tests;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import com.qa.salesforce.base.BaseTest;
import com.qa.salesforce.exceptions.FrameworkException;
import com.qa.salesforce.pages.LoginPage;

/**
 * Verifies the attributes and default state of the login page controls, so the
 * page is checked beyond simple presence. No credentials are used.
 */
public class LoginPageAttributesTest extends BaseTest {

    @Test(groups = "loginPage", description = "Login page elements expose the expected attributes")
    public void elementsExposeExpectedAttributes() {
        try {
            LoginPage loginPage = new LoginPage(getDriver()).open(CONFIG.getSalesforceUrl());

            SoftAssert soft = new SoftAssert();
            soft.assertEquals(loginPage.getUsernameAttribute("type"), "email",
                    "Username field should accept an email address");
            soft.assertEquals(loginPage.getUsernameAttribute("autocomplete"), "username",
                    "Username field should advertise the username autocomplete hint");
            soft.assertEquals(loginPage.getLoginButtonValue(), "Log In",
                    "Login button label should be 'Log In'");
            soft.assertTrue(loginPage.getForgotPasswordHref().contains("forgotpassword"),
                    "Forgot-password link should target the password recovery page");
            soft.assertFalse(loginPage.isRememberMeSelected(),
                    "Remember-me checkbox should be unchecked on load");
            soft.assertAll();
        } catch (FrameworkException e) {
            Assert.fail("Failed while verifying login page element attributes: " + e.getMessage(), e);
        }
    }
}
