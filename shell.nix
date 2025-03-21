{
  pkgs ? import <nixpkgs> { },
}:

(pkgs.buildFHSEnv {
  name = "bazel-userenv-example";
  targetPkgs = pkgs: [
    pkgs.bazelisk
    pkgs.jdk
    pkgs.gcc
    pkgs.glibc
    pkgs.libz
    pkgs.zip
    pkgs.unzip
  ];
}).env
