/** AdminModel stores the information for an Admin user and allows an Admin user to perform several operations. This class implements IAdminModel interface.
 */
package systemUsers;

import java.io.BufferedReader; // Required for the readCourseRecord method
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.List;
import java.util.Scanner; // Required for user input

import customDatatypes.UserType;
import offerings.CourseOffering; //Required for the readCourseRecord method
import offerings.ICourseOffering; // Required for the readCourseRecord method
import offerings.OfferingFactory; // Required for the readCourseRecord method
import registrar.ModelRegister; // Required for the readCourseRecord method
import systemOperations.SystemState;

public class AdminModel implements IAdminModel{
	/**
	 * instance variable for the name of the Admin
	 */
	private String name;
	/**
	 * instance variable for the last name of the Admin
	 */
	private String surname;
	/**
	 * instance variable for the ID of the Admin
	 */
	private String ID;
	/**
	 * instance variable for the password of the Admin
	 */
	private String password;
	/**
	 * instance variable to set the type of the current user to Admin
	 */
	private UserType userType = UserType.ADMIN;

	/**
	 * getter method to return the type of the user, in this case it would be Admin
	 * @return userType - return Admin as the userType
	 */
	public UserType getUserType() { return userType; }

	/**
	 * The services method will serve as the Admin's operations interface. Here the Admin can choose what operations he wants to perform.
	 */
	public void services() {
		Scanner read = new Scanner(System.in);  // Required for user input

		while(true)
		{

			if(SystemState.systemOn == false)
			{
				System.out.println("");
				System.out.println("Hello Admin. The system is currently off. Here are your options:\n1.Start the system\n2.Logoff\n");  // Operations for Admin to use when the system is off
				System.out.println("Enter: ");
				String input = read.nextLine();

				if(input.equals("1"))
				{
					startSystem();
					System.out.println("The system is currently on.");
				}
				else if (input.equals("2")){
					break;
				}
				else {
					System.out.println("Sorry - That command is not recognize");
				}
			}
			else
			{
				System.out.println("");
				System.out.println("Hello Admin. The system is currently on. Here are your options:\n1.Stop the system\n2.Read course files.\n3.Logoff\n");  // Operations for Admin to use when the system is on
				System.out.println("Enter: ");
				String input = read.nextLine();
				if(input.equals("1"))
				{
					stopSystem();
				}
				else if(input.equals("2"))
				{
					readCourseRecord();
				}
				else if (input.equals("3")){
					break;
				}

				else {
					System.out.println("Sorry - That command is not recognize");
				}
			}
		}
	}

	/**
	 * startSystem() will access the systemOperations.SystemState class in order to turn on the system. If the system is already on then an error is produced.
	 */
	public static void startSystem()
	{
		if(SystemState.systemOn)
		{
			System.out.println("ERROR. The system is already on.");
		}
		else
		{
			SystemState.systemOn = true;
		}

	}

	/**
	 * stopSystem() will access the systemOperations.SystemState class in order to turn off the system. If the system is already off then an error is produced.
	 */
	public static void stopSystem()
	{
		if(SystemState.systemOn == false)
		{
			System.out.println("ERROR. The system is already off.");
		}
		else
		{
			SystemState.systemOn = false;
		}
	}

