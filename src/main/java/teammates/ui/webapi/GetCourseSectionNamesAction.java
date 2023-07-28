package teammates.ui.webapi;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import teammates.common.datatransfer.attributes.CourseAttributes;
import teammates.common.datatransfer.attributes.InstructorAttributes;
import teammates.common.exception.EntityDoesNotExistException;
import teammates.common.util.Const;
import teammates.ui.output.CourseSectionNamesData;

/**
 * Gets the section names of a course.
 */
class GetCourseSectionNamesAction extends Action {

    @Override
    AuthType getMinAuthLevel() {
        return AuthType.LOGGED_IN;
    }

    @Override
    void checkSpecificAccessControl() throws UnauthorizedAccessException, URISyntaxException, IOException, InterruptedException {
        String courseId = getNonNullRequestParamValue(Const.ParamsNames.COURSE_ID);
        CourseAttributes course = logic.getCourse(courseId);
        InstructorAttributes instructor = logic.getInstructorForGoogleId(courseId, userInfo.id);
        gateKeeper.verifyAccessible(instructor, course);
    }

    @Override
    public JsonResult execute() throws URISyntaxException, IOException, InterruptedException {
        String courseId = getNonNullRequestParamValue(Const.ParamsNames.COURSE_ID);
        try {
            List<String> sectionNames = logic.getSectionNamesForCourse(courseId);
            return new JsonResult(new CourseSectionNamesData(sectionNames));
        } catch (EntityDoesNotExistException e) {
            throw new EntityNotFoundException(e);
        }
    }

}
