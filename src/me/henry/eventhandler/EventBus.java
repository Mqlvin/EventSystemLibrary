package me.henry.eventhandler;

import me.henry.eventhandler.event.Event;
import me.henry.eventhandler.event.Listen;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventBus {
    private HashMap<Class<? extends Event>, CopyOnWriteArrayList<EventListener>> listeners = new HashMap<>();

    public void register(Object listener) {
        for(Method method : listener.getClass().getDeclaredMethods()) {
            if(!method.isAnnotationPresent(Listen.class)) continue;
            if(method.getParameterCount() == 0) continue;
            Listen subscribeEvent = method.getAnnotation(Listen.class);

            Class<? extends Event> event;
            try {
                event = (Class<? extends Event>) method.getParameters()[0].getType();
            } catch(Exception e) {
                System.out.println("Method " + method.getName() + " was an annotated event, however the method had no event object parameter.");
                continue;
            }
            if(!listeners.containsKey(event)) {
                listeners.put(event, new CopyOnWriteArrayList<>());
            }
            listeners.get(event).add(new EventListener(listener, method, subscribeEvent.priority()));
            listeners.get(event).sort(Comparator.comparingInt(listen -> listen.getPriority().getLevel()));
        }
    }

    public void unregister(Object listener_) {
        listeners.values().forEach(arr -> arr.removeIf(listener -> listener.getListener() == listener_));
    }

    public void call(Event event) {
        if(listeners.containsKey(event.getClass())) {
            listeners.getOrDefault(event.getClass(), new CopyOnWriteArrayList<>()).forEach(method -> {
                try {
                    method.getMethod().invoke(method.getListener(), event);
                } catch(Exception e) {
                    System.out.println("Error while invoking method from class " + method.getListener().getClass().getName());
                    e.printStackTrace();
                }
            });
        }
    }

    public ArrayList<EventListener> getAllListeners() {
        ArrayList<EventListener> eventListenerReturn = new ArrayList<>();
        if(listeners == null || listeners.values().size() == 0) {
            return eventListenerReturn;
        }
        listeners.values().forEach(eventListenerReturn::addAll);
        return eventListenerReturn;
    }
}
