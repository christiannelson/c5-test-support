package com.carbonfive.testutils.spring.hibernate;

import org.hibernate.*;
import org.slf4j.*;
import org.springframework.orm.hibernate3.*;
import org.springframework.test.context.*;
import org.springframework.test.context.support.*;
import org.springframework.transaction.support.*;

import static java.lang.String.*;
import java.lang.reflect.*;
import java.util.*;

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
