package com.vaadin.testbench.addons.framework;

import java.util.Locale;

import com.vaadin.frp.functions.TriFunction;

public interface GenericIDGenerator {

  static TriFunction<Class, Class, String, String> genericID() {
    return (uiClass, componentClass, label)
        -> (uiClass.getSimpleName()
            + "-" + componentClass.getSimpleName()
            + "-" + label.replace(" ", "-"))
        .toLowerCase(Locale.US);
  }

}