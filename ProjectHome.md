Latest Version: 0.9.2-m2

A collection of very simple utilities with the goal of making writing tests easier with less code.

## FunctionalTestRunner - JUnit Extension ##

Added in 0.9.2-m1, read about it on the Carbon Five blog:
http://blog.carbonfive.com/2009/10/testing/c5-test-support-new-addition-functionaltestrunner

## Spring 2.5+ Test Extensions ##
### DataSetTestExecutionListener ###
Loads DBUnit test fixtures before test methods flagged with the @DataSet annotation.  Participates in an active transactions if available or in auto-commit mode if one is not.

```
@ContextConfiguration(locations = {"classpath:applicationContext.xml"})
public class TripRepositoryImplTest extends AbstractTransactionalDataSetTestCase
{
    @Autowired TripRepository repository;
 
    @Test
    @DataSet
    public void forIdShouldFindTrip() throws Exception
    {
        Trip trip = repository.forId(2);
        assertThat(trip, not(nullValue()));
    }
}
```

The high-level execution path for this example looks like:

  1. Inject dependencies (DependencyInjectionTestExecutionListener)
  1. Start transaction (TransactionalTestExecutionListener)
  1. Load dbunit data set from TripRepositoryImplTest.xml (DataSetTestExecutionListener) using the setup operation (default is CLEAN\_INSERT)
  1. Execute test
  1. Optionally cleanup dbunit data using the tear down operation (default is NONE)
  1. Rollback transaction (TransactionalTestExecutionListener)

Here’s the trimmed down log output for this test:

```
INFO: Began transaction (1): transaction manager; rollback [true] (TransactionalTestExecutionListener.java:259)
INFO: Loading dataset from location 'classpath:/eg/domain/TripRepositoryImplTest.xml' using operation 'CLEAN_INSERT'. (DataSetTestExecutionListener.java:152)
INFO: Tearing down dataset using operation 'NONE', leaving database connection open. (DataSetTestExecutionListener.java:67)
INFO: Rolled back transaction after test execution for test context (TransactionalTestExecutionListener.java:279)
```

See http://blog.carbonfive.com/2008/07/testing/database-testing-with-spring-25-and-dbunit for additional details.

### HibernateOpenSessionTestExecutionListener ###

Opens a Hibernate Session for the duration of a test method so that you can fetch lazy associations and collections.  Useful for integration testing.

## Data Fixtures ##
  * DBUnitFixture - Load a DBUnit dataset easily.

  * SQLFixture - Load a SQL script easily.

## EasyMock Extensions ##
  * ArgumentAssertion - Make assertions on parameters passed to a mock.
