package ru.netology.servlet;

import ru.netology.controller.PostController;
import ru.netology.exception.NotFoundException;
import ru.netology.handler.Handler;
import ru.netology.repository.PostRepository;
import ru.netology.repository.PostRepositoryClass;
import ru.netology.service.PostService;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class MainServlet extends HttpServlet {
    private PostController controller;
    private final Map<String, Map<String, Handler>> handlers = new ConcurrentHashMap<>();
    private static final String PATH = "/api/posts";
    private static final String PATH_WITH_PARAMS = "/api/posts/";

    @Override
    public void init() {
        final PostRepository repository = new PostRepositoryClass();
        final PostService service = new PostService(repository);
        controller = new PostController(service);

//        addHandler("GET", PATH, (path, request, response) -> controller.all(response));
//        addHandler("GET", PATH_WITH_PARAMS, (path, request, response) -> controller.getById(getIdByParsePath(path), response));
//        addHandler("POST", PATH, (path, request, response) -> controller.save(request.getReader(), response));
//        addHandler("DELETE", PATH_WITH_PARAMS, (path, request, response) -> controller.removeById(getIdByParsePath(path), response));
        addHandler("GET", PATH, (path, request, response) -> {
            try {
                controller.all(response);
                response.setStatus(HttpServletResponse.SC_OK);
            } catch (NotFoundException e) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        });
        addHandler("GET", PATH_WITH_PARAMS, (path, request, response) -> {
            try {
                controller.getById(getIdByParsePath(path), response);
                response.setStatus(HttpServletResponse.SC_OK);
            } catch (NotFoundException e) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        });
        addHandler("POST", PATH, (path, request, response) -> {
            try {
                controller.save(request.getReader(), response);
                response.setStatus(HttpServletResponse.SC_OK);
            } catch (NotFoundException e) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        });
        addHandler("PUT", PATH, (path, request, response) -> {
            try {
                controller.save(request.getReader(), response);
                response.setStatus(HttpServletResponse.SC_OK);
            } catch (NotFoundException e) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        });
        addHandler("DELETE", PATH_WITH_PARAMS, (path, request, response) -> {
            try {
                controller.removeById(getIdByParsePath(path), response);
                response.setStatus(HttpServletResponse.SC_OK);
            } catch (NotFoundException e) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        });
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) {

        try {
            final String method = request.getMethod();
            String path = request.getRequestURI();

            String pathHandler = path;
            if (path.startsWith(PATH_WITH_PARAMS) && path.matches(PATH_WITH_PARAMS + "\\d+")) {
                pathHandler = PATH_WITH_PARAMS;
            } else if (path.startsWith(PATH)) {
                pathHandler = PATH;
            }

            Handler handler = handlers.get(method).get(pathHandler);
            handler.handle(path, request, response);

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void addHandler(String method, String path, Handler handler) {
        Map<String, Handler> map = new ConcurrentHashMap<>();
        if (handlers.containsKey(method)) {
            map = handlers.get(method);
        }
        map.put(path, handler);
        handlers.put(method, map);
    }

    private long getIdByParsePath(String path) {
        return Long.parseLong(path.substring(path.lastIndexOf("/") + 1));
    }
}