	/**
	 * readCourseRecord() will read any course text files that are available to read. It will create course and student objects and will print off the information for the course.
	 */
	public static void readCourseRecord()
	{
		/**
		 * All three variables are required for reading and writing to a text file
		 */
		BufferedReader buffer = null;
		FileWriter writer = null;
		BufferedWriter bw = null;

		try {

			Scanner read = new Scanner(System.in);
			/**
			 * while loop to read course records until the Admin says otherwise.
			 */
			while(true)
			{
				Boolean duplicate = false; // Variable that will keep track if a course added is a duplicate or not
				System.out.println("What course would you like to read from?\nEnter the name of the text file: ");
				String name = read.nextLine();


				OfferingFactory factory = new OfferingFactory();
				BufferedReader br = new BufferedReader(new FileReader(new File(name)));

				String tempLine = br.readLine(); // read the first line of the file to determine the course ID

				for(CourseOffering course : ModelRegister.getInstance().getAllCourses()){
					if(course.getCourseID().equals(tempLine.split("\t")[1])) // If the course that is about to be read in already exists in the ModelRegister
					{
						if(duplicate == false) // makes sure error is printed only once
						{
							duplicate = true;
							System.out.println(name+" is a duplicate course. Please try again.");
						}
					}

				}

				br.close();
				/**
				 * close br and start actually reading in the file
				 */
				br = new BufferedReader(new FileReader(new File(name)));
				CourseOffering	courseOffering = factory.createCourseOffering(br);
				br.close();





				if(!duplicate) // as long as the current course that is being read from is not a duplicate
				{
					for(StudentModel student : courseOffering.getStudentsAllowedToEnroll())
					{
						try {

							buffer = new BufferedReader(new FileReader("users.csv"));
							writer = new FileWriter("users.csv", true);
							String line;
							while ((line = buffer.readLine()) != null) // while the end of line has not been reached
							{
								String list[];
								list = line.split(","); // Spilt the line because it is a csv file
								if(list[0].equals(student.getID())) // Means the student exists in the csv file
								{

								}
								else // else he does not exist and needs to be added
								{
									bw = new BufferedWriter(writer); // initialize the BufferedWriter to write to the csv file
									bw.write(student.getID()+",null\n"); // append the student with a null password to the csv file
								}
							}

						}
						catch(Exception c)
						{
							System.out.println("Error. Exception: "+c);
						}
						finally {
							/**
							 * finally, close the BufferedWriter and the FileWriter.
							 */
							try {
								if(bw != null)
								{
									bw.close();
								}
								if(writer != null)
								{
									writer.close();
								}
							}
							catch(Exception c)
							{
								System.out.println("Error. Exception: "+c);
							}
						}
					}

					for(CourseOffering course : ModelRegister.getInstance().getAllCourses()){
						System.out.println("ID : " + course.getCourseID() + "\nCourse name : " + course.getCourseName() + "\nSemester : " +
								course.getSemester());
						System.out.println("Students allowed to enroll\n");
						for(StudentModel student : course.getStudentsAllowedToEnroll()){
							System.out.println("Student name : " + student.getName() + "\nStudent surname : " + student.getSurname() +
									"\nStudent ID : " + student.getID() + "\nStudent EvaluationType : " +
									student.getEvaluationEntities().get(course) + "\n\n");
						}

						for(StudentModel student : course.getStudentsAllowedToEnroll()){
							for(ICourseOffering course2 : student.getCoursesAllowed())
								System.out.println(student.getName() + "\t\t -> " + course2.getCourseName());
						}
					}
				}

				System.out.println("Would you like to read from another course?\nEnter y for yes or n for no: "); // If the Admin wants to continue reading files then he has the choice to do so
				name = read.nextLine();
				if(name.equalsIgnoreCase("y"))
				{

				}
				else
				{
					break;
				}

			}



		}
		catch(Exception c)
		{
			System.out.println("Error occured. The following exception has been thrown: "+c);
		}


	}
	/**
	 * getter method for the name of an Admin
	 * @return name - the name of the Admin
	 */
	public String getName() {
		return name;
	}

	/**
	 * setter method to set the name of an Admin
	 * @param name - new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * getter method for the name of an Admin
	 * @return surname - the last name of the Admin
	 */
	public String getSurname() {
		return surname;
	}

	/**
	 * setter method to set the last name of an Admin
	 * @param surname - new surname
	 */
	public void setSurname(String surname) {
		this.surname = surname;
	}

	/**
	 * getter method for the ID of an Admin
	 * @return ID - the ID of an Admin
	 */
	public String getID() {
		return ID;
	}

	/**
	 * setter method to set the ID of an Admin
	 * @param iD - new ID
	 */
	public void setID(String iD) {
		ID = iD;
	}

	/**
	 * setter method to set the password of the Admin
	 * @param password - new password
	 */
	public void setPassword(String password){this.password = password;}

	/**
	 * getter method to return the password of the Admin
	 * @return password - return current password
	 */
	public String getPassword(){ return this.password;}
}
