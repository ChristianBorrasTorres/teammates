package teammates.logic.core;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jetty.http.HttpParser.HttpHandler;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;

import teammates.common.datatransfer.AttributesDeletionQuery;
import teammates.common.datatransfer.InstructorPrivileges;
import teammates.common.datatransfer.attributes.AccountAttributes;
import teammates.common.datatransfer.attributes.CourseAttributes;
import teammates.common.datatransfer.attributes.InstructorAttributes;
import teammates.common.datatransfer.attributes.StudentAttributes;
import teammates.common.exception.EntityAlreadyExistsException;
import teammates.common.exception.EntityDoesNotExistException;
import teammates.common.exception.InvalidParametersException;
import teammates.common.util.Const;
import teammates.common.util.JsonUtils;
import teammates.common.util.Logger;
import teammates.storage.api.CoursesDb;
import teammates.storage.entity.Course;

/**
 * Handles operations related to courses.
 *
 * @see CourseAttributes
 * @see CoursesDb
 */
public final class CoursesLogic {

    private static final Logger log = Logger.getLogger();

    private static final CoursesLogic instance = new CoursesLogic();

    /* Explanation: This class depends on CoursesDb class but no other *Db classes.
     * That is because reading/writing entities from/to the database is the
     * responsibility of the matching *Logic class.
     * However, this class can talk to other *Logic classes. That is because
     * the logic related to one entity type can involve the logic related to
     * other entity types.
     */

    private final CoursesDb coursesDb = CoursesDb.inst();

    private AccountsLogic accountsLogic;
    private FeedbackSessionsLogic feedbackSessionsLogic;
    private FeedbackQuestionsLogic fqLogic;
    private FeedbackResponsesLogic frLogic;
    private FeedbackResponseCommentsLogic frcLogic;
    private InstructorsLogic instructorsLogic;
    private StudentsLogic studentsLogic;
    private DeadlineExtensionsLogic deadlineExtensionsLogic;

    private CoursesLogic() {
        // prevent initialization
    }

    public static CoursesLogic inst() {
        return instance;
    }

    void initLogicDependencies() {
        accountsLogic = AccountsLogic.inst();
        feedbackSessionsLogic = FeedbackSessionsLogic.inst();
        fqLogic = FeedbackQuestionsLogic.inst();
        frLogic = FeedbackResponsesLogic.inst();
        frcLogic = FeedbackResponseCommentsLogic.inst();
        instructorsLogic = InstructorsLogic.inst();
        studentsLogic = StudentsLogic.inst();
        deadlineExtensionsLogic = DeadlineExtensionsLogic.inst();
    }

    /**
     * Gets the institute associated with the course.
     * @throws URISyntaxException
     * @throws InterruptedException
     * @throws IOException
     */
    public String getCourseInstitute(String courseId) throws URISyntaxException, IOException, InterruptedException {
        CourseAttributes cd = getCourse(courseId);
        assert cd != null : "Trying to getCourseInstitute for inexistent course with id " + courseId;
        return cd.getInstitute();
    }

    /**
     * Creates a course.
     *
     * @return the created course
     * @throws InvalidParametersException if the course is not valid
     * @throws EntityAlreadyExistsException if the course already exists in the database.
     * @throws InterruptedException
     * @throws IOException
     */
    CourseAttributes createCourse(CourseAttributes courseToCreate)
            throws InvalidParametersException, EntityAlreadyExistsException, URISyntaxException, IOException, InterruptedException {
    //////////// Reemplazar este método por un http-request tipo post /////////////////////////

    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    // Convertir courseToCreate a JSON
    String requestBody = objectMapper.writeValueAsString(courseToCreate);
    // Enviar la solicitud HTTP POST
    HttpRequest postRequest = HttpRequest.newBuilder()
            .uri(new URI("http://localhost:5000/Courses"))
            .header("Content-Type", "application/json")
            .POST(BodyPublishers.ofString(requestBody))
            .build();
    HttpClient httpClient = HttpClient.newHttpClient();
    HttpResponse<String> response = httpClient.send(postRequest, BodyHandlers.ofString());
    // Parsear la respuesta JSON y crear un nuevo objeto CourseAttributes
    JSONObject newCourse = new JSONObject(response.body().toString());

    String idCourse = newCourse.getString("id");
    String nameCourse = newCourse.getString("name");
    String timeZoneCourse = newCourse.getString("timeZone");
    String instituteCourse = newCourse.getString("institute");

    CourseAttributes courseAttributes = CourseAttributes.builder(idCourse)
                        .withName(nameCourse)
                        .withTimezone(timeZoneCourse)
                        .withInstitute(instituteCourse)
                        .build();
    // Retornar el objeto CourseAttributes recién creado
    return courseAttributes;
    }

