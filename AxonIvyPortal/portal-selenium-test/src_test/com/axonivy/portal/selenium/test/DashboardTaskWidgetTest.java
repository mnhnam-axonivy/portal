package com.axonivy.portal.selenium.test;

import static com.codeborne.selenide.CollectionCondition.sizeGreaterThanOrEqual;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;


import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.axonivy.ivy.webtest.IvyWebTest;
import com.axonivy.portal.selenium.common.BaseTest;
import com.axonivy.portal.selenium.common.Sleeper;
import com.axonivy.portal.selenium.common.TestAccount;
import com.axonivy.portal.selenium.page.CaseDetailsWidgetNewDashBoardPage;
import com.axonivy.portal.selenium.page.CaseEditWidgetNewDashBoardPage;
import com.axonivy.portal.selenium.page.CaseWidgetNewDashBoardPage;
import com.axonivy.portal.selenium.page.NewDashboardConfigurationPage;
import com.axonivy.portal.selenium.page.NewDashboardDetailsEditPage;
import com.axonivy.portal.selenium.page.NewDashboardPage;
import com.axonivy.portal.selenium.page.ProcessWidgetNewDashBoardPage;
import com.axonivy.portal.selenium.page.TaskEditWidgetNewDashBoardPage;
import com.axonivy.portal.selenium.page.TaskWidgetNewDashBoardPage;
import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.SelenideElement;

@IvyWebTest(headless = false)
public class DashboardTaskWidgetTest extends BaseTest {

  //WIDGET
  private static final String YOUR_CASES_WIDGET = "Your Cases";
  private static final String YOUR_TASKS_WIDGET = "Your Tasks";
  private static final String YOUR_PROCESS_WIDGET = "Your Processes";

  // CASES
  private static final String LEAVE_REQUEST_CASE_NAME = "Leave Request";
  private static final String ORDER_PIZZA = "Order Pizza";
  private static final String HIDE_CASE = "Repair Computer";
  private static final String LEAVE_REQUEST_DEFAULT_CASE= "Leave Request for Default Additional Case Details";
  private static final String INVESTMENT_REQUEST_CUSTOMIZATION_CASE = "Investment Request";
  private static final String CREATE_12_CASES_WITH_CATEGORY_CASE = "Create 12 Cases with category";

  // TASKS
  private static final String REPORT_HIDE_CASE = "Report and hide case";
  private static final String SICK_LEAVE_REQUEST = "Sick Leave Request";
  private static final String DESTROYED = "Destroyed";
  private static final String TASK_NUMBER = "Task number";
  private static final String DONE = "Done";
  private static final String NEW_YOUR_TASK = "New Your Tasks";
  private static final String SUSPENDED = "Suspended";
  private static final String EXPIRE_TODAY = "Expire today";
  
  private NewDashboardPage newDashboardPage;
  
  @Override
  @BeforeEach
  public void setup() {
    super.setup();
    newDashboardPage = new NewDashboardPage();
  }
  
  @Test()
  public void testHideTasks() {
    redirectToRelativeLink(hideCaseUrl);
    login(TestAccount.ADMIN_USER);
    redirectToNewDashBoard();
    TaskWidgetNewDashBoardPage taskWidget = newDashboardPage.selectTaskWidget(YOUR_TASKS_WIDGET);
    taskWidget.expand().shouldHave(sizeGreaterThanOrEqual(1));
    taskWidget.openFilterWidget();
    taskWidget.filterTaskName(REPORT_HIDE_CASE);
    taskWidget.applyFilter();
    taskWidget.startFirstTask();
    taskWidget.expand().shouldHaveSize(0);
    taskWidget.countTasks(REPORT_HIDE_CASE).shouldHaveSize(0);
    //Sleeper.sleep(3000000);
  }
  
  @Test
  public void testDestroyTaskWithPermission() {
    redirectToRelativeLink(createTestingTasksUrl);
    login(TestAccount.ADMIN_USER);
    redirectToNewDashBoard();
    TaskWidgetNewDashBoardPage taskWidget = newDashboardPage.selectTaskWidget(YOUR_TASKS_WIDGET);
    taskWidget.expand().shouldHave(sizeGreaterThanOrEqual(1));
    taskWidget.openFilterWidget();
    taskWidget.filterTaskName(SICK_LEAVE_REQUEST);
    taskWidget.applyFilter();
    taskWidget.clickOnTaskActionLink(0);
    taskWidget.destroy();
    taskWidget.stateOfFirstTask().shouldHave(text(DESTROYED));
  }
  
