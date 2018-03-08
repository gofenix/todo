package com.vertx.todo.service;

import com.vertx.todo.entity.Todo;
import io.vertx.core.Future;

import java.util.List;
import java.util.Optional;

/**
 * todo
 *
 * @author zhuzhenfeng
 * @date 2018/3/8
 */
public interface TodoService {
    Future<Boolean> initData();

    Future<Boolean> insert(Todo todo);

    Future<List<Todo>> getAll();

    Future<Optional<Todo>> getCertain(String todoId);

    Future<Todo> update(String todoId, Todo newTodo);

    Future<Boolean> delete(String todoId);

    Future<Boolean> deleteAll();
}
