/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.id;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.Session;
import org.hibernate.Transaction;

import org.hibernate.testing.TestForIssue;
import org.hibernate.testing.junit4.BaseCoreFunctionalTestCase;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * @author Vlad Mihalcea
 */
public class QuotedIdentifierTest extends BaseCoreFunctionalTestCase {
	@Test
	@TestForIssue( jiraKey="HHH-9271" )
	public void testDirectIdPropertyAccess() throws Exception {
		Session s = openSession();
		Transaction transaction = s.beginTransaction();
		QuotedIdentifier o = new QuotedIdentifier();
		o.timestamp = System.currentTimeMillis();
		s.persist( o );
		transaction.commit();
		s.close();

		s = openSession();
		transaction = s.beginTransaction();
		o = session.get( QuotedIdentifier.class, o.index );
		assertNotNull(o);
		transaction.commit();
		s.close();
	}

	@Override
	protected Class<?>[] getAnnotatedClasses() {
		return new Class<?>[] {
			QuotedIdentifier.class
		};
	}

	@Entity(name = "QuotedIdentifier")
	@Table( name = "`table`")
	public static class QuotedIdentifier {

		@Id
		@GeneratedValue(strategy = GenerationType.IDENTITY)
		@Column(name = "`index`")
		private int index;

		@Column(name = "`timestamp`")
		private long timestamp;
	}
}
