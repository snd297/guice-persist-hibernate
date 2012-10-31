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

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.persist.PersistService;
import com.google.inject.persist.UnitOfWork;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 * @author Robbie Vanbrabant
 * @since 1.0
 */
@Singleton
class HibernatePersistService implements Provider<Session>, UnitOfWork,
		PersistService {
	private final ThreadLocal<Session> session = new ThreadLocal<Session>();
	private final Provider<SessionFactory> sessionFactoryProvider;

	@Inject
	HibernatePersistService(
			Provider<SessionFactory> sessionFactoryProvider) {
		this.sessionFactoryProvider = sessionFactoryProvider;
	}

	public void start() {
		// the provider lazily loads, force start.
		// does its own synchronization and simply returns
		// a closed SessionFactory if it has been closed.
		sessionFactoryProvider.get();
	}

	public synchronized void stop() {
		// Hibernate silently lets this call pass
		// if the SessionFactory has been closed already,
		// but a SessionFactory is not thread safe,
		// so we define this method as synchronized.
		// If users use the SessionFactory directly, they're on their own.
		sessionFactoryProvider.get().close();
	}

	public String toString() {
		return String.format("%s[sessionFactory: %s]", super.toString(),
				this.sessionFactoryProvider);
	}

	public Session get() {
		if (!isWorking()) {
			begin();
		}

		Session em = session.get();
		Preconditions
				.checkState(
						null != em,
						"Requested EntityManager outside work unit. "
								+ "Try calling UnitOfWork.begin() first, or use a PersistFilter if you "
								+ "are inside a servlet environment.");

		return em;
	}

	public boolean isWorking() {
		return session.get() != null;
	}

	public void begin() {
		Preconditions
				.checkState(
						null == session.get(),
						"Work already begun on this thread. Looks like you have called UnitOfWork.begin() twice"
								+ " without a balancing call to end() in between.");

		session.set(sessionFactoryProvider.get().openSession());
	}

	public void end() {
		Session s = session.get();

		// Let's not penalize users for calling end() multiple times.
		if (null == s) {
			return;
		}

		s.close();
		session.remove();
	}

}
