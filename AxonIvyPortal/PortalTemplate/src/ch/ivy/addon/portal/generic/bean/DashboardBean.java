package ch.ivy.addon.portal.generic.bean;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.component.tabview.TabView;
import org.primefaces.event.TabChangeEvent;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.ivy.addon.portalkit.dto.dashboard.ColumnModel;
import ch.ivy.addon.portalkit.dto.dashboard.CreatedDateColumnModel;
import ch.ivy.addon.portalkit.dto.dashboard.Dashboard;
import ch.ivy.addon.portalkit.dto.dashboard.DashboardWidget;
import ch.ivy.addon.portalkit.dto.dashboard.DescriptionColumnModel;
import ch.ivy.addon.portalkit.dto.dashboard.ExpiryDateColumnModel;
import ch.ivy.addon.portalkit.dto.dashboard.IdColumnModel;
import ch.ivy.addon.portalkit.dto.dashboard.NameColumnModel;
import ch.ivy.addon.portalkit.dto.dashboard.PriorityColumnModel;
import ch.ivy.addon.portalkit.dto.dashboard.ResponsibleColumnModel;
import ch.ivy.addon.portalkit.dto.dashboard.StartColumnModel;
import ch.ivy.addon.portalkit.dto.dashboard.StateColumnModel;
import ch.ivy.addon.portalkit.dto.dashboard.TaskDashboardWidget;
import ch.ivy.addon.portalkit.dto.dashboard.WidgetSample;
import ch.ivy.addon.portalkit.enums.DashboardStandardTaskColumn;
import ch.ivy.addon.portalkit.enums.DashboardWidgetType;
import ch.ivy.addon.portalkit.enums.PortalLibrary;
import ch.ivy.addon.portalkit.enums.TaskSortField;
import ch.ivy.addon.portalkit.loader.ResourceLoader;
import ch.ivy.addon.portalkit.util.PermissionUtils;
import ch.ivyteam.ivy.application.ILibrary;
import ch.ivyteam.ivy.application.property.ICustomProperties;
import ch.ivyteam.ivy.application.property.ICustomProperty;
import ch.ivyteam.ivy.environment.Ivy;
import ch.ivyteam.ivy.workflow.ITask;

@ViewScoped
@ManagedBean
public class DashboardBean implements Serializable {

  private static final long serialVersionUID = -4224901891867040688L;
  private List<Dashboard> dashboards;
  private Dashboard selectedDashboard;
  private List<WidgetSample> samples;
  private ObjectMapper mapper;
  private DashboardWidget widget;
  private boolean isReadOnlyMode;
  private int tabActiveIndex;
  private boolean canEdit;
  private String dashboardPropertyPrefix;
  private String switchEditModeText;
  private String switchViewModeText;

  @PostConstruct
  public void init() {
    dashboardPropertyPrefix = "dashboard.widgets." + Ivy.session().getSessionUserName();
    canEdit = PermissionUtils.hasDashboardWritePermission();
    tabActiveIndex = 0;
    isReadOnlyMode = true;
    dashboards = new ArrayList<>();
    mapper = new ObjectMapper();
    mapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
    samples = List.of(taskSample(), caseSample(), statisticSample(), processSample());
    List<ICustomProperty> properties = customProperties().findAllStartingWith(dashboardPropertyPrefix);
    try {
      dashboards = defaultDashboards();
      if (CollectionUtils.isNotEmpty(properties)) {
        for (ICustomProperty property : properties) {
          Dashboard d = mapper.readValue(property.getValue(), Dashboard.class);
          dashboards.set(dashboards.indexOf(d), d);
        }
      }
      selectedDashboard = dashboards.get(0);
      buildWidgetModels();
    } catch (IOException e) {
      Ivy.log().error(e);
    }
    switchEditModeText = translate("/ch.ivy.addon.portalkit.ui.jsf/dashboard/switchToEditMode");
    switchViewModeText = translate("/ch.ivy.addon.portalkit.ui.jsf/dashboard/switchToViewMode");
  }
  
  private void buildWidgetModels() {
    for (Dashboard dashboard : dashboards) {
      for (DashboardWidget widget : dashboard.getWidgets()) {
        if (widget instanceof TaskDashboardWidget) {
          TaskDashboardWidget taskWidget = (TaskDashboardWidget) widget;
          buildColumns(taskWidget);
        }
      }
    }
  }
  
