package ch.ivy.addon.portal.generic.navigation;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.faces.context.FacesContext;

import org.apache.commons.lang3.StringUtils;

import ch.ivy.addon.portalkit.enums.GlobalVariable;
import ch.ivy.addon.portalkit.enums.MenuKind;
import ch.ivy.addon.portalkit.enums.SessionAttribute;
import ch.ivy.addon.portalkit.service.GlobalSettingService;
import ch.ivy.addon.portalkit.service.exception.PortalException;
import ch.ivy.addon.portalkit.support.UrlDetector;
import ch.ivy.addon.portalkit.util.IvyExecutor;
import ch.ivy.addon.portalkit.util.SecurityServiceUtils;
import ch.ivyteam.ivy.environment.Ivy;
import ch.ivyteam.ivy.request.RequestUriFactory;
import ch.ivyteam.ivy.server.ServerFactory;
import ch.ivyteam.ivy.workflow.StandardProcessType;

public final class PortalNavigator {
  private static final String PORTAL_PROCESS_START_NAME = "Start Processes/PortalStart/DefaultApplicationHomePage.ivp";
  private static final String PORTAL_PROCESS = "Start Processes/PortalPages/DefaultProcessStartListPage.ivp";
  private static final String PORTAL_TASK = "Start Processes/PortalStart/DefaultTaskListPage.ivp";
  private static final String PORTAL_CASE = "Start Processes/PortalStart/CaseListPage.ivp";
  private static final String PORTAL_STATISTIC = "Start Processes/PortalStart/StatisticPage.ivp";
  private static final String PORTAL_CASE_DETAILS = "Start Processes/PortalStart/CaseDetailsPage.ivp";
  private static final String PORTAL_RELATED_TASKS_OF_CASE = "Start Processes/PortalStart/RelatedTasksOfCasePage.ivp";
  private static final String PORTAL_TASK_DETAILS = "Start Processes/PortalStart/TaskDetailsPage.ivp";
  private static final String PORTAL_GLOBAL_SEARCH = "Start Processes/PortalStart/GlobalSearchPage.ivp";
  private static final String SLASH = "/";

  public String getPortalStartUrl() throws MalformedURLException {
    String homePageURL = getHomePageFromSetting();
    if (StringUtils.isNotEmpty(homePageURL)) {
      return homePageURL;
    }
    return defaultPortalStartUrl(false);
  }

  private String getHomePageFromSetting() {
    GlobalSettingService globalSettingService = new GlobalSettingService();
    return globalSettingService.findGlobalSettingValue(GlobalVariable.HOMEPAGE_URL.toString());
  }

  private String defaultPortalStartUrl(boolean isAbsoluteLink) throws MalformedURLException {
    String requestPath = SecurityServiceUtils.findProcessByUserFriendlyRequestPath(PORTAL_PROCESS_START_NAME);
    if (isAbsoluteLink) {
      UrlDetector urlDetector = new UrlDetector();
      String serverUrl = urlDetector.getBaseURL(FacesContext.getCurrentInstance());
      return serverUrl + requestPath;
    }
    return SLASH + RequestUriFactory.getIvyContextName() + requestPath;
  }

  public void redirect(String url) {
    try {
      FacesContext.getCurrentInstance().getExternalContext().redirect(url);
    } catch (IOException ex) {
      throw new PortalException(ex);
    }
  }

  public String getPortalStartUrlOfCurrentApplication() {
    String homePageURL = getHomePageFromSetting();
    if (StringUtils.isNotEmpty(homePageURL)) {
      return homePageURL;
    }
    return Ivy.html().startRef(PORTAL_PROCESS_START_NAME);
  }
  
  public String getSubMenuItemUrlOfCurrentApplication(MenuKind menuKind) {
    String subMenuUrl = StringUtils.EMPTY;
    switch (menuKind) {
      case PROCESS:
        subMenuUrl = PORTAL_PROCESS;
        break;
      case TASK:
        subMenuUrl = PORTAL_TASK;
        break;
      case CASE:
        subMenuUrl = PORTAL_CASE;
        break;
      case DASHBOARD:
        subMenuUrl = PORTAL_STATISTIC;
        break;
      default:
        break;
    }
    String customizePortalFriendlyRequestPath = SecurityServiceUtils.findProcessByUserFriendlyRequestPath(subMenuUrl);
    if (StringUtils.isNotEmpty(customizePortalFriendlyRequestPath)) {
      UrlDetector urlDetector = new UrlDetector();
      String serverUrl = StringUtils.EMPTY;
      try {
        serverUrl = urlDetector.getBaseURL(FacesContext.getCurrentInstance());
      } catch (MalformedURLException e) {
        Ivy.log().info("Cannot detect server Url" + e.getMessage());
      }
      return serverUrl + customizePortalFriendlyRequestPath;
    }
    return Ivy.html().startRef(subMenuUrl);
  }

