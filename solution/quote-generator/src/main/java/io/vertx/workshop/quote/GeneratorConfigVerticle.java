package io.vertx.workshop.quote;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.workshop.common.MicroServiceVerticle;

/**
 * a verticle generating "fake" quotes based on the configuration.
 */
public class GeneratorConfigVerticle extends MicroServiceVerticle {

  private static final Logger logger = LoggerFactory.getLogger(GeneratorConfigVerticle.class);

  /**
   * The address on which the data are sent.
   */
  public static final String ADDRESS = "market";

  /**
   * This method is called when the verticle is deployed.
   */
  @Override
  public void start() {
    super.start();

    // Read the configuration, and deploy a MarketDataVerticle for each company listed in the configuration.
    JsonArray quotes = config().getJsonArray("companies");
    for (Object q : quotes) {
      JsonObject company = (JsonObject) q;
      // Deploy the verticle with a configuration.
      vertx.deployVerticle(MarketDataVerticle.class.getName(), new DeploymentOptions().setConfig(company));
    }

    // Deploy another verticle without configuration.
    vertx.deployVerticle(RestQuoteAPIVerticle.class.getName());

    // Publish the services in the discovery infrastructure.
    publishMessageSource("market-data", ADDRESS, rec -> {
      if (!rec.succeeded()) {
        rec.cause().printStackTrace();
      }
      logger.info("Market-Data service published : " + rec.succeeded());
    });
  }
}
