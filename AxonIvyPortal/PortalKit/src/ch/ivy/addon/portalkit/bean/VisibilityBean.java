package ch.ivy.addon.portalkit.bean;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.commons.lang.StringUtils;

import ch.ivy.addon.portalkit.enums.GlobalVariable;
import ch.ivy.addon.portalkit.service.GlobalSettingService;

@ManagedBean
@ViewScoped
public class VisibilityBean {
  private GlobalSettingService globalSettingService;
  
  @PostConstruct
  public void init() {
    globalSettingService = new GlobalSettingService();
  }
  
  public boolean isShowButtonIcon() {
    return globalSettingService.findGlobalSettingValueAsBoolean(GlobalVariable.SHOW_BUTTON_ICON.toString());
  }
  
  public String generateButtonIcon(String iconClass) {
    return globalSettingService.findGlobalSettingValueAsBoolean(GlobalVariable.SHOW_BUTTON_ICON.toString())? iconClass : StringUtils.EMPTY;
  }
}