  public void navigateToPortalEndPage(Long taskId) {
    String customizePortalEndPage = getDefaultEndPage(); 
    redirect(customizePortalEndPage + "?endedTaskId=" + taskId);
  }
  
  /**
   * Navigates to PortalEndPage without finishing a task, e.g. clicking on Cancel button then back to previous page: task list or task details or global search
   * NOTES: is only used for the task not started in Portal IFrame
   */
  public void navigateToPortalEndPage() {
    String defaultEndPage = getDefaultEndPage(); 
    redirect(defaultEndPage + "?endedTaskId=" + Ivy.wfTask().getId());
    Ivy.session().setAttribute(SessionAttribute.IS_TASK_FINISHED.toString(), false);
  }
  
  private String getDefaultEndPage() {
    return IvyExecutor.executeAsSystem(() -> Ivy.html().startRef(Ivy.wf().getStandardProcessImplementation(StandardProcessType.DefaultEndPage).getUserFriendlyRequestPath()));
  }

  public void navigateToPortalProcess() {
    navigateByKeyword("DefaultProcessStartListPage.ivp", PORTAL_PROCESS, new HashMap<>());
  }

  public void navigateToPortalCase() {
    navigateByKeyword("CaseListPage.ivp", PORTAL_CASE, new HashMap<>());
  }

  public void navigateToPortalTask() {
    navigateByKeyword("DefaultTaskListPage.ivp", PORTAL_TASK, new HashMap<>());
  }

  public void navigateToPortalStatistic() {
    navigateByKeyword("StatisticPage.ivp", PORTAL_STATISTIC, new HashMap<>());
  }
  
  public void navigateToPortalHome() {
    navigateByKeyword("DefaultApplicationHomePage.ivp", PORTAL_PROCESS_START_NAME, new HashMap<>());
  }
  
  public void navigateToPortalCaseDetails(Long caseId) {
    Map<String, String> params = new HashMap<>();
    params.put("caseId", String.valueOf(caseId));
    navigateByKeyword("CaseDetailsPage.ivp", PORTAL_CASE_DETAILS, params);
  }
  
  public void navigateToPortalRelatedTasksOfCase(Long caseId, boolean isBusinessCase, String caseName) {
    Map<String, String> params = new HashMap<>();
    params.put("caseId", String.valueOf(caseId));
    params.put("isBusinessCase", String.valueOf(isBusinessCase));
    params.put("caseName", caseName);
    navigateByKeyword("RelatedTasksOfCasePage.ivp", PORTAL_RELATED_TASKS_OF_CASE, params);
  }
  
  public void navigateToPortalTaskDetails(Long taskId) {
    Map<String, String> params = new HashMap<>();
    params.put("selectedTaskId", String.valueOf(taskId));
    navigateByKeyword("TaskDetailsPage.ivp", PORTAL_TASK_DETAILS, params);
  }
  
  public void navigateToPortalGlobalSearch(String keyword) {
    Map<String, String> params = new HashMap<>();
    params.put("keyword", keyword);
    navigateByKeyword("GlobalSearchPage.ivp", PORTAL_GLOBAL_SEARCH, params);
  }

  private void navigateByKeyword(String keyword, String defaultFriendlyRequestPath, Map<String, String> param) {
    String customizePortalFriendlyRequestPath = SecurityServiceUtils.findFriendlyRequestPathContainsKeyword(keyword);
    if (StringUtils.isNotEmpty(customizePortalFriendlyRequestPath)) {
      navigate(customizePortalFriendlyRequestPath, param);
    } else {
      navigate(defaultFriendlyRequestPath, param);
    }
  }

  private void navigate(String friendlyRequestPath, Map<String, String> params) {
    String requestPath = SecurityServiceUtils.findProcessByUserFriendlyRequestPath(friendlyRequestPath);
    if (StringUtils.isNotEmpty(requestPath)) {
      try {
        String ivyContextName = ServerFactory.getServer().getSecurityManager().executeAsSystem(
            () -> RequestUriFactory.getIvyContextName());
        String paramStr = params.entrySet().stream()
            .map(e -> e.getKey()+"="+e.getValue())
            .collect(Collectors.joining("&"));
        redirect(SLASH + ivyContextName + requestPath + (StringUtils.isNotBlank(paramStr) ? "?" + paramStr : StringUtils.EMPTY));
      } catch (Exception e) {
        Ivy.log().error(e);
      }
    }
  }
}