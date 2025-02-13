package teammates.ui.webapi;

import java.io.IOException;
import java.net.URISyntaxException;

import teammates.common.exception.EntityDoesNotExistException;
import teammates.common.util.Const;

/**
 * Action: Restores a course from Recycle Bin.
 */
class RestoreCourseAction extends Action {

    @Override
    AuthType getMinAuthLevel() {
        return AuthType.LOGGED_IN;
    }

    @Override
    void checkSpecificAccessControl() throws UnauthorizedAccessException, URISyntaxException, IOException, InterruptedException {
        if (!userInfo.isInstructor) {
            throw new UnauthorizedAccessException("Instructor privilege is required to access this resource.");
        }
        String idOfCourseToRestore = getNonNullRequestParamValue(Const.ParamsNames.COURSE_ID);
        gateKeeper.verifyAccessible(logic.getInstructorForGoogleId(idOfCourseToRestore, userInfo.id),
                logic.getCourse(idOfCourseToRestore),
                Const.InstructorPermissions.CAN_MODIFY_COURSE);
    }

    @Override
    public JsonResult execute() throws URISyntaxException, IOException, InterruptedException {

        String idOfCourseToRestore = getNonNullRequestParamValue(Const.ParamsNames.COURSE_ID);
        String statusMessage;

        try {
            logic.restoreCourseFromRecycleBin(idOfCourseToRestore);

            statusMessage = "The course " + idOfCourseToRestore + " has been restored.";
        } catch (EntityDoesNotExistException e) {
            throw new EntityNotFoundException(e);
        }

        return new JsonResult(statusMessage);
    }
}
