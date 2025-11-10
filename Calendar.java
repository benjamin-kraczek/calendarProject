package quarterOneProject;

import java.util.ArrayList;

import java.awt.*;

import javax.swing.*;

import java.awt.event.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime; 
import java.time.format.DateTimeFormatter;

public class Calendar implements MouseListener, WindowListener {
	static final String filePath = System.getProperty("user.dir") + "\\dataFiles\\calendarEvents.csv";
	static ImageIcon trashIcon = new ImageIcon( ("user.dir")+"\\dataFiles\\Screenshot trach icon.png");
	static ArrayList<ArrayList<String>> events = new ArrayList<ArrayList<String>>();
	static ArrayList<ArrayList<String>> tasks = new ArrayList<ArrayList<String>>();
	
	static MouseListener normalMouseListener = new MouseListener()  {
		@Override
		public void mouseClicked(MouseEvent e) {
			mouseClicked = true; 

			mouseX=MouseInfo.getPointerInfo().getLocation().x;
			mouseY = MouseInfo.getPointerInfo().getLocation().y;

		}
		@Override
		public void mousePressed(MouseEvent e) {}
		@Override
		public void mouseReleased(MouseEvent e) {}
		@Override
		public void mouseEntered(MouseEvent e) {}
		@Override
		public void mouseExited(MouseEvent e) {}
	};

	
	static int mouseX=0;
	static int mouseY=0;
	static boolean mouseClicked = false;
	static boolean isRunning = true;
	static boolean dayIsRunning = false;
	static ArrayList<ArrayList<JLabel>> positionOfLabels = new ArrayList<ArrayList<JLabel>>();

