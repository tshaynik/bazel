package com.tshaynik;

import com.google.devtools.build.lib.runtime.Command;
import com.google.devtools.build.lib.runtime.commands.*;
import com.google.devtools.common.options.Option;
import com.google.devtools.common.options.OptionsBase;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.*;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.yaml.snakeyaml.Yaml;

class CarapaceSpec {
  public static void main(String[] args) {
    Map<String, Object> spec = new HashMap<>();
    spec.put("name", "bazel");
    String[] aliases = {"bazelisk"};
    spec.put("aliases", aliases);
    spec.put("description", "build system");

    Class<BuildCommand> classObject = BuildCommand.class;
    Map<String, Object> subcommands = readCommandAnnotation(classObject);
    spec.put("commands", subcommands);

    Class<com.google.devtools.build.lib.bazel.BazelStartupOptionsModule.Options> startupClass =
        com.google.devtools.build.lib.bazel.BazelStartupOptionsModule.Options.class;
    spec.put("flags", readOptionAnnotation(startupClass));

    PrintWriter writer = new PrintWriter(System.out, true);
    Yaml yaml = new Yaml();
    yaml.dump(spec, writer);
  }

  static Map<String, Object> readCommandAnnotation(AnnotatedElement element) {
    Map<String, Object> spec = new HashMap<>();
    try {
      if (element.isAnnotationPresent(Command.class)) {
        // getAnnotation returns Annotation type
        Annotation singleAnnotation = element.getAnnotation(Command.class);
        Command cmd = (Command) singleAnnotation;

        spec.put("name", cmd.name());
        spec.put("description", cmd.shortDescription());
        // spec.put("uses_configuration_options", cmd.usesConfigurationOptions());
        // spec.put("build_phase", cmd.buildPhase());

        Map<String, Object> options =
            Arrays.stream(cmd.options())
                .map(CarapaceSpec::readOptionAnnotation)
                .flatMap(map -> map.entrySet().stream())
                .collect(
                    Collectors.toMap(
                        entry -> entry.getKey(),
                        entry -> Optional.ofNullable(entry.getValue()),
                        (existingValue, newValue) -> newValue));
        spec.put("flags", options);
      }
    } catch (Exception exception) {
      exception.printStackTrace();
    }
    return spec;
  }

  static Map<String, Object> readOptionAnnotation(Class<? extends OptionsBase> optionGroup) {
    Map<String, Object> optionMap = new HashMap<>();
    Field[] fields = optionGroup.getDeclaredFields();
    for (Field field : fields) {
      try {
        if (field.isAnnotationPresent(Option.class)) {
          // getAnnotation returns Annotation type
          Annotation singleAnnotation = field.getAnnotation(Option.class);
          Option opt = (Option) singleAnnotation;

          String flag = "--" + opt.name();
          if (opt.abbrev() != '\0') {
            flag += ", -" + opt.abbrev();
          }
          optionMap.put(flag, getFlag(opt));

          if (field.getType().equals(boolean.class)) {
            optionMap.put("--no" + opt.name(), getFlag(opt));
          }
        }
      } catch (Exception exception) {
        exception.printStackTrace();
      }
    }
    return optionMap;
  }

  static Map<String, Object> getFlag(Option opt) {
    Map<String, Object> optionMap = new HashMap<>();
    optionMap.put("description", opt.help());
    // optionMap.put("valueHelp", opt.valueHelp());
    // optionMap.put("defaultValue", opt.defaultValue());
    // optionMap.put("documentationCategory", opt.documentationCategory());
    return optionMap;
  }
}
