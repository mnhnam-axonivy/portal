package ch.ivy.addon.portalkit.taskfilter;

import java.util.List;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.ivy.addon.portalkit.dto.SecurityMemberDTO;
import ch.ivy.addon.portalkit.util.SecurityMemberUtils;
import ch.ivyteam.ivy.environment.Ivy;
import ch.ivyteam.ivy.workflow.query.TaskQuery;

public class TaskResponsibleFilter extends TaskFilter {
  @JsonIgnore
  private SecurityMemberDTO selectedResponsible;
  private String selectedResponsibleMemberName;
  
  @Override
  public String label() {
    return Ivy.cms().co("/ch.ivy.addon.portalkit.ui.jsf/taskList/defaultColumns/ACTIVATOR");
  }

  @Override
  public String value() {
    if (StringUtils.isEmpty(selectedResponsibleMemberName)) {
      setSelectedResponsible(null);
      return ALL;
    } else {
      findSelectedResponsible();
    }
    return String.format(DOUBLE_QUOTES, formatName(selectedResponsible));
  }

  private SecurityMemberDTO getResponsibles(String memberName) {
    String queryParam = memberName.replaceFirst("#", "").trim();
    List<SecurityMemberDTO> responsibles = SecurityMemberUtils.findSecurityMembers(queryParam, 0, 1);
    if (CollectionUtils.isEmpty(responsibles)) {
      return null;
    }
    return responsibles.get(0);
  }

  @Override
  public TaskQuery buildQuery() {
    if (getSelectedResponsible() == null) {
      return null;
    }

    String memberName = selectedResponsible.getMemberName();
    return TaskQuery.create().where().activatorName().isEqual(memberName);
  }

  @Override
  public void resetValues() {
    selectedResponsible = null;
    selectedResponsibleMemberName = null;
  }

  @Override
  public boolean defaultFilter() {
    return true;
  }

  public String formatName(SecurityMemberDTO responsible) {
    String responsibleName = ALL;
    if (responsible != null) {
      if (StringUtils.isBlank(responsible.getDisplayName())) {
        responsibleName = responsible.getName();
      } else {
        responsibleName = responsible.getDisplayName() + " (" + responsible.getName() + ")";
      }
      return responsible.isEnabled()? responsibleName : Ivy.cms().co("/Labels/disabledUserPrefix") + " " + responsibleName;
    }
    return responsibleName;
  }

  public SecurityMemberDTO getSelectedResponsible() {
    return selectedResponsible;
  }

  public void setSelectedResponsible(SecurityMemberDTO selectedResponsible) {
    this.selectedResponsible = selectedResponsible;
    this.selectedResponsibleMemberName = Optional.ofNullable(selectedResponsible).map(SecurityMemberDTO::getMemberName).orElse(StringUtils.EMPTY);
  }

  /**
   * Checks selectedResponsibleMemberName which is saved on BusinessData
   * then find correspond SecurityMemberDTO of selectedResponsibleMemberName
   */
  public void findSelectedResponsible() {
    if (selectedResponsible == null || !StringUtils.equals(selectedResponsibleMemberName, selectedResponsible.getMemberName())) {
      setSelectedResponsible(getResponsibles(selectedResponsibleMemberName));
    }
  }

  public String getSelectedResponsibleMemberName() {
    return selectedResponsibleMemberName;
  }

  public void setSelectedResponsibleMemberName(String selectedResponsibleMemberName) {
    this.selectedResponsibleMemberName = selectedResponsibleMemberName;
  }
  
}
