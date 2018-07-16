package systemOperations;

import authenticatedUsers.LoggedInAuthenticatedUser;
import authenticationServer.AuthenticationToken;
import loggedInUserFactory.LoggedInUserFactory;
import offerings.CourseOffering;
import offerings.ICourseOffering;
import offerings.OfferingFactory;
import org.apache.commons.lang3.*;

import registrar.ModelRegister;
import systemUsers.*;

import java.io.*;
import java.util.ArrayList;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import javax.mail.*;


/**
 * Operating class will be initial landing point for users of the system
 * Initializes system and data
 * Logs users in and routes them to their command sets
 */
public class OperatingClass {

    private static LoggedInAuthenticatedUser activeUser; //current authenticated user. i.e. user currently using the system
    private static SystemUserModel activeUserModel; //made based on activeUser. allows them to access their operations and attributes
    private static LoggedInUserFactory loggedInFactory = new LoggedInUserFactory(); //class to create an authenticated user


    //logs users in. validates them based on user database file. places restrictions on systemOn
    private static void login(boolean systemOn){

        String idInput; //username inputted by current unauthenticateed user
        Boolean userLoggedIn = false; //whether or not login has been completed
        String authCode = "12345"; //authentication code for first time users
        String userLine; //line in data base storing a userID and the corresponding password
        String currentPassword; //password currently stored in database
        String enteredPassword; //password entered by user


        System.out.println("Login Menu");                   //Information for user (ease-of-use purposes)
        System.out.println("------------------\n\n");
        System.out.println(SystemState.systemStateAsString());
        System.out.println("\n\n");

        Scanner loginInfo = new Scanner(System.in); //accept user input

        if (!systemOn){ //system is off -> only admin can log in. just warning, not restriction yet
            System.out.println("Only the administrator can login at this time." +
                    "\nIf you are a student or instructor, please contact your system administrator to access EMU.");
        }

        while (!userLoggedIn){ //attempts to log user in

            System.out.println("4-Digit User ID: "); //unique user id is username
            idInput = loginInfo.nextLine();

            if (ModelRegister.getInstance().checkIfUserHasAlreadyBeenCreated(idInput)){ //check if user is in system
                System.out.println("\nUser found.");
                System.out.printf("Hello, %s %s\n", ModelRegister.getInstance().getRegisteredUser(idInput).getName(), ModelRegister.getInstance().getRegisteredUser(idInput).getSurname());

                if((ModelRegister.getInstance().getRegisteredUser(idInput) instanceof AdminModel) || systemOn){ //can only log in if system is on or you are an admin.

                    userLine = findUserLine(idInput); //finds line in database that stores the corresponding user and their password
                    currentPassword = userLine.split(",")[1]; //pw in database


                    if (currentPassword.equals("null")){ //if this is the users first time logging in, their current password is null


                        SendEmail.main("EMU Course Management: Authentication Code", "Authentication Code:  " + authCode);
                        System.out.println("Your password has not been set\nPlease enter 5 digit authorization code that has been emailed to you (12345 for testing purposes):   "); //auth code is 12345 for testing purposes.


                        if (loginInfo.nextLine().equals(authCode)){  //verifies correct user
                            System.out.println("Code correct. Please enter your desired password. Cannot be \"null\": ");
                            enteredPassword = loginInfo.nextLine();

                            while (enteredPassword.equals("null")){
                                System.out.println("Invalid pass. Please enter your desired password. Cannot be \"null\": ");
                                enteredPassword = loginInfo.nextLine();
                            }
                            changePassword(idInput, enteredPassword); //calls method to set password and update userLine in the datafile
                            System.out.println("Please try logging in with your new password.\n");
                        }


                    } else{ //user has logged in before
                        System.out.println("Enter password: ");
                        enteredPassword = loginInfo.nextLine();
                        if (enteredPassword.equals(currentPassword)){
                            userLoggedIn = true;
                            activeUser = authenticateUser(idInput);

                        } else{ //password incorrect
                            System.out.println("Password failed. Please try logging in again.\n"); //restarts while loop and login process
                        }


                    }

                } else { //if system is off and user is not admin
                    System.out.println("ID does not match with Admin ID. If you are an instructor or student, please contact administrator to turn system on.\n"); //restarts while loop and login process
                }




            } else{ //if id isnt in database
                System.out.println("ID does not belong to a registered user. Please try again.\n"); //restarts while loop and login process
            }



        }

    }


