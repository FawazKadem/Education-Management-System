package systemUsers;

import java.util.List;

import customDatatypes.*;
import offerings.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import registrar.ModelRegister;
import systemOperations.SendEmail;



public class InstructorModel implements IInstructorModel {

	private String name;
	private String surname;
	private String ID;
	private UserType userType = UserType.INSTRUCTOR;
	private List<ICourseOffering> isTutorOf;

	public InstructorModel(){
	}

	public UserType getUserType() {
		return userType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public String getID() {
		return ID;
	}

	public void setID(String iD) {
		ID = iD;
	}

	public List<ICourseOffering> getIsTutorOf() {
		return isTutorOf;
	}

	public void setIsTutorOf(List<ICourseOffering> courses) {
		this.isTutorOf = courses;
	}

	//This method presents the user with the operations they can perform as an instructor.
	public void services(){
		Scanner input = new Scanner(System.in);
		System.out.println("User has been logged in as an instructor.");

		//Gives users options for the operation they would like to perform.
		//If they enter a valid input, that respective operation is called. Otherwise, the program loops again.
		while(true) {
			System.out.println("Enter the number corresponding to the operation you wish to access.");
			System.out.println("1: Add/Modify mark for a student");
			System.out.println("2: Calculate final grade for a student");
			System.out.println("3: Print class record");
			System.out.println("4: Logout");
			String insChoice = input.nextLine();

			if(insChoice.equals(4)) {
				break;
			}
			else if(insChoice.equals(1)) {
				addMark();
			}
			else if(insChoice.equals(2)) {
				calculateFinalGrade();
			}
			else if(insChoice.equals(3)) {
				printClassRecord();
			}
			else {
				System.out.println("Invalid input.");
			}

		}
	}

	//This method calculates the final grade for a student or all the students in a course, depending on what the instructor wants.
	public static void calculateFinalGrade() {
		Scanner input = new Scanner(System.in);
		CourseOffering studentCourse = null;
		StudentModel studentToCalculate = null;
		List<StudentModel> studentsInCourse = null;

		//This while loop asks the user for a course ID and searches the ModelRegister for the course the user is trying to calculate the final grade for.
		//If the user enters an invalid input, the while loop loops again.
		//If the user enters 0 to exit, the method is returned.
		while(studentCourse == null) {
			System.out.println("Enter course ID you would like to calculate a final grade for (or 0 to exit): ");
			String userInput = input.nextLine();

			for(CourseOffering course : ModelRegister.getInstance().getAllCourses()) {
				if(course.getCourseID().equals(userInput)) {
					studentsInCourse = course.getStudentsEnrolled();
					studentCourse = course;
				}
			}

			if(userInput.equals("0")) {
				return;
			}

			if(studentCourse == null) {
				System.out.println("Invalid input - course not found.");
			}
		}

		int userChoice = 0;

		//This while loop asks if the user would like to calculate final grades for a student or all the students.
		//If the user enters 0, the method is returned.
		while(true) {
			System.out.println("Would you like to calculate final grades for ");
			System.out.println("1: A student in " + studentCourse.getCourseName());
			System.out.println("2: All students in " + studentCourse.getCourseName());
			System.out.println("0: Exit");
			int userInput = input.nextInt();
			if(userInput == 1) {
				userChoice = 1;
				break;
			}

			if(userInput == 2) {
				userChoice = 2;
				break;
			}

			if(userInput == 0) {
				return;
			}

			else {
				System.out.println("Invalid input.");
			}
		}

		//If the user chose to calculate final grades for 1 student, the program asks for the student ID of the student.
		//If the user enters 0, the method is returned.
		if(userChoice == 1) {
			while(studentToCalculate == null) {
				System.out.println("Enter the student ID of the student you are calculating a final grade for (or 0 to exit): ");
				String userInput = input.nextLine();

				if(userInput.equals("0")) {
					return;
				}

				//Traverses through the list of students in the course until the student is found and sets studentToCalculate to that student.
				for(StudentModel student : studentsInCourse) {
					if(student.getID().equals(userInput)) {
						studentToCalculate = student;
					}
				}

				if(userInput.equals("0")) {
					return;
				}

				if(studentToCalculate == null) {
					System.out.println("Invalid input - student not found.");
				}

			}

			//Prints the student information and final grade to the user
			System.out.println("Student Name: " + studentToCalculate.getName());
			System.out.println("Student ID: " + studentToCalculate.getID());
			System.out.println("Course: " + studentCourse.getCourseName());
			System.out.println("Course ID: " + studentCourse.getCourseID());
			System.out.println("Final Grade: " + studentCourse.calculateFinalGrade(studentToCalculate.getID()));
		}

		//If the user chose to calculate the final grade for all students, each student's final grade in the course is calculated and is printed to the user
		if(userChoice == 2) {
			for(StudentModel student : studentsInCourse) {
				System.out.println("Student: " + student.getName() + " (" + student.getID() + ")" + "\t" + "Final Grade: " + studentCourse.calculateFinalGrade(student.getID()));
			}
		}
	}

	//This method adds a mark for a student enrolled in a course.
	public static void addMark() {
		Scanner input = new Scanner(System.in);
		List<StudentModel> studentsInCourse = null;
		CourseOffering studentCourse = null;
		StudentModel studentToAdd = null;
		String markName = "";
		boolean allowed = true;
		boolean isMarkNew = false;
		double markToAdd = -1;
		boolean found = false;

		//This while loop asks the user for a course ID and searches the ModelRegister for the course the user is adding a mark for.
		//If the user enters an invalid input, the while loop loops again.
		//If the user enters 0 to exit, the method is returned.
		while (studentCourse == null) {
			System.out.println("Enter the course ID you are adding a mark for (or 0 to exit): ");
			String courseToAddMark = input.nextLine();

			for (CourseOffering course : ModelRegister.getInstance().getAllCourses()) {
				if (course.getCourseID().equals(courseToAddMark)) {
					studentsInCourse = course.getStudentsEnrolled();
					studentCourse = course;

				}
			}

			if (courseToAddMark.equals("0")) {
				return;
			}

			if (studentCourse == null) {
				System.out.println("Invalid input - course not found.");
			}
		}

		//This while loop asks the user to enter the student ID of the student they are adding a mark for.
		//If the user enters 0, the method is returned.
		while (studentToAdd == null) {
			System.out.println("Enter the student ID of the student you are adding a mark for (or 0 to exit): ");
			String studentID = input.nextLine();

			//Traverses through the list of students in the course until the student is found and sets studentToAdd to that student.
			for (StudentModel student : studentsInCourse) {
				if (student.getID().equals(studentID)) {
					studentToAdd = student;
				}
			}

			if (studentID.equals("0")) {
				return;
			}

			if (studentToAdd == null) {
				System.out.println("Invalid input - student not found.");
			}

		}

		//This while loop presents the user with a list of evaluation entities and asks the user to enter the name of the evaluation entity they are adding a mark for.
		while (true) {
			Marks oldMarks = studentToAdd.getPerCourseMarks().get(studentCourse);
			System.out.println("Type the name of the evaluation entity you would like to add a mark for from the list of entities below (or 0 to exit): ");


			//Prints the possible evaluation entities
			oldMarks.initializeIterator();
			while (oldMarks.hasNext()) {
				Map.Entry<String, Double> currentMark = oldMarks.getNextEntry();
				System.out.println("	Assessment " + currentMark.getKey());
			}
			markName = input.nextLine();

			markToAdd = -1;
			while (markToAdd == -1) {
				System.out.println("Enter the mark (out of 100) you are adding: ");
				markToAdd = input.nextDouble();

				//If user enters invalid grade, markToAdd is set to -1 and while loop loops again.
				if (!(markToAdd >= 0 && markToAdd <= 100)) {
					markToAdd = -1;
					System.out.println("Invalid input - mark out of range. Mark deleted.");
				}
			}

			if (oldMarks.getEvalStrategy().containsKey(markName)) {

				if (oldMarks.getValueWithKey(markName) < 0) {
					oldMarks.getEvalStrategy().replace(markName, markToAdd);
					sendStudentNotification(studentToAdd, true, markToAdd, markName, studentCourse.getCourseName());
					System.out.println("Success! Your mark has been added. Email has been sent to student.");
					break;
				} else {
					System.out.println("This mark already exists. Do you want to overwrite it? 1 for yes, 0 for no");

					if (input.nextLine().equals("1")) {
						oldMarks.getEvalStrategy().replace(markName, markToAdd);
						sendStudentNotification(studentToAdd, false, markToAdd, markName, studentCourse.getCourseName());
						System.out.println("Success! Your mark has been modified.");
						break;
					}

				}
			} else {
				System.out.println("Error: That student does not have that assessment in their evaluation strategy.");
			}


		}
	}

	//This method prints the record for the class the user enters.
	public static void printClassRecord()
	{
		Scanner read = new Scanner(System.in);


		while(true)
		{
			System.out.println("What course would you like to print a record for?\nEnter the course ID or 0 to exit: ");
			String ID = read.nextLine();

				//For loop traverses through ModelRegister to find course.
				for(CourseOffering course : ModelRegister.getInstance().getAllCourses()) {

					//If the course is found, the record is printed for the course.
					if(course.getCourseID().equals(ID)) {
						List<StudentModel> list = course.getStudentsEnrolled();

						for(int j=0; j<list.size();j++)	{
							StudentModel student = list.get(j);
							System.out.println("---\nStudent name : " + student.getName() + "\nStudent surname : " + student.getSurname() +
									"\nStudent ID : " + student.getID() + "\nStudent EvaluationType : " +
									student.getEvaluationEntities().get(course) +  "\n\n");

							Marks oneCourseMarks = student.getPerCourseMarks().get(course);
							oneCourseMarks.initializeIterator();
							while (oneCourseMarks.hasNext()) {
								Map.Entry<String, Double> currentMark = oneCourseMarks.getNextEntry();

								System.out.println("	Assessment: " + currentMark.getKey());
								System.out.println("	Grade: " + currentMark.getValue());


							}
						}

						return;
					}
				}

			if (read.equals("0")){
				System.out.println("Exiting...");
				return;
			}
			//If the user's input is invalid, they are redirected to the beginning of the loop.

			System.out.println("No course found.");
				return;
		}
	}

	//This method sends a student notification to the student passed as a parameter.
	public static void sendStudentNotification(StudentModel student, Boolean markIsNew, Double mark, String evaluation, String course) {
		boolean notifStatus = student.getNotificationStatus();
		NotificationTypes notifType = student.getNotificationType();
		String subject;
		String body;


			if ((notifType == NotificationTypes.EMAIL) && notifStatus == true){
			if (markIsNew){
				body = "Hello" + student.getName() + student.getSurname() + ", you have a new notification from" + course + "\n"
						+ "Your mark for " + evaluation + "has been added:" + mark;

				subject = "Email from EMU Course System: Grade Added";


			} else {

				body = "Hello" + student.getName() + student.getSurname() + ", you have a new notification from" + course + "\n"
						+ "Your mark for " + evaluation + "has been modified:" + mark;

				subject = "Email from EMU Course System: Grade Modified";

			}
			SendEmail.main(subject, body);
		}
	}
}
