Determining the Apportionment for the United States Congress
Overview
This project involves implementing Alexander Hamilton's Apportionment algorithm to allocate seats in the United States House of Representatives based on state populations. Apportionment is the process of dividing a finite number of discrete resources—in this case, congressional seats—among different parties based on their population. The algorithm ensures that each representative can only represent one state and that states with higher populations receive more representatives.

The project is divided into three parts:

Part A - Create a team repository, ensure all team members are added, and begin writing some initial code.
Part B - Develop a fully functional Apportionment program using Hamilton's method.
Part C - Implement three new or modified features based on Part B.
Objectives
By completing this assignment, you will:

Become familiar with using Git and GitHub for collaborative software development.
Gain experience working in a team environment.
Demonstrate proficiency in Java programming, particularly in handling CSV files, managing data structures, and implementing algorithms.
Learn to build software iteratively and adapt to changing requirements.
Understand the use of Gradle for building and managing Java projects.
Repository Structure
The project repository is organized as follows:

css
Copy code
/hw1-netid1-netid2-netid3/
│
├── build.gradle
├── gradlew
├── gradlew.bat
├── settings.gradle
├── README.md
└── src/
    ├── main/
    │   └── java/
    │       └── edu/
    │           └── virginia/
    │               └── sde/
    │                   └── hw1/
    │                       ├── Apportionment.java
    │                       ├── State.java
    │                       └── Main.java
    └── test/
        └── java/
            └── edu/
                └── virginia/
                    └── sde/
                        └── hw1/
                            └── ApportionmentTest.java
Installation and Setup
Clone the Repository:

bash
Copy code
git clone https://github.com/your-repository-url.git
cd hw1-netid1-netid2-netid3
Open the Project in IntelliJ:

Open IntelliJ and select "New -> Project from Version Control -> Git".
Paste the repository URL and clone it.
Once the project is cloned, allow IntelliJ to auto-import the Gradle project and run the initial setup tasks.
Build the Project:

Open the terminal in IntelliJ and run:
bash
Copy code
./gradlew build
This will compile the Java code and generate a .jar file in the build/libs/ directory.
Usage
To run the Apportionment program from the command line:

bash
Copy code
java -jar build/libs/Apportionment.jar population.csv [number_of_representatives]
population.csv: A CSV file containing state names and their populations.
[number_of_representatives]: (Optional) The total number of representatives to be apportioned. Defaults to 435.
Example
bash
Copy code
java -jar build/libs/Apportionment.jar population.csv 1000
This will output the number of representatives allocated to each state based on their population, printed in alphabetical order.

Program Features
Input Handling: The program reads the state population data from a CSV file, ignoring any malformed lines or extra columns.
Hamilton's Apportionment Algorithm: Allocates seats based on state populations and distributes remaining seats according to the largest fractional remainders.
Customizable Representative Count: Allows specifying a different total number of representatives for various what-if scenarios.
Error Handling
The program includes meaningful error messages for common issues such as:

Missing or incorrect command-line arguments.
Invalid CSV file formats or bad data.
File reading errors.
