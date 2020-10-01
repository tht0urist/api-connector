package com.connector.api_gateway;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.codec.BodyCodec;

import static io.vertx.core.json.JsonObject.mapFrom;

public class MainVerticle extends AbstractVerticle {

    private Logger logger = LoggerFactory.getLogger(MainVerticle.class);

    private WebClientOptions options = new WebClientOptions();

    @Override
    public void start(Promise<Void> fut) throws Exception {

        options.setConnectTimeout(config().getInteger("connect-timeout"));
        options.setLogActivity(true);

        // Create a router object.
        Router router = Router.router(vertx);

        // Bind "/" to our hello message.
        router.route("/").handler(routingContext -> {
            HttpServerResponse response = routingContext.response();
            response
                    .putHeader("content-type", "text/html")
                    .end("<span>Hello from api connector</span>");
        });

        router.get("/auth").handler(this::auth);
        router.get("/checkinfo").handler(this::checkInfo);
        router.get("/charge").handler(this::charge);

        // Create the HTTP server and pass the "accept" method to the request handler.
        vertx
                .createHttpServer()
                .requestHandler(router)
                .listen(
                        config().getInteger("http.port"),
                        result -> {
                            if (result.succeeded()) {
                                fut.complete();
                            } else {
                                fut.fail(result.cause());
                            }
                        }
                );
    }

    /*
    Get Token
     */
    private void auth(RoutingContext routingContext) {
        String refID = routingContext.request().getParam("refID");
        logger.info("Auth request received for refID = {0}", refID);
        WebClient.create(vertx, options)
                .postAbs("https://apis.telenor.com.pk/oauthtoken/v1/generate?grant_type=client_credentials")
                .timeout(config().getInteger("read-timeout"))
                .putHeader("Authorization", "Basic SVltMEVSS21TdlJCcXZRbFgwMzZwZ2h0QW1USzJBM0Y6WEowQTlCS2NZR0NtQzRRbg==")
                .putHeader("Postman-Token", routingContext.request().getParam("refID"))
                .putHeader("Content-Length", "0")
                .as(BodyCodec.jsonObject())
                .send(httpResponseAsyncResult -> {
                    if (httpResponseAsyncResult.succeeded()) {
                        HttpResponse<JsonObject> response = httpResponseAsyncResult.result();
                        logger.info("successfully Authenticated");
                        routingContext.response().end(response.body().toString());
                    } else {
                        logger.error("An error occured while authenticating user with refID {}, caused by {}", refID, httpResponseAsyncResult.cause());
                        routingContext.response().setStatusCode(500).end("An error occured");
                    }
                });
    }

    /*
     * Check Info
     * */
    private void checkInfo(RoutingContext routingContext) {
        String msisdn = routingContext.request().getParam("msisdn");
        String token = routingContext.request().getParam("token");
        String refID = routingContext.request().getParam("refID");
        logger.info("Check info  request received for msisdn = {0}, token = {1} and refID = {2}", msisdn, token, refID);

        WebClient.create(vertx, options)
                .getAbs("https://apis.telenor.com.pk/subscriberQuery/v1/checkinfo/" + msisdn)
                .timeout(config().getInteger("read-timeout"))
                .putHeader("Authorization", "Bearer " + token)
                .putHeader("Postman-Token", refID)
                .putHeader("Content-Length", "0")
                .putHeader("Content-Type", "application/json")
                .as(BodyCodec.jsonObject())
                .send(httpResponseAsyncResult -> {
                    if (httpResponseAsyncResult.succeeded()) {
                        HttpResponse<JsonObject> response = httpResponseAsyncResult.result();
                        routingContext.response().end(response.body().toString());
                    } else {
                        logger.error("An error occured while check info user with msisdn {}, caused by {}", msisdn, httpResponseAsyncResult.cause());
                        routingContext.response().setStatusCode(500).end("An error occured");
                    }
                });
    }

    /*
     * Charge
     * */
    private void charge(RoutingContext routingContext) {
        String msisdn = routingContext.request().getParam("msisdn");
        String token = routingContext.request().getParam("token");
        String refID = routingContext.request().getParam("refID");
        String amount = routingContext.request().getParam("amount");
        ChargeRequest chargeRequest = new ChargeRequest();
        chargeRequest.setChargableAmount(amount);
        chargeRequest.setCorrelationID(refID);
        chargeRequest.setMsisdn(msisdn);
        chargeRequest.setPartnerID("Binjeerenew2");
        chargeRequest.setProductID("Binjeerenew");
        chargeRequest.setRemarks("Binjee");
        chargeRequest.setTransactionID(refID);
        logger.info("Charge  request received for msisdn = {0}, token = {1}, refID = {2}, amount = {3}", msisdn, token, refID, amount);
        WebClient.create(vertx, options)
                .postAbs("https://apis.telenor.com.pk/payment/v1/charge")
                .timeout(config().getInteger("read-timeout"))
                .putHeader("Authorization", "Bearer " + token)
                .putHeader("Postman-Token", "de9497e6-4a03-41c1-b813-de58547e992d")
                .putHeader("Content-Length", "0")
                .putHeader("Content-Type", "application/json")
                .as(BodyCodec.jsonObject())
                .sendJsonObject(mapFrom(chargeRequest), httpResponseAsyncResult -> {
                    if (httpResponseAsyncResult.succeeded()) {
                        HttpResponse<JsonObject> response = httpResponseAsyncResult.result();
                        routingContext.response().end(response.body().toString());
                    } else {
                        logger.error("An error occured while charging transaction with msisdn {}, caused by {}", refID, httpResponseAsyncResult.cause());
                        routingContext.response().setStatusCode(500).end("An error occured");
                    }
                });
    }
}
