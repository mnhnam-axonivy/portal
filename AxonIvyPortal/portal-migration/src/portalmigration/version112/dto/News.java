package portalmigration.version112.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.ivy.addon.portalkit.configuration.AbstractConfiguration;
import ch.ivyteam.ivy.cm.ContentObject;
import ch.ivyteam.ivy.environment.Ivy;

public class News extends AbstractConfiguration implements Serializable {

  private static final long serialVersionUID = 3593599946007221868L;
  public static final String DEFAULT_NEWS_ICON = "si si-advertising-megaphone-2";

  private String icon;
  private String name;
  private String description;
  private Date createdDate;

  @JsonIgnore
  private Locale locale;
  @JsonIgnore
  private ContentObject contentObject;

  public News() {}

  public News(ContentObject contentObject) {
    this.setId(contentObject.name());
    this.contentObject = contentObject;
    this.locale = Ivy.session().getContentLocale();
    this.icon = "";
    this.name = "";
    this.description = "";
    this.createdDate = null;
  }

  public News(ContentObject contentObject, Locale locale) {
    this.setId(contentObject.name());
    this.contentObject = contentObject;
    this.locale = locale;
    this.icon = "";
    this.name = "";
    this.description = "";
    this.createdDate = null;
  }

  public News(String icon, String name, String description, Locale locale) {
    this.icon = icon;
    this.name = name;
    this.description = description;
    this.locale = locale;
  }

  public String getIcon() {
    return StringUtils.isEmpty(icon) ? DEFAULT_NEWS_ICON : icon;
  }

  public void setIcon(String icon) {  
    this.icon = icon;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Locale getLocale() {
    return locale;
  }

  public void setLocale(Locale locale) {
    this.locale = locale;
  }

  public Date getCreatedDate() {
    return createdDate;
  }

  public void setCreatedDate(Date createdDate) {
    this.createdDate = createdDate;
  }
}