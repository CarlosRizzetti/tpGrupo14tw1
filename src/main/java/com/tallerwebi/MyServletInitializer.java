package com.tallerwebi;

import com.tallerwebi.config.DatabaseInitializationConfig;
import com.tallerwebi.config.HibernateConfig;
import com.tallerwebi.config.MailConfig;
import com.tallerwebi.config.SecurityConfig;
import com.tallerwebi.config.SpringWebConfig;
import java.util.Collections;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.SessionTrackingMode;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class MyServletInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

  @Override
  public void onStartup(ServletContext servletContext) throws ServletException {
    super.onStartup(servletContext);
    servletContext.setSessionTrackingModes(Collections.singleton(SessionTrackingMode.COOKIE));
  }

  // services and data sources
  @Override
  protected Class<?>[] getRootConfigClasses() {
    return new Class[0];
  }

  // controller, view resolver, handler mapping
  @Override
  protected Class<?>[] getServletConfigClasses() {
    return new Class[] {
      SpringWebConfig.class,
      HibernateConfig.class,
      DatabaseInitializationConfig.class,
      MailConfig.class,
      SecurityConfig.class,
      com.tallerwebi.config.ClienteAuthenticationProvider.class,
      com.tallerwebi.config.ClienteSecurityConfig.class,
    };
  }

  @Override
  protected String[] getServletMappings() {
    return new String[] { "/" };
  }

  @Override
  protected javax.servlet.Filter[] getServletFilters() {
    org.springframework.web.filter.CharacterEncodingFilter characterEncodingFilter =
      new org.springframework.web.filter.CharacterEncodingFilter();
    characterEncodingFilter.setEncoding("UTF-8");
    characterEncodingFilter.setForceEncoding(true);
    return new javax.servlet.Filter[] { characterEncodingFilter };
  }
}
