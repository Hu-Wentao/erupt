package xyz.demo.erupt.example;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringRunner;
import xyz.erupt.auth.model.EruptUser;
import xyz.erupt.auth.util.IpUtil;
import xyz.erupt.core.config.EruptProp;
import xyz.erupt.core.dao.EruptDao;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.service.data_impl.EruptDbService;
import xyz.erupt.job.model.EruptJob;
import xyz.erupt.job.service.EruptJobService;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DemoApplicationTests {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private EruptDbService dbService;

    public static final String DATASOURCE_PREFIX = "spring.datasource.";

    @Autowired
    private EruptDao eruptDao;

    @Test
    public void testJob() throws ParseException, SchedulerException {
        EruptJob eruptJob = new EruptJob();
        eruptJobService.modifyJob(eruptJob);
    }

    @Test
    public void eruptDaoObj() {
        Object[] oo = eruptDao.queryObject(EruptUser.class, "account = 'erupt'", null, "id", "name");
        Assert.assertEquals(oo.length, 2);
        System.out.println(oo[0] + ":" + oo[1]);
    }

    @Test
    public void eruptDaoMap() {
        try {
            Map<String, Object> map = eruptDao.queryMap(EruptUser.class,
                    "account = '2222'", null, "id", "name");
            for (String s : map.keySet()) {
                System.out.println(s + ":" + map.get(s));
            }
        } catch (NoResultException e) {
            System.out.println(e.getMessage());
        }

    }

    @Test
    public void eruptDao() {
        List<EruptUser> list = eruptDao.queryEntityList(EruptUser.class, "1=1 order by account desc", null);
        for (EruptUser user : list) {
            System.out.println(user.getAccount());
        }
    }

    @Test
    public void erupt() {
        int i = 1000;
        long start = System.currentTimeMillis();
        for (int i1 = 0; i1 < i; i1++) {
            EruptCoreService.getErupt("Demo");
        }
        System.out.println(((System.currentTimeMillis() - start) / 1000) + 's');
    }

    @Autowired
    private EruptJobService eruptJobService;

    @Resource
    private DataSourceProperties dataSourceProperties;

    @Resource
    private Environment env;

    @Autowired
    private EruptProp eruptProp;

    @Test
    public void getProperties() {
        System.out.println(dataSourceProperties.getUrl());
        System.out.println(env.getProperty("server.compression.mime-types"));
        System.out.println(env.getProperty("spring.resources.datasource.url"));
        System.out.println(env.getProperty("erupt.upload-path"));
    }

//    @Test
//    public void craeteEntityManager() {
//        String sourceName = "one";
//        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
//        {
//            HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
//            vendorAdapter.setGenerateDdl(false);
//            vendorAdapter.setDatabase(Database.SQL_SERVER);
//            vendorAdapter.setShowSql(true);
//            factory.setJpaVendorAdapter(vendorAdapter);
//        }
//        {
//            factory.setDataSource(DataSourceBuilder.create()
//                    .url(env.getProperty(DATASOURCE_PREFIX + sourceName + ".url"))
//                    .username(env.getProperty(DATASOURCE_PREFIX + sourceName + ".username"))
//                    .password(env.getProperty(DATASOURCE_PREFIX + sourceName + ".password"))
//                    .build());
//        }
//        factory.setPackagesToScan(eruptProp.getScannerPackage());
//        factory.afterPropertiesSet();
//        EntityManager entityManager = factory.getObject().createEntityManager();
//        List list = entityManager.createNativeQuery("select * from t_xinwen").getResultList();
//        for (Object o : list) {
//            System.out.println(o);
//        }
//    }


//    @Test
//    public void prop() throws IllegalAccessException, InstantiationException {
//        for (DbConfig prop : multiDB.getProps()) {
//            LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
//            {
//                JpaProperties jpa = prop.getJpa();
//                HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
//                vendorAdapter.setGenerateDdl(jpa.isGenerateDdl());
//                vendorAdapter.setDatabase(jpa.getDatabase());
//                vendorAdapter.setShowSql(jpa.isShowSql());
//                vendorAdapter.setDatabasePlatform(jpa.getDatabasePlatform());
//                factory.setJpaVendorAdapter(vendorAdapter);
//            }
//            factory.setDataSource(prop.getDatasource().initializeDataSourceBuilder().build());
//            factory.setPackagesToScan(eruptProp.getScannerPackage());
//            factory.afterPropertiesSet();
//            EntityManager entityManager = factory.getObject().createEntityManager();
//        }
//    }

    @Test
    public void joinTest() {
        entityManager.createQuery("select eruptUserTree.name from Demo where choice='xxx'").getResultList();
    }

    @Autowired
    private HttpServletRequest request;

    @Test
    public void ipCity() {
        System.out.println(IpUtil.getCityInfo(IpUtil.getIpAddr(request)));
    }

    @org.junit.Test
    public void testScriptengine() throws ScriptException {
        ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
        ScriptEngine scriptEngine = scriptEngineManager.getEngineByName("JavaScript");
        for (int i = 0; i < 1000; i++) {
            System.out.println(scriptEngine.eval("1+1"));
        }
    }


}
