package com.zosh.exceptions;

public class InvalidRoleLoginException extends RuntimeException {
  public InvalidRoleLoginException(String message) {
    super(message);
  }
}