  private void buildColumns(TaskDashboardWidget taskWidget) {
    List<ColumnModel> columns = taskWidget.getColumns();
    for (int i = 0; i < columns.size(); i++) {
      ColumnModel column = columns.get(i);
      String field = column.getField();
      if (DashboardStandardTaskColumn.START.getField().equalsIgnoreCase(field)) {
        column = mapper.convertValue(column, StartColumnModel.class);
      } else if (DashboardStandardTaskColumn.PRIORITY.getField().equalsIgnoreCase(field)) {
        column = mapper.convertValue(column, PriorityColumnModel.class);
      } else if (DashboardStandardTaskColumn.ID.getField().equalsIgnoreCase(field)) {
        column = mapper.convertValue(column, IdColumnModel.class);
      } else if (DashboardStandardTaskColumn.NAME.getField().equalsIgnoreCase(field)) {
        column = mapper.convertValue(column, NameColumnModel.class);
      } else if (DashboardStandardTaskColumn.DESCRIPTION.getField().equalsIgnoreCase(field)) {
        column = mapper.convertValue(column, DescriptionColumnModel.class);
      } else if (DashboardStandardTaskColumn.RESPONSIBLE.getField().equalsIgnoreCase(field)) {
        column = mapper.convertValue(column, ResponsibleColumnModel.class);
      } else if (DashboardStandardTaskColumn.STATE.getField().equalsIgnoreCase(field)) {
        column = mapper.convertValue(column, StateColumnModel.class);
      } else if (DashboardStandardTaskColumn.CREATED.getField().equalsIgnoreCase(field)) {
        column = mapper.convertValue(column, CreatedDateColumnModel.class);
      } else if (DashboardStandardTaskColumn.EXPIRY.getField().equalsIgnoreCase(field)) {
        column = mapper.convertValue(column, ExpiryDateColumnModel.class);
      }
      column.initDefaultValue();
      columns.set(i, column);
    }
  }
  
  private List<Dashboard> defaultDashboards() throws IOException {
    ILibrary portalStyleLib = Ivy.wf().getApplication().findReleasedLibrary(PortalLibrary.PORTAL_STYLE.getValue());
    ResourceLoader loader = new ResourceLoader(portalStyleLib.getProcessModelVersion());
    Optional<Path> path = loader.getWidgetConfiguration();
    String read = String.join("\n", Files.readAllLines(path.get()));
    return new ArrayList<>(Arrays.asList(mapper.readValue(read, Dashboard[].class)));
  }
  
  public List<Dashboard> getDashboards() {
    return dashboards;
  }

  public List<WidgetSample> getSamples() {
    return samples;
  }

  public TaskDashboardWidget getDefaultTaskDashboardWidget() throws IOException {
    TaskDashboardWidget result = new TaskDashboardWidget();
    result.setId(getNewTaskWidgetId());
    result.setName(translate("/ch.ivy.addon.portalkit.ui.jsf/dashboard/yourTasks"));
    result.setWidth(8);
    result.setHeight(6);
    result.setAutoPosition(true);
    result.setSortField(TaskSortField.ID.toString());
    result.setSortDescending(true);
    buildColumns(result);
    return result;
  }
  
  public void save() throws JsonParseException, JsonMappingException, IOException {
    Map<String, String> requestParamMap = getRequestParameterMap();
    String nodes = Optional.ofNullable(requestParamMap.get("nodes")).orElse(StringUtils.EMPTY);
    List<DashboardWidget> widgets = Arrays.asList(mapper.readValue(nodes, DashboardWidget[].class));
    for (DashboardWidget widget : widgets) {
      DashboardWidget updatedWidget = selectedDashboard.getWidgets().get(selectedDashboard.getWidgets().indexOf(widget));
      updatedWidget.setX(widget.getX());
      updatedWidget.setY(widget.getY());
      updatedWidget.setWidth(widget.getWidth());
      updatedWidget.setHeight(widget.getHeight());
    }
    customProperties().property(dashboardProperty(selectedDashboard)).setValue(mapper.writeValueAsString(selectedDashboard));
  }

  public void saveWidget() throws JsonProcessingException {
    List<DashboardWidget> widgets = selectedDashboard.getWidgets();
    if (widgets.contains(widget)) {
      widgets.set(widgets.indexOf(widget), widget);
    } else {
      widgets.add(widget);
    }
    dashboards.set(dashboards.indexOf(selectedDashboard), selectedDashboard);
    List<ICustomProperty> properties = customProperties().findAllStartingWith(dashboardPropertyPrefix);
    if (CollectionUtils.isNotEmpty(properties)) {
      customProperties().property(dashboardProperty(selectedDashboard)).setValue(mapper.writeValueAsString(selectedDashboard));
    } else {
      for (Dashboard dashboard : dashboards) {
        customProperties().property(dashboardProperty(dashboard)).setValue(mapper.writeValueAsString(dashboard));
      }
    }
  }
  
