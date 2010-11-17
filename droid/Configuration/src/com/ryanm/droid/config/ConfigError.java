package com.ryanm.droid.config;

import com.ryanm.droid.config.annote.Variable;

/**
 * Thrown to indicate something is wrong with the structure of your
 * {@link Variable}s
 * 
 * @author ryanm
 */
public class ConfigError extends Error {
	ConfigError(String reason) {
		super(reason);
	}
}
