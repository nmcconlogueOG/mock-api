package net.mcfarb.testing.ddmock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
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

import net.bytebuddy.asm.MemberSubstitution.Substitution.Chain.Step;
import net.mcfarb.testing.TestParent;
import net.mcfarb.testing.ddmock.model.MockGeneratorInfo;
import net.mcfarb.testing.ddmock.sample.SampleArgClass;
import net.mcfarb.testing.ddmock.sample.SampleData;
import net.mcfarb.testing.ddmock.sample.SampleService;
import net.mcfarb.testing.ddmock.service.JsonProcessor;
import net.mcfarb.testing.ddmock.service.MockProviderImpl;
import net.mcfarb.testing.ddmock.service.MonoMockProvider;
import net.mcfarb.testing.ddmock.service.SpringBeanMonoProvider;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class MonoMockBuilderTest<S> implements TestParent {

    private ObjectMapper objectMapper = new ObjectMapper();
    private JsonProcessor jsonProcessor = new JsonProcessor();

    private final DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy, HH:mm:ss");

    private SampleData mockSampleReturn = new SampleData();
    private String mockSampleReturn2 = "This is from the mock";

    MonoMockProvider<MonoMockBuilderTest, Object> mockProvider;

    @Autowired
    private SpringBeanMonoProvider<S> springBeanMonoProvider;

    @BeforeEach
    public void setup() throws ParseException, IOException, URISyntaxException {
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).setDateFormat(dateFormat);

        jsonProcessor.setObjectMapper(this.objectMapper);

        mockProvider = new MonoMockProvider<>();
        mockProvider.setJsonProcessor(jsonProcessor);

        mockProvider.initialize("mockdata/MockBuilderTest");
        mockProvider.setSpringBeanMonoProvider(springBeanMonoProvider);

        // mockGeneratorService.setMockDataStore(new MockDataStore());

        mockSampleReturn.setData1("Mock Data");
        mockSampleReturn.setData2(55L);
        mockSampleReturn.setData3(dateFormat.parse("7/27/2020, 05:03:17"));

    }

    @Test
    public void testMockBuilder() throws Exception {

        // setup();
        // MockGeneratorInfo mockInfo = jsonProcessor
        // .buildMockInfoObjectFromJson("mockdata/" + this.getClass().getSimpleName());

        Mono<Object> sampleService = mockProvider.getBean("sampleService");

        StepVerifier
                .create(sampleService)
                .expectNextCount(1)
                .verifyComplete();

        StepVerifier
                .create(sampleService)
                // .expectNextMatches(sampleServiceObj -> sampleServiceObj instanceof
                // SampleService)
                .consumeNextWith(sampleServiceObj -> {
                    SampleService service = (SampleService) sampleServiceObj;
                    SampleData mockedReturn1 = service.getSomeData(new SampleArgClass(), "dummyArgument", 2L);
                    String mockedReturn2 = service.getSomeOtherDataNoArgs();
                    List<Long> argument = new ArrayList<Long>(Arrays.asList(1L, 2L));
                    List<Long> listOfLongs = service.getAListOfLongs(argument);

                    assertEquals(mockSampleReturn.getData1(), mockedReturn1.getData1());
                    assertEquals(mockSampleReturn.getData2(), mockedReturn1.getData2());
                    assertEquals(mockSampleReturn.getData3(), mockedReturn1.getData3());
                })
                .verifyComplete();

        StepVerifier
                .create(sampleService)
                .consumeNextWith(sampleServiceObj -> {
                    SampleService service = (SampleService) sampleServiceObj;
                    SampleData mockedReturn1 = service.getSomeData(new SampleArgClass(),
                            "dummyArgument", 2L);
                    String mockedReturn2 = service.getSomeOtherDataNoArgs();
                    List<Long> argument = new ArrayList<Long>(Arrays.asList(1L, 2L));
                    List<Long> listOfLongs = service.getAListOfLongs(argument);

                    assertEquals(mockSampleReturn.getData1(), mockedReturn1.getData1());
                    assertEquals(mockSampleReturn.getData2(), mockedReturn1.getData2());
                    assertEquals(mockSampleReturn.getData3(), mockedReturn1.getData3());

                    assertEquals(mockSampleReturn2, mockedReturn2);
                    assertTrue(listOfLongs.size() > 0, "Array Not Empty");

                    List<SampleData> sampleDataList = service.getAListOfObjects(300);
                    assertEquals(7, sampleDataList.size(), "Size of list match");
                    assertEquals("data3", sampleDataList.get(3).getData1(), "random data match");

                    Map<Long, SampleData> sampleDataMap = service.getMapOfObjects(300);
                    assertEquals(5, sampleDataMap.size(), "Size of the map matches");
                    assertEquals(sampleDataMap.get(101L).getData1(), "mapData1");
                    assertEquals(sampleDataMap.get(101L).getData2(), 1L);
                    assertEquals(sampleDataMap.get(103L).getData1(), "mapData3");
                    assertEquals(sampleDataMap.get(103L).getData2(), 3L);
                    assertEquals(sampleDataMap.get(105L).getData1(), "mapData5");
                    assertEquals(sampleDataMap.get(105L).getData2(), 5L);
                }).verifyComplete();

    }

    @Test
    public void testBuildMockInfoObjectFromJson() throws Exception {
        MockGeneratorInfo mockGeneratorInfo = jsonProcessor
                .buildMockInfoObjectFromJson("mockdata/MockBuilderTest");
        assertTrue(mockGeneratorInfo.getMockObjects().size() > 0);
        assertTrue(mockGeneratorInfo.getMockServices().size() > 0);
    }

}