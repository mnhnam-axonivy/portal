package ch.ivy.addon.portalkit.dto;

import java.util.ArrayList;
import java.util.List;

import ch.ivy.addon.portalkit.util.SecurityMemberDisplayNameUtils;
import ch.ivyteam.ivy.environment.Ivy;
import ch.ivyteam.ivy.security.IRole;
import ch.ivyteam.ivy.security.ISecurityMember;
import ch.ivyteam.ivy.security.IUser;
import ch.ivyteam.ivy.security.SubstitutionType;

public class DeputyRole {
  private List<ISecurityMember> deputies;
  private DeputyRoleType deputyRoleType;
  private IRole substitutionRole;
  private String deputyRoleDisplayName;
  private SubstitutionType substitutionType;
  private String description;
  private IUser ownerUser;

  public DeputyRole() {
    this.deputies = new ArrayList<>();
  }

  public List<ISecurityMember> getDeputies() {
    return deputies;
  }

  public void setDeputies(List<ISecurityMember> deputies) {
    this.deputies = deputies;
  }

  public void addDeputy(ISecurityMember deputy) {
    this.deputies.add(deputy);
  }

  public void removeDeputy(ISecurityMember deputy) {
    this.deputies.remove(deputy);
  }

  public DeputyRoleType getDeputyRoleType() {
    return deputyRoleType;
  }

  public void setDeputyRoleType(DeputyRoleType deputyRoleType) {
    this.deputyRoleType = deputyRoleType;
  }

  public String getDeputyRoleDisplayName() {
    this.deputyRoleDisplayName = DeputyRoleType.TASK_FOR_ROLE.equals(this.deputyRoleType) ? Ivy.cms().co("/ch.ivy.addon.portalkit.ui.jsf/AbsenceAndDeputy/taskForRole").concat(substitutionRole.getDisplayName())
        : Ivy.cms().co("/ch.ivy.addon.portalkit.ui.jsf/AbsenceAndDeputy/personalTask");
    return deputyRoleDisplayName;
  }

  public SubstitutionType getSubstitutionType() {
    return substitutionType;
  }

  public void setSubstitutionType(SubstitutionType substitutionType) {
    this.substitutionType = substitutionType;
  }

  public IRole getSubstitutionRole() {
    return substitutionRole;
  }

  public void setSubstitutionRole(IRole substitutionRole) {
    this.substitutionRole = substitutionRole;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public IUser getOwnerUser() {
    return ownerUser;
  }

  public void setOwnerUser(IUser ownerUser) {
    this.ownerUser = ownerUser;
  }

  public String getDeputiesDisplayNames() {
    List<String> responsibleNames = new ArrayList<>();
    for (ISecurityMember securityMember : deputies) {
      responsibleNames.add(SecurityMemberDisplayNameUtils.generateBriefDisplayNameForSecurityMember(securityMember, securityMember.getName()));
    }
    return String.join(", ", responsibleNames);
  }
}
