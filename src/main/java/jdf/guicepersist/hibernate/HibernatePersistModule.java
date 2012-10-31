/**
 * Copyright (C) 2010 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jdf.guicepersist.hibernate;

import java.util.Properties;

import org.aopalliance.intercept.MethodInterceptor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Singleton;
import com.google.inject.persist.PersistModule;
import com.google.inject.persist.PersistService;
import com.google.inject.persist.UnitOfWork;
import com.google.inject.util.Providers;

/**
 * Hibernate provider for guice persist.
 * 
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public final class HibernatePersistModule extends PersistModule {
	private final String jpaUnit;

	public HibernatePersistModule(String jpaUnit) {
		Preconditions.checkArgument(null != jpaUnit && jpaUnit.length() > 0,
				"JPA unit name must be a non-empty string.");
		this.jpaUnit = jpaUnit;
	}

	private Properties properties;
	private MethodInterceptor transactionInterceptor;

	@Override
	protected void configurePersistence() {
		bindConstant().annotatedWith(Hibernate.class).to(jpaUnit);

		if (null != properties) {
			bind(Properties.class).annotatedWith(Hibernate.class).toInstance(
					properties);
		} else {
			bind(Properties.class).annotatedWith(Hibernate.class)
					.toProvider(Providers.<Properties> of(null));
		}

		bind(HibernatePersistService.class).in(Singleton.class);

		bind(PersistService.class).to(HibernatePersistService.class);
		bind(UnitOfWork.class).to(HibernatePersistService.class);
		bind(Session.class).toProvider(HibernatePersistService.class);
		bind(SessionFactory.class)
				.toProvider(SessionFactoryProvider.class);

		transactionInterceptor = new HibernateLocalTxnInterceptor();
		requestInjection(transactionInterceptor);

	}

	@Override
	protected MethodInterceptor getTransactionInterceptor() {
		return transactionInterceptor;
	}

	/**
	 * Configures the JPA persistence provider with a set of properties.
	 * 
	 * @param properties A set of name value pairs that configure a JPA
	 *            persistence provider as per the specification.
	 */
	public HibernatePersistModule properties(Properties properties) {
		this.properties = properties;
		return this;
	}

}
