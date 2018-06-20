package ch.ivy.addon.portal.generic.navigation;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.apache.commons.lang3.StringUtils;
import org.primefaces.extensions.util.json.GsonConverter;

import ch.ivy.addon.portalkit.enums.GlobalVariable;
import ch.ivy.addon.portalkit.service.GlobalSettingService;
import ch.ivy.addon.portalkit.service.exception.PortalException;
import ch.ivy.addon.portalkit.support.UrlDetector;
import ch.ivy.addon.portalkit.util.SecurityServiceUtils;
import ch.ivyteam.ivy.environment.Ivy;
import ch.ivyteam.ivy.request.RequestUriFactory;
import ch.ivyteam.ivy.server.ServerFactory;

public final class PortalNavigator
{

  private static final String PORTAL_PROCESS_START_NAME = "Start Processes/PortalStart/PortalStart.ivp";
  private static final String PORTAL_END_PAGE = "Start Processes/PortalStart/DefaultEndPage.ivp";
  private static final String PORTAL_PROCESS = "Start Processes/PortalProcess/start.ivp";
  private static final String PORTAL_TASK = "Start Processes/PortalTask/start.ivp";
  private static final String PORTAL_CASE = "Start Processes/PortalCase/start.ivp";
  private static final String PORTAL_STATISTIC = "Start Processes/PortalStatistic/start.ivp";

  public String getPortalStartUrl() throws MalformedURLException
  {
    String homePageURL = getHomePageFromSetting();
    if (!StringUtils.isEmpty(homePageURL))
    {
      return homePageURL;
    }
    return defaultPortalStartUrl(false);
  }

  private String getHomePageFromSetting()
  {
    GlobalSettingService globalSettingSerive = new GlobalSettingService();
    return globalSettingSerive.findGlobalSettingValue(GlobalVariable.HOMEPAGE_URL.toString());
  }

  private String defaultPortalStartUrl(boolean isAbsoluteLink) throws MalformedURLException
  {
    String requestPath = SecurityServiceUtils.findProcessByUserFriendlyRequestPath(PORTAL_PROCESS_START_NAME);
    if (isAbsoluteLink)
    {
      UrlDetector urlDetector = new UrlDetector();
      String serverUrl = urlDetector.getBaseURL(FacesContext.getCurrentInstance());
      return serverUrl + requestPath;
    }
    return "/"
            + RequestUriFactory
                    .getIvyContextName(ServerFactory.getServer().getApplicationConfigurationManager())
            + requestPath;
  }

  public String getPortalStartUrlOf(PortalPage portalPage, Map<String, String> pageParameters)
          throws MalformedURLException
  {
    String baseUrl = getPortalStartUrl();
    return generatePortalStartUrl(baseUrl, portalPage, pageParameters);
  }

  private String generatePortalStartUrl(String baseUrl, PortalPage portalPage,
          Map<String, String> pageParameters)
  {
    Map<String, List<String>> parameters = new HashMap<>();
    parameters.put("portalNavigator", Arrays.asList(portalPage.name()));
    parameters.put("parameters", Arrays.asList(GsonConverter.getGson().toJson(pageParameters)));
    return FacesContext.getCurrentInstance().getExternalContext().encodeRedirectURL(baseUrl, parameters);
  }

  public void redirect(String url)
  {
    try
    {
      FacesContext.getCurrentInstance().getExternalContext().redirect(url);
    }
    catch (Exception e)
    {
      throw new PortalException(e);
    }
  }

  public String getPortalStartUrlOfCurrentApplication()
  {
    String homePageURL = getHomePageFromSetting();
    if (!StringUtils.isEmpty(homePageURL))
    {
      return homePageURL;
    }
    return Ivy.html().startref(PORTAL_PROCESS_START_NAME);
  }

  public void navigateToPortalEndPage() throws MalformedURLException
  {
    String requestPath = SecurityServiceUtils.findProcessByUserFriendlyRequestPath(PORTAL_END_PAGE);
    if (!requestPath.isEmpty())
    {
      UrlDetector urlDetector = new UrlDetector();
      String serverUrl = urlDetector.getBaseURL(FacesContext.getCurrentInstance());
      redirect(serverUrl + requestPath + "?endedTaskId=" + Ivy.wfTask().getId());
    }
  }

  public void navigateToPortalProcess() throws MalformedURLException
  {
    navigate(PORTAL_PROCESS);
  }

  public void navigateToPortalCase() throws MalformedURLException
  {
    navigate(PORTAL_CASE);
  }

  public void navigateToPortalTask() throws MalformedURLException
  {
    navigate(PORTAL_TASK);
  }

  public void navigateToPortalStatistic() throws MalformedURLException
  {
    navigate(PORTAL_STATISTIC);
  }

  private void navigate(String menu) throws MalformedURLException
  {
    String requestPath = SecurityServiceUtils.findProcessByUserFriendlyRequestPath(menu);
    if (!requestPath.isEmpty())
    {
      UrlDetector urlDetector = new UrlDetector();
      String serverUrl = urlDetector.getBaseURL(FacesContext.getCurrentInstance());
      redirect(serverUrl + requestPath);
    }
  }
}
