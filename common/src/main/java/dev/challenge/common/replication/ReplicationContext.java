package dev.challenge.common.replication;
public final class ReplicationContext {
    private static final ThreadLocal<Boolean> FLAG = ThreadLocal.withInitial(() -> false);
    private ReplicationContext(){}
    public static void mark(boolean v){ FLAG.set(v); }
    public static void clear(){ FLAG.remove(); }
}