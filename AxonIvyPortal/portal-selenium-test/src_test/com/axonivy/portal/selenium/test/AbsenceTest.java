package com.axonivy.portal.selenium.test;

import static com.axonivy.portal.selenium.common.Variable.HIDE_YEAR;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.axonivy.ivy.webtest.IvyWebTest;
import com.axonivy.portal.selenium.common.BaseTest;
import com.axonivy.portal.selenium.common.TestAccount;
import com.axonivy.portal.selenium.page.AbsencePage;
import com.axonivy.portal.selenium.page.NewAbsencePage;
import com.axonivy.portal.selenium.page.NewDashboardPage;
import com.axonivy.portal.selenium.page.TemplatePage;

import ch.ivy.addon.portalkit.enums.DeputyRoleType;


@IvyWebTest
public class AbsenceTest extends BaseTest {
  private static final LocalDate TODAY = LocalDate.now();
  private static final LocalDate YESTERDAY = TODAY.minusDays(1);
  private static final LocalDate TOMORROW = TODAY.plusDays(1);

  @Override
  @BeforeEach
  public void setup() {
    super.setupWithAlternativeLinkAndAccount(cleanUpAbsencesAndSubstituesLink, TestAccount.DEMO_USER);
    updatePortalSetting(HIDE_YEAR.getKey(), "false");
  }

  @Test
  public void whenLoginAsNormalUserThenManageAbsencesOfThatUser() {
    NewDashboardPage newDashboardPage = changeDateFormat();
    AbsencePage absencePage = openAbsencePage(newDashboardPage);
    createAbsenceForCurrentUser(YESTERDAY, YESTERDAY, "For travel", absencePage);
    createAbsenceForCurrentUser(TODAY, TODAY, "For party", absencePage);
    assertEquals(1, absencePage.countAbsences());
    absencePage.showAbsencesInThePast(true);
    assertEquals(2, absencePage.countAbsences());
  }

  @Test
  public void whenLoginAsAdminUserThenManageAbsencesOfAllUsers() {
    login(TestAccount.ADMIN_USER);
    NewDashboardPage newDashboardPage = changeDateFormat();
    AbsencePage absencePage = openAbsencePage(newDashboardPage);
    createAbsenceForCurrentUser(TODAY, TODAY, "For party", absencePage);
    String demoFullName = TestAccount.DEMO_USER.getFullName();
    createAbsence(demoFullName, YESTERDAY, YESTERDAY, "For travel of another user", absencePage);
    createAbsence(demoFullName, TODAY, TODAY, "For party of another user", absencePage);
    assertEquals(1, absencePage.countAbsences());
  }

  @Test
  public void displayMessageWhenInputOverlappingAbsence() {
    LocalDate chosenDay = LocalDate.now();
    LocalDate theNextDayOfChosenDay = chosenDay.plusDays(1);
    NewDashboardPage newDashboardPage = changeDateFormat();
    AbsencePage absencePage = openAbsencePage(newDashboardPage);
    createAbsenceForCurrentUser(chosenDay, theNextDayOfChosenDay, "Just day off", absencePage);
    assertEquals(1, absencePage.countAbsences());

    NewAbsencePage newAbsencePage = absencePage.openNewAbsenceDialog();
    newAbsencePage.input(chosenDay, theNextDayOfChosenDay, "Overlapping absence");
    newAbsencePage.proceed();

    assertEquals(newAbsencePage.isErrorMessageDisplayed(), true);
    assertEquals("The absence is overlapping with another absence.", newAbsencePage.getErrorMessage());
  }

