package com.miko.appinstall.annotation.scanner;

import com.miko.appinstall.annotation.RouteController;
import com.miko.appinstall.annotation.RouteMapping;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.Map;

@Slf4j
public class AnnotationRouteScanner {

  private final Router router;
  private final Map<Class<?>, Object> controllerInstances; // Map of controller class to instance

  public AnnotationRouteScanner(Router router, Map<Class<?>, Object> controllerInstances) {
    this.router = router;
    this.controllerInstances = controllerInstances;
  }

  public void scanAndRegisterRoutes(String basePackage) {
    Reflections reflections = new Reflections(basePackage);
    Set<Class<?>> controllers = reflections.getTypesAnnotatedWith(RouteController.class);

    for (Class<?> controller : controllers) {
      RouteController routeController = controller.getAnnotation(RouteController.class);
      String basePath = routeController.path();

      Object controllerInstance = controllerInstances.get(controller);
      if (controllerInstance == null) {
        log.info("No instance found for controller: " + controller.getName());
        continue;
      }

      for (Method method : controller.getDeclaredMethods()) {
        if (method.isAnnotationPresent(RouteMapping.class)) {
          RouteMapping routeMapping = method.getAnnotation(RouteMapping.class);
          String fullPath = basePath + routeMapping.path();
          HttpMethod httpMethod = HttpMethod.valueOf(routeMapping.method().toUpperCase());

          router.route(httpMethod, fullPath).handler(ctx -> {
            try {
              method.invoke(controllerInstance, ctx);
            } catch (InvocationTargetException e) {
              Throwable cause = e.getCause();
              log.error("Error invoking method: " + method.getName(), cause);
              ctx.fail(500, cause); // Fails with the root cause
            } catch (Exception e) {
              log.error("Unexpected error invoking method: " + method.getName(), e);
              ctx.fail(500, e);
            }
          });
        }
      }
    }
  }
}
