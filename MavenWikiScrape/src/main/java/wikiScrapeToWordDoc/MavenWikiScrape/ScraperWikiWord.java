package wikiScrapeToWordDoc.MavenWikiScrape;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

//import org.apache.poi.wp.usermodel.HeaderFooterType;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRelation;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHyperlink;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTText;
import org.pushingpixels.substance.api.skin.SubstanceGraphiteLookAndFeel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import org.apache.poi.xwpf.usermodel.*;

public class ScraperWikiWord {

	private static final long serialVersionUID = 1L;
	private JFrame frame;
	private JPanel formPanel;
	private JTextField linkInput, keywordInput, outputPathInput, yearRangeInput;
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
	String yearRange;
	boolean getDates;
	
	private ArrayList<String> stringsToBeSortedByDate = new ArrayList<String>();
	boolean includeCompiledSortedTimeline;
	
	public ScraperWikiWord() {
		initialize();
	}
	
	public static void main(String[] args) throws Exception {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ScraperWikiWord window = new ScraperWikiWord();
					window.frame.pack();
					window.frame.setLocationRelativeTo(null);
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

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
		
		gbc.gridy++;
		formPanel.add(new JLabel("Year Range (YYYY-YYYY):"), gbc);
		
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

		gbc.gridy++;
		yearRangeInput = new JTextField(20);
		yearRangeInput.setPreferredSize(new Dimension(300, 25));
		formPanel.add(yearRangeInput, gbc);
		
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
	
	private void startScraping() {
		// Implement scraping logic here based on the input values
		links = linkInput.getText();
		keywords = keywordInput.getText();
		extractionTypeSelected = (String) extractionType.getSelectedItem();
		outputPath = outputPathInput.getText();
		yearRange = yearRangeInput.getText();
		getDates = getDatesToggle.isSelected();

		includeCompiledSortedTimeline = includeCompiledSortedTimelineToggle.isSelected();
		
		strictTimeline = strictTimelineToggle.isSelected();

		scrapeAndWriteEachLinkToFile(getLinksFromFile(links), outputPath, getKeywordsFromFile(keywords),
				extractionTypeSelected, getDates, includeCompiledSortedTimeline, stringsToBeSortedByDate,
				strictTimeline, yearRange);

	}
	
	public static void scrapeAndWriteEachLinkToFile(ArrayList<String> linksToScrape, String outputPath,
			ArrayList<String> keywords, String extractionTypeSelected, boolean getDates,
			boolean includeCompiledSortedTimeline, ArrayList<String> stringsToBeSortedByDate, boolean strictTimeline,
			String yearRange) {

		HashMap<String, HashSet<String>> linksToStrings = new HashMap<String, HashSet<String>>();
		for (int i = 0; i < linksToScrape.size(); i++) {
			String writeText = "";
			String url = "https://en.wikipedia.org/wiki/" + linksToScrape.get(i);
			Document document = null;
			try {
				document = Jsoup.connect(url).get();
			} catch (IOException e1) {
				System.out.println("COULDNT CONNECT TO DOCUMENT, DO SOMETHING HERE TO TRY TO QUERY WIKI FOR TOPIC");
				//1. take the linkstoscrape i
				//2. put it into wiki querier
				//3. select first appearing article
				//4. somehow denote that string was corrected/or not do whatever bro
				//5. if nothing comes up then just do not make this one be scraped
				e1.printStackTrace();
				continue;
			}

			try (XWPFDocument wordDoc = new XWPFDocument()) {
				Element bodyContent = document.getElementById("bodyContent");
				Element contentText = bodyContent.getElementById("mw-content-text");
				Element contentTags = contentText.selectFirst("div.mw-content-ltr.mw-parser-output");
				// Elements paragraphs = e2.select("p");
				Element currentTag = contentTags.selectFirst("p");
				
				String currentHeading = "";
				
				while (currentTag != null) {
					if(currentTag.tagName().equals("h2") || currentTag.tagName().equals("h3") || currentTag.tagName().equals("h4")) {
						currentHeading = currentTag.text();
					}
					String link = url + "#" + currentHeading.trim().replaceAll(" ", "_").replaceAll("\"", "%22");
					if (currentTag.tagName().equals("p") && containsDateInformation(currentTag.text(), getDates)) {
						// sentences
						if (extractionTypeSelected.equals("Sentence")) {
							for (String sentence : getSentences(currentTag.text())) {
								if (!writeText.contains(sentence)) {
									
									if (includeCompiledSortedTimeline) {
										stringsToBeSortedByDate.add(sentence);
										
										if (!linksToStrings.containsKey(link)) {
											linksToStrings.put(link, new HashSet<String>());
										}
										linksToStrings.get(link).add(sentence);
									}
									writeText += sentence + "\n\n";
									XWPFParagraph para = wordDoc.createParagraph();
									addClickableHyperlink(para, sentence, link);
								}
							}
						} // paragraphs
						else if (extractionTypeSelected.equals("Paragraph")) {
							if (!writeText.contains(currentTag.text())) {
								if (includeCompiledSortedTimeline) {
									stringsToBeSortedByDate.add(currentTag.text());
									if (!linksToStrings.containsKey(link)) {
										linksToStrings.put(link, new HashSet<String>());
									}
									linksToStrings.get(link).add(currentTag.text());
								}
								writeText += currentTag.text() + "\n\n";
								XWPFParagraph para = wordDoc.createParagraph();
								addClickableHyperlink(para, currentTag.text(), link);
							}
						}

					}
					
					if (currentTag.tagName().equals("p")) {
						for (String keyword : keywords) {
							if (keyword != null && !keyword.equals("") && currentTag.text().contains(keyword)) {
								if (extractionTypeSelected.equals("Sentence")) {
									for (String sentence : getSentences(currentTag.text(), keyword)) {
										if (!writeText.contains(sentence)) {
											writeText += sentence + "\n\n";
											XWPFParagraph para = wordDoc.createParagraph();
											addClickableHyperlink(para, sentence, link);
										}
									}
								} else if (extractionTypeSelected.equals("Paragraph")) {
									if (!writeText.contains(currentTag.text())) {
										writeText += currentTag.text() + "\n\n";
										XWPFParagraph para = wordDoc.createParagraph();
										addClickableHyperlink(para, currentTag.text(), link);
									}
								}
							}
						}
					} // before this line, check if the text contains any of the keywords -> only if
						// contained in the P, then find sentence it is in
					currentTag = currentTag.nextElementSibling();
				}
				FileOutputStream out = new FileOutputStream(outputPath + document.selectFirst("h1").text() + ".docx");
				wordDoc.write(out);
				out.close();
				wordDoc.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if (includeCompiledSortedTimeline && getDates) {
			try {
				ArrayList<String> arr = new ArrayList<String>();
				// here if strict timeline selected then
				if (strictTimeline) {
					arr = extraStrictSort(sortByYear(stringsToBeSortedByDate));
				} else {
					arr = sortYearsAndMonths(stringsToBeSortedByDate);
				}

				// handle range stuff on arr
				if (!yearRange.trim().equals("") && yearRange != null && containsYear(yearRange)) {
					arr = limitTimelineRange(arr, yearRange);
					
				}
				
				XWPFDocument timelineWordDoc = new XWPFDocument();
				
				//System.out.println(linksToStrings);
				ArrayList<String> ifStringContainedHereDontAdd = new ArrayList<String>();
				for (String s : arr) {
					//System.out.println("Sentence with year: "+s);
					for(String key : linksToStrings.keySet()) {
						//System.out.println(key);
						//System.out.println(linksToStrings.get(key));
						if(linksToStrings.get(key).contains(s) && !ifStringContainedHereDontAdd.contains(s)) {
							//System.out.println(true);
							XWPFParagraph para = timelineWordDoc.createParagraph();
							addClickableHyperlink(para, s, key);
							ifStringContainedHereDontAdd.add(s);
							break;
						}
						//System.out.println(false);
					}
					
				}
				FileOutputStream outTimeline = new FileOutputStream(outputPath + "Timeline.docx");
				timelineWordDoc.write(outTimeline);
				outTimeline.close();
				timelineWordDoc.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

//	static XWPFHyperlinkRun createHyperlinkRun(XWPFParagraph paragraph, String uri) throws Exception {
//    String encodedUri = URLEncoder.encode(uri, "UTF-8");
//    String rId = paragraph.getPart().getPackagePart()
//            .addExternalRelationship(encodedUri, XWPFRelation.HYPERLINK.getRelation()).getId();
//
//    CTHyperlink cthyperLink = paragraph.getCTP().addNewHyperlink();
//    cthyperLink.setId(rId);
//    cthyperLink.addNewR();
//
//    return new XWPFHyperlinkRun(cthyperLink, cthyperLink.getRArray(0), paragraph);
//}

	static XWPFHyperlinkRun createHyperlinkRun(XWPFParagraph paragraph, String uri) throws Exception {
		//String encodedUri = URLEncoder.encode(uri, "UTF-8");
		//must do your own encoding when seeing spaces convert to _ when " conv to %22 etc...
		String rId = paragraph.getPart().getPackagePart()
				.addExternalRelationship(uri, XWPFRelation.HYPERLINK.getRelation()).getId();

		CTHyperlink cthyperLink = paragraph.getCTP().addNewHyperlink();
		cthyperLink.setId(rId);
		cthyperLink.addNewR();

		return new XWPFHyperlinkRun(cthyperLink, cthyperLink.getRArray(0), paragraph);
	}

	static void addClickableHyperlink(XWPFParagraph paragraph, String text, String link) throws Exception {
// Clear existing runs in the paragraph
		for (int i = paragraph.getRuns().size() - 1; i >= 0; i--) {
			paragraph.removeRun(i);
		}
		link = link.replaceAll(" ", "_");
		XWPFHyperlinkRun hyperlinkrun = createHyperlinkRun(paragraph, link);
		hyperlinkrun.setText(text);
		hyperlinkrun.setColor("000000");
		hyperlinkrun.setUnderline(UnderlinePatterns.SINGLE);
		hyperlinkrun.setFontFamily("Times New Roman");
		hyperlinkrun.setFontSize(12);
	}

	public static ArrayList<String> limitTimelineRange(ArrayList<String> strict, String range) {
		System.out.println("Range: " + range + " r");
		ArrayList<String> ret = new ArrayList<String>();

		// if range not included, only 1 year input then assume it is lower and assume
		// upper is modern day
		int lower = ifBCNegate(range.trim());
		int upper = Integer.MAX_VALUE; // whatever modern day is either make this the modern year or integer.max

		if (range.contains("-")) { // only if range included then find rnage
			String[] bounds = range.split("-");
			lower = ifBCNegate(bounds[0].trim());
			upper = ifBCNegate(bounds[1].trim());
		}

		for (String s : strict) {
			if (ifBCNegate(s) >= lower && ifBCNegate(s) <= upper) {
				ret.add(s);
			}
		}
		return ret;
	}

	public static int ifBCNegate(String str) {
		String[] words = str.trim().split(" ");

		int j = 0;
		while (j < words.length) {
			if (containsYear(words[j])) {
				break;
			}
			j++;
		}
		int idxOfYear = j; // save year array index
		int curWord = idxOfYear + 1;
		int wordsToRight = 1;
		while (curWord < words.length && wordsToRight <= 2) {
			if (anyStringEqualsIgnoreCase(words[curWord],
					new String[] { "BCE", "BC", "B.C.", "B.C", "BC.", "BC,", "BC)" }) && wordsToRight <= 2) {
				return -1 * extractYear(str);
			}
			wordsToRight++;
			curWord++;
		}
		return extractYear(str);
	}

	/**
	 * Locate a file at filepath and line by line add each string to an array list
	 * and return that array list
	 */
	public static ArrayList<String> getLinksFromFile(String filePath) {
		ArrayList<String> links = new ArrayList<String>();

		Path path = Paths.get(filePath);

		if (Files.exists(path)) {
			System.out.println("File exists at path: " + path.toAbsolutePath());

			String line;
			try (BufferedReader reader = Files.newBufferedReader(path)) {
				while ((line = reader.readLine()) != null) {
					line = line.trim();
					line = line.replace("https://en.wikipedia.org/wiki/", "");
					links.add(line);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else {
			System.out.println("File does not exist at this path. Assume comma separated list inputted");
			for (String s : filePath.split(",")) {
				links.add(s.trim());
			}
		}
		return links;
	}

	/**
	 * Get each keyword string as an item in an array list by splitting a text
	 * file's string by commas
	 */	public static ArrayList<String> getKeywordsFromFile(String keywordsFilePath) {

		ArrayList<String> keywords = new ArrayList<String>();

		Path path = Paths.get(keywordsFilePath);
		System.out.println("kwfp"+keywordsFilePath);
		System.out.println(path);
		if (Files.exists(path) && !keywordsFilePath.equals("")) {
			System.out.println("File exists at path: " + path.toAbsolutePath());
			
			try (BufferedReader reader = Files.newBufferedReader(path)) {
				StringBuilder content = new StringBuilder();
				String line;

				// Read the entire file into a string
				while ((line = reader.readLine()) != null) {
					content.append(line);
				}

				// Split the string by commas and store in an ArrayList
				keywords = new ArrayList<String>(Arrays.asList(content.toString().split(",")));
				for (int i = 0; i < keywords.size(); i++) {
					keywords.set(i, keywords.get(i).trim());
				}
				for (int i = 0; i < keywords.size(); i++) {
					if (keywords.get(i).equals("")) {
						keywords.remove(i);
						i--;
					}
				}
			} catch (IOException e) {
				System.out.println("Keywords reader failed");
				e.printStackTrace();
				System.out.println();
			}

		} else {
			System.out.println("File does not exist at this path. Assume comma separated list inputted");
			for (String s : keywordsFilePath.split(",")) {
				keywords.add(s.trim());
			}
		}
		return keywords;
	}

	public static ArrayList<String> getSentences(String paragraph) {
		ArrayList<String> sentences = new ArrayList<String>();

		// Split paragraph into sentences using regex
		String[] sentenceArray = paragraph.split("[.!?]");

		// Check each sentence for the presence of the keyword
		for (String sentence : sentenceArray) {
			if (containsDateInformation(sentence, true)) { // we already know its true because we only call this func
															// from a place where it must be previously true to reach
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
		Comparator<String> yearComparator = Comparator.comparingInt(ScraperWikiWord::extractYear)
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
		for (String s : strs) {
			if (str.equalsIgnoreCase(s)) {
				return true;
			}
		}
		return false;
	}

	public static ArrayList<String> sortByYear(List<String> inputList) {
		ArrayList<String> withYear = new ArrayList<>();
		// List<String> withoutYear = new ArrayList<>();

		// Separate strings with years from those without years
		for (String str : inputList) {
			if (containsYear(str)) {
				withYear.add(str);
			} /*
				 * else { withoutYear.add(str); }
				 */
		}

		// Sort strings with years
		withYear.sort((str1, str2) -> extractYear(str1) - extractYear(str2));
//	        onlyYears.addAll(withYear);
//	        // Combine the sorted lists
//	        ArrayList<String> sortedList = new ArrayList<>(withYear);
//	        sortedList.addAll(withoutYear);

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

		// iterate each sentence/paragraph in the collected date strings list
		for (int i = 0; i < sentences.size(); i++) {
			boolean proceedToNextSentence = false;

			// split sentence/para into word array ( "xyz," type words may exist )
			String[] words = sentences.get(i).trim().split(" ");

			// iterate until finding idx position of first year in string (should work on
			// "YYYY," type strings)
			int j = 0;
			while (j < words.length) {
				if (containsYear(words[j])) {
					break;
				}
				j++;
			}
			// if j == word.length, year for some reason not present (should always be
			// present as
			// sortByYear is called before this method
			int idxOfYear = j; // save year array index
			int curWord = idxOfYear + 1;

			// "BCE", "BC", "B.C", "B.C.", "AD", "A.D", "A.D." within 2 (arbitrary) words to
			// the right of the year's index/position in the string
			int wordsToRight = 1;
			while (curWord < words.length && wordsToRight <= 2) {
				if (anyStringEqualsIgnoreCase(words[curWord],
						new String[] { "BCE", "BC", "B.C.", "B.C", "BC.", "BC,", "BC)" }) && wordsToRight <= 2) {
					bceList.add(sentences.get(i));
					proceedToNextSentence = true;
					break;
				} else if (anyStringEqualsIgnoreCase(words[curWord],
						new String[] { "AD", "A.D", "A.D.", "AD.", "AD,", "AD)", "CE", "C.E." }) && wordsToRight <= 2) {
					strictList.add(sentences.get(i));
					proceedToNextSentence = true;
					break;
				}
				wordsToRight++;
				curWord++;
			}
			if (proceedToNextSentence)
				continue;

			// in this while loop we are checking if to the left of the year "on" or "in"
			// appear
			int wordsToLeft = 1;
			curWord = idxOfYear - 1;
			while (curWord >= 0 && wordsToLeft <= 4) {
				if ((words[curWord].equalsIgnoreCase("in") || words[curWord].equalsIgnoreCase("since"))
						&& wordsToLeft <= 2) {
					// this means case is: "In 2022" or "In January 2022", and is a valid case
					strictList.add(sentences.get(i));
					break;
				}

				if (words[curWord].equalsIgnoreCase("on") && wordsToLeft <= 4) {
					// this means case is: "On Monday, January 1, 2022", or "On January 1, 2022" and
					// is a valid case
					strictList.add(sentences.get(i));
					break;
				}
				wordsToLeft++;
				curWord--; // adjust word left 1
			}
		}

		Collections.reverse(bceList);
		strictList.addAll(0, bceList);
		return strictList;
	}

}