  @Test
  public void testDestroyTaskWithoutPermission() {
    redirectToRelativeLink(createTestingTasksUrl);
    login(TestAccount.DEMO_USER);
    redirectToNewDashBoard();
    TaskWidgetNewDashBoardPage taskWidget = newDashboardPage.selectTaskWidget(YOUR_TASKS_WIDGET);
    taskWidget.expand().shouldHave(sizeGreaterThanOrEqual(1));
    taskWidget.openFilterWidget();
    taskWidget.filterTaskName(SICK_LEAVE_REQUEST);
    taskWidget.applyFilter();
    taskWidget.clickOnTaskActionLink(0);
    taskWidget.destroyTaskLink().shouldNotHave(visible);
  }
  
  @Test
  public void testStickyFilterTaskList() {
    redirectToRelativeLink(create12CasesWithCategoryUrl);
    login(TestAccount.DEMO_USER);
    redirectToNewDashBoard();
    TaskWidgetNewDashBoardPage taskWidget = newDashboardPage.selectTaskWidget(YOUR_TASKS_WIDGET);
    taskWidget.expand().shouldHave(sizeGreaterThanOrEqual(1));
    //Filter Task Name
    taskWidget.openFilterWidget();
    taskWidget.filterTaskName(TASK_NUMBER);
    taskWidget.applyFilter();
    taskWidget.countAllTasks().shouldHave(sizeGreaterThanOrEqual(5));
    //Filter State
    taskWidget.openFilterWidget();
    taskWidget.filterTaskName(CREATE_12_CASES_WITH_CATEGORY_CASE);
    taskWidget.filterTaskState();
    taskWidget.selectState(DONE);
    taskWidget.applyFilter();
    taskWidget.countAllTasks().shouldHaveSize(1);
    taskWidget.stateOfFirstTask().shouldHave(text(DONE));
  }
  
  @Test
  public void testEditFilterTaskList() {
    redirectToRelativeLink(create12CasesWithCategoryUrl);
    login(TestAccount.ADMIN_USER);
    redirectToNewDashBoard();
    TaskWidgetNewDashBoardPage taskWidget = newDashboardPage.selectTaskWidget(YOUR_TASKS_WIDGET);
    taskWidget.expand().shouldHave(sizeGreaterThanOrEqual(1));
    
    newDashboardPage.openDashboardConfigurationDialog();
    NewDashboardConfigurationPage configPage = newDashboardPage.navigateToEditPublicDashboardPage();
    configPage.navigateToEditDashboardDetailsByName("Dashboard");
    TaskEditWidgetNewDashBoardPage taskEditWidget = taskWidget.openEditTaskWidget();
    taskEditWidget.changeWidgetTitle(NEW_YOUR_TASK);
    taskEditWidget.filterTaskName(TASK_NUMBER);
    taskEditWidget.clickOnStateToShowDropdown();
    taskEditWidget.selectState(SUSPENDED);
    taskEditWidget.preview();
    taskEditWidget.countAllTasks().shouldHaveSize(5);
    taskEditWidget.nextPageTable();
    taskEditWidget.countAllTasks().shouldHaveSize(5);
    taskEditWidget.nextPageTable();
    taskEditWidget.countAllTasks().shouldHaveSize(2);
    taskEditWidget.save();
    TaskWidgetNewDashBoardPage taskWidgetEdited = newDashboardPage.selectTaskWidget(NEW_YOUR_TASK);
    taskWidgetEdited.expand().shouldHave(sizeGreaterThanOrEqual(1));
    taskWidgetEdited.countAllTasks().shouldHaveSize(5);
  }
  
  @Test
  public void testAddNewTaskList() {
    redirectToRelativeLink(createTestingTasksUrl);
    login(TestAccount.ADMIN_USER);
    redirectToNewDashBoard();

    newDashboardPage.openDashboardConfigurationDialog();
    NewDashboardConfigurationPage configPage = newDashboardPage.navigateToEditPublicDashboardPage();
    NewDashboardDetailsEditPage newDashboardDetailsEditPage = configPage.navigateToEditDashboardDetailsByName("Dashboard");

    newDashboardDetailsEditPage.addWidget();
    TaskEditWidgetNewDashBoardPage newtaskWidget = newDashboardDetailsEditPage.addNewTaskWidget();
    newtaskWidget.changeWidgetTitle(NEW_YOUR_TASK);
    newtaskWidget.save();
    TaskWidgetNewDashBoardPage taskWidget = newDashboardPage.selectTaskWidget(NEW_YOUR_TASK);
    taskWidget.expand().shouldHaveSize(1);
  }
  
