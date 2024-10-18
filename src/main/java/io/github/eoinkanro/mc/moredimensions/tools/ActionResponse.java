package io.github.eoinkanro.mc.moredimensions.tools;

public class ActionResponse {

  private final int code;
  private final String message;

  public ActionResponse(int code, String message) {
    this.code = code;
    this.message = message;
  }

  public int getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }
}
