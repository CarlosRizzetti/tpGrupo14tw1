package com.tallerwebi.presentacion.dto;

public class CategoryOptionDto {

  private Long id;
  private String name;
  private boolean is_present;

  public CategoryOptionDto(Long id, String name, boolean is_present) {
    this.id = id;
    this.name = name;
    this.is_present = is_present;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isIs_present() {
    return is_present;
  }

  public void setIs_present(boolean is_present) {
    this.is_present = is_present;
  }
}
