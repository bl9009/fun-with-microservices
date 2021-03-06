package fwd.farmer.main;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import fwd.common.kv.ConnectionException;
import fwd.common.kv.RedisStore;
import fwd.farmer.dispatch.MqDispatch;
import fwd.farmer.fulfillment.FarmerFulfillment;
import fwd.farmer.fulfillment.MongoFulfillment;
import fwd.farmer.resources.PotatoesResource;
import fwd.farmer.warehouse.RedisWarehouse;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FarmerApplication extends Application<FarmerConfiguration> {

    private Farmer farmer;

    private Logger log;

    public static void main(String[] args) throws Exception {
        new FarmerApplication().run(args);
    }

    public FarmerApplication() {
        log = LoggerFactory.getLogger(FarmerApplication.class);
    }

    @Override
    public void initialize(Bootstrap<FarmerConfiguration> bootstrap)
    {
        // ;
    }


    @Override
    public void run(FarmerConfiguration configuration, Environment environment)
    {
        RedisStore store;

        try {
            store = new RedisStore("fwd_redis_1");
        }
        catch (ConnectionException e) {
            log.error("could not connect to redis");

            return;
        }

        MongoClientOptions.Builder builder = MongoClientOptions.builder();
        builder.connectTimeout(10000);

        MongoClient mongoClient = new MongoClient("fwd_mongo_1", builder.build());

        RedisWarehouse warehouse = new RedisWarehouse(store);
        FarmerFulfillment fulfillment = new MongoFulfillment(mongoClient);
        MqDispatch dispatch = new MqDispatch();

        farmer = new Farmer(warehouse, fulfillment, dispatch, configuration.getProductionRate());

        final FarmerRunner runner = new FarmerRunner(farmer);

        final PotatoesResource resource = new PotatoesResource(farmer);

        environment.jersey().register(resource);

        environment.lifecycle().manage(runner);
    }
}
