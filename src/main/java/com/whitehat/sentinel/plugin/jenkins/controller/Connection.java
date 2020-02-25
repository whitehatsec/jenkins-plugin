package com.whitehat.sentinel.plugin.jenkins.controller;

public interface Connection  {
	boolean validate(String apiKey, String url) throws ValidateException;
}
