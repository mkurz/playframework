package play.db.jpa;

import play.mvc.Http;

import javax.persistence.EntityManager;
import java.util.ArrayDeque;
import java.util.Deque;

public class JPAEntityManagerContext {

    private static final String CURRENT_ENTITY_MANAGER = "entityManagerContext";

    /**
     * Get the default EntityManager for the current Http.Context.
     *
     * @throws RuntimeException if no EntityManager is bound to the current Http.Context or the current Http.Context.
     * @return the EntityManager
     */
    public static EntityManager em() {
        Deque<EntityManager> ems = emStack();

        if (ems.isEmpty()) {
            throw new RuntimeException("No EntityManager found in the Http.Context. Try to annotate your action method with @play.db.jpa.Transactional");
        }

        return ems.peekFirst();
    }

    /**
     * Get the EntityManager stack.
     */
    @SuppressWarnings("unchecked")
    public static Deque<EntityManager> emStack() {
        Http.Context context = Http.Context.current.get();
        if (context != null) {
            Object emsObject = context.args.get(CURRENT_ENTITY_MANAGER);
            if (emsObject != null) {
                return (Deque<EntityManager>) emsObject;
            } else {
                Deque<EntityManager> ems = new ArrayDeque<>();
                context.args.put(CURRENT_ENTITY_MANAGER, ems);
                return ems;
            }
        } else {
            throw new RuntimeException("No Http.Context is present. If you want to invoke this method outside of a HTTP request, you need to wrap the call with JPA.withTransaction instead.");
        }
    }

    public static void push(EntityManager em) {
        Deque<EntityManager> ems = emStack();
        if (em != null) {
            ems.push(em);
        }
    }

    public static void pop() {
        Deque<EntityManager> ems = emStack();
        if (ems.isEmpty()) {
            throw new IllegalStateException("Tried to remove the EntityManager, but none was set.");
        }
        ems.pop();
    }
}