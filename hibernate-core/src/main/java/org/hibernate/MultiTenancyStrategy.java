/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2011, Red Hat Inc. or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.hibernate;

import java.util.EnumSet;
import java.util.Map;

import org.hibernate.cfg.Environment;
import org.hibernate.internal.CoreMessageLogger;
import org.jboss.logging.Logger;

/**
 * Describes the methods for multi-tenancy understood by Hibernate.
 *
 * @author Steve Ebersole
 */
public enum MultiTenancyStrategy {
	/**
	 * Multi-tenancy implemented by use of discriminator columns.
	 */
	DISCRIMINATOR,
	/**
	 * Multi-tenancy implemented as separate schemas.
	 */
	SCHEMA,
	/**
	 * Multi-tenancy implemented as separate databases.
	 */
	DATABASE,
	/**
	 * No multi-tenancy.
	 */
	NONE;

	private static final CoreMessageLogger LOG = Logger.getMessageLogger(
			CoreMessageLogger.class,
			MultiTenancyStrategy.class.getName()
	);

	/**
	 * Does this strategy indicate a requirement for the specialized
	 * {@link org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider}, rather than the
	 * traditional {@link org.hibernate.engine.jdbc.connections.spi.ConnectionProvider}?
	 *
	 * @return {@code true} indicates a MultiTenantConnectionProvider is required; {@code false} indicates it is not.
	 */
	public boolean requiresMultiTenantConnectionProvider() {
		return this == DATABASE || this == SCHEMA;
	}

	public static boolean enabled(EnumSet<MultiTenancyStrategy> enabledStrategies) {
		return !enabledStrategies.contains(NONE);
	}

	public static boolean requiresMultiTenantConnectionProvider(EnumSet<MultiTenancyStrategy> enabledStrategies) {
		return enabledStrategies.contains(DATABASE) || enabledStrategies.contains(SCHEMA);
	}

	/**
	 * Extract the MultiTenancyStrategy from the setting map.
	 *
	 * @param properties The map of settings.
	 *
	 * @return The selected strategy.  {@link #NONE} is always the default.
	 */
	public static EnumSet<MultiTenancyStrategy> determineMultiTenancyStrategy(Map properties) {
		final Object strategy = properties.get( Environment.MULTI_TENANT );
		if ( strategy == null ) {
			return EnumSet.of(MultiTenancyStrategy.NONE);
		}

		if ( MultiTenancyStrategy.class.isInstance( strategy ) ) {
			return EnumSet.of((MultiTenancyStrategy) strategy);
		}

		final String strategyName = strategy.toString();
		try {
			return EnumSet.of(MultiTenancyStrategy.valueOf( strategyName.toUpperCase() ));
		}
		catch ( RuntimeException e ) {
			try {
				EnumSet<MultiTenancyStrategy> strategySet = EnumSet.noneOf(MultiTenancyStrategy.class);
				String[] split = strategyName.toUpperCase().split(",");
				for (int i = 0; i < split.length; i++) {
					strategySet.add(MultiTenancyStrategy.valueOf(split[i]));
				}
				if (strategySet.size() == 0 || (strategySet.size() > 1 && strategySet.contains(NONE)) ) {
					EnumSet.of(MultiTenancyStrategy.NONE);
				}
				return strategySet;
			} catch ( RuntimeException e2 ) {
				LOG.warn( "Unknown multi tenancy strategy [ " +strategyName +" ], using MultiTenancyStrategy.NONE." );
				return EnumSet.of(MultiTenancyStrategy.NONE);
			}
		}
	}
}
