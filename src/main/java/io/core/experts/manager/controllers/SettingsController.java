package io.core.experts.manager.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import io.core.experts.manager.repositories.SettingsRepository;

@Controller    // This means that this class is a Controller
@RequestMapping(path="/settings") // This means URL's start with /demo (after Application path)
public class SettingsController {
	@Autowired
	private SettingsRepository settingsRepository;
}
