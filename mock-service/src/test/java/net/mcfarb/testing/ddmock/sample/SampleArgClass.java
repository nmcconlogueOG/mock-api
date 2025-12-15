package net.mcfarb.testing.ddmock.sample;

import java.util.Date;

public class SampleArgClass {
    private String argData = "argString";
    private Date now = new Date();

    public String getArgData() {
        return argData;
    }

    public void setArgData(String argData) {
        this.argData = argData;
    }

    public Date getNow() {
        return now;
    }

    public void setNow(Date now) {
        this.now = now;
    }

}
