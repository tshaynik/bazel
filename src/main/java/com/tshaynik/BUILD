java_binary(
    name = "CarapaceSpec",
    srcs = ["CarapaceSpec.java"],
    deps = [
        "//src/main/java/com/google/devtools/build/lib:runtime",
        "//src/main/java/com/google/devtools/build/lib/bazel:modules",
        "//src/main/java/com/google/devtools/build/lib/bazel/commands",
        "//src/main/java/com/google/devtools/build/lib/runtime/commands",
        "//src/main/java/com/google/devtools/build/lib/runtime/mobileinstall",
        "//src/main/java/com/google/devtools/common/options",
        "@maven//:org_yaml_snakeyaml",
    ],
)

genrule(
    name = "carapace_spec",
    outs = ["bazel.yaml"],
    tools = [":CarapaceSpec"],
    cmd = "$(location :CarapaceSpec) > $@",
)