	static ArrayList<ArrayList<String>> calendarLabels = new ArrayList<ArrayList<String>>();
	static String day;
	static int today;
	static String month;
	static int monthIndex=0; // this stores the months number-1, so October would be 9, November 10, etc.
	static int[] daysOfMonth = {31,28,31,30,31,30,31,31,30,31,30,31};
	static String[] daysList = {"Sun","Mon","Tue","Wed","Thu","Fri","Sat"};
	static String[][] months = {{"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"},
			{"January","February","March","April","May","June","July","August","September","October","November","December"}};
	//Default window events, I don't know why these need to exist as the listeners define them anyway, but they need to be here
	public void windowOpened(WindowEvent e) {}
	public void windowClosed(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowActivated(WindowEvent e) {}
	public void windowDeactivated(WindowEvent e) {}
	public void windowClosing(WindowEvent e) {}
	//Default mouse events 
	public void mouseClicked(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}

	
	
	/*
	 * This adds labels to the main JPanel
	 * Calendar labels are added first, with the days, and numbers. The events are then added in the correct location
	 * There is code implemented to ensure that multiple events can be placed on the same day and not overlap
	 */
	public static void addLabels(JPanel p) {
		for(int i = 0; i < positionOfLabels.size(); i++) {
			for(int j = 0; j< positionOfLabels.get(i).size(); j++) {
				p.remove(positionOfLabels.get(i).get(j));
			}
		}
		positionOfLabels.clear();
		Font smallFont = new Font("Arial",Font.PLAIN, 10);
		positionOfLabels.add(new ArrayList<JLabel>()); 
		//adds the day names
		for(int i = 0; i <7; i++) {
			positionOfLabels.get(0).add(new JLabel(daysList[i]));
			positionOfLabels.get(0).get(i).setBounds(10+80*i,10,40,20); 
			positionOfLabels.get(0).get(i).setFont(smallFont);
			p.add(positionOfLabels.get(0).get(i)); 
		}
		//Adds the numbers, pulling from the list already derived. 
		for (int i = 1; i< 7; i++) {
			positionOfLabels.add(new ArrayList<JLabel>());
			for(int j = 0; j<7;j++) {
				positionOfLabels.get(i).add(new JLabel(calendarLabels.get(i).get(j)));
				positionOfLabels.get(i).get(j).setBounds(10+80*j,-30+80*i,40,20); 
				positionOfLabels.get(i).get(j).setFont(smallFont);
				p.add(positionOfLabels.get(i).get(j));
			}

		}

		//Adds events to the calendar 6 13 10 27
		positionOfLabels.add(0,new ArrayList<JLabel>());
		ArrayList<Integer> eventStacking = new ArrayList<Integer>();
		for(int i = 0; i < 32;i++) {
			eventStacking.add(0); 
		}
		for(int i = 1; i<events.size(); i++) {
			if((Integer.parseInt(events.get(i).get(5))) == monthIndex+1) { 
				if(events.get(i).get(1).equals("true")) {
					positionOfLabels.get(0).add(0,new JLabel(events.get(i).get(0)));
				}else {
					positionOfLabels.get(0).add(0,new JLabel(events.get(i).get(0)+ " " + events.get(i).get(2)+":"+events.get(i).get(3)));}
				int LXPosition =10+ (80*getCell(Integer.parseInt(events.get(i).get(4)))[0]);
				int LYPosition = 50 + 10*eventStacking.get(Integer.parseInt(events.get(i).get(4))) + 
						(52*getCell(Integer.parseInt(events.get(i).get(4)))[1]);
				positionOfLabels.get(0).get(0).setBounds(LXPosition,LYPosition,80,LYPosition+10);
				positionOfLabels.get(0).get(0).setFont(smallFont);
				p.add(positionOfLabels.get(0).get(0));
				eventStacking.set(Integer.parseInt(events.get(i).get(4)),1+ eventStacking.get(Integer.parseInt(events.get(i).get(4))));
			}
		}
		p.repaint();
	}

	/*
	 * This is used to create a second JFrame and JPanel. The JPanel has text input boxes and if the user inputs logical data, 
	 * it is added to the list of events. 
	 * */
	@SuppressWarnings("unchecked")
	public static void selectDay(int[] selected) {
		if(calendarLabels.get(selected[1]+1).get(selected[0]).equals("null")) {
			System.out.println("Select valid box");
		}else {
			
			dayIsRunning = true;

			
			int thisDay = Integer.parseInt(calendarLabels.get(selected[1]+1).get(selected[0]));
			ArrayList<ArrayList<String>> thisDayEvents = new ArrayList<ArrayList<String>>();
			ArrayList<Boolean> removed = new ArrayList<Boolean>();
			
			for (int i = 1; i < events.size(); i++) {
				if(Integer.parseInt(events.get(i).get(4)) == thisDay && Integer.parseInt(events.get(i).get(5)) ==monthIndex+1) {
					thisDayEvents.add(events.get(i));
				}
			}
			for(int i = 0; i<thisDayEvents.size(); i++) {
				removed.add(false);
				
			}
			JFrame dayFrame = new JFrame(String.valueOf(thisDay) + " (Close Window With All Text Inputs Filled To Save Changes)");
			dayFrame.addWindowListener(new WindowAdapter() {
				@Override 
				public void windowClosing(WindowEvent d) {
					dayIsRunning = false;
				}
			});
			
			JPanel dayPanel = new JPanel(){
				@Override
				protected void paintComponent(Graphics g) 
				{
					super.paintComponent(g);  
					g.setColor(new Color(0,0,0));
					int yPos = 0;
					for (int i = 0; i < thisDayEvents.size()+1; i++) 
					{
						yPos=45+(i*40);
						g.drawRect(5,yPos,295,5); 
						g.fillRect(5,yPos,295,5);  
					}
					for (int i = 0; i < thisDayEvents.size(); i++) 
					{
						yPos=55+(i*40);
						
						if(removed.get(i)) 
						{
							g.drawRect(5,yPos,295,25); 
							g.fillRect(5,yPos,295,25);
						}
					}
				}
			};
			dayPanel.setLayout(null);
			ArrayList<JLabel> trashStorage = new ArrayList<JLabel>();
			for(int i = 0; i< thisDayEvents.size(); i++) {
				trashStorage.add(new JLabel(trashIcon));
				trashStorage.get(i).setBounds(300,(55+i*40),25,25);	
				dayPanel.add(trashStorage.get(i));
			}
			
			dayPanel.addMouseListener(normalMouseListener);

		 	ArrayList<ArrayList<JTextField>> eventEditingFields = new ArrayList<ArrayList<JTextField>>();
			for(int i = 0; i<thisDayEvents.size(); i++) {
				eventEditingFields.add(new ArrayList<JTextField>());
				eventEditingFields.get(i).add(new JTextField(thisDayEvents.get(i).get(0),1));
				eventEditingFields.get(i).get(0).setBounds(5, (50+i*40),150,35);
				dayPanel.add(eventEditingFields.get(i).get(0));
				
				if(thisDayEvents.get(i).get(1).equals("true")) {
					eventEditingFields.get(i).add(new JTextField("true",1));
				}else {
					eventEditingFields.get(i).add(new JTextField("false",1));
				}
				eventEditingFields.get(i).get(1).setBounds(155, (50+i*40), 50,35);
				dayPanel.add(eventEditingFields.get(i).get(1));
				
				eventEditingFields.get(i).add(new JTextField(thisDayEvents.get(i).get(2),1));
				eventEditingFields.get(i).get(2).setBounds(205, (50+i*40),50,35);
				dayPanel.add(eventEditingFields.get(i).get(2));
				
				eventEditingFields.get(i).add(new JTextField(thisDayEvents.get(i).get(3),1));
				eventEditingFields.get(i).get(3).setBounds(255, (50+i*40),50,35);
				dayPanel.add(eventEditingFields.get(i).get(3));
			}
			
			//creation of the user input spaces
			JTextField nameField = new JTextField("Name",1);
			nameField.setBounds(5,2,100,30);
			dayPanel.add(nameField);
			JTextField startField = new JTextField("Start Time",1);
			startField.setBounds(110,2,50,30);
			dayPanel.add(startField);
			JTextField endField = new JTextField("End Time",1);
			endField.setBounds(165,2,50,30);
			dayPanel.add(endField);
			JTextField allField = new JTextField("all day true/false",1);
			allField.setBounds(220,2,50,30);
			dayPanel.add(allField);

			dayPanel.setBackground(new Color(192, 192, 192));

			dayFrame.add(dayPanel);
			dayFrame.setSize(500,300);
			dayFrame.setVisible(true);
			while (dayIsRunning) 
			{
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if(mouseClicked) 
				{
					for(int i = 0; i < thisDayEvents.size(); i++) 
					{
						if(mouseX>300 && mouseX<330 && mouseY>80 +(i*40) && mouseY<115 +(i*40)) 
						{
							ArrayList<String> obligatory = new ArrayList<String>();
							obligatory = (ArrayList<String>) thisDayEvents.get(i).clone();
							//thisDayEvents.get(i).remove(i);
							int j=0;
							while(!obligatory.equals(events.get(j))) {
								j++;
							}
							events.remove(j);
							removed.set(i, true);
							dayFrame.repaint();
						}
					}
					mouseClicked = false;
				}
			}
			//This code checks if the input be valid, invalid, or cause type conversion errors
			boolean premited = false;
			boolean isAllDay = false;
			
			
			
			if(nameField.getText().equals("Name") && startField.getText().equals("Start Time") && 
					endField.getText().equals("End Time") && allField.getText().equals("all day true/false") ) {
			}
			else {
				try{
					if(allField.getText().toLowerCase().equals("true") || allField.getText().toLowerCase().equals("false")) {
						premited = true;
						if(allField.getText().toLowerCase().equals("true")) {isAllDay = true;}
					}if(startField.getText().equals("") && startField.getText().equals("")) {
						premited = true;
						isAllDay = true;
					}
					else {
						//Check if the time inputs are logical
						if(Integer.parseInt(endField.getText().split(":")[0]) <25 && 
								Integer.parseInt(endField.getText().split(":")[1]) <60 && endField.getText().split(":").length ==2) {}
						else {premited = false;}
						if(Integer.parseInt(startField.getText().split(":")[0]) <25 && 
							Integer.parseInt(startField.getText().split(":")[1]) <60 && startField.getText().split(":").length ==2) {}
						else {premited = false;}
					}
				} catch(Exception e) {
					//e.printStackTrace();
					premited = false;
					System.out.println("please input data of the correct type  \n Die in a hole");
				}
				
				if(premited) {
					//This code adds the user input to the ArrayList of events
					events.add(new ArrayList<String>());
					events.get(events.size()-1).add(nameField.getText());
					if(isAllDay) {
						events.get(events.size()-1).add("true");
						events.get(events.size()-1).add("1:00");
						events.get(events.size()-1).add("23:59");
					}
					else {
						events.get(events.size()-1).add("false");
						events.get(events.size()-1).add(startField.getText());
						events.get(events.size()-1).add(endField.getText());
					}
					events.get(events.size()-1).add(String.valueOf(thisDay));
					events.get(events.size()-1).add(String.valueOf(monthIndex+1));

				}else {System.out.println("Please input logical data");}
			}
			for(int i = 0; i< thisDayEvents.size(); i++) {
				for(int  j = 0; j< events.size(); j++) {
					if(thisDayEvents.get(i).equals(events.get(j))) {
						events.set(j, new ArrayList<String>());
						events.get(j).add(eventEditingFields.get(i).get(0).getText());
						if(eventEditingFields.get(i).get(0).getText().equals("true")) {
							events.get(j).add("true");
							events.get(j).add("1:00");
							events.get(j).add("23:59");
						}
						else {
							events.get(j).add("false");
							events.get(j).add(eventEditingFields.get(i).get(2).getText());
							events.get(j).add(eventEditingFields.get(i).get(3).getText());
						}
						events.get(j).add(String.valueOf(thisDay));
						events.get(j).add(String.valueOf(monthIndex+1));
					}
				}
			}
			//end of else
		}
	}
	
	/*
	 * This gets the cell based off some of the static variables and the provided day as an integer
	 */
	public static int[] getCell(int selectedDay) {
		int[] cell = {0,0};
		int arbitrary = today%7;
		int index = 0;

		while(!daysList[index].equals(day)) {
			index++;
		}
		if (arbitrary == 0) {index++;}
		while(arbitrary > 1) {
			arbitrary--;
			index--;
			if(index ==-1) index+=7;
		}
		cell[0] = (selectedDay+index-1)%7;
		cell[1]=(selectedDay+index)/7;
		return cell ;
	}




	public static void main(String[]args) throws IOException{

		LocalDateTime sysDate = LocalDateTime.now(); 
		// DateTimeFormatter formatedThing = DateTimeFormatter.ofPattern("E, MMM dd yyyy HH:mm:ss"); //This is an example with all values
		//DateTimeFormatter formatedThing = DateTimeFormatter.ofPattern("E, MMM dd yyyy ");  
		DateTimeFormatter tempDay = DateTimeFormatter.ofPattern("E");
		DateTimeFormatter tempMonth = DateTimeFormatter.ofPattern("MMM");
		DateTimeFormatter tempToday = DateTimeFormatter.ofPattern("dd");

		day = sysDate.format(tempDay);  
		month = sysDate.format(tempMonth);
		today = Integer.parseInt(sysDate.format(tempToday));
		while(!month.equals(months[0][monthIndex])) {
			monthIndex = monthIndex+1;
		}
		month = "Nov";
		day = "Sat";
		today=1;
		monthIndex=10; //number of the month -1
		//Code to derive the first day of the month
		int arbitrary = today%7;
		int index = 0;

		while(!daysList[index].equals(day)) {
			index++;
		}
		if (arbitrary == 0) {index++;}
		while(arbitrary > 1) {
			arbitrary--;
			index--;
			if(index ==-1) index+=7;
		}

		//Adds the names of the days as the first row of the ArrayList
		calendarLabels.add(new ArrayList<String>());
		for (int i = 0; i < daysList.length; i++) {

			calendarLabels.get(0).add(daysList[i]);
		}

		// create the first row of the month view calendar
		calendarLabels.add(new ArrayList<String>());
		for(int i = 0; i<index;i++) {
			calendarLabels.get(1).add("");
		}
		for(int i = 1; i<(8-index);i++) {
			calendarLabels.get(1).add(String.valueOf(i));
		}

		//adds the remaining 5 rows
		int calendarOffset = Integer.parseInt(calendarLabels.get(1).get(6));
		int whatDisNum = 1;
		for(int i = 1; i<6;i++) {
			calendarLabels.add(new ArrayList<String>());
			for(int j =1; j<8;j++) {
				if(j+calendarOffset>daysOfMonth[monthIndex]) {
					calendarLabels.get(i+1).add(String.valueOf(whatDisNum));
					whatDisNum++;
				}
				else {calendarLabels.get(i+1).add(String.valueOf(j+calendarOffset));}

			}
			calendarOffset = calendarOffset + 7;
		}

		//Initialize the CSV reader and copies everything into an ArrayList

		BufferedReader reader = new BufferedReader(new FileReader(filePath));
		String line; 
		int offset = 0;

		while((line = reader.readLine()) != null) {
			String values[] = line.split(";");
			events.add(new ArrayList<String>());
			for(int i = 0; i < values.length; i++) {
				events.get(offset).add(values[i]);;
			}
			offset++;
		}

		//JFrame initialization

		JFrame frame = new JFrame(month);


		JPanel panel = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);  
				g.setColor(new Color(0,0,0));
				int xPos = 0;
				int yPos = 0;

				
				for (int i = 0; i < 8; i++) {
					xPos=0+(i*80);
					g.drawRect(xPos,yPos,5,520);
					g.fillRect(xPos,yPos,5,520);  

				}
				xPos =0;
				g.drawRect(xPos, yPos, 560, 5);
				g.fillRect(xPos, yPos, 560, 5);
				for(int i = 1; i <8; i++) {
					yPos = 0 + (i*80-40);
					g.drawRect(xPos, yPos, 565, 5);
					g.fillRect(xPos, yPos, 565, 5);
				}
				yPos=0;
			}
		};
		frame.getContentPane().add(panel);
		panel.setBackground(new Color(112, 150, 255));
		frame.setSize(1000, 700);
		frame.setVisible(true);
		/*
		 *This overrides the default window listener when the window closes and stops the while loop 
		 */
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				isRunning = false;

			}
		});
		panel.addMouseListener(normalMouseListener);
		//This overrides the default mouse listener and logs the X and Y coordinates of the mouse on the JPanel when the mouse is clicked
		
		panel.repaint(); 

		//I don't know where to put this, but it is needed to have it be the right size
		trashIcon.setImage(trashIcon.getImage().getScaledInstance(25,25,Image.SCALE_DEFAULT));
		//This code adds the day numbers to the calendar 
		Font smallFont = new Font("Arial",Font.PLAIN, 10);
		positionOfLabels.add(new ArrayList<JLabel>()); 
		//adds the day names
		for(int i = 0; i <7; i++) {
			positionOfLabels.get(0).add(new JLabel(daysList[i]));
			positionOfLabels.get(0).get(i).setBounds(10+80*i,10,40,20); 
			positionOfLabels.get(0).get(i).setFont(smallFont);
			panel.add(positionOfLabels.get(0).get(i)); 
		}
		addLabels(panel);
		

		while(isRunning) {
			//The wait saves resources and lets the mouse input work, without it the if statement will never work
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}	
			if(mouseClicked) {
				ArrayList<Integer> selectedBox = new ArrayList<Integer>();	

				if(mouseX < 572 && mouseX > 8 && mouseY <555 && mouseY >75) {
					for(int i = 0; i <8; i++) { 
						if (mouseX > 13+i*80 && mouseX < 12+(i+1)*80) { 
							selectedBox.add(i);
						}
					}
					for(int i = 0; i < 8; i++) {
						if(mouseY > (i*80+75) && mouseY < ((i+1)*80+74)) {
							selectedBox.add(i);
						}
					}
					if(selectedBox.size() ==2) {

						int[] selectDayInput = {selectedBox.get(0),selectedBox.get(1)};
						selectDay(selectDayInput);
						addLabels(panel);
					}

				}else {
					addLabels(panel);
				}


				selectedBox.clear();
				mouseClicked = false;
			}
		}
		PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(filePath)));
		for(int i = 0; i< events.size(); i++) {
			
			for(int j =0; j<events.get(i).size(); j++) {
				writer.append(events.get(i).get(j)+";");
			}
			writer.append(System.lineSeparator());
		}
		writer.close();
		reader.close();	
		System.out.println("program ended");
		System.exit(0);






	}
}

