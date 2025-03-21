package com.tshaynik;

import com.google.devtools.build.lib.runtime.Command;
import com.google.devtools.build.lib.runtime.commands.*;
import com.google.devtools.common.options.Option;
import com.google.devtools.common.options.OptionsBase;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;

class CarapaceSpec {
  public static void main(String[] args) {
    Class<BuildCommand> classObject = BuildCommand.class;
    readCommandAnnotation(classObject);
  }

  static void readCommandAnnotation(AnnotatedElement element) {
    try {
      if (element.isAnnotationPresent(Command.class)) {
        // getAnnotation returns Annotation type
        Annotation singleAnnotation = element.getAnnotation(Command.class);
        Command cmd = (Command) singleAnnotation;

        System.out.println(cmd.name());
        System.out.println(cmd.shortDescription());

        for (Class<? extends OptionsBase> optionClass : cmd.options()) {
          readOptionAnnotation(optionClass);
        }
      }
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  static void readOptionAnnotation(Class<? extends OptionsBase> optionGroup) {
    Field[] fields = optionGroup.getDeclaredFields();
    for (Field field : fields) {
      System.out.println("  name:" + field.getName());
      System.out.println("  type:" + field.getType().getName());
      try {
        if (field.isAnnotationPresent(Option.class)) {
          // getAnnotation returns Annotation type
          Annotation singleAnnotation = field.getAnnotation(Option.class);
          Option opt = (Option) singleAnnotation;

          System.out.println("  " + opt.name());
        }
      } catch (Exception exception) {
        exception.printStackTrace();
      }
      System.out.println("");
    }
  }
}
