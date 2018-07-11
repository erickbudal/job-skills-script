package web.scraping.scripts;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class GlassdoorScript {
    private String inputFilePath;
    private String outputFilePath;
    private String searchKeyword;
    private int locationId; //1 = United States; 2 = United Kingdom; 3 = Canada; 36 = Brazil; 96 = Germany; 123 = Japan;

    public GlassdoorScript(String inputFilePath, String outputFilePath, String searchKeyword, int locationId) {
        this.inputFilePath = inputFilePath;
        this.outputFilePath = outputFilePath;
        this.searchKeyword = searchKeyword;
        this.locationId = locationId;
    }

    public void start() {
        String initialURL;
        final int TOTAL_PAGES = 30; //Max possible page is 30 because of a bug of Glassdoor
        int jobCounter = 0;
        int exceptions = 0;
        String jobListUrl;
        HashMap<String, Integer> searchTerms;

        searchKeyword = searchKeyword.toLowerCase().replace(" ", "+");

        try {
            initialURL = "https://www.glassdoor.com/Job/jobs.htm?suggestCount=0&suggestChosen=false&clickSource=searchBtn&typedKeyword=" + searchKeyword + "&sc.keyword=" + searchKeyword + "&locT=N&locId=" + locationId + "&jobType=";
            Document jobListInitialPage = Jsoup.connect(initialURL).get();
            String selector = "#FooterPageNav > div > ul > li:nth-child(3) > a";
            String url = "https://www.glassdoor.com" + jobListInitialPage.select(selector).attr("href");
            jobListUrl = url.replaceAll("IP\\d+\\.htm", "IP%s.htm");
        }
        catch (Exception ex){
            ex.printStackTrace();
            return;
        }

        searchTerms = Util.loadSearchTerms(inputFilePath);
        System.out.println("Loaded " + searchTerms.size() + " search terms.");

        for (int i = 1; i <= TOTAL_PAGES; i++) {
            try {
                System.out.println("Current page: " + i + "/" + TOTAL_PAGES);

                Document jobListPage = Jsoup.connect(String.format(jobListUrl, i)).get();
                System.out.println("    " + jobListPage.location());

                for (int j = 1; j < 30; j++) {
                    String selector = "#MainCol > div > ul > li:nth-child(" + (j + 1) + ") > div:nth-child(2) > div:nth-child(1) > div:nth-child(1) > a";
                    Element linkTag = jobListPage.select(selector).get(0);
                    String jobUrl = linkTag.attr("href").replace("amp;", "");
                    Document jobPage = Jsoup.connect("https://www.glassdoor.com" + jobUrl).get();

                    String jobDescription;
                    try {
                        jobDescription = jobPage.getElementsByClass("jobDescriptionContent desc module pad noMargBot").get(0).text().toLowerCase();
                    } catch (Exception ex) {
                        System.out.println("    This job announcement is not from Glassdoor. It will be ignored. " + jobPage.location());
                        continue;
                    }

                    Elements jobTitleTag = jobPage.getElementsByClass("noMargTop margBotXs strong");
                    if(jobTitleTag.size() == 0){
                        jobTitleTag = jobPage.getElementsByClass("noMargTop margBotSm strong");
                    }

                    System.out.println("    #" + (jobCounter + 1) + " " + jobTitleTag.get(0).text() + " (" + jobPage.location() + ")");

                    for (String termsLine : searchTerms.keySet()) {
                        String regexFindTerm = Util.getFindTermRegex(termsLine);
                        if (jobDescription.matches(regexFindTerm)) {
                            searchTerms.put(termsLine, searchTerms.get(termsLine) + 1);
                            //System.out.println("        Found " + termsLine);
                        }
                    }

                    jobCounter++;
                }

            }
            catch(Exception ex){
                ex.printStackTrace();
                exceptions++;
            }
        }

        System.out.println("\n========================================");
        System.out.println("Saving collected data to a csv file.");
        System.out.println("========================================\n");

        LinkedHashMap<String, Integer> termsFrequencySorted = Util.sortHashMapByValues(searchTerms);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath, true))) {
            for (String termSearched : termsFrequencySorted.keySet()) {
                writer.write(termSearched + ";" + searchTerms.get(termSearched) + ";" + String.format("%.2f", searchTerms.get(termSearched) * 100.0 / jobCounter) + "%");
                writer.newLine();
            }
            String todayDate = new SimpleDateFormat("dd/MM/yy").format(new Date());
            writer.write("\nTotal:;" + jobCounter);
            writer.write("\nDate:;" + todayDate.toString());
            writer.write("\nLocationID:;" + locationId);
            writer.write("\nSearch Keyword:;" + searchKeyword);
        } catch (Exception e) {
            exceptions++;
            e.printStackTrace();
        }

        System.out.println("=====================================");
        System.out.println("Finished with " + exceptions + " exceptions.");
        System.out.println("=====================================\n");
    }

}
