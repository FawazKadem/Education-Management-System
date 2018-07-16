package systemUsers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import offerings.*;
import registrar.ModelRegister;
import customDatatypes.*;
import systemUsers.*;
import systemOperations.*;
import java.util.Iterator;


public class StudentModel implements IStudentModel{

	private String name;
	private String surname;
	private String ID;
	private UserType userType = UserType.STUDENT;
	private List<ICourseOffering> coursesAllowed = new ArrayList<ICourseOffering>();

	private List<ICourseOffering> coursesEnrolled = new ArrayList<ICourseOffering>();
	private Map<ICourseOffering, EvaluationTypes> evaluationEntities;
	//	check the comments at the Marks Class this map should contain as many pairs of <CourseOffering, Marks> as course that
//	the student has enrolled in.
	private Map<ICourseOffering, Marks> perCourseMarks = new HashMap<ICourseOffering, Marks>();
	private NotificationTypes notificationType;
	private String eType;

	private static Scanner input = new Scanner (System.in); //allows for user input

	private boolean notificationStatus = false; //students status to receive notifications

	public UserType getUserType() { return userType; }

	public String getName() { return name; }

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

	public List<ICourseOffering> getCoursesAllowed() {
		return coursesAllowed;
	}

	public void setCoursesAllowed(List<ICourseOffering> coursesAllowed) {
		this.coursesAllowed = coursesAllowed;
	}

	public List<ICourseOffering> getCoursesEnrolled() {
		return coursesEnrolled;
	}

	public void setCoursesEnrolled(List<ICourseOffering> coursesEnrolled) {
		this.coursesEnrolled = coursesEnrolled;
	}

	public Map<ICourseOffering, EvaluationTypes> getEvaluationEntities() {
		return evaluationEntities;
	}

	public void setEvaluationEntities(Map<ICourseOffering, EvaluationTypes> evaluationEntities) {
		this.evaluationEntities = evaluationEntities;
	}

	public Map<ICourseOffering, Marks> getPerCourseMarks() {
		return perCourseMarks;
	}

	public void setPerCourseMarks(Map<ICourseOffering, Marks> perCourseMarks) {
		this.perCourseMarks = perCourseMarks;
	}

	public NotificationTypes getNotificationType() {
		return notificationType;
	}

	public void setNotificationType(NotificationTypes notificationType) {
		this.notificationType = notificationType;
	}

	public boolean getNotificationStatus(){
		return this.notificationStatus;
	}

	private void enroll() {

		Boolean success = false;
		String courseID;

		System.out.println("Enter the course ID you want to enroll: ");
		courseID = input.next();

		CourseOffering course = ModelRegister.getInstance().getRegisteredCourse(courseID);

		if (course == null) {
			System.out.println("Course does not exist. Enrolment failed.");
		} else {

			for (StudentModel student : course.getStudentsAllowedToEnroll()) {
				if (student.getID().equals(this.getID())) {
					System.out.println("1");
					if (!this.getCoursesEnrolled().contains(course)) {
						System.out.println("2");
						this.coursesEnrolled.add(course);
						course.getStudentsEnrolled().add(student);
						success = true;

					} else {
						System.out.println("Student is already enrolled");
					}

				}
			}

			if (success){

					boolean valid = false;
					while (!valid) {
						System.out.println("Enter a valid EvaluationType: FC, FA, PC, or PA: "); //set evaluation type for student in that course
						eType = input.next();
						if (eType.equals("FC")) {
							this.evaluationEntities.put(course, EvaluationTypes.FULL_CREDIT);
							valid = true;
						} else if (eType.equals("FA")) {
							this.evaluationEntities.put(course, EvaluationTypes.FULL_AUDIT);
							valid = true;
						} else if (eType.equals("PC")) {
							this.evaluationEntities.put(course, EvaluationTypes.PART_CREDIT);
							valid = true;
						} else if (eType.equals("PA")) {
							this.evaluationEntities.put(course, EvaluationTypes.PART_AUDIT);
							valid = true;
						} else {
							System.out.println("Not valid Evaluation Type");
						}
					}

					Marks newMarks = new Marks();
					Weights weightsTemp = course.getEvaluationStrategies().get(this.evaluationEntities.get(course));

					weightsTemp.initializeIterator();
					while (weightsTemp.hasNext()){
						weightsTemp.next();
						String assessment = weightsTemp.getCurrentKey();
						newMarks.addToEvalStrategy(assessment, -5.0);
					}

					this.perCourseMarks.put(course, newMarks);


					System.out.println("You have been enrolled into the course");

			} else{
				System.out.println("Enrolment Failed. ");
			}



		}
	}






