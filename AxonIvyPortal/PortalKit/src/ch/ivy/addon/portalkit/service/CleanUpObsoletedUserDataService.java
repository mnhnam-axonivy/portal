package ch.ivy.addon.portalkit.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import ch.ivy.addon.portalkit.bo.CaseColumnsConfiguration;
import ch.ivy.addon.portalkit.bo.RemoteUser;
import ch.ivy.addon.portalkit.bo.TaskColumnsConfiguration;
import ch.ivy.addon.portalkit.casefilter.CaseFilterData;
import ch.ivy.addon.portalkit.persistence.domain.UserProcess;
import ch.ivy.addon.portalkit.statistics.StatisticChart;
import ch.ivy.addon.portalkit.taskfilter.TaskFilterData;
import ch.ivy.addon.portalkit.util.SecurityServiceUtils;
import ch.ivyteam.ivy.environment.Ivy;
import ch.ivyteam.ivy.process.call.SubProcessCall;
import ch.ivyteam.ivy.security.IUser;
import ch.ivyteam.ivy.security.SecurityManagerFactory;
import ch.ivyteam.ivy.server.ServerFactory;

public class CleanUpObsoletedUserDataService {

  private static final String SECURITY_SERVICE_CALLABLE = "MultiPortal/SecurityService";
  List<RemoteUser> currentUsers;

  public void cleanUpData() {
    try {
      currentUsers = ServerFactory.getServer().getSecurityManager().executeAsSystem(new Callable<List<RemoteUser>>() {
        @SuppressWarnings("unchecked")
        @Override
        public List<RemoteUser> call() throws Exception {
          return SubProcessCall.withPath(SECURITY_SERVICE_CALLABLE).withStartName("findAllUsersByApplication")
              .call(Ivy.request().getApplication().getName()).get("users", List.class);
        }
      });

    } catch (Exception e) {
      Ivy.log().error("Can't get list of users", e);
      return;
    }
    cleanUpUserFavouriteProcess();
    cleanUpUserTaskCaseFilter();
    cleanUpUserTaskColumnsConfigData();
    cleanUpUserCaseColumnsConfigData();
    cleanUpUserStatisticChartData();
  }

  private void cleanUpUserFavouriteProcess() {
    List<String> userNames = currentUsers.stream().map(RemoteUser::getUsername).distinct().collect(Collectors.toList());
    UserProcessService userProcessService = new UserProcessService();
    List<UserProcess> userProcesses = userProcessService.findAll();
    List<UserProcess> obsoletedUserProcess = new ArrayList<>();
    if (userProcesses != null) {
      for (UserProcess userProcess : userProcesses) {
        String processUserName = userProcess.getUserName();
        if (checkIfUserBelongToCurrentApp(processUserName) && !userNames.contains(processUserName)) {
          obsoletedUserProcess.add(userProcess);
        }
      }
    }
    userProcessService.deleteAll(obsoletedUserProcess);
  }

  private boolean checkIfUserBelongToCurrentApp(String userName) {
    try {
      return SecurityManagerFactory.getSecurityManager().executeAsSystem(() -> {
        IUser user = Ivy.request().getApplication().getSecurityContext().findUser(userName);
        return user != null;
      });
    } catch (Exception e) {
      Ivy.log().error("Check user belongs to current app failed ", e);
      return false;
    }
  }

  private void cleanUpUserTaskCaseFilter() {
    long applicationId = Ivy.request().getApplication().getId();
    List<Long> userIds = currentUsers.stream().map(RemoteUser::getId).collect(Collectors.toList());
    AbstractFilterService<TaskFilterData> taskFilterService = new TaskFilterService();
    List<TaskFilterData> allPrivateTaskFilters = taskFilterService.getAllPrivateFilters();
    if (allPrivateTaskFilters != null) {
      for (TaskFilterData privateTaskFilter : allPrivateTaskFilters) {
        if (Ivy.repo().getInfo(privateTaskFilter).getCreatedByAppId() == applicationId
            && !userIds.contains(privateTaskFilter.getUserId())) {
          taskFilterService.delete(privateTaskFilter.getId());
        }
      }
    }
    AbstractFilterService<CaseFilterData> caseFilterService = new CaseFilterService();
    List<CaseFilterData> allPrivateCaseFilters = caseFilterService.getAllPrivateFilters();
    if (allPrivateCaseFilters != null) {
      for (CaseFilterData privateCaseFilter : allPrivateCaseFilters) {
        if (Ivy.repo().getInfo(privateCaseFilter).getCreatedByAppId() == applicationId
            && !userIds.contains(privateCaseFilter.getUserId())) {
          caseFilterService.delete(privateCaseFilter.getId());
        }
      }
    }
  }

  private void cleanUpUserTaskColumnsConfigData() {
    Long serverId = SecurityServiceUtils.getServerIdFromSession();
    List<Long> userIds = currentUsers.stream().map(RemoteUser::getId).collect(Collectors.toList());
    Long applicationId = Ivy.request().getApplication().getId();
    TaskColumnsConfigurationService service = new TaskColumnsConfigurationService();
    List<TaskColumnsConfiguration> allColumnConfigs = service.getAllConfiguration(serverId, applicationId);
    if (allColumnConfigs != null) {
      for (TaskColumnsConfiguration columnConfig : allColumnConfigs) {
        if (!userIds.contains(columnConfig.getUserId())) {
          Ivy.repo().delete(columnConfig);
        }
      }
    }
  }
  
  private void cleanUpUserCaseColumnsConfigData() {
    Long serverId = SecurityServiceUtils.getServerIdFromSession();
    List<Long> userIds = currentUsers.stream().map(RemoteUser::getId).collect(Collectors.toList());
    Long applicationId = Ivy.request().getApplication().getId();
    CaseColumnsConfigurationService service = new CaseColumnsConfigurationService();
    List<CaseColumnsConfiguration> allColumnConfigs = service.getAllConfiguration(serverId, applicationId);
    if (allColumnConfigs != null) {
      for (CaseColumnsConfiguration columnConfig : allColumnConfigs) {
        if (!userIds.contains(columnConfig.getUserId())) {
          Ivy.repo().delete(columnConfig);
        }
      }
    }
  }


  private void cleanUpUserStatisticChartData() {
    List<Long> userIds = currentUsers.stream().map(RemoteUser::getId).collect(Collectors.toList());
    StatisticService statisticService = new StatisticService();
    List<StatisticChart> allStatisticCharts = statisticService.findAllStatisticCharts();
    if (allStatisticCharts != null) {
      for (StatisticChart chart : allStatisticCharts) {
        Long applicationId = Ivy.request().getApplication().getId();
        if (Ivy.repo().getInfo(chart).getCreatedByAppId() == applicationId && !userIds.contains(chart.getUserId())) {
          statisticService.delete(chart.getId());
        }
      }
    }
  }
}
