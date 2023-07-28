package teammates.ui.webapi;

import java.io.IOException;
import java.net.URISyntaxException;

import teammates.common.util.Const;
import teammates.ui.output.MessageOutput;

/**
 * Delete a course.
 */
class DeleteCourseAction extends Action {

    @Override
    AuthType getMinAuthLevel() {
        return AuthType.LOGGED_IN;
    }

    @Override
    void checkSpecificAccessControl() throws UnauthorizedAccessException, URISyntaxException, IOException, InterruptedException {
        if (!userInfo.isInstructor) {
            throw new UnauthorizedAccessException("Instructor privilege is required to access this resource.");
        }
        String idOfCourseToDelete = getNonNullRequestParamValue(Const.ParamsNames.COURSE_ID);
        gateKeeper.verifyAccessible(logic.getInstructorForGoogleId(idOfCourseToDelete, userInfo.id),
                logic.getCourse(idOfCourseToDelete),
                Const.InstructorPermissions.CAN_MODIFY_COURSE);
    }

    @Override
    public JsonResult execute() throws URISyntaxException, IOException, InterruptedException {
        String idOfCourseToDelete = getNonNullRequestParamValue(Const.ParamsNames.COURSE_ID);

        logic.deleteCourseCascade(idOfCourseToDelete);

        return new JsonResult(new MessageOutput("OK"));
    }
}
