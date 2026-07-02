package com.tallerwebi.presentacion;

import java.io.PrintWriter;
import java.io.StringWriter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(Exception.class)
  public ModelAndView handleAllExceptions(Exception ex) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    ex.printStackTrace(pw);

    ModelAndView mav = new ModelAndView("error_detallado");
    mav.addObject("mensaje", ex.getMessage());
    mav.addObject("stackTrace", sw.toString());
    return mav;
  }
}
