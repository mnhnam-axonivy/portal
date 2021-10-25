package ch.ivy.addon.portal.generic.bean;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.commons.lang3.StringUtils;

import ch.ivy.addon.portalkit.dto.dashboard.CustomDashboardWidget;
import ch.ivy.addon.portalkit.dto.dashboard.CustomDashboardWidgetParam;
import ch.ivy.addon.portalkit.enums.DashboardCustomWidgetType;
import ch.ivy.addon.portalkit.ivydata.service.impl.ProcessService;
import ch.ivyteam.ivy.environment.Ivy;
import ch.ivyteam.ivy.workflow.IProcessStart;
import ch.ivyteam.ivy.workflow.start.IWebStartable;

@ManagedBean
@ViewScoped
public class DashboardCustomWidgetBean implements Serializable {

  private static final long serialVersionUID = 7637567927058415789L;

  private static final String IS_DASHBOARD_PROCESS = "isDashboardProccess";

  private DashboardCustomWidgetType selectedType;
  private DashboardCustomWidgetType[] customWidgetTypes = DashboardCustomWidgetType.values();
  private String externalUrl;
  private String process;
  private List<IWebStartable> allPortalProcesses;
  private List<IProcessStart> startableProcessStarts;
  

  @PostConstruct
  public void init() {
    selectedType = DashboardCustomWidgetType.EXTERNAL_URL;
    allPortalProcesses = ProcessService.newInstance().findProcesses().getProcesses();
    allPortalProcesses = allPortalProcesses.stream()
      .filter(proccess -> StringUtils.isNotBlank(proccess.customFields().value(IS_DASHBOARD_PROCESS))
          && proccess.customFields().value(IS_DASHBOARD_PROCESS).contentEquals("true"))
      .collect(Collectors.toList());
    startableProcessStarts = Ivy.session().getStartableProcessStarts();
  }

  public void onSelectProcess(CustomDashboardWidget widget) {
    widget.getData().setProcessStart(getStartableProcessStarts().stream()
        .filter(process -> process.getLink().getRelative().contentEquals(widget.getData().getStartableProcessStart().getLink().getRelative()))
        .findFirst().get().getUserFriendlyRequestPath());
    widget.getData().setUrl(widget.getData().getStartableProcessStart().getLink().getRelative());
    widget.loadParametersFromProcess();
  }

  public String generateParamName(CustomDashboardWidgetParam param) {
    return param.getType().getPrefix().concat(param.getName());
  }

  public List<IWebStartable> completeProcesses(String query) {
    return this.allPortalProcesses.stream()
        .filter(process -> StringUtils.containsIgnoreCase(process.getName(), query)).collect(Collectors.toList());
  }

  public DashboardCustomWidgetType getSelectedType() {
    return selectedType;
  }

  public void setSelectedType(DashboardCustomWidgetType selectedType) {
    this.selectedType = selectedType;
  }

  public DashboardCustomWidgetType[] getCustomWidgetTypes() {
    return customWidgetTypes;
  }

  public void setCustomWidgetTypes(DashboardCustomWidgetType[] customWidgetTypes) {
    this.customWidgetTypes = customWidgetTypes;
  }

  public String getExternalUrl() {
    return externalUrl;
  }

  public void setExternalUrl(String externalUrl) {
    this.externalUrl = externalUrl;
  }

  public String getProcess() {
    return process;
  }

  public void setProcess(String process) {
    this.process = process;
  }

  public List<IWebStartable> getAllPortalProcesses() {
    return allPortalProcesses;
  }

  public void setAllPortalProcesses(List<IWebStartable> allPortalProcesses) {
    this.allPortalProcesses = allPortalProcesses;
  }

  public List<IProcessStart> getStartableProcessStarts() {
    return startableProcessStarts;
  }

  public void setStartableProcessStarts(List<IProcessStart> startableProcessStarts) {
    this.startableProcessStarts = startableProcessStarts;
  }
}
