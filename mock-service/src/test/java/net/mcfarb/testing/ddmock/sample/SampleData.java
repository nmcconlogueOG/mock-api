package net.mcfarb.testing.ddmock.sample;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

public class SampleData {
    private String data1;
    private Long data2;
    private Date data3;
    private Timestamp data4;
    private Calendar data5;

    public String getData1() {
        return data1;
    }

    public void setData1(String data1) {
        this.data1 = data1;
    }

    public Long getData2() {
        return data2;
    }

    public void setData2(Long data2) {
        this.data2 = data2;
    }

    public Date getData3() {
        return data3;
    }

    public void setData3(Date data3) {
        this.data3 = data3;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((data1 == null) ? 0 : data1.hashCode());
        result = prime * result + ((data2 == null) ? 0 : data2.hashCode());
        result = prime * result + ((data3 == null) ? 0 : data3.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SampleData other = (SampleData) obj;
        if (data1 == null) {
            if (other.data1 != null)
                return false;
        } else if (!data1.equals(other.data1))
            return false;
        if (data2 == null) {
            if (other.data2 != null)
                return false;
        } else if (!data2.equals(other.data2))
            return false;
        if (data3 == null) {
            if (other.data3 != null)
                return false;
        } else if (!data3.equals(other.data3))
            return false;
        return true;
    }

    public Timestamp getData4() {
        return data4;
    }

    public void setData4(Timestamp data4) {
        this.data4 = data4;
    }

    public Calendar getData5() {
        return data5;
    }

    public void setData5(Calendar data5) {
        this.data5 = data5;
    }

}
