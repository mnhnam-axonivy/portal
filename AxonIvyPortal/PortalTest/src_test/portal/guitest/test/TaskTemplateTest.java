package portal.guitest.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.TimeoutException;

import com.jayway.awaitility.Awaitility;
import com.jayway.awaitility.Duration;

import portal.guitest.common.BaseTest;
import portal.guitest.common.NavigationHelper;
import portal.guitest.common.TestAccount;
import portal.guitest.common.Variable;
import portal.guitest.page.NewDashboardPage2;
import portal.guitest.page.NoteHistoryPage;
import portal.guitest.page.TaskDetailsPage;
import portal.guitest.page.TaskTemplatePage;
import portal.guitest.page.TaskWidgetPage;
import portal.guitest.page.WorkingTaskDialogPage;

public class TaskTemplateTest extends BaseTest {

  private String createImpersistentTaskUrl = "portal-developer-examples/169BDE2F368D6EC4/ApplicationShowcase.ivp";
  private static final String ANNUAL_LEAVE_REQUEST_TASK ="Annual Leave Request";
  @Override
  @Before
  public void setup() {
    super.setup();
  }

  @Test
  public void testCaseDetailsTabDisplayed() {
    createTestData();
    TaskTemplatePage taskTemplatePage = startATaskAndOpenCaseInfo();
    assertTrue("Case details is not displayed", taskTemplatePage.containsCaseDetails());
  }

  @Test
  public void testAddingANote() {
    createTestData();
    TaskTemplatePage taskTemplatePage = startATaskAndOpenCaseInfo();
    assertEquals(0, taskTemplatePage.countNoteItems());
    taskTemplatePage.addNewNote("Sample note message");
    assertEquals(1, taskTemplatePage.countNoteItems());
  }

  @Test
  @Ignore
  public void testOpeningFinishedTaskInHistoryArea() {
    TaskTemplatePage taskTemplatePage = startATaskAndOpenCaseInfo();
    taskTemplatePage.openFinishedTaskInHistoryArea();

    NoteHistoryPage caseHistoryPage = new NoteHistoryPage();

    Awaitility.await().atMost(new Duration(5, TimeUnit.SECONDS)).until(() -> taskTemplatePage.countBrowserTab() > 1);
    taskTemplatePage.switchLastBrowserTab();
    int numberOfNotes = 0;
    try {
        numberOfNotes = caseHistoryPage.countNotes();
    } catch (TimeoutException e) { // sometimes session is destroyed (don't know reason why!!!) so we cannot reach the page
        System.out.println("Stop testShowCaseNoteHistory test here because session is destroyed");
        return ;
    }
    assertEquals(1, numberOfNotes);
  }

  @Test
  public void testOpeningRelatedTask() {
    updateGlobalVariable(Variable.TASK_BEHAVIOUR_WHEN_CLICKING_ON_LINE_IN_TASK_LIST.getKey(), "ACCESS_TASK_DETAILS");
    createTestData();
    TaskTemplatePage taskTemplatePage = startATaskAndOpenCaseInfo();

    getBrowser().getDriver().switchTo().defaultContent();
    TaskDetailsPage taskDetailsPage = taskTemplatePage.openRelatedTaskInList(ANNUAL_LEAVE_REQUEST_TASK);
    assertEquals("Task: Annual Leave Request", taskDetailsPage.getTaskNameInDialog());

    taskDetailsPage.clickBackButton();
    getBrowser().getDriver().switchTo().defaultContent();
    taskTemplatePage = new TaskTemplatePage();
    assertTrue(taskTemplatePage.countRelatedTasks() > 0);
  }

  @Test
  public void testOpeningDocumentUploading() {
    createTestData();
    TaskTemplatePage taskTemplatePage = startATaskAndOpenCaseInfo();
    taskTemplatePage.openDocumentUploadingDialog();
    assertTrue(taskTemplatePage.isDocumentUploadingDialogDisplayed());
  }
  
  @Test
  public void testLeaveWorkingTaskByClickingOnLogo() {
    createTestData();
    TaskTemplatePage taskTemplatePage = startATaskAndOpenCaseInfo();
    taskTemplatePage.clickOnLogo();
    WorkingTaskDialogPage dialogPage = new WorkingTaskDialogPage();
    dialogPage.leaveTask();
    TaskWidgetPage taskWidget = NavigationHelper.navigateToTasList();
    assertTrue(taskWidget.isTaskStateOpen(0));
  }
  
  @Test
  public void testReserveWorkingTaskByClickingOnLogo() {
    redirectToRelativeLink(simplePaymentUrl);
    login(TestAccount.ADMIN_USER);
    redirectToRelativeLink(NewDashboardPage2.PORTAL_HOME_PAGE_URL);
    NewDashboardPage2 home = new NewDashboardPage2();
    home.waitForPageLoaded();
    TaskTemplatePage taskTemplatePage = startATaskAndOpenCaseInfo();
    taskTemplatePage.clickOnLogo();
    WorkingTaskDialogPage dialogPage = new WorkingTaskDialogPage();
    dialogPage.reserveTask();
    redirectToRelativeLink(NewDashboardPage2.PORTAL_HOME_PAGE_URL);
    TaskWidgetPage taskWidget = NavigationHelper.navigateToTasList();
    Assert.assertTrue(taskWidget.isTaskStateReserved(0));
  }
  
  @Test
  public void testResetTaskWhenStartSideStep() {
    redirectToRelativeLink(createTestingCaseMapUrl);
    NewDashboardPage2 newDashboardPage2 = new NewDashboardPage2();
    TaskWidgetPage taskWidgetPage = NavigationHelper.navigateToTasList();
    int latestTask = taskWidgetPage.countTasks() - 1;
    TaskTemplatePage taskTemplatePage = taskWidgetPage.startTask(latestTask);
    taskTemplatePage.clickTaskActionMenu();
    taskTemplatePage.startSideStep();
    TaskWidgetPage taskWidget = NavigationHelper.navigateToTasList();
    assertTrue(taskWidget.isTaskStateOpen(0));
  }

  @Test
  public void testNotShowStartAdhocWhenOpenImpersistedTask() {
    redirectToRelativeLink(createImpersistentTaskUrl);
    TaskTemplatePage taskTemplatePage = new TaskTemplatePage();
    taskTemplatePage.clickTaskActionMenu();
    assertEquals(true, taskTemplatePage.isStartAdhocBtnNotExist());
  }

  private void createTestData() {
    redirectToRelativeLink(createTestingTasksUrl);
  }
  
  private TaskTemplatePage startATaskAndOpenCaseInfo() {
    TaskWidgetPage taskWidgetPage = NavigationHelper.navigateToTasList();
    TaskTemplatePage taskTemplatePage = taskWidgetPage.startTask(0);
    taskTemplatePage.openCaseInfo();
    return taskTemplatePage;
  }
  
  @Test
  public void testShowCategoryColummnByDefault() {
    createTestData();
    NewDashboardPage2 newDashboardPage2 = new NewDashboardPage2();
    TaskWidgetPage taskList = newDashboardPage2.openTaskList();
    assertTrue(taskList.isCategoryColumnDisplayed());
  }
}
