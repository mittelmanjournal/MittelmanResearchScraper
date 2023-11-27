package Main;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.pushingpixels.substance.api.skin.SubstanceGraphiteLookAndFeel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScraperApp {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JFrame frame;
	private JPanel formPanel;
	private JTextField linkInput, keywordInput, outputPathInput;
	private JComboBox<String> extractionType;
	private JCheckBox getDatesToggle;
	
	private JCheckBox includeCompiledSortedTimelineToggle;
	
	private JCheckBox strictTimelineToggle;
	boolean strictTimeline;
	
	private JButton startButton;
	private JScrollPane scrollPane;
	
	String links;
	String keywords;
	String extractionTypeSelected;
	String outputPath;
	boolean getDates;
	
	private ArrayList<String> stringsToBeSortedByDate;
	FileWriter compiledSortedTimelineWriter;
	boolean includeCompiledSortedTimeline;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ScraperAppTesting window = new ScraperAppTesting();
					window.frame.pack();
					window.frame.setLocationRelativeTo(null);
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public ScraperAppTesting() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		try {
			UIManager.setLookAndFeel(new SubstanceGraphiteLookAndFeel());
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}

		frame.setTitle("Mittelman's Keyword & Date Scraper");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		ImageIcon icon = new ImageIcon("logo.png");
		frame.setIconImage(icon.getImage());
		frame.setPreferredSize(new Dimension(800, 300));
		
		formPanel = new JPanel(new GridBagLayout());
		formPanel.setBackground(Color.WHITE); // Set background color for the form panel
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);

		// Labels
		gbc.gridx = 0;
		gbc.gridy = 0;
		
		formPanel.add(new JLabel("List of Links (line separated, .txt file):"), gbc);

		gbc.gridy++;
		formPanel.add(new JLabel("List of Keywords (comma separated, .txt file):"), gbc);

		gbc.gridy++;
		formPanel.add(new JLabel("Extraction Type:"), gbc);

		gbc.gridy++;
		formPanel.add(new JLabel("Output Folder Path (ends with '\\'):"), gbc);

		// Text Inputs
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 3;
		linkInput = new JTextField(20);
		linkInput.setPreferredSize(new Dimension(300, 25));
		formPanel.add(linkInput, gbc);

		gbc.gridy++;
		keywordInput = new JTextField(20);
		keywordInput.setPreferredSize(new Dimension(300, 25));
		formPanel.add(keywordInput, gbc);

		gbc.gridy++;
		String[] extractionTypes = { "Sentence", "Paragraph"};
		extractionType = new JComboBox<>(extractionTypes);
		extractionType.setPreferredSize(new Dimension(300, 25));
		formPanel.add(extractionType, gbc);

		gbc.gridy++;
		outputPathInput = new JTextField(20);
		outputPathInput.setPreferredSize(new Dimension(300, 25));
		formPanel.add(outputPathInput, gbc);

		//add date range input here
		
		// Toggle for Dates
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 2;
		getDatesToggle = new JCheckBox("Get Dates");
		formPanel.add(getDatesToggle, gbc);

		
		gbc.gridx = 1;
		includeCompiledSortedTimelineToggle = new JCheckBox("Sorted Timeline");
		formPanel.add(includeCompiledSortedTimelineToggle, gbc);
		
		//restrict timeline toggle here
		gbc.gridx = 3;
		strictTimelineToggle = new JCheckBox("Restrict Timeline");
		formPanel.add(strictTimelineToggle, gbc);
		
		// Button
		gbc.gridy++;
		gbc.gridx = 0;
		gbc.gridwidth = 0;
		startButton = new JButton("Start Scraping");
		startButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				startScraping();
			}
		});
		formPanel.add(startButton, gbc);

		scrollPane = new JScrollPane(formPanel);
		frame.add(scrollPane, BorderLayout.CENTER);

		
	}
	/**
	 * Update our instance variables
  	 * call scrape func
	 */
	private void startScraping() {
		// Implement scraping logic here based on the input values
		links = linkInput.getText();
		keywords = keywordInput.getText();
		extractionTypeSelected = (String) extractionType.getSelectedItem();
		outputPath = outputPathInput.getText();
		getDates = getDatesToggle.isSelected();

		includeCompiledSortedTimeline = includeCompiledSortedTimelineToggle.isSelected();
		if (includeCompiledSortedTimeline && getDates) {
			stringsToBeSortedByDate = new ArrayList<String>();
			try {
				compiledSortedTimelineWriter = new FileWriter(outputPath + "CompiledSortedTimeline.txt");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		strictTimeline = strictTimelineToggle.isSelected();

		scrapeAndWriteEachLinkToFile(getLinksFromFile(links), outputPath, getKeywordsFromFile(keywords),
				extractionTypeSelected, getDates, includeCompiledSortedTimeline, stringsToBeSortedByDate,
				compiledSortedTimelineWriter, strictTimeline);

	}

	/**
	 * Create a file for each link with each one containing sentences or paragraphs that contain any of the keywords and/or dates seen in that wiki page
	 */
	public static void scrapeAndWriteEachLinkToFile(ArrayList<String> linksToScrape, String outputPath,
			ArrayList<String> keywords, String extractionTypeSelected, boolean getDates,
			boolean includeCompiledSortedTimeline, ArrayList<String> stringsToBeSortedByDate,
			FileWriter compiledSortedTimelineWriter, boolean strictTimeline) {
		// for every link, create a document that has each sentence (or other type) that
		// contains any of the keywords in the document
		for (int i = 0; i < linksToScrape.size(); i++) {
			String writeText = "";
			String url = "https://en.wikipedia.org/wiki/" + linksToScrape.get(i);
			Document document = null;
			try {
				document = Jsoup.connect(url).get();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			Element heading = document.selectFirst("h1");

			try (FileWriter textWriter = new FileWriter(outputPath + heading.text() + ".txt")) { //maybe instead of heading name of file be linksToScrape.get(i)
				Element bodyContent = document.getElementById("bodyContent");
				Element contentText = bodyContent.getElementById("mw-content-text");
				Element contentTags = contentText.selectFirst("div.mw-content-ltr.mw-parser-output");
				// Elements paragraphs = e2.select("p");
				Element currentTag = contentTags.selectFirst("p");
				while (currentTag != null) {
					if (currentTag.tagName().equals("p")) {
						for (String keyword : keywords) {
							if (keyword != null && !keyword.equals("") && currentTag.text().contains(keyword)) {
								if (extractionTypeSelected.equals("Sentence")) {
									for (String sentence : getSentences(currentTag.text(), keyword)) {
										if (!writeText.contains(sentence)) {
											writeText += sentence + "\n\n";
										}
									}
								} else if (extractionTypeSelected.equals("Paragraph")) {
									if (!writeText.contains(currentTag.text())) {
										writeText += currentTag.text() + "\n\n";
									}
								}
							}
						}

						if (containsDateInformation(currentTag.text(), getDates)) {
							// sentences
							if (extractionTypeSelected.equals("Sentence")) {
								for (String sentence : getSentences(currentTag.text())) {
									if (!writeText.contains(sentence)) {
										if(includeCompiledSortedTimeline) {
											stringsToBeSortedByDate.add(url.replace("https://en.", "") + ": " + sentence);
										}
										writeText += sentence + "\n\n";
									}
								}
							} // paragraphs
							else if (extractionTypeSelected.equals("Paragraph")) {
								if (!writeText.contains(currentTag.text())) {
									if(includeCompiledSortedTimeline) {
										stringsToBeSortedByDate.add(url.replace("https://en.", "") + ": " + currentTag.text());
									}
									writeText += currentTag.text() + "\n\n";
								}
							}

						}

					} // before this line, check if the text contains any of the keywords -> only if
						// contained in the P, then find sentence it is in
					currentTag = currentTag.nextElementSibling();
				}
				textWriter.write(writeText);
				textWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		//need to check getDates truthness too because if not true, and includeCST is true, we could be adding to a null arraylist and writing to a null filewriter.
		if (includeCompiledSortedTimeline && getDates) {
			try {
				//here if strict timeline selected then
				if(strictTimeline) {
					for (String s : extraStrictSort(sortByYear(stringsToBeSortedByDate))) {
						compiledSortedTimelineWriter.write(s + "\n\n");
					}
				} else {
					for (String s : sortYearsAndMonths(stringsToBeSortedByDate)) {
						compiledSortedTimelineWriter.write(s + "\n\n");
					}
				}

				compiledSortedTimelineWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Locate a file at filepath and line by line add each string to an array list and return that array list
	 */
	public static ArrayList<String> getLinksFromFile(String filePath) {
		ArrayList<String> links = new ArrayList<String>();
		String linksFilePath = filePath;

		Path path = Paths.get(linksFilePath);

		try (BufferedReader reader = Files.newBufferedReader(path)) {
			if (Files.exists(path)) {
				System.out.println("File exists at path: " + path.toAbsolutePath());

				String line;
				while ((line = reader.readLine()) != null) {
					line = line.trim();
					line = line.replace("https://en.wikipedia.org/wiki/", "");
					links.add(line);
				}

			} else {
				System.out.println("File does not exist at this path.");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return links;
	}

	/**
	 * Get each keyword string as an item in an array list by splitting a text file's string by commas
	 */
	public static ArrayList<String> getKeywordsFromFile(String keywordsFilePath) {
		if (!keywordsFilePath.equals("") && keywordsFilePath != null) {
			File file = new File(keywordsFilePath);

			try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
				StringBuilder content = new StringBuilder();
				String line;

				// Read the entire file into a string
				while ((line = reader.readLine()) != null) {
					content.append(line);
				}

				// Split the string by commas and store in an ArrayList
				ArrayList<String> dataList = new ArrayList<String>(Arrays.asList(content.toString().split(",")));
				for (int i = 0; i < dataList.size(); i++) {
					dataList.set(i, dataList.get(i).trim());
				}
				for (int i = 0; i < dataList.size(); i++) {
					if (dataList.get(i).equals("")) {
						dataList.remove(i);
						i--;
					}
				}

				return dataList;

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return new ArrayList<String>();
	}

	public static ArrayList<String> getSentences(String paragraph) {
		ArrayList<String> sentences = new ArrayList<String>();

		// Split paragraph into sentences using regex
		String[] sentenceArray = paragraph.split("[.!?]");

		// Check each sentence for the presence of the keyword
		for (String sentence : sentenceArray) {
			if (containsDateInformation(sentence, true)) { // we already know its true because we only call this func from a place where it must be previously true to reach
				sentences.add(sentence.trim()); // trim() removes leading and trailing whitespaces
			}
		}

		return sentences;
	}
	
	public static ArrayList<String> getSentences(String paragraph, String keyword) {
		ArrayList<String> sentences = new ArrayList<String>();

		// Split the paragraph into sentences using a simple regex
		String[] sentenceArray = paragraph.split("[.!?]");

		// Check each sentence for the presence of the keyword
		for (String sentence : sentenceArray) {
			if (containsKeyword(sentence, keyword)) {
				sentences.add(sentence.trim()); // trim() removes leading and trailing whitespaces
			}
		}

		return sentences;
	}

	private static boolean containsKeyword(String sentence, String keyword) {
		// Using a case-insensitive search for the keyword in the sentence
		Pattern pattern = Pattern.compile("\\b" + Pattern.quote(keyword) + "\\b", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(sentence);

		return matcher.find();
	}

	public static boolean containsDateInformation(String input, boolean getDates) {
		if (getDates) {
			// Regular expressions for weekdays, months, and years
			String weekdaysRegex = "\\b(Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday)\\b";
			String monthsRegex = "\\b(January|February|March|April|May|June|July|August|September|October|November|December)\\b";
			String yearsRegex = "\\b(\\d{4})\\b";

			// Combine the regex patterns
			String combinedRegex = String.join("|", weekdaysRegex, monthsRegex, yearsRegex);

			// Create a pattern and matcher
			Pattern pattern = Pattern.compile(combinedRegex);
			Matcher matcher = pattern.matcher(input);

			// Return true if any match is found
			return matcher.find();
		}
		return false;
	}

	public static ArrayList<String> sortYearsAndMonths(ArrayList<String> stringsToBeSortedByDate) {
		// Create a custom comparator for sorting
        Comparator<String> yearComparator = Comparator.comparingInt(ScraperAppTesting::extractYear)
                .thenComparing(Comparator.naturalOrder());

        // Sort the list using the custom comparator
        stringsToBeSortedByDate.sort(yearComparator);

        return stringsToBeSortedByDate;
    }

    private static Integer extractYear(String input) {
    	Pattern yearPattern = Pattern.compile("\\b(\\d{4})\\b");
        Matcher matcher = yearPattern.matcher(input);

        if (matcher.find()) {
            return Integer.parseInt(matcher.group());
        }

        // Return a large value for strings without a year
        return Integer.MAX_VALUE;
    }

	public static boolean anyStringEqualsIgnoreCase(String str, String[] strs) {
		for(String s : strs) {
			if(str.equalsIgnoreCase(s)) {
				return true;
			}
		}
		return false;
	}
	
    public static ArrayList<String> sortByYear(List<String> inputList) {
        ArrayList<String> withYear = new ArrayList<>();
        //List<String> withoutYear = new ArrayList<>();

        // Separate strings with years from those without years
        for (String str : inputList) {
            if (containsYear(str)) {
                withYear.add(str);
            }/* else {
                withoutYear.add(str);
            }*/
        }

        // Sort strings with years
        withYear.sort((str1, str2) -> extractYear(str1) - extractYear(str2));
//        onlyYears.addAll(withYear);
//        // Combine the sorted lists
//        ArrayList<String> sortedList = new ArrayList<>(withYear);
//        sortedList.addAll(withoutYear);

        return withYear;
    }

    private static boolean containsYear(String input) {
        Pattern yearPattern = Pattern.compile("\\b(\\d{4})\\b");
        Matcher matcher = yearPattern.matcher(input);
        return matcher.find();
    }
	
    public static ArrayList<String> extraStrictSort(ArrayList<String> sentences) {
		ArrayList<String> strictList = new ArrayList<String>();
		ArrayList<String> bceList = new ArrayList<String>();
		
		//iterate each sentence/paragraph in the collected date strings list
	  	for(int i = 0; i < sentences.size(); i++) {
	  		boolean proceedToNextSentence = false;
	  		
	  		//split sentence/para into word array ( "xyz," type words may exist )
    		String[] words = sentences.get(i).trim().split(" ");
    		
    		//iterate until finding idx position of first year in string (should work on "YYYY," type strings)
    		int j = 0;
    		while(j < words.length) {    			
    			if(containsYear(words[j])) {
    				break;
    			}
    			j++;
    		}
    		//if j == word.length, year for some reason not present (should always be present as 
    		//sortByYear is called before this method
			int idxOfYear = j; // save year array index
			int curWord = idxOfYear + 1;

			// "BCE", "BC", "B.C", "B.C.", "AD", "A.D", "A.D." within 2 (arbitrary) words to
			// the right of the year's index/position in the string
			int wordsToRight = 1;
			while (curWord < words.length && wordsToRight <= 2) {	
				if (anyStringEqualsIgnoreCase(words[curWord], new String[] { "BCE", "BC", "B.C.", "B.C", "BC.", "BC,", "BC)" }) && wordsToRight <= 2) {
					bceList.add(sentences.get(i));
					proceedToNextSentence = true;
					break;
				} else if (anyStringEqualsIgnoreCase(words[curWord], new String[] { "AD", "A.D", "A.D.", "AD.", "AD,", "AD)" })	&& wordsToRight <= 2) {	
					strictList.add(sentences.get(i));
					proceedToNextSentence = true;
					break;
				}
				wordsToRight++;
				curWord++;
			}
			if(proceedToNextSentence) continue;
			
			// in this while loop we are checking if to the left of the year "on" or "in" appear
			int wordsToLeft = 1;
			curWord = idxOfYear - 1;
    		while(curWord >= 0 && wordsToLeft <= 4) {
    			if((words[curWord].equalsIgnoreCase("in") || words[curWord].equalsIgnoreCase("since"))&& wordsToLeft <= 2) {
    				//this means case is: "In 2022" or "In January 2022", and is a valid case
    				strictList.add(sentences.get(i));
    				break;
    			}
    			
    			if(words[curWord].equalsIgnoreCase("on") && wordsToLeft <= 4) {
    				//this means case is: "On Monday, January 1, 2022", or "On January 1, 2022" and is a valid case
    				strictList.add(sentences.get(i));
    				break;
    			}
    			wordsToLeft++;
    			curWord--; //adjust word left 1 
    		}
    	}
	  	
	  	Collections.reverse(bceList);
	  	strictList.addAll(0, bceList);
	  	return strictList;
    }
}
