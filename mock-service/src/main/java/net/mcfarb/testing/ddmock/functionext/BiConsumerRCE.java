package net.mcfarb.testing.ddmock.functionext;

import java.util.function.BiConsumer;

@FunctionalInterface
public interface BiConsumerRCE<T, U> extends BiConsumer<T, U> {

    @Override
    default void accept(T var1, U var2) {
        try {
            acceptThrows(var1, var2);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    void acceptThrows(T var1, U var2) throws Exception;

}