    /**
     * Creates a course and an associated instructor for the course.
     *
     * <br/>Preconditions: <br/>
     * * {@code instructorGoogleId} already has an account and instructor privileges.
     */
    public void createCourseAndInstructor(String instructorGoogleId, CourseAttributes courseToCreate)
            throws InvalidParametersException, EntityAlreadyExistsException, URISyntaxException, IOException, InterruptedException {

        AccountAttributes courseCreator = accountsLogic.getAccount(instructorGoogleId);
        assert courseCreator != null : "Trying to create a course for a non-existent instructor :" + instructorGoogleId;

        CourseAttributes createdCourse = createCourse(courseToCreate);

        // Create the initial instructor for the course
        InstructorPrivileges privileges = new InstructorPrivileges(
                Const.InstructorPermissionRoleNames.INSTRUCTOR_PERMISSION_ROLE_COOWNER);
        InstructorAttributes instructor = InstructorAttributes
                .builder(createdCourse.getId(), courseCreator.getEmail())
                .withName(courseCreator.getName())
                .withGoogleId(instructorGoogleId)
                .withPrivileges(privileges)
                .build();

        try {
            instructorsLogic.createInstructor(instructor);
        } catch (EntityAlreadyExistsException | InvalidParametersException e) {
            // roll back the transaction
        ///////////////////Reemplazar este método por un http-request tipo delete /////////////
            coursesDb.deleteCourse(createdCourse.getId());
        ///////////////////////////////////////////////////////////////////////////////////////
            String errorMessage = "Unexpected exception while trying to create instructor for a new course "
                                  + System.lineSeparator() + instructor.toString();
            assert false : errorMessage;
        }
    }

    /**
     * Gets the course with the specified ID.
     * @throws URISyntaxException
     * @throws InterruptedException
     * @throws IOException
     */
    public CourseAttributes getCourse(String courseId) throws URISyntaxException, IOException, InterruptedException {

    /////////////////Reemplazar esta linea con und http-Request tipo get  al microservicio///////////////////
        HttpRequest get_request = HttpRequest.newBuilder()
            .uri(new URI("http://localhost:5000/Course/"+courseId))
            .build(); 

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpResponse<String> response = httpClient.send(get_request, BodyHandlers.ofString());

        JSONObject course = new JSONObject(response.body().toString());

        String idCourse = course.getString("id");
        String nameCourse = course.getString("name");
        String timeZoneCourse = course.getString("timeZone");
        String instituteCourse = course.getString("institute");
        Instant createdAtCourse = Instant.parse(course.getString("createdAt"));
        String deletedAt = course.getString("deletedAt");
        
        Instant deletedAtCourse;
        if (deletedAt == null){
            deletedAtCourse = null;
        }
        else {
            deletedAtCourse = Instant.parse(course.getString("deletedAt"));
        }

        CourseAttributes courseAttributes = CourseAttributes.builder(idCourse)
                        .withName(nameCourse)
                        .withTimezone(timeZoneCourse)
                        .withInstitute(instituteCourse)
                        .build();
        
        courseAttributes.setDeletedAt(deletedAtCourse);
        courseAttributes.setCreatedAt(createdAtCourse);
        
        return courseAttributes;
    /////////////////El http-request debe retornar un objeto tipo CourseAttributes //////////////////////////
    }

    /**
     * Returns true if the course with ID courseId is present.
     */
    public boolean isCoursePresent(String courseId) {
        return coursesDb.getCourse(courseId) != null;
    }

    /**
     * Used to trigger an {@link EntityDoesNotExistException} if the course is not present.
     */
    void verifyCourseIsPresent(String courseId) throws EntityDoesNotExistException {
        if (!isCoursePresent(courseId)) {
            throw new EntityDoesNotExistException("Course does not exist: " + courseId);
        }
    }

