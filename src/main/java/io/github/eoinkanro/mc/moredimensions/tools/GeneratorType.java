package io.github.eoinkanro.mc.moredimensions.tools;

import java.util.Arrays;
import java.util.Set;

public enum GeneratorType {

  RANDOM(Set.of("rand", "random")),
  OVERWORLD(Set.of("over", "overworld", "main")),;

  private final Set<String> names;

  GeneratorType(Set<String> names) {
    this.names = names;
  }

  public Set<String> getNames() {
    return names;
  }

  public static GeneratorType ofName(String name) {
    return Arrays.stream(values())
        .filter(x -> x.names.contains(name))
        .findAny()
        .orElse(OVERWORLD);
  }

}
