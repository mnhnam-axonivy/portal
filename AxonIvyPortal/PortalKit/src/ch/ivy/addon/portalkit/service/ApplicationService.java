package ch.ivy.addon.portalkit.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import ch.ivy.addon.portalkit.constant.IvyCacheIdentifier;
import ch.ivy.addon.portalkit.constant.PortalConstants;
import ch.ivy.addon.portalkit.ivydata.bo.IvyApplication;
import ch.ivy.addon.portalkit.ivydata.service.IApplicationService;
import ch.ivy.addon.portalkit.ivydata.utils.ServiceUtilities;
import ch.ivy.addon.portalkit.persistence.dao.ApplicationDao;
import ch.ivy.addon.portalkit.persistence.domain.Application;
import ch.ivyteam.ivy.application.IApplication;
import ch.ivyteam.ivy.environment.Ivy;

public class ApplicationService extends AbstractService<Application> {

  public ApplicationService() {
    super(ApplicationDao.class);
  }

  @Override
  protected ApplicationDao getDao() {
    return (ApplicationDao) super.getDao();
  }

  public List<Application> findAllThirdPartyApplications() {
    return getDao().findAllThirdPartyApplications();
  }

  public List<Application> findAllIvyApplications() {
    return getDao().findAllIvyApplications();
  }

  public Application findByDisplayNameAndName(String displayName, String name) {
    return getDao().findByDisplayNameAndName(displayName, name);
  }

  public Application findByName(String name) {
    return getDao().findByName(name);
  }

  public List<Application> findByNames(List<String> names) {
    return getDao().findByNames(names);
  }

  public String convertApplicationIdsToString(List<Long> applicationIds) {
    if (CollectionUtils.isEmpty(applicationIds)) {
      return StringUtils.EMPTY;
    }
    return applicationIds.stream().map(String::valueOf).collect(Collectors.joining(","));
  }

  public long countIvyApplications(List<Application> applications) {
    return applications.stream().filter(application -> application.getServerId() != null).count();
  }
  
  /**
   * Finds names of the active applications based on the configuration; if the current app is not Portal, returns the current app.
   * Otherwise, finds the active applications registered by the admin user; if empty, means there is no
   * configuration in admin settings, finds all of active applications of the engine.
   * 
   * @param username
   * @return {@link java.util.List} of application names
   */
  @SuppressWarnings("unchecked")
  public List<String> findActiveIvyAppsBasedOnConfiguration(String username) {
    if (StringUtils.isBlank(username)) {
      return new ArrayList<>();
    }
    
    Optional<Object> cacheValueOpt = IvyCacheService.newInstance().getSessionCacheValue(IvyCacheIdentifier.ONLINE_APPLICATIONS_BASED_ON_CONFIGURATION, username);
    if (cacheValueOpt.isPresent()) {
      return (List<String>) cacheValueOpt.get();
    }
    
    List<String> workOnApps;
    String currentApplicationName =
        Optional.ofNullable(Ivy.request().getApplication()).map(IApplication::getName).orElse(StringUtils.EMPTY);
    if (PortalConstants.PORTAL_APPLICATION_NAME.equals(currentApplicationName)) {
      workOnApps = findActiveIvyAppsUserCanWorkOn(Ivy.session().getSessionUserName());
    } else {
      workOnApps = Arrays.asList(currentApplicationName);
    }
    
    IvyCacheService.newInstance().setSessionCache(IvyCacheIdentifier.ONLINE_APPLICATIONS_BASED_ON_CONFIGURATION, username, workOnApps);
    return workOnApps;
  }
  
  /**
   * Finds names of the active applications registered by the admin user; if empty, means there is no
   * configuration in admin settings, finds all of active applications of the engine.
   * 
   * @param username
   * @return {@link java.util.List} of application names
   */
  @SuppressWarnings("unchecked")
  public List<String> findActiveIvyAppsUserCanWorkOn(String username) {
    if (StringUtils.isBlank(username)) {
      return new ArrayList<>();
    }
    
    Optional<Object> cacheValueOpt = IvyCacheService.newInstance().getSessionCacheValue(IvyCacheIdentifier.ONLINE_APPLICATIONS_USER_CAN_WORK_ON, username);
    if (cacheValueOpt.isPresent()) {
      return (List<String>) cacheValueOpt.get();
    }

    List<String> workOnApps = findInvolvedAppsOfUser(username);
    if (CollectionUtils.isEmpty(workOnApps)) {
      return new ArrayList<>();
    }
    IvyCacheService.newInstance().setSessionCache(IvyCacheIdentifier.ONLINE_APPLICATIONS_USER_CAN_WORK_ON, username, workOnApps);
    return workOnApps;
  }
  
  private List<String> findInvolvedAppsOfUser(String username) {
    IApplicationService applicationService = ch.ivy.addon.portalkit.ivydata.service.impl.ApplicationService.newInstance();
    List<String> registeredApplicationNames = findAllIvyApplications().stream().map(Application::getName).collect(Collectors.toList());
    return applicationService.findActiveAll().stream()
        .filter(app -> ServiceUtilities.findUser(username, app.getName()) != null 
          && (CollectionUtils.isEmpty(registeredApplicationNames) || CollectionUtils.containsAny(registeredApplicationNames, app.getName())))
        .map(IvyApplication::getName)
        .collect(Collectors.toList());
  }

}
