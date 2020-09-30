package com.connector.api_gateway;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.core.logging.Logger;

public class MainVerticle extends AbstractVerticle {

    private Logger logger = LoggerFactory.getLogger(MainVerticle.class);

    private WebClientOptions options = new WebClientOptions();

    @Override
    public void start(Promise<Void> fut) throws Exception {

        options.setConnectTimeout(5000);
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
        router.get("/checkinfo").handler(this::checkInfo);

        // Create the HTTP server and pass the "accept" method to the request handler.
        vertx
                .createHttpServer()
                .requestHandler(router)
                .listen(
                        // Retrieve the port from the configuration,
                        // default to 8080.
                        config().getInteger("http.port", 8080),
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
                .putHeader("Authorization", "Basic SVltMEVSS21TdlJCcXZRbFgwMzZwZ2h0QW1USzJBM0Y6WEowQTlCS2NZR0NtQzRRbg==")
                .putHeader("Postman-Token", routingContext.request().getParam("refID"))
                .putHeader("Content-Length", "0")
                .as(BodyCodec.jsonObject())
                .send(httpResponseAsyncResult -> {
                    if (httpResponseAsyncResult.succeeded()) {
                        HttpResponse<JsonObject> response = httpResponseAsyncResult.result();
                        logger.info("successfully Authenticated");
                        routingContext.response().end(httpResponseAsyncResult.result().body().toString());
                    } else {
                        logger.error("An error occured while authenticating user with refID {}, caused by {}", refID, httpResponseAsyncResult.cause());
                        routingContext.response().setStatusCode(500).end("An error occured");
                    }
                });
    }

    private void checkInfo(RoutingContext routingContext) {
        String msisdn = routingContext.request().getParam("msisdn");
        String token = routingContext.request().getParam("token");
        String refID = routingContext.request().getParam("refID");
        logger.info("Check info  request received for msisdn = {0}, token = {1} and refID = {2}", msisdn, token, refID);

        WebClient.create(vertx, options)
                .getAbs("https://apis.telenor.com.pk/subscriberQuery/v1/checkinfo/" + msisdn)
                .putHeader("Authorization", "Bearer " + token)
                .putHeader("Postman-Token", refID)
                .putHeader("Content-Length", "0")
                .putHeader("Content-Type", "application/json")
                .as(BodyCodec.jsonObject())
                .send(httpResponseAsyncResult -> {
                    if (httpResponseAsyncResult.succeeded()) {
                        HttpResponse<JsonObject> response = httpResponseAsyncResult.result();
                        routingContext.response().end(httpResponseAsyncResult.result().body().toString());
                    } else {
                        routingContext.response().setStatusCode(500).end("An error occured");
                    }
                });
    }
}
