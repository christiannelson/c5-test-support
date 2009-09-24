package com.carbonfive.test.spring.hibernate;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.orm.hibernate3.SessionHolder;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static java.lang.String.format;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * TestExecutionListener that opens a hibernate session and associates it with the current thread, simulating the open session in view pattern.
 */
public class HibernateOpenSessionTestExecutionListener extends AbstractTestExecutionListener
{
    protected final Logger log = LoggerFactory.getLogger(getClass());

    private final Map<Method, SessionFactory> sessionFactoryCache = Collections.synchronizedMap(new IdentityHashMap<Method, SessionFactory>());

    @Override
    public void beforeTestMethod(TestContext testContext) throws Exception
    {
        SessionFactory sessionFactory = lookupSessionFactory(testContext);

        sessionFactoryCache.put(testContext.getTestMethod(), sessionFactory);

        if (log.isInfoEnabled())
        {
            log.info(format("Associating a Hibernate Session with the current thread of execution."));
        }

        // Open a hibernate session and associate it with the current thread, simulating open session in view.
        Session session = SessionFactoryUtils.getSession(sessionFactory, true);
        TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(session));
    }

    @Override
    public void afterTestMethod(TestContext testContext) throws Exception
    {
        SessionFactory sessionFactory = sessionFactoryCache.get(testContext.getTestMethod());

        if (sessionFactoryCache.remove(testContext.getTestMethod()) == null) { return; }

        if (log.isInfoEnabled())
        {
            log.info(format("Closing associated Hibernate Session from the current thread of execution."));
        }

        SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager.unbindResource(sessionFactory);
        SessionFactoryUtils.closeSession(sessionHolder.getSession());
    }

    SessionFactory lookupSessionFactory(TestContext testContext)
    {
        String[] sfNames = testContext.getApplicationContext().getBeanNamesForType(SessionFactory.class);
        if (sfNames.length != 1)
        {
            final String s = "A single, unambiguous SessionFactory must be defined in the application context.";
            log.error(s);
            throw new IllegalStateException(s);
        }
        return (SessionFactory) testContext.getApplicationContext().getBean(sfNames[0]);
    }
}
