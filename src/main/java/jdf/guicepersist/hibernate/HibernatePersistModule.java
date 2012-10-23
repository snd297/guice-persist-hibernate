/**
 * Copyright (C) 2008 Wideplay Interactive.
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

import javax.persistence.EntityManagerFactory;

import org.aopalliance.intercept.MethodInterceptor;
import org.hibernate.Session;

import com.google.common.base.Preconditions;
import com.google.inject.persist.PersistModule;
import com.google.inject.persist.PersistService;
import com.google.inject.persist.UnitOfWork;
import com.google.inject.util.Providers;

/**
 * @author Sam Donnelly
 */
public class HibernatePersistModule extends PersistModule {

	private final String jpaUnit;

	public HibernatePersistModule(String hibernateUnit) {
		Preconditions.checkArgument(
				null != hibernateUnit && hibernateUnit.length() > 0,
				"Hibernate unit name must be a non-empty string.");
		this.jpaUnit = hibernateUnit;
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

		bind(PersistService.class).to(HibernatePersistService.class);
		bind(UnitOfWork.class).to(HibernatePersistService.class);
		bind(Session.class).toProvider(HibernatePersistService.class);
		bind(EntityManagerFactory.class)
				.toProvider(
						JpaPersistService.EntityManagerFactoryProvider.class);

		transactionInterceptor = new JpaLocalTxnInterceptor();
		requestInjection(transactionInterceptor);

		// Bind dynamic finders.
		for (Class<?> finder : dynamicFinders) {

	}

	@Override
	protected MethodInterceptor getTransactionInterceptor() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

}