	public void setNotificationStatus(){ //allows student to set notification status
		System.out.println("Would you like to turn notifications on?\n Yes: 1 \n No: 0");
		String status = input.next();
		if(status.equals("1")){ //turn on notifications
			System.out.println("Notifications are turned on");
			this.notificationStatus = true;
		}
		else if(status.equals("2")){ //turn off notifications
			System.out.println("Notifications are turned off");
			this.notificationStatus = false;
		}
		else{ //student did not enter valid data
			System.out.println("Status was not able to update");
		}
	}

	public void setNotificationPreference(){ //let students set their preference of notifications
		if(this.getNotificationStatus()==false){ //notification status must be on in order to change preference
			System.out.println("Notification Status must be on to change preference");
		}
		else{
			System.out.println("Choose your notification preference \n Email: 1 \n SMS: 2 \n Pigeon Post: 3");
			int preference = input.nextInt();
			if(preference==1){ //change preference to email notifications
				this.setNotificationType(NotificationTypes.EMAIL);
				System.out.println("Notification preference changed to Email");
			}
			else if(preference==2){ //change preference to sms notifications
				setNotificationType(NotificationTypes.CELLPHONE);
				System.out.println("Notification preference changed to SMS");
			}
			else if(preference==3){ //change preference to pigeon post notifications
				setNotificationType(NotificationTypes.PIGEON_POST);
				System.out.println("Notifciation preference changed to Pigeon post");
			}
			else{ //student did not enter a valid preference
				System.out.println("Notification preference not changed");
			}
		}
	}

	public void printRecord() { //print either all course records for student or specified course
		System.out.println("Print record for course or all courses: Type ALL or the course ID");
		String record = input.next();
		boolean enrolled = false;

		if (record.equals("ALL")) { //print record for each course student is enrolled

			System.out.println("Record for all enrolled courses for " + name + " " + surname);
			if (this.getCoursesEnrolled().isEmpty()) {
				System.out.print("Not enrolled in any courses");
			} else {
				for (ICourseOffering course : this.coursesEnrolled) {

					System.out.println(course.getCourseName() + " " + course.getCourseID() + ":");
					System.out.println("Evaluation Type: " + " " + evaluationEntities.get(course).getText());
					System.out.println("Marks: ");
					System.out.println();
					Marks oneCourseMarks = perCourseMarks.get(course);
					oneCourseMarks.initializeIterator();
					while (oneCourseMarks.hasNext()) {
						Map.Entry<String, Double> currentMark = oneCourseMarks.getNextEntry();

						System.out.println("	Assessment: " + currentMark.getKey());
						System.out.println("	Grade: " + currentMark.getValue());


					}
					System.out.print("	Total Grade So Far: " + ((CourseOffering) course).calculateFinalGrade(this.getID()));
				}
			}

		} else {
			System.out.println("Record for course " + record + " for student " + name + " " + surname);

				for (ICourseOffering course : this.coursesEnrolled) {
					System.out.println("3");
					if (course.getCourseID().equals(record)) {
						System.out.println(course.getCourseName() + " " + course.getCourseID() + ":");
						System.out.println("Evaluation Type: " + " " + evaluationEntities.get(course).getText());
						System.out.println("Marks: \n");
						System.out.println();
						Marks oneCourseMarks = perCourseMarks.get(course);

						oneCourseMarks.initializeIterator();
						while (oneCourseMarks.hasNext()) {
							Map.Entry<String, Double> currentMark = oneCourseMarks.getNextEntry();

							System.out.println("	Assessment: " + currentMark.getKey());
							System.out.println("	Grade: " + currentMark.getValue());


						}
						System.out.println("\n	Final Grade (Ignore if \"-50\"): " + ((CourseOffering) course).calculateFinalGrade(this.getID()));
				}
			}
		}
	}

	public void services (){
		System.out.println("USER has been logged in as a student.");
		while(true){
			System.out.println("Enter the number corresponding to the operation you wish to access:");
			System.out.println("1: Enroll in course");
			System.out.println("2: Notification status");
			System.out.println("3: Notifications preference");
			System.out.println("4: Print course record");
			System.out.println("5: Logout");
			String option = input.next();

			if(option.equals("1")){
				this.enroll();
			}
			else if(option.equals("2")){
				setNotificationStatus();
			}
			else if(option.equals("3")){
				setNotificationPreference();
			}
			else if(option.equals("4")){
				printRecord();
			}
			else if(option.equals("5")){
				break;
			}
			else{
				System.out.println("Invalid input");
			}
		}
	}

}
