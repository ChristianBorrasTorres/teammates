package teammates.ui.webapi;

import java.io.IOException;
import java.net.URISyntaxException;

import teammates.common.util.Const;

/**
 * Action: deletes an existing account (either student or instructor).
 */
class DeleteAccountAction extends AdminOnlyAction {

    @Override
    public JsonResult execute() throws URISyntaxException, IOException, InterruptedException {
        String googleId = getNonNullRequestParamValue(Const.ParamsNames.INSTRUCTOR_ID);
        logic.deleteAccountCascade(googleId);
        return new JsonResult("Account is successfully deleted.");
    }

}
