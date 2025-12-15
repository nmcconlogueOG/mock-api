package net.mcfarb.testing.ddmock.sample;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SampleService {

    public static String staticGetString() {
        return "unmocked";
    }

    public SampleData getSomeData(SampleArgClass arg1, String arg2, Long arg3) {
        SampleData returnData = new SampleData();
        returnData.setData1("data1");
        returnData.setData2(arg3);
        return returnData;
    }

    public String getSomeOtherDataNoArgs() {
        return "this is from getSomeOtherDataNoArgs";
    }

    public List<Long> getAListOfLongs(List<Long> originalList) {
        List<Long> longList = new ArrayList<>(Arrays.asList(1L, 2L, 3L, 5L, 8L, 13L));
        longList.addAll(originalList);
        return longList;
    }

    public List<SampleData> getAListOfObjects(Integer size) {
        List<SampleData> sampleList = new ArrayList<>();
        for (Integer item = 0; item < size; item++) {
            SampleData listItem = new SampleData();
            listItem.setData1("data" + item);
            listItem.setData2((item.longValue()));
            sampleList.add(listItem);
        }
        return sampleList;
    }

    public Map<Long, SampleData> getMapOfObjects(Integer size) {
        throw new java.lang.UnsupportedOperationException();
    }
}