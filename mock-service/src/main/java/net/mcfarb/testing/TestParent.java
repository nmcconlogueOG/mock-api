package net.mcfarb.testing;

public interface TestParent {

    public default String getSimpleName() {
        return this.getClass().getSimpleName();
    }

}