  @Test
  public void testDeputyAsNormalUser() {
    AbsencePage absencePage = openAbsencePage();
    List<String> personalTaskDuringAbsenceDeputyNames = Arrays.asList(TestAccount.CASE_OWNER_USER.getFullName(), TestAccount.GUEST_USER.getFullName());
    absencePage.setDeputy(personalTaskDuringAbsenceDeputyNames, DeputyRoleType.PERSONAL_TASK_DURING_ABSENCE);
    List<String> personalTaskPermanentDeputyNames = Arrays.asList(TestAccount.ADMIN_USER.getFullName(), TestAccount.HR_ROLE_USER.getFullName());
    absencePage.setDeputy(personalTaskPermanentDeputyNames, DeputyRoleType.PERSONAL_TASK_PERMANENT);
    absencePage.saveSubstitute();
    absencePage.waitForAbsencesGrowlMessageDisplay();
    assertEquals(joinDeputyNames(personalTaskDuringAbsenceDeputyNames), absencePage.getMyDeputy(absencePage.indexOfDeputyRole(DeputyRoleType.PERSONAL_TASK_DURING_ABSENCE)));
    assertEquals(joinDeputyNames(personalTaskPermanentDeputyNames), absencePage.getMyDeputy(absencePage.indexOfDeputyRole(DeputyRoleType.PERSONAL_TASK_PERMANENT)));
  }

  @Test
  public void testAddDeputyInPermanentToDuringAbsence() {
    login(TestAccount.ADMIN_USER);
    AbsencePage absencePage = openAbsencePage();
    absencePage.setSubstitutedByAdmin(TestAccount.DEMO_USER.getFullName());
    List<String> deputyNames = Arrays.asList(TestAccount.CASE_OWNER_USER.getFullName());
    absencePage.setDeputy(deputyNames, DeputyRoleType.PERSONAL_TASK_PERMANENT);
    absencePage.setDeputy(deputyNames, DeputyRoleType.PERSONAL_TASK_DURING_ABSENCE, false);
    assertEquals(absencePage.getChooseDeputyDialogError().startsWith("Substitute is already selected in"), true);
  }

  @Test
  public void testAddDeputyInDuringAbsenceToPermanent() {
    login(TestAccount.ADMIN_USER);
    AbsencePage absencePage = openAbsencePage();
    absencePage.setSubstitutedByAdmin(TestAccount.DEMO_USER.getFullName());
    List<String> deputyNames = Arrays.asList(TestAccount.CASE_OWNER_USER.getFullName());
    absencePage.setDeputy(deputyNames, DeputyRoleType.PERSONAL_TASK_DURING_ABSENCE);
    absencePage.setDeputy(deputyNames, DeputyRoleType.PERSONAL_TASK_PERMANENT, false);
    assertEquals(absencePage.getChooseDeputyDialogError().startsWith("Substitute is already selected in"), true);
  }

  @Test
  public void testIAmDeputyFor() {
    login(TestAccount.ADMIN_USER);
    NewDashboardPage newDashboardPage = changeDateFormat();
    AbsencePage absencePage = openAbsencePage(newDashboardPage);
    createAbsenceForCurrentUser(TOMORROW, TOMORROW, "For Family", absencePage);

    absencePage.setDeputy(Arrays.asList(TestAccount.DEMO_USER.getFullName()), 0);
    absencePage.saveSubstitute();
    login(TestAccount.DEMO_USER);
    absencePage = openAbsencePage(newDashboardPage);
    assertEquals(absencePage.getIAMDeputyFor().contains(TestAccount.ADMIN_USER.getFullName()), true);
  }

  private String joinDeputyNames(List<String> deputyNames) {
    return String.join(", ", deputyNames);
  }

  private NewDashboardPage changeDateFormat() {
    NewDashboardPage newDashboardPage = new NewDashboardPage();
    return newDashboardPage;
  }

  private void createAbsenceForCurrentUser(LocalDate from, LocalDate till, String comment, AbsencePage absencePage) {
    createAbsence("", from, till, comment, absencePage);
  }

  private void createAbsence(String fullname, LocalDate from, LocalDate till, String comment, AbsencePage absencePage) {
    NewAbsencePage newAbsencePage = absencePage.openNewAbsenceDialog();
    newAbsencePage.input(fullname, from, till, comment);
    newAbsencePage.proceed();
  }

  private AbsencePage openAbsencePage(TemplatePage templatePage) {
    return templatePage.openAbsencePage();
  }

  private AbsencePage openAbsencePage() {
    return new NewDashboardPage().openAbsencePage();
  }

}
