package ch.ivy.addon.portalkit.bean;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.commons.lang.StringUtils;

import ch.ivy.addon.portalkit.enums.PortalPermission;
import ch.ivy.addon.portalkit.ivydata.utils.ServiceUtilities;
import ch.ivy.addon.portalkit.util.PermissionUtils;
import ch.ivyteam.ivy.environment.Ivy;
import ch.ivyteam.ivy.security.IPermission;
import ch.ivyteam.ivy.security.ISecurityContext;
import ch.ivyteam.ivy.security.ISession;
import ch.ivyteam.ivy.security.restricted.permission.IPermissionRepository;
import ch.ivyteam.ivy.workflow.ITask;
import ch.ivyteam.ivy.workflow.TaskState;

@ManagedBean
@ViewScoped
public class TaskActionBean {

  private Map<Long, Boolean> canResumeByTaskId = new HashMap<>();
  private boolean isShowResetTask;
  private boolean isShowReserveTask;
  private boolean isShowDelegateTask;
  private boolean isShowAdditionalOptions;

  public TaskActionBean() {
    isShowResetTask = PermissionUtils.hasPortalPermission(PortalPermission.TASK_DISPLAY_RESET_ACTION);
    isShowReserveTask = PermissionUtils.hasPortalPermission(PortalPermission.TASK_DISPLAY_RESERVE_ACTION);
    isShowDelegateTask = PermissionUtils.hasPortalPermission(PortalPermission.TASK_DISPLAY_DELEGATE_ACTION);
    isShowAdditionalOptions = PermissionUtils.hasPortalPermission(PortalPermission.TASK_DISPLAY_ADDITIONAL_OPTIONS);
  }

  public boolean canReset(ITask task) {
    TaskState taskState = task.getState();
    if (taskState != TaskState.RESUMED && taskState != TaskState.PARKED) {
      return false;
    }

    return (hasPermission(task, IPermission.TASK_RESET_OWN_WORKING_TASK) && canResume(task))
        || hasPermission(task, IPermission.TASK_RESET);
  }

  public boolean canDelegate(ITask task) {
    if (!isNotDone(task)) {
      return false;
    }

    if (userCanOnlyDelegateAssignedTask(task)) {
      return canResume(task);
    } else {
      return hasPermission(task, IPermission.TASK_WRITE_ACTIVATOR);
    }
  }

  private boolean userCanOnlyDelegateAssignedTask(ITask task) {
    IPermission permission =
        IPermissionRepository.get().findByName(PortalPermission.TASK_WRITE_ACTIVATOR_OWN_TASKS.getValue());
    if (Objects.isNull(permission)) {
      return false;
    }
    return hasPermission(task, permission) && !hasPermission(task, IPermission.TASK_WRITE_ACTIVATOR);
  }

  public boolean canResume(ITask task) {
    if (canResumeByTaskId.containsKey(task.getId())) {
      return canResumeByTaskId.get(task.getId());
    }

    ISession session = null;
    try {
      session = ServiceUtilities.findUserWorkflowSession(Ivy.session().getSessionUserName(), task.getApplication());
      boolean canResume = task.canUserResumeTask(session).wasSuccessful();
      canResumeByTaskId.put(task.getId(), canResume);
      return canResume;
    } finally {
      if (session != null && !Objects.equals(Ivy.wf().getApplication(), task.getApplication())) {
        ISecurityContext securityContext = task.getApplication().getSecurityContext();
        securityContext.destroySession(session.getIdentifier());
      }
    }
  }

  public boolean canPark(ITask task) {
    if (task.getState() != TaskState.SUSPENDED && task.getState() != TaskState.PARKED
        && task.getState() != TaskState.RESUMED) {
      return false;
    }

    if (!canResume(task)) {
      return false;
    }

    return hasPermission(task, IPermission.TASK_PARK_OWN_WORKING_TASK);
  }

  private boolean hasPermission(ITask task, IPermission permission) {
    if (task == null || permission == null) {
      return false;
    }
    return PermissionUtils.hasPermission(task.getApplication(), Ivy.session().getSessionUserName(), permission);
  }

  public boolean canChangePriority(ITask task) {
    return isNotDone(task) && hasPermission(task, IPermission.TASK_WRITE_ORIGINAL_PRIORITY);
  }

  public boolean canChangeExpiry(ITask task) {
    return (hasPermission(task, IPermission.TASK_WRITE_EXPIRY_TIMESTAMP) && task.getExpiryActivator() != null)
        || (task != null && StringUtils.isNotBlank(task.getExpiryTaskStartElementPid()));
  }

  public boolean notHaveExpiryHandleLogic(ITask task) {
    return isNotDone(task) && hasPermission(task, IPermission.TASK_WRITE_EXPIRY_TIMESTAMP)
        && task.getExpiryActivator() == null && StringUtils.isBlank(task.getExpiryTaskStartElementPid());
  }

  public boolean canChangeName(ITask task) {
    return isNotDone(task) && hasPermission(task, IPermission.TASK_WRITE_NAME);
  }

  public boolean canChangeDescription(ITask task) {
    return isNotDone(task) && hasPermission(task, IPermission.TASK_WRITE_DESCRIPTION);
  }

  public boolean canWriteDocument(ITask task) {
    return hasPermission(task, IPermission.DOCUMENT_WRITE)
        || hasPermission(task, IPermission.DOCUMENT_OF_INVOLVED_CASE_WRITE);
  }

  private boolean isNotDone(ITask task) {
    if (task == null) {
      return false;
    }
    EnumSet<TaskState> taskStates =
        EnumSet.of(TaskState.RESUMED, TaskState.PARKED, TaskState.SUSPENDED, TaskState.UNASSIGNED);
    return taskStates.contains(task.getState());
  }
  
  public void removeFromCanResumeByTaskId(long taskId) {
    canResumeByTaskId.remove(taskId);
  }

  public boolean isShowResetTask() {
    return isShowResetTask;
  }

  public void setShowResetTask(boolean isShowResetTask) {
    this.isShowResetTask = isShowResetTask;
  }

  public boolean isShowReserveTask() {
    return isShowReserveTask;
  }

  public void setShowReserveTask(boolean isShowReserveTask) {
    this.isShowReserveTask = isShowReserveTask;
  }

  public boolean isShowDelegateTask() {
    return isShowDelegateTask;
  }

  public void setShowDelegateTask(boolean isShowDelegateTask) {
    this.isShowDelegateTask = isShowDelegateTask;
  }

  public boolean isShowAdditionalOptions() {
    return isShowAdditionalOptions;
  }

  public void setShowAdditionalOptions(boolean isShowAdditionalOptions) {
    this.isShowAdditionalOptions = isShowAdditionalOptions;
  }
}
