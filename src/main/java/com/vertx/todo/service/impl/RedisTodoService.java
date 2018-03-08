package com.vertx.todo.service.impl;

import com.vertx.todo.Constants;
import com.vertx.todo.entity.Todo;
import com.vertx.todo.service.TodoService;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.redis.RedisClient;
import io.vertx.redis.RedisOptions;

import java.util.List;
import java.util.Optional;

/**
 * todo
 *
 * @author zhuzhenfeng
 * @date 2018/3/8
 */
public class RedisTodoService implements TodoService {

    private final Vertx vertx;
    private final RedisOptions config;
    private final RedisClient redis;

    public RedisTodoService(Vertx vertx, RedisOptions config) {
        this.vertx = vertx;
        this.config = config;
        this.redis = RedisClient.create(vertx, config);
    }

    public RedisTodoService(RedisOptions config) {
        this(Vertx.vertx(), config);
    }

    @Override
    public Future<Boolean> initData() {
        return insert(new Todo(Math.abs(new java.util.Random().nextInt()),
                "Something to do...", false, 1, "todo/ex"));
    }

    @Override
    public Future<Boolean> insert(Todo todo) {
        Future<Boolean> result = Future.future();
        final String encoded = Json.encodePrettily(todo);
        redis.hset(Constants.REDIS_TODO_KEY, String.valueOf(todo.getId()),
                encoded, res -> {
                    if (res.succeeded()) {
                        result.complete(true);
                    } else {
                        result.fail(res.cause());
                    }
                });
        return result;
    }

    @Override
    public Future<List<Todo>> getAll() {
        return null;
    }

    @Override
    public Future<Optional<Todo>> getCertain(String todoId) {
        return null;
    }

    @Override
    public Future<Todo> update(String todoId, Todo newTodo) {
        return null;
    }

    @Override
    public Future<Boolean> delete(String todoId) {
        return null;
    }

    @Override
    public Future<Boolean> deleteAll() {
        return null;
    }
}