    /**
     * authenticates user
     * gives verified user an authentication token and makes a authenticatedUser with a tokenID that is the same as their user ID
     */
    private static LoggedInAuthenticatedUser authenticateUser(String idInput) {
        AuthenticationToken verified = new AuthenticationToken();
        verified.setTokenID(idInput);
        SystemUserModel currentModel = ModelRegister.getInstance().getRegisteredUser(idInput);

        if (currentModel instanceof AdminModel){
            verified.setUserType("Admin");
        } else if (currentModel instanceof InstructorModel) {
            verified.setUserType("Instructor");
        } else{
            verified.setUserType("Student");
        }

        return loggedInFactory.createAuthenticatedUser(verified);

    }


    //changes password of user by editing their information in data file
    //takes user id and new password as parameters
    private static void changePassword(String idInput, String password){

        System.out.println("\nChanging Password\n-------------\n");
        BufferedReader br;
        BufferedWriter bw;
        ArrayList<String> storeLines = new ArrayList<String>();


        try {
            br = new BufferedReader(new FileReader("users.csv"));

            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith(idInput)) {
                    line = line.replace(",null", "," + password);
                }
            storeLines.add(line);

            }
            br.close();

            bw = new BufferedWriter(new FileWriter("users.csv", false));
            for (String userInfo : storeLines){
                bw.write(userInfo);
                bw.write("\n");
            }
            bw.flush();
            bw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        System.out.println("Password saved. \n");


    }


    /**
     * Helper method to changePassword
     * takes in user ID, returns line in data file that stores the user ID and the corresponding password.
     * Doesn't need to check if user is in file, because the method is only called if user is in file
     */
    private static String findUserLine(String idInput) {

        String line = null; //line in users file
        Boolean foundUser = false; //whether user has been found in file


        BufferedReader br = null; //read datafile
        try {
            br = new BufferedReader(new FileReader("users.csv"));
            line = br.readLine();

            if (line.startsWith(idInput)){foundUser = true;}


            while((!foundUser) && ((line = br.readLine()) != null)) {
                    if (line.startsWith(idInput)) {
                        foundUser = true;
                        break;
                    }
            }

            br.close(); //close reader

            return line; //return line: userID,password

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * writes users in register to data file, so that current users in system and current users that can log in are consistent with each other
     * stores users in format: userID,password
     * one user per line
     */
    public static void loadUserDatabase(){
        System.out.println("\n\nLoading users...");
        System.out.println("----------------------------\n\n");

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter("users.csv", false)); //write to datafile. does not append as this is a initialization method

            for (Map.Entry<String, SystemUserModel> entry : ModelRegister.getInstance().getModelRegister().entrySet()){ //iterates over all users in system

                bw.write(entry.getKey());
                bw.write(',');

                if(entry.getValue() instanceof AdminModel){ //password of admin will be predetermined by whoever initialized user. every other user will add password on first login
                    bw.write(((AdminModel) entry.getValue()).getPassword());
                } else {
                    bw.write("null"); //sets password to null. this flags to login system that user has not yet set password
                }
                bw.write("\n");
            }
            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    /**
     * loads external course files into system
     * adds all users and instructors to system
     * adds course to system
     */
    private static void loadInfo(){

        ArrayList<File> courseFilesInputted = new ArrayList<>(); //all course files that user requests to be loaded
        ArrayList<File> courseFilesToLoad = new ArrayList<>(); //all the valid files inputted that can/will actually be loaded
        File courseFile; //represents a txt file that stores information for a course
        String courseFileInput; //represents one individual file name inputted by user


        Scanner read = new Scanner(System.in); //read from user
        OfferingFactory factory = new OfferingFactory(); //factory to create courses from files and store them and related users in the system's register

        System.out.println("Enter .txt files storing course data, separated by the Enter key. After last entry, press Enter again to quit");


        courseFileInput = read.nextLine(); //read in course file name


        while (!(courseFileInput.isEmpty())){ //takes in input until user presses enter to quit
            courseFile = new File(courseFileInput);
            courseFilesInputted.add(courseFile);
            courseFileInput = read.nextLine();

        }


        Integer requestedCourseFilesCount = courseFilesInputted.size();
        System.out.printf("You have entered %d course files\n\n", requestedCourseFilesCount);
        System.out.println("Verifying files...");

        for (File courseFileInputted : courseFilesInputted){

            if (courseFileInputted.isFile() && courseFileInputted.getName().endsWith((".txt"))){
                courseFilesToLoad.add(courseFileInputted);
            }
        }

        Integer invalidFilesCount = requestedCourseFilesCount - courseFilesToLoad.size();

        System.out.printf("Valid files: %d/%d \nInvalid files: %d/%d\n", courseFilesToLoad.size(), requestedCourseFilesCount, invalidFilesCount, requestedCourseFilesCount);

        System.out.println("Loading valid files...\n0% complete");



        BufferedReader br; //reads course offering. passed as parameter to factory
        CourseOffering courseOffering;
        Integer percentComplete; //tracks percentage of files that have completed loading


        //for every file that needs to be loaded, creates a reader for it and loads it into factory class to create a courseOffering
        for (File courseFileInputted : courseFilesToLoad) {
            try {
                br = new BufferedReader(new FileReader((courseFileInputted)));
                courseOffering = factory.createCourseOffering(br);
                br.close();
                percentComplete = ((((courseFilesInputted.indexOf(courseFileInputted)) + 1) * 100) / courseFilesInputted.size());
                System.out.printf("Currently: Creating course from %s \nCourse loading %d%% complete\n", courseFileInputted.getName(), percentComplete);
            } catch (FileNotFoundException e) {
                System.out.println("Error Code 1: File Initialization Error. Please restart program or submit a ticket at emu.org/support");
                e.printStackTrace();
            } catch (IOException e) {
                System.out.println("Error Code 2: Reader Error. Please restart program or submit a ticket at emu.org/support");
                e.printStackTrace();
            }
        }



        System.out.println("\n-----------------------------\nSystem successfully initialized.\n-----------------------------\n");

    }

    /**
     * registers admin user during initialization
     * info inputted by whoever initializes the system
     */
    private static void registerAdmin(){

        Scanner readAdminInfo = new Scanner(System.in); //read user input to get admin info
        Boolean valid = false; //whether current input is valid


        System.out.println("Register Admin...");
        System.out.println("-----------------------------\n");

        System.out.println("4-digit ID:   ");          //get id
        String adminID = readAdminInfo.nextLine();

        if (StringUtils.isNumeric(adminID) && adminID.length() == 4){ //check if id is correct format
            valid = true;
        }

        while (!valid){
            System.out.println("ID must be a 4-digit code. Enter again.");
            System.out.println("4-digit ID:   ");
            adminID = readAdminInfo.nextLine();
            if (StringUtils.isNumeric(adminID) && adminID.length() == 4){
                valid = true;
            }
        }


        System.out.println("First name:   ");               //get first name
        String adminFirstName = readAdminInfo.nextLine();

        System.out.println("Surname:   ");               //get surname
        String adminSurname = readAdminInfo.nextLine();

        valid = false;
        System.out.println("Password:   ");               //get password
        String adminPassword = readAdminInfo.nextLine();

        if (!adminPassword.equals("null")){ //verify password isn't "null"
            valid = true;
        }

        while (!valid){
            System.out.println("Password cannot be \"null\"");
            System.out.println("Password:   ");
            adminID = readAdminInfo.nextLine();
            if (!adminPassword.equals("null")){
                valid = true;
            }
        }



        AdminModel admin = new AdminModel(); //creates admin model based on inputted info
        admin.setID(adminID);
        admin.setName(adminFirstName);
        admin.setSurname(adminSurname);
        admin.setPassword(adminPassword);

        ModelRegister.getInstance().registerUser(adminID, admin); //stores admin in system register

    }


    //contains or calls everything necessary to log a user in and allow them to perform their commands
    public static void runLoginProcesses(boolean systemOn) {

        login(systemOn); //sends user to login screen


        activeUserModel = ModelRegister.getInstance().getRegisteredUser(activeUser.getAuthenticationToken().getTokenID()); //determines which user current verified user

        //gives user permissions
        if (activeUserModel instanceof AdminModel) {
            ((AdminModel) activeUserModel).services();
        }
        else if (activeUserModel instanceof StudentModel) {
            ((StudentModel) activeUserModel).services();
        }
        else if (activeUserModel instanceof InstructorModel){
            ((InstructorModel) activeUserModel).services();

        }

        //program comes here when user logs out. this removes the authenticatedUser from the system, but keep their userModel registered.
        activeUserModel = null;
        AuthenticationToken tokenToDelete = activeUser.getAuthenticationToken();
        tokenToDelete = null;
        activeUser = null;
        System.out.println("You have been logged off. Thank you for using EMU. \nPress Enter key to be redirected to login screen.");

        try { //if enter key is pressed, redirect user to login screen
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

        runLoginProcesses(SystemState.systemOn); //infinite loop. Program never closes, just goes back to login screen.

    }


    public static void main(String [] args){


        loadInfo(); //initialize system
        registerAdmin(); //set up administrator
        loadUserDatabase(); //stores login credentials

        System.out.println("\nWelcome to the Education Management for Universities (EMU) System!\n");

        runLoginProcesses(SystemState.systemOn); //redirects to login screen



    }




}