  private String dashboardProperty(Dashboard dashboard) {
    return dashboardPropertyPrefix + "." + dashboard.getId();
  }
  
  public void create() throws IOException {
    widget = getDefaultTaskDashboardWidget();
  }
  
  public void restore() throws IOException {
    List<ICustomProperty> properties = customProperties().findAllStartingWith(dashboardProperty(selectedDashboard));
    for (ICustomProperty property : properties) {
      customProperties().delete(property.getName());
    }
    List<Dashboard> defaultDashboards = defaultDashboards();
    selectedDashboard = defaultDashboards.get(defaultDashboards.indexOf(selectedDashboard));
    dashboards.set(dashboards.indexOf(selectedDashboard), selectedDashboard);
    for (DashboardWidget widget : selectedDashboard.getWidgets()) {
      if (widget instanceof TaskDashboardWidget) {
        TaskDashboardWidget taskWidget = (TaskDashboardWidget) widget;
        buildColumns(taskWidget);
      }
    }
  }
  
  public void onTabChange(TabChangeEvent event) {
    selectedDashboard = (Dashboard) event.getData();
    tabActiveIndex = ((TabView) event.getSource()).getIndex();
  }
  
  public String getNewTaskWidgetId() {
    String result = "";
    List<String> ids = selectedDashboard.getWidgets().stream()
        .filter(widget -> widget.getId().startsWith("task_"))
        .map(DashboardWidget::getId).collect(Collectors.toList());

    Integer maxId = Collections.max(ids.stream().map(id -> Integer.parseInt(id.replace("task_", ""))).collect(Collectors.toList()));
    if (maxId == null || maxId < 0) {
      result = "task_0";
    } else {
      result = "task_".concat(Integer.toString(maxId + 1));
    }
    return result;
  }
  
  public void toggleMode() {
    isReadOnlyMode = !isReadOnlyMode;
  }
  
  public void startTask(ITask task) throws IOException {
    FacesContext.getCurrentInstance().getExternalContext().redirect(task.getStartLinkEmbedded().getRelative());
  }
  
  private Map<String, String> getRequestParameterMap() {
    Map<String, String> requestParamMap =
        FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
    return requestParamMap;
  }
  
  private WidgetSample taskSample() {
    return new WidgetSample(translate("/ch.ivy.addon.portalkit.ui.jsf/dashboard/taskList"), DashboardWidgetType.TASK, "task-widget-prototype.png");
  }
  
  private WidgetSample caseSample() {
    return new WidgetSample(translate("/ch.ivy.addon.portalkit.ui.jsf/dashboard/caseList"), DashboardWidgetType.CASE, "case-widget-prototype.png");
  }
  
  private WidgetSample statisticSample() {
    return new WidgetSample(translate("/ch.ivy.addon.portalkit.ui.jsf/dashboard/statisticWidget"), DashboardWidgetType.STATISTIC, "statistic-widget-prototype.png");
  }
  
  private WidgetSample processSample() {
    return new WidgetSample(translate("/ch.ivy.addon.portalkit.ui.jsf/dashboard/processList"), DashboardWidgetType.PROCESS, "process-widget-prototype.png");
  }
  
  public DashboardWidget getWidget() {
    return widget;
  }
  
  public void setWidget(DashboardWidget widget) {
    this.widget = widget;
  }
  
  public boolean getIsReadOnlyMode() {
    return isReadOnlyMode;
  }
  
  public void setIsReadOnlyMode(boolean isReadOnlyMode) {
    this.isReadOnlyMode = isReadOnlyMode;
  }
  
  public int getTabActiveIndex() {
    return tabActiveIndex;
  }
  
  public void setTabActiveIndex(int tabActiveIndex) {
    this.tabActiveIndex = tabActiveIndex;
  }
  
  public boolean getCanEdit() {
    return canEdit;
  }
  
  public String getSwitchButtonText() {
    return isReadOnlyMode ? switchEditModeText : switchViewModeText;
  }
  
  private String translate(String cms) {
    return Ivy.cms().co(cms);
  }
  
  private ICustomProperties customProperties() {
    return Ivy.wf().getApplication().customProperties();
  }
}
