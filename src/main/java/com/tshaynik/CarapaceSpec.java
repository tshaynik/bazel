package com.tshaynik;

import com.google.devtools.build.lib.runtime.BlazeCommand;
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

    List<Class<? extends BlazeCommand>> commandObjects = new ArrayList<>();
    commandObjects.add(AqueryCommand.class);
    commandObjects.add(BuildCommand.class);
    commandObjects.add(QueryCommand.class);

    List<Map<String, Object>> subcommands =
        commandObjects.stream()
            .map(CarapaceSpec::readCommandAnnotation)
            .collect(Collectors.toCollection(ArrayList::new));

    // `help` subcommand needs all other bazel subcommands as its subcommands
    // but just the names without all the flags.
    List<Map<String, Object>> subcommandNames =
        subcommands.stream()
            .map(
                map -> {
                  Map<String, Object> filteredMap = new HashMap<>();
                  if (map.containsKey("name")) {
                    filteredMap.put("name", map.get("name"));
                  }
                  return filteredMap;
                })
            .collect(Collectors.toList());

    Map<String, Object> helpCommand = readCommandAnnotation(HelpCommand.class);
    helpCommand.put("commands", subcommandNames);
    subcommands.add(helpCommand);

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

        Map<String, Object> options =
            Arrays.stream(cmd.options())
                .map(CarapaceSpec::readOptionAnnotation)
                .flatMap(map -> map.entrySet().stream())
                .collect(
                    Collectors.toMap(
                        entry -> entry.getKey(),
                        entry -> entry.getValue(),
                        (existingValue, newValue) -> newValue));
        spec.put("flags", options);
      }
    } catch (Exception exception) {
      exception.printStackTrace();
    }
    return spec;
  }

  static Map<String, String> readOptionAnnotation(Class<? extends OptionsBase> optionGroup) {
    Map<String, String> optionMap = new HashMap<>();
    Field[] fields = optionGroup.getDeclaredFields();
    for (Field field : fields) {
      try {
        if (field.isAnnotationPresent(Option.class)) {
          // getAnnotation returns Annotation type
          Annotation singleAnnotation = field.getAnnotation(Option.class);
          Option opt = (Option) singleAnnotation;

          String flag = "--" + opt.name();
          if (opt.abbrev() != '\0') {
            flag = "-" + opt.abbrev() + ", " + flag;
          }
          optionMap.put(flag, opt.valueHelp() + opt.help().replace("\\", "\\\\"));

          if (field.getType().equals(boolean.class)) {
            optionMap.put("--no" + opt.name(), opt.valueHelp() + " " + opt.help());
          }
        }
      } catch (Exception exception) {
        exception.printStackTrace();
      }
    }
    return optionMap;
  }
}