    /**
     * Returns a list of section names for the course with valid ID courseId.
     *
     * @param courseId Course ID of the course
     */
    public List<String> getSectionsNameForCourse(String courseId) throws EntityDoesNotExistException {
        verifyCourseIsPresent(courseId);

        List<StudentAttributes> studentDataList = studentsLogic.getStudentsForCourse(courseId);

        Set<String> sectionNameSet = new HashSet<>();
        for (StudentAttributes sd : studentDataList) {
            if (!Const.DEFAULT_SECTION.equals(sd.getSection())) {
                sectionNameSet.add(sd.getSection());
            }
        }

        List<String> sectionNameList = new ArrayList<>(sectionNameSet);
        sectionNameList.sort(null);

        return sectionNameList;
    }

    /**
     * Returns team names for a particular courseId.
     *
     * <p>Note: This method does not returns any Loner information presently.
     * Loner information must be returned as we decide to support loners in future.
     * @throws URISyntaxException
     * @throws InterruptedException
     * @throws IOException
     */
    public List<String> getTeamsForCourse(String courseId) throws EntityDoesNotExistException, URISyntaxException, IOException, InterruptedException {

        if (getCourse(courseId) == null) {
            throw new EntityDoesNotExistException("The course " + courseId + " does not exist");
        }

        return studentsLogic.getStudentsForCourse(courseId)
                .stream()
                .map(StudentAttributes::getTeam)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Returns team names for a particular section of a course.
     *
     * <p>Note: This method does not returns any Loner information presently.
     * Loner information must be returned as we decide to support loners in future.
     * @throws URISyntaxException
     * @throws InterruptedException
     * @throws IOException
     */
    public List<String> getTeamsForSection(String sectionName, String courseId) throws EntityDoesNotExistException, URISyntaxException, IOException, InterruptedException {

        if (getCourse(courseId) == null) {
            throw new EntityDoesNotExistException("The course " + courseId + " does not exist");
        }

        return studentsLogic.getStudentsForCourse(courseId)
                .stream()
                .filter(studentAttributes -> studentAttributes.getSection().equals(sectionName))
                .map(StudentAttributes::getTeam)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Returns a list of {@link CourseAttributes} for all courses a given student is enrolled in.
     *
     * @param googleId The Google ID of the student
     */
    public List<CourseAttributes> getCoursesForStudentAccount(String googleId) {
        List<StudentAttributes> studentDataList = studentsLogic.getStudentsForGoogleId(googleId);

        List<String> courseIds = studentDataList.stream()
                .filter(student -> {
                    try {
                        try {
                            return !getCourse(student.getCourse()).isCourseDeleted();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    } catch (URISyntaxException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    return false;
                })
                .map(StudentAttributes::getCourse)
                .collect(Collectors.toList());
    ////////////////// Reemplazar este método (coursesDb) por un http-request tipo get ///////////////////////////////
        return coursesDb.getCourses(courseIds);
    ////////////////// El http-request debe traer un lista de objetos tipo CourseAttributes //////////////
    }

    /**
     * Returns a list of {@link CourseAttributes} for all courses for a given list of instructors
     * except for courses in Recycle Bin.
     */
    public List<CourseAttributes> getCoursesForInstructor(List<InstructorAttributes> instructorList) {
        assert instructorList != null;

        List<String> courseIdList = instructorList.stream()
        /////////////////// Quitar coursesDb ///////////////////////////////////////////////////////////
                .filter(instructor -> !coursesDb.getCourse(instructor.getCourseId()).isCourseDeleted())
        ////////////////////////////////////////////////////////////////////////////////////////////////
                .map(InstructorAttributes::getCourseId)
                .collect(Collectors.toList());

        /////////////////////// Reemplazar el método coursesDB por el mismo método que trae la list de cursos ///////
        List<CourseAttributes> courseList = coursesDb.getCourses(courseIdList);
        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        // Check that all courseIds queried returned a course.
        if (courseIdList.size() > courseList.size()) {
            for (CourseAttributes ca : courseList) {
                courseIdList.remove(ca.getId());
            }
            log.severe("Course(s) was deleted but the instructor still exists: " + System.lineSeparator()
                    + courseIdList.toString());
        }

        return courseList;
    }

    /**
     * Returns a list of {@link CourseAttributes} for soft-deleted courses for a given list of instructors.
     */
    public List<CourseAttributes> getSoftDeletedCoursesForInstructors(List<InstructorAttributes> instructorList) {
        assert instructorList != null;

        List<String> softDeletedCourseIdList = instructorList.stream()
    //////////////////////////////// Quitar coursesDb /////////////////////////////////////////////////////
                .filter(instructor -> coursesDb.getCourse(instructor.getCourseId()).isCourseDeleted())
    ///////////////////////////////////////////////////////////////////////////////////////////////////////
                .map(InstructorAttributes::getCourseId)
                .collect(Collectors.toList());

    //////////////////////////////// Reemplazar el método coursesDB por el mismo método que trae la list de cursos //////////////
        List<CourseAttributes> softDeletedCourseList = coursesDb.getCourses(softDeletedCourseIdList);
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        if (softDeletedCourseIdList.size() > softDeletedCourseList.size()) {
            for (CourseAttributes ca : softDeletedCourseList) {
                softDeletedCourseIdList.remove(ca.getId());
            }
            log.severe("Course(s) was deleted but the instructor still exists: " + System.lineSeparator()
                    + softDeletedCourseIdList.toString());
        }

        return softDeletedCourseList;
    }

    /**
     * Updates a course by {@link CourseAttributes.UpdateOptions}.
     *
     * <p>If the {@code timezone} of the course is changed, cascade the change to its corresponding feedback sessions.
     *
     * @return updated course
     * @throws InvalidParametersException if attributes to update are not valid
     * @throws EntityDoesNotExistException if the course cannot be found
     */
    public CourseAttributes updateCourseCascade(CourseAttributes.UpdateOptions updateOptions)
            throws InvalidParametersException, EntityDoesNotExistException {
    //////////////////////////////// Quitar coursesDb ////////////////////////////////////////////////////
        CourseAttributes oldCourse = coursesDb.getCourse(updateOptions.getCourseId());
    //////////////////////////////////////////////////////////////////////////////////////////////////////

    /////////////////////////////////// Reemplazar este método por un http-request tipo put //////////////
        CourseAttributes updatedCourse = coursesDb.updateCourse(updateOptions);
    /////////////////////////////////// Debe retornar un objeto tipo CourseAttributes ////////////////////

        if (!updatedCourse.getTimeZone().equals(oldCourse.getTimeZone())) {
            feedbackSessionsLogic
                    .updateFeedbackSessionsTimeZoneForCourse(updatedCourse.getId(), updatedCourse.getTimeZone());
        }

        return updatedCourse;
    }

    /**
     * Deletes a course cascade its students, instructors, sessions, responses, deadline extensions and comments.
     *
     * <p>Fails silently if no such course.
     * @throws URISyntaxException
     * @throws InterruptedException
     * @throws IOException
     */
    public void deleteCourseCascade(String courseId) throws URISyntaxException, IOException, InterruptedException {
        if (getCourse(courseId) == null) {
            return;
        }

        AttributesDeletionQuery query = AttributesDeletionQuery.builder()
                .withCourseId(courseId)
                .build();
        frcLogic.deleteFeedbackResponseComments(query);
        frLogic.deleteFeedbackResponses(query);
        fqLogic.deleteFeedbackQuestions(query);
        feedbackSessionsLogic.deleteFeedbackSessions(query);
        studentsLogic.deleteStudents(query);
        instructorsLogic.deleteInstructors(query);
        deadlineExtensionsLogic.deleteDeadlineExtensions(query);

        ////////////////////// Reemplazar este método por un http-request tipo delete /////////////////////
        coursesDb.deleteCourse(courseId);
        ///////////////////////////////////////////////////////////////////////////////////////////////////
    }

    /**
     * Moves a course to Recycle Bin by its given corresponding ID.
     * @return the time when the course is moved to the recycle bin
     */
    public Instant moveCourseToRecycleBin(String courseId) throws EntityDoesNotExistException {
    ////////////////// Reemplazar este método por un método put que cambio el atributo deletedAt ////
        return coursesDb.softDeleteCourse(courseId);
    /////////////////////////////////////////////////////////////////////////////////////////////////
    }

    /**
     * Restores a course from Recycle Bin by its given corresponding ID.
     */
    public void restoreCourseFromRecycleBin(String courseId) throws EntityDoesNotExistException {
    ////////////////// Reemplazar este método por un método put que cambio el atributo deletedAt ////
        coursesDb.restoreDeletedCourse(courseId);
    /////////////////////////////////////////////////////////////////////////////////////////////////
    }

    /**
     * Gets the number of courses created within a specified time range.
     */
    int getNumCoursesByTimeRange(Instant startTime, Instant endTime) {
    /////////////////////////// Reemplazar este método por un http-request tipo get /////////////////
        return coursesDb.getNumCoursesByTimeRange(startTime, endTime);
    /////////////////////////// Debe retornar un int con el número de cursos creados dentro de startTime y endTime //////////
    }

}