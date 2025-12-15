package net.mcfarb.testing.ddmock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

import net.mcfarb.testing.TestParent;
import net.mcfarb.testing.ddmock.model.MockGeneratorInfo;
import net.mcfarb.testing.ddmock.sample.SampleArgClass;
import net.mcfarb.testing.ddmock.sample.SampleData;
import net.mcfarb.testing.ddmock.sample.SampleService;
import net.mcfarb.testing.ddmock.service.JsonProcessor;
import net.mcfarb.testing.ddmock.service.MockProviderImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class MockBuilderTest implements TestParent {

    private ObjectMapper objectMapper = new ObjectMapper();
    private JsonProcessor jsonProcessor = new JsonProcessor();

    private final DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy, HH:mm:ss");

    private SampleData mockSampleReturn = new SampleData();
    private String mockSampleReturn2 = "This is from the mock";

    MockProviderImpl<MockBuilderTest, Object> mockProvider = new MockProviderImpl<>();

    @BeforeEach
    public void setup() throws ParseException {
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).setDateFormat(dateFormat);

        jsonProcessor.setObjectMapper(this.objectMapper);
        mockProvider.setJsonProcessor(jsonProcessor);
        // mockGeneratorService.setMockDataStore(new MockDataStore());

        mockSampleReturn.setData1("Mock Data");
        mockSampleReturn.setData2(55L);
        mockSampleReturn.setData3(dateFormat.parse("7/27/2020, 05:03:17"));
    }

    @Test
    public void testMockBuilder() throws Exception {

        MockGeneratorInfo mockInfo = jsonProcessor
                .buildMockInfoObjectFromJson("mockdata/" + this.getClass().getSimpleName());

        mockProvider.initialize(mockInfo);

        SampleService sampleService = (SampleService) mockProvider.getBean("sampleService");

        SampleData mockedReturn1 = sampleService.getSomeData(new SampleArgClass(), "dummyArgument", 2L);
        String mockedReturn2 = sampleService.getSomeOtherDataNoArgs();
        List<Long> argument = new ArrayList<Long>(Arrays.asList(1L, 2L));
        List<Long> listOfLongs = sampleService.getAListOfLongs(argument);

        assertEquals(mockSampleReturn.getData1(), mockedReturn1.getData1());
        assertEquals(mockSampleReturn.getData2(), mockedReturn1.getData2());
        assertEquals(mockSampleReturn.getData3(), mockedReturn1.getData3());

        assertEquals(mockSampleReturn2, mockedReturn2);
        assertTrue(listOfLongs.size() > 0, "Array Not Empty");

        List<SampleData> sampleDataList = sampleService.getAListOfObjects(300);
        assertEquals(7, sampleDataList.size(), "Size of list match");
        assertEquals("data3", sampleDataList.get(3).getData1(), "random data match");

        Map<Long, SampleData> sampleDataMap = sampleService.getMapOfObjects(300);
        assertEquals(5, sampleDataMap.size(), "Size of the map matches");
        assertEquals(sampleDataMap.get(101L).getData1(), "mapData1");
        assertEquals(sampleDataMap.get(101L).getData2(), 1L);
        assertEquals(sampleDataMap.get(103L).getData1(), "mapData3");
        assertEquals(sampleDataMap.get(103L).getData2(), 3L);
        assertEquals(sampleDataMap.get(105L).getData1(), "mapData5");
        assertEquals(sampleDataMap.get(105L).getData2(), 5L);
    }

    @Test
    public void testBuildMockInfoObjectFromJson() throws Exception {
        MockGeneratorInfo mockGeneratorInfo = jsonProcessor
                .buildMockInfoObjectFromJson("mockdata/" + this.getClass().getSimpleName());
        assertTrue(mockGeneratorInfo.getMockObjects().size() > 0);
        assertTrue(mockGeneratorInfo.getMockServices().size() > 0);
    }

}