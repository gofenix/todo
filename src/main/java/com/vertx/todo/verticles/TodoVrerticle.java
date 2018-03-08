package com.vertx.todo.verticles;

import com.vertx.todo.Constants;
import com.vertx.todo.entity.Todo;
import com.vertx.todo.service.TodoService;
import com.vertx.todo.service.impl.RedisTodoService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.redis.RedisOptions;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * todo
 *
 * @author zhuzhenfeng
 * @date 2018/3/8
 */
public class TodoVrerticle extends AbstractVerticle {
    private static Logger LOGGER = LoggerFactory.getLogger(TodoVrerticle.class);

    private static final String HOST = "0.0.0.0";
    private static final int PORT = 8082;

    private TodoService service;

    private void initData() {
        final String serviceType = config().getString("service.type", "redis");
        LOGGER.info("service type is: " + serviceType);
        switch (serviceType) {
            case "jdbc":
                break;
            case "redis":
            default:
                RedisOptions config = new RedisOptions()
                        .setHost(config().getString("redis.host", "127.0.0.1"))
                        .setPort(config().getInteger("redis.port", 6379));
                service = new RedisTodoService(config);
        }

        service.initData().setHandler(res -> {
            if (res.failed()) {
                LOGGER.error("Persistence service is not running");
            }
        });

    }

    @Override
    public void start(Future<Void> future) throws Exception {
        Router router = Router.router(vertx);
        // CORS support
        Set<String> allowHeaders = new HashSet<>();
        allowHeaders.add("x-requested-with");
        allowHeaders.add("Access-Control-Allow-Origin");
        allowHeaders.add("origin");
        allowHeaders.add("Content-Type");
        allowHeaders.add("accept");
        Set<HttpMethod> allowMethods = new HashSet<>();
        allowMethods.add(HttpMethod.GET);
        allowMethods.add(HttpMethod.POST);
        allowMethods.add(HttpMethod.DELETE);
        allowMethods.add(HttpMethod.PATCH);

        router.route().handler(BodyHandler.create());
        router.route().handler(CorsHandler.create("*")
                .allowedHeaders(allowHeaders)
                .allowedMethods(allowMethods));

        // routes
        router.get(Constants.API_GET).handler(this::handleGetTodo);
        router.get(Constants.API_LIST_ALL).handler(this::handleGetAll);
        router.post(Constants.API_CREATE).handler(this::handleCreateTodo);
        router.patch(Constants.API_UPDATE).handler(this::handleUpdateTodo);
        router.delete(Constants.API_DELETE).handler(this::handleDeleteOne);
        router.delete(Constants.API_DELETE_ALL).handler(this::handleDeleteAll);

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(PORT, HOST, result -> {
                    if (result.succeeded()) {
                        future.complete();
                    } else {
                        future.fail(result.cause());
                    }

                });

        initData();
    }


    private <T> Handler<AsyncResult<T>> resultHandler(RoutingContext context, Consumer<T> consumer) {
        return res -> {
            if (res.succeeded()) {
                consumer.accept(res.result());
            } else {
                serviceUnavailable(context);
            }
        };
    }


    private void handleCreateTodo(RoutingContext context) {
        try {
            final Todo todo = wrapObject(new Todo(context.getBodyAsString()), context);
            final String encoded = Json.encodePrettily(todo);

            service.insert(todo).setHandler(resultHandler(context, res -> {
                if (res) {
                    context.response()
                            .setStatusCode(201)
                            .putHeader("content-type", "application/json")
                            .end(encoded);
                } else {
                    serviceUnavailable(context);
                }
            }));
        } catch (DecodeException e) {
            sendError(400, context.response());
        }
    }

    private void handleGetTodo(RoutingContext context) {
        String todoId = context.request().getParam("todoId");
        if (todoId == null) {
            sendError(400, context.response());
            return;
        }


    }

    private void handleGetAll(RoutingContext context) {
        // TODO
    }

    private void handleUpdateTodo(RoutingContext context) {
        // TODO
    }

    private void handleDeleteOne(RoutingContext context) {
        // TODO
    }

    private void handleDeleteAll(RoutingContext context) {
        // TODO
    }

    private void sendError(int statusCode, HttpServerResponse response) {
        response.setStatusCode(statusCode).end();
    }

    private void badRequest(RoutingContext context) {
        context.response().setStatusCode(400).end();
    }

    private void notFound(RoutingContext context) {
        context.response().setStatusCode(404).end();
    }

    private void serviceUnavailable(RoutingContext context) {
        context.response().setStatusCode(503).end();
    }

    private Todo wrapObject(Todo todo, RoutingContext context) {
        int id = todo.getId();
        if (id > Todo.getIncId()) {
            Todo.setIncIdWith(id);
        } else if (id == 0) {
            todo.setIncId();
        }
        todo.setUrl(context.request().absoluteURI() + "/" + todo.getId());
        return todo;
    }

}
