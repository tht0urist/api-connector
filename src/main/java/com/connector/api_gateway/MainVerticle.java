package com.connector.api_gateway;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.codec.BodyCodec;

public class MainVerticle extends AbstractVerticle {

  private HttpRequest<JsonObject> request;

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
    System.out.println(routingContext.request().getParam("refID"));
    WebClient.create(vertx, options)
      .postAbs("https://apis.telenor.com.pk/oauthtoken/v1/generate?grant_type=client_credentials")
      .putHeader("Authorization", "Basic SVltMEVSS21TdlJCcXZRbFgwMzZwZ2h0QW1USzJBM0Y6WEowQTlCS2NZR0NtQzRRbg==")
      .putHeader("Postman-Token", routingContext.request().getParam("refID"))
      .putHeader("Content-Length", "0")
      .as(BodyCodec.jsonObject())
      .send(httpResponseAsyncResult -> {
        if (httpResponseAsyncResult.succeeded()) {
          HttpResponse<JsonObject> response = httpResponseAsyncResult.result();
          routingContext.response().end(httpResponseAsyncResult.result().body().toString());
        } else {
          System.out.println(httpResponseAsyncResult.cause().getMessage());
          routingContext.response().setStatusCode(500).end("An error occured");
        }
      });
  }

  private void checkInfo(RoutingContext routingContext) {
    WebClient.create(vertx, options)
      .postAbs("https://apis.telenor.com.pk/subscriberQuery/v1/checkinfo/$msisdn")
      .putHeader("Authorization", "Basic SVltMEVSS21TdlJCcXZRbFgwMzZwZ2h0QW1USzJBM0Y6WEowQTlCS2NZR0NtQzRRbg==")
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
