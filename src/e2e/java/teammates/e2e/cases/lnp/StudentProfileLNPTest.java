package teammates.e2e.cases.lnp;

import java.io.IOException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import teammates.common.datatransfer.DataBundle;
import teammates.common.datatransfer.InstructorPrivileges;
import teammates.common.datatransfer.attributes.AccountAttributes;
import teammates.common.datatransfer.attributes.CourseAttributes;
import teammates.common.datatransfer.attributes.InstructorAttributes;
import teammates.common.datatransfer.attributes.StudentAttributes;
import teammates.common.datatransfer.attributes.StudentProfileAttributes;
import teammates.common.util.Const;
import teammates.e2e.util.JMeterElements;
import teammates.e2e.util.LNPTestData;

/**
 * L&P Test Case for student profile API endpoint.
 */
public final class StudentProfileLNPTest extends BaseLNPTestCase {

    private static final int NUMBER_OF_USER_ACCOUNTS = 500;
    private static final int RAMP_UP_PERIOD = 2;

    private static final String STUDENT_NAME = "LnPStudent";
    private static final String STUDENT_EMAIL = "personalEmail";

    @Override
    protected LNPTestData getTestData() {
        return new LNPTestData() {
            @Override
            protected Map<String, AccountAttributes> generateAccounts() {
                Map<String, AccountAttributes> accounts = new LinkedHashMap<>();

                for (int i = 0; i < NUMBER_OF_USER_ACCOUNTS; i++) {
                    accounts.put(STUDENT_NAME + i, AccountAttributes.builder(STUDENT_NAME + i + ".tmms")
                            .withEmail(STUDENT_EMAIL + i + "@gmail.tmt")
                            .withName(STUDENT_NAME + i)
                            .withIsInstructor(false)
                            .withInstitute("TEAMMATES Test Institute 1")
                            .build()
                    );
                }

                return accounts;
            }

            @Override
            protected Map<String, CourseAttributes> generateCourses() {
                Map<String, CourseAttributes> courses = new LinkedHashMap<>();

                courses.put("course", CourseAttributes.builder("TestData.CS101")
                        .withName("Intro To Programming")
                        .withTimezone(ZoneId.of("UTC"))
                        .build()
                );

                return courses;
            }

            @Override
            protected Map<String, InstructorAttributes> generateInstructors() {
                Map<String, InstructorAttributes> instructors = new LinkedHashMap<>();

                instructors.put("teammates.test.instructor",
                        InstructorAttributes.builder("TestData.CS101", "tmms.test@gmail.tmt")
                                .withGoogleId("TestData.instructor")
                                .withName("Teammates Test")
                                .withRole("Co-owner")
                                .withIsDisplayedToStudents(true)
                                .withDisplayedName("Co-owner")
                                .withPrivileges(new InstructorPrivileges(
                                        Const.InstructorPermissionRoleNames.INSTRUCTOR_PERMISSION_ROLE_COOWNER))
                                .build()
                );

                return instructors;
            }

            @Override
            protected Map<String, StudentAttributes> generateStudents() {
                Map<String, StudentAttributes> students = new LinkedHashMap<>();

                for (int i = 0; i < NUMBER_OF_USER_ACCOUNTS; i++) {
                    students.put(STUDENT_NAME + i,
                            StudentAttributes.builder("TestData.CS101", STUDENT_EMAIL + i + "@gmail.tmt")
                                .withGoogleId(STUDENT_NAME + i + ".tmms")
                                .withName(STUDENT_NAME + i)
                                .withComment("This student's name is " + STUDENT_NAME + i)
                                .withTeamName("Team 1")
                                .withSectionName("None")
                                .build()
                    );
                }

                return students;
            }

            @Override
            protected Map<String, StudentProfileAttributes> generateProfiles() {
                Map<String, StudentProfileAttributes> profiles = new LinkedHashMap<>();

                for (int i = 0; i < NUMBER_OF_USER_ACCOUNTS; i++) {
                    profiles.put(STUDENT_NAME + i, StudentProfileAttributes.builder(STUDENT_NAME + i + ".tmms")
                            .withEmail(STUDENT_EMAIL + i + "@gmail.tmt")
                            .withShortName(String.valueOf(i))
                            .withInstitute("TEAMMATES Test Institute 222")
                            .withMoreInfo("I am " + i)
                            .withPictureKey("")
                            .withGender(StudentProfileAttributes.Gender.MALE)
                            .withNationality("American")
                            .build()
                    );
                }

                return profiles;
            }

            @Override
            public List<String> generateCsvHeaders() {
                List<String> headers = new ArrayList<>();

                headers.add("loginId");
                headers.add("isAdmin");
                headers.add("googleId");

                return headers;
            }

            @Override
            public List<List<String>> generateCsvData() {
                DataBundle dataBundle = loadDataBundle(getJsonDataPath());
                List<List<String>> csvData = new ArrayList<>();

                dataBundle.students.forEach((key, student) -> {
                    List<String> csvRow = new ArrayList<>();

                    csvRow.add(student.googleId); // "googleId" is used for logging in, not "email"
                    csvRow.add("no");
                    csvRow.add(student.googleId);

                    csvData.add(csvRow);
                });

                return csvData;
            }
        };
    }

    private String getTestEndpoint() {
        return Const.ResourceURIs.URI_PREFIX + Const.ResourceURIs.STUDENT_PROFILE + "?googleid=${googleId}";
    }

    @Override
    protected ListedHashTree getLnpTestPlan() {
        ListedHashTree testPlan = new ListedHashTree(JMeterElements.testPlan());
        HashTree threadGroup = testPlan.add(
                JMeterElements.threadGroup(NUMBER_OF_USER_ACCOUNTS, RAMP_UP_PERIOD, 1));

        threadGroup.add(JMeterElements.csvDataSet(getPathToTestDataFile(getCsvConfigPath())));
        threadGroup.add(JMeterElements.cookieManager());
        threadGroup.add(JMeterElements.defaultSampler());
        threadGroup.add(JMeterElements.onceOnlyController())
                .add(JMeterElements.loginSampler());

        // Add HTTP sampler for test endpoint
        threadGroup.add(JMeterElements.httpGetSampler(getTestEndpoint()));

        return testPlan;
    }

    @BeforeClass
    public void classSetup() {
        createTestData();
        persistTestData();
    }

    @Test
    public void runLnpTest() throws IOException {
        runJmeter(false);

        // TODO: Generate summary report from .jtl results file + ReportGenerator, and log to console:
        //  Replace "summariser.out" with Reporter.log("JMeter summary: ", true);
        //  Also, consider displaying the link to the JMeter HTML report.
    }

    @AfterClass
    public void classTearDown() throws IOException {
        deleteTestData();
        deleteDataFiles();
    }

}
