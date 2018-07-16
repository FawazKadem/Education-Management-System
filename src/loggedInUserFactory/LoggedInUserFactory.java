package loggedInUserFactory;

import authenticatedUsers.LoggedInAdmin;
import authenticatedUsers.LoggedInAuthenticatedUser;
import authenticatedUsers.LoggedInInstructor;
import authenticatedUsers.LoggedInStudent;
import authenticationServer.AuthenticationToken;

public class LoggedInUserFactory {

	public LoggedInUserFactory(){
		
	}
	
	public LoggedInAuthenticatedUser createAuthenticatedUser(AuthenticationToken authenticationToken){
		switch(authenticationToken.getUserType()){
		case "Admin":
			return createLoggedInAdmin(authenticationToken);
		case "Student":
			return createLoggedInStudent(authenticationToken);
		case "Instructor":
			return createLoggedInInstructor(authenticationToken);
		default:
			return null;
		}
	}

	// only need to set ids - all we need to relate that to a model and track identity
	public LoggedInStudent createLoggedInStudent(AuthenticationToken authenticationToken){
		LoggedInStudent currentUser = new LoggedInStudent();
		currentUser.setAuthenticationToken(authenticationToken);
		currentUser.setID(currentUser.getAuthenticationToken().getTokenID());

		return currentUser;
	}
	
	public LoggedInAdmin createLoggedInAdmin(AuthenticationToken authenticationToken){
		LoggedInAdmin currentUser = new LoggedInAdmin();
		currentUser.setAuthenticationToken(authenticationToken);
		currentUser.setID(currentUser.getAuthenticationToken().getTokenID());
		return currentUser;
	}
	
	public LoggedInInstructor createLoggedInInstructor(AuthenticationToken authenticationToken){
		LoggedInInstructor currentUser = new LoggedInInstructor();
		currentUser.setAuthenticationToken(authenticationToken);
		currentUser.setID(currentUser.getAuthenticationToken().getTokenID());
		return currentUser;
	}
}