  @Test
  public void testTaskWidgetInformation() {
    redirectToRelativeLink(createTestingTasksUrl);
    login(TestAccount.ADMIN_USER);
    redirectToNewDashBoard();

    TaskWidgetNewDashBoardPage taskWidget = newDashboardPage.selectTaskWidget(YOUR_TASKS_WIDGET);
    taskWidget.expand().shouldHave(sizeGreaterThanOrEqual(1));
    taskWidget.clickOnButtonWidgetInformation();
    taskWidget.getExpiryTodayLabelInWidgetInfo().shouldHave(text(EXPIRE_TODAY));
    taskWidget.clickToExpandNumberOfTaskByState();
    taskWidget.getFirstStateLabelInWidgetInfo().shouldHave(text(SUSPENDED));
    taskWidget.clickToExpandNumberOfTaskByCategory();    
    taskWidget.clickToExpandPredefinedFilters();
    taskWidget.closeWidgetInformationDialog();
  }
  
  @Test
  public void testExpandAndCollapseTaskWidget() {
    redirectToRelativeLink(createTestingTasksUrl);
    login(TestAccount.ADMIN_USER);
    redirectToNewDashBoard();

    TaskWidgetNewDashBoardPage taskWidget = newDashboardPage.selectTaskWidget(YOUR_TASKS_WIDGET);
    taskWidget.expand().shouldHave(sizeGreaterThanOrEqual(1));
    
    taskWidget.clickOnButtonExpandTaskWidget();
    taskWidget.getExpandedTaskWidget().shouldHaveSize(1);
    taskWidget.clickOnButtonCollapseTaskWidget();
    taskWidget.getExpandedWidget().shouldHaveSize(0);
  }
  
  @Test
  public void testStickySortTaskList() {
    redirectToRelativeLink(createTestingTasksUrl);
    login(TestAccount.ADMIN_USER);
    
    
    
    // Sort task on Dashboard
    /*taskWidgetPage.openCompactSortMenu();
    taskWidgetPage.selectCompactSortByName("Expiry (Newest first)", 0, "Maternity Leave Request");
    // Navigate around Portal
    CaseWidgetPage caseWidgetPage = taskWidgetPage.openCaseList();
    // Check result at full Task List
    taskWidgetPage = caseWidgetPage.openTaskList();
    String selectedSortColumn = taskWidgetPage.getSelectedSortColumn();
    assertTrue(StringUtils.equalsIgnoreCase("Expiry", selectedSortColumn));
    String taskName = taskWidgetPage.getTaskListCustomCellValue(0, "task-name");
    assertTrue(StringUtils.equalsIgnoreCase("Maternity Leave Request", taskName));
    // Change to another column - which is not include at compact task list
    taskWidgetPage.sortTaskListByColumn("Name / Description", 0, "task-name", "Annual Leave Request");
    // Back to Dashboard - compact task list will sort by default column
    taskWidgetPage.clickOnLogo();
    // Create new task
    createTestingTasks();
    homePage = new HomePage();
    taskWidgetPage = homePage.getTaskWidget();
    selectedSortColumn = taskWidgetPage.getSelectedCompactSortLable();
    assertTrue(StringUtils.equalsIgnoreCase("Creation date (Newest first)", selectedSortColumn));
    // Change User sort selection
    UserProfilePage userProfilePage = taskWidgetPage.openMyProfilePage();
    userProfilePage.selectTaskSortField("Priority");
    userProfilePage.selectTaskSortDirection("Sort ascending");
    homePage = userProfilePage.save();
    // Check result
    taskWidgetPage = homePage.openTaskList();
    selectedSortColumn = taskWidgetPage.getSelectedSortColumn();
    assertTrue(StringUtils.equalsIgnoreCase("Prio", selectedSortColumn));
    assertEquals("high", taskWidgetPage.getPriorityOfTask(0));*/
  }
  
}
